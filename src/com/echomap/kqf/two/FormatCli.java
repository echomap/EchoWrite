/**
 * 
 */
package com.echomap.kqf.two;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.echomap.kqf.two.biz.FormatBiz;
import com.echomap.kqf.two.data.FormatDao;

/**
 * 
 */
public class FormatCli {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final FormatBiz biz = new FormatBiz();
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
			biz.format(formatDao);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Format CLI 2 Done");
	}

	private static Options setupOptions() {
		// create Options object
		Options options = new Options();

		// Add options
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

		return options;
	}
}
