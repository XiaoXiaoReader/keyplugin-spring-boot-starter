package com.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动主键生成，配合mybatis Interceptor使用
 * 默认类型为SNID（对应字段类型为String）。可选类型LUID（对应字段类型为Long），UUID（对应字段类型为String）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

    IdType value() default IdType.SNID;
}
