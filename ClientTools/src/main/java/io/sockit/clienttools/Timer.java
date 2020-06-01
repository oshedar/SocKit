/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.clienttools;

/**
 *
 * @author Hoshedar Irani
 */
public class Timer {
    
    private static final java.util.Timer timer=new java.util.Timer();
    
    public static void execute(Runnable runnable){
        timer.schedule(new TimerTask(runnable), 0);
    }
    
    public static void setTimeOut(Runnable runnable,long delay){
        timer.schedule(new TimerTask(runnable), delay);
    }
    
    private static class TimerTask extends java.util.TimerTask{
        Runnable runnable;

        public TimerTask(Runnable runnable) {
            this.runnable = runnable;
        }
        
        @Override
        public void run() {
            runnable.run();
        }
        
    }
}
