package com.echomap.kqf.looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.two.data.FormatDao;

/**
 * 
 * @author mkatz
 */
public class FileLooper {
	private final static Logger LOGGER = LogManager.getLogger(FileLooper.class);
	final static String newLine = System.getProperty("line.separator");
	public static final String DEFAULToutputEncoding = "Cp1252";
	
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

			final FileReader fr = new FileReader(inputFile);
			reader = new BufferedReader(fr);
			String st = "";
			// System.out.println("Chapter\t\t\tWords\tTitle");
			while ((st = reader.readLine()) != null) {
				flHandler.preLine(formatDao, ldao, st);
				//
				flHandler.handleLine(formatDao, ldao, st);
				// cdao.addOneToNumLines();
				flHandler.postLine(formatDao, ldao, st);
			}
			flHandler.postLastLine(formatDao, ldao, st);

			// tdao.copy(cdao);
			// chapters.add(tdao);
			// tdao = new CountDao();
			// SummaryOutputFile
			// outputSummaryOutputFile(summaryOut, outputDir, chapters);
		} finally {
			if (reader != null) {
				reader.close();
			}
			flHandler.postHandler(formatDao, ldao);
		}
		LOGGER.info("Loop: Done");
	}

	public void count(final FormatDao formatDao) throws IOException {
		LOGGER.info("count...>");
		LOGGER.info("dao: \n" + formatDao.prettyPrint());
		LOGGER.info("..>");

		// check dirs
		final String inputFilename = formatDao.getInputFilename();
		final File inputFile;
		File inputFileTemp = new File(inputFilename);
		if (!inputFileTemp.exists()) {
			final int idx = inputFileTemp.getAbsolutePath().lastIndexOf(".");
			if (idx > 0) {
				String nFilename = inputFileTemp.getAbsolutePath().substring(0, idx);
				inputFile = new File(nFilename);
				LOGGER.info("InputFilename:\t" + TextBiz.wrapString(inputFile.getAbsolutePath()));
			} else
				inputFile = null;
		} else
			inputFile = inputFileTemp;

		if (inputFile == null || !inputFile.exists()) {
			throw new IOException(
					"Input file " + TextBiz.wrapString(inputFile == null ? "null" : inputFile.getAbsolutePath())
							+ " does not exist!");
		}
		final FileLooperHandler flHandler = new FileLooperHandlerCount();
		final LooperDao dao = new LooperDao();
		dao.setInputFile(inputFile);
		// LOOPER
		loop(formatDao, dao, flHandler);

		LOGGER.info("Total Words: " + dao.getTotalCount().getNumWords());
		// LOGGER.info("Version: " + props.getProperty("version"));
	}

	public void outline(final FormatDao formatDao) throws IOException {
		// check dirs
		final String inputFilename = formatDao.getInputFilename();
		final File inputFile;
		File inputFileTemp = new File(inputFilename);
		if (!inputFileTemp.exists()) {
			final int idx = inputFileTemp.getAbsolutePath().lastIndexOf(".");
			if (idx > 0) {
				String nFilename = inputFileTemp.getAbsolutePath().substring(0, idx);
				inputFile = new File(nFilename);
				LOGGER.info("InputFilename:\t" + TextBiz.wrapString(inputFile.getAbsolutePath()));
			} else
				inputFile = null;
		} else
			inputFile = inputFileTemp;

		if (inputFile == null || !inputFile.exists()) {
			throw new IOException(
					"Input file " + TextBiz.wrapString(inputFile == null ? "null" : inputFile.getAbsolutePath())
							+ " does not exist!");
		}
		final FileLooperHandler flHandler = new FileLooperHandlerOutline();
		final LooperDao dao = new LooperDao();
		dao.setInputFile(inputFile);
		// LOOPER
		loop(formatDao, dao, flHandler);
	}

	public void format(final FormatDao formatDao) throws IOException {
		LOGGER.info("Formatter...>");
		LOGGER.info(formatDao);
		LOGGER.info("\n" + formatDao.prettyPrint());
		LOGGER.info("..>");

		final File inputFile = new File(formatDao.getInputFilename());

		final FileLooperHandler flHandler = new FileLooperHandlerFormatter();
		final LooperDao dao = new LooperDao();
		dao.setInputFile(inputFile);

		// LOOPER
		loop(formatDao, dao, flHandler);

	}

}
