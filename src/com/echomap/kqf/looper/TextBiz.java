package com.echomap.kqf.looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;

public class TextBiz {
	private final static Logger LOGGER = LogManager.getLogger(TextBiz.class);

	final static String newLine = System.getProperty("line.separator");
	final static String special1 = "* * * * * * * *";

	// public static enum DOCTAGTYPE {
	// NONE, ALLDOCTAG, HASDOCTAG, LONGDOCTAG
	// };

	public static enum LINETYPE {
		PLAIN, CHAPTER, SECTION
	};

	public static enum SECTIONTYPE {
		PLAIN, INSPECIAL, NOTSPECIAL
	};

	public static boolean lineEmpty(String st) {
		if (StringUtils.isBlank(st))
			return true;
		if (st.compareTo("&nbsp;") == 0)
			return true;
		return false;
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

	public static String createPreTag(final String tagname) {
		return "<" + tagname + ">";
	}

	public static String createPostTag(final String tagname) {
		return "</" + tagname + ">";
	}

	public static String cleanText(String st, Boolean removeSectDiv, String sectionDivider, SECTIONTYPE sectionType,
			final String docTagStart, final String docTagEnd) {
		String st2 = st.replaceAll("Section: ", "");
		return cleanText(st2, removeSectDiv, sectionDivider, docTagStart, docTagEnd);
	}

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

	public static boolean checkHTMLCenterLine(FormatDao formatDao, String st) {
		if (st.startsWith("<center>") && st.endsWith("</center>"))
			return true;
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

	public static SECTIONTYPE isSection(final String line, final String miscDiv, final boolean isInSpecial) {
		if (line.startsWith("Section")) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoSection>";
			return SECTIONTYPE.PLAIN;
		} else if (line.indexOf("Section:") > -1) {
			// preTag = "<p class=MsoSection>";
			return SECTIONTYPE.PLAIN;
		} else if (line.startsWith(special1) && !isInSpecial) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoPlainText>";
			return SECTIONTYPE.NOTSPECIAL;
		} else if (line.indexOf("Section") > -1 && isInSpecial) {
			// preTag = "<p class=MsoSection>";
			return SECTIONTYPE.INSPECIAL;
		}
		// if (miscDiv != null) {
		// if (line.contains(miscDiv))// && line.contains("Section"))
		// return SECTIONTYPE.PLAIN;
		// }
		return null;
	}

	public static String isAnHtmlLine(final String line) {
		String line2 = line.toLowerCase();
		if (line2.startsWith("<div"))
			return "div";
		return null;
	}

	public static String wrapString(final String str) {
		if (null == str)
			return "<null>";
		return "'" + str + "'";
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
		return st.trim();

	}

