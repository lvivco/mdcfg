/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
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
    Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor) throws MdcException;
    /** Set up change watcher */
    void observeChange(Runnable onChange, long reloadInterval) throws MdcException;
}
