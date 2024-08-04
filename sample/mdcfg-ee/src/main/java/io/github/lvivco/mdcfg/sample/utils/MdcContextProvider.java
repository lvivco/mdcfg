package io.github.lvivco.mdcfg.sample.utils;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mdcfg.provider.MdcContext;

@Stateless
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@Inject))
public class MdcContextProvider {

    HttpServletRequest request;

    public MdcContext getGlobal(){
        return  clone((MdcContext)request.getAttribute("mdcContext"));
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