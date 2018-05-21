package com.echomap.kqf.data;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DataItemMFC {
	String tag;
	boolean expand;
	boolean compress;
	String prefix;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isExpand() {
		return expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
