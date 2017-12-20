package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DocTag {
	private String fullText;
	private String name;
	private String value;

	private List<DocTag> sublist = null;

	public DocTag(String docTagText) {
		parseText(docTagText);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
		// return super.toString();
	}

	public void parseText(String docTagText) {
		this.fullText = docTagText;
		int idx1 = docTagText.indexOf(":");
		if (idx1 > -1) {
			String onet = docTagText.substring(0, idx1);
			String twot = docTagText.substring(idx1 + 1);
			this.setName(onet);
			this.setValue(twot);

			// TODO REGEX Collect # from twot: "[+in#]"
			// TODO REGEX Collect # from twot: "[+out#]"

		} else {

		}
	}

	public String getName() {
		if (name == null)
			name = "";
		return name;
	}

	public void setName(String name) {
		if (name != null) {
			name = name.trim();
			name = name.toLowerCase();
		}
		this.name = name;
	}

	public String getValue() {
		if (value == null)
			value = "";
		return value;
	}

	public void setValue(String value) {
		if (value != null)
			value = value.trim();
		this.value = value;
	}

	public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public List<DocTag> getSublist() {
		return sublist;
	}

	public void setSublist(List<DocTag> sublist) {
		this.sublist = sublist;
	}

	public void addSubDocTag(final DocTag docTag) {
		if (sublist == null)
			sublist = new ArrayList<DocTag>();
		this.sublist.add(docTag);
	}
}
