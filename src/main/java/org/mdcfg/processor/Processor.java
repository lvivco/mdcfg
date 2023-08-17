package org.mdcfg.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.utils.SourceUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Processor {

    private static final Pattern SUB_PROPERTY_PATTERN = Pattern.compile(":");
    private static final String SUB_PROPERTY_SEPARATOR = ".";
    private static final String ALIASES = "aliases";

    private static final String SELECTOR_SEPARATOR= "@";

    private final List<Hook> loadHooks;

    public Processor(List<Hook> loadHooks) {
        this.loadHooks = loadHooks;
    }

    public Map<String, Property> process(Map<String, Map<String, String>> data) throws MdcException {
        Map<String, List<Alias>> aliases = getAliases(data);
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


    private Map<String, String> processAliases(Map<String, String> selectors, Map<String, List<Alias>> aliases) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> selectorEntry : selectors.entrySet()) {
            StringBuilder selectorKey = new StringBuilder();
            String[] split = SUB_PROPERTY_PATTERN.split(selectorEntry.getKey());
            for (String subSelector : split) {
                List<Alias> dimensionAliases = aliases.get(SourceUtils.splitSelector(subSelector).getKey());
                if(dimensionAliases != null){
                    for (Alias alias : dimensionAliases) {
                        subSelector = subSelector.replace(alias.getFrom(), alias.getTo());
                    }
                }
                if(selectorKey.length() > 0){
                    selectorKey.append(SUB_PROPERTY_PATTERN.pattern());
                }
                selectorKey.append(subSelector);
            }
            result.put(selectorKey.toString(), selectorEntry.getValue());
        }
        return result;
    }

    private Map<String, List<Alias>> getAliases(Map<String, Map<String, String>> data) {
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String propertyName = entry.getKey();
            if(propertyName.startsWith(ALIASES)) {
               return entry.getValue().entrySet().stream()
                       .map(this::createAlias)
                       .collect(Collectors.groupingBy(Alias::getTargetDimension));
            }
        }
        return Map.of();
    }

    private Alias createAlias(Map.Entry<String, String> entry) {
        Pair<String, String> from = SourceUtils.splitSelector(entry.getValue());
        Pair<String, String> to = SourceUtils.splitSelector(entry.getKey());
        // if from dimension equals to dimension then replace only selectors
        if(from.getKey().equals(to.getKey())) {
            return new Alias(from.getKey(), from.getValue(), to.getValue());
        } else {
            return new Alias(from.getKey(), entry.getValue(), entry.getKey());
        }
    }
}
