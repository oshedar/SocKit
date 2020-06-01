/*
 * IntList.java Created Aug 310, 2010 by Andrew Butler, PSL
 */
package io.sockit.servertools;

import java.util.Arrays;

/**
 * Acts like an {@link java.util.ArrayList} but for primitive int values
 */
public class ArrayIntList {
  private int[] theValues;

  private int theSize;

  /**
   * Creates a list with a capacity of 5
   */
  public ArrayIntList() {
    this(5);
  }

  /**
   * Creates a list with a set capacity
   * 
   * @param size
   *            The initial capacity of the list
   */
  public ArrayIntList(int size) {
    theValues = new int[size];
  }

  /**
   * Creates a list with a set of values
   * 
   * @param values
   *            The values for the list
   */
  public ArrayIntList(int[] values) {
    theValues = values;
    theSize = values.length;
  }

  /**
   * @return The number of elements in the list
   */
  public int size() {
    return theSize;
  }

  /**
   * @return Whether this list is empty of elements
   */
  public boolean isEmpty() {
    return theSize == 0;
  }

  /**
   * Clears this list, setting its size to 0
   */
  public void clear() {
    theSize = 0;
  }

  /**
   * Gets the value in the list at the given index
   * 
   * @param index
   *            The index of the value to get
   * @return The value at the given index
   */
  public int get(int index) {
    if (index < 0 || index >= theSize)
      throw new ArrayIndexOutOfBoundsException(index);
    return theValues[index];
  }

  /**
   * Adds a value to the end of this list
   * 
   * @param value
   *            The value to add to the list
   */
  public void add(int value) {
    ensureCapacity(theSize + 1);
    theValues[theSize++] = value;
  }

  /**
   * Adds a value to this list at the given index
   * 
   * @param index
   *            The index to add the value at
   * @param value
   *            The value to add to the list
   */
  public void add(int index, int value) {
    if (index < 0 || index > theSize)
      throw new ArrayIndexOutOfBoundsException(index);
    ensureCapacity(theSize + 1);
    for (int i = theSize; i > index; i--)
      theValues[i] = theValues[i - 1];
    theValues[index] = value;
    theSize++;
  }

  /**
   * Adds an array of values to the end of this list
   * 
   * @param value
   *            The values to add
   */
  public void addAll(int[] value) {
    ensureCapacity(theSize + value.length);
    for (int i = 0; i < value.length; i++)
      theValues[theSize + i] = value[i];
    theSize += value.length;
  }

  /**
   * Adds a list of values to the end of this list
   * 
   * @param list
   *            The list of values to add
   */
  public void addAll(ArrayIntList list) {
    ensureCapacity(theSize + list.theSize);
    for (int i = 0; i < list.theSize; i++)
      theValues[theSize + i] = list.theValues[i];
    theSize += list.theSize;
  }

  /**
   * Replaces a value in this list with another value
   * 
   * @param index
   *            The index of the value to replace
   * @param value
   *            The value to replace the old value with
   * @return The old value at the given index
   */
  public int set(int index, int value) {
    if (index < 0 || index >= theSize)
      throw new ArrayIndexOutOfBoundsException(index);
    int ret = theValues[index];
    theValues[index] = value;
    return ret;
  }

  /**
   * Removes a value from this list
   * 
   * @param index
   *            The index of the value to remove
   * @return The value that was removed
   */
  public int remove(int index) {
    if (index < 0 || index >= theSize)
      throw new ArrayIndexOutOfBoundsException(index);
    int ret = theValues[index];
    for (int i = index; i < theSize - 1; i++)
      theValues[i] = theValues[i + 1];
    theSize--;
    return ret;
  }

  /**
   * Removes a value from this list
   * 
   * @param value
   *            The value to remove
   * @return Whether the value was found and removed
   */
  public boolean removeValue(int value) {
    for (int i = 0; i < theSize; i++)
      if (theValues[i] == value) {
        remove(i);
        return true;
      }
    return false;
  }

  /**
   * Removes all instances of the given value from this list
   * 
   * @param value
   *            The value to remove
   * @return The number of times the value was removed
   */
  public int removeAll(int value) {
    int ret = 0;
    for (int i = 0; i < theSize; i++)
      if (theValues[i] == value) {
        remove(i);
        i--;
        ret++;
      }
    return ret;
  }

  /**
   * Determines if this list contains a given value
   * 
   * @param value
   *            The value to find
   * @return Whether this list contains the given value
   */
  public boolean contains(int value) {
    return indexOf(value) >= 0;
  }

  /**
   * Counts the number of times a value is represented in this list
   * 
   * @param value
   *            The value to count
   * @return The number of times the value appears in this list
   */
  public int instanceCount(int value) {
    int ret = 0;
    for (int i = 0; i < theSize; i++)
      if (theValues[i] == value)
        ret++;
    return ret;
  }

  /**
   * Finds a value in this list
   * 
   * @param value
   *            The value to find
   * @return The first index whose value is the given value
   */
  public int indexOf(int value) {
    for (int i = 0; i < theSize; i++)
      if (theValues[i] == value)
        return i;
    return -1;
  }

  /**
   * Finds a value in this list
   * 
   * @param value
   *            The value to find
   * @return The last index whose value is the given value
   */
  public int lastIndexOf(int value) {
    for (int i = theSize - 1; i >= 0; i--)
      if (theValues[i] == value)
        return i;
    return -1;
  }

  /**
   * @return The list of values currently in this list
   */
  public int[] toArray() {
    int[] ret = new int[theSize];
    System.arraycopy(theValues, 0, ret, 0, theSize);
    return ret;
  }

  /**
   * Similary to {@link #toArray()} but creates an array of {@link Integer}
   * wrappers
   * 
   * @return The list of values currently in this list
   */
  public Integer[] toObjectArray() {
    Integer[] ret = new Integer[theSize];
    for (int i = 0; i < ret.length; i++)
      ret[i] = new Integer(theValues[i]);
    return ret;
  }

  /**
   * Trims this list so that it wastes no space and its capacity is equal to
   * its size
   */
  public void trimToSize() {
    if (theValues.length == theSize)
      return;
    int[] oldData = theValues;
    theValues = new int[theSize];
    System.arraycopy(oldData, 0, theValues, 0, theSize);
  }

  /**
   * Ensures that this list's capacity is at list the given value
   * 
   * @param minCapacity
   *            The minimum capacity for the list
   */
  public void ensureCapacity(int minCapacity) {
    int oldCapacity = theValues.length;
    if (minCapacity > oldCapacity) {
      int oldData[] = theValues;
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      theValues = new int[newCapacity];
      System.arraycopy(oldData, 0, theValues, 0, theSize);
    }
  }
    /** 
     * sorts the list in ascending order
     * 
     * @throws UnsupportedOperationException when this operation is not 
     *         supported
     */
    public void sort(){        
        Arrays.sort(theValues,0,theSize);        
    }
}

