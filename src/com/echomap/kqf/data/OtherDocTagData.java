package com.echomap.kqf.data;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class OtherDocTagData {
	private String name;
	private String file;
	private String docTags;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public List<String> getDocTagsList() {
		return Arrays.asList(docTags.split("\\s*,\\s*"));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getDocTags() {
		return docTags;
	}

	public void setDocTags(String docTags) {
		this.docTags = docTags;
	}

}
