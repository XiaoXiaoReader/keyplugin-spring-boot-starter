package com.plugin.annotation;

public enum IdType {

	/**
	 * 雪花算法-Long主键
	 */

	LNID("LNID"),

	/**
	 * 雪花算法-String主键
	 */
	SNID("SNID"),

	/**
	 * UUID-String主键
	 */
	UUID("UUID");

	private final String key;

	private IdType(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}
