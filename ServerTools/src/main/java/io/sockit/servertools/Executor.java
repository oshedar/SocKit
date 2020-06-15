/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 
 */
public class Executor {
    
    private static volatile StoppableThreadPoolExecutor threadPoolExecutor;
    private static volatile ScheduledThreadPoolExecutor scheduledExecutor;

    public static boolean isInitialized(){
        return threadPoolExecutor!=null;
    }
    
    public static void init(String threadNamePrefix,int coreThreadPoolSize, int maximumThreadPoolSize) {
        init(threadNamePrefix,coreThreadPoolSize, maximumThreadPoolSize, 1024*128);
    }
    
    public static void init(String threadNamePrefix,int coreThreadPoolSize, int maximumThreadPoolSize, int threadStackSize) {
        init(threadNamePrefix,coreThreadPoolSize, maximumThreadPoolSize, threadStackSize, 2);
    }
    
    public static void init(String threadNamePrefix,int coreThreadPoolSize, int maximumThreadPoolSize, int threadStackSize, long keepAliveTimeSecs) {
        if(threadPoolExecutor==null){
            synchronized(Executor.class){
                if(threadPoolExecutor!=null)
                    return;
                int availableProcessors=Runtime.getRuntime().availableProcessors();
                scheduledExecutor= new ScheduledThreadPoolExecutor(availableProcessors>2?2:1, new ExecutorThreadFactory(1024*32, "SE_"));
                threadPoolExecutor=new StoppableThreadPoolExecutor(coreThreadPoolSize, maximumThreadPoolSize, keepAliveTimeSecs, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ExecutorThreadFactory(threadStackSize, threadNamePrefix));
            }
        }
    }
    
    public static void execute(Runnable runnable){
        if(threadPoolExecutor==null)
            Executor.init("te", Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors()*20);
        threadPoolExecutor.execute(runnable);
    }    
    
    public static WaitRunnable executeWait(Runnable task,int delayInMillisSec){
        if(threadPoolExecutor==null)
            Executor.init("te", Runtime.getRuntime().availableProcessors()*2, Runtime.getRuntime().availableProcessors()*20);
        WaitRunnable waitRunnable;
        if(task instanceof WaitRunnable)
            waitRunnable=(WaitRunnable)task;
        else
            waitRunnable=new WaitRunnable(threadPoolExecutor,task);
        scheduledExecutor.schedule(waitRunnable, delayInMillisSec, TimeUnit.MILLISECONDS);
        return waitRunnable;
    }
    
    public static void removeExecuteWait(Runnable runnable){
        if(!(runnable instanceof WaitRunnable))
            return;
        scheduledExecutor.remove(runnable);
    }
    
    public static WaitRunnable newWaitRunnable(Runnable runnable){
        if(threadPoolExecutor==null)
            Executor.init("te", Runtime.getRuntime().availableProcessors()*2, Runtime.getRuntime().availableProcessors()*20);
        return new WaitRunnable(threadPoolExecutor,runnable);
    }
    
    public static void stopThread(Thread thread){
        if(threadPoolExecutor!=null)
            threadPoolExecutor.stopThread(thread);
    }
    
    public static final void shutDown(){        
        synchronized(Executor.class){
            if(threadPoolExecutor==null)
                return;
            try{
                scheduledExecutor.shutdownNow();
            }catch(Exception ex){}
            threadPoolExecutor.shutdown();
            threadPoolExecutor=null;
        }
    }
    
}