	public static void countWords(final LooperDao ldao, final CountDao dao, final FormatDao fdao) {
		boolean inWord = false;

		String text2 = cleanPlainText(ldao.getCurrentLine(), fdao.getDocTagStart(), fdao.getDocTagEnd(), false);

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
	// public static boolean isChapter(final String line, final String
	// miscChapterDiv) {
	// if (line.startsWith("Chapter"))
	// return true;
	// if (line.contains("--") && line.contains("Chapter"))
	// return true;
	// if (line.contains("-=") && line.contains("Chapter"))
	// return true;
	// if (miscChapterDiv != null)
	// if (line.contains(miscChapterDiv) && line.contains("Chapter"))
	// return true;
	// return false;
	// }

	public static SimpleChapterDao isChapter(final String line, final String chapterDivider) {
		SimpleChapterDao dao = new SimpleChapterDao();
		if (line.startsWith("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, null);
			dao = parseChapterName(line, null);
			return dao;
		}
		if (line.contains("--") && line.contains("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, "--");
			dao = parseChapterName(line, "--");
			return dao;
		}
		if (line.contains("-=") && line.contains("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, "-=");
			dao = parseChapterName(line, "-=");
			return dao;
		}
		if (chapterDivider != null && line.contains(chapterDivider) && line.contains("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, chapterDivider);
			dao = parseChapterName(line, chapterDivider);
			return dao;
		}
		dao.isChapter = false;
		dao.name = null;
		return dao;
	}

	static DocTagLine isDocTag(final String line, final String startTag, final String endTag) {		
		final DocTagLine dtl = new DocTagLine();
		if (StringUtils.isBlank(startTag) || StringUtils.isBlank(endTag)) {
			dtl.setupNotADocTag(line);
			// return DOCTAGTYPE.NONE;
			return dtl;
		}

		if (line.contains(startTag) && line.contains(endTag)) {
			int idx1 = line.indexOf(startTag);
			int idx2 = line.indexOf(endTag);
			if (idx1 == 0 && idx2 == line.length() - endTag.length()) {
				dtl.setupOnlyDocTag(line.substring(idx1 + startTag.length(), idx2));
				// return DOCTAGTYPE.ALLDOCTAG;
			} else {
				dtl.setupContainsDocTag(line, line.substring(idx1 + startTag.length(), idx2));

				String line2 = line;// .substring(idx2 + endTag.length());
				DocTag dt = findNextDocTag(startTag, endTag, line2);
				while (dt != null) {
					dtl.addDocTag(dt);
					if (line2.length() > idx2 + endTag.length()) {
						line2 = line2.substring(idx2 + endTag.length());
						dt = findNextDocTag(startTag, endTag, line2);
						idx2 = line2.indexOf(endTag);
					} else
						dt = null;
				}
				// return DOCTAGTYPE.HASDOCTAG;
			}
		} else if (line.contains(startTag)) {
			int idx1 = line.indexOf(startTag);
			dtl.setupLongDocTag(line, line.substring(idx1 + startTag.length()));
			// return DOCTAGTYPE.LONGDOCTAG;
		} else if (line.contains(endTag)) {
			dtl.setEndDocTag(true);		
			// return DOCTAGTYPE.LONGDOCTAG;
		}
		// return DOCTAGTYPE.NONE;
		return dtl;
	}

	private static DocTag findNextDocTag(final String startTag, final String endTag, final String line) {
		DocTag dt = null;
		int idx1 = line.indexOf(startTag);
		int idx2 = line.indexOf(endTag);
		if (idx1 > -1 && idx2 > -1) {
			final String str = line.substring(idx1 + startTag.length(), idx2);
			dt = new DocTag(str);
		}
		return dt;
	}

	public static String parseForDocTags(final String st, final String docTagStart, final String docTagEnd) {
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

	private static SimpleChapterDao parseChapterName(final String line, final String div) {
		final SimpleChapterDao dao = new SimpleChapterDao();
		final int idxColon = line.indexOf(":");
		// String ret = line;
		StringBuffer divBuf = new StringBuffer();
		divBuf.append(div);
		StringBuffer divBufR = divBuf.reverse();
		String divR = divBufR.toString();
		String pre = line;
		String post = null;
		Integer num = null;
		if (idxColon != -1) {
			pre = line.substring(0, idxColon);
			post = line.substring(idxColon + 1);
		}
		if (pre == null) {
			pre = line;
		}

		if (div != null) {
			if (pre.startsWith(div))
				pre = pre.substring(div.length());
			else if (pre.indexOf(div) > -1)
				pre = pre.substring(pre.indexOf(div) + div.length());
			if (post.endsWith(divR))
				post = post.substring(0, post.length() - divR.length());
		}
		if (pre != null)
			pre = pre.trim();
		if (post != null)
			post = post.trim();

		final int idxSpcePre = pre.lastIndexOf(" ");
		if (idxSpcePre >= 1) {
			String temp = pre.substring(idxSpcePre);
			if (temp != null)
				temp = temp.trim();

			if (temp != null) {
				try {
					num = Integer.valueOf(temp);
				} catch (NumberFormatException e) {
					// e.printStackTrace();
					num = 0;
				}
			}
		}

		dao.name = pre;
		dao.isChapter = true;
		dao.numerical = num;
		dao.title = post;

		// return ret;
		return dao;
	}
}
