/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.utils.SourceUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *  Process data form source and create appropriate properties with corresponding chains.
 */
public class Processor {

    private static final Pattern SUB_PROPERTY_PATTERN = Pattern.compile(":");
    private static final String SUB_PROPERTY_SEPARATOR = ".";
    private static final Pattern ALIASES = Pattern.compile("aliases(?:\\:|$).*$");
    private static final Pattern INCLUDES = Pattern.compile("includes(?:\\:|$).*$");
    private static final Pattern PROPERTY = Pattern.compile("^(?:(?!(?:aliases|includes)(?:\\:|$)).)*$");
    private static final String ALIAS_REPLACER = "(?:\\[|^|\\s|,|@)(%s)(?:,|\\s|]|$)";

    private final List<Hook> loadHooks;

    public Processor(List<Hook> loadHooks) {
        this.loadHooks = loadHooks;
    }

    /**
     * Process data including aliases and calling appropriate hooks.
     *
     * @param data Map configuration to be processed.
     * @return Map of properties.
     * @throws MdcException thrown in case something went wrong.
     */
    public Map<String, Property> process(Map<String, Map<String, String>> data) throws MdcException {
        Map<String, List<Alias>> aliases = getAliases(data);
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            if(PROPERTY.matcher(entry.getKey()).matches()) {
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

    /** Parse config to find additional config sources configured in {@code includes} tag. */
    public Map<String, String> getIncludes(Map<String, Map<String, String>> data) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            if(INCLUDES.matcher(entry.getKey()).matches()) {
                for (Map.Entry<String, String> e : entry.getValue().entrySet()) {
                    result.put(entry.getKey() + SUB_PROPERTY_SEPARATOR + e.getKey(), e.getValue());
                }
            }
        }
        return result;
    }

    /**
     *  Replace aliases with their substitutors.
     *
     *  <p> For example alias and property:
     *  <pre>
     *    aliases:
     *      model@bmw:
     *        cat@crossover: line@x5
     *
     *    horsepower:
     *      any@: 400
     *      line@x5: 500
     *  </pre>
     *  after processing final config will be following:
     *  <pre>
     *    horsepower:
     *      any@: 400
     *      model@bmw:
     *        cat@crossover: 500
     *  </pre>
     */
    private Map<String, String> processAliases(Map<String, String> selectors, Map<String, List<Alias>> aliases) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> selectorEntry : selectors.entrySet()) {
            StringBuilder selectorKey = new StringBuilder();
            String[] split = SUB_PROPERTY_PATTERN.split(selectorEntry.getKey());
            for (String subSelector : split) {
                List<Alias> dimensionAliases = aliases.get(SourceUtils.splitSelector(subSelector).getKey());
                if(dimensionAliases != null){
                    for (Alias alias : dimensionAliases) {
                        subSelector = SourceUtils.replaceGroup(alias.getFrom(), 1, subSelector, alias.getTo());
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

    /** Parse config to find aliases */
    private Map<String, List<Alias>> getAliases(Map<String, Map<String, String>> data) {
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            if(ALIASES.matcher(entry.getKey()).matches()) {
               return entry.getValue().entrySet().stream()
                       .map(this::createAlias)
                       .collect(Collectors.groupingBy(Alias::getTargetDimension));
            }
        }
        return Map.of();
    }

    /** Create alias helper */
    private Alias createAlias(Map.Entry<String, String> entry) {
        Pair<String, String> from = SourceUtils.splitSelector(entry.getValue().toLowerCase(Locale.ROOT));
        Pair<String, String> to = SourceUtils.splitSelector(entry.getKey());
        // if "from" dimension equals "to" dimension then replace only selectors
        if(from.getKey().equals(to.getKey())) {
            return new Alias(from.getKey(), compileAliasMatcher(from.getValue()), to.getValue());
        } else {
            return new Alias(from.getKey(), compileAliasMatcher(entry.getValue().toLowerCase(Locale.ROOT)), entry.getKey());
        }
    }

    private Pattern compileAliasMatcher(String value) {
        return Pattern.compile(String.format(ALIAS_REPLACER, Pattern.quote(value)));
    }
}
