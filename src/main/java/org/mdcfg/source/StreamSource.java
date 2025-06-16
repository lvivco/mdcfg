/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

/** Base class for all File based source implementations */
public abstract class StreamSource implements Source {

    private InputStream sourceStream;

    protected StreamSource() {}

    protected StreamSource(InputStream is) {
        this.sourceStream = is;
    }

    @Override
    public Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor, boolean isCaseSensitive) throws MdcException {
        Map<String, Map<String, String>> main = read(sourceStream, isCaseSensitive);
        List<InputStream> streams = new ArrayList<>();
        for (String value : includesExtractor.apply(main).values()) {
            streams.add(toInputStream(value));
        }
        return readAndMerge(streams, main, isCaseSensitive);
    }

    /** Read one file */
    abstract Map<String, Map<String, String>> read(InputStream is, boolean isCaseSensitive) throws MdcException;

    private InputStream toInputStream(String source) throws MdcException {
        try {
            return new URL(source).openStream();
        } catch (IOException e) {
            throw new MdcException("Invalid config source url.", e);
        }
    }

    /** Read properties from files and merge them into one Map */
    protected Map<String, Map<String, String>> readAndMerge(List<InputStream> inputStreams, Map<String, Map<String, String>> merged, boolean isCaseSensitive) throws MdcException {
        for (InputStream is : inputStreams) {
            Map<String, Map<String, String>> map = read(is, isCaseSensitive);
            Set<String> interfileKeys = getInterfileKeys(map, merged);
            if(!interfileKeys.isEmpty()){
                throw new MdcException(String.format("There is interfile configuration for keys %s", interfileKeys));
            }
            merged.putAll(map);
        }
        return merged;
    }

    /** Get list of properties that exists in different source files */
    private Set<String> getInterfileKeys(Map<String, Map<String, String>> map, Map<String, Map<String, String>> merged) {
        Set<String> intersection = new HashSet<>(merged.keySet());
        intersection.retainAll(map.keySet());
        return intersection;
    }
}