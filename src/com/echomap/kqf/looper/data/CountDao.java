package com.echomap.kqf.looper.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author mkatz
 *
 */
public class CountDao {
	private int numChars = 0;
	private int numWords = 0;
	private int numLines = 0;
	private int counter = -1;
	// private int numChapters = -1;

	private String name = "";
	private String title = "";
	private String number = "";

	private String parent = "";

	public CountDao() {
		clear();
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

	public String getName() {
		return name;
	}

	public void setName(String cName) {
		this.name = cName;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String chapterNumber) {
		this.number = chapterNumber;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getTitle() {
		return title;
	}

	public String getQuotedChapterTitle() {
		if (title != null) {
			title = title.trim();
			if (!title.startsWith("\"")) {
				title = "\"" + title;
			}
			if (!title.endsWith("\"")) {
				title = title + "\"";
			}
		}
		return title;
	}

	public void setTitle(String chapterTitle) {
		this.title = chapterTitle;
	}

	public void clear() {
		this.name = null;
		this.title = null;
		this.number = "";
		this.parent = "";
		this.numChars = 0;
		this.numLines = 0;
		this.numWords = 0;
		// this.numChapters = 0;
	}

	public void copy(CountDao cdao) {
		this.name = cdao.name;
		this.title = cdao.title;
		this.number = cdao.number;
		this.parent = cdao.parent;
		this.numChars = cdao.numChars;
		this.numLines = cdao.numLines;
		this.numWords = cdao.numWords;
		// this.numChapters = cdao.numChapters;
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
		setCounter(getCounter() + i);
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
	// public int getNumChapters() {
	// return numChapters;
	// }
	//
	// public void setNumChapters(int numChapters) {
	// this.numChapters = numChapters;
	// }
}
