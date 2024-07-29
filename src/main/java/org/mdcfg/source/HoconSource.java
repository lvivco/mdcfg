package org.mdcfg.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class HoconSource extends FileSource {

    private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<>() {};

    public HoconSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> readFile(File source, boolean isCaseSensitive) throws MdcException {
        try (InputStream is = new FileInputStream(source)) {
            Map<String, Object> rawData = new ObjectMapper(new HoconFactory()).readValue(is, TYPE);
            Map<String, Object> sortedRawData = orderData(rawData);
            Map<String, Object> flattened = SourceUtils.flatten(sortedRawData, isCaseSensitive);
            return SourceUtils.collectProperties(flattened);
        } catch (IOException e) {
            throw new MdcException(String.format("Couldn't read source %s.", source.getAbsolutePath()), e);
        }
    }

    @Override
    File[] extractFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".conf"));
    }

    private Map<String, Object> orderData(Map<String, Object> rawData) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> otherData = new LinkedHashMap<>();
        Map.Entry<String, Object> anyEntry = null;

        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                entry.setValue(orderData((Map<String, Object>) entry.getValue()));
            }
            if (entry.getKey().equals("any@")) {
                anyEntry = entry;
            } else {
                otherData.put(entry.getKey(), entry.getValue());
            }
        }

        if (anyEntry != null) {
            result.put(anyEntry.getKey(), anyEntry.getValue());
        }
        result.putAll(otherData);
        return result;
    }
}
