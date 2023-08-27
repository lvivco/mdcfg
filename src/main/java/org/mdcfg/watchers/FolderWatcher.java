/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg.watchers;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/** Periodically checks whether folder is changed by its modification time */
public class FolderWatcher implements Watcher, Runnable{
    private final String path;
    private final DelayTimer timer;
    private Thread thread;

    public FolderWatcher(String path, Runnable onChange, long interval) {
        this.path = path;
        timer = new DelayTimer(onChange, interval);
    }

    @Override
    public void start(){
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try ( WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path watchPath = Paths.get(path);
            watchPath.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                List<WatchEvent<?>> watchEvents = key.pollEvents();
                if(!watchEvents.isEmpty()) {
                    timer.schedule();
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        timer.cancel();
        thread.interrupt();
    }
}
