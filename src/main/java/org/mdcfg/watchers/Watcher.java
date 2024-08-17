/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.watchers;

/** Periodically checks whether source is changed */
public interface Watcher {
    void start();
    void stop();
}
