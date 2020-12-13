/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author h
 */
class Seats<E> implements Iterable<E> {
    private Object[] elements;
    private volatile int occupiedCount;
    protected volatile transient int modCount=0;
    
    public Seats(int capacity){
        elements=new Object[capacity];
        occupiedCount=0;
    }
    
    public int getOccupiedCount(){
        return occupiedCount;
    }
    
    public int getCapacity(){
        return elements.length;
    }
    
    public void clear(){
        for(int ctr=0;ctr<elements.length;ctr++)
            elements[ctr]=null;
        modCount++;
        occupiedCount=0;
    }
    
    public void copyFrom(Seats<E> src){
        clear();
        for(int ctr=0;ctr<src.elements.length;ctr++)
            elements[ctr]=src.elements[ctr];
        occupiedCount=src.occupiedCount;
    }
    
    public boolean add(E e,int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        if(e==null)
            throw new NullPointerException();
        if(elements[seatNo-1]==null){
            elements[seatNo-1]=e;
            occupiedCount++;
            modCount++;
            return true;
        }
        return false;
    }
    
    public E remove(int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        E e=(E)elements[seatNo-1];
        elements[seatNo-1]=null;
        if(e!=null){
            modCount++;
            occupiedCount--;
        }
        return e;
    }
    
    public E remove(E e){
        if(e==null)
            throw new NullPointerException();
        for(int ctr=0;ctr<elements.length;ctr++){
            if(elements[ctr]==e){
                elements[ctr]=null;
                modCount++;
                occupiedCount--;
                return e;
            }
        }
        return null;
    }
    
    public E get(int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        return (E)elements[seatNo-1];
    }
    
    public int getSeatNo(E e){
        for(int ctr=0;ctr<elements.length;ctr++){
            if(elements[ctr]==e){
                return ctr+1;
            }
        }
        return -1;        
    }
    
    public boolean isSeatFree(int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        return elements[seatNo-1]==null;
    }
    
    public E getFirstOccupant(){
        E element;
        for(int ctr=0;ctr<elements.length;ctr++){
            element=(E)elements[ctr];
            if(element!=null){
                return element;
            }
        }
        return null;
    }
    
    public int getFirstOccupiedSeatNo(){
        for(int ctr=0;ctr<elements.length;ctr++){
            if(elements[ctr]!=null){
                return ctr+1;
            }
        }
        return 0;        
    }
    
    public int getFirstFreeSeatNo(){
        for(int ctr=0;ctr<elements.length;ctr++){
            if(elements[ctr]==null){
                return ctr+1;
            }
        }
        return 0;                
    }
    
    public E getNextOccupant(int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        E element;
        for(int ctr=seatNo;ctr<elements.length;ctr++){
            element=(E)elements[ctr];
            if(element!=null){
                return element;
            }
        }
        for(int ctr=0;ctr<seatNo-1;ctr++){
            element=(E)elements[ctr];
            if(element!=null){
                return element;
            }            
        }
        return null;                
    }
    
    public int getNextOccupiedSeatNo(int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        for(int ctr=seatNo;ctr<elements.length;ctr++){
            if(elements[ctr]!=null){
                return ctr+1;
            }
        }
        for(int ctr=0;ctr<seatNo-1;ctr++){
            if(elements[ctr]!=null){
                return ctr+1;
            }            
        }
        return 0;                        
    }
    
    public int getNextFreeSeatNo(int seatNo){
        if(seatNo<1 || seatNo>elements.length)
            throw new ArrayIndexOutOfBoundsException(seatNo);
        for(int ctr=seatNo;ctr<elements.length;ctr++){
            if(elements[ctr]==null){
                return ctr+1;
            }
        }
        for(int ctr=0;ctr<seatNo-1;ctr++){
            if(elements[ctr]==null){
                return ctr+1;
            }            
        }
        return 0;                                
    }
    
    public E getNextOccupant(E e){
        int seatNo=getSeatNo(e);
        if(seatNo>0)
            return getNextOccupant(seatNo);
        return null;        
    }
    
    public int getNextOccupiedSeatNo(E e){
        int seatNo=getSeatNo(e);
        if(seatNo>0)
            return getNextOccupiedSeatNo(seatNo);
        return 0;                
    }
    
    @Override
    public Iterator<E> iterator() {
        return new SeatIterator(modCount);
    }
    
    private class SeatIterator implements Iterator<E>{
        int curIndex;
        int expectedModCount;
        SeatIterator(int modCount) {
            expectedModCount=modCount;
            for(curIndex=0;curIndex<elements.length;curIndex++){
                if(elements[curIndex]!=null)
                    break;                
            }
        }
        
        @Override
        public boolean hasNext() {
            return curIndex<elements.length;
        }

        private int lastNextIndex=-1;
        @Override
        public E next() {
            if(modCount!=expectedModCount)
                throw new ConcurrentModificationException();
            if(curIndex>=elements.length)
                throw new NoSuchElementException();
            lastNextIndex=curIndex;
            E e=(E)elements[curIndex++];
            for(;curIndex<elements.length;curIndex++){
                if(elements[curIndex]!=null)
                    break;                
            }
            return e;
        }        

        @Override
        public void remove() {
            if(modCount!=expectedModCount)
                throw new ConcurrentModificationException();
            if(lastNextIndex>=0 && elements[lastNextIndex]!=null){
                elements[lastNextIndex]=null;
                occupiedCount--;
            }
        }
    }
    
    Iterable<E> getIterable(){
        return iterableSeats;
    }
    
    private Iterable<E> iterableSeats=new IterableSeats();
    private class IterableSeats implements Iterable<E>{
        
        @Override
        public Iterator<E> iterator() {
            return new SeatIterator(modCount);
        }
        
    }
}
