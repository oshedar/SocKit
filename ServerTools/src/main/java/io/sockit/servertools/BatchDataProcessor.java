/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Hoshedar Irani
 */
public class BatchDataProcessor implements Runnable{
    final private ConcurrentLinkedQueue<Object> dataQ=new ConcurrentLinkedQueue();
    final private AtomicBoolean runnableStarted=new AtomicBoolean(false);
    final private AtomicBoolean qProcessorRunning=new AtomicBoolean(false);
    final private Runnable qProcessor;
    private boolean destroyed=false;
    public final int waitTimeMillis;
    public final int maxQSize;
    
    final DataProcessor serializer;

    public BatchDataProcessor(DataProcessor dataProcessor,int maxQSize,int waitTimeMillis) {
        this.waitTimeMillis = waitTimeMillis;
        this.serializer = dataProcessor;
        this.maxQSize=maxQSize;
        this.qProcessor=Executor.newWaitRunnable(this);
    }

    public void process(Object data) {
        dataQ.add(data);
        if(runnableStarted.compareAndSet(false, true))
            Executor.executeWait(qProcessor,waitTimeMillis);
        else if(dataQ.size()>=maxQSize){
            if(qProcessorRunning.compareAndSet(false, true))
                Executor.execute(this);
        }
    }

    public synchronized void destroy() {
        if(destroyed)
            return;
        run();
        destroyed=true;
    }

    @Override
    public void run() {
        if(destroyed)
            return;
        try{
            qProcessorRunning.set(true);
            if(dataQ.isEmpty())
                return;
            Object data;
            while((data=dataQ.poll())!=null){
                serializer.process(data);
            }
        }catch(Exception ex){
            Console.log(ex);
        }
        finally{
            runnableStarted.set(false);
            qProcessorRunning.set(false);
        }
    }
    
    
}
