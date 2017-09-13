package com.echomap.kqf.looper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.two.data.DocTag;
import com.echomap.kqf.two.data.FormatDao;

public class FileLooperHandlerOutline implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerOutline.class);
	final static Properties props = new Properties();

	FileWriter fWriter = null;

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
				// final String docTagText = TextBiz.parseForDocTags(st,
				// formatDao.getDocTagStart(), formatDao.getDocTagEnd());
				// if (!StringUtils.isBlank(docTagText)) {
				// final DocTag docTag = new DocTag(docTagText);
				fWriter.write("\"");
				fWriter.write(docTag.getName());
				fWriter.write("\",");
				fWriter.write(docTag.getValue());
				fWriter.write(",");
				fWriter.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
				fWriter.write(",");
				fWriter.write(String.valueOf(cdao.getNumLines()));// lineCount));
				fWriter.write(TextBiz.newLine);
			}
		}
	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao, String st) {

	}

	@Override
	public void postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {

		if (fWriter != null) {
			fWriter.flush();
			fWriter.close();
		}
	}

	@Override
	public void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		// open output file
		final File outputFile = new File(formatDao.getOutputOutlineFile());
		final File outputDir = outputFile.getParentFile();
		if (outputDir != null) {
			outputDir.getParentFile().mkdirs();
			outputDir.mkdirs();
		}

		LOGGER.info("Writing data to " + outputFile);
		fWriter = new FileWriter(outputFile, false);
		fWriter.write("TagName,TagValue,Chpt,Line");
		fWriter.write(TextBiz.newLine);

		ldao.InitializeCount();
	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao, String st) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

}
