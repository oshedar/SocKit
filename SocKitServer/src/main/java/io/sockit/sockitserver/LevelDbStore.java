/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 *
 * An implementation of the DataStore interface which uses LevelDB to save/persist the Game Engine data.
 */
public class LevelDbStore implements DataStore{
    private File dataFolder;
    private DB db;

    /**
     *
     * @param dbFolder - the folder where the LevelDB data files are saved
     */
    public LevelDbStore(File dbFolder) {
        this.dataFolder = dbFolder;
    }
    
    /**
     *
     * @param dbFolder - the folder where the LevelDB data files are saved
     */
    public LevelDbStore(String dbFolder){
        this(new File(dbFolder));
    }

    /**
     * Opens the database
     * @throws IOException - 
     */
    @Override
    public void open() throws IOException {
        Options options=new Options();
        options.createIfMissing(true);
        if(!dataFolder.exists())
            dataFolder.mkdir();
        db=factory.open(dataFolder, options);
    }

    /**
     * Closes the database
     * @throws IOException -
     */
    @Override
    public void close() throws IOException{
        if(db!=null){
            db.close();
            db=null;
        }
    }

    /**
     * reads the value associated with the key from the database
     * @param key - the key whose value is to be read
     * @return - the value associated with the key
     */
    @Override
    public byte[] get(byte[] key) {        
        return db.get(key);
    }

    /**
     * writes a value into the database
     * @param key - the key associated with the value
     * @param value - the value to be written to the database
     */
    @Override
    public void put(byte[] key, byte[] value) {
        db.put(key, value);
    }

    /**
     * Deletes a value from the database
     * @param key - the key whose value is to be deleted
     */
    @Override
    public void delete(byte[] key) {
        db.delete(key);
    }

    @Override
    public DataIterator iterator() {
        return new LevelDBIterator();
    }
    
    private class LevelDBIterator implements DataIterator{
        DBIterator dBIterator;

        public LevelDBIterator() {
            dBIterator=db.iterator();
        }
        
        @Override
        public void seek(byte[] keyPrefix) {
            dBIterator.seek(keyPrefix);
        }

        @Override
        public void seekToFirst() {
            dBIterator.seekToFirst();
        }

        @Override
        public Map.Entry<byte[], byte[]> peekNext() {
            return dBIterator.peekNext();
        }

        @Override
        public boolean hasPrev() {
            return dBIterator.hasPrev();
        }

        @Override
        public Map.Entry<byte[], byte[]> prev() {
            return dBIterator.prev();
        }

        @Override
        public Map.Entry<byte[], byte[]> peekPrev() {
            return dBIterator.peekPrev();
        }

        @Override
        public void seekToLast() {
            dBIterator.seekToLast();
        }

        @Override
        public boolean hasNext() {
            return dBIterator.hasNext();
        }

        @Override
        public Map.Entry<byte[], byte[]> next() {
            return dBIterator.next();
        }

    }
}
