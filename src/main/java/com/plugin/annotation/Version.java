package com.plugin.annotation;

import java.lang.annotation.*;

/**
 * 乐观锁注解，标记字段属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Version {
    /**
     * 数据库列名
     *
     * @return
     */
    String value() default "version";
}
