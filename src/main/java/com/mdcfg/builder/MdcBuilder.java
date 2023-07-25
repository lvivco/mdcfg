package com.mdcfg.builder;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.model.Hook;
import com.mdcfg.provider.MdcProvider;
import com.mdcfg.source.JsonSource;
import com.mdcfg.source.Source;
import com.mdcfg.source.YamlSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class MdcBuilder {

    public static class MdcConfigBuilder{
        private final Source source;
        private boolean autoReload;
        private long reloadInterval = 1000L;
        private Consumer<MdcException> onFail;
        private final List<Hook> loadHooks = new ArrayList<>();

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

        public MdcConfigBuilder loadHook(UnaryOperator<String> loadHook) {
            Hook hook = new Hook(Pattern.compile(".*"), loadHook);
            loadHooks.add(hook);
            return this;
        }

        public MdcConfigBuilder loadHook(String property, UnaryOperator<String> loadHook) {
            Hook hook = new Hook(Pattern.compile(property), loadHook);
            loadHooks.add(hook);
            return this;
        }

        public MdcConfigBuilder loadHook(Pattern pattern, UnaryOperator<String> loadHook) {
            Hook hook = new Hook(pattern, loadHook);
            loadHooks.add(hook);
            return this;
        }

        public MdcProvider build() throws MdcException {
            return new MdcProvider(source, autoReload, reloadInterval, onFail, loadHooks);
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
