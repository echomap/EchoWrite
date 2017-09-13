package com.echomap.kqf.data;

public class LineTracker {

	private boolean inSpecial = false;
	private boolean inLongDocTag = false;
	private String htmlLine = null;
	// private FormatMode formatMode;
	private boolean lastLineWasChapter = false;
	private Integer thisLineCharacterCount = 0;

	public void clear() {
		setInSpecial(false);
		setInLongDocTag(false);
		setHtmlLine(null);

	}

	public void preReadLine() {
		// TODO Auto-generated method stub

	}

	public void postReadLine() {
		// TODO Auto-generated method stub
	}

	public void inReadLine() {
		// TODO Auto-generated method stub
	}

	public String getHtmlLine() {
		return htmlLine;
	}

	public void setHtmlLine(String htmlLine) {
		this.htmlLine = htmlLine;
	}

	public boolean isInSpecial() {
		return inSpecial;
	}

	public void setInSpecial(boolean inSpecial) {
		this.inSpecial = inSpecial;
	}

	public boolean isInLongDocTag() {
		return inLongDocTag;
	}

	public void setInLongDocTag(boolean inLongDocTag) {
		this.inLongDocTag = inLongDocTag;
	}

	public boolean isLastLineWasChapter() {
		return lastLineWasChapter;
	}

	public void setLastLineWasChapter(boolean lastLineWasChapter) {
		this.lastLineWasChapter = lastLineWasChapter;
	}

	public Integer getThisLineCharacterCount() {
		return thisLineCharacterCount;
	}

	public void setThisLineCharacterCount(Integer thisLineCharacterCount) {
		this.thisLineCharacterCount = thisLineCharacterCount;
	}

	public Integer addThisLineCharacterCount(final Integer newNum) {
		this.thisLineCharacterCount += newNum;
		return this.thisLineCharacterCount;
	}
}
