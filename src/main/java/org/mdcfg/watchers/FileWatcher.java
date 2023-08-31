/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg.watchers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** Periodically checks whether file is changed by its modification time */
public class FileWatcher implements Watcher {
    private final List<File> files;
    private final long interval;
    private final Runnable onChange;
    private boolean isWatching = true;

    public FileWatcher(List<File> files, Runnable onChange, long interval) {
        this.files = files;
        this.onChange = onChange;
        this.interval = interval;
    }

    @Override
    public void start() {
        isWatching = true;
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {
            try {
                Map<File, Long> original = files.stream()
                        .collect(Collectors.toMap(f->f, File::lastModified, (existing, replacement) -> existing));
                while (isWatching) {
                    Thread.sleep(interval);
                    boolean changed = original.entrySet().stream().anyMatch(e -> e.getKey().lastModified() > e.getValue());
                    if (changed) {
                        onChange.run();
                        original.replaceAll((k, v) -> k.lastModified());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        executorService.shutdown();
    }

    @Override
    public void stop() {
        isWatching = false;
    }
}
