package com.mdcfg.processor;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.Hook;
import com.mdcfg.model.Property;
import com.mdcfg.source.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Processor {

    private final List<Hook> loadHooks;

    public Processor(List<Hook> loadHooks) {
        this.loadHooks = loadHooks;
    }

    public Map<String, Property> process(Source source) throws MdcException {
        Map<String, Map<String, String>> data = source.read();
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            List<Hook> appropriateHooks = loadHooks.stream()
                    .filter(hook -> hook.getPattern().matcher(entry.getKey()).matches())
                    .collect(Collectors.toList());
            Property property = new PropertyProcessor(entry.getKey(), appropriateHooks)
                    .getProperty(entry.getValue());
            properties.put(entry.getKey(), property);
        }
        return properties;
    }

    /*private void applyLoadHooks(Property property) {
        loadHooks.stream()
                .filter(hook -> hook.getPattern().matcher(property.getName()).matches())
                .forEach(hook -> property.getChains()
                        .forEach(chain -> hook.getConsumer().accept(chain)));
    }*/
}
