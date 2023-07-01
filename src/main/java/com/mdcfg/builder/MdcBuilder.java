package com.mdcfg.builder;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.provider.MdcProvider;
import com.mdcfg.source.Source;
import com.mdcfg.source.JsonSource;
import com.mdcfg.source.YamlSource;

import java.util.function.Consumer;

public class MdcBuilder {

    public static class MdcConfigBuilder{
        private final Source source;
        private boolean autoReload;
        private long reloadInterval = 1000L;
        private Consumer<MdcException> onFail;

        public MdcConfigBuilder(Source source) {
            this.source = source;
        }

        public MdcConfigBuilder autoReload(){
            autoReload = true;
            return this;
        }

        public MdcConfigBuilder autoReload(Consumer<MdcException> onFail){
            this.autoReload = true;
            this.onFail = onFail;
            return this;
        }

        public MdcConfigBuilder autoReload(Consumer<MdcException> onFail, long reloadInterval){
            this.autoReload = true;
            this.reloadInterval = reloadInterval;
            this.onFail = onFail;
            return this;
        }

        public MdcProvider build() throws MdcException {
            return new MdcProvider(source, autoReload, reloadInterval, onFail);
        }
    }

    private MdcBuilder() {}

    public static MdcConfigBuilder withYaml(String path){
        return new MdcConfigBuilder(new YamlSource(path));
    }

    public static MdcConfigBuilder withJson(String path){
        return new MdcConfigBuilder(new JsonSource(path));
    }
}
