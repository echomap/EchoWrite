package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DocTag {
	private String fullText;
	private String name;
	private String value;
	private String tagInfo;// TODO

	// The line without this tag
	private String bareLine = null;
	// The full tag, start and end
	private String fullTag = null;
	private String baseAdded = null;
	private List<String> addedLines = new ArrayList<>();

	private List<DocTag> sublist = null;

	public DocTag(String docTagText) {
		parseText(docTagText);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
		// return super.toString();
	}

	public void appendText(final String newDataString) {
		// parseText(newDataString);
		this.fullText = getFullText() + newDataString;

		if (StringUtils.isEmpty(this.name)) {
			int idx1 = this.fullText.indexOf(":");
			if (idx1 > -1) {
				String onet = this.fullText.substring(0, idx1);
				String twot = this.fullText.substring(idx1 + 1);
				this.setName(onet);
				this.setValue(twot);
				appendFullText(twot);
				// TODO REGEX Collect # from twot: "[+in#]"
				// TODO REGEX Collect # from twot: "[+out#]"
				return;
			}
		}
		// Don't bother to get a new name for this tag if we already have one!
		this.setValue(this.fullText);
		addToAddedLines(newDataString);
		// addedLines.add(newDataString);
		// if (addedLines.size() > 1)
		// System.out.println("asdf");
	}

	public void parseText(final String docTagText) {
		this.fullText = docTagText;

		if (StringUtils.isEmpty(this.name)) {
			int idx1 = docTagText.indexOf(":");
			if (idx1 > -1) {
				String onet = docTagText.substring(0, idx1);
				String twot = docTagText.substring(idx1 + 1);
				this.setName(onet);
				this.setValue(twot);
				appendFullText(twot);
				// TODO REGEX Collect # from twot: "[+in#]"
				// TODO REGEX Collect # from twot: "[+out#]"
				return;
			}
		}
		// Don't bother to get a new name for this tag if we already have one!
		this.setValue(docTagText);
		addToAddedLines(docTagText);
	}

	private void addToAddedLines(final String docTagText) {
		if (docTagText == null || docTagText == "")
			return;
		if (baseAdded == null && getFullTag() != null) {
			baseAdded = this.getValue();
		}
		addedLines.add(docTagText.trim());
		// if (addedLines.size() > 1)
		// System.out.println("asdf");
	}

	private void appendFullText(String twot) {
		if (this.fullTag != null)
			this.fullText = this.fullText + twot;
		else
			this.fullText = twot;
		this.fullText = this.fullText.trim();
		//
		if (baseAdded == null && this.getValue() != null) {
			baseAdded = this.getValue();
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

	public String getBareLine() {
		return bareLine;
	}

	public void setBareLine(String bareLine) {
		this.bareLine = bareLine;
	}

	public String getFullTag() {
		return fullTag;
	}

	public void setFullTag(String fullTag) {
		this.fullTag = fullTag;
	}

	public List<String> getAddedLines() {
		return addedLines;
	}

	public String getBaseAdded() {
		return baseAdded;
	}

	public String getTagInfo() {
		return tagInfo;
	}

	public void setTagInfo(String tagInfo) {
		this.tagInfo = tagInfo;
	}
	

}
