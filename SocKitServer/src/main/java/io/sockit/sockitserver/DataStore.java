/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import java.io.IOException;

/**
 * A DataStore is a representation of the Game Database as a Key Value data store. An instance of this interface can be registered as the Game Engine's database. This is so that the Game developer is not locked down to the Default Database that comes with this Game Engine.
 * 
 */
public interface DataStore {

    /**
     * Opens the Database for IO operations  
     * @throws IOException - 
     */
    void open() throws IOException;

    /**
     * Closes the database
     * @throws IOException - 
     */
    void close() throws IOException;

    /**
     * reads the value associated with the key from the database
     * @param key - the key whose value is to be read
     * @return - the value associated with the key
     */
    byte[] get(byte[] key);

    /**
     * writes a value into the database
     * @param key - the key associated with the value
     * @param value - the value to be written to the database
     */
    void put(byte[] key,byte[] value);

    /**
     * Deletes a value from the database
     * @param key - the key whose value is to be deleted
     */
    void delete(byte[] key);
    
    /**
     * Returns a DataIterator whish iterates over all the entries in the dataStore
     * @return DataIterator - an iterator which iterates through entries in this Data Store
     */
    DataIterator iterator();
}
