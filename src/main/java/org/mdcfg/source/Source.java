package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;

import java.util.Map;

public interface Source {
    Map<String, Map<String, String>> read() throws MdcException;
    void observeChange(Runnable onChange, long reloadInterval) throws MdcException;
}
