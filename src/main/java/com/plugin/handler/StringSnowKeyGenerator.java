package com.plugin.handler;

import com.plugin.annotation.IdType;
import com.plugin.SnowflakeIdWorker;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * String类型主键雪花赋值
 */
public class StringSnowKeyGenerator extends AbstractKeyGenerator {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public void process(Field field, Object paramObj) throws Exception {
        if (null == field) {
            defaultGeneratorKey(paramObj);
            return;
        }
        if (!field.getType().isAssignableFrom(String.class)) {
            throw new IllegalArgumentException("主键策略SNID==》对应主键属性类型必须为String");
        }
        if (super.checkField(field, paramObj)) {
            field.set(paramObj, String.valueOf(snowflakeIdWorker.nextId()));
        }
    }

    @Override
    public IdType getType() {
        return IdType.SNID;
    }

    /**
     * 默认id主键赋值
     *
     * @return
     */
    @Override
    protected void defaultGeneratorKey(Object paramObj) {
        MetaObject metaObject = SystemMetaObject.forObject(paramObj);
        if (metaObject.getValue(DEFAULT_ID) == null) {
            if (!STRING_TYPE.equals(metaObject.getSetterType(DEFAULT_ID).getSimpleName())) {
                throw new IllegalArgumentException("主键策略SNID==》对应主键属性类型必须为String");
            }
            metaObject.setValue(DEFAULT_ID, String.valueOf(snowflakeIdWorker.nextId()));
        }
    }
}