package com.mdcfg.watchers;

import java.util.Timer;
import java.util.TimerTask;

public class DelayTimer {

    private final long delay;
    private final Timer timer;
    private Task timerTask;
    private boolean started;
    private final Runnable executor;

    class Task extends TimerTask {
        @Override
        public void run() {
            executor.run();
            started = false;
        }
    }

    public DelayTimer(Runnable executor, long delay) {
        this.executor = executor;
        this.delay = delay;
        timer = new Timer("Delay Timer");
    }

    public void schedule(){
        if(!started) {
            timerTask = new Task();
            timer.schedule(timerTask, delay);
            started = true;
        }
    }

    public void cancel(){
        if(started) {
            timer.cancel();
        }
    }
}
