/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.watchers;

import java.util.Timer;
import java.util.TimerTask;

/** Helper class for making delayed async calls */
public class DelayTimer {
    private final long delay;
    private final Timer timer;
    private boolean started;
    private final Runnable executor;

    class Task extends TimerTask {
        @Override
        public void run() {
            executor.run();
            started = false;
        }
    }

    /**
     * Constructor.
     *
     * @param executor Function to be called periodically.
     * @param delay interval in ms.
     */
    public DelayTimer(Runnable executor, long delay) {
        this.executor = executor;
        this.delay = delay;
        timer = new Timer("Delay Timer");
    }

    /** Schedule async delayed calls until someone calls {@code cancel()} */
    public void schedule(){
        if(!started) {
            timer.schedule(new Task(), delay);
            started = true;
        }
    }

    /** Cancel scheduled calls */
    public void cancel(){
        if(started) {
            timer.cancel();
        }
    }
}
