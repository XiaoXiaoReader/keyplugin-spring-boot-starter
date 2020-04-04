package com.plugin;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 乐观锁
 */
@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter({MybatisAutoConfiguration.class})
@ConditionalOnProperty(prefix = "lockplugin", name = "enabled", havingValue = "true")
public class OptimisticLockerPluginAutoConfiguration {
    private static final Log log = LogFactory.getLog(OptimisticLockerPluginAutoConfiguration.class);

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @PostConstruct
    public void addPageInterceptor() {
        log.debug("Init optimisticLockerPlugin");
        OptimisticLockerInterceptor optimisticLockerInterceptor = new OptimisticLockerInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(optimisticLockerInterceptor);
        }
    }

}
