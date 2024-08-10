package io.github.lvivco.mdcfg.sample.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;

import java.io.*;
import java.nio.file.Files;

@ApplicationScoped
public class MdcFactory {

    @Singleton
    @Produces
    public MdcProvider getMdConfig() throws MdcException, IOException {

        final File tempFile = File.createTempFile("conf", "tmp.yaml");
        tempFile.deleteOnExit();
        try ( InputStream in = getClass().getClassLoader().getResourceAsStream("config/config.yaml")) {
            if(in != null) {
                Files.write(tempFile.toPath(), in.readAllBytes());
            }
        }
        return MdcBuilder.withYaml(tempFile.getPath()).build();
    }
}