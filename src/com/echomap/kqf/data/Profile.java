package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Profile {

	// Main Data
	private String key = null;
	private String seriesTitle = null;
	private String volume = null;
	private String mainTitle = "Unnamed Story";
	private String subTitle = null;
	private String inputFile = null;
	private String inputFilePrefix = null;
	private boolean appendUnderscoreToPrefix = false;
	private String keywords = "";

	// Formatting/Tagging
	private String docTagStart = null;
	private String docTagEnd = null;
	private String fmtMode = null;
	private String outputEncoding = null;
	private String chpDiv = null;
	private String secDiv = null;
	private String chapterHeaderTag = "[[*";
	private String sectionHeaderTag = "*]]";

	private String outputFormatChpTextDir = null;

	// RegExp
	private String regexpChapter = null;
	private String regexpSection = null;

	// Word Count
	private String outputCountFile = null;

	// Formatting
	private boolean cbCenterStars = false;
	private boolean cbDropCapChapters = false;
	private boolean cbRemoveSectDiv = false;
	private boolean cbRemoveDiv = false;
	private boolean wantTextChptOutput = false;
	private int counterDigitChoice = 0;

	// DocTags
	private int outputDocTagsMaxLineLength = 70;
	private String sceneCoalateDiv = null;
	private String outputDocTagsScenePrefix = null;
	private String outputDocTagsSubScenePrefix = null;

	private String outputCSVOutlineFile = null;
	private String outputCSVAllFile = null;
	private String outputDocTagsOutlineFile = null;
	private String outputDocTagsSceneFile = null;

	private String docTagsOutlineTags = null;
	private String docTagsOutlineCompressTags = null;
	private String docTagsSceneTags = null;
	private String docTagsSceneCompressTags = null;

	// UNKNOWN
	// private String ouputFile = null;// setOutputFormatSingleFile
	// private String outputDir = null;//outputFormatChpHtmlDirText
	private String outputFormatSingleFile = null;
	private String outputFormatChpHtmlDir = null;

	//
	// final List<OtherDocTagData> outputs = loadOutputs(child);
	// pd.setOutputs(outputs);

	// private String series = null;

	private List<OtherDocTagData> outputs = null;

	private List<KeyValuePair> externalIDs = null;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	//
	// public void setOutputs(final List<OtherDocTagData> outputs) {
	// this.outputs = outputs;
	// }
	//
	// public List<OtherDocTagData> getOutputs() {
	// return outputs;
	// }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSeriesTitle() {
		return seriesTitle;
	}

	public void setSeriesTitle(String seriesTitle) {
		this.seriesTitle = seriesTitle;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getMainTitle() {
		return mainTitle;
	}

	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getInputFilePrefix() {
		return inputFilePrefix;
	}

	public void setInputFilePrefix(String inputFilePrefix) {
		this.inputFilePrefix = inputFilePrefix;
	}

	public boolean isAppendUnderscoreToPrefix() {
		return appendUnderscoreToPrefix;
	}

	public void setAppendUnderscoreToPrefix(boolean appendUnderscoreToPrefix) {
		this.appendUnderscoreToPrefix = appendUnderscoreToPrefix;
	}

	public String getDocTagStart() {
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

	public String getFmtMode() {
		return fmtMode;
	}

	public void setFmtMode(String fmtMode) {
		this.fmtMode = fmtMode;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public String getChpDiv() {
		return chpDiv;
	}

	public void setChpDiv(String chpDiv) {
		this.chpDiv = chpDiv;
	}

	public String getSecDiv() {
		return secDiv;
	}

	public void setSecDiv(String secDiv) {
		this.secDiv = secDiv;
	}

	public String getChapterHeaderTag() {
		return chapterHeaderTag;
	}

	public void setChapterHeaderTag(String chapterHeaderTag) {
		this.chapterHeaderTag = chapterHeaderTag;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getSectionHeaderTag() {
		return sectionHeaderTag;
	}

	public void setSectionHeaderTag(String sectionHeaderTag) {
		this.sectionHeaderTag = sectionHeaderTag;
	}

	public String getOutputFormatChpTextDir() {
		return outputFormatChpTextDir;
	}

	public void setOutputFormatChpTextDir(String outputFormatChpTextDir) {
		this.outputFormatChpTextDir = outputFormatChpTextDir;
	}

	public String getRegexpChapter() {
		return regexpChapter;
	}

	public void setRegexpChapter(String regexpChapter) {
		this.regexpChapter = regexpChapter;
	}

	public String getRegexpSection() {
		return regexpSection;
	}

	public void setRegexpSection(String regexpSection) {
		this.regexpSection = regexpSection;
	}

	public String getOutputCountFile() {
		return outputCountFile;
	}

	public void setOutputCountFile(String outputCountFile) {
		this.outputCountFile = outputCountFile;
	}

	public boolean isCbCenterStars() {
		return cbCenterStars;
	}

	public void setCbCenterStars(boolean cbCenterStars) {
		this.cbCenterStars = cbCenterStars;
	}

	public boolean isCbDropCapChapters() {
		return cbDropCapChapters;
	}

	public void setCbDropCapChapters(boolean cbDropCapChapters) {
		this.cbDropCapChapters = cbDropCapChapters;
	}

	public boolean isCbRemoveSectDiv() {
		return cbRemoveSectDiv;
	}

	public void setCbRemoveSectDiv(boolean cbRemoveSectDiv) {
		this.cbRemoveSectDiv = cbRemoveSectDiv;
	}

	public boolean isCbRemoveDiv() {
		return cbRemoveDiv;
	}

	public void setCbRemoveDiv(boolean cbRemoveDiv) {
		this.cbRemoveDiv = cbRemoveDiv;
	}

	public boolean isWantTextChptOutput() {
		return wantTextChptOutput;
	}

	public void setWantTextChptOutput(boolean wantTextChptOutput) {
		this.wantTextChptOutput = wantTextChptOutput;
	}

	public int getCounterDigitChoice() {
		return counterDigitChoice;
	}

	public void setCounterDigitChoice(int counterDigitChoice) {
		this.counterDigitChoice = counterDigitChoice;
	}

	public int getOutputDocTagsMaxLineLength() {
		return outputDocTagsMaxLineLength;
	}

	public void setOutputDocTagsMaxLineLength(int outputDocTagsMaxLineLength) {
		this.outputDocTagsMaxLineLength = outputDocTagsMaxLineLength;
	}

	public String getSceneCoalateDiv() {
		return sceneCoalateDiv;
	}

	public void setSceneCoalateDiv(String sceneCoalateDiv) {
		this.sceneCoalateDiv = sceneCoalateDiv;
	}

	public String getOutputDocTagsScenePrefix() {
		return outputDocTagsScenePrefix;
	}

	public void setOutputDocTagsScenePrefix(String outputDocTagsScenePrefix) {
		this.outputDocTagsScenePrefix = outputDocTagsScenePrefix;
	}

	public String getOutputDocTagsSubScenePrefix() {
		return outputDocTagsSubScenePrefix;
	}

	public void setOutputDocTagsSubScenePrefix(String outputDocTagsSubScenePrefix) {
		this.outputDocTagsSubScenePrefix = outputDocTagsSubScenePrefix;
	}

	public String getOutputCSVOutlineFile() {
		return outputCSVOutlineFile;
	}

	public void setOutputCSVOutlineFile(String outputCSVOutlineFile) {
		this.outputCSVOutlineFile = outputCSVOutlineFile;
	}

	public String getOutputCSVAllFile() {
		return outputCSVAllFile;
	}

	public void setOutputCSVAllFile(String outputCSVAllFile) {
		this.outputCSVAllFile = outputCSVAllFile;
	}

	public String getOutputDocTagsOutlineFile() {
		return outputDocTagsOutlineFile;
	}

	public void setOutputDocTagsOutlineFile(String outputDocTagsOutlineFile) {
		this.outputDocTagsOutlineFile = outputDocTagsOutlineFile;
	}

	public String getOutputDocTagsSceneFile() {
		return outputDocTagsSceneFile;
	}

	public void setOutputDocTagsSceneFile(String outputDocTagsSceneFile) {
		this.outputDocTagsSceneFile = outputDocTagsSceneFile;
	}

	public String getDocTagsOutlineTags() {
		return docTagsOutlineTags;
	}

	public void setDocTagsOutlineTags(String docTagsOutlineTags) {
		this.docTagsOutlineTags = docTagsOutlineTags;
	}

	public String getDocTagsOutlineCompressTags() {
		return docTagsOutlineCompressTags;
	}

	public void setDocTagsOutlineCompressTags(String docTagsOutlineCompressTags) {
		this.docTagsOutlineCompressTags = docTagsOutlineCompressTags;
	}

	public String getDocTagsSceneTags() {
		return docTagsSceneTags;
	}

	public void setDocTagsSceneTags(String docTagsSceneTags) {
		this.docTagsSceneTags = docTagsSceneTags;
	}

	public String getDocTagsSceneCompressTags() {
		return docTagsSceneCompressTags;
	}

	public void setDocTagsSceneCompressTags(String docTagsSceneCompressTags) {
		this.docTagsSceneCompressTags = docTagsSceneCompressTags;
	}

	public String getOutputFormatSingleFile() {
		return outputFormatSingleFile;
	}

	public void setOutputFormatSingleFile(String outputFormatSingleFile) {
		this.outputFormatSingleFile = outputFormatSingleFile;
	}

	public String getOutputFormatChpHtmlDir() {
		return outputFormatChpHtmlDir;
	}

	public void setOutputFormatChpHtmlDir(String outputFormatChpHtmlDir) {
		this.outputFormatChpHtmlDir = outputFormatChpHtmlDir;
	}

	public List<OtherDocTagData> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<OtherDocTagData> outputs) {
		this.outputs = outputs;
	}

	public void addOutput(final OtherDocTagData output) {
		if (outputs == null)
			outputs = new ArrayList<OtherDocTagData>();
		// TODO Compare for duplicats!!
		outputs.add(output);
	}

	public List<KeyValuePair> getExternalIDs() {
		return externalIDs;
	}

	public void setExternalIDs(List<KeyValuePair> externalIDs) {
		this.externalIDs = externalIDs;
	}

	public void addExternalIDs(final KeyValuePair output) {
		if (externalIDs == null)
			externalIDs = new ArrayList<KeyValuePair>();
		// TODO Compare for duplicats!!
		externalIDs.add(output);
	}

}
