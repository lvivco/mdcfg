package org.mdcfg.provider;

import org.mdcfg.builder.MdcCallback;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.processor.Processor;
import org.mdcfg.source.Source;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MdcProvider {
    private static final UnaryOperator<String> TO_STRING = v -> v;
    private static final Function<String, Boolean> TO_BOOLEAN = Boolean::parseBoolean;
    private static final Function<String, Short> TO_SHORT = Short::parseShort;
    private static final Function<String, Integer> TO_INTEGER = Integer::parseInt;
    private static final Function<String, Long> TO_LONG = Long::parseLong;
    private static final Function<String, Float> TO_FLOAT = Float::parseFloat;
    private static final Function<String, Double> TO_DOUBLE = Double::parseDouble;
    private static final Pattern LIST_SIGN_PATTERN= Pattern.compile("[\\[\\]]");

    private final Processor processor;
    private final Source source;
    private final MdcCallback<Integer, MdcException> callback;

    private Map<String, Property> properties;

    public MdcProvider(Source source, boolean autoReload, long reloadInterval, MdcCallback<Integer, MdcException> callback,
                       List<Hook> loadHooks) throws MdcException {
        this.source = source;
        this.callback = callback;

        this.processor = new Processor(loadHooks);

        readProperties();

        if(autoReload){
            source.observeChange(this::updateProperties, reloadInterval);
        }
    }

    public int getSize() {
        return properties.size();
    }

    public String getString(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_STRING);
    }

    public Optional<String> getStringOptional(MdcContext context, String key){
        return getValueOptional(context, key, TO_STRING);
    }

    public Boolean getBoolean(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_BOOLEAN);
    }

    public Optional<Boolean>getBooleanOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_BOOLEAN);
    }

    public Float getFloat(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_FLOAT);
    }

    public Optional<Float> getFloatOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_FLOAT);
    }

    public Double getDouble(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_DOUBLE);
    }

    public Optional<Double> getDoubleOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_DOUBLE);
    }

    public Short getShort(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_SHORT);
    }

    public Optional<Short> getShortOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_SHORT);
    }

    public Integer getInteger(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_INTEGER);
    }
    public Optional<Integer> getIntegerOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_INTEGER);
    }

    public Long getLong(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_LONG);
    }

    public Optional<Long> getLongOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_LONG);
    }

    public List<String> getStringList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_STRING);
    }

    public Optional<List<String>> getStringListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_STRING);
    }

    public List<Boolean> getBooleanList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_BOOLEAN);
    }
    public Optional<List<Boolean>> getBooleanListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_BOOLEAN);
    }

    public List<Float> getFloatList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_FLOAT);
    }

    public Optional<List<Float>> getFloatListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_FLOAT);
    }

    public List<Double> getDoubleList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_DOUBLE);
    }

    public Optional<List<Double>> getDoubleListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_DOUBLE);
    }

    public List<Short> getShortList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_SHORT);
    }

    public Optional<List<Integer>> getIntegerListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_INTEGER);
    }

    public List<Long> getLongList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_LONG);
    }

    public Optional<List<Long>> getLongListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_LONG);
    }

    public <T> T getValue(MdcContext context, String key, Function<String, T> converter) throws MdcException {
        Property property = Optional.ofNullable(properties.get(key.toLowerCase(Locale.ROOT)))
                .orElseThrow(() -> new MdcException(String.format("Property %s not found.", key)));
        return Optional.ofNullable(property.getString(context))
                .map(converter)
                .orElse(null);
    }

    public <T> Optional<T> getValueOptional(MdcContext context, String key, Function<String, T> converter) {
        try {
            return Optional.ofNullable(getValue(context, key, converter));
        } catch (MdcException e) {
            return Optional.empty();
        }
    }

    private <T> List<T> getValueList(MdcContext context, String key, Function<String, T> converter) throws MdcException {
        Property property = Optional.ofNullable(properties.get(key.toLowerCase(Locale.ROOT)))
                .orElseThrow(() -> new MdcException(String.format("Property %s not found.", key)));
        String listString = Optional.ofNullable(property.getString(context))
                .map((s)->LIST_SIGN_PATTERN.matcher(s).replaceAll(""))
                .orElse(null);

        if(listString == null){
            return null;
        }
        if(StringUtils.isNotEmpty(listString)) {
            return Arrays.stream(listString.split(","))
                    .map(StringUtils::trim)
                    .map(converter)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private <T> Optional<List<T>> getValueListOptional(MdcContext context, String key, Function<String, T> converter) {
        try {
            return Optional.ofNullable(getValueList(context, key, converter));
        } catch (MdcException e) {
            return Optional.empty();
        }
    }

    private void updateProperties() {
        try {
            readProperties();
            Optional.ofNullable(callback).ifPresent(c->c.success(properties.size()));
        } catch (MdcException e) {
            Optional.ofNullable(properties).ifPresent(Map::clear);
            properties.clear();
            Optional.ofNullable(callback).ifPresent(c->c.fail(e));
        }
    }

    private void readProperties() throws MdcException {
        Map<String, Map<String, String>> data = source.read();
        properties = processor.process(data);
    }
}
