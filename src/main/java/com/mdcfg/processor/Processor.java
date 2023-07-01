package com.mdcfg.processor;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.Property;
import com.mdcfg.source.Source;

import java.util.HashMap;
import java.util.Map;

public class Processor {

    public Processor() {}

    public Map<String, Property> process(Source source) throws MdcException {
        Map<String, Map<String, String>> data = source.read();
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            Property property = new PropertyProcessor(entry.getKey()).getProperty(entry.getValue());
            properties.put(entry.getKey(), property);
        }
        return properties;
    }
}
