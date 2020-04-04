package com.plugin.utils;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;

public class SqlUtil {

    /**
     * 版本号+1
     *
     * @param clazz
     * @param originalVersionVal
     * @return
     */
    public static Object getUpdatedVersionVal(Class<?> clazz, Object originalVersionVal) {
        if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return ((long) originalVersionVal) + 1;
        } else if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            return ((int) originalVersionVal) + 1;
        } else if (Date.class.equals(clazz)) {
            return new Date();
        } else if (Timestamp.class.equals(clazz)) {
            return new Timestamp(System.currentTimeMillis());
        } else if (LocalDateTime.class.equals(clazz)) {
            return LocalDateTime.now();
        }
        //不支持类型，返回原始值
        return originalVersionVal;
    }

    /**
     * 获取所有属性包括父类
     *
     * @param model
     * @return
     */
    public static List<Field> getAllField(Object model) {
        Class clazz = model.getClass();
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 格式化SQL
     *
     * @param configuration
     * @param boundSql
     * @param statementId
     * @return
     */
    public static String getSql(Configuration configuration, BoundSql boundSql, String statementId) {
        String sql = showSql(configuration, boundSql);
        StringBuilder str = new StringBuilder(100);
        str.append(statementId);
        str.append(" : ");
        str.append(sql);
        return str.toString();
    }

    /**
     * 进行？的替换
     *
     * @param configuration
     * @param boundSql
     * @return
     */
    public static String showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object paramObj = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 多个空格替换成一个空格
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (!CollectionUtils.isEmpty(parameterMappings) && paramObj != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换　　　　　　　
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(paramObj.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(paramObj)));
            } else {
                MetaObject metaObject = configuration.newMetaObject(paramObj);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 该分支是动态sql
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));

                    } else {
                        //打印出缺失，提醒该参数缺失并防止错位
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    /**
     * 如果参数是String，则添加单引号， 如果是日期，则转换为时间格式器并加单引号；
     * 对参数是null和不是null的情况作了处理<br>
     *
     * @param obj
     * @return
     */
    public static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }
}
