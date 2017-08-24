package com.echomap.kqf.two;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Not thread safe
 */
public class FormatBiz {

	final static String newLine = System.getProperty("line.separator");

	final static String special1 = "* * * * * * * *";

	private FormatMode formatMode = null;

	boolean inSpecial = false;
	boolean lastLineWasChapter = false;
	String htmlLine = null;
	Integer thisLineCharacterCount = 0;
	final Map<String, String> filterMap = new HashMap<String, String>();

	Integer chapterCounter = 0;
	File outputChapterDir = null;
	FileWriter chapterWriter = null;

	Integer sectionCounter = 0;
	File outputSectionDir = null;
	FileWriter sectionWriter = null;

	public void format(final FormatDao formatDao) throws IOException {
		System.out.println("Formatter...>");
		System.out.println(formatDao);
		System.out.println(formatDao.prettyPrint());
		System.out.println("..>");

		if (formatDao.getFormatMode() == null) {
			formatMode = new MobiMode();
		} else if (formatDao.getFormatMode().toLowerCase().compareTo("sigil") == 0) {
			formatMode = new SigilMode();
		}

		final File inputFile = new File(formatDao.getInputFilename());
		final File outputFile = new File(formatDao.getOutputFilename());

		filterMap.put("@TITLE@", formatDao.getStoryTitle1());
		filterMap.put("@SUBTITLE@", formatDao.getStoryTitle1());
		filterMap.put("@ENCODING@", formatDao.getOutputEncoding());

		String charsetName = FormatDao.DEFAULToutputEncoding;
		if (formatDao.getOutputEncoding() != null && formatDao.getOutputEncoding().length() > 0)
			charsetName = formatDao.getOutputEncoding();

		// Create dirs
		final File outputDir = outputFile.getParentFile();
		if (outputDir != null)
			outputDir.mkdirs();

		FileWriter fWriter = null;
		BufferedReader reader = null;
		try {

			if (formatDao.getWriteChapters() != null && !StringUtils.isBlank(formatDao.getWriteChapters())) {
				outputChapterDir = new File(formatDao.getWriteChapters());
				outputChapterDir.mkdirs();

				outputSectionDir = new File(formatDao.getWriteChapters());
				outputSectionDir.mkdirs();

			}

			if (outputDir != null)
				fWriter = new FileWriter(outputFile, false);

			if (!inputFile.exists() && inputFile.length() < 0) {
				System.out.println("The specified file does not exist");
			} else {
				if (fWriter != null) {
					outputHeader(fWriter);
					fWriter.flush();

					final FileInputStream is = new FileInputStream(inputFile);
					final InputStreamReader isr = new InputStreamReader(is, charsetName);
					reader = new BufferedReader(isr);
					// final FileReader fr = new FileReader(inputFile);
					// reader = new BufferedReader(fr);

					String st = "";
					inSpecial = false;
					htmlLine = null;
					while ((st = reader.readLine()) != null) {
						writeLine(st, fWriter, formatDao);
					}
					outputFooter(fWriter);
					fWriter.flush();
				}
			}
		} finally {
			if (fWriter != null) {
				fWriter.flush();
				fWriter.close();
			}
			if (reader != null) {
				reader.close();
			}
			if (chapterWriter != null) {
				closeChapterWriter(chapterCounter);
			}
		}
	}

	private String filterLine(String st) {
		final Set<String> keys = filterMap.keySet();
		for (final String key : keys) {
			final String val = filterMap.get(key);
			final int idx = st.indexOf(key);
			if (idx > -1) {
				final String pre = st.substring(0, idx);
				final int endidx = idx + key.length();
				final String pst = st.substring(endidx, st.length());
				final String str = pre + val + pst;
				st = str;
			}
		}
		//
		// if (st.indexOf("@TITLE@") > -1) {
		// return st.replace("@TITLE@", storyTitle1);
		// }
		return st;
	}

	enum LINETYPE {
		PLAIN, CHAPTER, SECTION
	};

	enum SECTIONTYPE {
		PLAIN, INSPECIAL, NOTSPECIAL
	};

	private String cleanText(String st, Boolean removeSectDiv, String sectionDivider, SECTIONTYPE sectionType) {
		String st2 = st.replaceAll("Section: ", "");
		return cleanText(st2, removeSectDiv, sectionDivider);
	}

