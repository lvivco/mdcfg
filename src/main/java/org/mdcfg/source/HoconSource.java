/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HoconSource extends FileSource {

    private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<>() {};
    private static final String MARKER = "\u200B";
    private final Pattern pattern = Pattern.compile("\"(.*@)");

    public HoconSource(InputStream stream) {
        super(stream);
    }

    public HoconSource(File file) {
        super(file);
    }

    public HoconSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> read(InputStream is, boolean isCaseSensitive) throws MdcException {
        try (is) {
            String inputString = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String data = numberRows(inputString);
            Map<String, Object> rawData = new ObjectMapper(new HoconFactory()).readValue(data, TYPE);
            Map<String, Object> flattened = SourceUtils.flatten(orderData(rawData), isCaseSensitive);
            return SourceUtils.collectProperties(flattened);
        } catch (IOException e) {
            throw new MdcException("Couldn't read input source", e);
        }
    }

    @Override
    File[] listFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".conf"));
    }

    private String numberRows(String sourceData) {
        Matcher matcher = pattern.matcher(sourceData);
        StringBuilder res = new StringBuilder();
        int rowNumber = 0;

        while(matcher.find()) {
            matcher.appendReplacement(res, "\"" + rowNumber + MARKER + matcher.group(1));
            rowNumber++;
        }
        matcher.appendTail(res);
        return res.toString();
    }

    private Map<String, Object> orderData(Map<String, Object> rawData) {
        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            if (!entry.getKey().contains(MARKER) && entry.getValue() instanceof Map) {
                entry.setValue(orderData((Map<String, Object>) entry.getValue()));
            } else if (entry.getKey().contains(MARKER)) {
                return rawData.entrySet().stream()
                        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().split(MARKER), e.getValue()))
                        .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey()[0])))
                        .collect(Collectors.toMap(e -> e.getKey()[1],
                                e -> e.getValue() instanceof Map
                                        ? orderData((Map<String, Object>)e.getValue())
                                        : e.getValue(),
                                (k1, k2) -> k1,
                                LinkedHashMap::new));
            }
        }
        return rawData;
    }
}
