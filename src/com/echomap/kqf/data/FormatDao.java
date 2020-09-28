package com.echomap.kqf.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.echomap.kqf.EchoWriteConst;

/**
 * 
 */
public class FormatDao {
	public static final String DEFAULToutputEncoding = "Cp1252";
	// private final static Logger LOGGER =
	// LogManager.getLogger(FormatDao.class);
	private String version = null;

	// Profile
	private String inputFilename = null;
	private String profileName = null;
	private String storyTitle1 = null;
	private String storyTitle2 = null;
	// private String storyTitle3 = null;
	private String seriesTitle = null;
	private String volume = null;
	private String keywords = null;
	private String formatMode = null;
	private String filePrefix = null;
	private String outputEncoding = DEFAULToutputEncoding;
	private boolean includeChapterName = true;

	// Count

	// Input
	private String docTagStart = null;
	private String docTagEnd = null;
	private String chapterHeaderTag = "h1";
	private String sectionHeaderTag = "h1";
	private String regexpChapter = null;
	private String regexpSection = null;
	// private String chapterDivider = null;
	// private String sectionDivider = null;

	// Outputs
	private String outputFilename = null;
	private String outputCountFile = null;
	private String outputOutlineFile = null;
	private String outputOutlineFile1 = null;

	// Formatting Options
	private Boolean removeChptDiv = false;
	private Boolean removeSectDiv = false;
	private String centerableLineText = null;
	private Boolean centerStars = false;
	private Boolean dropCapChapter = false;
	private Boolean wantTextChptOutput = false;
	private Boolean timeLineAddTimePerScene = false;

	private Integer outputFormatDigits = null;

	//
	private String writeChapters = null;
	private String writeChaptersText = null;

	// DocTags
	private String outputDocTagsOutlineFile;
	private String outputDocTagsSceneFile;
	// private String outputDocTagsOther1File;

	private String docTagsOutlineCompressTags;
	private String docTagsOutlineExpandTags;
	private String docTagsSceneTags;
	private String docTagsSceneCoTags;
	// private String docTagsOther1Tags;

	private List<OtherDocTagData> outputs = new ArrayList<>();
	private List<KeyValuePair> externalIDs = new ArrayList<>();

	private Integer docTagsMaxLineLength = 70;
	private String docTagsScenePrefix = "";
	private String docTagsSubScenePrefix = "";
	private String sceneCoalateDivider = "";

	//
	private String outputCharCardFile = null;
	private String timelineTimePadding = null;
	// Calculated once at set of string list
	// private List<String> docTagsOutlineCompressTagsList;

