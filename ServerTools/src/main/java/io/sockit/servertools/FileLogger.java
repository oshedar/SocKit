/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Hoshedar Irani
 */
public class FileLogger implements Logger, Runnable{
    final ConcurrentLinkedQueue<LogMesg> logs=new ConcurrentLinkedQueue();
    final private AtomicBoolean runnableStarted=new AtomicBoolean(false);
    final Runnable qProcessor;
    private AtomicBoolean destroyed=new AtomicBoolean(false);
    final File logFile;
    final static int waitTimeMillis=1000*60*1;
    static final int maxLogFileSize=1024*10000;
    static final Charset utf_8=Charset.forName("UTF-8");
    private static SimpleDateFormat timeFormat;            
    static final private byte[] newLine="\r\n".getBytes(Charset.forName("UTF-8"));
    static final private byte[] colonSpace=": ".getBytes(Charset.forName("UTF-8"));
    public final boolean timestampEnabled;
    
    public FileLogger(String logFile,boolean enableTimestamp) {
        if(logFile!=null){
            this.logFile=new File(logFile);
            File parent=this.logFile.getParentFile();
            if(!parent.exists())
                parent.mkdirs();
        }
        else{
            throw new NullPointerException("logFile cant be null");
        }
        this.qProcessor=Executor.newWaitRunnable(this);
        this.timestampEnabled=enableTimestamp;
        if(enableTimestamp && timeFormat==null){
            timeFormat=new SimpleDateFormat("yyyy.MM.dd HH:mm z");
            timeFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        }
        
    }

    @Override
    public void log(String mesg) {
        logs.add(new LogMesg(mesg));
        if(runnableStarted.compareAndSet(false, true)){
            if(!destroyed.get())
                Executor.executeWait(qProcessor,waitTimeMillis);
        }
    }

    private static String getStackTrace(Throwable ex){
        StringWriter stringWriter=new StringWriter(512);
        PrintWriter printWriter=new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        return stringWriter.toString();       
    }

    @Override
    public void log(Throwable ex) {
        logs.add(new LogMesg(getStackTrace(ex)));
        if(runnableStarted.compareAndSet(false, true)){
            if(!destroyed.get())
                Executor.executeWait(qProcessor,waitTimeMillis);
        }
    }

    @Override
    public synchronized void destroy() {
        if(!destroyed.compareAndSet(false, true))
            return;
        run();        
        Executor.removeExecuteWait(qProcessor);
    }
    
    @Override
    public synchronized void run() {
        OutputStream os=null;
        try{
            if(logs.isEmpty())
                return;
            if(logFile.length()>maxLogFileSize)
                trimLogFile();
            os=new FileOutputStream(logFile, true);
            Date date=new Date();
            LogMesg log;
            while((log=logs.poll())!=null){
                if(timestampEnabled){
                    date.setTime(log.loggedTime);
                    os.write(timeFormat.format(date).getBytes());
                    os.write(colonSpace);
                }
                os.write(log.mesg.getBytes(utf_8));
                os.write(newLine);
            }
        }catch(Exception ex){
            Console.log(ex);
        }
        finally{
            if(os!=null){
                try{os.close();}catch(Exception ex){}
            }
            if(!logs.isEmpty() && !destroyed.get()){
                Executor.executeWait(qProcessor,waitTimeMillis);                
            }
            else 
                runnableStarted.set(false);
        }
    }
    
    void trimLogFile(){
        File tempFile=null;
        boolean success=false;
        FileOutputStream fos=null;
        FileInputStream fis=null;
        try{
            tempFile=File.createTempFile("log", ".tmp");
            fos=new FileOutputStream(tempFile);
            fis=new FileInputStream(logFile);
            fis.skip(logFile.length()-maxLogFileSize/2);
            save(fis,fos);
            success=true;
        }catch(IOException ex){
            Console.log(ex);
        }finally{
            if(fos!=null){try{fos.close();}catch(Exception ex){}}
            if(fis!=null){try{fis.close();}catch(Exception ex){}}
        }
        if(success){
            logFile.delete();
            tempFile.renameTo(logFile);
        }
    }
    
    private static void save(InputStream is,OutputStream os) throws IOException{
        byte[] buffer=new byte[2048];
        int charsRead=0;
        while ((charsRead = is.read(buffer)) >0){
            os.write(buffer, 0, charsRead);
        }        
    }
    
    static class LogMesg{
        final long loggedTime;
        final String mesg;

        LogMesg(String mesg) {
            this.mesg = mesg;
            this.loggedTime=System.currentTimeMillis();
        }
    }
    
}
