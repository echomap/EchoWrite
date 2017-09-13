package com.echomap.kqf.two.biz;

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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.LineTracker;
import com.echomap.kqf.data.MobiMode;
import com.echomap.kqf.data.SigilMode;
import com.echomap.kqf.two.data.FormatDao;
import com.echomap.kqf.two.data.FormatMode;

/**
 * Not thread safe
 */
public class FormatBiz extends BaseBiz {
	private final static Logger LOGGER = LogManager.getLogger(FormatBiz.class);
	final LineTracker lineTracker = new LineTracker();

	final static String newLine = System.getProperty("line.separator");
	//
	final static String special1 = "* * * * * * * *";
	final String formatOutputNumber = "%03d";
	private FormatMode formatMode = null;
	//
	// boolean inSpecial = false;
	// boolean inLongDocTag = false;
	// boolean lastLineWasChapter = false;
	// String htmlLine = null;
	// Integer thisLineCharacterCount = 0;
	final Map<String, String> filterMap = new HashMap<String, String>();
	//
	Integer chapterCounter = 0;
	File outputChapterDir = null;
	FileWriter chapterWriter = null;
	//
	Integer sectionCounter = 0;
	File outputSectionDir = null;
	FileWriter sectionWriter = null;

	public void format(final FormatDao formatDao) throws IOException {
		LOGGER.info("Formatter...>");
		LOGGER.info(formatDao);
		LOGGER.info("\n" + formatDao.prettyPrint());
		LOGGER.info("..>");

		// FormatMode formatMode = null;

		if (formatDao.getFormatMode() == null) {
			formatMode = new MobiMode();
		} else if (formatDao.getFormatMode().toLowerCase().compareTo("sigil") == 0) {
			formatMode = new SigilMode();
		}

		lineTracker.clear();
		// lineTracker.setFormatMode(formatMode);

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
				// lineTracker.setOutputChapterDir(outputChapterDir);

				outputSectionDir = new File(formatDao.getWriteChapters());
				outputSectionDir.mkdirs();
				// lineTracker.setOutputSectionDir(outputSectionDir);
			}

			if (outputDir != null)
				fWriter = new FileWriter(outputFile, false);

