package com.echomap.kqf.looper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;

public class FileLooperHandlerOutline implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerOutline.class);
	final static Properties props = new Properties();

	FileWriter fWriterOutline = null;
	FileWriter fWriterAll = null;

	final List<String> outlineTags = new ArrayList<String>();

	public FileLooperHandlerOutline() {
		try {
			final InputStream asdf = FileLooperHandlerOutline.class.getClassLoader()
					.getResourceAsStream("oo.properties");
			if (asdf != null)
				props.load(asdf);
		} catch (IOException e) {
			e.printStackTrace();
			props.setProperty("version", "0.0.0");
		}
		LOGGER.info("Version: " + props.getProperty("version"));
	}

	@Override
	public void preLine(FormatDao formatDao, LooperDao ldao, String st) {

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
			outlineLine(st, cdao, formatDao);
		}

		// parseLine(st, fWriter, chapterDivider, storyTitle1,
		// storyTitle2);
		cdao.addOneToNumLines();
	}

	private void outlineLine(String st, CountDao cdao, final FormatDao formatDao) throws IOException {
		DocTagLine dtt = TextBiz.isDocTag(st, formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		if (dtt.isHasDocTag()) {
			// if (dtt != TextBiz.DOCTAGTYPE.NONE) {
			final DocTag docTag = dtt.getDocTag();
			if (docTag != null) {
				if (outlineTags.contains(docTag.getName())) {
					writeEntryToCSV(fWriterOutline, docTag, cdao);
				}
				writeEntryToCSV(fWriterAll, docTag, cdao);
			}
		}
	}

	private void writeEntryToCSV(final FileWriter fWriter, final DocTag docTag, final CountDao cdao)
			throws IOException {
		fWriter.write("\"");
		fWriter.write(docTag.getName());
		fWriter.write("\",\"");
		fWriter.write(docTag.getValue());
		fWriter.write("\",");
		fWriter.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
		fWriter.write(",");
		fWriter.write(String.valueOf(cdao.getNumLines()));// lineCount));
		fWriter.write(TextBiz.newLine);
	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao, String st) {

	}

	@Override
	public void postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {

		if (fWriterAll != null) {
			fWriterAll.flush();
			fWriterAll.close();
		}
		if (fWriterOutline != null) {
			fWriterOutline.flush();
			fWriterOutline.close();
		}
	}

	@Override
	public void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		// open output file
		final File outputFile0 = new File(formatDao.getOutputOutlineFile());
		final File outputDir0 = outputFile0.getParentFile();
		if (outputDir0 != null) {
			outputDir0.getParentFile().mkdirs();
			outputDir0.mkdirs();
		}
		final File outputFile1 = new File(formatDao.getOutputOutlineFile1());
		final File outputDir1 = outputFile1.getParentFile();
		if (outputDir1 != null) {
			outputDir1.getParentFile().mkdirs();
			outputDir1.mkdirs();
		}

		// LOGGER.info("Writing data to " + outputFile1);
		fWriterAll = new FileWriter(outputFile1, false);
		fWriterAll.write("TagName,TagValue,Chpt,Line");
		fWriterAll.write(TextBiz.newLine);

		fWriterOutline = new FileWriter(outputFile0, false);
		fWriterOutline.write("TagName,TagValue,Chpt,Line");
		fWriterOutline.write(TextBiz.newLine);

		ldao.InitializeCount();

		outlineTags.add("time");
		outlineTags.add("loc");
		outlineTags.add("Date");
		outlineTags.add("loc");
		outlineTags.add("eventnote");
	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao, String st) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

}
