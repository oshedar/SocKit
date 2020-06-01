package io.sockit.servertools;

/*
 * #%L
 * ch-commons-util
 * %%
 * Copyright (C) 2012 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A modified version of the one found here Originally found here: http://www.java2s.com/Code/Java/File-Input-Output/AspeedyimplementationofByteArrayOutputStream.htm
 *
 * A speedy implementation of ByteArrayOutputStream. It's not synchronized, and it
 * does not copy buffers when it's expanded. There's also no copying of the internal buffer
 * if it's contents is extracted with the writeTo(stream) method.
 * if data inserted in stream exceeds max Size then data will be written to temp file instead of a memory
 *
 * @author Rickard berg
 * @author Brat Baker (Atlassian)
 * @author Alexey
 * @version $Date: 2008-01-19 10:09:56 +0800 (Sat, 19 Jan 2008) $ $Id: FastByteArrayOutputStream.java 3000 2008-01-19 02:09:56Z tm_jee $
 */
public final class FastGrowingOutputStream extends OutputStream {

    private final int maxSize;
    // Static --------------------------------------------------------
    private static final int MAX_LIFE_OF_FILE_MILLIS=5*60*1000;//5 mins
    private static final int TMP_FILE_CLEANER_RUN_INTERVAL=8*60*1000;//8 mins
    private static final AtomicReference<File> tmpFolderReference=new AtomicReference();
    private static final int DEFAULT_BLOCK_SIZE = 256;
    private static final int DEFAULT_MAX_SIZE = 4*1024*1024;//4 MB
    private static final int MIN_MAX_SIZE = 1024*1024;//1 MB
    private LinkedList<byte[]> buffers;
    // Attributes ----------------------------------------------------
    // internal buffer
    private byte[] buffer;
    // is the stream closed?
    private boolean closed;
    private int blockSize;
    private int index;
    private int size;
    private File file=null;
    private OutputStream outputStream=null;

    // Constructors --------------------------------------------------
    public FastGrowingOutputStream() {
        this(DEFAULT_BLOCK_SIZE,DEFAULT_MAX_SIZE); 
    }

    public FastGrowingOutputStream(int blockSize) {
        this(blockSize,DEFAULT_MAX_SIZE); 
    }

    public FastGrowingOutputStream(int blockSize,int maxSize) {
        if(blockSize<64)
            blockSize=64;
        this.blockSize = blockSize;
        buffer = new byte[blockSize];
        if(maxSize<MIN_MAX_SIZE)
            maxSize=MIN_MAX_SIZE;
        this.maxSize=maxSize;
    }

    public int getSize() {
        return size + index;
    }

    @Override
    public void close() throws IOException {
        if(closed)
            return;
        closed = true;
        if(outputStream!=null){            
            outputStream.close();
            outputStream=null;
        }
    }

    public byte[] toByteArray() {
        if(file!=null){
            return null;
        }
        
        byte[] data = new byte[getSize()];

        // Check if we have a list of buffers
        int pos = 0;

        if (buffers != null) {
            Iterator iter = buffers.iterator();

            while(iter.hasNext()) {
                byte[] bytes = (byte[])iter.next();
                System.arraycopy(bytes, 0, data, pos, blockSize);
                pos += blockSize;
            }
        }

        // write the internal buffer directly
        System.arraycopy(buffer, 0, data, pos, index);

        return data;
    }
        
