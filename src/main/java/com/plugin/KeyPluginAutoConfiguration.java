package com.plugin;

import com.plugin.handler.KeyGeneratorFactory;
import com.plugin.properties.KeyPluginProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
@Import({HanderFactoryAutoConfiguration.class})
public class KeyPluginAutoConfiguration {

    private static final String ID_TYPE = "idType";

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    private final KeyPluginProperties keyPluginProperties;

    private final KeyGeneratorFactory keyGeneratorFactory;

    public KeyPluginAutoConfiguration(KeyPluginProperties keyPluginProperties, KeyGeneratorFactory keyGeneratorFactory) {
        this.keyPluginProperties = keyPluginProperties;
        this.keyGeneratorFactory = keyGeneratorFactory;
    }

    @PostConstruct
    public void addPageInterceptor() {
        GenerateKeyInterceptor generateKeyPlugin = new GenerateKeyInterceptor(keyGeneratorFactory);
        Properties properties = new Properties();
        // TODO 全局主键（id）策略
        properties.put(ID_TYPE, keyPluginProperties.getKeyType());
        generateKeyPlugin.setProperties(properties);
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(generateKeyPlugin);
        }
    }
}
