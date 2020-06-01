/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author 
 */
public final class ExecutorThreadFactory implements ThreadFactory{
    private static final int minStackSize=16*1024;
    private int threadStackSize;
    private static AtomicInteger threadCount=new AtomicInteger(0);
    private static AtomicInteger namePrefixGenarator=new AtomicInteger(1000);
    private final String namePrefix;
    
    String getName(){
        return namePrefix + (threadCount.incrementAndGet());
    }

    public ExecutorThreadFactory() {
        this(128*1024);
    }

    public ExecutorThreadFactory(int threadStackSize) {
        this(minStackSize, null);
    }
    
    public ExecutorThreadFactory(int stackSize,String threadNamePrefix) {
        this.threadStackSize = stackSize>=minStackSize?stackSize:minStackSize;
        if(threadNamePrefix!=null){
            threadNamePrefix=threadNamePrefix.trim();
            if(threadNamePrefix.length()<1)
                threadNamePrefix=null;
            else if(threadNamePrefix.length()==1)
                threadNamePrefix+='_';
        }
        if(threadNamePrefix!=null)
            this.namePrefix=threadNamePrefix;
        else
            this.namePrefix="e" + Integer.toHexString(namePrefixGenarator.incrementAndGet())+"_";        
        
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(null, r, getName(), threadStackSize);
    }
        
}
