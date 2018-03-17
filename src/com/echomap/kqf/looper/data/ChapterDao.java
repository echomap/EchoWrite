package com.echomap.kqf.looper.data;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ChapterDao implements PartitionDao {

	private int numChars = 0;
	private int numWords = 0;
	private int numLines = 0;

	private String chapterName = "";
	private String chapterTitle = "";
	private String chapterNumber = "";

	public ChapterDao() {
		clear();
	}

	public ChapterDao(final CountDao cdao) {
		this.setChapterName(cdao.getChapterName());
		this.setChapterNumber(cdao.getChapterNumber());
		this.setChapterTitle(cdao.getChapterTitle());
		// cdao.getNumChapters());
		this.setNumChars(cdao.getNumChars());
		this.setNumLines(cdao.getNumLines());
		this.setNumWords(cdao.getNumWords());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int getNumChars() {
		return numChars;
	}

	public void setNumChars(int numChars) {
		this.numChars = numChars;
	}

	public int getNumLines() {
		return numLines;
	}

	public void setNumLines(int numLines) {
		this.numLines = numLines;
	}

	public int getNumWords() {
		return numWords;
	}

	public void setNumWords(int numWords) {
		this.numWords = numWords;
	}

	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}

	public String getChapterNumber() {
		return chapterNumber;
	}

	public void setChapterNumber(String chapterNumber) {
		this.chapterNumber = chapterNumber;
	}

	public String getChapterTitle() {
		if (chapterTitle != null) {
			chapterTitle = chapterTitle.trim();
			if (!chapterTitle.startsWith("\"")) {
				chapterTitle = "\"" + chapterTitle;
			}
			if (!chapterTitle.endsWith("\"")) {
				chapterTitle = chapterTitle + "\"";
			}
		}
		return chapterTitle;
	}

	public void setChapterTitle(String chapterTitle) {
		this.chapterTitle = chapterTitle;
	}

	public void clear() {
		this.chapterName = null;
		this.chapterTitle = null;
		this.chapterNumber = "";
		this.numChars = 0;
		this.numLines = 0;
		this.numWords = 0;
	}

	public void copy(ChapterDao cdao) {
		this.chapterName = cdao.chapterName;
		this.chapterTitle = cdao.chapterTitle;
		this.chapterNumber = cdao.chapterNumber;
		this.numChars = cdao.numChars;
		this.numLines = cdao.numLines;
		this.numWords = cdao.numWords;
	}

	public void addOneToNumChars() {
		this.numChars++;
	}

	public void addOneToNumLines() {
		this.numLines++;
	}

	public void addOneToNumWords() {
		this.numWords++;
	}

	public void addNumWords(int numWords2) {
		this.numWords += numWords2;
	}

}
