package io.github.lvivco.mdcfg.sample.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.mdcfg.provider.MdcContext;

@Provider
public class MdcContextFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String locale = requestContext.getUriInfo().getPathParameters().getFirst("locale");

        MdcContext context = new MdcContext();
        context.put("locale", locale);

        httpServletRequest.setAttribute("mdcContext", context);
    }
}
