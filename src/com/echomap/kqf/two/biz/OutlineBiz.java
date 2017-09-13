package com.echomap.kqf.two.biz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.two.biz.CountBiz.ChapterDaoLoc;
import com.echomap.kqf.two.biz.FormatBiz.DOCTAGTYPE;
import com.echomap.kqf.two.data.DocTag;

public class OutlineBiz extends BaseBiz {
	private final static Logger LOGGER = LogManager.getLogger(OutlineBiz.class);
	final static String newLine = System.getProperty("line.separator");

	OutlineBiz() {
		try {
			final InputStream is = OutlineBiz.class.getClassLoader().getResourceAsStream("oo.properties");
			if (is != null)
				props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			props.setProperty("version", "0.0.0");
		}
		LOGGER.info("Version: " + props.getProperty("version"));
	}

	boolean runOutline(final String inputFileName, final String outputFileName, final String docTagStart,
			final String docTagEnd) throws IOError, IOException {
		LOGGER.info("runOutline: " + props.getProperty("version"));

		// open output file
		final File outputFile = new File(outputFileName);
		final File outputDir = outputFile.getParentFile();
		if (outputDir != null) {
			outputDir.getParentFile().mkdirs();
			outputDir.mkdirs();
		}

		// open input file
		final File inputFile = new File(inputFileName);
		final File inputDir = inputFile.getParentFile();
		if (inputDir != null) {
			inputDir.getParentFile().mkdirs();
			inputDir.mkdirs();
		}

		// read file, looking for doctags
		FileWriter fWriter = null;
		BufferedReader reader = null;
		try {
			if (!inputFile.exists() && inputFile.length() < 0) {
				LOGGER.error("The specified file does not exist");
			} else {
				final FileReader fr = new FileReader(inputFile);
				reader = new BufferedReader(fr);
			}
			LOGGER.info("Writing data to " + outputFile);
			fWriter = new FileWriter(outputFile, false);
			fWriter.write("TagName,TagValue,ChapterNum,LineNum");
			fWriter.write(newLine);

			final CountBiz countBiz = new CountBiz();
			int chapterCount = 0;
			int lineCount = 0;
			String st = null;
			while ((st = reader.readLine()) != null) {
				lineCount++;
				final ChapterDaoLoc chpt = countBiz.isChapter(st);
				if (chpt.isChapter) {
					chapterCount++;
				}
				DOCTAGTYPE dtt = isDocTag(st, docTagStart, docTagEnd);
				if (dtt != DOCTAGTYPE.NONE) {
					final String docTagText = parseForDocTags(st, docTagStart, docTagEnd);
					if (!StringUtils.isBlank(docTagText)) {
						final DocTag docTag = new DocTag(docTagText);
						fWriter.write(docTag.getName());
						fWriter.write(",");
						fWriter.write(docTag.getValue());
						fWriter.write(",");
						fWriter.write(String.valueOf(chapterCount));
						fWriter.write(",");
						fWriter.write(String.valueOf(lineCount));
						fWriter.write(newLine);
					}
				}
			}
			// fWriter.write(newLine);
			// fWriter.write(newLine);
			// fWriter.write(newLine);
			//
			// fWriter.write(newLine);
			// fWriter.write("Version:, " + props.getProperty("version"));

		} catch (IOException e) {

		} finally {
			if (fWriter != null) {
				fWriter.flush();
				fWriter.close();
			}
			if (reader != null) {
				// reader.flush();
				reader.close();
			}
		}

		// output doctags to file name,key, in csv format

		return false;
	}
}
