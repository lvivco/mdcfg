package org.mdcfg.processor;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Processor {

    private static final Pattern SUB_PROPERTY_PATTERN = Pattern.compile(":");
    private static final String SUB_PROPERTY_SEPARATOR = ".";
    private static final String ALIASES= "aliases";

    private final List<Hook> loadHooks;

    public Processor(List<Hook> loadHooks) {
        this.loadHooks = loadHooks;
    }

    public Map<String, Property> process(Map<String, Map<String, String>> data) throws MdcException {
        Map<String, String> aliases = getAliases(data);
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            if(!entry.getKey().startsWith(ALIASES)) {
                String propertyName = SUB_PROPERTY_PATTERN.matcher(entry.getKey()).replaceAll(SUB_PROPERTY_SEPARATOR);
                List<Hook> appropriateHooks = loadHooks.stream()
                        .filter(hook -> hook.getPattern().matcher(propertyName).matches())
                        .collect(Collectors.toList());
                Map<String, String> processedSelectors = processAliases(entry.getValue(), aliases);
                Property property = new PropertyProcessor(propertyName, appropriateHooks)
                        .getProperty(processedSelectors);
                properties.put(propertyName, property);
            }
        }
        return properties;
    }


    private Map<String, String> processAliases(Map<String, String> selectors, Map<String, String> aliases) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> selectorEntry : selectors.entrySet()) {
            String selectorKey = selectorEntry.getKey();
            for (Map.Entry<String, String> aliasEntry : aliases.entrySet()) {
                selectorKey = selectorKey.replace(aliasEntry.getValue(), aliasEntry.getKey());
            }
            result.put(selectorKey, selectorEntry.getValue());
        }
        return result;
    }

    private Map<String, String> getAliases(Map<String, Map<String, String>> data) throws MdcException {
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String propertyName = entry.getKey();
            if(propertyName.startsWith(ALIASES)) {
               return entry.getValue();
            }
        }
        return Map.of();
    }
}
