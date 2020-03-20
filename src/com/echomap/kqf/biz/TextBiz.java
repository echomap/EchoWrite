package com.echomap.kqf.biz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.looper.data.SimpleSectionDao;

public class TextBiz {
	private final static Logger LOGGER = LogManager.getLogger(TextBiz.class);

	public final static String newLine = System.getProperty("line.separator");
	// TODO need this? special1
	public final static String special1 = "* * * * * * * *";

	public static String wrapString(final String str) {
		if (null == str)
			return "<null>";
		return "'" + str + "'";
	}

	public static DocTag isMetaTag(final DocTagLine dttGL) {
		if (dttGL.isLongDocTag() || !dttGL.isOnlyDoctag() || dttGL.getDocTags() == null
				|| dttGL.getDocTags().size() < 1)
			return null;
		final DocTag docTag0 = dttGL.getDocTags().get(0);
		if (docTag0 == null)
			return null;
		if ("meta".compareToIgnoreCase(docTag0.getName()) == 0) {
			TextParsingBiz.parseDocTagSubValues(docTag0);
			return docTag0;
		}
		return null;
	}

	public static SimpleSectionDao isSection(final String line, final String miscDiv) {
		final SimpleSectionDao ssd = new SimpleSectionDao();

		// Create a Pattern object
		final Pattern r = Pattern.compile(miscDiv);

		// Now create matcher object.
		final Matcher matcher = r.matcher(line);
		if (matcher.find()) {
			String mNum = null;
			try {
				mNum = matcher.group("snum");
			} catch (Exception e) {
				LOGGER.error("SNum in section not found", e);
				e.printStackTrace();
			}
			String mName = null;
			try {
				mName = matcher.group("sname");
			} catch (Exception e) {
				LOGGER.error("SName in section not found", e);
				e.printStackTrace();
			}
			String mTitle = null;
			try {
				mTitle = matcher.group("stitle");
			} catch (Exception e) {
				LOGGER.error("STitle in section not found", e);
				e.printStackTrace();
			}
			// final Integer mNumI = Integer.valueOf(mNum);

			ssd.isSection = true;
			ssd.sname = mName;
			ssd.snum = mNum;
			ssd.title = mTitle;
		}
		return ssd;
	}

	public static SimpleChapterDao isChapter(final String line, final String chapterDivider) {
		SimpleChapterDao dao = new SimpleChapterDao();
		// Create a Pattern object
		final Pattern r = Pattern.compile(chapterDivider);

		// Now create matcher object.
		final Matcher matcher = r.matcher(line);
		if (matcher.find()) {
			dao.isChapter = true;
			String mNum = null;
			// Integer mNumI = null;
			try {
				mNum = matcher.group("cnum");
				// mNumI =
				Integer.valueOf(mNum);
			} catch (NumberFormatException e1) {
				LOGGER.warn("Failed to convert Chapter Number to a NUMBER.");
				// e1.printStackTrace(); // TODO report exception
			}
			dao.chpNum = mNum;
			// if (mNumI != null)
			// dao.numerical = mNumI;

			try {
				final String mName = matcher.group("cname");
				// if (mNum != null)
				// dao.name = mName + " " + mNum;
				// else
				dao.name = mName;
			} catch (Exception e) {
				LOGGER.error("CName in chapter not found", e);
				e.printStackTrace();
			}
			try {
				final String mTitle = matcher.group("ctitle");
				dao.title = mTitle;
			} catch (Exception e) {
				LOGGER.error("CTitle in chapter not found", e);
				e.printStackTrace();
			}
			return dao;
		}
		dao.isChapter = false;
		dao.name = null;
		return dao;
	}

