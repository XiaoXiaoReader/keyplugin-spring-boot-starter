package com.plugin;

import com.plugin.annotation.Id;
import com.plugin.handler.KeyGeneratorFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mybatis主键生成策略拦截器，属性有@Id注解时，优先匹配注解，否则匹配全局配置的主键策略【spring.datasource.idType=UUID】，两种都没有时默认SNID处理
 * <br/>
 * 1、主键注解：@Id(IdType.UUID) 和 @Id(IdType.LUID) <br/>
 * 2、配置文件全局配置：spring.datasource.idType=UUID <br/>
 * 3、无主键注解的时候，全局默认主键名为id
 */
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
@SuppressWarnings("all")
public class GenerateKeyInterceptor implements Interceptor {

    private final KeyGeneratorFactory handlerFactory;

    /**
     * key为传入参数类型，value为参数的注解字段，如果没有注解， 则保存一个空的set对象，这个Map避免每次都要重新反射判断
     */
    private Map<Class, List<Field>> filesMap = new ConcurrentHashMap();

    public GenerateKeyInterceptor(KeyGeneratorFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    /**
     * 全局主键策略
     */
    private String idType;

    /**
     * 拦截目标对象的方法执行
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object paramObj = invocation.getArgs()[1]; // 获取参数
        if (SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType())) { // 获取 SQL类型
            // 获取实体集合
            Set<Object> entitySet = getParamSet(paramObj);
            // 设置id
            for (Object object : entitySet) {
                process(object, this.idType);
            }
        }
        // 执行目标方法
        return invocation.proceed();
    }

    /**
     * 为目标对象创建代理对象
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        // 利用mybatis工具类创建代理对象
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * 配置信息
     *
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        if (StringUtils.isNoneBlank(properties.getProperty("idType"))) {
            this.idType = properties.getProperty("idType");
        }
    }

    /**
     * object是需要新增的实体数据,它可能是对象,也可能是批量插入的对象。 如果是单个对象,那么object就是当前对象
     * 如果是批量插入对象，那么object就是一个map集合,key值为"list",value为ArrayList集合对象
     */
    private Set<Object> getParamSet(Object object) {
        Set<Object> set = new HashSet<>();
        if (object instanceof Map) {
            // 批量插入对象
            Collection values = (Collection) ((Map) object).get("list");
            for (Object value : values) {
                if (value instanceof Collection) {
                    set.addAll((Collection) value);
                } else {
                    set.add(value);
                }
            }
        } else {
            // 单个插入对象
            set.add(object);
        }
        return set;
    }

    /**
     * 反射获取注解字段
     *
     * @param object 参数
     * @param idType 全局配置的策略
     * @throws Throwable
     */
    private void process(Object paramObj, String idType) throws Throwable {
        Class objClass = paramObj.getClass(); // 实体类型
        List<Field> handlerList = filesMap.get(objClass);

        // TODO 性能优化，List参数（批量操作）的时候只遍历一次
        syn:
        if (handlerList == null) {
            synchronized (this) {
                handlerList = filesMap.get(objClass); // 多线程双重检测
                // 如果到这里map集合已经存在，则跳出syn标签
                if (handlerList != null) {
                    break syn;
                }
                handlerList = new ArrayList<Field>();
                // 获取带有Id注解的所有属性字段
                List<Field> fieldList = getAllField(paramObj);
                if (fieldList != null && fieldList.size() > 0) {
                    for (Field field : fieldList) {
                        if (field.isAnnotationPresent(Id.class)) { // 是否包含主键注解
                            handlerList.add(field);
                        }
                    }
                }
                filesMap.put(objClass, handlerList);
            }
        }
        generatorKey(handlerList, paramObj, idType);
    }

    /**
     * 获取所有属性包括父类
     *
     * @param model
     * @return
     */
    private List<Field> getAllField(Object model) {
        Class clazz = model.getClass();
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 主键生成
     *
     * @param paramObj
     * @param idType   全局配置的策略
     * @throws Exception
     * @throws Throwable
     */
    private void generatorKey(List<Field> fieldList, Object paramObj, String idType) throws Exception {
        if (fieldList.size() > 0) {
            for (Field field : fieldList) {
                handlerFactory.doHandler(field.getAnnotation(Id.class).value().getKey(), paramObj, field);
            }
        } else { // 无主键注解时执行
            handlerFactory.doHandler(idType, paramObj, null);
        }
    }

}
