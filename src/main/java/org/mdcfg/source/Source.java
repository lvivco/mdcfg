/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;

import java.util.Map;
import java.util.function.Function;
import org.mdcfg.model.Config;

/**
 * Interface for config source implementations.
 */
public interface Source {
    /** Read properties into Map object */
    Map<String, Map<String, String>> read(
            Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor,
            Config config) throws MdcException;
    /** Set up change watcher */
    void observeChange(Runnable onChange, long reloadInterval) throws MdcException;

    /**
     * Stop watching for source changes. Default implementation does nothing
     * allowing stream-based sources to ignore this call.
     */
    default void stopAutoReload() {
        // no-op
    }
}
