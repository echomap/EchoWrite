package com.echomap.kqf.data;

import java.util.List;

public class BookDataObj {
	private String bookKey;
	private String bookTitle;
	private String asin;
	private List<KeyValuePair> externalIDlist;

	public String getBookKey() {
		return bookKey;
	}

	public void setBookKey(String bookKey) {
		this.bookKey = bookKey;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public List<KeyValuePair> getExternalIDlist() {
		return externalIDlist;
	}

	public void setExternalIDlist(List<KeyValuePair> externalIDlist) {
		this.externalIDlist = externalIDlist;
	}

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

}
