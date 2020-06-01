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
class ExecutingTask {
        public long startTime;
        public Runnable runnable;

        ExecutingTask(long startTime, Runnable runnable) {
            this.startTime = startTime;
            this.runnable = runnable;
        }
    
}
