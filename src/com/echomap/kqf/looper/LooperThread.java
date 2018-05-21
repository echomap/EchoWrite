package com.echomap.kqf.looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.looper.data.SimpleSectionDao;
import com.echomap.kqf.two.gui.WorkDoneNotify;

public class LooperThread extends Thread {
	final FormatDao formatDao;
	final FileLooperHandler flHandler;
	final LooperDao ldao;
	final String workType = null;

	private WorkDoneNotify notifyCtrl = null;

	private final static Logger LOGGER = LogManager.getLogger(LooperThread.class);
	final static String newLine = System.getProperty("line.separator");
	public static final String DEFAULToutputEncoding = "Cp1252";

	public LooperThread(final FormatDao formatDao, final FileLooperHandler flHandler, final LooperDao ldao,
			final WorkDoneNotify notifyCtrl) {
		this.formatDao = formatDao;
		this.flHandler = flHandler;
		this.ldao = ldao;
		this.notifyCtrl = notifyCtrl;
	}

	public void run() {
		// LOOPER
		try {
			loop(formatDao, ldao, flHandler);
			// TODO done callback.
			// final Object objret =
			if (notifyCtrl != null) {
				notifyCtrl.finishedWithWork(flHandler.getWorkType());
				// notifyCtrl.finalResultFromWork(flHandler.getWorkResult());
			} else
				LOGGER.error("NO Notifier setup!!");
		} catch (IOException e) {
			// TODO Error callback!
			// e.printStackTrace();
			if (notifyCtrl != null)
				notifyCtrl.errorWithWork(flHandler.getWorkType(), e);
			else {
				LOGGER.error("!!!!");
				LOGGER.error(e);
				e.printStackTrace();
			}
		} catch (Exception e) {
			if (notifyCtrl != null)
				notifyCtrl.errorWithWork(flHandler.getWorkType(), e);
			LOGGER.error("!!!!");
			LOGGER.error(e);
			e.printStackTrace();
		} catch (Throwable e) {
			if (notifyCtrl != null)
				notifyCtrl.errorWithWork(flHandler.getWorkType(), e);
			LOGGER.error("!!!!");
			LOGGER.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param formatDao
	 * @param ldao
	 * @param flHandler
	 * @throws IOException
	 */
	private void loop(final FormatDao formatDao, final LooperDao ldao, final FileLooperHandler flHandler)
			throws IOException {
		LOGGER.info("Loop: Called");
		File inputFile = ldao.getInputFile();

		flHandler.preHandler(formatDao, ldao);
		BufferedReader reader = null;
		try {
			if (!inputFile.exists() && inputFile.length() < 0) {
				LOGGER.error("The specified input file does not exist");
				return;
			}
			if (notifyCtrl != null)
				notifyCtrl.statusUpdateForWork(flHandler.getWorkType(), "Starting up...");
			final FileReader fr = new FileReader(inputFile);
			reader = new BufferedReader(fr);
			String st = "";
			int lineCount = 0;
			long lastReport = System.currentTimeMillis();
			// System.out.println("Chapter\t\t\tWords\tTitle");
			while ((st = reader.readLine()) != null) {
				lineCount++;
				ldao.setLineCount(lineCount);
				if (lineCount % 1000 == 0) {
					long nowReport = System.currentTimeMillis();
					if ((nowReport - lastReport) > 999)
						if (notifyCtrl != null)
							notifyCtrl.statusUpdateForWork(flHandler.getWorkType(), "Continuing on line " + lineCount);
				}
				setupLine(st, ldao, formatDao);
				//
				flHandler.preLine(formatDao, ldao);
				//
				flHandler.handleLine(formatDao, ldao);

				if (dttGL != null) {
					flHandler.handleDocTagMaybeTag(formatDao, ldao);

					if (dttGL.isHasDocTag()) {
						if (!dttGL.isLongDocTag() || (dttGL.isLongDocTag() && dttGL.isEndDocTag())) {
							flHandler.handleDocTag(formatDao, ldao);
						}
					} else
						flHandler.handleDocTagNotTag(formatDao, ldao);
				}

				flHandler.postLine(formatDao, ldao);
			}
			flHandler.postLastLine(formatDao, ldao);

			if (notifyCtrl != null)
				notifyCtrl.statusUpdateForWork(flHandler.getWorkType(), "Finishing up.");
			// tdao.copy(cdao);
			// chapters.add(tdao);
			// tdao = new CountDao();
			// SummaryOutputFile
			// outputSummaryOutputFile(summaryOut, outputDir, chapters);
		} finally {
			if (reader != null) {
				reader.close();
			}
			final String finalMsg = flHandler.postHandler(formatDao, ldao);
			if (notifyCtrl != null) {
				notifyCtrl.statusUpdateForWork(flHandler.getWorkType(), "Finished.");

				if (finalMsg != null) {
					notifyCtrl.finalResultFromWork(finalMsg);
					notifyCtrl.statusUpdateForWork(flHandler.getWorkType(), finalMsg);
				}
			}
		}
		LOGGER.info("Loop: Done");
	}

	private DocTagLine dttGL = null;
	private boolean inLongDocTag = false;

	/**
	 * 
	 * @param st
	 * @param ldao
	 * @param formatDao
	 */
	private void setupLine(final String st, final LooperDao ldao, final FormatDao formatDao) {
		//
		ldao.setOriginalLine(st);
		ldao.setCurrentLine(st);
		final SimpleChapterDao chpt = TextBiz.isChapter(st, formatDao.getRegexpChapter());
		final SimpleSectionDao sectionType = TextBiz.isSection(st, formatDao.getRegexpSection());
		ldao.setCurrentChapter(chpt);
		ldao.setCurrentSection(sectionType);

		//
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
			cdao.setChapterNumber(chpt.chpNum);// numerical);//
												// tdao.getNumChapters());
		}
		//
		// final DocTagLine currentDocTagLine = TextBiz.isDocTag(st,
		// formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		// ldao.setCurrentDocTagLine(currentDocTagLine);

		//
		LOGGER.debug("LINE: '" + ldao.getCurrentLine() + "'");
		DocTagLine dtt = TextBiz.isDocTag(ldao.getCurrentLine(), formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		//
		LOGGER.debug("DTLE: '" + dtt.getLine() + "'");
		LOGGER.debug("IS: END: " + dtt.isEndDocTag() + " HAS: " + dtt.isHasDocTag() + " LNG: " + dtt.isLongDocTag()
				+ " ONY: " + dtt.isOnlyDoctag());
		if (dttGL != null)
			LOGGER.debug("GL: END: " + dttGL.isEndDocTag() + " HAS: " + dttGL.isHasDocTag() + " LNG: "
					+ dttGL.isLongDocTag() + " ONY: " + dttGL.isOnlyDoctag());
		if (dttGL != null && !dttGL.isLongDocTag()) {
			dttGL = new DocTagLine();
			dttGL.setLineNumber(ldao.getLineCount());
		}
		if (dtt.isLongDocTag() || inLongDocTag) {
			inLongDocTag = true;
			dttGL.setLongDocTag(true);
			// dttGL.setHasDocTag(true);
			dttGL.setLongDocTag(true);
			dttGL.setEndDocTag(false);

			if (dtt.isEndDocTag()) {
				inLongDocTag = false;

				dttGL.addDocTag(dtt.getDocTags());
				if (!dtt.isHasDocTag()) {
					dttGL.appendTextToLast(dtt.getLine());
				}
				dttGL.setHasDocTag(true);
				dttGL.setLongDocTag(true);
				dttGL.setEndDocTag(true);
				dtt = null;
			} else {
				if (dttGL == null) {
					dttGL = new DocTagLine();
					dttGL.setLineNumber(ldao.getLineCount());
				}
				dttGL.addDocTag(dtt.getDocTags());
				if (!dtt.isHasDocTag()) {
					dttGL.appendTextToLast(dtt.getLine());
				}
			}
		} else {
			dttGL = dtt;
			dttGL.setLineNumber(ldao.getLineCount());
		}

		ldao.setCurrentDocTagLine(dttGL);

	}

}