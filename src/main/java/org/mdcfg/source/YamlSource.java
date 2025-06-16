/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Source implementation for Yaml based config files */
public class YamlSource extends FileSource {

    public YamlSource(InputStream stream) {
        super(stream);
    }

    public YamlSource(File file) {
        super(file);
    }

    public YamlSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> read(InputStream is, boolean caseSensitive) throws MdcException {
        try (is) {
            Map<String, Object> rawData = new Yaml().load(is);
            rawData = Optional.ofNullable(rawData).orElse(new HashMap<>());
            Map<String, Object> flattened = SourceUtils.flatten(rawData, caseSensitive);
            return SourceUtils.collectProperties(flattened);
        } catch (Exception e) {
            throw new MdcException("Couldn't read input source", e);
        }
    }

    @Override
    File[] listFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yaml"));
    }
}
