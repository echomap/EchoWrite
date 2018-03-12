package com.echomap.kqf.looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;

/**
 * 
 * @author mkatz
 */
public class FileLooperHandlerCount implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerCount.class);

	// String workResult = null;

	public FileLooperHandlerCount() {

	}

	@Override
	public String getWorkType() {
		return "Counter";
	}

	// @Override
	// public String getWorkResult() {
	// return workResult;
	// }

	@Override
	public void preLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// LOGGER.info("preLine-->");
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		// final SimpleChapterDao chpt = ldao.getCurrentChapter();
		// final CountDao cdao = ldao.getChaptCount();
		// final CountDao tdao = ldao.getTotalCount();
		// if (chpt.isChapter) {
		// cdao.setChapterNumber(tdao.getNumChapters());
		// } else {
		// TextBiz.countWords(ldao, cdao, formatDao);
		// }
		// cdao.addOneToNumLines();
	}

	@Override
	public void handleDocTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {

	}

	@Override
	public void handleDocTagNotTag(FormatDao formatDao, LooperDao ldao) throws IOException {
		LOGGER.debug("CDTL: " + ldao.getCurrentDocTagLine());

		final SimpleChapterDao chpt = ldao.getCurrentChapter();
		final DocTagLine dttGL = ldao.getCurrentDocTagLine();
		final CountDao cdao = ldao.getChaptCount();
		final CountDao tdao = ldao.getTotalCount();
		LOGGER.debug("CDTL: " + ldao.getCurrentDocTagLine().getLine());

		if (chpt.isChapter) {
			cdao.setChapterNumber(tdao.getNumChapters());
		} else {
			TextBiz.countWords(ldao, cdao, formatDao);
		}
		cdao.addOneToNumLines();

	}

	@Override
	public void handleDocTagMaybeTag(FormatDao formatDao, LooperDao ldao) throws IOException {

	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// LOGGER.info("postLine-->");
	}

	@Override
	public String postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		LOGGER.info("postHandler-->");
		// outputSummaryOutputFile(summaryOut, outputDir, chapters);
		Integer totalWords = 0;
		for (ChapterDao cdao : ldao.getChapters()) {
			LOGGER.debug("cdao = " + cdao);
			totalWords += cdao.getNumWords();
		}
		LOGGER.debug("TotalWords: " + totalWords.toString());
		outputSummaryOutputFile(formatDao.getOutputCountFile(), null, ldao.getChapters(), formatDao);
		LOGGER.debug("Version:, " + formatDao.getVersion());
		// TODO format with commas...

		return "TotalWords: " + totalWords.toString();
	}

	@Override
	public void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		LOGGER.info("preHandler-->");
		ldao.InitializeCount();
	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

	private void outputSummaryOutputFile(final String summaryOut, final File outputDir, final List<ChapterDao> chapters,
			final FormatDao formatDao) throws IOException {
		Writer fWriter = null;
		try {
			Integer totalWords = 0;
			File summaryOutputFile = null;
			if (null != summaryOut && summaryOut.length() > 0) {
				summaryOutputFile = new File(summaryOut);
			} else {
				summaryOutputFile = new File(outputDir, "ChapterCount1.csv");
			}
			LOGGER.info("Writing summary data to " + summaryOutputFile);

			Charset selCharSet = formatDao.getCharSet();
			LOGGER.debug("preHandler: Charset chosen: " + selCharSet);
			fWriter = new OutputStreamWriter(new FileOutputStream(summaryOutputFile), selCharSet);

			// fWriter = new FileWriter(summaryOutputFile, false);
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
			fWriter.write("Version:, " + formatDao.getVersion());
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