			if (!inputFile.exists() && inputFile.length() < 0) {
				LOGGER.error("The specified file does not exist");
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
					// inSpecial = false;
					// inLongDocTag = false;
					// htmlLine = null;
					while ((st = reader.readLine()) != null) {
						lineTracker.preReadLine();
						writeLine(st, fWriter, formatDao);
						lineTracker.postReadLine();
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

	enum LINETYPE {
		PLAIN, CHAPTER, SECTION
	};

	enum SECTIONTYPE {
		PLAIN, INSPECIAL, NOTSPECIAL
	};

	enum DOCTAGTYPE {
		NONE, ALLDOCTAG, INDOCTAG, LONGDOCTAG
	};

	String filterLine(String st) {
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
		} else if (line.startsWith(special1) && !lineTracker.isInSpecial()) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoPlainText>";
			return SECTIONTYPE.NOTSPECIAL;
		} else if (line.indexOf("Section") > -1 && lineTracker.isInSpecial()) {
			// preTag = "<p class=MsoSection>";
			return SECTIONTYPE.INSPECIAL;
		}
		// if (miscDiv != null) {
		// if (line.contains(miscDiv))// && line.contains("Section"))
		// return SECTIONTYPE.PLAIN;
		// }
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
		lineTracker.inReadLine();
		String preTag = "";
		String postTag = null;
		// String postTag = "";
		// if (st == null) {
		// return;
		// }
		boolean htmlIsClosed = false;
		if (lineTracker.getHtmlLine() == null) {
			lineTracker.setHtmlLine(isAnHtmlLine(st));
			// htmlLine = isAnHtmlLine(st);
		} else {
			final String htmlLineNew = isAnHtmlLine(st);
			if (htmlLineNew != null && htmlLineNew.equalsIgnoreCase(lineTracker.getHtmlLine())) {
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
		} else if (lineTracker.getHtmlLine() != null) {
			preTag = " ";
		} else {
			sectionType = isSection(st, formatDao.getSectionDivider());
			if (sectionType != null)
				LOGGER.debug("sectionType: " + sectionType + " for line: " + st);
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
		DOCTAGTYPE docTagType = null;
		docTagType = isDocTag(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());

		if (st.compareTo(formatDao.getStoryTitle1()) == 0 || (!StringUtils.isEmpty(formatDao.getStoryTitle2())
				&& st.compareTo(formatDao.getStoryTitle2()) == 0)) {
			preTag = "<p class=\"MsoTitle\">";
		}

		st = filterLine(st);

		if (st.startsWith(formatDao.getDocTagStart())) {
			lineTracker.setInLongDocTag(true);
			// inLongDocTag = true;
		}

		if (st.startsWith(special1)) {
			if (lineTracker.isInSpecial()) {
				LOGGER.debug("inSpecial flipped to false on: " + st);
				// System.out.println("preTag: " + preTag);
				lineTracker.setInSpecial(false);
				// inSpecial = false;
			} else {
				// inSpecial = true;
				lineTracker.setInSpecial(true);
				LOGGER.debug("inSpecial flipped to true on: " + st);
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

		String docTagText = null;
		if (docTagType == DOCTAGTYPE.INDOCTAG) {
			docTagText = parseForDocTags(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		}

		if (docTagType == DOCTAGTYPE.LONGDOCTAG) {

		} else {
			// if (docTagType != DOCTAGTYPE.ALLDOCTAG)
			fWriter.write(preTag);

			String textToWrite = null;

			if (isChapter) {
				textToWrite = cleanText(st, formatDao.getRemoveChptDiv(), formatDao.getChapterDivider(),
						formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				// fWriter.write(cleanText(st, formatDao.getRemoveChptDiv(),
				// formatDao.getChapterDivider()));
			} else if (sectionType != null) {
				textToWrite = cleanText(st, formatDao.getRemoveSectDiv(), formatDao.getSectionDivider(), sectionType,
						formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				// fWriter.write(cleanText(st, formatDao.getRemoveSectDiv(),
				// formatDao.getSectionDivider()));
			} else if (lineTracker.isLastLineWasChapter() && st != null && st.length() > 1 && !lineEmpty(st)
					&& !formatDao.getDropCapChapter()) {
				textToWrite = cleanPlainText(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				lineTracker.setLastLineWasChapter(false);
			} else if (lineTracker.isLastLineWasChapter() && st != null && st.length() > 1 && !lineEmpty(st)
					&& docTagType == DOCTAGTYPE.NONE) {
				// <span class="dropcaps">I</span>
				textToWrite = doDropCaps(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				lineTracker.setLastLineWasChapter(false);
			} else {
				textToWrite = cleanPlainText(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				// fWriter.write(st);
			}
			// if (docTagType != DOCTAGTYPE.ALLDOCTAG) {
			if (!StringUtils.isBlank(textToWrite))
				lineTracker.addThisLineCharacterCount(textToWrite.length());
			// lineTracker.setThisLineCharacterCount(
			// (
			// lineTracker.getThisLineCharacterCount()
			// += textToWrite.length()
			// )
			// )
			// ;
			fWriter.write(textToWrite);

			if (sectionType != null) {
				openNewSectionWriter(sectionCounter, chapterCounter);
				writeToSectionWriter(textToWrite, formatDao.getSectionHeaderTag());
				closeSectionWriter(sectionCounter, chapterCounter);
			} else {
				// writeToChapterWriter(formatDao.get);
				// final String stTSt =
				// "<"+formatDao.getChapterHeaderTag()+">";
				writeToChapterWriter(preTag);
				writeToChapterWriter(textToWrite);
			}
			// }
			if (lineTracker.getHtmlLine() == null) {
				// if (docTagType != DOCTAGTYPE.ALLDOCTAG) {
				if (isChapter) {
					final String stTEnd = createPostTag(formatDao.getChapterHeaderTag());
					fWriter.write(stTEnd);
					writeToChapterWriter(stTEnd);
					// fWriter.write(formatMode.getChapterPostTag());
					// writeToChapterWriter(formatMode.getChapterPostTag());
				} else {
					if (!StringUtils.isBlank(textToWrite)) {
						if (lineTracker.getThisLineCharacterCount() == 0)
							fWriter.write("&nbsp;");
						fWriter.write("</p>");
						writeToChapterWriter("</p>");
						// thisLineCharacterCount = 0;
						lineTracker.setThisLineCharacterCount(0);
					} else {
						if (lineTracker.getThisLineCharacterCount() == 0)
							fWriter.write("&nbsp;");
						fWriter.write("</p>");
						writeToChapterWriter("</p>");
						// thisLineCharacterCount = 0;
						lineTracker.setThisLineCharacterCount(0);
					}
				}

				fWriter.write(newLine);
				writeToChapterWriter(newLine);
				if (postTag != null) {
					fWriter.write(postTag);
					fWriter.write(newLine);

					writeToChapterWriter(postTag);
					writeToChapterWriter(newLine);

					lineTracker.setThisLineCharacterCount(0);
					// thisLineCharacterCount = 0;
				}
			}
			// }

		} // LONGDOCTAG

		if (htmlIsClosed) {
			// htmlLine = null;
			lineTracker.setHtmlLine(null);
		}
		if (isChapter) {
			lineTracker.setLastLineWasChapter(true);
			// lastLineWasChapter = true;
		}
		if (lineTracker.isInLongDocTag() && st.contains(formatDao.getDocTagEnd())) {
			// inLongDocTag = false;
			lineTracker.setInLongDocTag(false);
		}
	}

	private boolean lineEmpty(String st) {
		if (StringUtils.isBlank(st))
			return true;
		if (st.compareTo("&nbsp;") == 0)
			return true;
		return false;
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
		LOGGER.debug("closeChapterWriter");
		chapterWriter.flush();
		outputFooter(chapterWriter);
		chapterWriter.flush();
		chapterWriter.close();
	}

	private void openNewChapterWriter(final Integer chapterCounter) throws IOException {
		if (outputChapterDir == null) {
			return;
		}
		LOGGER.debug("openNewChapterWriter: count: " + chapterCounter);
		chapterWriter = new FileWriter(
				new File(outputChapterDir, "chapter" + String.format(formatOutputNumber, chapterCounter) + ".html"));
		outputHeader(chapterWriter);
		chapterWriter.flush();
	}

	private void openNewSectionWriter(final Integer count, final Integer chapterCounter) throws IOException {
		if (outputSectionDir == null) {
			return;
		}
		LOGGER.debug("openNewSectionWriter: count: " + count);
		sectionWriter = new FileWriter(new File(outputSectionDir,
				"chapter" + String.format(formatOutputNumber, chapterCounter) + "_section" + count + ".html"));
		outputHeader(sectionWriter);
		sectionWriter.flush();
	}

	private void closeSectionWriter(final Integer sectionCount, final Integer chapterCounter) throws IOException {
		if (sectionWriter == null) {
			return;
		}
		LOGGER.debug("closeSectionWriter");
		sectionWriter.flush();
		outputFooter(sectionWriter);
		sectionWriter.flush();
		sectionWriter.close();
	}

	// void doDropCaps(final String st, final FileWriter fWriter) throws
	// IOException {
	// // <span class="dropcaps">I</span>
	// String st2 = cleanPlainText(st, formatDao.getDocTagStart(),
	// formatDao.getDocTagEnd());
	// System.out.println("st = " + st2);
	// final String str1 = st.substring(0, 1);
	// System.out.println("str1 = '" + str1 + "'");
	// final String str2 = st.substring(1);
	// System.out.println("str2 = '" + str2 + "'");
	// fWriter.write("<span class=\"dropcaps\">");
	// fWriter.write(str1);
	// fWriter.write("</span>");
	// fWriter.write(str2);
	// }
	String doDropCaps(final String st, final String docTagStart, final String docTagEnd) throws IOException {
		String st2 = "";
		List<String> dropCapsList = doDropCapsList(st, docTagStart, docTagEnd);
		for (String dct : dropCapsList) {
			st2 += dct;
		}
		return st2;
	}

	List<String> doDropCapsList(final String st, final String docTagStart, final String docTagEnd) throws IOException {
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

	private boolean outputFooter(final FileWriter fWriter) {
		LOGGER.debug("Outputting file: footer.html");
		return outputFromFile(fWriter, "/footer.html");
	}

	private boolean outputHeader(final FileWriter fWriter) {
		LOGGER.debug("Outputting file: header.html");
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
