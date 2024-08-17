/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.config;

import io.github.lvivco.mdcfg.sample.utils.MdcContextProvider;
import io.github.lvivco.mdcfg.sample.utils.MdcPropertyProvider;
import lombok.AllArgsConstructor;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.provider.MdcProvider;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Optional;

@Configuration
@AllArgsConstructor
public class MdcPropertyConfig {

    private MdcProvider mdc;
    private MdcContextProvider mdcContextProvider;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MdcPropertyProvider<String> getMdcStringPropertyProvider(InjectionPoint ip) {
        return new MdcPropertyProvider<String>(ip, mdcContextProvider) {
            @Override
            protected Optional<String> get(String property, MdcContext ctx) {
                return mdc.getStringOptional(ctx, property);
            }
        };
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MdcPropertyProvider<Integer> getMdcIntegerPropertyProvider(InjectionPoint ip) {
        return new MdcPropertyProvider<>(ip, mdcContextProvider) {
            @Override
            protected Optional<Integer> get(String property, MdcContext ctx) {
                return mdc.getIntegerOptional(ctx, property);
            }
        };
    }
}
