/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.rest;

import io.github.lvivco.mdcfg.sample.utils.MdcProperty;
import io.github.lvivco.mdcfg.sample.utils.MdcPropertyProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Path("/")
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SimpleRestService {

    @MdcProperty("simple-service.name")
    MdcPropertyProvider<String> name;

    @MdcProperty(property = "simple-service.rating", fallBack = "3")
    MdcPropertyProvider<Integer> rating;

    @GET
    @Produces("text/plain")
    @Path("/{locale}/info")
    public String hello() {
        return "App: " + name.get() + ",  rate: " + rating.get();
    }
}