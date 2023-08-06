package org.mdcfg.utils;

import org.mdcfg.exceptions.MdcException;

@FunctionalInterface
public interface SilentFunction<T, R> {
    R apply(T t) throws MdcException;
}

