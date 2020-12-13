/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Hoshedar Irani
 */
public class Cache<K,V> implements Iterable<V>{
    private Map<K, Entry<K,V>> map;
    private Entry<K,V> header=new Entry(null,null);
    private int capacity;
    private boolean usedOnGet;
    private final FieldGetter fieldGetter;
    
    public static final char compoundKeySeparator=':';
    
    static final byte actionRemove=1;
    static final byte actionAdd=2;
    static final byte actionMoveToEnd=3;
    static final byte actionRemoveFirst=4;

    private AtomicReference<ConcurrentLinkedQueue<EntryAction<K,V>>> actionQueueReference=new AtomicReference();
    private ReentrantLock processQueueLock=new ReentrantLock();
    
    private String[][] secondaryKeys;
    private Map<Object,Entry<K,V>>[] secondaryMaps;
    
    private static class EntryAction<K,V>{
        byte action;
        Entry<K,V> entry;

        public EntryAction(byte action, Entry<K, V> entry) {
            this.action = action;
            this.entry = entry;
        }
    }
    
    private static class Entry<K,V>{
        final K key;
        final V value;
        volatile Entry<K,V> next=null;
        volatile Entry<K,V> previous=null;

        public Entry(K key,V value) {
            this.key=key;
            this.value = value;
        }        
    }
   
    private Cache(int initialCapacity,int capacity,boolean usedOnGet,FieldGetter fieldGetter,String[] secondaryKeyNames) {
        map=new ConcurrentHashMap(initialCapacity,0.75f,16);
        this.capacity=capacity;
        header.next=header.previous=header;
        this.usedOnGet=usedOnGet;
        this.fieldGetter=fieldGetter;
        if(secondaryKeyNames!=null && secondaryKeyNames.length>0){
            this.secondaryKeys=new String[secondaryKeyNames.length][];
            this.secondaryMaps=new Map[secondaryKeyNames.length];
            String[] compoundKeys;
            for(int ctr=0;ctr<secondaryKeys.length;ctr++){
                compoundKeys=split(secondaryKeyNames[ctr], compoundKeySeparator);
                this.secondaryKeys[ctr]=new String[compoundKeys.length];
                for(int ctr2=0;ctr2<compoundKeys.length;ctr2++)
                    this.secondaryKeys[ctr][ctr2]=compoundKeys[ctr2];
                this.secondaryMaps[ctr]=new ConcurrentHashMap(capacity/4,0.75f,16);
            }
        }
    }
    
    public static Cache createBoundedCache(int capacity,boolean usedOnGet,FieldGetter fieldGetter,String... secondaryKeyNames){
        return new Cache(capacity/4, capacity, usedOnGet, fieldGetter, secondaryKeyNames);
    }
        
    public static Cache createUnboundedCache(int initialCapacity,boolean usedOnGet,FieldGetter fieldGetter,String... secondaryKeyNames){
        return new Cache(initialCapacity, -1, usedOnGet, fieldGetter, secondaryKeyNames);
    }
        
    public V put(K key,V value){
        Entry<K,V> newEntry=new Entry(key, value);
        Entry<K,V> oldEntry=map.put(key, newEntry);
        V oldValue=null;
        if(oldEntry!=null){
            oldValue=oldEntry.value;
            removeFromSecondaryMaps(oldEntry.value);
            // remove old entry
            addAction(actionRemove, oldEntry);
        }
        else if(this.capacity>0 && map.size()>this.capacity)
            addAction(actionRemoveFirst, null);
        addToSecondaryMaps(newEntry);
        //add new entry
        addAction(actionAdd, newEntry);
        tryProcessActionQueue();
        return oldValue;
    }
    
    public V remove(K key){
        Entry<K,V> entry=map.remove(key);        
        V value=null;
        if(entry!=null){
           value=entry.value;
            removeFromSecondaryMaps(value);
            //remove entry
            addAction(actionRemove, entry);
        }
        tryProcessActionQueue();
        return value;
    }
    
    public V removeWithSecondaryKey(String keyName,Object keyValue){
        if(secondaryKeys==null)
            return null;        
        Entry<K,V> entry=secondaryMaps[secondaryKeyIndex(keyName)].get(keyValue);
        if(entry!=null)
            return remove(entry.key);
        return null;
    }
    
    public boolean containsKey(K key){
        return map.containsKey(key);
    }
    
    public V get(K key){
        Entry<K,V> entry=map.get(key);
        if(entry!=null){
            if(usedOnGet){
                //move entry to end
                addAction(actionMoveToEnd, entry);
            }
            if(actionQueueReference.get()!=null)
                tryProcessActionQueue();
            return entry.value;
        }
        return null;
    }
    
