/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.processor;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mdcfg.model.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Parse data of one property and create {@link Property} object. */
public class PropertyProcessor {
    private static final String DIMENSION_SEPARATOR = ":";
    private static final String LIST_SIGN = "*";
    private static final String RANGE_SIGN = "..";
    private static final String ANY = "any";
    private static final String NEGATIVE_SELECTOR= "!";
    private static final Pattern LIST_SIGN_PATTERN = Pattern.compile("[\\s\\[\\]]");
    private static final Pattern NUMERIC_SPLITERATOR_PATTERN = Pattern.compile("!|,\\s*|\\.\\.");
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\{[^}]+}");
    private static final char UNIT_SEPARATOR = (char) 31;
    private static final String LIST_EXPRESSION = "\\[(.*" + UNIT_SEPARATOR + ")*%s(" + UNIT_SEPARATOR + ".*)*\\]";
    private final String name;
    private final Map<String, Dimension> dimensions = new HashMap<>();
    private final List<Chain> chains = new ArrayList<>();
    private final Map<String, List<Chain>> listChains = new HashMap<>();
    private final List<Hook> loadHooks;
    private boolean hasReference;

    public PropertyProcessor(String name, List<Hook> loadHooks) {
        this.name = name;
        this.loadHooks = loadHooks;
    }

    /** Create {@link Property} object */
    public Property getProperty(Map<String, String> map, Property enabled) throws MdcException {
        createDimensions(map);
        createSelectorChains(map);

        return new Property(name, dimensions, chains, listChains, hasReference, enabled);
    }

    /** Create {@code List} of {@link Dimension} objects with down to up order. */
    private void createDimensions(Map<String, String> map) throws MdcException {
        ListIterator<Map.Entry<String, String>> reverseIter = reverseIterator(map);
        while(reverseIter.hasPrevious()){
            Map.Entry<String, String> entry = reverseIter.previous();
            addDimensions(entry.getKey());
        }
    }

    /** Create and add all {@link Dimension} objects parsing one flattened chain string. */
    private void addDimensions(String key) throws MdcException {
        for (String selector : key.split(DIMENSION_SEPARATOR)) {
            if(!selector.contains(ANY)) {
                addDimension(selector);
            }
        }
    }

    /** Create and add {@link Dimension} object parsing one selector. */
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

    /** Create and add all {@link Chain} objects parsing all flattened chain strings with down to up order. */
    private void createSelectorChains(Map<String, String> map){
        for(var entry : map.entrySet()) {
            Map<String, String> chainMap = new HashMap<>();
            for (var item:entry.getKey().split(DIMENSION_SEPARATOR)) {
                Pair<String, String> pair = SourceUtils.splitSelector(item);
                if(!pair.getKey().equals(ANY)) {
                    chainMap.put(pair.getKey(), pair.getValue());
                }
            }
            createChain(chainMap, entry.getValue());
        }
    }

    /**
     * Create {@link Chain} object parsing one flattened chain string split into map by selector name and value.
     *  <p> For example property:
     *  <pre>
     *    horsepower:
     *      any@: 400
     *      model@bmw:
     *        drive@4WD: 500
     *  </pre>
     *  contains two chains:
     *  <ul>
     *  <li>{@code model@any.drive@any}</li>
     *  <li>{@code model@bmw.drive@4WD}</li>
     *  </ul>
     *  Those chains will be represented in RegExps:
     *  <ul>
     *  <li>{@code model@.*\.drive@.*$}</li>
     *  <li>{@code model@bmw\.drive@4WD$}</li>
     *  </ul>
     */
    private void createChain(Map<String, String> selectors, String value) {
        StringBuilder plusBuilder = new StringBuilder();
        StringBuilder minusBuilder = new StringBuilder();
        boolean negativeUsed = false;
        List<Range> ranges = new ArrayList<>();
        List<Dimension> nonEmptyListDimensions = new ArrayList<>();
        for ( Dimension dimension : dimensions.values()) {
            if(plusBuilder.length() > 0){
                plusBuilder.append("\\.");
            }
            if(minusBuilder.length() > 0){
                minusBuilder.append("\\.");
            }
            String dimensionKey = dimension.getName() + (dimension.isList() ? LIST_SIGN : "");
            if(selectors.containsKey(dimensionKey)) {
                String selector = selectors.get(dimensionKey);
                boolean negative = selector.startsWith(NEGATIVE_SELECTOR);
                if(negative) {
                    negativeUsed = true;
                    // remove "!" from selector
                    selector = selector.substring(NEGATIVE_SELECTOR.length());
                    applySelector(selector, minusBuilder, ranges, dimension, true);
                    plusBuilder.append(dimension.getName()).append("@.*");
                } else {
                    applySelector(selector, plusBuilder, ranges, dimension, false);
                    minusBuilder.append(dimension.getName()).append("@.*");
                }
                if(dimension.isList()) {
                    nonEmptyListDimensions.add(dimension);
                }
            } else {
                // any
                plusBuilder.append(dimension.getName()).append("@.*");
                minusBuilder.append(dimension.getName()).append("@.*");
            }
        }
        // end
        plusBuilder.append("$");
        minusBuilder.append("$");

        if (null != loadHooks) {
            for (var hook : loadHooks) {
                value = hook.getFunction().apply(value);
            }
        }

        if(!hasReference) {
            hasReference = REFERENCE_PATTERN.matcher(value).find();
        }

        Pattern positivePattern = Pattern.compile(plusBuilder.toString());
        Pattern negativePattern = negativeUsed ? Pattern.compile(minusBuilder.toString()) : null;
        Chain chain = new Chain(positivePattern, negativePattern, value, ranges);
        chains.add(0, chain);
        addListableChains(nonEmptyListDimensions, chain);
    }

    /** Append pattern for one selector and add {@link Range} objects if needed. */
    private void applySelector(String selector, StringBuilder pattern, List<Range> ranges, Dimension dimension, boolean negative) {
        if (selector.contains(RANGE_SIGN)) {
            selector = LIST_SIGN_PATTERN.matcher(selector).replaceAll("");
            for (String selectorPart : COMMA_PATTERN.split(selector)) {
                ranges.add(createRange(selectorPart, dimension, negative));
            }
            selector = "-?(?:\\d|,|\\s|\\.)*";
        } else if (LIST_SIGN_PATTERN.matcher(selector).find()) {
            // selector lists
            selector = LIST_SIGN_PATTERN.matcher(selector).replaceAll("");
            selector = Arrays.stream(COMMA_PATTERN.split(selector))
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|", "(", ")"));
        } else {
            selector = Pattern.quote(selector);
        }

        // dimension list
        if (dimension.isList()) {
            selector = String.format(LIST_EXPRESSION, selector);
        }

        pattern.append(dimension.getName()).append("@").append(selector);
    }

    /** Return iterator in revers order */
    private ListIterator<Map.Entry<String, String>> reverseIterator(Map<String, String> map) {
        return new ArrayList<>(map.entrySet()).listIterator(map.size());
    }

    /** Create {@link Range} object from selector string. */
    private Range createRange(String selectorPart, Dimension dimension, boolean negative) {
        int index = selectorPart.indexOf(RANGE_SIGN);
        String min = null;
        String max = null;
        boolean minInclusive = true;
        boolean maxInclusive = true;

        if (index < 0) {
            min = max = selectorPart;
        } else {
            if (index > 0) {
                min = selectorPart.substring(0, index);
                if(min.startsWith("!")){
                    min = min.substring(1);
                    minInclusive = false;
                }
            }
            if (index < selectorPart.length() - RANGE_SIGN.length()) {
                max = selectorPart.substring(index + RANGE_SIGN.length());
                if(max.startsWith("!")){
                    max = max.substring(1);
                    maxInclusive = false;
                }
            }
        }
        return new Range(dimension, minInclusive, min, maxInclusive, max, negative);
    }


    /** Store chain in map where key is list dimension name and value is list of appropriate chains */
    private void addListableChains(List<Dimension> dimensions, Chain chain) {
        for (Dimension isIterableDimension : dimensions) {
            if(!listChains.containsKey(isIterableDimension.getName())) {
                listChains.put(isIterableDimension.getName(), new ArrayList<>());
            }
            listChains.get(isIterableDimension.getName()).add(0, chain);
        }
    }
}
