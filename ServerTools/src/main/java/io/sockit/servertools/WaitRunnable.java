/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 *
 * @author Hoshedar Irani
 */
public class WaitRunnable implements Runnable {
        public final Runnable task;
        private java.util.concurrent.Executor executor;
        WaitRunnable(java.util.concurrent.Executor executor,Runnable task) {
            this.task = task;
            this.executor=executor;
        }
        
        @Override
        public void run() {
            executor.execute(task);
        }
    
}