	private String cleanText(final String st, final boolean remChapterDiv, final String miscChapterDiv) {
		if (remChapterDiv) {
			String st2 = st.replaceAll("--", "");
			st2 = st2.replaceAll("--", "");
			st2 = st2.replaceAll("-=", "");
			st2 = st2.replaceAll("=-", "");
			if (miscChapterDiv != null)
				st2 = st2.replaceAll(miscChapterDiv, "");
			if (st2.startsWith(" "))
				st2 = st2.substring(1, st2.length());
			if (st2.endsWith(" "))
				st2 = st2.substring(0, st2.length() - 1);
			System.out.println("  Cleaned Chapter-Divs: " + st2);
			return st2.trim();
		}
		return st;
	}

	private boolean isChapter(final String line, final String miscChapterDiv) {
		if (line.startsWith("Chapter"))
			return true;
		if (line.contains("--") && line.contains("Chapter"))
			return true;
		if (line.contains("-=") && line.contains("Chapter"))
			return true;
		if (miscChapterDiv != null)
			if (line.contains(miscChapterDiv) && line.contains("Chapter"))
				return true;
		return false;
	}

	private SECTIONTYPE isSection(final String line, final String miscDiv) {
		if (line.startsWith("Section")) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoSection>";
			return SECTIONTYPE.PLAIN;
		} else if (line.indexOf("Section:") > -1) {
			// preTag = "<p class=MsoSection>";
			return SECTIONTYPE.PLAIN;
		} else if (line.startsWith(special1) && !inSpecial) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoPlainText>";
			return SECTIONTYPE.NOTSPECIAL;
		} else if (line.indexOf("Section") > -1 && inSpecial) {
			// preTag = "<p class=MsoSection>";
			return SECTIONTYPE.INSPECIAL;
		}
		// if (miscDiv != null) {
		// if (line.contains(miscDiv))// && line.contains("Section"))
		// return SECTIONTYPE.PLAIN;
		// }
		return null;
	}

	private String isAnHtmlLine(final String line) {
		String line2 = line.toLowerCase();
		if (line2.startsWith("<div"))
			return "div";
		return null;
	}

	private String createPreTag(final String tagname) {
		return "<" + tagname + ">";
	}

	private String createPostTag(final String tagname) {
		return "</" + tagname + ">";
	}

	private void writeLine(String st, final FileWriter fWriter, final FormatDao formatDao) throws IOException {
		// st = st.replace("<", "&lt;");
		String preTag = "";
		String postTag = null;
		// String postTag = "";
		// if (st == null) {
		// return;
		// }
		boolean htmlIsClosed = false;
		if (htmlLine == null) {
			htmlLine = isAnHtmlLine(st);
		} else {
			final String htmlLineNew = isAnHtmlLine(st);
			if (htmlLineNew != null && htmlLineNew.equalsIgnoreCase(htmlLine)) {
				htmlIsClosed = true;
			}
		}
		boolean isChapter = isChapter(st, formatDao.getChapterDivider());
		SECTIONTYPE sectionType = null;
		if (isChapter) {
			closeChapterWriter(chapterCounter);
			chapterCounter++;
			openNewChapterWriter(chapterCounter);
			if (chapterCounter > 1)
				preTag = createPreTag(formatDao.getChapterHeaderTag());
			// preTag = formatMode.getChapterPreTag();// "<mbp:pagebreak/>\n" +
			// "<p
			// class=\"MsoChapter\">";
			else
				preTag = createPreTag(formatDao.getChapterHeaderTag());
			// preTag = formatMode.getFirstChapterPreTag();// "<p
			// class=\"MsoChapter\">";
			postTag = "<p class=\"" + formatMode.getPlainTextTag() + "\">&nbsp;</p>";
		} else if (htmlLine != null) {
			preTag = " ";
		} else {
			sectionType = isSection(st, formatDao.getSectionDivider());
			if (sectionType != null)
				System.out.println("sectionType: " + sectionType + " for line: " + st);
			if (sectionType != null) {
				// closeSectionWriter(sectionCounter);
				sectionCounter++;
				// openNewSectionWriter(sectionCounter);
				if (sectionCounter > 1) {
					// TODO
				}
				// writeToSectionWriter(textToWrite);
				switch (sectionType) {
				case PLAIN:
					preTag = "<mbp:pagebreak/>\n" + "<p class=\"MsoSection\">";
					break;
				case INSPECIAL:
					preTag = "<p class=MsoSection>";
					break;
				case NOTSPECIAL:
					preTag = "<mbp:pagebreak/>\n" + "<p class=\"" + formatMode.getPlainTextTag() + "\">";
					break;
				default:
					break;
				}
			}
		}
		if (st.compareTo(formatDao.getStoryTitle1()) == 0 || (!StringUtils.isEmpty(formatDao.getStoryTitle2())
				&& st.compareTo(formatDao.getStoryTitle2()) == 0)) {
			preTag = "<p class=\"MsoTitle\">";
		}

		st = filterLine(st);

		if (st.startsWith(special1)) {
			if (inSpecial) {
				System.out.println("inSpecial flipped to false on: " + st);
				// System.out.println("preTag: " + preTag);
				inSpecial = false;
			} else {
				inSpecial = true;
				System.out.println("inSpecial flipped to true on: " + st);
			}
		}
		boolean centerThisLine = centerCheck(formatDao, st);
		if (centerThisLine) {
			preTag = "<p class=\"" + formatMode.getPlainCenterTextTag() + "\">";
		}

		if (preTag == null || preTag.length() < 1) {
			preTag = "<p class=\"" + formatMode.getPlainTextTag() + "\">";
		}
		boolean isHtmlCenter = checkHTMLCenterLine(formatDao, st);
		if (isHtmlCenter) {
			preTag = String.format("<p class=\"%s\">", formatMode.getHTMLCenterReplacement());
			int indx1 = st.indexOf(">");
			int indx2 = st.indexOf("<", 7);
			String stL = st.substring(indx1 + 1, indx2);
			st = stL;
		}

		fWriter.write(preTag);
		String textToWrite = null;
		if (isChapter) {
			textToWrite = cleanText(st, formatDao.getRemoveChptDiv(), formatDao.getChapterDivider());
			// fWriter.write(cleanText(st, formatDao.getRemoveChptDiv(),
			// formatDao.getChapterDivider()));
		} else if (sectionType != null) {
			textToWrite = cleanText(st, formatDao.getRemoveSectDiv(), formatDao.getSectionDivider(), sectionType);
			// fWriter.write(cleanText(st, formatDao.getRemoveSectDiv(),
			// formatDao.getSectionDivider()));
		} else if (lastLineWasChapter && st != null && st.length() > 1) {
			// TODO doDropCaps(st, fWriter);
			// <span class="dropcaps">I</span>
			textToWrite = cleanPlainText(st);
			// fWriter.write(st);
			lastLineWasChapter = false;
		} else {
			textToWrite = cleanPlainText(st);
			// fWriter.write(st);
		}
		if (!StringUtils.isBlank(textToWrite))
			thisLineCharacterCount += textToWrite.length();
		fWriter.write(textToWrite);

		if (sectionType != null) {
			openNewSectionWriter(sectionCounter, chapterCounter);
			writeToSectionWriter(textToWrite, formatDao.getSectionHeaderTag());
			closeSectionWriter(sectionCounter, chapterCounter);
		} else {
			// writeToChapterWriter(formatDao.get);
			// final String stTSt = "<"+formatDao.getChapterHeaderTag()+">";
			writeToChapterWriter(preTag);
			writeToChapterWriter(textToWrite);
		}

		if (htmlLine == null) {
			if (isChapter) {
				final String stTEnd = createPostTag(formatDao.getChapterHeaderTag());
				fWriter.write(stTEnd);
				writeToChapterWriter(stTEnd);
				// fWriter.write(formatMode.getChapterPostTag());
				// writeToChapterWriter(formatMode.getChapterPostTag());
			} else {
				if (!StringUtils.isBlank(textToWrite)) {
					if (thisLineCharacterCount == 0)
						fWriter.write("&nbsp;");
					fWriter.write("</p>");
					writeToChapterWriter("</p>");
					thisLineCharacterCount = 0;
				} else {
					if (thisLineCharacterCount == 0)
						fWriter.write("&nbsp;");
					fWriter.write("</p>");
					writeToChapterWriter("</p>");
					thisLineCharacterCount = 0;
				}
			}
		}

		fWriter.write(newLine);
		writeToChapterWriter(newLine);
		if (postTag != null) {
			fWriter.write(postTag);
			fWriter.write(newLine);

			writeToChapterWriter(postTag);
			writeToChapterWriter(newLine);

			thisLineCharacterCount = 0;
		}

		if (htmlIsClosed) {
			htmlLine = null;
		}
		if (isChapter) {
			lastLineWasChapter = true;
		}
	}

	private String cleanPlainText(String st) {
		// String st2 = st.replaceAll("\x", ""\"x");
		if (StringUtils.isEmpty(st))
			st = "&nbsp;";
		return st.trim();
	}

	private boolean checkHTMLCenterLine(FormatDao formatDao, String st) {
		if (st.startsWith("<center>") && st.endsWith("</center>"))
			return true;
		return false;
	}

	private void writeToSectionWriter(final String textToWrite, final String sectionDivider) throws IOException {
		if (sectionWriter == null) {
			return;
		}
		sectionWriter.write("\n<p class=\"plain\">&nbsp;</p>\n\n");
		sectionWriter.write("<hr/>\n");

		sectionWriter.write(createPreTag(sectionDivider));
		sectionWriter.write(textToWrite);
		sectionWriter.write(createPostTag(sectionDivider));
		sectionWriter.write("\n");

		sectionWriter.write("<hr/>\n");
		sectionWriter.write("<p class=\"plain\">&nbsp;</p>\n");
	}

	private void writeToChapterWriter(String textToWrite) throws IOException {
		if (chapterWriter == null) {
			return;
		}
		chapterWriter.write(textToWrite);
	}

	private void closeChapterWriter(final Integer chapterCount) throws IOException {
		if (chapterWriter == null) {
			return;
		}
		System.out.println("closeChapterWriter");
		chapterWriter.flush();
		outputFooter(chapterWriter);
		chapterWriter.flush();
		chapterWriter.close();
	}

	private void openNewChapterWriter(final Integer count) throws IOException {
		if (outputChapterDir == null) {
			return;
		}
		System.out.println("openNewChapterWriter: count: " + count);
		chapterWriter = new FileWriter(new File(outputChapterDir, "chapter" + count + ".html"));
		outputHeader(chapterWriter);
		chapterWriter.flush();
	}

	private void openNewSectionWriter(final Integer count, final Integer chapterCounter) throws IOException {
		if (outputSectionDir == null) {
			return;
		}
		System.out.println("openNewSectionWriter: count: " + count);
		sectionWriter = new FileWriter(
				new File(outputSectionDir, "chapter" + chapterCounter + " _section" + count + ".html"));
		outputHeader(sectionWriter);
		sectionWriter.flush();
	}

	private void closeSectionWriter(final Integer sectionCount, final Integer chapterCounter) throws IOException {
		if (sectionWriter == null) {
			return;
		}
		System.out.println("closeSectionWriter");
		sectionWriter.flush();
		outputFooter(sectionWriter);
		sectionWriter.flush();
		sectionWriter.close();
	}

	void doDropCaps(final String st, final FileWriter fWriter) throws IOException {
		// <span class="dropcaps">I</span>
		System.out.println("st = " + st);
		final String str1 = st.substring(0, 1);
		System.out.println("str1 = '" + str1 + "'");
		final String str2 = st.substring(1);
		System.out.println("str2 = '" + str2 + "'");
		fWriter.write("<span class=\"dropcaps\">");
		fWriter.write(str1);
		fWriter.write("</span>");
		fWriter.write(str2);
	}

	List<String> doDropCaps(final String st) throws IOException {
		final List<String> slist = new ArrayList<String>();
		// <span class="dropcaps">I</span>
		System.out.println("st = " + st);
		final String str1 = st.substring(0, 1);
		System.out.println("str1 = '" + str1 + "'");
		final String str2 = st.substring(1);
		System.out.println("str2 = '" + str2 + "'");

		slist.add("<span class=\"dropcaps\">");
		slist.add(str1);
		slist.add("</span>");
		slist.add(str2);
		return slist;
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

	private boolean outputFooter(final FileWriter fWriter) {
		System.out.println("Outputting file: footer.html");
		return outputFromFile(fWriter, "/footer.html");
	}

	private boolean outputHeader(final FileWriter fWriter) {
		System.out.println("Outputting file: header.html");
		return outputFromFile(fWriter, formatMode.getHeaderFile());// "/header.html");
	}

	private boolean outputFromFile(final FileWriter fWriter, final String cpFilename) {
		try {
			final InputStream inputStream = this.getClass().getResourceAsStream(cpFilename);
			final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = filterLine(sCurrentLine);
				fWriter.write(sCurrentLine);
				fWriter.write(newLine);
			}
			fWriter.flush();
			br.close();
			inputStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
