package org.mdcfg.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HoconSource extends FileSource {

    private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<>() {};
    private final Pattern pattern = Pattern.compile("[a-zA-Z]*@");

    public HoconSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> readFile(File source, boolean isCaseSensitive) throws MdcException {
        try {
            String data = numberRows(Files.readString(source.toPath()));
            Map<String, Object> rawData = new ObjectMapper(new HoconFactory()).readValue(data, TYPE);

            for (Map.Entry<String, Object> entry : rawData.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    entry.setValue(orderData((Map<String, Object>)entry.getValue()));
                }
            }

            Map<String, Object> flattened = SourceUtils.flatten(rawData, isCaseSensitive);
            return SourceUtils.collectProperties(flattened);
        } catch (IOException e) {
            throw new MdcException(String.format("Couldn't read source %s.", source.getAbsolutePath()), e);
        }
    }

    @Override
    File[] extractFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".conf"));
    }

    private String numberRows(String sourceData) {
        Matcher matcher = pattern.matcher(sourceData);
        StringBuilder res = new StringBuilder();
        int rowNumber = 0;

        while(matcher.find()) {
            matcher.appendReplacement(res, rowNumber + matcher.group());
            rowNumber++;
        }
        matcher.appendTail(res);
        return res.toString();
    }

    private Map<String, Object> orderData(Map<String, Object> rawData) {
        return rawData.entrySet().stream()
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(0, 1))))
                .collect(Collectors.toMap(e -> e.getKey().substring(1),
                        e -> e.getValue() instanceof Map
                                ? orderData((Map<String, Object>)e.getValue())
                                : e.getValue(),
                        (k1, k2) -> k1,
                        LinkedHashMap::new));
    }
}
