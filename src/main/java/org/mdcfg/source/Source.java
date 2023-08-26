package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;

import java.util.Map;
import java.util.function.Function;

public interface Source {
    Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor) throws MdcException;
    void observeChange(Runnable onChange, long reloadInterval) throws MdcException;
}
