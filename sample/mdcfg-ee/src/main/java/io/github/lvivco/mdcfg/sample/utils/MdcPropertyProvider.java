package io.github.lvivco.mdcfg.sample.utils;

import jakarta.enterprise.inject.spi.InjectionPoint;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.mdcfg.provider.MdcContext;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

@Data
public abstract class MdcPropertyProvider<T>{

    private String property;
    private T fallBack;
    private MdcContextProvider contextProvider;

    public MdcPropertyProvider(InjectionPoint ip, MdcContextProvider contextProvider) {
        MdcProperty annotation = getProperty(ip);
        this.contextProvider = contextProvider;
        this.property = StringUtils.isNotBlank(annotation.value()) ? annotation.value() : annotation.property();
        if(StringUtils.isNotBlank(annotation.fallBack())) {
            this.fallBack = convert(reflectClassType(), annotation.fallBack());
        }
    }

    public T get() {
        return get(property, contextProvider.getGlobal()).orElse(fallBack);
    };

    public T get(MdcContext ctx) {
        return get(property, contextProvider.getMerged(ctx)).orElse(fallBack);
    };

    protected abstract Optional<T> get(String property, MdcContext ctx);

    public T isolated(MdcContext ctx) {
        return get(property, ctx).orElse(fallBack);
    };

    @SuppressWarnings("unchecked")
    private Class<T> reflectClassType() {
        return ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    private T convert(Class<T> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return (T)editor.getValue();
    }

    private MdcProperty getProperty(InjectionPoint ip) {
        return Optional.ofNullable(ip.getAnnotated().getAnnotation(MdcProperty.class))
                .orElseGet(() -> ((Method)ip.getMember()).getAnnotation(MdcProperty.class));
    }
}