	public static DocTagLine isDocTag(final String line, final String startTag, final String endTag) {
		final DocTagLine dtl = new DocTagLine();
		if (StringUtils.isBlank(startTag) || StringUtils.isBlank(endTag)) {
			dtl.setupNotADocTag(line);
			// return DOCTAGTYPE.NONE;
			parseInnerLIne(dtl, endTag, endTag);
			return dtl;
		}
		verifyLineCheck(dtl, line, startTag, endTag);

		if (line.contains(startTag) && line.contains(endTag)) {
			final String trimLine = line.trim();
			int idx1 = trimLine.indexOf(startTag);
			int idx2 = trimLine.indexOf(endTag);
			if (idx1 == 0 && idx2 == (trimLine.length() - endTag.length())) {
				final DocTag dt = findNextDocTag(startTag, endTag, line);
				dtl.setupOnlyDocTag(dt);// line.substring(idx1 +
										// startTag.length(), idx2));
				// return DOCTAGTYPE.ALLDOCTAG;
			} else {
				idx1 = line.indexOf(startTag);
				idx2 = line.indexOf(endTag);
				if (idx1 > idx2)
					idx1 = 0 - startTag.length();
				LOGGER.debug("isDocTag: idx1=" + idx1 + " idx2=" + idx2);
				dtl.setupContainsDocTag(line, line.substring(idx1 + startTag.length(), idx2));
				// Processing line2
				String line2 = line;// .substring(idx2 + endTag.length());
				DocTag dt = findNextDocTag(startTag, endTag, line2);
				// bareLine.delete(idx1, idx2+ endTag.length());
				while (dt != null) {
					dtl.addDocTag(dt);
					// String parseOut = line2.substring(idx1, idx2 +
					// endTag.length());
					// bareLine = bareLine.replace(parseOut, "");
					if (line2.length() > idx2 + endTag.length()) {
						line2 = line2.substring(idx2 + endTag.length());
						dt = findNextDocTag(startTag, endTag, line2);
						idx2 = line2.indexOf(endTag);
						// bareLine.delete()
					} else
						dt = null;
				}
				// return DOCTAGTYPE.HASDOCTAG;
				// dtl.setBareLine(bareLine.toString());
			}

			// Remove DocTags
			if (dtl.isHasDocTag()) {
				// final StringBuilder bareLine = new StringBuilder();
				// bareLine.append(line);
				String strBare = line;
				final List<DocTag> docTags = dtl.getDocTags();
				if (docTags != null) {
					for (final DocTag docTag : docTags) {
						LOGGER.debug("docTag: text: " + docTag.getFullText() + " tag: " + docTag.getFullTag());
						if (docTag.getFullTag() != null)
							strBare = strBare.replace(docTag.getFullTag(), "");
					}
				}
				dtl.setBareLine(strBare.trim());
				if (StringUtils.isEmpty(dtl.getBareLine()))
					dtl.setOnlyDoctag(true);
				else
					LOGGER.debug("docTag: has more data after DocTag...");
			}

		} else if (line.contains(startTag)) {
			int idx1 = line.indexOf(startTag);
			dtl.setupLongDocTag(line, line.substring(idx1 + startTag.length()));
			// return DOCTAGTYPE.LONGDOCTAG;
		} else if (line.contains(endTag)) {
			int idx1 = line.indexOf(endTag);
			// dtl.setupLongDocTag(line, line.substring(0, idx1));
			dtl.setLine(line.substring(0, idx1));
			dtl.setEndDocTag(true);
			// return DOCTAGTYPE.LONGDOCTAG;
		} else {
			dtl.setupNotADocTag(line);
		}

		//
		parseLineForMasterSub(dtl);

		// return DOCTAGTYPE.NONE;
		parseInnerLIne(dtl, startTag, endTag);
		return dtl;
	}

	public static void countWords(final LooperDao ldao, final CountDao dao, final FormatDao fdao) {
		// boolean inWord = false;
		String text2 = cleanPlainText(ldao.getCurrentLine(), fdao.getDocTagStart(), fdao.getDocTagEnd(), false);
		if (text2.length() < 1)
			return;
		countWords(text2, dao, fdao);
	}

	public static void countWords(String text2, final CountDao dao, final FormatDao fdao) {
		boolean inWord = false;
		if (text2 == null || text2.length() < 1)
			return;

		final int len = text2.length();
		for (int i = 0; i < len; i++) {
			final char c = text2.charAt(i);
			dao.addOneToNumChars();
			switch (c) {
			case '\n':
			case '\t':
				break;
			case ' ':
				if (inWord) {
					dao.addOneToNumWords();
					inWord = false;
				}
				break;
			default:
				inWord = true;
			}
		}
		if (inWord)
			dao.addOneToNumWords();
	}

