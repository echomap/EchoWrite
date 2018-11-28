package com.echomap.kqf.looper.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.echomap.kqf.data.DocTagLine;

public class LooperDao {

	private File inputFile;
	private CountDao chaptCount = null;
	private CountDao sectionCount = null;
	private CountDao totalCount = new CountDao();
	private long lineCount = 0;

	private final List<SectionDao> sections = new ArrayList<SectionDao>();
	private final List<ChapterDao> chapters = new ArrayList<ChapterDao>();
	// private final List<PartitionDao> partitions = new
	// ArrayList<PartitionDao>();

	private boolean inSpecial = false;
	private boolean inLongDocTag = false;
	private boolean htmlLine = false;
	private boolean lastLineWasChapter = false;
	private Integer thisLineCharacterCount = 0;

	private String originalLine = null;
	private String currentLine = null;

	private SimpleChapterDao lineChapter = null;
	private SimpleSectionDao lineSection = null;
	private DocTagLine lineDocTagLine = null;

	// Count of tags for validity
	private long dtStartCount = 0;
	private long dtEndCount = 0;

	public LooperDao() {

	}

	public void clear() {
		setInSpecial(false);
		setInLongDocTag(false);
		setHtmlLine(false);
		dtStartCount = 0;
		dtEndCount = 0;
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

	public boolean getHtmlLine() {
		return htmlLine;
	}

	public void setHtmlLine(boolean htmlLine) {
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

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public void InitializeCount() {
		setTotalCount(new CountDao());
		setChaptCount(new CountDao());
		setSectionCount(new CountDao());

		getChapters().clear();

		getTotalCount().setCounter(1);
		getChaptCount().addOneToNumLines();
	}

	public CountDao getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(CountDao totalCount) {
		this.totalCount = totalCount;
	}

	public CountDao getChaptCount() {
		return chaptCount;
	}

	public void setChaptCount(CountDao chaptCount) {
		this.chaptCount = chaptCount;
	}

	public CountDao getSectionCount() {
		return sectionCount;
	}

	public void setSectionCount(CountDao sectionCount) {
		this.sectionCount = sectionCount;
	}

	public List<ChapterDao> getChapters() {
		return chapters;
	}

	public List<SectionDao> getSections() {
		return sections;
	}

	public String getOriginalLine() {
		return originalLine;
	}

	public void setOriginalLine(String originalLine) {
		this.originalLine = originalLine;
	}

	public String getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(String currentLine) {
		this.currentLine = currentLine;
	}

	public SimpleChapterDao getLineChapter() {
		return lineChapter;
	}

	public void setLineChapter(SimpleChapterDao lineChapter) {
		this.lineChapter = lineChapter;
	}

	public SimpleSectionDao getLineSection() {
		return lineSection;
	}

	public void setLineSection(SimpleSectionDao lineSection) {
		this.lineSection = lineSection;
	}

	public DocTagLine getLineDocTagLine() {
		return lineDocTagLine;
	}

	public void setLineDocTagLine(DocTagLine lineDocTagLine) {
		this.lineDocTagLine = lineDocTagLine;
	}

	public long getLineCount() {
		return lineCount;
	}

	public void setLineCount(long lineCount) {
		this.lineCount = lineCount;
	}

	public long getDtStartCount() {
		return dtStartCount;
	}

	public long getDtEndCount() {
		return dtEndCount;
	}

	public void addOneStartTag() {
		dtStartCount += 1;
	}

	public void addOneEndTag() {
		dtEndCount += 1;
	}

	public void addStartTag(final long cnt) {
		dtStartCount += cnt;
	}

	public void addEndTag(final long cnt) {
		dtEndCount += cnt;
	}
}
