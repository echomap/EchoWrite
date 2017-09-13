package com.echomap.kqf.looper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;

public class FileLooperHandlerCount implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerCount.class);
	final static Properties props = new Properties();

	public FileLooperHandlerCount() {
		try {
			props.load(FileLooperHandlerCount.class.getClassLoader().getResourceAsStream("cwc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			props.setProperty("version", "0.0.0");
		}
		LOGGER.info("Version: " + props.getProperty("version"));
	}

	@Override
	public void preLine(FormatDao formatDao, LooperDao ldao, String st) {
		// LOGGER.info("preLine-->");
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao, final String st) throws IOException {
		LOGGER.info("handleLine-->");

		final SimpleChapterDao chpt = TextBiz.isChapter(st, formatDao.getChapterDivider());

		final CountDao cdao = ldao.getChaptCount();
		final CountDao tdao = ldao.getTotalCount();
		if (chpt.isChapter) {
			if (cdao.getChapterName() != null && cdao.getChapterName().length() > 0) {
				// System.out.println(cdao.getChapterName() + "\t\t"
				// + cdao.getNumWords() + "\t" + chpt.title);
				tdao.addNumWords(cdao.getNumWords());
				// totalWordCount += cdao.getNumWords();
				// tdao.copy(cdao);
				ldao.getChapters().add(new ChapterDao(cdao));
				// tdao = new CountDao();
				cdao.clear();
				tdao.addChapterCount(1);
				// chapterCount++;
			}
			cdao.setChapterName(chpt.name);
			cdao.setChapterTitle(chpt.title);
			// cdao.setChapterNumber(chapterCount);
			cdao.setChapterNumber(tdao.getNumChapters());

		} else {
			TextBiz.countWords(st, cdao);
		}

		// parseLine(st, fWriter, chapterDivider, storyTitle1,
		// storyTitle2);
		cdao.addOneToNumLines();
	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao, String st) {
		// LOGGER.info("postLine-->");
	}

	@Override
	public void postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		LOGGER.info("postHandler-->");
		// outputSummaryOutputFile(summaryOut, outputDir, chapters);
		Integer totalWords = 0;
		for (ChapterDao cdao : ldao.getChapters()) {
			LOGGER.debug("cdao = " + cdao);
			totalWords += cdao.getNumWords();
		}
		LOGGER.debug("TotalWords: " + totalWords.toString());
		outputSummaryOutputFile(formatDao.getOutputCountFile(), null, ldao.getChapters());
		LOGGER.debug("Version:, " + props.getProperty("version"));
	}

	@Override
	public void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		LOGGER.info("preHandler-->");
		// boolean noseperate = true;

		// final String outputFileName = formatDao.getOutputFilename();
		// final File outputDir = new File(outputFileName).getParentFile();
		// File outputFile = null;
		ldao.InitializeCount();

		// final List<CountDao> chapters = new ArrayList<CountDao>();
		// CountDao tdao = new CountDao();
		// final CountDao cdao = new CountDao();

	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao, String st) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

	private void outputSummaryOutputFile(final String summaryOut, final File outputDir, final List<ChapterDao> chapters)
			throws IOException {
		FileWriter fWriter = null;
		try {
			Integer totalWords = 0;
			File summaryOutputFile = null;
			if (null != summaryOut && summaryOut.length() > 0) {
				summaryOutputFile = new File(summaryOut);
			} else {
				summaryOutputFile = new File(outputDir, "Chapter Count.csv");
			}
			LOGGER.info("Writing summary data to " + summaryOutputFile);
			fWriter = new FileWriter(summaryOutputFile, false);
			fWriter.write("Chapter,Words,Title,Chars,Lines");
			fWriter.write(TextBiz.newLine);
			for (ChapterDao countDao : chapters) {
				fWriter.write(countDao.getChapterName() + "," + countDao.getNumWords() + ","
						+ countDao.getChapterTitle() + "," + countDao.getNumChars() + "," + countDao.getNumLines());
				fWriter.write(TextBiz.newLine);
				totalWords += countDao.getNumWords();
			}
			fWriter.write(TextBiz.newLine);
			fWriter.write(TextBiz.newLine);
			fWriter.write(TextBiz.newLine);
			fWriter.write("TotalWords:,");
			fWriter.write(totalWords.toString());
			fWriter.write(TextBiz.newLine);
			fWriter.write("Version:, " + props.getProperty("version"));
		} finally {
			if (fWriter != null) {
				fWriter.flush();
				fWriter.close();
			}
		}
		LOGGER.debug("Chapter\t\t\tWords\tTitle");
		for (ChapterDao countDao : chapters) {
			LOGGER.debug(
					countDao.getChapterName() + "\t\t" + countDao.getNumWords() + "\t" + countDao.getChapterTitle());
		}

		// System.out.println(cdao.getChapterName() + "\t\t"
		// + cdao.getNumWords() + "\t" + chpt.title);
	}
}
