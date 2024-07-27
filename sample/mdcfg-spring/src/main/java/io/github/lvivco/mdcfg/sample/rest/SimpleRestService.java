package io.github.lvivco.mdcfg.sample.rest;

import io.github.lvivco.mdcfg.sample.utils.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

@RestController
@SessionScope
@AllArgsConstructor
public class SimpleRestService {

    @MdcProperty("simple-service.name")
    public MdcPropertyProvider<String> name;

    @MdcProperty(property = "simple-service.rating", fallBack = "3")
    public MdcPropertyProvider<Integer> rating;

    @GetMapping("/{locale}/info")
    public String info() {
        return "App: " + name.get() + ",  rate:" + rating.get();
    }
}
