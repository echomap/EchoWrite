package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DocTagLine {

	private List<DocTag> docTags = null;
	// rawLine = The line without the TagName, (sometimes has tags in it!!)
	private String rawLine = null;
	// bareLine= Seems to be used when there is text data on a doctag line?
	private String bareLine = null;
	//bare line but without tags
	private String innerLine = null;
	//
	private long lineCount = 0;
	// Counter
	private long numberOfStartTags = 0;
	// Counter
	private long numberOfEndTags = 0;
	//
	private boolean onlyDoctag = false;
	//
	private boolean hasDocTag = false;
	//
	private boolean longDocTag = false;
	//
	private boolean endDocTag = false;

	// The entire text of the input line
	private String textLine;
	// The text used as the tag's value
	private String textTagLine;

	//
	private String parentTag;
	private String childTag;

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
		// second try at standardizing fields
		this.textLine = dt.getFullTag();
		this.textTagLine = dt.getFullText();
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
		// second try at standardizing fields
		this.textLine = line;
		this.textTagLine = docTagText;
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
		// second try at standardizing fields
		this.addToTextLine(line);
		this.textTagLine = docTagText;
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
			// TODO ? this.textLine = this.textLine + docTag.get
		}
		this.onlyDoctag = false;
		this.hasDocTag = true;

		// second try at standardizing fields
		// this.textLine = docTagsl.getFullTag();
		// this.textTagLine = docTagsl.getFullText();
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
		// lastDt.appendText(lastDt.getFullText() + line2);
		lastDt.appendText(line2);
		this.appendBareLine(line2);

		// second try at standardizing fields
		addToTextLine(line2);
		this.textTagLine = lastDt.getFullText();
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

	public long getNumberOfStartTags() {
		return numberOfStartTags;
	}

	public long getNumberOfEndTags() {
		return numberOfEndTags;
	}

	public String getTextLine() {
		return textLine;
	}

	public void setTextLine(String textLine) {
		this.textLine = textLine;
	}

	public String getTextTagLine() {
		return textTagLine;
	}

	public void setTextTagLine(String textTagLine) {
		this.textTagLine = textTagLine;
	}

	public String getParentTag() {
		return parentTag;
	}

	public void setParentTag(String parentTag) {
		this.parentTag = parentTag;
	}

	public String getChildTag() {
		return childTag;
	}

	public void setChildTag(String childTag) {
		this.childTag = childTag;
	}

	public void addNumberToStartTags(long numberOfStartTags) {
		this.numberOfStartTags += numberOfStartTags;
	}

	public void addNumberToEndTags(long numberOfEndTags) {
		this.numberOfEndTags += numberOfEndTags;
	}

	public void addToTextLine(final String line2) {
		if (this.textLine != null)
			this.textLine = this.textLine + line2;
		else
			this.textLine = line2;
		this.textLine = this.textLine.trim();
	}

	public String getInnerLine() {
		return innerLine;
	}

	public void setInnerLine(String innerLine) {
		this.innerLine = innerLine;
	}

}
