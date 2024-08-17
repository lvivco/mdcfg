/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.config;

import io.github.lvivco.mdcfg.sample.utils.MdcContextProvider;
import io.github.lvivco.mdcfg.sample.utils.MdcPropertyProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.provider.MdcProvider;

import java.util.Optional;

@ApplicationScoped
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class MdcPropertyFactory {

    private MdcContextProvider mdcContextProvider;
    private MdcProvider mdc;

    @Produces
    public MdcPropertyProvider<String> getMdcStringPropertyProvider(InjectionPoint ip) {
        return new MdcPropertyProvider<String>(ip, mdcContextProvider) {
            @Override
            protected Optional<String> get(String property, MdcContext ctx) {
                return mdc.getStringOptional(ctx, property);
            }
        };
    }

    @Produces
    public MdcPropertyProvider<Integer> getMdcIntegerPropertyProvider(InjectionPoint ip) {
        return new MdcPropertyProvider<>(ip, mdcContextProvider) {
            @Override
            protected Optional<Integer> get(String property, MdcContext ctx) {
                return mdc.getIntegerOptional(ctx, property);
            }
        };
    }
}
