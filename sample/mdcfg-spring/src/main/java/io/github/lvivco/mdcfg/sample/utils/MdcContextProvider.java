/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.mdcfg.provider.MdcContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MdcContextProvider {
    private final HttpServletRequest request;

    public MdcContext getGlobal(){
        return clone((MdcContext)request.getAttribute("mdcContext"));
    }

    public MdcContext getMerged(MdcContext ctx){
        MdcContext result = getGlobal();
        result.putAll(ctx);
        return result;
    }

    private MdcContext clone(MdcContext ctx){
        MdcContext cloned = new MdcContext();
        cloned.putAll(ctx);
        return cloned;
    }
}
