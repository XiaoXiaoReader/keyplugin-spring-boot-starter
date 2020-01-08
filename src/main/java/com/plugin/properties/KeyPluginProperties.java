package com.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(KeyPluginProperties.PREFIX)
public class KeyPluginProperties {

    protected static final String PREFIX = "keyplugin";
    private static final String KEY_TYPE = "SNID";
    private static final String EN_ABLED = "false";
    private static final long WORKER_ID = 1L;
    private static final long DATACENTER_ID = 1L;

    /**
     * 主键策略（LNID、SNID、UUID）
     */
    private String keyType = KEY_TYPE;

    /**
     * 是否开启主键策略插件
     */
    private String enabled = EN_ABLED;

    /**
     * 工作ID
     */
    private Long workerId = WORKER_ID;

    /**
     * 数据中心ID
     */
    private Long datacenterId = DATACENTER_ID;

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(Long datacenterId) {
        this.datacenterId = datacenterId;
    }

    @Override
    public String toString() {
        return "KeyPluginProperties{" +
                "keyType='" + keyType + '\'' +
                ", enabled='" + enabled + '\'' +
                ", workerId=" + workerId +
                ", datacenterId=" + datacenterId +
                '}';
    }
}