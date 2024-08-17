/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.mdcfg.provider.MdcContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MdcContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String locale = parceLocale(request.getRequestURI());

        MdcContext context = new MdcContext();
        context.put("locale", locale);

        request.setAttribute("mdcContext", context);

        filterChain.doFilter(request, response);
    }

    private String parceLocale(String path){
        String[] parts = path.split("/");
        return switch(parts.length) {
            case 0 -> "";
            case 1, 2 -> parts[0];
            default -> StringUtils.isNotBlank(parts[0]) ? parts[0] : parts[1];
        };
    }
}
