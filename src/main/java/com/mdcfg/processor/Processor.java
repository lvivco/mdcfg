package com.mdcfg.processor;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.Property;
import com.mdcfg.source.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Processor {

    private static final Pattern SUB_PROPERTY_PATTERN = Pattern.compile(":");
    private static final String SUB_PROPERTY_SEPARATOR = ".";

    public Map<String, Property> process(Source source) throws MdcException {
        Map<String, Map<String, String>> data = source.read();
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String propertyName = SUB_PROPERTY_PATTERN.matcher(entry.getKey()).replaceAll(SUB_PROPERTY_SEPARATOR);
            Property property = new PropertyProcessor(propertyName).getProperty(entry.getValue());
            properties.put(propertyName, property);
        }
        return properties;
    }
}
