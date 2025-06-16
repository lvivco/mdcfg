/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Source implementation for Json based config files */
public class JsonSource extends FileSource {
    private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<>() {};

    public JsonSource(InputStream stream) {
        super(stream);
    }

    public JsonSource(File file) {
        super(file);
    }

    public JsonSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> read(InputStream is, boolean isCaseSensitive) throws MdcException {
        try (is) {
            Map<String, Object> rawData = new ObjectMapper().readValue(is, TYPE);
            Map<String, Object> flattened = SourceUtils.flatten(rawData, isCaseSensitive);
            return SourceUtils.collectProperties(flattened);
        } catch (Exception e) {
            throw new MdcException("Couldn't read input source", e);
        }
    }

    @Override
    File[] listFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
    }
}
