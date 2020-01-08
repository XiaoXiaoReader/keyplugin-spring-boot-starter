package com.plugin.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KeyGeneratorFactory implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final Map<String, KeyGenerator> idHandlerMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        Map<String, KeyGenerator> idHandlers = applicationContext.getBeansOfType(KeyGenerator.class);
        if (idHandlers == null) {
            throw new IllegalArgumentException("idHandlers 不能为空 ");
        }
        idHandlers.forEach((k, v) -> idHandlerMap.put(v.getType().getKey(), v));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 对应类型逻辑处理
     *
     * @param type     类型
     * @param paramObj 参数对象
     * @param field    主键列属性
     * @throws Exception
     */
    public void doHandler(String type, Object paramObj, Field field) throws Exception {
        KeyGenerator keyGenerator = idHandlerMap.get(type);
        Optional.ofNullable(keyGenerator).orElseThrow(() -> new IllegalArgumentException(String.format("不存在%s类型主键生成策略", type)));
        keyGenerator.process(field, paramObj);
    }
}
