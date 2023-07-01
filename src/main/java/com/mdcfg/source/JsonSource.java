package com.mdcfg.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdcfg.exceptions.MdcException;
import com.mdcfg.utils.SourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class JsonSource extends FileSource {

    private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<>() {};

    public JsonSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> readFile(File source) throws MdcException {
        try (InputStream is = new FileInputStream(source)) {
            Map<String, Object> rawData = new ObjectMapper().readValue(is, TYPE);
            Map<String, Object> flattened = SourceUtils.flatten(rawData);
            return SourceUtils.collectProperties(flattened);
        } catch (IOException e) {
            throw new MdcException(String.format("Couldn't read source %s.", source.getAbsolutePath()), e);
        }
    }

    @Override
    File[] extractFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
    }
}
