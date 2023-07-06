package com.mdcfg.processor;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.Hook;
import com.mdcfg.model.Property;
import com.mdcfg.source.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor {

    private final List<Hook> loadHooks;

    public Processor(List<Hook> loadHooks) {
        this.loadHooks = loadHooks;
    }

    public Map<String, Property> process(Source source) throws MdcException {
        Map<String, Map<String, String>> data = source.read();
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            Property property = new PropertyProcessor(entry.getKey()).getProperty(entry.getValue());
            applyLoadHooks(property);
            properties.put(entry.getKey(), property);
        }
        return properties;
    }

    private void applyLoadHooks(Property property) {
        loadHooks.stream()
                .filter(hook -> hook.getPattern().matcher(property.getName()).matches())
                .forEach(hook -> property.getChains()
                        .forEach(chain -> hook.getConsumer().accept(chain)));
    }
}