    public V getWithSecondaryKey(String keyName,Object keyValue){
        if(secondaryKeys==null)
            return null;        
        Entry<K,V> entry=secondaryMaps[secondaryKeyIndex(keyName)].get(keyValue);
        if(entry!=null){
            if(usedOnGet){
                //move entry to end
                addAction(actionMoveToEnd, entry);
            }
            if(actionQueueReference.get()!=null)
                tryProcessActionQueue();
            return entry.value;
        }
        return null;                
    }
    
    public V getWithCompoundSecondaryKey(String[] keyNames,Object... keyValues){
        if(secondaryKeys==null)
            return null;        
        Entry<K,V> entry=secondaryMaps[secondaryKeyIndex(keyNames)].get(concantenateSecondaryKeyValues(keyValues));
        if(entry!=null){
            if(usedOnGet){
                //move entry to end
                addAction(actionMoveToEnd, entry);
            }
            if(actionQueueReference.get()!=null)
                tryProcessActionQueue();
            return entry.value;
        }
        return null;                
    }
    
    private void addAction(byte action,Entry<K,V> entry){
        ConcurrentLinkedQueue<EntryAction<K,V>> actionQueue=getActionQueue();
        actionQueue.add(new EntryAction(action, entry));
    }
    
    private ConcurrentLinkedQueue<EntryAction<K,V>> getActionQueue(){
        ConcurrentLinkedQueue<EntryAction<K,V>> actionQueue=actionQueueReference.get();
        if(actionQueue!=null)
            return actionQueue;
        actionQueue=new ConcurrentLinkedQueue();
        ConcurrentLinkedQueue<EntryAction<K,V>> oldActionQueue;            
        while(true){
            if(actionQueueReference.compareAndSet(null, actionQueue))
               return actionQueue;
            if((oldActionQueue=actionQueueReference.get())!=null)
                return oldActionQueue;
        }
    }
    
    private void tryProcessActionQueue(){
        if(processQueueLock.tryLock()){
            try{
                ConcurrentLinkedQueue<EntryAction<K,V>> actionQueue=actionQueueReference.getAndSet(null);
                if(actionQueue==null)
                    return;
                for(EntryAction<K,V> entryAction:actionQueue){
                    switch(entryAction.action){
                        case actionAdd: 
                            addEntry(entryAction.entry);
                            break;
                        case actionRemove:
                            removeEntry(entryAction.entry);
                            break;
                        case actionMoveToEnd:
                            moveEntryToEnd(entryAction.entry);
                            break;
                        case actionRemoveFirst:
                            removeFirst();
                            break;
                    }
                }
            }finally{
                processQueueLock.unlock();
            }
        }
    }
    
    private void addEntry(Entry<K,V> entry){
        entry.next = header;
        entry.previous = header.previous;
        entry.previous.next = entry;
        entry.next.previous = entry;        
    }
    
    private void removeEntry(Entry<K,V> entry){
        if(entry.previous==null)
            return;
	entry.previous.next = entry.next;
	entry.next.previous = entry.previous;
        entry.next = entry.previous = null;
        
    }
    
    private void moveEntryToEnd(Entry<K,V> entry){
        if(entry.previous==null)
            return;
	entry.previous.next = entry.next;
	entry.next.previous = entry.previous;
        entry.next = header;
        entry.previous = header.previous;
        entry.previous.next = entry;
        entry.next.previous = entry;                
    }
    
    private void removeFirst(){
        if(header.next==header)
            return;
	Entry<K,V> firstNode=header.next;
        firstNode.previous.next = firstNode.next;
	firstNode.next.previous = firstNode.previous;
        firstNode.next = firstNode.previous = null;
        if(firstNode!=null){
            map.remove(firstNode.key);
            removeFromSecondaryMaps(firstNode.value);
        }            
    }
    
