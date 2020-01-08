package com.plugin.handler;

import java.lang.reflect.Field;

import com.plugin.annotation.IdType;

/**
 * 策略模式：id赋值操作
 */
public interface KeyGenerator {

	/**
	 * 主键赋值操作
	 */
	void process(Field field, Object paramObj) throws Exception;

	/**
	 * 返回主键策略类型
	 *
	 * @return
	 */
	IdType getType();

}
