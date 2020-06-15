/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Hoshedar Irani
 */
public class Console {
    private final static ConcurrentLinkedQueue<String> logs=new ConcurrentLinkedQueue();
    private final static AtomicBoolean runnableStarted=new AtomicBoolean(false);
    private final static Runnable qProcessor=Executor.newWaitRunnable(new QProcessor());
    final static int waitTimeMillis=4;
//    static final Charset utf_8=Charset.forName("UTF-8");
    private static final byte[] newLine="\r\n".getBytes(Charset.forName("UTF-8"));

    
    public static void log(String mesg) {
        logs.add(mesg==null?"null":mesg);
        if(runnableStarted.compareAndSet(false, true)){
            Executor.executeWait(qProcessor,waitTimeMillis);
        }
    }

    private static String getStackTrace(Throwable ex){
        StringWriter stringWriter=new StringWriter(512);
        PrintWriter printWriter=new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        return stringWriter.toString();       
    }

    public static void log(Throwable ex) {
        logs.add(getStackTrace(ex));
        if(runnableStarted.compareAndSet(false, true)){
            Executor.executeWait(qProcessor,waitTimeMillis);
        }
    }

    private static class QProcessor implements Runnable{
        @Override
        public void run() {
            try{
                PrintStream os=System.out;
                String mesg;
                while((mesg=logs.poll())!=null){
                    os.println(mesg);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            finally{
                runnableStarted.set(false);
            }
        }
    }
    
}
