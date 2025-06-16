package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.builder.MdcCallback;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;

import java.util.List;

/**
 * Container for provider internal configuration.
 */
@Getter
@AllArgsConstructor
public class Config {
    private final boolean autoReload;
    private final long reloadInterval;
    private final MdcCallback<Integer, MdcException> callback;
    private final List<Hook> loadHooks;
    private final boolean keySensitive;
    private final boolean selectorSensitive;
}
