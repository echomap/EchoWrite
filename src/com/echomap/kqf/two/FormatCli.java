/**
 * 
 */
package com.echomap.kqf.two;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.two.gui.WorkDoneNotify;

/**
 * 
 */
public class FormatCli implements WorkDoneNotify {
	private final static Logger LOGGER = LogManager.getLogger(FormatCli.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info("KQF Cli started....");
		try {
			Options options = setupOptions();
			final CommandLineParser parser = new PosixParser();
			final CommandLine line = parser.parse(options, args);

			final FormatDao formatDao = new FormatDao();

			// Setup the argument passed
			if (line.hasOption("inputfile"))
				formatDao.setInputFilename(line.getOptionValue("inputfile"));
			if (line.hasOption("outputfile"))
				formatDao.setOutputFilename(line.getOptionValue("outputfile"));
			else if (line.hasOption("outputdir") && line.getOptionValue("outputdir") != null) {
				final File basedir = new File(line.getOptionValue("outputdir"));
				if (line.hasOption("action")) {
					if (line.getOptionValue("action").compareToIgnoreCase("outline") == 0) {
						final File filename0 = new File(basedir, "Outline1.csv");
						final File filename1 = new File(basedir, "DocTags1.csv");
						formatDao.setOutputOutlineFile(filename0.getAbsolutePath());
						formatDao.setOutputOutlineFile1(filename1.getAbsolutePath());// all
					}
					if (line.getOptionValue("action").compareToIgnoreCase("count") == 0) {
						final File filename0 = new File(basedir, "ChapterCount1.csv");
						formatDao.setOutputCountFile(filename0.getAbsolutePath());
					}
					if (line.getOptionValue("action").compareToIgnoreCase("format") == 0) {
						// final File filename0 = new
						// File(basedir,"ChapterCount1.csv");
						// formatDao.setOutputCountFile(filename0.getAbsolutePath());
					}
				} else {
					// asdf
				}
			}

			if (line.hasOption("summaryout"))
				formatDao.setOutputCountFile(line.getOptionValue("summaryout"));

			if (line.hasOption("outlinefile"))
				formatDao.setOutputOutlineFile(line.getOptionValue("outlinefile"));
			if (line.hasOption("doctagsfile"))
				formatDao.setOutputOutlineFile1(line.getOptionValue("doctagsfile"));

			if (line.hasOption("storytitle1"))
				formatDao.setStoryTitle1(line.getOptionValue("storytitle1"));
			if (line.hasOption("storytitle2"))
				formatDao.setStoryTitle2(line.getOptionValue("storytitle2"));
			if (line.hasOption("formatmode"))
				formatDao.setFormatMode(line.getOptionValue("formatmode"));
			if (line.hasOption("outputencoding")) {
				final String OE = line.getOptionValue("outputencoding");
				if (OE != null && OE.length() > 0)
					formatDao.setOutputEncoding(line.getOptionValue("outputencoding"));
			}
			if (line.hasOption("chapterdivider"))
				formatDao.setChapterDivider(line.getOptionValue("chapterdivider"));
			if (line.hasOption("sectiondivider"))
				formatDao.setSectionDivider(line.getOptionValue("sectiondivider"));
			if (line.hasOption("removechptdiv"))
				formatDao.setRemoveChptDiv(true);
			if (line.hasOption("removesectdiv"))
				formatDao.setRemoveSectDiv(true);

			if (line.hasOption("centerstars"))
				formatDao.setCenterStars(true);
			if (line.hasOption("dropcapchapter"))
				formatDao.setDropCapChapter(true);
			if (line.hasOption("removediv")) {
				formatDao.setRemoveChptDiv(true);
				formatDao.setRemoveSectDiv(true);
			}
			if (line.hasOption("writechapters")) {
				formatDao.setWriteChapters(line.getOptionValue("writechapters"));
			}

			if (line.hasOption("centerable")) {
				formatDao.setCenterableLineText(line.getOptionValue("centerable"));
			}

			if (line.hasOption("doctagstart"))
				formatDao.setDocTagStart(line.getOptionValue("doctagend"));
			if (line.hasOption("doctagend"))
				formatDao.setDocTagEnd(line.getOptionValue("doctagend"));

			// biz.format(formatDao);
			// setupDao(formatDao);
			final FileLooper fileLooper = new FileLooper(new FormatCli());
			if (line.hasOption("action")) {
				if (line.getOptionValue("action").compareToIgnoreCase("outline") == 0) {
					fileLooper.outline(formatDao);
					return;
				}
				if (line.getOptionValue("action").compareToIgnoreCase("count") == 0) {
					fileLooper.count(formatDao);
					return;
				}
				if (line.getOptionValue("action").compareToIgnoreCase("format") == 0) {
					fileLooper.format(formatDao);
					return;
				}
			}
			fileLooper.format(formatDao);
		} catch (

		ParseException e)

		{
			e.printStackTrace();
		} catch (

		IOException e)

		{
			e.printStackTrace();
		}
		System.out.println("Format CLI 2 Done");

	}

	private static Options setupOptions() {
		// create Options object
		Options options = new Options();

		// Add options
		options.addOption("action", true, "action");

		options.addOption("inputfile", true, "inputfile");
		options.addOption("outputfile", true, "outputfile");

		options.addOption("storytitle1", true, "storytitle1");
		options.addOption("storytitle2", true, "Sub title");

		options.addOption("formatmode", true, "Format Mode");
		options.addOption("outputencoding", true, "Output Encoding");

		options.addOption("chapterdivider", true, "chapterdivider custom symbol");
		options.addOption("sectiondivider", true, "sectiondivider custom symbol");
		options.addOption("centerable", true, "centerable line text, ie '* * *'");
		options.addOption("dropcapchapter", false, "First letter in first word of new chapter should be bigger");
		options.addOption("removechptdiv", false, "Remove chapter divider marks");
		options.addOption("removesectdiv", false, "Remove section divider marks");
		options.addOption("removediv", false, "Remove chapter and section divider marks");
		options.addOption("centerstars", false, "If set will center all * or ** or ***... with or without spaces");
		options.addOption("writechapters", true, "If want chapters written out to seperate files in this DIR");

		options.addOption("doctagstart", true, "Starting doctag marker");
		options.addOption("doctagend", true, "Ending doctag marker");
		options.addOption("outlinefile", true, "Doctags/outline file");
		options.addOption("doctagsfile", true, "Doctags/all file");

		options.addOption("outputdir", true, "outputdir");
		options.addOption("summaryout", true, "summaryout");
		options.addOption("noseperate", false, "noseperate");

		return options;
	}

	@Override
	public void finishedWithWork(String msg) {
		LOGGER.info("Done with process " + msg);
	}

	@Override
	public void errorWithWork(String msg, Exception e) {
		LOGGER.error("Errorwith process " + msg);
		LOGGER.info(e);
	}

	@Override
	public void statusUpdateForWork(String header, String msg) {
		LOGGER.info("----process " + header + ", " + msg);
	}

}
