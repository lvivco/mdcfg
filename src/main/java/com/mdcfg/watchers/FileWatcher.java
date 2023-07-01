package com.mdcfg.watchers;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileWatcher implements Watcher {

    private final File file;
    private final long interval;
    private final Runnable onChange;
    private boolean isWatching = true;

    public FileWatcher(String path, Runnable onChange, long interval) {
        file = new File(path);
       this.onChange = onChange;
       this.interval = interval;
    }

    @Override
    public void start() {
        isWatching = true;
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {
            try {
                long originTime = file.lastModified();
                while (isWatching) {
                    Thread.sleep(interval);
                    long currentModTime = file.lastModified();
                    if (currentModTime > originTime) {
                        onChange.run();
                        originTime = currentModTime;
                    }
                }
            } catch (InterruptedException e) {
            }
        });
        executorService.shutdown();
    }

    @Override
    public void stop() {
        isWatching = false;
    }
}