    // OutputStream overrides ----------------------------------------
    public void write(int datum) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        } else if(outputStream!=null || size+index+1>maxSize){ //if fos!=null or new size > maxSize write to file;
            getOutputStream().write(datum);
            size+=index+1;
            index=0;            
        } else {            
            if (index == blockSize) {
                addBuffer();
            }

            // store the byte
            buffer[index++] = (byte) datum;
        }
    }

    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        if (data == null) 
            throw new NullPointerException();
        if (length<1)
            return;
        if ((offset < 0) || ((offset + length) > data.length) || (length < 0)) 
            throw new IndexOutOfBoundsException();
        if (closed)
            throw new IOException("Stream closed");
        if(outputStream!=null || size+index+length>maxSize){ //if fos!=null or new size > maxSize write to file;
            getOutputStream().write(data, offset, length);
            size+=index+length;
            index=0;            
        } else {            
            if ((index + length) > blockSize) {
                int copyLength;

                do {
                    if (index == blockSize) {
                        addBuffer();
                    }

                    copyLength = blockSize - index;

                    if (length < copyLength) {
                        copyLength = length;
                    }

                    System.arraycopy(data, offset, buffer, index, copyLength);
                    offset += copyLength;
                    index += copyLength;
                    length -= copyLength;
                } while (length > 0);
            } else {
                // Copy in the subarray
                System.arraycopy(data, offset, buffer, index, length);
                index += length;
            }
        }
    }
    
    public boolean isFile(){
        return file!=null;
    }
    
    public File getFile(){
        if(file!=null && !closed){
            try{
                this.close();
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
        return file;
    }
    
    private OutputStream getOutputStream() throws IOException{
        if(outputStream==null){
            file=File.createTempFile("f",null, getTmpFolder());
            outputStream=new BufferedOutputStream(new FileOutputStream(file));
        }
        return outputStream;
    }
    
    private static File getTmpFolder() throws IOException{
        File tmpFolder=tmpFolderReference.get();
        if(tmpFolder!=null)
            return tmpFolder;
        synchronized(tmpFolderReference){
            tmpFolder=tmpFolderReference.get();
            if(tmpFolder!=null)
                return tmpFolder;
            String path=System.getProperty("fgosTmpFolder");
            if(path!=null){
                tmpFolder=new File(path);
                if(tmpFolder.exists()){
                    if(tmpFolder.isDirectory()){
                        tmpFolderReference.set(tmpFolder);
                        Executor.executeWait(new TmpFileCleaner(), TMP_FILE_CLEANER_RUN_INTERVAL);
                        return tmpFolder;                        
                    }
                    tmpFolder.delete();
                } 
            }
            tmpFolder=File.createTempFile("fgos", "");
            if(!(tmpFolder.delete()))
            {
                throw new IOException("Could not delete temp file: " + tmpFolder.getAbsolutePath());
            }

            if(!(tmpFolder.mkdir()))
            {
                throw new IOException("Could not create temp directory: " + tmpFolder.getAbsolutePath());
            }
            System.setProperty("fgosTmpFolder", tmpFolder.getAbsolutePath());
            tmpFolderReference.set(tmpFolder);
            Executor.executeWait(new TmpFileCleaner(), TMP_FILE_CLEANER_RUN_INTERVAL);
            return tmpFolder;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Create a new buffer and store the
     * current one in linked list
     */
    protected void addBuffer() {
        if (buffers == null) {
            buffers = new LinkedList<byte[]>();
        }

        buffers.addLast(buffer);

        buffer = new byte[blockSize];
        size += index;
        index = 0;
    }
    
    public InputStream getInputStream(){        
        if(file!=null){
            try{
                if(!closed)
                    this.close();
                return new FileInputStream(file);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
        return new FastInputStream();
    }
    
    class FastInputStream extends InputStream{
        int offsetInStream=0;
        int size;
        Iterator<byte[]> bufferIterator=null;
        byte[] curBuffer;
        
        public FastInputStream() {
            this.size=getSize();
            if(buffers!=null){
                bufferIterator=buffers.iterator();
            }
            curBuffer=getNextBuffer();
        }
        
        byte[] getNextBuffer(){
            if(bufferIterator!=null){
                if(bufferIterator.hasNext())
                    return bufferIterator.next();
                return buffer;
            }
            return buffer;
        }
        
        @Override
        public int read() throws IOException {
            if(offsetInStream>=size)
                return -1;
            byte byteRead=curBuffer[offsetInStream++%buffer.length];
            if(offsetInStream%buffer.length==0)
                curBuffer=getNextBuffer();
            return byteRead & 0xFF;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int offSetInStreamAtStart=offsetInStream;
            if(off+len>b.length)
                throw new IndexOutOfBoundsException("off + len > b.length");
            if(offsetInStream+len>size){
                len=size-offsetInStream;
            }
            int offsetInBuffer;           
            int bytesToReadFromBuffer=0;
            //attempt to read len bytes
            while(len>0){
                offsetInBuffer=offsetInStream%buffer.length;
                bytesToReadFromBuffer=buffer.length-offsetInBuffer;
                if(len<bytesToReadFromBuffer)
                    bytesToReadFromBuffer=len;
                //read bytes from buffer
                System.arraycopy(curBuffer, offsetInBuffer, b, off, bytesToReadFromBuffer);
                off+=bytesToReadFromBuffer;
                len-=bytesToReadFromBuffer;
                offsetInStream+=bytesToReadFromBuffer;
                if(offsetInBuffer+bytesToReadFromBuffer>=buffer.length)
                    curBuffer=getNextBuffer();
            }
            return offsetInStream-offSetInStreamAtStart;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }        
    }
    
    static class TmpFileCleaner implements Runnable{

        @Override
        public void run() {
            try{
                File tmpFolder=null;
                try{
                    tmpFolder=getTmpFolder();
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }
                File[] files=tmpFolder.listFiles();
                long curTime=System.currentTimeMillis();
                for(File file:files){
                    if(curTime-file.lastModified()>MAX_LIFE_OF_FILE_MILLIS){
                        file.delete();
                    }                    
                } 
            }finally{
                Executor.executeWait(this, TMP_FILE_CLEANER_RUN_INTERVAL);
            }
        }        
    }
    
}