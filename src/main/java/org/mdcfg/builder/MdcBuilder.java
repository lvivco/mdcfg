/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.builder;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.provider.MdcProvider;
import org.mdcfg.source.HoconSource;
import org.mdcfg.source.JsonSource;
import org.mdcfg.source.Source;
import org.mdcfg.source.YamlSource;
import org.mdcfg.model.Config;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 *  Builder for MDC config, can be initialized for Yaml or Json.
 */
public class MdcBuilder {

    public static class MdcConfigBuilder{
        private final Source source;
        private boolean autoReload;
        private long reloadInterval = 1000L;
        private MdcCallback<Integer, MdcException> callback;
        private final List<Hook> loadHooks = new ArrayList<>();
        private boolean keyCaseSensitive = false;
        private boolean selectorCaseSensitive = false;

        public MdcConfigBuilder(Source source) {
            this.source = source;
        }

        /**
         *
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder caseSensitive() {
            this.keyCaseSensitive = true;
            this.selectorCaseSensitive = true;
            return this;
        }

        /**
         * Make property keys case sensitive.
         */
        public MdcConfigBuilder caseSensitiveKeys() {
            this.keyCaseSensitive = true;
            return this;
        }

        /**
         * Make selector values case sensitive.
         */
        public MdcConfigBuilder caseSensitiveSelectors() {
            this.selectorCaseSensitive = true;
            return this;
        }

        /**
         * Set up auto reload if source changed with default interval 1 sec.
         *
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder autoReload(){
            autoReload = true;
            return this;
        }

        /**
         * Set up auto reload if source changed with default interval 1 sec.
         *
         * @param callback Call back on reload with {@code onSuccess} and {@code onFailure} handlers. See {@link MdcCallback}.
         *                 {@code onSuccess} will take count of properties as a parameter.
         *                 {@code onFailure} will take exception as a parameter.
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder autoReload(MdcCallback<Integer, MdcException> callback){
            this.autoReload = true;
            this.callback = callback;
            return this;
        }

        /**
         * Set up auto reload if source changed with default interval 1 sec.
         *
         * @param reloadInterval
         * @param callback Call back on reload with {@code onSuccess} and {@code onFailure} handlers. See {@link MdcCallback}.
         *                 {@code onSuccess} will take count of properties as a parameter.
         *                 {@code onFailure} will take exception as a parameter.
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder autoReload(long reloadInterval, MdcCallback<Integer, MdcException> callback){
            this.autoReload = true;
            this.reloadInterval = reloadInterval;
            this.callback = callback;
            return this;
        }

        /**
         * Sometimes when you load config you need to preprocess values, for example encode.
         * Function will be called for each config value in each config property.
         *
         * @param loadHook function that takes config value and returns processed value.
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder loadHook(UnaryOperator<String> loadHook) {
            Hook hook = new Hook(Pattern.compile(".*"), loadHook);
            loadHooks.add(hook);
            return this;
        }

        /**
         * Sometimes when you load config you need to preprocess values, for example encode.
         *
         * @param property config property for which values hook will be called.
         * @param loadHook function that takes config value and returns processed value.
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder loadHook(String property, UnaryOperator<String> loadHook) {
            Hook hook = new Hook(Pattern.compile(property), loadHook);
            loadHooks.add(hook);
            return this;
        }

        /**
         * Sometimes when you load config you need to preprocess values, for example encode.
         *
         * @param pattern RegExp pattern that matches properties for which values hook will be called.
         * @param loadHook function that takes config value and returns processed value.
         * @return current instance of {@link MdcConfigBuilder}
         */
        public MdcConfigBuilder loadHook(Pattern pattern, UnaryOperator<String> loadHook) {
            Hook hook = new Hook(pattern, loadHook);
            loadHooks.add(hook);
            return this;
        }

        /**
         * Build config.
         *
         * @return new instance of configured {@link MdcProvider}.
         * @throws MdcException thrown in case something went wrong.
         */
        public MdcProvider build() throws MdcException {
            Config config = new Config(
                    autoReload,
                    reloadInterval,
                    callback,
                    loadHooks,
                    keyCaseSensitive,
                    selectorCaseSensitive);
            return new MdcProvider(source, config);
        }
    }

    private MdcBuilder() {}

    /**
     * Start building Yaml base config.
     *
     * @param path absolute path for config Yaml file or folder that contains Yaml files (sub folders will be ignored)
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withYaml(String path){
        return new MdcConfigBuilder(new YamlSource(path));
    }

    /**
     * Start building Yaml base config.
     *
     * @param file config Yaml file or folder that contains Yaml files (sub folders will be ignored)
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withYaml(File file){
        return new MdcConfigBuilder(new YamlSource(file));
    }

    /**
     * Start building Yaml base config.
     *
     * @param stream Input stream for config Yaml data
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withYaml(InputStream stream){
        return new MdcConfigBuilder(new YamlSource(stream));
    }

    /**
     * Start building Json base config.
     *
     * @param path absolute path for config Json file or folder that contains Json files (sub folders will be ignored)
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withJson(String path){
        return new MdcConfigBuilder(new JsonSource(path));
    }

    /**
     * Start building Json base config.
     *
     * @param file config Json file or folder that contains Json files (sub folders will be ignored)
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withJson(File file){
        return new MdcConfigBuilder(new JsonSource(file));
    }

    /**
     * Start building Json base config.
     *
     * @param stream Input stream for config Json data
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withJson(InputStream stream){
        return new MdcConfigBuilder(new JsonSource(stream));
    }

    /**
     * Start building HOCON base config.
     *
     * @param path absolute path for config HOCON file or folder that contains HOCON files (sub folders will be ignored)
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withHocon(String path) {
        return new MdcConfigBuilder(new HoconSource(path));
    }

    /**
     * Start building HOCON base config.
     *
     * @param file config HOCON file or folder that contains Json files (sub folders will be ignored)
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withHocon(File file){
        return new MdcConfigBuilder(new HoconSource(file));
    }

    /**
     * Start building HOCON base config.
     *
     * @param stream Input stream for config HOCON data
     * @return new instance of {@link MdcConfigBuilder} for further configuration.
     */
    public static MdcConfigBuilder withHocon(InputStream stream){
        return new MdcConfigBuilder(new HoconSource(stream));
    }
}
