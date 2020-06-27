package com.echomap.kqf.looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.format.FormatMode;
import com.echomap.kqf.data.format.MobiMode;
import com.echomap.kqf.data.format.SigilMode;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.looper.data.SimpleSectionDao;

/**
 * 
 * @author mkatz
 */
public class FileLooperHandlerFormatter implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerFormatter.class);
	public static final String WORKTYPE = "Formatter";

	Writer fWriterHTML = null;
	Writer fWriterPlain = null;

	FormatMode formatMode = null;
	final Map<String, String> filterMap = new HashMap<String, String>();

	Writer chapterWriter = null;
	Writer sectionWriter = null;
	Writer chapterWriterPlain = null;

	Integer chapterCounter = 0;
	Integer sectionCounter = 0;

	File outputChapterDir = null;
	File outputChapterDirText = null;
	File outputSectionDir = null;

	String formatOutputNumber = "%03d"; // formatDao.getOutputFormatDigits();

	public FileLooperHandlerFormatter() {

	}

	@Override
	public String getWorkType() {
		return WORKTYPE;
	}

	@Override
	public void looperMsgWarn(final String errorMsg) {
		LOGGER.warn(errorMsg);
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		// if (ldao.getCurrentSection() != null &&
		// ldao.getCurrentSection().isSection) {
		// writeChapterData(formatDao, ldao, null);
		// writeSectionData(formatDao, ldao);
		// }
		// final SimpleChapterDao chpt = ldao.getCurrentChapter();
		// final CountDao cdao = ldao.getChaptCount();
		// if (chpt.isChapter) {
		// writeChapterData(formatDao, ldao, cdao);
		// levelledCount = 0;
		// }
		// cdao.addOneToNumLines();
	}

	// @Override
	// public void handleLine(final FormatDao formatDao, final LooperDao ldao)
	// throws IOException {
	// LOGGER.info("handleLine-->");
	//
	// LOGGER.info("Formatter...>");
	// LOGGER.info(formatDao);
	// LOGGER.info("\n" + formatDao.prettyPrint());
	// LOGGER.info("..>");
	//
	// final int numDigits = formatDao.getOutputFormatDigits();
	// if (numDigits != 3 && numDigits > 0)
	// formatOutputNumber = "%0" + numDigits + "d";
	//
	// final SimpleChapterDao chpt = TextBiz.isChapter(ldao.getCurrentLine(),
	// formatDao.getRegexpChapter());
	//
	// final CountDao cdao = ldao.getChaptCount();
	// final CountDao tdao = ldao.getTotalCount();
	// if (chpt.isChapter) {
	// if (cdao.getChapterName() != null && cdao.getChapterName().length() > 0)
	// {
	// tdao.addNumWords(cdao.getNumWords());
	// ldao.getChapters().add(new ChapterDao(cdao));
	// cdao.clear();
	// tdao.addChapterCount(1);
	// }
	// cdao.setChapterName(chpt.name);
	// cdao.setChapterTitle(chpt.title);
	// String cn = Integer.toString(tdao.getCounter());
	// cdao.setChapterNumber(cn);// tdao.getNumChapters());
	// // formatLine(st, cdao, formatDao);
	// } else {
	// // TextBiz.isSection(line, miscDiv, isInSpecial);
	// // formatLine(st, cdao, formatDao);
	// }
	//
	// // XXXX formatLine(formatDao, ldao, cdao, chpt);
	// // parseLine(st, fWriter, chapterDivider, storyTitle1,
	// // storyTitle2);
	// cdao.addOneToNumLines();
	// }

	@Override
	public void handleMetaDocTag(FormatDao formatDao, LooperDao ldao, DocTag metaDocTag) {
		//
	}

	@Override
	public void handleSection(FormatDao formatDao, LooperDao ldao) {
		//
	}

	@Override
	public void handleChapter(FormatDao formatDao, LooperDao ldao) {
		//
	}

	@Override
	public void handleDocTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		// LOGGER.debug("handleDocTag: xx ");
		final SimpleChapterDao chpt = TextBiz.isChapter(ldao.getCurrentLine(), formatDao.getRegexpChapter());
		final CountDao cdao = ldao.getChaptCount();
		final DocTagLine docTagLine = ldao.getLineDocTagLine();
		if (!docTagLine.isLongDocTag() && !docTagLine.isOnlyDoctag())
			formatLine(formatDao, ldao, cdao, chpt);
	}

	@Override
	public void handleDocTagNotTag(FormatDao formatDao, LooperDao ldao) throws IOException {
		final SimpleChapterDao chpt = TextBiz.isChapter(ldao.getCurrentLine(), formatDao.getRegexpChapter());
		final CountDao cdao = ldao.getChaptCount();
		formatLine(formatDao, ldao, cdao, chpt);
	}

	// @Override
	// public void handleDocTagMaybeTag(FormatDao formatDao, LooperDao ldao)
	// throws IOException {
	//
	// }

	@Override
	public void preLine(final FormatDao formatDao, final LooperDao ldao) {
		ldao.preReadLine();
	}

	@Override
	public void postLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		ldao.postReadLine();
	}

	@Override
	public Object postHandlerPackage(FormatDao formatDao, LooperDao ldao) {
		return null;
	}

	@Override
	public String postHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		closeSectionWriter(sectionCounter, chapterCounter, formatDao);
		closeChapterWriter(chapterCounter, formatDao);

		final StringBuilder sb = new StringBuilder();
		sb.append("Chapters: ");
		sb.append(chapterCounter);
		sb.append("  Sections: ");
		sb.append(sectionCounter);

		if (fWriterHTML != null) {
			outputFooter(fWriterHTML, formatDao);
			fWriterHTML.flush();
		}
		if (fWriterHTML != null) {
			fWriterHTML.flush();
			fWriterHTML.close();
		}
		if (fWriterPlain != null) {
			fWriterPlain.flush();
			fWriterPlain.close();
		}
		if (chapterWriterPlain != null) {
			chapterWriterPlain.flush();
			chapterWriterPlain.close();
		}
		return sb.toString();
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.debug("preHandler-->");

		final int numDigits = formatDao.getOutputFormatDigits();
		if (numDigits != 3 && numDigits > 0)
			formatOutputNumber = "%0" + numDigits + "d";

		if (formatDao.getFormatMode() == null) {
			formatMode = new MobiMode();
		} else if (formatDao.getFormatMode().toLowerCase().compareTo("sigil") == 0) {
			formatMode = new SigilMode();
		}

		ldao.clear();
		// lineTracker.setFormatMode(formatMode);

		// final File inputFile = new File(formatDao.getInputFilename());
		final File outputFileHTML = new File(formatDao.getOutputFilename());

		filterMap.put("@TITLE@", formatDao.getStoryTitle1());
		filterMap.put("@SUBTITLE@", formatDao.getStoryTitle1());
		filterMap.put("@ENCODING@", formatDao.getOutputEncoding());

		// String charsetName = FormatDao.DEFAULToutputEncoding;
		// if (formatDao.getOutputEncoding() != null &&
		// formatDao.getOutputEncoding().length() > 0)
		// charsetName = formatDao.getOutputEncoding();

		// Change encoding?
		Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

		// Create dirs
		final File outputDirHTML = outputFileHTML.getParentFile();
		if (outputDirHTML != null) {
			outputDirHTML.getParentFile().mkdirs();
			outputDirHTML.mkdirs();
		}

		if (formatDao.getWriteChapters() != null && !StringUtils.isBlank(formatDao.getWriteChapters())) {
			outputChapterDir = new File(formatDao.getWriteChapters());
			outputChapterDir.mkdirs();

			// lineTracker.setOutputChapterDir(outputChapterDir);

			outputSectionDir = new File(formatDao.getWriteChapters());
			outputSectionDir.mkdirs();
			// lineTracker.setOutputSectionDir(outputSectionDir);
		}
		if (formatDao.getWantTextChptOutput()) {
			outputChapterDirText = new File(formatDao.getWriteChaptersText());
			outputChapterDirText.mkdirs();
		}

		if (outputDirHTML != null) {
			LOGGER.info("Writing html text file to: " + outputFileHTML);
			// fWriter = new FileWriter(outputFile, false);
			fWriterHTML = new OutputStreamWriter(new FileOutputStream(outputFileHTML), selCharSet);
		}
		// Why am I renaming the extension to TXT, was it a problem once?
		if (outputDirHTML != null) {
			String outFilename = null;
			final String filenameOnly = outputFileHTML.getName();
			final int extIdx = filenameOnly.lastIndexOf(".");
			String ext = "";
			if (extIdx >= 1) {
				ext = filenameOnly.substring(extIdx + 1);
				outFilename = filenameOnly.replaceAll(ext, "txt");
			} else {
				outFilename = filenameOnly + ".txt";
			}
			final File outputFilePlain = new File(outputDirHTML, outFilename);
			LOGGER.info("Writing plain text file to: " + outputFilePlain);
			// fWriterPlain = new FileWriter(outputFilePlain, false);
			fWriterPlain = new OutputStreamWriter(new FileOutputStream(outputFilePlain), selCharSet);
		}

		ldao.InitializeCount();

		if (fWriterHTML != null) {
			outputHeader(fWriterHTML, formatDao);
			fWriterHTML.flush();
		}
	}

	@Override
	public void postLastLine(final FormatDao formatDao, final LooperDao ldao) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

	private void formatLine(final FormatDao formatDao, final LooperDao ldao, final CountDao cdao,
			final SimpleChapterDao chpt) throws IOException {
		ldao.inReadLine();
		String preTag = "";
		String postTag = null;

		final DocTagLine docTagLine = ldao.getLineDocTagLine();
		String st = docTagLine.getBareLine();
		if (st == null)
			return;
		// String st = ldao.getCurrentLine();

		// boolean htmlIsClosed = false;
		// if (ldao.getHtmlLine() == null) {
		if (TextBiz.isAnHtmlLine(st))
			ldao.setHtmlLine(true);
		else
			ldao.setHtmlLine(false);
		// } else {
		// final String htmlLineNew = TextBiz.isAnHtmlLine(st);
		// if (htmlLineNew != null &&
		// htmlLineNew.equalsIgnoreCase(ldao.getHtmlLine())) {
		// htmlIsClosed = true;
		// }
		// }

		boolean isChapter = chpt.isChapter;

		SimpleSectionDao sectionType = null;
		if (isChapter) {
			closeChapterWriter(chapterCounter, formatDao);
			closeChapterWriterPlain(chapterCounter);
			chapterCounter++;
			openNewChapterWriter(chapterCounter, formatDao);
			openNewChapterWriterPlain(chapterCounter, formatDao);
			if (chapterCounter > 1)
				preTag = TextBiz.createPreTag(formatDao.getChapterHeaderTag());
			// preTag = formatMode.getChapterPreTag();// "<mbp:pagebreak/>\n" +
			// "<p
			// class=\"MsoChapter\">";
			else
				preTag = TextBiz.createPreTag(formatDao.getChapterHeaderTag());
			// preTag = formatMode.getFirstChapterPreTag();// "<p
			// class=\"MsoChapter\">";
			postTag = "<p class=\"" + formatMode.getPlainTextTag() + "\">&nbsp;</p>";
		} else if (ldao.getHtmlLine()) {
			preTag = " ";
			postTag = "\n";
		} else {
			sectionType = TextBiz.isSection(st, formatDao.getRegexpSection());
			if (sectionType != null && sectionType.isSection) {
				LOGGER.debug("sectionType: " + sectionType + " for line: " + st);
				// if (sectionType != null) {
				sectionCounter++;
				// if (sectionType.isSection) {
				// yyyy preTag = "<mbp:pagebreak/>\n" + "<p
				// class=\"MsoSection\">";
				preTag = "<p class=\"MsoSection\">";
				// postTag = "</p>";
				// }
				// switch (sectionType) {
				// case PLAIN:
				// preTag = "<mbp:pagebreak/>\n" + "<p class=\"MsoSection\">";
				// break;
				// case INSPECIAL:
				// preTag = "<p class=MsoSection>";
				// break;
				// case NOTSPECIAL:
				// preTag = "<mbp:pagebreak/>\n" + "<p class=\"" +
				// formatMode.getPlainTextTag() + "\">";
				// break;
				// default:
				// break;
				// }
			}
		}
		// DOCTAGTYPE docTagType = null;
		final DocTagLine docTagType = TextBiz.isDocTag(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());

		if (st.compareTo(formatDao.getStoryTitle1()) == 0 || (!StringUtils.isEmpty(formatDao.getStoryTitle2())
				&& st.compareTo(formatDao.getStoryTitle2()) == 0)) {
			preTag = "<p class=\"MsoTitle\">";
		}

		st = filterLine(st);

		if (st.startsWith(formatDao.getDocTagStart())) {
			ldao.setInLongDocTag(true);
		}

		if (st.startsWith(TextBiz.special1)) {
			if (ldao.isInSpecial()) {
				LOGGER.debug("inSpecial flipped to false on: " + st);
				ldao.setInSpecial(false);
			} else {
				ldao.setInSpecial(true);
				LOGGER.debug("inSpecial flipped to true on: " + st);
			}
		}
		boolean centerThisLine = TextBiz.centerCheck(formatDao, st);
		if (centerThisLine) {
			preTag = "<p class=\"" + formatMode.getPlainCenterTextTag() + "\">";
		}

		if (preTag == null || preTag.length() < 1) {
			preTag = "<p class=\"" + formatMode.getPlainTextTag() + "\">";
		}
		boolean isHtmlCenter = TextBiz.checkHTMLCenterLine(formatDao, st);
		if (isHtmlCenter) {
			preTag = String.format("<p class=\"%s\">", formatMode.getHTMLCenterReplacement());
			int indx1 = st.indexOf(">");
			int indx2 = st.indexOf("<", 7);
			String stL = st.substring(indx1 + 1, indx2);
			st = stL;
		}

		// String docTagText = null;
		// if (docTagType == DOCTAGTYPE.INDOCTAG) {
		// docTagText = TextBiz.parseForDocTags(st, formatDao.getDocTagStart(),
		// formatDao.getDocTagEnd());
		// }

		if (docTagType.isLongDocTag()) {
			// if (docTagType == DOCTAGTYPE.LONGDOCTAG) {

		} else {

			String textToWrite = null;
			String textToWrite2 = null;
			boolean cancelLine = false;

			String textPClean = TextBiz.cleanPlainText(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
			if (isChapter) {
				if (formatDao.getRemoveChptDiv()) {
					// @TODO put this format in formatDao to be picked up from
					// UI or settings
					textToWrite = String.format("%s %s: %s", chpt.name, chpt.chpNum, chpt.title);
				} else {
					textToWrite = TextBiz.cleanText(st, formatDao.getRemoveChptDiv(), null,
							// formatDao.getChapterDivider(),
							formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				}
			} else if (sectionType != null && sectionType.isSection) {
				textToWrite = TextBiz.cleanText(st, formatDao.getRemoveSectDiv(), null, // formatDao.getSectionDivider(),
						sectionType, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
			} else if (ldao.isLastLineWasChapter() && st != null && st.length() > 1 && !TextBiz.lineEmpty(st)
					&& !formatDao.getDropCapChapter()) {
				textToWrite = TextBiz.cleanPlainText(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				ldao.setLastLineWasChapter(false);
			} else if (ldao.isLastLineWasChapter() && st != null && st.length() > 1 && !TextBiz.lineEmpty(st)
					&& (!docTagType.isLongDocTag() && !docTagType.isOnlyDoctag()) && !StringUtils.isBlank(textPClean)) {
				// && (docTagType == DOCTAGTYPE.NONE || docTagType ==
				// DOCTAGTYPE.HASDOCTAG)) {
				// <span class="dropcaps">I</span>
				textToWrite2 = st;
				textToWrite = TextBiz.doDropCaps(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				ldao.setLastLineWasChapter(false);
			} else {
				textToWrite = TextBiz.cleanPlainText(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
			}

			if (docTagType.isHasDocTag() && StringUtils.isBlank(textToWrite)) {
				// if (docTagType != DOCTAGTYPE.NONE &&
				// StringUtils.isBlank(textToWrite)) {
				cancelLine = true;
			}

			// if (docTagType != DOCTAGTYPE.ALLDOCTAG) {
			if (!StringUtils.isBlank(textToWrite))
				ldao.addThisLineCharacterCount(textToWrite.length());
			// lineTracker.setThisLineCharacterCount(
			// (
			// lineTracker.getThisLineCharacterCount()
			// += textToWrite.length()
			// ) ) ;
			if (!cancelLine && !ldao.getHtmlLine()) {
				fWriterHTML.write(preTag);
				fWriterHTML.write(textToWrite);

				if (textToWrite2 == null)
					textToWrite2 = textToWrite;

				if (TextBiz.lineEmpty(textToWrite2)) {
					fWriterPlain.write("");
					writeToChapterWriterPlain("");
				} else {
					// fWriterPlain.write(textToWrite2);
					if (ldao.getOriginalLine().startsWith("\t")) {
						fWriterPlain.write("\t");
						writeToChapterWriterPlain("\t");
					}
					fWriterPlain.write(textPClean);// todo simplify?

					if (sectionType == null || !sectionType.isSection)
						writeToChapterWriterPlain(textPClean);
				}
				fWriterPlain.write(TextBiz.newLine);
				writeToChapterWriterPlain(TextBiz.newLine);
			}

			if (!cancelLine) {
				if (sectionType != null && sectionType.isSection) {
					openNewSectionWriter(sectionCounter, chapterCounter, formatDao);
					writeToSectionWriter(textToWrite, formatDao.getSectionHeaderTag());
					closeSectionWriter(sectionCounter, chapterCounter, formatDao);
				} else {
					writeToChapterWriter(preTag);
					writeToChapterWriter(textToWrite);
				}
			}

			if (ldao.getHtmlLine()) {
				if (!cancelLine) {
					// todo
					fWriterHTML.write(st);
					// writeToChapterWriter(TextBiz.newLine);
					if (postTag != null) {
						fWriterHTML.write(postTag);
						fWriterHTML.write(TextBiz.newLine);

						writeToChapterWriter(postTag);
						writeToChapterWriter(TextBiz.newLine);

						ldao.setThisLineCharacterCount(0);
					}
				}
			} else {
				if (!cancelLine) {
					// if (docTagType != DOCTAGTYPE.ALLDOCTAG) {
					if (sectionType != null && sectionType.isSection) {
						// Since we are in section do not output endings yyyy
						fWriterHTML.write("</p>");
					} else if (isChapter) {
						final String stTEnd = TextBiz.createPostTag(formatDao.getChapterHeaderTag());
						fWriterHTML.write(stTEnd);
						writeToChapterWriter(stTEnd);
						// writeToChapterWriterPlain(stTEnd);
					} else {
						if (!StringUtils.isBlank(textToWrite)) {
							if (ldao.getThisLineCharacterCount() == 0) {
								fWriterHTML.write("&nbsp;");
								writeToChapterWriter("&nbsp;");// yyyy
							}
							fWriterHTML.write("</p>");
							writeToChapterWriter("</p>");
							// writeToChapterWriterPlain("</p>");
							ldao.setThisLineCharacterCount(0);
						} else {
							if (ldao.getThisLineCharacterCount() == 0) {
								fWriterHTML.write("&nbsp;");
								writeToChapterWriter("&nbsp;");// yyyy
							}
							fWriterHTML.write("</p>");
							writeToChapterWriter("</p>");
							// writeToChapterWriterPlain("</p>");
							ldao.setThisLineCharacterCount(0);
						}
					}

					fWriterHTML.write(TextBiz.newLine);
					writeToChapterWriter(TextBiz.newLine);
					// writeToChapterWriterPlain(TextBiz.newLine);
					if (postTag != null) {
						fWriterHTML.write(postTag);
						fWriterHTML.write(TextBiz.newLine);

						writeToChapterWriter(postTag);
						writeToChapterWriter(TextBiz.newLine);
						// writeToChapterWriterPlain(postTag);
						// writeToChapterWriterPlain(TextBiz.newLine);

						ldao.setThisLineCharacterCount(0);
					}
				}
			}
			if (chapterWriter != null) {
				// TODO REMOVE?
				chapterWriter.flush();
				fWriterHTML.flush();
			}
			ldao.setCurrentLine(textToWrite);
		} // LONGDOCTAG

		// if (htmlIsClosed) {
		// ldao.setHtmlLine(null);
		// }
		if (isChapter)

		{
			ldao.setLastLineWasChapter(true);
		}
		if (ldao.isInLongDocTag() && st.contains(formatDao.getDocTagEnd())) {
			ldao.setInLongDocTag(false);
		}
	}

	private void writeToChapterWriterPlain(String textToWrite) throws IOException {
		if (chapterWriterPlain == null) {
			return;
		}
		chapterWriterPlain.write(textToWrite);
	}

	private void openNewChapterWriterPlain(Integer chapterCounter2, final FormatDao formatDao) throws IOException {
		if (!formatDao.getWantTextChptOutput()) {
			return;
		}

		if (outputChapterDirText == null) {
			return;
		}
		LOGGER.debug("openNewChapterWriterPlain: count: " + chapterCounter2);

		// Change encoding?
		Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

		final File chapterWriterPlainFile = new File(outputChapterDirText,
				"chapter" + String.format(formatOutputNumber, chapterCounter2) + ".txt");
		// chapterWriterPlain = new FileWriter(chapterWriterPlainFile);
		chapterWriterPlain = new OutputStreamWriter(new FileOutputStream(chapterWriterPlainFile), selCharSet);

		// outputHeader(chapterWriterPlain);
		chapterWriterPlain.flush();
	}

	private void closeChapterWriterPlain(Integer chapterCounter2) throws IOException {
		if (chapterWriterPlain == null) {
			return;
		}

		// formatDao.getVersion()

		LOGGER.debug("closeChapterWriterPlain");
		chapterWriterPlain.flush();
		// outputFooter(chapterWriter);
		chapterWriterPlain.flush();
		chapterWriterPlain.close();
		chapterWriterPlain = null;
	}

	private void openNewChapterWriter(final Integer chapterCounter2, final FormatDao formatDao) throws IOException {
		if (outputChapterDir == null) {
			return;
		}
		LOGGER.debug("openNewChapterWriter: count: " + chapterCounter2);

		// Encoding
		Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

		final File chapterWriterFile = new File(outputChapterDir,
				"chapter" + String.format(formatOutputNumber, chapterCounter2) + ".html");
		chapterWriter = new OutputStreamWriter(new FileOutputStream(chapterWriterFile), selCharSet);
		// chapterWriter = new FileWriter(chapterWriterFile);
		outputHeader(chapterWriter, formatDao);
		chapterWriter.flush();
	}

	private void openNewSectionWriter(final Integer count, final Integer chapterCounter, final FormatDao formatDao)
			throws IOException {
		if (outputSectionDir == null) {
			return;
		}
		LOGGER.debug("openNewSectionWriter: count: " + count);

		// Change encoding?
		Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

		final File sectionWriterFile = new File(outputSectionDir,
				"chapter" + String.format(formatOutputNumber, chapterCounter) + "_section" + count + ".html");
		sectionWriter = new OutputStreamWriter(new FileOutputStream(sectionWriterFile), selCharSet);
		// sectionWriter = new FileWriter(sectionWriterFile);
		outputHeader(sectionWriter, formatDao);
		sectionWriter.flush();
	}

	private void closeSectionWriter(final Integer sectionCount, final Integer chapterCounter, final FormatDao formatDao)
			throws IOException {
		if (sectionWriter == null) {
			return;
		}
		// formatDao.getVersion()
		LOGGER.debug("closeSectionWriter");
		sectionWriter.flush();
		outputFooter(sectionWriter, formatDao);
		sectionWriter.flush();
		sectionWriter.close();
		sectionWriter = null;
	}

	private void closeChapterWriter(final Integer chapterCount, final FormatDao formatDao) throws IOException {
		if (chapterWriter == null) {
			return;
		}
		// formatDao.getVersion()
		LOGGER.debug("closeChapterWriter");
		chapterWriter.flush();
		outputFooter(chapterWriter, formatDao);
		chapterWriter.flush();
		chapterWriter.close();
		chapterWriter = null;
	}

	private void writeToSectionWriter(final String textToWrite, final String sectionDivider) throws IOException {
		if (sectionWriter == null) {
			return;
		}
		sectionWriter.write("\n<p class=\"plain\">&nbsp;</p>\n\n");
		sectionWriter.write("<hr/>\n");

		sectionWriter.write(TextBiz.createPreTag(sectionDivider));
		sectionWriter.write(textToWrite);
		sectionWriter.write(TextBiz.createPostTag(sectionDivider));
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

	private boolean outputHeader(final Writer fWriter, final FormatDao formatDao) {
		LOGGER.debug("Outputting file: header.html");
		if (formatMode == null)
			throw new NullPointerException("No format mode specified.");
		if (formatMode.getHeaderFile() == null)
			throw new NullPointerException("No format mode specified with a good header file");
		return outputFromFile(fWriter, formatMode.getHeaderFile(), formatDao);// "/header.html");
	}

	private boolean outputFooter(final Writer fWriter, final FormatDao formatDao) {
		LOGGER.debug("Outputting file: footer.html");
		return outputFromFile(fWriter, "/footer.html", formatDao);
	}

	private boolean outputFromFile(final Writer fWriter, final String cpFilename, final FormatDao formatDao) {
		try {
			Charset selCharSet = formatDao.getCharSet();
			LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

			final InputStream inputStream = this.getClass().getResourceAsStream(cpFilename);
			final InputStreamReader isr = new InputStreamReader(inputStream, selCharSet);
			final BufferedReader br = new BufferedReader(isr);

			// final InputStream inputStream =
			// this.getClass().getResourceAsStream(cpFilename);
			// final BufferedReader br = new BufferedReader(new
			// InputStreamReader(inputStream));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = filterLine(sCurrentLine);
				fWriter.write(sCurrentLine);
				fWriter.write(TextBiz.newLine);
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
}