	public static boolean isAnHtmlLine(final String line) {
		if (line == null)
			return false;
		String line2 = line.toLowerCase();
		final int idx1 = line2.indexOf("<div");
		final int idx2 = line2.indexOf("/div");
		if (idx1 > -1)
			return true;
		if (idx2 > -1)
			return true;
		// if (line2.startsWith("<div")) return true;
		// if (line2.endsWith("/div>")) return true;
		return false;
	}

	public static boolean centerCheck(final FormatDao formatDao, final String st) {
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

	public static boolean checkHTMLCenterLine(FormatDao formatDao, String st) {
		if (st.startsWith("<center>") && st.endsWith("</center>"))
			return true;
		return false;
	}

	public static String cleanFormatTags(final String textData) {
		String st2 = textData.replace(EchoWriteConst.DOCTAG_LIST, "");
		st2 = st2.replace(EchoWriteConst.DOCTAG_NEWLINE, "");
		st2 = st2.replace(EchoWriteConst.DOCTAG_PRE1, "");
		st2 = st2.replace(EchoWriteConst.DOCTAG_PRE2, "");
		st2 = st2.replace(EchoWriteConst.DOCTAG_SUBLIST, "");

		return st2;
	}

	public static String cleanPlainText(String st, final String docTagStart, final String docTagEnd) {
		return cleanPlainText(st, docTagStart, docTagEnd, true);
	}

	public static String cleanPlainText(final String stIn, final String docTagStart, final String docTagEnd,
			boolean htmlIfy) {
		// String st2 = st.replaceAll("\x", ""\"x");
		String st = stIn;
		if (StringUtils.isEmpty(st))
			if (htmlIfy)
				st = "&nbsp;";
			else
				st = "";

		int idx1 = st.indexOf(docTagStart);
		while (idx1 > -1) {
			// final int idx2 = st.indexOf(docTagEnd);
			final String st2 = st.substring(0, st.indexOf(docTagStart));
			// int idx2 = st.indexOf(docTagEnd);
			final String st3 = st.substring(st.indexOf(docTagEnd) + docTagEnd.length());
			// st = st.substring(0, st.indexOf(docTagStart) +
			// docTagStart.length());
			st = st2 + st3;
			// st = st.substring(st.indexOf(docTagEnd));
			idx1 = st.indexOf(docTagStart);
			// if (st.length() > idx2)
			// idx1 = st.substring(idx2).indexOf(docTagStart);
			// else
			// idx1 = -1;
		}

		// if (StringUtils.isEmpty(st))
		// st = "&nbsp;";
		st = stripHTMLTags(st);
		return st.trim();
	}

	public static String cleanText(final String st, final Boolean removeSectDiv, final String sectionDivider,
			final SimpleSectionDao sectionType, final String docTagStart, final String docTagEnd) {
		final String st2 = st.replaceAll("Section: ", "");
		return cleanText(st2, removeSectDiv, sectionDivider, docTagStart, docTagEnd);
	}

	// @TODO change misc to be a list?
	public static String cleanText(final String st, final boolean remChapterDiv, final String miscChapterDiv,
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

	public static boolean lineEmpty(String st) {
		if (StringUtils.isBlank(st))
			return true;
		if (st.compareTo("&nbsp;") == 0)
			return true;
		return false;
	}

	public static String createPreTag(final String tagname) {
		return "<" + tagname + ">";
	}

	public static String createPostTag(final String tagname) {
		return "</" + tagname + ">";
	}

	public static String doDropCaps(final String st, final String docTagStart, final String docTagEnd)
			throws IOException {
		String st2 = "";
		List<String> dropCapsList = doDropCapsList(st, docTagStart, docTagEnd);
		for (String dct : dropCapsList) {
			st2 += dct;
		}
		return st2;
	}

	public static List<String> doDropCapsList(final String st, final String docTagStart, final String docTagEnd)
			throws IOException {
		final List<String> slist = new ArrayList<String>();
		// <span class="dropcaps">I</span>
		String st2 = cleanPlainText(st, docTagStart, docTagEnd);
		LOGGER.debug("st = '" + st2 + "'");
		final String str1 = st2.substring(0, 1);
		LOGGER.debug("str1 = '" + str1 + "'");
		final String str2 = st2.substring(1);
		LOGGER.debug("str2 = '" + str2 + "'");

		slist.add("<span class=\"dropcaps\">");
		slist.add(str1);
		slist.add("</span>");
		slist.add(str2);
		return slist;
	}

	private static void parseInnerLIne(final DocTagLine dtl, final String startTag, final String endTag) {
		dtl.setBareLine(dtl.getRawLine().replace(startTag, "").replace(endTag, ""));
	}

	// TODO use a real library to do this
	private static String stripHTMLTags(final String htmlText) {
		final Pattern pattern = Pattern.compile("<[^>]*>");
		final Matcher matcher = pattern.matcher(htmlText);
		final StringBuffer sb = new StringBuffer(htmlText.length());
		while (matcher.find()) {
			matcher.appendReplacement(sb, "");
		}
		matcher.appendTail(sb);
		return sb.toString().trim();
	}

	private static void parseLineForMasterSub(final DocTagLine dtl) {
		if (dtl == null || StringUtils.isEmpty(dtl.getTextTagLine()))
			return;
		String line = dtl.getTextTagLine().trim();

		final String[] strList = line.split(":");
		if (strList != null && strList.length > 0)
			dtl.setParentTag(strList[0].trim());
		if (strList != null && strList.length > 1)
			dtl.setChildTag(strList[1].trim());
	}

	private static void verifyLineCheck(final DocTagLine dtl, final String line, final String startTag,
			final String endTag) {
		// Start Tags
		int idx1 = line.indexOf(startTag);
		int idx2 = idx1 + startTag.length();
		while (idx1 > -1) {
			try {
				dtl.addNumberToStartTags(1);
				idx1 = line.indexOf(startTag, idx2);
				idx2 = idx1 + startTag.length();
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				LOGGER.error(e);
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Probably a errant tag in line: <" + line + ">");
			}
		}

		// End Tags
		idx1 = line.indexOf(endTag);
		idx2 = idx1 + endTag.length();
		while (idx1 > -1) {
			try {
				dtl.addNumberToEndTags(1);
				idx1 = line.indexOf(endTag, idx2);
				idx2 = idx1 + endTag.length();
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				LOGGER.error(e);
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Probably a errant tag in line: <" + line + ">");
			}
		}
	}

	private static DocTag findNextDocTag(final String startTag, final String endTag, final String line) {
		DocTag dt = null;
		int idx1 = line.indexOf(startTag);
		int idx2 = line.indexOf(endTag);
		LOGGER.debug("line: <" + line + "> idx1:" + idx1 + " idx2:" + idx2);
		if (idx1 > -1 && idx2 > -1 && line.length() > idx2) {
			try {
				final String str = line.substring(idx1 + startTag.length(), idx2);
				final String str2 = line.substring(idx1, idx2 + endTag.length());
				dt = new DocTag(str);
				dt.setFullTag(str2);
				final String barePre = line.substring(0, idx1);
				final String barePost = line.substring(idx2 + endTag.length());
				final String bare = barePre + barePost;
				dt.setBareLine(bare.trim());
				dt.setInnerLine((bare.trim().replace(startTag, "").replace(endTag, "")));
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				LOGGER.error(e);
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Probably a errant tag in line: <" + line + ">");
			}
		}
		return dt;
	}

	public static String getFileNameOnly(final String filename) {
		String outFilename = null;
		// final String filenameOnly = outputFile.getName();
		final int extIdx = filename.lastIndexOf(".");
		if (extIdx >= 1) {
			// String ext = filename.substring(extIdx + 1);
			// outFilename = filename.replaceAll(ext, "txt");
			outFilename = filename.substring(0, extIdx);
		} else {
			outFilename = filename;
		}
		return outFilename;
	}

}
