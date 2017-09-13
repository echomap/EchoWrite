package com.echomap.kqf.two.biz;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.two.biz.FormatBiz.DOCTAGTYPE;
import com.echomap.kqf.two.biz.FormatBiz.SECTIONTYPE;
import com.echomap.kqf.two.data.FormatDao;

public class BaseBiz {
	private final static Logger LOGGER = LogManager.getLogger(BaseBiz.class);
	final static Properties props = new Properties();

	protected BaseBiz() {
		// try {
		// props.load(FormatBiz.class.getClassLoader().getResourceAsStream("cwc.properties"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// props.setProperty("version", "0.0.0");
		// }
	}

	String isAnHtmlLine(final String line) {
		String line2 = line.toLowerCase();
		if (line2.startsWith("<div"))
			return "div";
		return null;
	}

	boolean centerCheck(final FormatDao formatDao, final String st) {
		if (formatDao.getCenterableLineText() != null && st.compareTo(formatDao.getCenterableLineText()) == 0)
			return true;
		if (formatDao.getCenterStars()) {
			if (st.startsWith("*")) {
				String stIn = st.trim();
				stIn = stIn.replace("", "");
				if (stIn.matches(".*[**]")) {
					return true;
				}
			}
		}
		return false;
	}

	String cleanPlainText(String st, final String docTagStart, final String docTagEnd) {
		// String st2 = st.replaceAll("\x", ""\"x");
		if (StringUtils.isEmpty(st))
			st = "&nbsp;";
		else {
			int idx1 = st.indexOf(docTagStart);
			if (idx1 > -1) {
				String st2 = st.substring(0, st.indexOf(docTagStart));
				// int idx2 = st.indexOf(docTagEnd);
				String st3 = st.substring(st.indexOf(docTagEnd) + docTagEnd.length());
				// st = st.substring(0, st.indexOf(docTagStart) +
				// docTagStart.length());
				st = st2 + st3;
				// st = st.substring(st.indexOf(docTagEnd));
			}
		}
		if (StringUtils.isEmpty(st))
			st = "&nbsp;";
		return st.trim();
	}

	DOCTAGTYPE isDocTag(final String line, final String startTag, final String endTag) {
		if (StringUtils.isBlank(startTag) || StringUtils.isBlank(endTag))
			return DOCTAGTYPE.NONE;
		if (line.contains(startTag) && line.contains(endTag)) {
			int idx1 = line.indexOf(startTag);
			int idx2 = line.indexOf(endTag);
			if (idx1 == 0 && idx2 == line.length() - endTag.length())
				return DOCTAGTYPE.ALLDOCTAG;
			return DOCTAGTYPE.INDOCTAG;
		}
		if (line.contains(startTag)) {
			return DOCTAGTYPE.LONGDOCTAG;
		}
		return DOCTAGTYPE.NONE;
	}

	String getCurrentDateFmt() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		LOGGER.debug(dateFormat.format(cal)); // 2016/11/16 12:08:43
		return dateFormat.format(cal);
	}

	String parseForDocTags(final String st, final String docTagStart, final String docTagEnd) {
		String st2 = st;
		int idx1 = st2.indexOf(docTagStart);
		if (idx1 > -1) {
			st2 = st2.substring(idx1 + docTagStart.length());
			int idx2 = st2.indexOf(docTagEnd);
			if (idx2 < -1)
				idx2 = st2.length();
			st2 = st2.substring(0, idx2);
			LOGGER.debug("parseForDocTags: '" + st2 + "'");
		}
		return st2;
	}

	String cleanText(String st, Boolean removeSectDiv, String sectionDivider, SECTIONTYPE sectionType,
			final String docTagStart, final String docTagEnd) {
		String st2 = st.replaceAll("Section: ", "");
		return cleanText(st2, removeSectDiv, sectionDivider, docTagStart, docTagEnd);
	}

	String cleanText(final String st, final boolean remChapterDiv, final String miscChapterDiv,
			final String docTagStart, final String docTagEnd) {
		if (remChapterDiv) {
			String st2 = st.replaceAll("--", "");
			st2 = st2.replaceAll("--", "");
			st2 = st2.replaceAll("-=", "");
			st2 = st2.replaceAll("=-", "");
			if (miscChapterDiv != null)
				st2 = st2.replaceAll(miscChapterDiv, "");

			// st2 = st2.substring(st2.indexOf(docTagStart) + 1);
			// st2 = st2.substring(0, st2.indexOf(docTagEnd));

			if (st2.startsWith(" "))
				st2 = st2.substring(1, st2.length());
			if (st2.endsWith(" "))
				st2 = st2.substring(0, st2.length() - 1);
			LOGGER.debug("  Cleaned Chapter-Divs: " + st2);
			return st2.trim();
		}
		return st;
	}

}
