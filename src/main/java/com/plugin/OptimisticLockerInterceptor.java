package com.plugin;

import com.plugin.annotation.Version;
import com.plugin.utils.SqlUtil;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 乐观锁插件
 */
@Intercepts({@Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),})
public class OptimisticLockerInterceptor implements Interceptor {
    private static final Log log = LogFactory.getLog(OptimisticLockerInterceptor.class);

    /**
     * 缓存@Version字段，优化性能
     */
    private Map<Class, FieldInfo> filesMap = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Exception {
        if (invocation.getTarget() instanceof StatementHandler) {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject routingMetaObject = SystemMetaObject.forObject(statementHandler);
            MetaObject preMetaObject = routingMetaObject.metaObjectForProperty("delegate");
            MappedStatement mappedStatement = (MappedStatement) preMetaObject.getValue("mappedStatement");
            if (!SqlCommandType.UPDATE.equals(mappedStatement.getSqlCommandType())) {
                return invocation.proceed();
            }
            BoundSql boundSql = (BoundSql) preMetaObject.getValue("boundSql");
            Object paramObj = boundSql.getParameterObject();
            boolean isOptimistic = getParam(paramObj);
            if (!isOptimistic) {
                return invocation.proceed();
            }
            // 获取对应对象@Version的属性
            FieldInfo fieldInfo = filesMap.get(paramObj.getClass());
            String originalSql = boundSql.getSql();
            StringBuilder builderSql = new StringBuilder(originalSql);
            builderSql.append(" and ").append(fieldInfo.getColumn()).append(" = ?");
            //通过反射修改sql语句
            preMetaObject.setValue("boundSql.sql", builderSql.toString());
        }
        if (invocation.getTarget() instanceof ParameterHandler) {
            ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
            MetaObject parameterHandlerMetaObject = SystemMetaObject.forObject(parameterHandler);
            Object paramObj = parameterHandlerMetaObject.getValue("parameterObject");
            // 由于StatementHandler优先于ParameterHandler执行，直接判断缓存中有没有值即可，没有直接跳过
            FieldInfo fieldInfo = filesMap.get(paramObj.getClass());
            if (fieldInfo == null || fieldInfo.getFieldName() == null) {
                return invocation.proceed();
            }

            BoundSql boundSql = (BoundSql) parameterHandlerMetaObject.getValue("boundSql");
            Configuration configuration = (Configuration) parameterHandlerMetaObject.getValue("configuration");
            MetaObject paramObjMetaObject = SystemMetaObject.forObject(paramObj);
            //版本号原始值
            Object originalVersionVal = paramObjMetaObject.getValue(fieldInfo.getFieldName());
            if (originalVersionVal == null) {
                //这里根据实际情况处理
                throw new NullPointerException(String.format("%s版本号属性%s不能为空", paramObj.getClass().getName(), fieldInfo.getFieldName()));
                //return invocation.proceed();
            }
            //创建新的参数映射
            ParameterMapping versionMapping = new ParameterMapping.Builder(configuration, fieldInfo.getFieldName(), Object.class).build();
            //版本号类型
            TypeHandler typeHandler = versionMapping.getTypeHandler();
            JdbcType jdbcType = versionMapping.getJdbcType();
            if (originalVersionVal == null && jdbcType == null) {
                jdbcType = configuration.getJdbcTypeForNull();
            }
            //第几个参数
            int versionLocation = boundSql.getParameterMappings().size() + 1;
            try {
                PreparedStatement ps = (PreparedStatement) invocation.getArgs()[0];
                //重点: 调用参数对应的typeHandler的setParameter方法为该ps设置额外参数值
                typeHandler.setParameter(ps, versionLocation, originalVersionVal, jdbcType);
            } catch (TypeException | SQLException e) {
                throw new TypeException("版本号参数添加失败", e);
            }
            //版本号+1
            Object newVersionVal = SqlUtil.getUpdatedVersionVal(fieldInfo.getFieldType(), originalVersionVal);
            paramObjMetaObject.setValue(fieldInfo.getFieldName(), newVersionVal);
            //输出SQL
            String id = ((MappedStatement) parameterHandlerMetaObject.getValue("mappedStatement")).getId();
            String sql = SqlUtil.getSql(configuration, boundSql, id).replace("?", originalVersionVal.toString());
            log.debug("乐观锁sql ==> " + sql);
            /*这里的invocation.proceed()返回null，不能作为sql执行成功或失败的标志。
            要想知道乐观锁是否执行成功或者失败可以拦截Executor对象的update方法判断*/
            return invocation.proceed();
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler || target instanceof ParameterHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }


    /**
     * 是否支持乐观锁
     * 判断标准：1、不是批量update  2、必须有@Version注解
     *
     * @param object
     * @return
     */
    private boolean getParam(Object object) {
        if (object instanceof Map) {
            // 不支持批量update
            return false;
        }
        Class objectClass = object.getClass(); // 实体类型
        FieldInfo fieldInfo = filesMap.get(objectClass);
        if (fieldInfo != null) {
            return fieldInfo.getFieldName() != null;
        }
        List<Field> fieldList = SqlUtil.getAllField(object);
        if (fieldList.size() > 0) {
            for (Field field : fieldList) {
                if (field.isAnnotationPresent(Version.class)) { // 是否包含注解
                    String column = field.getAnnotation(Version.class).value();
                    fieldInfo = new FieldInfo(field.getName(), field.getType(), column);
                    filesMap.put(objectClass, fieldInfo);
                    return true;
                }
            }
        }
        filesMap.put(objectClass, new FieldInfo());
        return false;
    }

   /* private boolean getParam(Object object) {
        if (object instanceof Map) {
            // 不支持批量update
            return false;
        }
        Class objectClass = object.getClass();
        FieldInfo fieldInfo = filesMap.get(objectClass);
        if (fieldInfo != null) {
            return fieldInfo.getFieldName() != null;
        }
        synchronized (this) {
            fieldInfo = filesMap.get(objectClass);
            if (fieldInfo != null) {
                return fieldInfo.getFieldName() != null;
            }
            List<Field> fieldList = SqlUtil.getAllField(object);
            if (fieldList.size() > 0) {
                for (Field field : fieldList) {
                    //是否包含注解
                    if (field.isAnnotationPresent(Version.class)) {
                        String column = field.getAnnotation(Version.class).value();
                        fieldInfo = new FieldInfo(field.getName(), field.getType(), column);
                        filesMap.put(objectClass, fieldInfo);
                        return true;
                    }
                }
            }
        }
        filesMap.put(objectClass, new FieldInfo());
        return false;
    }*/

}
