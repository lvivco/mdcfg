package com.mdcfg.processor;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.Hook;
import com.mdcfg.model.Property;
import com.mdcfg.source.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Processor {

    private static final Pattern SUB_PROPERTY_PATTERN = Pattern.compile(":");
    private static final String SUB_PROPERTY_SEPARATOR = ".";

    private final List<Hook> loadHooks;

    public Processor(List<Hook> loadHooks) {
        this.loadHooks = loadHooks;
    }

    public Map<String, Property> process(Source source) throws MdcException {
        Map<String, Map<String, String>> data = source.read();
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String propertyName = SUB_PROPERTY_PATTERN.matcher(entry.getKey()).replaceAll(SUB_PROPERTY_SEPARATOR);
            List<Hook> appropriateHooks = loadHooks.stream()
                    .filter(hook -> hook.getPattern().matcher(propertyName).matches())
                    .collect(Collectors.toList());
            Property property = new PropertyProcessor(propertyName, appropriateHooks)
                    .getProperty(entry.getValue());
            properties.put(entry.getKey(), property);
        }
        return properties;
    }
}
