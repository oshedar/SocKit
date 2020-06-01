/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Hoshedar Irani
 */
public class StoppableThreadPoolExecutor extends ThreadPoolExecutor{

    public StoppableThreadPoolExecutor(int i, int i1, long l, TimeUnit tu, BlockingQueue<Runnable> bq) {
        super(i, i1, l, tu, bq);
    }

    public StoppableThreadPoolExecutor(int i, int i1, long l, TimeUnit tu, BlockingQueue<Runnable> bq, ThreadFactory tf) {
        super(i, i1, l, tu, bq, tf);
    }

    public StoppableThreadPoolExecutor(int i, int i1, long l, TimeUnit tu, BlockingQueue<Runnable> bq, RejectedExecutionHandler reh) {
        super(i, i1, l, tu, bq, reh);
    }

    public StoppableThreadPoolExecutor(int i, int i1, long l, TimeUnit tu, BlockingQueue<Runnable> bq, ThreadFactory tf, RejectedExecutionHandler reh) {
        super(i, i1, l, tu, bq, tf, reh);
    }

    private Map<Thread,ExecutingTask> executingTasks=new ConcurrentHashMap(125);
    
    @Override
    protected void afterExecute(Runnable r, Throwable thrwbl) {
        //remove thread and runnable from the set
        executingTasks.remove(Thread.currentThread());
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable r) {
        //add thread and runnable to set 
        executingTasks.put(thread, new ExecutingTask(System.currentTimeMillis(), r));
    }
    
    void stopThread(Thread thread){
        //stop thread and remove thread from set
        ExecutingTask task = null;
        if(Thread.currentThread()!=thread){
            task=executingTasks.remove(thread);
            String stackTrace=Utils.toLines(thread.getStackTrace());
            thread.stop();
            //log details
            StringBuilder sb=new StringBuilder(256);
            sb.append("Thread forcibly stopped. ");
            if(task!=null){
                sb.append("Runnable ").append(task.runnable.getClass().getName()).append(". ");
                sb.append("Task Duration= ").append((System.currentTimeMillis()-task.startTime));                
            }
            sb.append("\n").append(stackTrace);
            if(!isShutdown())
                Utils.log(sb.toString());
            else
                System.err.println(sb.toString());
        }
    }
    
    void stopAllThreads(){
        Set<Thread> threads=executingTasks.keySet();
        for(Thread thread:threads)
            stopThread(thread);
    }
    
    @Override
    public void shutdown(){
        try{
            super.shutdown();
            awaitTermination(30, TimeUnit.SECONDS);            
        }catch(Exception ex){}
        finally{
            stopAllThreads();
            try{shutdownNow();}catch(Exception ex){}            
        }
        
    }

}
