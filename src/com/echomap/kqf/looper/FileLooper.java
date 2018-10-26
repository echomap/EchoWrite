package com.echomap.kqf.looper;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.two.gui.WorkDoneNotify;

/**
 * 
 * @author mkatz
 */
public class FileLooper {
	private final static Logger LOGGER = LogManager.getLogger(FileLooper.class);
	final static String newLine = System.getProperty("line.separator");
	public static final String DEFAULToutputEncoding = "Cp1252";
	private WorkDoneNotify notifyCtrl = null;

	public FileLooper(final WorkDoneNotify notifyCtrl) {
		this.notifyCtrl = notifyCtrl;
	}

	/**
	 * 
	 * @param formatDao
	 * @throws IOException
	 */
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

		// loop(formatDao, dao, flHandler);
		final LooperThread p = new LooperThread(formatDao, flHandler, dao, notifyCtrl);
		p.start();

		try {
			LOGGER.info("Total Words: " + dao.getTotalCount().getNumWords());
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		// LOGGER.info("Version: " + props.getProperty("version"));
		LOGGER.info("Written to: " + formatDao.getOutputCountFile());
	}

	/**
	 * 
	 * @param formatDao
	 * @throws IOException
	 */
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
			final IOException e = new IOException(
					"Input file " + TextBiz.wrapString(inputFile == null ? "null" : inputFile.getAbsolutePath())
							+ " does not exist!");
			notifyCtrl.errorWithWork("No file selected!", e);
			throw e;
		}
		final FileLooperHandler flHandler = new FileLooperHandlerOutline();
		final LooperDao dao = new LooperDao();
		dao.setInputFile(inputFile);

		// loop(formatDao, dao, flHandler);
		final LooperThread p = new LooperThread(formatDao, flHandler, dao, notifyCtrl);
		p.start();
	}

	/**
	 * 
	 * @param formatDao
	 * @throws IOException
	 */
	public void format(final FormatDao formatDao) throws IOException {
		LOGGER.info("Formatter...>");
		LOGGER.info(formatDao);
		LOGGER.info("\n" + formatDao.prettyPrint());
		LOGGER.info("..>");

		final File inputFile = new File(formatDao.getInputFilename());

		final FileLooperHandler flHandler = new FileLooperHandlerFormatter();
		final LooperDao dao = new LooperDao();
		dao.setInputFile(inputFile);

		final LooperThread p = new LooperThread(formatDao, flHandler, dao, notifyCtrl);
		p.start();

		// // LOOPER
		// loop(formatDao, dao, flHandler);

	}

}
