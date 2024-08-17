/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.config;

import lombok.AllArgsConstructor;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.ApplicationScope;

@Configuration
@AllArgsConstructor
public class MdcConfig {

    @Bean
    @ApplicationScope
    public MdcProvider getMdConfig() throws MdcException {
        return MdcBuilder.withYaml(getClass().getClassLoader().getResource("config/config.yaml").getPath()).build();
    }
}
