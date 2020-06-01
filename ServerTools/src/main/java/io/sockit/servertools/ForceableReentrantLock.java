/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * A reentrant lock which has a variant of tryLock (forceLock) which forces the lock to be acquired within the specified time. This works only with tasks executing on the executor
 */
public class ForceableReentrantLock extends ReentrantLock{

    /**
     *
     * Acquires the lock in the specified time. if the lock is not acquired the thread holding the lock will be killed and the lock will be acquired
     */
    public void forceLock(int waitTimeMIllis){
        try{
            while(!this.tryLock(waitTimeMIllis, TimeUnit.MILLISECONDS)){
                Executor.stopThread(this.getOwner());
            }
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }
}
