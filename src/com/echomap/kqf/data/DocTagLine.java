package com.echomap.kqf.data;

public class DocTagLine {

	private DocTag docTag = null;
	private String line = null;
	private boolean onlyDoctag = false;
	private boolean hasDocTag = false;
	private boolean longDocTag = false;

	public DocTag getDocTag() {
		return docTag;
	}

	public void setDocTag(DocTag docTag) {
		this.docTag = docTag;
	}

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

	public void setupNotADocTag(String line2) {
		this.docTag = null;
		this.line = null;
		this.onlyDoctag = false;
		this.hasDocTag = false;
		this.longDocTag = false;
	}

	public void setupOnlyDocTag(String docTagText) {
		this.docTag = new DocTag(docTagText);
		this.line = docTagText;
		this.onlyDoctag = true;
		this.hasDocTag = true;
		this.longDocTag = false;
	}

	public void setupContainsDocTag(String line, String docTagText) {
		this.docTag = new DocTag(docTagText);
		this.line = line;
		this.onlyDoctag = false;
		this.hasDocTag = true;
		this.longDocTag = false;
	}

	public void setupLongDocTag(String line, String docTagText) {
		this.docTag = new DocTag(docTagText);
		this.line = line;
		this.onlyDoctag = false;
		this.hasDocTag = true;
		this.longDocTag = true;
	}

}
