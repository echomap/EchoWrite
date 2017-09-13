package com.echomap.kqf.data;

public class DocTag {
	private String fullText;
	private String name;
	private String value;

	public DocTag(String docTagText) {
		parseText(docTagText);
	}

	public void parseText(String docTagText) {
		this.fullText = docTagText;
		int idx1 = docTagText.indexOf(":");
		if (idx1 > -1) {
			String onet = docTagText.substring(0, idx1);
			String twot = docTagText.substring(idx1 + 1);
			this.setName(onet);
			this.setValue(twot);
		} else {

		}
	}

	public String getName() {
		if (name == null)
			name = "";
		return name;
	}

	public void setName(String name) {
		if (name != null)
			name = name.trim();
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
}
