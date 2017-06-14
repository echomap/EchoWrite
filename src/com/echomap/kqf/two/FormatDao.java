package com.echomap.kqf.two;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class FormatDao {
	public static final String DEFAULToutputEncoding = "Cp1252";
	String inputFilename = null;
	String outputFilename = null;
	String storyTitle1 = null;
	String storyTitle2 = null;
	String formatMode = null;
	String outputEncoding = DEFAULToutputEncoding;

	String chapterDivider = null;
	String sectionDivider = null;
	String centerableLineText = null;
	Boolean removeChptDiv = false;
	Boolean removeSectDiv = false;
	Boolean centerStars = false;
	Boolean dropCapChapter = false;
	String writeChapters = null;

	public String prettyPrint() {
		final StringBuilder sbuf = new StringBuilder();
		addLine(sbuf, "InputFilename", this.inputFilename);
		addLine(sbuf, "InputFilename", this.inputFilename);
		addLine(sbuf, "OutputFilename", this.outputFilename);
		addLine(sbuf, "OutputEncoding", this.outputEncoding);
		addLine(sbuf, "StoryTitle1", this.storyTitle1);
		addLine(sbuf, "StoryTitle2", this.storyTitle2);
		addLine(sbuf, "ChapterDivider", this.chapterDivider);
		addLine(sbuf, "SectionDivider", this.sectionDivider);
		addLine(sbuf, "Centerablevalues", this.centerableLineText);
		addLine(sbuf, "CenterStars", this.centerStars.toString());
		addLine(sbuf, "DropCapChapter", this.dropCapChapter.toString());
		addLine(sbuf, "FormatMode:", this.formatMode);
		addLine(sbuf, "WriteChapters:", this.writeChapters);

		sbuf.setLength(sbuf.length() - 1);
		return sbuf.toString();
	}

	private void addLine(final StringBuilder sbuf, final String header, final String value) {
		if (value == null)
			return;
		sbuf.append(header + ": '" + value + "'");
		sbuf.append("\n");
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
		// return super.toString();
	}

	public String getInputFilename() {
		return inputFilename;
	}

	public void setInputFilename(String inputFilename) {
		this.inputFilename = inputFilename;
	}

	public String getOutputFilename() {
		return outputFilename;
	}

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	public String getStoryTitle1() {
		return storyTitle1;
	}

	public void setStoryTitle1(String storyTitle1) {
		this.storyTitle1 = storyTitle1;
	}

	public String getStoryTitle2() {
		return storyTitle2;
	}

	public void setStoryTitle2(String storyTitle2) {
		this.storyTitle2 = storyTitle2;
	}

	public String getChapterDivider() {
		return chapterDivider;
	}

	public void setChapterDivider(String chapterDivider) {
		this.chapterDivider = chapterDivider;
	}

	public String getSectionDivider() {
		return sectionDivider;
	}

	public void setSectionDivider(String sectionDivider) {
		this.sectionDivider = sectionDivider;
	}

	public String getCenterableLineText() {
		return centerableLineText;
	}

	public void setCenterableLineText(String centerableLineText) {
		this.centerableLineText = centerableLineText;
	}

	public Boolean getRemoveChptDiv() {
		return removeChptDiv;
	}

	public void setRemoveChptDiv(Boolean removeChptDiv) {
		this.removeChptDiv = removeChptDiv;
	}

	public Boolean getRemoveSectDiv() {
		return removeSectDiv;
	}

	public void setRemoveSectDiv(Boolean removeSectDiv) {
		this.removeSectDiv = removeSectDiv;
	}

	public Boolean getCenterStars() {
		return centerStars;
	}

	public void setCenterStars(Boolean centerStars) {
		this.centerStars = centerStars;
	}

	public Boolean getDropCapChapter() {
		return dropCapChapter;
	}

	public void setDropCapChapter(Boolean dropCapChapter) {
		this.dropCapChapter = dropCapChapter;
	}

	public String getFormatMode() {
		return formatMode;
	}

	public void setFormatMode(String formatMode) {
		this.formatMode = formatMode;
	}

	public String getWriteChapters() {
		return writeChapters;
	}

	public void setWriteChapters(String writeChapters) {
		this.writeChapters = writeChapters;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

}
