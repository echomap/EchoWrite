package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DocTagLine {

	private List<DocTag> docTags = null;
	// rawLine = The full tag, start and end
	private String rawLine = null;
	// bareLine= The line without this tag
	private String bareLine = null;
	private long lineCount = 0;
	private boolean onlyDoctag = false;
	private boolean hasDocTag = false;
	private boolean longDocTag = false;
	private boolean endDocTag = false;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
		// return super.toString();
	}

	public String getLine() {
		return rawLine;
	}

	public void setLine(String line) {
		this.rawLine = line;
	}

	public boolean isHasDocTag() {
		return hasDocTag;
	}

	public void setHasDocTag(boolean hasDocTag) {
		this.hasDocTag = hasDocTag;
	}

	public boolean isEndDocTag() {
		return endDocTag;
	}

	public void setEndDocTag(boolean endDocTag) {
		this.endDocTag = endDocTag;
	}

	public boolean isLongDocTag() {
		return longDocTag;
	}

	public void setLongDocTag(boolean longDocTag) {
		this.longDocTag = longDocTag;
	}

	public boolean isOnlyDoctag() {
		return onlyDoctag;
	}

	public void setOnlyDoctag(boolean onlyDoctag) {
		this.onlyDoctag = onlyDoctag;
	}

	public List<DocTag> getDocTags() {
		return docTags;
	}

	public void setDocTags(final List<DocTag> docTags) {
		this.docTags = docTags;
	}

	public void setupNotADocTag(final String docTagText) {
		this.setDocTags(null);
		this.rawLine = docTagText;
		this.bareLine = docTagText;
		this.onlyDoctag = false;
		this.hasDocTag = false;
		this.longDocTag = false;
	}

	public void setupOnlyDocTag(final DocTag dt) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		this.getDocTags().add(dt);
		this.rawLine = dt.getFullText();
		this.bareLine = dt.getBareLine();
		this.onlyDoctag = true;
		this.hasDocTag = true;
		this.longDocTag = false;
	}

	// public void setupOnlyDocTag(final String docTagText) {
	// if (getDocTags() == null)
	// setDocTags(new ArrayList<DocTag>());
	// this.getDocTags().add(new DocTag(docTagText));
	// this.rawLine = docTagText;
	// this.bareLine = null;
	// this.onlyDoctag = true;
	// this.hasDocTag = true;
	// this.longDocTag = false;
	// }

	public void setupContainsDocTag(final String line, final String docTagText) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		// final DocTag dt = new DocTag(docTagText);
		// this.getDocTags().add(dt);

		this.rawLine = line;
		this.bareLine = docTagText;
		this.onlyDoctag = false;
		this.hasDocTag = true;
		this.longDocTag = false;
		// return dt;
	}

	public void setupLongDocTag(String line, String docTagText) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		final DocTag dt = new DocTag(docTagText);
		this.getDocTags().add(dt);

		this.rawLine = line;
		this.bareLine = docTagText;
		this.onlyDoctag = false;
		this.hasDocTag = true;
		this.longDocTag = true;
	}

	public void addDocTag(final DocTag docTag) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		this.getDocTags().add(docTag);
	}

	public void addDocTag(final List<DocTag> docTagsl) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		if (docTagsl == null)
			return;
		for (DocTag docTag : docTagsl) {
			this.getDocTags().add(docTag);
		}
		this.onlyDoctag = false;
		this.hasDocTag = true;
	}

	public void appendTextToLast(String line2) {
		DocTag lastDt = null;
		if (getDocTags() == null) {
			lastDt = new DocTag(line2);
			List<DocTag> dtList = new ArrayList<DocTag>();
			dtList.add(lastDt);
			setDocTags(dtList);
		} else {
			lastDt = getDocTags().get(getDocTags().size() - 1);
		}
		lastDt.parseText(lastDt.getFullText() + line2);
		this.appendBareLine(line2);
	}

	private void appendBareLine(String line2) {
		if (this.bareLine != null)
			this.bareLine = this.bareLine + line2;
		else
			this.bareLine = line2;
		this.bareLine = this.bareLine.trim();
	}

	public void setLineNumber(long lineCount) {
		this.lineCount = lineCount;
	}

	public long getLineCount() {
		return lineCount;
	}

	public String getBareLine() {
		return bareLine;
	}

	public void setBareLine(String bareLine) {
		this.bareLine = bareLine;
	}

	public String getRawLine() {
		return rawLine;
	}

	public void setRawLine(String rawLine) {
		this.rawLine = rawLine;
	}

}
