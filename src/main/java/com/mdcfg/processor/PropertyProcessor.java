package com.mdcfg.processor;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.*;
import com.mdcfg.utils.SourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropertyProcessor {

    private static final String DIMENSION_SEPARATOR = ":";
    private static final String LIST_SIGN = "*";
    private static final String RANGE_SIGN = "..";
    private static final String ANY = "any";
    private static final Pattern LIST_SIGN_PATTERN= Pattern.compile("\\s|\\[|]");
    private static final Pattern NUMERIC_SPLITERATOR_PATTERN= Pattern.compile(",|\\.\\.");
    private static final Pattern COMMA_PATTERN= Pattern.compile(",");


    private final String name;
    private final Map<String, Dimension> dimensions = new HashMap<>();
    private final List<Chain> chains = new ArrayList<>();
    private final List<Hook> loadHooks;

    public PropertyProcessor(String name, List<Hook> loadHooks) {
        this.name = name;
        this.loadHooks = loadHooks;
    }

    public Property getProperty(Map<String, String> map) throws MdcException {
        createDimensions(map);
        createSelectorChains(map);

        return new Property(name, dimensions, chains);
    }

    private void createDimensions(Map<String, String> map) throws MdcException {
        ListIterator<Map.Entry<String, String>> reverseIter = reverseIterator(map);
        while(reverseIter.hasPrevious()){
            Map.Entry<String, String> entry = reverseIter.previous();
            addDimensions(entry.getKey());
        }
    }

    private void addDimensions(String key) throws MdcException {
        for (String selector : key.split(DIMENSION_SEPARATOR)) {
            if(!selector.contains(ANY)) {
                addDimension(selector);
            }
        }
    }

    private void addDimension(String selector) throws MdcException {
        Pair<String, String> pair = SourceUtils.splitSelector(selector);
        String key = pair.getKey();
        if(StringUtils.isBlank(pair.getValue())){
            throw new MdcException(String.format("Invalid nesting for %s", key));
        }

        String value = LIST_SIGN_PATTERN.matcher(pair.getValue()).replaceAll("") ;
        boolean isList = key.contains(LIST_SIGN);
        boolean isRange = value.contains(RANGE_SIGN);
        boolean isNumeric = Arrays.stream(NUMERIC_SPLITERATOR_PATTERN.split(value))
                .filter(StringUtils::isNotBlank)
                .allMatch(NumberUtils::isCreatable);
        if(isList){
            key = key.replace(LIST_SIGN, "");
        }

        if(dimensions.containsKey(key)){
            Dimension current = dimensions.get(key);
            if(current.isList() != isList){
                throw new MdcException(String.format("Mixed list type for dimension name %s", key));
            }

            if((current.isRange() && !isNumeric) || (isRange && !current.isNumeric())){
                throw new MdcException(String.format("Mixed number type for dimension name %s", key));
            }

            // update dimension to be numeric range
            if(!current.isRange() && isRange){
                dimensions.put(key, new Dimension(key, true, isList, true));
            }
        } else {
            dimensions.put(key, new Dimension(key, isRange, isList, isNumeric));
        }
    }

    private void createSelectorChains(Map<String, String> map){
        for(var entry : map.entrySet()) {
            Map<String, String> chain = new HashMap<>();
            for (var item:entry.getKey().split(DIMENSION_SEPARATOR)) {
                Pair<String, String> pair = SourceUtils.splitSelector(item);
                if(!pair.getKey().equals(ANY)) {
                    chain.put(pair.getKey(), pair.getValue());
                }
            }
            chains.add(0, createChain(chain, entry.getValue()));
        }
    }

    private Chain createChain(Map<String, String> selectors, String value) {
        StringBuilder pattern = new StringBuilder();
        List<Range> ranges = new ArrayList<>();
        for ( Dimension dimension : dimensions.values()) {
            if(pattern.length() > 0){
                pattern.append("\\.");
            }
            String dimensionKey = dimension.getName() + (dimension.isList() ? LIST_SIGN : "");
            if(selectors.containsKey(dimensionKey)){
                String selector = selectors.get(dimensionKey);
                applySelector(selector, pattern, ranges, dimension);
            } else {
                // any
                pattern.append(dimension.getName()).append("@.*");
            }
        }
        // end
        pattern.append("$");

        if (null != loadHooks) {
            for (var hook : loadHooks) {
                value = hook.getFunction().apply(value);
            }
        }

        return new Chain(pattern.toString(), value, ranges);
    }

    private void applySelector(String selector, StringBuilder pattern, List<Range> ranges, Dimension dimension) {
        if(selector.contains(RANGE_SIGN)){
            // numeric ranges
            selector = LIST_SIGN_PATTERN.matcher(selector).replaceAll("");
            int index = selector.indexOf(RANGE_SIGN);
            String min = null;
            String max = null;
            if(index > 0){
                min = selector.substring(0, index);
            }
            if(index < selector.length() - RANGE_SIGN.length()){
                max = selector.substring(index + RANGE_SIGN.length());
            }
            ranges.add(new Range(dimension, min, max));
            selector = "(\\d|\\.)*";

        } else if(selector.contains("[")) {
            // selector lists
            selector = LIST_SIGN_PATTERN.matcher(selector).replaceAll("");
            selector = Arrays.stream(COMMA_PATTERN.split(selector))
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|", "(", ")"));
        } else {
            selector = Pattern.quote(selector);
        }

        // dimension list
        if(dimension.isList()){
            selector = "\\[(.*,)*" + selector + "(,.*)*\\]";
        }

        pattern.append(dimension.getName()).append("@").append(selector);
    }

    private ListIterator<Map.Entry<String, String>> reverseIterator(Map<String, String> map){
        return new ArrayList<>(map.entrySet()).listIterator(map.size());
    }

}