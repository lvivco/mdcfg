/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;

import java.util.Map;
import java.util.function.Function;

/**
 * Interface for config source implementations.
 */
public interface Source {
    /** Read properties into Map object */
    Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor, boolean isCaseSensitive) throws MdcException;
    /** Set up change watcher */
    void observeChange(Runnable onChange, long reloadInterval) throws MdcException;

    /**
     * Stop watching for source changes. Default implementation does nothing
     * allowing stream-based sources to ignore this call.
     */
    default void stopWatching() {
        // no-op
    }
}
