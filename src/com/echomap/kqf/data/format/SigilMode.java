package com.echomap.kqf.data.format;

public class SigilMode implements FormatMode {
	final String chapterPreTag = "<p class=\"MsoChapter\">";

	public SigilMode() {
		System.out.println("SigilMode: Created");
	}

	@Override
	public String getHeaderFile() {
		return "/sigilheader.html";
	}

	@Override
	public String getPlainCenterTextTag() {
		return "center";
	}

	@Override
	public String getPlainTextTag() {
		return "plain";
	}

	/*
	@Override
	public String getFirstChapterPreTag() {
		return "<h1>";
	}

	@Override
	public String getChapterPreTag() {
		return "<h1>";
	}

	@Override
	public String getChapterPostTag() {
		return "</h1>";
	}
	*/
	
	@Override
	public String getHTMLCenterReplacement() {
		return "center";
		//<p class="plain"><center>* *</center></p>
	}
}
