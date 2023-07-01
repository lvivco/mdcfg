package com.mdcfg.source;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.utils.SourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class YamlSource extends FileSource {

    public YamlSource(String path) {
        super(path);
    }

    @Override
    Map<String, Map<String, String>> readFile(File source) throws MdcException {
        try (InputStream is = new FileInputStream(source)) {
            Map<String, Object> rawData =  new Yaml().load(is);
            Map<String, Object> flattened = SourceUtils.flatten(rawData);
            return SourceUtils.collectProperties(flattened);
        } catch (IOException e) {
            throw new MdcException(String.format("Couldn't read source %s.", source.getAbsolutePath()), e);
        }
    }

    @Override
    File[] extractFiles(File folder) {
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yaml"));
    }
}
