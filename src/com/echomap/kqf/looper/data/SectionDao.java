package com.echomap.kqf.looper.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SectionDao implements PartitionDao {

	private int numChars = 0;
	private int numWords = 0;
	private int numLines = 0;

	private String name = "";
	private String title = "";
	private String number = "";

	public SectionDao() {
		clear();
	}

	public SectionDao(final SimpleSectionDao ssDao) {
		clear();
		this.setName(ssDao.sname);
		this.setNumber(ssDao.snum);
		this.setTitle(ssDao.title);
		// this.setNumChars(ssDao.getNumChars());
		// this.setNumLines(ssDao.getNumLines());
		// this.setNumWords(ssDao.getNumWords());
	}

	public SectionDao(final SectionDao cdao) {
		this.setName(cdao.getName());
		this.setNumber(cdao.getNumber());
		this.setTitle(cdao.getTitle());
		// cdao.getNums());
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getTitle() {
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

	public void setTitle(String title) {
		this.title = title;
	}

	public void clear() {
		this.name = null;
		this.title = null;
		this.number = "";
		this.numChars = 0;
		this.numLines = 0;
		this.numWords = 0;
	}

	public void copy(SectionDao cdao) {
		this.name = cdao.name;
		this.title = cdao.title;
		this.number = cdao.number;
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
