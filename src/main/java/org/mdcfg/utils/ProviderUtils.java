package org.mdcfg.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ProviderUtils {

    private ProviderUtils() {}

    public static List toList(Object object){
        List<?> list = null;
        if (object.getClass().isArray()) {
            list = Arrays.asList((Object[]) object);
        } else if (object instanceof Collection) {
            list = new ArrayList<>((Collection<?>) object);
        }
        return list;
    }
}