	/**
	 *  
	 */
	public String prettyPrint() {
		final StringBuilder sbuf = new StringBuilder();
		addLine(sbuf, "InputFilename", this.inputFilename);

		addLine(sbuf, "OutputFilename", this.outputFilename);
		addLine(sbuf, "OutputCountFile", this.outputCountFile);
		addLine(sbuf, "OutputOutlineFile", this.outputOutlineFile);
		addLine(sbuf, "OutputOutlineFile1", this.outputOutlineFile1);

		addLine(sbuf, "OutputEncoding", this.outputEncoding);

		addLine(sbuf, "ProfileName", this.profileName);
		addLine(sbuf, "SeriesTitle", this.seriesTitle);
		addLine(sbuf, "volume", this.volume);
		addLine(sbuf, "keywords", this.keywords);

		addLine(sbuf, "StoryTitle1", this.storyTitle1);
		addLine(sbuf, "StoryTitle2", this.storyTitle2);
		// addLine(sbuf, "StoryTitle3", this.storyTitle3);
		// addLine(sbuf, "ChapterDivider", this.chapterDivider);
		// addLine(sbuf, "SectionDivider", this.sectionDivider);
		addLine(sbuf, "ChapterDivider", this.regexpChapter);
		addLine(sbuf, "SectionDivider", this.regexpSection);
		addLine(sbuf, "Centerablevalues", this.centerableLineText);
		addLine(sbuf, "CenterStars", this.centerStars.toString());
		addLine(sbuf, "DropCapChapter", this.dropCapChapter.toString());
		addLine(sbuf, "WantTextOutput", this.wantTextChptOutput.toString());
		addLine(sbuf, "TimeLineAddTimePerScene", this.timeLineAddTimePerScene.toString());

		addLine(sbuf, "FormatMode:", this.formatMode);
		addLine(sbuf, "WriteChapters:", this.getWriteChapters());
		addLine(sbuf, "WriteChaptersText:", this.getWriteChaptersText());
		addLine(sbuf, "ChapterHeaderTag:", this.getChapterHeaderTag());
		addLine(sbuf, "SectionHeaderTag:", this.getSectionHeaderTag());

		addLine(sbuf, "DocTagStart:", this.getDocTagStart());
		addLine(sbuf, "DocTagEnd:", this.getDocTagEnd());

		addLine(sbuf, "OutputDocTagsOutlineFile:", this.getOutputDocTagsOutlineFile());
		addLine(sbuf, "OutputDocTagsSceneFile:", this.getOutputDocTagsSceneFile());
		// addLine(sbuf, "OutputDocTagsOther1File:",
		// this.getOutputDocTagsOther1File());

		addLine(sbuf, "DocTagsOutlineCTags:", this.getDocTagsOutlineCompressTags());
		addLine(sbuf, "DocTagsOutlineETags:", this.getDocTagsOutlineExpandTags());
		addLine(sbuf, "DocTagsSceneTags:", this.getDocTagsSceneTags());
		addLine(sbuf, "DocTagsSceneCoTags:", this.getDocTagsSceneCoTags());
		// addLine(sbuf, "DocTagsOther1Tags:", this.getDocTagsOther1Tags());

		addLine(sbuf, "DocTagsScenePrefix:", this.getDocTagsScenePrefix());
		addLine(sbuf, "DocTagsSubScenePrefix:", this.getDocTagsSubScenePrefix());

		addLine(sbuf, "CountOutputDigits:",
				(getOutputFormatDigits() == null ? "" : this.getOutputFormatDigits().toString()));

		addLine(sbuf, "OutputCharCardFile:", getOutputCharCardFile());
		addLine(sbuf, "TimelineTimePadding:", getTimelineTimePadding());

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

	public String getOutputCharCardFile() {
		return outputCharCardFile;
	}

	public void setOutputCharCardFile(String outputCharCardFile) {
		this.outputCharCardFile = outputCharCardFile;
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
		// LOGGER.debug("outputOutlineFile = '" + outputOutlineFile + "'");
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

	// public String getChapterDivider() {
	// return chapterDivider;
	// }
	//
	// public void setChapterDivider(String chapterDivider) {
	// this.chapterDivider = chapterDivider;
	// }
	//
	// public String getSectionDivider() {
	// return sectionDivider;
	// }
	//
	// public void setSectionDivider(String sectionDivider) {
	// this.sectionDivider = sectionDivider;
	// }

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

	public Boolean getTimeLineAddTimePerScene() {
		return timeLineAddTimePerScene;
	}

	public void setTimeLineAddTimePerScene(Boolean timeLineAddTimePerScene) {
		this.timeLineAddTimePerScene = timeLineAddTimePerScene;
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
			outputEncoding = EchoWriteConst.DEFAULToutputEncoding;
		return outputEncoding;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	// TODO Add more encodings?
	public Charset getCharSet() {
		Charset selCharSet = StandardCharsets.UTF_8;
		switch (this.getOutputEncoding()) {
		case "ISO_8859_1":
		case "iso_8859_1":
		case "8859_1":
		case "Cp1252":
		case "CP1252":
		case "1252":
			selCharSet = StandardCharsets.ISO_8859_1;
			break;
		case "ASCII":
		case "ascii":
			selCharSet = StandardCharsets.US_ASCII;
			break;
		case "UTF":
		case "utf":
			selCharSet = StandardCharsets.UTF_8;
			break;
		case "UTF-8":
		case "UTF8":
		case "utf-8":
		case "utf8":
			selCharSet = StandardCharsets.UTF_8;
			break;
		case "UTF-16":
		case "UTF16":
		case "utf-16":
		case "utf16":
			selCharSet = StandardCharsets.UTF_16;
			break;
		default:
			selCharSet = StandardCharsets.US_ASCII;
			break;
		}
		// LOGGER.debug("preHandler: Charset chosen: " + selCharSet);
		return selCharSet;
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

	public Integer getOutputFormatDigits() {
		if (outputFormatDigits == null)
			outputFormatDigits = 1;
		return outputFormatDigits;
	}

	public void setOutputFormatDigits(Integer countOutputDigits) {
		this.outputFormatDigits = countOutputDigits;
	}

	public String getOutputOutlineFile1() {
		return outputOutlineFile1;
	}

	public void setOutputOutlineFile1(String outputOutlineFile1) {
		// LOGGER.debug("outputOutlineFile1 = '" + outputOutlineFile1 + "'");
		this.outputOutlineFile1 = outputOutlineFile1;
	}

	public Boolean getWantTextChptOutput() {
		return wantTextChptOutput;
	}

	public void setWantTextChptOutput(Boolean wantTextOutput) {
		this.wantTextChptOutput = wantTextOutput;
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

	// public String getOutputDocTagsOther1File() {
	// return outputDocTagsOther1File;
	// }
	//
	// public void setOutputDocTagsOther1File(String outputDocTagsOther1File) {
	// this.outputDocTagsOther1File = outputDocTagsOther1File;
	// }

	// TODO create list like for other Lists, to prevent string matches when
	// shouldn't be?
	public String getDocTagsSceneTags() {
		return docTagsSceneTags;
	}

	public void setDocTagsSceneTags(String docTagsSceneTags) {
		this.docTagsSceneTags = docTagsSceneTags;
	}

	// public String getDocTagsOther1Tags() {
	// return docTagsOther1Tags;
	// }
	//
	// public void setDocTagsOther1Tags(String docTagsOther1Tags) {
	// this.docTagsOther1Tags = docTagsOther1Tags;
	// }

	public String getDocTagsSceneCoTags() {
		return docTagsSceneCoTags;
	}

	public void setDocTagsSceneCoTags(String docTagsSceneCoTags) {
		this.docTagsSceneCoTags = docTagsSceneCoTags;
	}

	public String getWriteChaptersText() {
		return writeChaptersText;
	}

	public void setWriteChaptersText(String writeChaptersText) {
		this.writeChaptersText = writeChaptersText;
	}

	public Integer getDocTagsMaxLineLength() {
		return docTagsMaxLineLength;
	}

	public void setDocTagsMaxLineLength(Integer docTagsMaxLineLength) {
		this.docTagsMaxLineLength = docTagsMaxLineLength;
	}

	public String getDocTagsOutlineCompressTags() {
		return docTagsOutlineCompressTags;
	}

	// public List<String> getDocTagsOutlineCompressTags() {
	// return docTagsOutlineCompressTagsList;
	// }
	//
	public void setDocTagsOutlineCompressTags(String docTagsOutlineCompressTags) {
		this.docTagsOutlineCompressTags = docTagsOutlineCompressTags;
		// if (!StringUtils.isBlank(docTagsOutlineCompressTags)) {
		// final String[] strs = StringUtils.split(docTagsOutlineCompressTags,
		// ", ");
		// this.docTagsOutlineCompressTagsList = Arrays.asList(strs);
		// } else
		// this.docTagsOutlineCompressTagsList = new ArrayList<>();
	}

	// TODO create list like for other Lists, to prevent string matches when
	// shouldn't be?
	public String getDocTagsOutlineExpandTags() {
		return docTagsOutlineExpandTags;
	}

	public void setDocTagsOutlineExpandTags(String docTagsOutlineExpandTags) {
		this.docTagsOutlineExpandTags = docTagsOutlineExpandTags;
	}

	public void setVersion(final String property) {
		this.version = property;
	}

	public String getVersion() {
		return version;
	}

	public void setFilePrefix(final String property) {
		this.filePrefix = property;
	}

	public String getFilePrefix() {
		return filePrefix;
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

	public String getDocTagsScenePrefix() {
		return docTagsScenePrefix;
	}

	public void setDocTagsScenePrefix(String docTagsScenePrefix) {
		this.docTagsScenePrefix = docTagsScenePrefix;
	}

	public String getDocTagsSubScenePrefix() {
		return docTagsSubScenePrefix;
	}

	public void setDocTagsSubScenePrefix(String docTagsSubScenePrefix) {
		this.docTagsSubScenePrefix = docTagsSubScenePrefix;
	}

	public void setSceneCoalateDivider(String text) {
		sceneCoalateDivider = text;
	}

	public String getSceneCoalateDivider() {
		return sceneCoalateDivider;
	}

	public List<OtherDocTagData> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<OtherDocTagData> outputs) {
		this.outputs = outputs;
	}

	public List<KeyValuePair> getExternalIDs() {
		return externalIDs;
	}

	public void setExternalIDs(List<KeyValuePair> externalIDs) {
		this.externalIDs = externalIDs;
	}

	public String getSeriesTitle() {
		return seriesTitle;
	}

	public void setSeriesTitle(String seriesTitle) {
		this.seriesTitle = seriesTitle;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public boolean isIncludeChapterName() {
		return includeChapterName;
	}

	public void setIncludeChapterName(boolean includeChapterName) {
		this.includeChapterName = includeChapterName;
	}

	public String getTimelineTimePadding() {
		return timelineTimePadding;
	}

	public void setTimelineTimePadding(String timelineTimePadding) {
		this.timelineTimePadding = timelineTimePadding;
	}

}
