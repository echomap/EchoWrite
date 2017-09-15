package com.echomap.kqf.data;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.echomap.kqf.looper.FileLooper;

/**
 * 
 */
public class FormatDao {
	public static final String DEFAULToutputEncoding = "Cp1252";
	private String inputFilename = null;

	private String outputFilename = null;
	private String outputCountFile = null;
	private String outputOutlineFile = null;
	private String outputOutlineFile1 = null;

	private String storyTitle1 = null;
	private String storyTitle2 = null;
	private String formatMode = null;
	private String outputEncoding = DEFAULToutputEncoding;

	private String chapterDivider = null;
	private String sectionDivider = null;
	private String centerableLineText = null;
	private Boolean removeChptDiv = false;
	private Boolean removeSectDiv = false;
	private Boolean centerStars = false;
	private Boolean dropCapChapter = false;
	private String writeChapters = null;

	private String docTagStart = null;
	private String docTagEnd = null;

	private String chapterHeaderTag = "h1";
	private String sectionHeaderTag = "h1";

	private Integer countOutputDigits = null;

	public String prettyPrint() {
		final StringBuilder sbuf = new StringBuilder();
		addLine(sbuf, "InputFilename", this.inputFilename);

		addLine(sbuf, "OutputFilename", this.outputFilename);
		addLine(sbuf, "OutputCountFile", this.outputCountFile);
		addLine(sbuf, "OutputOutlineFile", this.outputOutlineFile);
		addLine(sbuf, "OutputOutlineFile1", this.outputOutlineFile1);

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
		addLine(sbuf, "ChapterHeaderTag:", this.getChapterHeaderTag());
		addLine(sbuf, "SectionHeaderTag:", this.getSectionHeaderTag());

		addLine(sbuf, "DocTagStart:", this.getDocTagStart());
		addLine(sbuf, "DocTagEnd:", this.getDocTagEnd());

		addLine(sbuf, "CountOutputDigits:",
				(getCountOutputDigits() == null ? "" : this.getCountOutputDigits().toString()));

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

	public String getOutputCountFile() {
		return outputCountFile;
	}

	public void setOutputCountFile(String outputCountFile) {
		this.outputCountFile = outputCountFile;
	}

	public String getOutputOutlineFile() {
		return outputOutlineFile;
	}

	public void setOutputOutlineFile(String outputOutlineFile) {
		this.outputOutlineFile = outputOutlineFile;
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
		if (outputEncoding == null)
			outputEncoding = FileLooper.DEFAULToutputEncoding;
		return outputEncoding;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public String getChapterHeaderTag() {
		return chapterHeaderTag;
	}

	public void setChapterHeaderTag(String chapterHeaderTag) {
		this.chapterHeaderTag = chapterHeaderTag;
	}

	public String getSectionHeaderTag() {
		return sectionHeaderTag;
	}

	public void setSectionHeaderTag(String sectionHeaderTag) {
		this.sectionHeaderTag = sectionHeaderTag;
	}

	public String getDocTagStart() {
		if (docTagStart == null)
			docTagStart = "1234567890-!@#$%^&*()";// Impossible text, because im
													// lazy
		return docTagStart;
	}

	public void setDocTagStart(String docTagStart) {
		this.docTagStart = docTagStart;
	}

	public String getDocTagEnd() {
		return docTagEnd;
	}

	public void setDocTagEnd(String docTagEnd) {
		this.docTagEnd = docTagEnd;
	}

	public Integer getCountOutputDigits() {
		if (countOutputDigits == null)
			countOutputDigits = 1;
		return countOutputDigits;
	}

	public void setCountOutputDigits(Integer countOutputDigits) {
		this.countOutputDigits = countOutputDigits;
	}

	public String getOutputOutlineFile1() {
		return outputOutlineFile1;
	}

	public void setOutputOutlineFile1(String outputOutlineFile1) {
		this.outputOutlineFile1 = outputOutlineFile1;
	}

}
