package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;

public class DocTagLine {

	private List<DocTag> docTags = null;
	private String line = null;
	private boolean onlyDoctag = false;
	private boolean hasDocTag = false;
	private boolean longDocTag = false;
	private boolean endDocTag = false;

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
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

	public void setDocTags(List<DocTag> docTags) {
		this.docTags = docTags;
	}

	public void setupNotADocTag(String line2) {
		this.setDocTags(null);
		this.line = null;
		this.onlyDoctag = false;
		this.hasDocTag = false;
		this.longDocTag = false;
	}

	public void setupOnlyDocTag(String docTagText) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		this.getDocTags().add(new DocTag(docTagText));
		this.line = docTagText;
		this.onlyDoctag = true;
		this.hasDocTag = true;
		this.longDocTag = false;
	}

	public void setupContainsDocTag(String line, String docTagText) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		// final DocTag dt = new DocTag(docTagText);
		// this.getDocTags().add(dt);

		this.line = line;
		this.onlyDoctag = false;
		this.hasDocTag = true;
		this.longDocTag = false;
		// return dt;
	}

	public void setupLongDocTag(String line, String docTagText) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		this.getDocTags().add(new DocTag(docTagText));

		this.line = line;
		this.onlyDoctag = false;
		this.hasDocTag = true;
		this.longDocTag = true;
	}

	public void addDocTag(final DocTag docTag) {
		if (getDocTags() == null)
			setDocTags(new ArrayList<DocTag>());
		this.getDocTags().add(docTag);
	}

}
