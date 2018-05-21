package com.echomap.kqf.data;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DocTagDataOption {
	String name;
	boolean showCompress = false;
	boolean showExpand = true;
	String prefix = "";

	public DocTagDataOption(final String docTag) {
		this.name = docTag;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String docTag) {
		this.name = docTag;
	}

	public void setDocTag(String docTag) {
		this.name = docTag;
	}

	public boolean isShowCompress() {
		return showCompress;
	}

	public void setShowCompress(boolean showCompress) {
		this.showCompress = showCompress;
	}

	public boolean isShowExpand() {
		return showExpand;
	}

	public void setShowExpand(boolean showExpand) {
		this.showExpand = showExpand;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
