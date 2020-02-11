package com.echomap.kqf.looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.biz.TextParsingBiz;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SectionDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.looper.data.SimpleSectionDao;

public class LooperThread extends Thread {
	final FormatDao formatDao;
	final FileLooperHandler flHandler;
	final LooperDao ldao;
	final String workType = null;
	boolean reportedCountError = false;

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

				final DocTagLine dttGL = ldao.getLineDocTagLine();

				if (dttGL != null) {
					// flHandler.handleDocTagMaybeTag(formatDao, ldao);
					if (dttGL.isHasDocTag()) {
						// If still processing a long tag, don't throw it to the
						// handlers
						final DocTag metaDocTag = TextBiz.isMetaTag(dttGL);
						if (metaDocTag != null) {
							flHandler.handleMetaDocTag(formatDao, ldao, metaDocTag);
						} else if (!dttGL.isLongDocTag() || (dttGL.isLongDocTag() && dttGL.isEndDocTag())) {
							setupDocTagCapped(dttGL);
							flHandler.handleDocTag(formatDao, ldao);
						}
					} else {
						ldao.getChaptCount().addOneToNumLines();
						flHandler.handleDocTagNotTag(formatDao, ldao);
					}
				}

				flHandler.postLine(formatDao, ldao);
			} // while lines
			postHandler(formatDao, ldao);
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
					// notifyCtrl.statusUpdateForWork(flHandler.getWorkType(),
					// finalMsg);
				}
				final Object finalObj = flHandler.postHandlerPackage(formatDao, ldao);
				if (finalObj != null) {
					notifyCtrl.finalResultPackageFromWork(finalObj);
				}
			}
		}
		LOGGER.info("Loop: Done" + (workType != null ? "(" + workType + ")" : ""));
	}

	/**
	 * Parse out the Full Text of the tag into component Key/Values
	 * 
	 * @param dttGL
	 */
	private void setupDocTagCapped(final DocTagLine dttGL) {
		if (dttGL.isHasDocTag()) {
			final List<DocTag> docTags = dttGL.getDocTags();
			if (docTags != null) {
				for (final DocTag docTag : docTags) {
					if (!StringUtils.isEmpty(docTag.getName())) {
						docTag.setData(TextParsingBiz.parseNameValueAtDivided(docTag.getFullText()));
					}
				}
			}
		}
		//
	}

	private void postHandler(final FormatDao formatDao, final LooperDao ldao) {
		// ?? needed??
		int wds = ldao.getChaptCount().getNumWords();
		ldao.getLastSection().addNumWords(wds);
	}

	// private DocTagLine dttGL = null;
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
		final SimpleChapterDao lineChptData = TextBiz.isChapter(st, formatDao.getRegexpChapter());
		final SimpleSectionDao lineSectData = TextBiz.isSection(st, formatDao.getRegexpSection());
		ldao.setLineChapter(lineChptData);
		ldao.setLineSection(lineSectData);

		// Could be multiple!
		// if (st.indexOf(formatDao.getDocTagStart()) > 0)
		// ldao.containsStartTag();
		// if (st.indexOf(formatDao.getDocTagEnd()) > 0)
		// ldao.containsEndTag();
		//
		final Integer numDigits = formatDao.getOutputFormatDigits();
		final String formatOutputNumber = "%0" + (numDigits == null || numDigits == 0 ? 2 : numDigits) + "d";

		//
		final CountDao sdao = ldao.getSectionCount();

		if (lineSectData.isSection) {
			// Sections dont always have full data like chapters
			// if (sdao.getName() != null && sdao.getName().length() > 0) {
			// tdao.addNumWords(cdao.getNumWords());
			// int wds = ldao.getChaptCount().getNumWords();
			// ldao.getLastSection().addNumWords(wds);
			final SectionDao sectionDao = new SectionDao(lineSectData);
			ldao.getSections().add(sectionDao);
			ldao.getSectionCount().addChapterCount(1);
			sdao.clear();
			// tdao.addChapterCount(1);
			// }
			sdao.setName(lineSectData.sname);
			sdao.setTitle(lineSectData.title);
			sdao.setNumber(lineSectData.snum);

			// ldao.getSections().add(new SectionDao(lineSectData));
			flHandler.handleSection(formatDao, ldao);
		}

		final CountDao cdao = ldao.getChaptCount();
		final CountDao tdao = ldao.getTotalCount();

		if (lineChptData.isChapter) {
			if (cdao.getName() != null && cdao.getName().length() > 0) {
				tdao.addNumWords(cdao.getNumWords());
				ldao.getLastSection().addNumWords(cdao.getNumWords());
				ldao.getChapters().add(new ChapterDao(cdao));
				cdao.clear();
				tdao.addChapterCount(1);
			}
			cdao.setName(lineChptData.name);
			cdao.setTitle(lineChptData.title);
			cdao.setNumber(lineChptData.chpNum);
			if (!StringUtils.isEmpty(sdao.getNumber())) {
				try {
					final int parInt = Integer.valueOf(sdao.getNumber());
					final String parStr = String.format(formatOutputNumber, parInt);
					cdao.setParent(parStr);
				} catch (NumberFormatException e) {
					// e.printStackTrace()
					cdao.setParent(sdao.getParent());
				}
			}
			flHandler.handleChapter(formatDao, ldao);
		}
		//
		// final DocTagLine currentDocTagLine = TextBiz.isDocTag(st,
		// formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		// ldao.setCurrentDocTagLine(currentDocTagLine);

		//
		LOGGER.debug("TEXT: '" + ldao.getCurrentLine() + "'");
		DocTagLine dtt = TextBiz.isDocTag(ldao.getCurrentLine(), formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		//
		LOGGER.debug("LINE: '" + dtt.getLine() + "'");
		// bare:'" + dtt.getBareLine() + "'" + "' raw:'" + dtt.getRawLine() +
		// "'");
		LOGGER.debug("FLAG: END=" + dtt.isEndDocTag() + " HAS=" + dtt.isHasDocTag() + " LNG=" + dtt.isLongDocTag()
				+ " ONY=" + dtt.isOnlyDoctag());
		LOGGER.debug("PACH: Parent=" + dtt.getParentTag() + " Child=" + dtt.getChildTag());

		// Global Tag
		DocTagLine dttGL = ldao.getLineDocTagLine();

		// Verify start and end
		ldao.addStartTag(dtt.getNumberOfStartTags());
		ldao.addEndTag(dtt.getNumberOfEndTags());
		final long cntDiff = ldao.getDtEndCount() - ldao.getDtStartCount();
		if (!reportedCountError && (dttGL != null && !dttGL.isLongDocTag()) && (cntDiff < 0 || cntDiff > 0)) {
			String eLine = ldao.getCurrentLine();
			eLine = eLine.replace("\n", "").replace("\t", "").replace("\r", "");
			LOGGER.warn("Count might be off! (" + cntDiff + ") at line: [" + ldao.getLineCount() + "] <" + eLine + ">");
			if (!dtt.isLongDocTag() && dttGL != null && !dttGL.isLongDocTag()) {
				reportedCountError = true;
				final String msg = "Count is off! at line: [" + ldao.getLineCount() + "] <" + eLine + ">";
				// TODO not always being able to pass eline???
				// TODO? notifyCtrl.errorWithWork(msg, key);
				notifyCtrl.statusUpdateForWork("WARNING", msg);
				flHandler.looperMsgWarn(msg);
				LOGGER.debug("Count is off! (" + cntDiff + ") at line: [" + ldao.getLineCount() + "] <" + eLine + ">");
			}
		}

		if (dttGL != null) {
			LOGGER.debug("GLOB: END=" + dttGL.isEndDocTag() + " HAS=" + dttGL.isHasDocTag() + " LNG="
					+ dttGL.isLongDocTag() + " ONY=" + dttGL.isOnlyDoctag());
			if (!dttGL.isLongDocTag() || (dttGL.isLongDocTag() && dttGL.isEndDocTag())) {
				dttGL = new DocTagLine();
				dttGL.setLineNumber(ldao.getLineCount());
			}
			if (dttGL.isHasDocTag() && st.contains(formatDao.getDocTagStart())) {
				LOGGER.error("May contain an unclosed TAG!");
				if (notifyCtrl != null) {
					final String errmsg = "May contain an unclosed TAG at line <" + ldao.getLineCount()
							+ "> as line is <" + ldao.getCurrentLine() + ">";
					notifyCtrl.errorWithWork(errmsg, "UnclosedTag");
					flHandler.looperMsgWarn(errmsg);
				}
			}
		}
		if (dtt.isLongDocTag() || inLongDocTag) {
			inLongDocTag = true;
			if (!dttGL.isLongDocTag()) {
				// dttGL.setupLongDocTag(line, docTagText);
				dttGL.setRawLine(dtt.getRawLine());
				dttGL.setBareLine(dtt.getBareLine());
				dttGL.setTextLine(st);
				dttGL.setTextTagLine(dtt.getBareLine());
			}
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
				dttGL.addToTextLine(st);
				dtt = null;
			} else {
				// if (dttGL == null) {
				// dttGL = new DocTagLine();
				// dttGL.setLineNumber(ldao.getLineCount());
				// }
				dttGL.addDocTag(dtt.getDocTags());
				if (!dtt.isHasDocTag()) {
					dttGL.appendTextToLast(dtt.getLine());
				}
			}
		} else {
			dttGL = dtt;
			dttGL.setLineNumber(ldao.getLineCount());
		}

		ldao.setLineDocTagLine(dttGL);
	}

}