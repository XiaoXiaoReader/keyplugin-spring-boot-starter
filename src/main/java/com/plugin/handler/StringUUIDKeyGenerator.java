package com.plugin.handler;

import com.plugin.annotation.IdType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * String类型主键赋值
 */
public class StringUUIDKeyGenerator extends AbstractKeyGenerator {

    @Override
    public void process(Field field, Object paramObj) throws Exception {
        if (null == field) {
            defaultGeneratorKey(paramObj);
            return;
        }
        if (!field.getType().isAssignableFrom(String.class)) {
            throw new IllegalArgumentException("主键策略UUID==》对应主键属性类型必须为String");
        }
        if (super.checkField(field, paramObj)) {
            field.set(paramObj, getUUID());
        }
    }

    @Override
    public IdType getType() {
        return IdType.UUID;
    }

    @Override
    protected void defaultGeneratorKey(Object paramObj) {
        MetaObject metaObject = SystemMetaObject.forObject(paramObj);
        if (metaObject.getValue(DEFAULT_ID) == null) {
            if (!STRING_TYPE.equals(metaObject.getSetterType(DEFAULT_ID).getSimpleName())) {
                throw new IllegalArgumentException("主键策略UUID==》对应主键属性类型必须为String");
            }
            metaObject.setValue(DEFAULT_ID, getUUID());
        }
    }

    private String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
