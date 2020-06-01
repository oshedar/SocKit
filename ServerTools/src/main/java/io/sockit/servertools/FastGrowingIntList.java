/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.*;
/**
 *
 * A list implementation that gives the performance of ArrayList without its drawbacks. Unline Arraylist FastGrowingList doesn't waste memory
 */
public class FastGrowingIntList implements Cloneable{

    private short blockSize = 40;
    private int modCount=0;
    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    private transient int[][] blocks;
    private int size;

    /**
     * Constructs an empty list with an incrementBy of 40. IncrementBy is the number of elements by which the storage capacity is increased by when more storage is required.
     */
    public FastGrowingIntList() {
        blocks = new int[5][];
        size=0;
    }

    /**
     * Constructs an empty list with the specified incrementBy. IncrementBy is the number of elements by which the storage capacity is increased by when more storage is required.
     * @param incrementBy - the number of elements by which to increase the storage capacity when more storage is required
     */
    public FastGrowingIntList(int incrementBy){
        this();
        if(incrementBy>=5)
            this.blockSize=(short)incrementBy;
        else
            this.blockSize=5;
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public FastGrowingIntList(FastGrowingIntList c) {
        this();
        if(c instanceof FastGrowingIntList){
            FastGrowingIntList c2=c;
            if(c2.size==0)
                return;
            ensureCapacity(size + c2.size);
            int ctr;
            for(ctr=0;ctr<c2.size;ctr++)
                blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize]=c2.blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize];
            size+=c2.size;
        }
        else {
            int[] a = c.toArray();
            int numNew=a.length;
            if(numNew==0)
                return;
            ensureCapacity(size + numNew);
            int ctr;
            for(ctr=0;ctr<numNew;ctr++)
                blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize]=a[ctr];
            size+=numNew;
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(int o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * @param o
     * @return 
     */
    public int indexOf(int o) {
        for (int i = 0; i < size; i++) {
            if (o==(blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * @param o
     * @return 
     */
    public int lastIndexOf(int o) {
        for (int i = size - 1; i >= 0; i--) {
            if (o==(blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */
    public Object clone() {
        try {
            FastGrowingIntList v = (FastGrowingIntList) super.clone();
            for(int i=0;i<v.blocks.length;i++){
                if(v.blocks[i]==null)
                    break;
                v.blocks[i]=new int[blockSize];
            }
            for(int i=0;i<size;i++)
                v.blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize]=blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize];
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list in
     *         proper sequence
     */
    public int[] toArray() {
        int[] array=new int[size];
        for(int i=0;i<size;i++)
            array[i]=blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize];
        return array;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array.  If the list fits in the
     * specified array, it is returned therein.  Otherwise, a new array is
     * allocated with the runtime type of the specified array and the size of
     * this list.
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list), the element in
     * the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of the
     * list <i>only</i> if the caller knows that the list does not contain
     * any null elements.)
     *
     * @param array
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    public  int[] toArray(int[] array) {
        if (array.length < size) // Make a new array of a's runtime type, of length size
            array=new int[size];
        for(int i=0;i<size;i++)
            array[i]=blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize];
        return array;
    }

    // Positional Access Operations
    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public int get(int index) {
        if (index<0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        return blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public int set(int index, int element) {
        if (index<0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        int oldValue =  blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize];
        blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize] = element;
        return oldValue;
    }

    /**
     * increments the element at the specified position in this list.
     *
     * @param index index of the element to replace
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void increment(int index) {
        if (index<0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize]++;
    }

    /**
     * decrements the element at the specified position in this list.
     *
     * @param index index of the element to replace
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void decrement(int index) {
        if (index<0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize]--;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(int e) {
        modCount++;//used by iterator to check  if list is perturbed in such a fashion that iterations in progress may yield incorrect results.
        int requiredBlockIndex=(size+blockSize)/blockSize-1;
        if(requiredBlockIndex>=blocks.length){
            int[][] blocks2=new int[blocks.length+10][];
            System.arraycopy(blocks, 0, blocks2, 0, blocks.length);
            blocks=blocks2;
        }
        if(blocks[requiredBlockIndex]==null)
            blocks[requiredBlockIndex]=new int[blockSize];
        int posInBlock=(size+blockSize) % blockSize;
        blocks[requiredBlockIndex][posInBlock]=e;
        size++;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, int element) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        modCount++;//used by iterator to check  if list is perturbed in such a fashion that iterations in progress may yield incorrect results.
        ensureCapacity(size+1);
        if(index<size)
            fastPushDown(index, 1);
        blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize]=element;
        size++;
    }

    //does not call ensureCapacity as capacity is already ensured also does not check if index is correct
    private void fastPushDown(int fromIndex,int qty){
        int ctr;
        ctr=size-1;
        for(;ctr>=fromIndex;ctr--)
            blocks[(ctr+qty+blockSize)/blockSize-1][(ctr+qty+blockSize) % blockSize]=blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize];
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public int remove(int index) {
        if(index<0 && index>=size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        modCount++;//used by iterator to check  if list is perturbed in such a fashion that iterations in progress may yield incorrect results.
        int removedObject=blocks[(index+blockSize)/blockSize-1][(index+blockSize) % blockSize];
        //push up
        int ctr;
        for(ctr=index+1;ctr<size;ctr++)
            blocks[(ctr-1+blockSize)/blockSize-1][(ctr-1+blockSize) % blockSize]=blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize];
         size--;
         if(size%blockSize==0){
             if(blocks.length>size/blockSize)
                 blocks[size/blockSize]=null;
         }
         return removedObject;
    }

    private void fastRemove(int index){
        modCount++;//used by iterator to check  if list is perturbed in such a fashion that iterations in progress may yield incorrect results.
        //push up
        int ctr;
        for(ctr=index+1;ctr<size;ctr++)
            blocks[(ctr-1+blockSize)/blockSize-1][(ctr-1+blockSize) % blockSize]=blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize];
         size--;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean removeByValue(int o) {
        int index=indexOf(o);
        if(index>=0){
            fastRemove(index);
            return true;
        }
        return false;
    }


    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        modCount++;

        // Let gc do its work
        for(int i=0;i<blocks.length;i++)
            blocks[i]=null;
        if(blocks.length>5)
            blocks=new int[5][];
        size=0;
    }

    /**
     * Removes all of the elements from this list - but maintains capacity if maintainCapacity is true
     * The list will be empty after this call returns.
     * @param maintainCapacity
     */
    public void clear(boolean maintainCapacity) {
        modCount++;

        // Let gc do its work
        if(!maintainCapacity){
            for(int i=0;i<blocks.length;i++)
                blocks[i]=null;
            if(blocks.length>10)
                blocks=new int[10][];
        }
        size=0;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(FastGrowingIntList c) {
        FastGrowingIntList c2=(FastGrowingIntList)c;
        if(c2.size==0)
            return false;
        modCount++;
        ensureCapacity(size + c2.size);
        int blockSize2=c2.blockSize;
        int ctr;
        for(ctr=0;ctr<c2.size;ctr++){
            blocks[(size+ctr+blockSize)/blockSize-1][(size+ctr+blockSize) % blockSize]=c2.blocks[(ctr+blockSize2)/blockSize2-1][(ctr+blockSize2) % blockSize2];
        }
        size+=c2.size;
        return true;
    }

    private void ensureCapacity(int capacity){
        int requiredBlocks=(capacity-1+blockSize)/blockSize;
        int lastBlockIndex=(size-1+blockSize)/blockSize-1;
        if(requiredBlocks>blocks.length){
            int[][] blocks2=new int[requiredBlocks+8][];
            System.arraycopy(blocks, 0, blocks2, 0, blocks.length);
            blocks=blocks2;
        }
        if(lastBlockIndex<0)
            lastBlockIndex=0;
        for(;lastBlockIndex<requiredBlocks;lastBlockIndex++){
            if(blocks[lastBlockIndex]==null)
                blocks[lastBlockIndex]=new int[blockSize];
        }
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, FastGrowingIntList c) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
        }
        FastGrowingIntList c2=(FastGrowingIntList)c;
        if(c2.size==0)
            return false;
        int blockSize2=c2.blockSize;
        modCount++;
        ensureCapacity(size + c2.size);
        if(index<size)
            fastPushDown(index, c2.size);
        for(int ctr=0;ctr<c2.size;ctr++)
            blocks[(index+ctr+blockSize)/blockSize-1][(index+ctr+blockSize) % blockSize]=c2.blocks[(ctr+blockSize2)/blockSize2-1][(ctr+blockSize2) % blockSize2];
        size+=c2.size;
        return true;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     * @throws IndexOutOfBoundsException if fromIndex or toIndex out of
     *              range (fromIndex &lt; 0 || fromIndex &gt;= size() || toIndex
     *              &gt; size() || toIndex &lt; fromIndex)
     */
    private void removeRange(int fromIndex, int toIndex) {
        if(fromIndex<0 || fromIndex>=size)
            throw new IndexOutOfBoundsException("Index: "+fromIndex+", Size: "+size);
        if(toIndex<0 || toIndex>=size)
            throw new IndexOutOfBoundsException("Index: "+toIndex+", Size: "+size);
        int temp;
        if(fromIndex>toIndex){//swap
            temp=fromIndex;
            fromIndex=toIndex;
            toIndex=temp;
        }
        int numRemoved=toIndex-fromIndex + 1;
        int ctr;
        if(numRemoved<1)
            return;
        modCount++;//used by iterator to check  if list is perturbed in such a fashion that iterations in progress may yield incorrect results.
        for(ctr=fromIndex;ctr<size-numRemoved;ctr++)
            blocks[(ctr+blockSize)/blockSize-1][(ctr+blockSize) % blockSize]=blocks[(ctr+numRemoved+blockSize)/blockSize-1][(ctr+numRemoved+blockSize) % blockSize];
        size-=numRemoved;
        trimToSize();
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
        int lastBlockIndex=(size-1+blockSize)/blockSize-1;
        int ctr,i;
        for(ctr=blocks.length-1;ctr>lastBlockIndex;ctr--){
            blocks[ctr]=null;
        }

        if(size==0)
            blocks[0]=null;
        if(blocks.length-1>lastBlockIndex)//remove extra blocks
            blocks=Arrays.copyOf(blocks, lastBlockIndex+1);
    }

    /**
     * Searches for an element in the List. Returns the index of the first element in the list whose string representation begins with the specified string
     * @param s
     * @return
     */
    public int indexOfStartsWith(String s){
        Object item;
        if(s==null || size==0)
            return -1;
        s=s.toUpperCase();
        for(int i=0;i<size;i++){
            item=blocks[(i+blockSize)/blockSize-1][(i+blockSize) % blockSize];
            if(item==null)
                continue;
            if(item instanceof String){
                if(((String)item).toUpperCase().startsWith(s))
                    return i;
            }
            else if(item.toString().toUpperCase().startsWith(s))
               return i;
        }
        return -1;
    }

//    public static void main(String[] args){
//        FastGrowingIntList list1=new FastGrowingIntList();
//        FastGrowingIntList list2=new FastGrowingIntList(5);
//        for(int ctr=10;ctr<=40;ctr++)
//            list1.add(ctr);
//        list2.add(5);
//        list2.addAll(0,list1);
//        for(int ctr=0;ctr<6;ctr++)
//            System.out.println(list2.remove(list2.size-1));
//        for(int ctr=0;ctr<6;ctr++)
//            System.out.println(list2.get(ctr));
//    }
}
