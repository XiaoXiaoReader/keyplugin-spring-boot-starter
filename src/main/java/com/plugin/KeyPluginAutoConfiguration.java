package com.plugin;

import com.plugin.handler.KeyGeneratorFactory;
import com.plugin.handler.LongSnowKeyGenerator;
import com.plugin.handler.StringSnowKeyGenerator;
import com.plugin.handler.StringUUIDKeyGenerator;
import com.plugin.properties.KeyPluginProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

/**
 * 自定义主键赋值插件
 */
@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@EnableConfigurationProperties(KeyPluginProperties.class)
@AutoConfigureAfter({MybatisAutoConfiguration.class})
@ConditionalOnProperty(prefix = "keyplugin", name = "enabled", havingValue = "true")
public class KeyPluginAutoConfiguration {
    private static final Log log = LogFactory.getLog(KeyPluginAutoConfiguration.class);

    private static final String ID_TYPE = "idType";

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired
    private KeyGeneratorFactory keyGeneratorFactory;

    private final KeyPluginProperties keyPluginProperties;

    public KeyPluginAutoConfiguration(KeyPluginProperties keyPluginProperties) {
        this.keyPluginProperties = keyPluginProperties;
    }

    @PostConstruct
    public void addPageInterceptor() {
        log.debug("Init keyPlugin");
        log.debug(keyPluginProperties.toString());
        GenerateKeyInterceptor generateKeyPlugin = new GenerateKeyInterceptor(keyGeneratorFactory);
        Properties properties = new Properties();
        // TODO 全局主键（id）策略
        properties.put(ID_TYPE, keyPluginProperties.getKeyType());
        generateKeyPlugin.setProperties(properties);
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(generateKeyPlugin);
        }
    }

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
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