    private void addToSecondaryMaps(Entry<K,V> entry){
        if(secondaryKeys==null)
            return;
        Object keyValue;
        try{
            for(int ctr=0;ctr<secondaryKeys.length;ctr++){
                keyValue=getSecondaryKeyValueFromObject(secondaryKeys[ctr], entry.value);
                if(keyValue!=null)
                    secondaryMaps[ctr].put(keyValue, entry);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    
    private Object getSecondaryKeyValueFromObject(String[] secondaryKeys,Object object){
        try{
            if(secondaryKeys.length<2)
                return fieldGetter.getValue(object, secondaryKeys[0]);
            StringBuilder keyValue=new StringBuilder(30);
            String value=Utils.stringz(fieldGetter.getValue(object,secondaryKeys[0]));
            keyValue.append(value==null?"":value);
            for(int ctr=1;ctr<secondaryKeys.length;ctr++){
                value=Utils.stringz(fieldGetter.getValue(object,secondaryKeys[ctr]));
                keyValue.append(compoundKeySeparator).append(value==null?"":value);
            }
            if(keyValue.length()==secondaryKeys.length-1)
                return null;
            return keyValue.toString();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    
    private void removeFromSecondaryMaps(V value){
        if(fieldGetter==null || secondaryKeys==null)
            return;
        Object keyValue;
        try{
            for(int ctr=0;ctr<secondaryKeys.length;ctr++){
                keyValue=getSecondaryKeyValueFromObject(secondaryKeys[ctr], value);
                if(keyValue!=null)
                    secondaryMaps[ctr].remove(keyValue);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }        
    }
    
    public void secondaryKeyValueChanged(K key,String secondaryKeyName,Object oldSecondaryKeyValue,Object newSecondaryKeyValue){
        if(secondaryKeys==null)
            return;
        Entry<K,V> entry=map.get(key);
        if(entry==null)
            return;
        int keyIndex=secondaryKeyIndex(secondaryKeyName);
        if(oldSecondaryKeyValue!=null)
            secondaryMaps[keyIndex].remove(oldSecondaryKeyValue);
        secondaryMaps[keyIndex].put(newSecondaryKeyValue, entry);
    }
    
    public void compoundSecondaryKeyValueChanged(K key,String[] secondaryKeyNames,Object[] oldSecondaryKeyValues,Object... newSecondaryKeyValues){
        if(secondaryKeys==null)
            return;
        Entry<K,V> entry=map.get(key);
        if(entry==null)
            return;
        int keyIndex=secondaryKeyIndex(secondaryKeyNames);
        if(!isCompoundValueNull(oldSecondaryKeyValues))
            secondaryMaps[keyIndex].remove(concantenateSecondaryKeyValues(oldSecondaryKeyValues));
        secondaryMaps[keyIndex].put(concantenateSecondaryKeyValues(newSecondaryKeyValues), entry);
    }
    
    private static boolean isCompoundValueNull(Object[] values){
        for(Object value:values){
            if(value!=null)
                return false;
        }
        return true;
    }
    
    private int secondaryKeyIndex(String keyName){
        for(int ctr=0;ctr<secondaryKeys.length;ctr++){
            if(secondaryKeys[ctr].length<2 && keyName.equals(secondaryKeys[ctr][0]))
                return ctr;
        }
        return -1;
    }
    
    private int secondaryKeyIndex(String[] keyNames){
        for(int ctr=0;ctr<secondaryKeys.length;ctr++){
            if(Arrays.equals(keyNames,secondaryKeys[ctr]))
                return ctr;
        }
        return -1;
    }
    
    private static String concantenateSecondaryKeyValues(Object[] values){
        StringBuilder compoundValue=new StringBuilder(30);        
        compoundValue.append(values[0]==null?"":String.valueOf(values[0]));
        for(int ctr=1;ctr<values.length;ctr++)
            compoundValue.append(compoundKeySeparator).append(values[ctr]==null?"":String.valueOf(values[ctr]));
        if(compoundValue.length()==values.length-1)
            return null;
        return compoundValue.toString();
    } 
    
    public int size(){
        return map.size();
    }
    
    public Iterator<V> iterator(){
        return new StraightIterator();
    }
    
    private class StraightIterator implements Iterator<V>{
        private Entry<K,V> nextEntry;
        private Entry<K,V> prevEntry=null;
        public StraightIterator() {
            nextEntry=header.next;
        }

        @Override
        public boolean hasNext() {
            return nextEntry!=header;
        }

        @Override
        public V next() {
            prevEntry=nextEntry;
            nextEntry=nextEntry.next;
            return (V)prevEntry.value;
        }

        @Override
        public void remove() {
            if(prevEntry==null)
                return;
            Cache.this.remove(prevEntry.key);                    
        }
    }
    
    private static String[] split(String str,char delim){
        List<Integer> delimIndexes=new ArrayList(2);
        int index=0;
        int fromIndex=0;
        int delimSize=1;
        while(true){
            index=str.indexOf(delim, fromIndex);
            if(index<0)
                break;
            delimIndexes.add(index);
            fromIndex=index+delimSize;
        }
        int delimCount=delimIndexes.size();
        String[] values=new String[delimCount+1];
        int toIndex;
        int ctr=0;
        fromIndex=0;
        for(;ctr<delimCount;ctr++){
            toIndex=delimIndexes.get(ctr);
            values[ctr]=str.substring(fromIndex,toIndex);
            fromIndex=toIndex+delimSize;
        }
        values[ctr]=str.substring(fromIndex);
        return values;
    }
    
}
