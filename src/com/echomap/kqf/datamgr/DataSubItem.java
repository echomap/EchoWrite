package com.echomap.kqf.datamgr;

import org.apache.commons.lang3.StringUtils;

import com.echomap.kqf.EchoWriteConst;

public class DataSubItem {
	private String name;
	private String value;

	public DataSubItem() {
	}

	public DataSubItem(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this); public String
		// toString() {
		final String msg = String.format("DataSubItem: name=%s, value=%s", name, value);
		return msg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getValueInt() {
		if (StringUtils.isEmpty(value))
			return Integer.valueOf(0);
		String value2 = value;
		if (value != null)
			value2 = value.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
		return Integer.valueOf(value2);
	}

}
