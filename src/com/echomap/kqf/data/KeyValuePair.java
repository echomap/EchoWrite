package com.echomap.kqf.data;

import org.apache.commons.lang.builder.ToStringBuilder;

public class KeyValuePair {
	private String key;
	private String value;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
