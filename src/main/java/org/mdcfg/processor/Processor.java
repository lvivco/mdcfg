/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.model.Config;
import org.mdcfg.utils.SourceUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Process data form source and create appropriate properties with corresponding chains.
 */
public class Processor {

    private static final Pattern SUB_PROPERTY_PATTERN = Pattern.compile(":");
    private static final String DIMENSION_SEPARATOR = ":";
    private static final String SUB_PROPERTY_SEPARATOR = ".";
    private static final String ENABLED_PREFIX = "enabled@:";
    private static final String SELECTOR_SEPARATOR= "@";
    private static final String NEGATIVE_SELECTOR= "!";
    private static final Pattern ALIASES = Pattern.compile("aliases(?::|$).*$");
    private static final Pattern INCLUDES = Pattern.compile("includes(?::|$).*$");
    private static final Pattern PROPERTY = Pattern.compile("^(?:(?!(?:aliases|includes)(?::|$)).)*$");
    private static final String ALIAS_REPLACER = "(?:\\[|^|\\s|,|@|!)(%s)(?:,|\\s|]|$)";
    private static final Pattern HYPER_SELECTOR_PATTERN = Pattern.compile("(^\\w+@\\w+(?::\\w+@\\w+)*):\\w+[^@]\\w+(?::|$)");

    private final Config config;

    public Processor(Config config) {
        this.config = config;
    }

    /**
     * Process data including aliases and calling appropriate hooks.
     *
     * @param data Map configuration to be processed.
     * @return Map of properties.
     * @throws MdcException thrown in case something went wrong.
     */
    public Map<String, Property> process(Map<String, Map<String, String>> data) throws MdcException {
        data = processHyperSelectors(data);
        Map<String, List<Alias>> aliases = getAliases(data);
        Map<String, Property> properties = new HashMap<>();
        
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            if (PROPERTY.matcher(entry.getKey()).matches()) {
                Property property = processProperty(entry, aliases);
                String propertyName = extractPropertyName(entry.getKey());
                properties.put(propertyName, property);
            }
        }
        return properties;
    }
    
    private Property processProperty(Map.Entry<String, Map<String, String>> entry, Map<String, List<Alias>> aliases) throws MdcException {
        String propertyName = extractPropertyName(entry.getKey());
        List<Hook> appropriateHooks = findApplicableHooks(propertyName);
        Map<String, String> processedSelectors = processAliases(entry.getValue(), aliases);
        Map<String, String> enabledSelectors = filterByPrefix(processedSelectors, ENABLED_PREFIX);
        
        Property enabledProperty = createEnabledProperty(propertyName, enabledSelectors);
        return new PropertyProcessor(propertyName, appropriateHooks)
                .getProperty(processedSelectors, enabledProperty);
    }
    
    private String extractPropertyName(String key) {
        return SUB_PROPERTY_PATTERN.matcher(key).replaceAll(SUB_PROPERTY_SEPARATOR);
    }
    
    private List<Hook> findApplicableHooks(String propertyName) {
        return config.getLoadHooks().stream()
                .filter(hook -> hook.getPattern().matcher(propertyName).matches())
                .collect(Collectors.toList());
    }
    
    private Property createEnabledProperty(String propertyName, Map<String, String> enabledSelectors) throws MdcException {
        return enabledSelectors.isEmpty() 
                ? null 
                : new PropertyProcessor(propertyName, null).getProperty(enabledSelectors, null);
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

    /** Process selectors to change root:dim@sel:property -> root:property:dim@sel */
    private Map<String, Map<String, String>> processHyperSelectors(Map<String, Map<String, String>> data) throws MdcException {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> propertyEntry : data.entrySet()) {
            for (Map.Entry<String, String> selectorEntry : propertyEntry.getValue().entrySet()) {
                String selectors = selectorEntry.getKey();
                Matcher matcher = HYPER_SELECTOR_PATTERN.matcher(selectors);
                if(matcher.find()){
                    String tail = selectors.substring(matcher.group(1).length() + 1);
                    if(tail.endsWith(":any@")){
                        tail = tail.substring(0, tail.length()-5);
                    }
                    selectors = tail + DIMENSION_SEPARATOR + matcher.group(1);
                    Pair<String, String> propertyMap = SourceUtils.splitProperty(selectors);
                    putSafe(result,propertyEntry.getKey() + DIMENSION_SEPARATOR + propertyMap.getKey(),
                            propertyMap.getValue(), selectorEntry.getValue());
                } else {
                    putSafe(result, propertyEntry.getKey(),
                            selectorEntry.getKey(), selectorEntry.getValue());
                }
            }
        }
        return result;
    }

    /** Put value to sub map creating it if absent */
    private void putSafe(Map<String, Map<String, String>> map, String propertyName, String key, String value) {
        map.computeIfAbsent(propertyName, k -> new LinkedHashMap<>()).put(key, value);
    }

    /**
     * Extract selectors that starts with prefix into result map and remove them from original map
     * @param selectors map to process
     * @param prefix string that will be used to compare selectors
     * @return new filtered map
     */
    private Map<String, String> filterByPrefix(Map<String, String> selectors, String prefix) {
        Map<String, String> result = selectors.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(prefix.length()),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        // Remove entries that were filtered
        result.keySet().forEach(key -> selectors.remove(prefix + key));
        return result;
    }

    /**
     *  Replace aliases with their substitutes.
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
                       .flatMap(this::createAliases)
                       .collect(Collectors.groupingBy(Alias::getTargetDimension));
            }
        }
        return Map.of();
    }

    /** Create positive and negative aliases helpers */
    private Stream<Alias> createAliases(Map.Entry<String, String> entry) {
        return Stream.of(
                createAlias(entry, false),
                createAlias(entry, true));
    }

    /** Create alias helper */
    private Alias createAlias(Map.Entry<String, String> entry, boolean negative) {
        String fromSelector = config.isSelectorSensitive()
                ? entry.getValue()
                : entry.getValue().toLowerCase(Locale.ROOT);
        Pair<String, String> from = SourceUtils.splitSelector(fromSelector);
        Pair<String, String> to = SourceUtils.splitSelector(entry.getKey());
        // if "from" dimension equals "to" dimension then replace only selectors
        if(from.getKey().equals(to.getKey())) {
            return new Alias(from.getKey(), compileAliasMatcher(from.getValue()), to.getValue());
        } else {
            String fromValue = negative
                    ? entry.getValue().replace(SELECTOR_SEPARATOR, SELECTOR_SEPARATOR + NEGATIVE_SELECTOR)
                    : entry.getValue();
            String toValue = negative
                    ? entry.getKey().replace(SELECTOR_SEPARATOR, SELECTOR_SEPARATOR + NEGATIVE_SELECTOR)
                    : entry.getKey();
            String matcherValue = config.isSelectorSensitive() ? fromValue : fromValue.toLowerCase(Locale.ROOT);
            return new Alias(from.getKey(), compileAliasMatcher(matcherValue), toValue);
        }
    }

    private Pattern compileAliasMatcher(String value) {
        return Pattern.compile(String.format(ALIAS_REPLACER, Pattern.quote(value)));
    }
}
