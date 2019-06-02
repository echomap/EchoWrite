package com.echomap.kqf.looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SectionDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.looper.data.SimpleSectionDao;

/**
 * 
 * @author mkatz
 */
public class FileLooperHandlerCount implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerCount.class);
	public static final String WORKTYPE = "Counter";
	NumberFormat numberFormat;
	// String workResult = null;

	public FileLooperHandlerCount() {
		numberFormat = NumberFormat.getInstance(Locale.getDefault());
	}

	@Override
	public String getWorkType() {
		return WORKTYPE;
	}

	// @Override
	// public String getWorkResult() {
	// return workResult;
	// }

	@Override
	public void preLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		// LOGGER.info("preLine-->");
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		// Not counting anything here
	}

	@Override
	public void handleDocTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		// Not counting anything in a DocTag
	}

	@Override
	public void handleDocTagNotTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.debug("CDTL: " + ldao.getLineDocTagLine());

		final SimpleSectionDao sect = ldao.getLineSection();
		final SimpleChapterDao chpt = ldao.getLineChapter();
		final CountDao cdao = ldao.getChaptCount();
		if (sect.isSection) {
			// cdao.setSectionNumber(sect.snum);
		} else if (chpt.isChapter) {
			// cdao.setChapterNumber(chpt.chpNum);
			// cdao.setSectionNumber(sect.snum);
		} else {
			TextBiz.countWords(ldao, cdao, formatDao);
		}
		// cdao.addOneToNumLines();
	}

	// @Override
	// public void handleDocTagMaybeTag(final FormatDao formatDao, final
	// LooperDao ldao) throws IOException {
	// //
	// }

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// LOGGER.info("postLine-->");
	}

	@Override
	public Object postHandlerPackage(FormatDao formatDao, LooperDao ldao) {
		return null;
	}

	@Override
	public String postHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.info("postHandler-->");
		Integer totalWords = 0;
		for (ChapterDao cdao : ldao.getChapters()) {
			LOGGER.debug("cdao = " + cdao.getChapterNumber() + " [" + cdao + "]");
			totalWords += cdao.getNumWords();
		}
		for (SectionDao sdao : ldao.getSections()) {
			LOGGER.debug("sdao = " + sdao.getNumber() + " [" + sdao + "]");
			// totalWords += sdao.getNumWords();
		}
		final CountDao cdTC = ldao.getTotalCount();
		LOGGER.debug("TotalWords: " + numberFormat.format(totalWords));
		LOGGER.debug("ChaptCount: " + cdTC.getCounter());
		// TODO THIS IS WRONG!
		LOGGER.debug("TotalWords: " + numberFormat.format(cdTC.getNumWords()));
		// outputSummaryOutputFile(summaryOut, outputDir, chapters);
		outputSummaryOutputFile(formatDao.getOutputCountFile(), null, ldao.getChapters(), formatDao);
		LOGGER.debug("Version:, " + formatDao.getVersion());
		// TODO format with commas...
		final StringBuilder sbResult = new StringBuilder();
		sbResult.append("--Counter Process: TotalWords: ");
		sbResult.append(numberFormat.format(totalWords));

		sbResult.append("\n");
		sbResult.append("--Counter Process: Chapters: ");
		sbResult.append(numberFormat.format(cdTC.getCounter()));

		sbResult.append("\n");
		sbResult.append("--Counter Process: Sections: ");
		final CountDao sCount = ldao.getSectionCount();
		sbResult.append(numberFormat.format(sCount.getCounter()));

		return sbResult.toString();
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
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

			final File outputDirO = summaryOutputFile.getParentFile();
			if (outputDirO != null) {
				outputDirO.getParentFile().mkdirs();
				outputDirO.mkdirs();
			}

			Charset selCharSet = formatDao.getCharSet();
			LOGGER.debug("preHandler: Charset chosen: " + selCharSet);
			fWriter = new OutputStreamWriter(new FileOutputStream(summaryOutputFile), selCharSet);

			// fWriter = new FileWriter(summaryOutputFile, false);
			fWriter.write("Chapter,Words,Title,Chars,Lines,Section");
			fWriter.write(TextBiz.newLine);
			final String CSV_DIV = ", ";
			for (ChapterDao countDao : chapters) {
				// TODO format for leading zeros?
				fWriter.write(
						countDao.getChapterName() + " " + countDao.getChapterNumber() + CSV_DIV + countDao.getNumWords()
								+ CSV_DIV + countDao.getChapterTitle() + CSV_DIV + countDao.getNumChars() + CSV_DIV
								+ countDao.getNumLines() + CSV_DIV + countDao.getSectionNumber());
				fWriter.write(TextBiz.newLine);
				totalWords += countDao.getNumWords();
			}
			// fWriter.write(TextBiz.newLine);
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
		LOGGER.debug("Chapter\t\tWords\tTitle");
		for (ChapterDao countDao : chapters) {
			LOGGER.debug(countDao.getChapterName() + " " + countDao.getChapterNumber() + "\t\t" + countDao.getNumWords()
					+ "\t" + countDao.getChapterTitle());
		}

		// System.out.println(cdao.getChapterName() + "\t\t"
		// + cdao.getNumWords() + "\t" + chpt.title);
	}
}
