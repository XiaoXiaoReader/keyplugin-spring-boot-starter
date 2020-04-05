package com.plugin;

import java.io.Serializable;

/**
 * 版本号属性信息
 */
public class FieldInfo implements Serializable {

    private static final long serialVersionUID = 2800274681280509537L;

    /**
     * 属性名
     */
    String fieldName;

    /**
     * 类型
     */
    Class fieldType;

    /**
     * 数据版本号列名
     */
    String column;

    public FieldInfo() {
    }

    public FieldInfo(String fieldName, Class fieldType, String column) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.column = column;
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class fieldType) {
        this.fieldType = fieldType;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldType=" + fieldType +
                ", column='" + column + '\'' +
                '}';
    }
}
