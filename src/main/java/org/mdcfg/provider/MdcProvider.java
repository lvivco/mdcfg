package org.mdcfg.provider;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.processor.Processor;
import org.mdcfg.source.Source;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;
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

    private final MdcOptional optional;
    private final Processor processor;

    private final Source source;
    private final Consumer<MdcException> onFail;

    private Map<String, Property> properties;

    public MdcProvider(Source source, boolean autoReload, long reloadInterval, Consumer<MdcException> onFail,
                       List<Hook> loadHooks) throws MdcException {
        this.source = source;
        this.onFail = onFail;

        this.processor = new Processor(loadHooks);
        this.optional = new MdcOptional(this);

        properties = processor.process(source);

        if(autoReload){
            source.observeChange(this::updateProperties, reloadInterval);
        }
    }

    public int getSize() {
        return properties.size();
    }

    public MdcOptional getOptional() {
        return optional;
    }

    public String getString(MdcContext context, String key){
        return getValue(context, key, TO_STRING);
    }

    public Boolean getBoolean(MdcContext context, String key){
        return getValue(context, key, TO_BOOLEAN);
    }

    public Float getFloat(MdcContext context, String key){
        return getValue(context, key, TO_FLOAT);
    }

    public Double getDouble(MdcContext context, String key){
        return getValue(context, key, TO_DOUBLE);
    }

    public Short getShort(MdcContext context, String key){
        return getValue(context, key, TO_SHORT);
    }

    public Integer getInteger(MdcContext context, String key){
        return getValue(context, key, TO_INTEGER);
    }

    public Long getLong(MdcContext context, String key){
        return getValue(context, key, TO_LONG);
    }

    public <T> T getValue(MdcContext context, String key, Function<String, T> converter){
        Property property = properties.get(key.toLowerCase(Locale.ROOT));
        if(property != null){
            return converter.apply(property.getString(context));
        }
        return null;
    }

    public List<String> getStringList(MdcContext context, String key){
        return getValueList(context, key, TO_STRING);
    }

    public List<Boolean> getBooleanList(MdcContext context, String key){
        return getValueList(context, key, TO_BOOLEAN);
    }

    public List<Float> getFloatList(MdcContext context, String key){
        return getValueList(context, key, TO_FLOAT);
    }

    public List<Double> getDoubleList(MdcContext context, String key){
        return getValueList(context, key, TO_DOUBLE);
    }

    public List<Short> getShortList(MdcContext context, String key){
        return getValueList(context, key, TO_SHORT);
    }

    public List<Integer> getIntegerList(MdcContext context, String key){
        return getValueList(context, key, TO_INTEGER);
    }

    public List<Long> getLongList(MdcContext context, String key){
        return getValueList(context, key, TO_LONG);
    }

    public <T> List<T> getValueList(MdcContext context, String key, Function<String, T> converter){
        Property property = properties.get(key.toLowerCase(Locale.ROOT));
        if(property != null){
            String listString = Optional.ofNullable(property.getString(context))
                    .map((s)->LIST_SIGN_PATTERN.matcher(s).replaceAll(""))
                    .orElse(null);
            if(StringUtils.isNotBlank(listString)) {
                return Arrays.stream(listString.split(","))
                        .map(StringUtils::trim)
                        .map(converter)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private void updateProperties() {
        try {
            properties = processor.process(source);
        } catch (MdcException e) {
            properties.clear();
            if(onFail != null) {
                onFail.accept(e);
            }
        }
    }
}
