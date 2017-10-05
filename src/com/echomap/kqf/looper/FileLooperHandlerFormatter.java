package com.echomap.kqf.looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.FormatMode;
import com.echomap.kqf.data.MobiMode;
import com.echomap.kqf.data.SigilMode;
import com.echomap.kqf.looper.TextBiz.SECTIONTYPE;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;

public class FileLooperHandlerFormatter implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerFormatter.class);
	final static Properties props = new Properties();

	FileWriter fWriter = null;
	FileWriter fWriterPlain = null;

	FormatMode formatMode = null;
	final Map<String, String> filterMap = new HashMap<String, String>();

	FileWriter chapterWriter = null;
	FileWriter sectionWriter = null;
	FileWriter chapterWriterPlain = null;

	Integer chapterCounter = 0;
	Integer sectionCounter = 0;

	File outputChapterDir = null;
	File outputChapterDirText = null;
	File outputSectionDir = null;

	String formatOutputNumber = "%03d"; // TODO Param

	public FileLooperHandlerFormatter() {
		try {
			final InputStream asdf = FileLooperHandlerFormatter.class.getClassLoader()
					.getResourceAsStream("fmt.properties");
			if (asdf != null)
				props.load(asdf);
		} catch (IOException e) {
			e.printStackTrace();
			props.setProperty("version", "0.0.0");
		}
		LOGGER.info("Version: " + props.getProperty("version"));
	}

	@Override
	public String getWorkType() {
		return "Formatter";
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.info("handleLine-->");

		LOGGER.info("Formatter...>");
		LOGGER.info(formatDao);
		LOGGER.info("\n" + formatDao.prettyPrint());
		LOGGER.info("..>");

		final int numDigits = formatDao.getOutputFormatDigits();
		if (numDigits != 3 && numDigits > 0)
			formatOutputNumber = "%0" + numDigits + "d";

		final SimpleChapterDao chpt = TextBiz.isChapter(ldao.getCurrentLine(), formatDao.getChapterDivider());

		final CountDao cdao = ldao.getChaptCount();
		final CountDao tdao = ldao.getTotalCount();
		if (chpt.isChapter) {
			if (cdao.getChapterName() != null && cdao.getChapterName().length() > 0) {
				tdao.addNumWords(cdao.getNumWords());
				ldao.getChapters().add(new ChapterDao(cdao));
				cdao.clear();
				tdao.addChapterCount(1);
			}
			cdao.setChapterName(chpt.name);
			cdao.setChapterTitle(chpt.title);
			cdao.setChapterNumber(tdao.getNumChapters());
			// formatLine(st, cdao, formatDao);
		} else {
			// formatLine(st, cdao, formatDao);
		}
		formatLine(formatDao, ldao, cdao, chpt);
		// parseLine(st, fWriter, chapterDivider, storyTitle1,
		// storyTitle2);
		cdao.addOneToNumLines();
	}

	private void formatLine(final FormatDao formatDao, final LooperDao ldao, final CountDao cdao,
			final SimpleChapterDao chpt) throws IOException {
		ldao.inReadLine();
		String preTag = "";
		String postTag = null;
		String st = ldao.getCurrentLine();

		boolean htmlIsClosed = false;
		if (ldao.getHtmlLine() == null) {
			ldao.setHtmlLine(TextBiz.isAnHtmlLine(st));
		} else {
			final String htmlLineNew = TextBiz.isAnHtmlLine(st);
			if (htmlLineNew != null && htmlLineNew.equalsIgnoreCase(ldao.getHtmlLine())) {
				htmlIsClosed = true;
			}
		}

		boolean isChapter = chpt.isChapter;

		SECTIONTYPE sectionType = null;
		if (isChapter) {
			closeChapterWriter(chapterCounter);
			closeChapterWriterPlain(chapterCounter);
			chapterCounter++;
			openNewChapterWriter(chapterCounter);
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
		} else if (ldao.getHtmlLine() != null) {
			preTag = " ";
		} else {
			sectionType = TextBiz.isSection(st, formatDao.getSectionDivider(), false);// TODO
			if (sectionType != null)
				LOGGER.debug("sectionType: " + sectionType + " for line: " + st);
			if (sectionType != null) {
				sectionCounter++;
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
				textToWrite = TextBiz.cleanText(st, formatDao.getRemoveChptDiv(), formatDao.getChapterDivider(),
						formatDao.getDocTagStart(), formatDao.getDocTagEnd());
			} else if (sectionType != null) {
				textToWrite = TextBiz.cleanText(st, formatDao.getRemoveSectDiv(), formatDao.getSectionDivider(),
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
			if (!cancelLine) {
				fWriter.write(preTag);
				fWriter.write(textToWrite);

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
					writeToChapterWriterPlain(textPClean);
				}
				fWriterPlain.write(TextBiz.newLine);
				writeToChapterWriterPlain(TextBiz.newLine);
			}

			if (!cancelLine) {
				if (sectionType != null) {
					openNewSectionWriter(sectionCounter, chapterCounter);
					writeToSectionWriter(textToWrite, formatDao.getSectionHeaderTag());
					closeSectionWriter(sectionCounter, chapterCounter);
				} else {
					writeToChapterWriter(preTag);
					writeToChapterWriter(textToWrite);
				}
			}

			if (ldao.getHtmlLine() == null) {
				if (!cancelLine) {
					// if (docTagType != DOCTAGTYPE.ALLDOCTAG) {
					if (isChapter) {
						final String stTEnd = TextBiz.createPostTag(formatDao.getChapterHeaderTag());
						fWriter.write(stTEnd);
						writeToChapterWriter(stTEnd);
						// writeToChapterWriterPlain(stTEnd);
					} else {
						if (!StringUtils.isBlank(textToWrite)) {
							if (ldao.getThisLineCharacterCount() == 0)
								fWriter.write("&nbsp;");
							fWriter.write("</p>");
							writeToChapterWriter("</p>");
							// writeToChapterWriterPlain("</p>");
							ldao.setThisLineCharacterCount(0);
						} else {
							if (ldao.getThisLineCharacterCount() == 0)
								fWriter.write("&nbsp;");
							fWriter.write("</p>");
							writeToChapterWriter("</p>");
							// writeToChapterWriterPlain("</p>");
							ldao.setThisLineCharacterCount(0);
						}
					}

					fWriter.write(TextBiz.newLine);
					writeToChapterWriter(TextBiz.newLine);
					// writeToChapterWriterPlain(TextBiz.newLine);
					if (postTag != null) {
						fWriter.write(postTag);
						fWriter.write(TextBiz.newLine);

						writeToChapterWriter(postTag);
						writeToChapterWriter(TextBiz.newLine);
						// writeToChapterWriterPlain(postTag);
						// writeToChapterWriterPlain(TextBiz.newLine);

						ldao.setThisLineCharacterCount(0);
					}
				}
			}
			ldao.setCurrentLine(textToWrite);
		} // LONGDOCTAG

		if (htmlIsClosed) {
			ldao.setHtmlLine(null);
		}
		if (isChapter) {
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
		chapterWriterPlain = new FileWriter(new File(outputChapterDirText,
				"chapter" + String.format(formatOutputNumber, chapterCounter2) + ".txt"));
		// outputHeader(chapterWriterPlain);
		chapterWriterPlain.flush();
	}

	private void closeChapterWriterPlain(Integer chapterCounter2) throws IOException {
		if (chapterWriterPlain == null) {
			return;
		}
		LOGGER.debug("closeChapterWriterPlain");
		chapterWriterPlain.flush();
		// outputFooter(chapterWriter);
		chapterWriterPlain.flush();
		chapterWriterPlain.close();
		chapterWriterPlain = null;
	}

	private void openNewChapterWriter(final Integer chapterCounter2) throws IOException {
		if (outputChapterDir == null) {
			return;
		}
		LOGGER.debug("openNewChapterWriter: count: " + chapterCounter2);
		chapterWriter = new FileWriter(
				new File(outputChapterDir, "chapter" + String.format(formatOutputNumber, chapterCounter2) + ".html"));
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
		sectionWriter = null;
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

	@Override
	public void preLine(final FormatDao formatDao, final LooperDao ldao) {
		ldao.preReadLine();
	}

	@Override
	public void postLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		ldao.postReadLine();
	}

	@Override
	public void postHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		closeSectionWriter(sectionCounter, chapterCounter);
		closeChapterWriter(chapterCounter);
		if (fWriter != null) {
			outputFooter(fWriter);
			fWriter.flush();
		}
		if (fWriter != null) {
			fWriter.flush();
			fWriter.close();
		}
		if (fWriterPlain != null) {
			fWriterPlain.flush();
			fWriterPlain.close();
		}
		if (chapterWriterPlain != null) {
			chapterWriterPlain.flush();
			chapterWriterPlain.close();
		}
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.debug("preHandler-->");
		if (formatDao.getFormatMode() == null) {
			formatMode = new MobiMode();
		} else if (formatDao.getFormatMode().toLowerCase().compareTo("sigil") == 0) {
			formatMode = new SigilMode();
		}

		ldao.clear();
		// lineTracker.setFormatMode(formatMode);

		// final File inputFile = new File(formatDao.getInputFilename());
		final File outputFile = new File(formatDao.getOutputFilename());

		filterMap.put("@TITLE@", formatDao.getStoryTitle1());
		filterMap.put("@SUBTITLE@", formatDao.getStoryTitle1());
		filterMap.put("@ENCODING@", formatDao.getOutputEncoding());

		// String charsetName = FormatDao.DEFAULToutputEncoding;
		// if (formatDao.getOutputEncoding() != null &&
		// formatDao.getOutputEncoding().length() > 0)
		// charsetName = formatDao.getOutputEncoding();

		// Create dirs
		final File outputDir = outputFile.getParentFile();
		if (outputDir != null) {
			outputDir.getParentFile().mkdirs();
			outputDir.mkdirs();
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

		if (outputDir != null) {
			LOGGER.info("Writing html text file to: " + outputFile);
			fWriter = new FileWriter(outputFile, false);
		}
		if (outputDir != null) {
			String outFilename = null;
			final String filenameOnly = outputFile.getName();
			final int extIdx = filenameOnly.lastIndexOf(".");
			String ext = "";
			if (extIdx >= 1) {
				ext = filenameOnly.substring(extIdx + 1);
				outFilename = filenameOnly.replaceAll(ext, "txt");
			} else {
				outFilename = filenameOnly + ".txt";
			}
			final File outputFile2 = new File(outputDir, outFilename);
			LOGGER.info("Writing plain text file to: " + outputFile2);
			fWriterPlain = new FileWriter(outputFile2, false);
		}

		ldao.InitializeCount();

		if (fWriter != null) {
			outputHeader(fWriter);
			fWriter.flush();
		}
	}

	@Override
	public void postLastLine(final FormatDao formatDao, final LooperDao ldao) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

	private boolean outputHeader(final FileWriter fWriter) {
		LOGGER.debug("Outputting file: header.html");
		return outputFromFile(fWriter, formatMode.getHeaderFile());// "/header.html");
	}

	private boolean outputFooter(final FileWriter fWriter) {
		LOGGER.debug("Outputting file: footer.html");
		return outputFromFile(fWriter, "/footer.html");
	}

	private boolean outputFromFile(final FileWriter fWriter, final String cpFilename) {
		try {
			final InputStream inputStream = this.getClass().getResourceAsStream(cpFilename);
			final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
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
