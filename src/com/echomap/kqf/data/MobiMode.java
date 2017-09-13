package com.echomap.kqf.data;

public class MobiMode implements FormatMode {
	public MobiMode() {
		System.out.println("MobiMode: Created");
	}

	@Override
	public String getHeaderFile() {
		return "/mobiheader.html";
	}


	@Override
	public String getPlainCenterTextTag() {
		return "MsoPlainTextCenter";
	}

	@Override
	public String getPlainTextTag() {
		return "MsoPlainText";
	}

	/*
	@Override
	public String getFirstChapterPreTag() {
		return "<p class=\"MsoChapter\">";
	}

	@Override
	public String getChapterPreTag() {
		return "<mbp:pagebreak/>\n" + "<p class=\"MsoChapter\">";
	}

	@Override
	public String getChapterPostTag() {
		return "</p>";
	}*/

	@Override
	public String getHTMLCenterReplacement() {
		return "MsoPlainTextCenter";
		//<p class="plain"><center>* *</center></p>
	}
	
	

}
