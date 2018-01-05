package com.echomap.kqf.looper.data;

public class CountDao {
	private int numChars = 0;
	private int numWords = 0;
	private int numLines = 0;
	private int numChapters = -1;

	private String chapterName = "";
	private String chapterTitle = "";
	private int chapterNumber = -1;

	public CountDao() {
		clear();
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

	public int getChapterNumber() {
		return chapterNumber;
	}

	public void setChapterNumber(int chapterNumber) {
		this.chapterNumber = chapterNumber;
	}

	public String getChapterTitle() {
		return chapterTitle;
	}

	public String getQuotedChapterTitle() {
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
		this.chapterNumber = 0;
		this.numChars = 0;
		this.numLines = 0;
		this.numWords = 0;
		this.numChapters = 0;
	}

	public void copy(CountDao cdao) {
		this.chapterName = cdao.chapterName;
		this.chapterTitle = cdao.chapterTitle;
		this.chapterNumber = cdao.chapterNumber;
		this.numChars = cdao.numChars;
		this.numLines = cdao.numLines;
		this.numWords = cdao.numWords;
		this.numChapters = cdao.numChapters;
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

	public void addChapterCount(int i) {
		setNumChapters(getNumChapters() + i);
	}

	public int getNumChapters() {
		return numChapters;
	}

	public void setNumChapters(int numChapters) {
		this.numChapters = numChapters;
	}
}
