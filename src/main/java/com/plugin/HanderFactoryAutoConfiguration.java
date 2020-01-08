package com.plugin;

import com.plugin.handler.KeyGeneratorFactory;
import com.plugin.handler.LongSnowKeyGenerator;
import com.plugin.handler.StringSnowKeyGenerator;
import com.plugin.handler.StringUUIDKeyGenerator;
import com.plugin.properties.KeyPluginProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HanderFactoryAutoConfiguration {

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker(KeyPluginProperties keyPluginProperties) {
        return new SnowflakeIdWorker(keyPluginProperties.getWorkerId(), keyPluginProperties.getDatacenterId());
    }

    @Bean
    public KeyGeneratorFactory keyGeneratorFactory() {
        return new KeyGeneratorFactory();
    }

    @Bean
    public LongSnowKeyGenerator longSnowKeyGenerator() {
        return new LongSnowKeyGenerator();
    }

    @Bean
    public StringSnowKeyGenerator stringSnowKeyGenerator() {
        return new StringSnowKeyGenerator();
    }

    @Bean
    public StringUUIDKeyGenerator stringUUIDKeyGenerator() {
        return new StringUUIDKeyGenerator();
    }
}
