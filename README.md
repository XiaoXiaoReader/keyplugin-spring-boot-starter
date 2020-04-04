# keyplugin-spring-boot-starter
mybatis主键生成策略，支持分布式。支持UUID（UUID-String主键）、SNID（默认雪花算法-String主键）、LNID（雪花算法-Long主键）。支持注解@Id指定主键，注解优先级大于全局配置。

## 使用方式
直接引入即可使用，或者自己下载install引入使用

```
<dependency>
    <groupId>com.github.xiaoxiaoreader</groupId>
    <artifactId>keyplugin-spring-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

yml配置，必须配置enabled: true，否则默认false不起作用
```yml
keyplugin:
  enabled: true #开启插件
  keyType: SNID #主键策略
  workerId: 2 #工作ID，默认1可以不配置
  datacenterId: 2 #数据中心ID，默认1可以不配置
```

实体示例
```java
public class User {

    @Id(IdType.SNID)
    private String id;

    private String userName;

    private String password;
}
```

# mybatis乐观锁插件
mybatis拦截器乐观锁插件，配合注解实现CAS乐观锁，自动加上版本号。这个部分的代码没有提交到中央仓库，需要copy到本地install引入使用。

**使用条件：**
- 版本号属性必须加注解@Version，支持Date Long Integer属性
- lockplugin.enabled=true 配置文件必须配置

yml配置，必须配置enabled: true，否则默认false不起作用
```yml
lockplugin:
  enabled: true #开启插件
```
使用示例
注解指定数据库版本号列名，默认version
```java
public class User {
    
    private String id;

    private String userName;

    private String password;
    @Version("version")
    private Integer version;
}
```

```sql
# 原始sql
update user SET name='小可爱', version=1 where id=520  
# 插件处理后sql
update user SET name='小可爱', version=2 where id=520 and version = 1 

```


