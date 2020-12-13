/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import java.util.Iterator;
import java.util.Map;

/**
 * An Iterator over entries in a DataStore
 */
public interface DataIterator
        extends Iterator<Map.Entry<byte[], byte[]>>
{
    /**
     * Repositions the iterator to the next entry whose key begins with the keyPrefix
     * @param keyPrefix - the prefix of the key to seek
     */
    void seek(byte[] keyPrefix);

    /**
     * Repositions the iterator so is is at the beginning of the DataStore.
     */
    void seekToFirst();

    /**
     * Returns the next element in the iteration, without advancing the iteration.
     * @return Map.Entry&lt;byte[], byte[]&gt; - the next element in the iteration, without advancing the iteration.
     */
    Map.Entry<byte[], byte[]> peekNext();

    /**
     * @return boolean - true if there is a previous entry in the iteration.
     */
    boolean hasPrev();

    /**
     * @return Map.Entry&lt;byte[], byte[]&gt; - the previous element in the iteration and rewinds the iteration.
     */
    Map.Entry<byte[], byte[]> prev();

    /**
     * @return Map.Entry&lt;byte[], byte[]&gt; - the previous element in the iteration, without rewinding the iteration.
     */
    Map.Entry<byte[], byte[]> peekPrev();

    /**
     * Repositions the iterator so it is at the end of of the DataStore.
     */
    void seekToLast();
}
