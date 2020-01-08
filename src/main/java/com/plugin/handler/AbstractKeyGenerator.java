package com.plugin.handler;

import java.lang.reflect.Field;

import com.plugin.annotation.IdType;

public abstract class AbstractKeyGenerator implements KeyGenerator {

    protected static final String STRING_TYPE = "String";

    protected static final String LONG_TYPE = "Long";
    /**
     * 默认主键
     */
    protected static final String DEFAULT_ID = "id";

    @Override
    public abstract void process(Field field, Object paramObj) throws Exception;

    @Override
    public abstract IdType getType();

    /**
     * 默认id主键赋值
     *
     * @return
     */
    protected abstract void defaultGeneratorKey(Object paramObj);

    /**
     * Field值否判断
     *
     * @param field
     * @param object
     * @return
     * @throws IllegalAccessException
     */
    protected boolean checkField(Field field, Object object) throws IllegalAccessException {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(object) == null;
    }

}
