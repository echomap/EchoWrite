package com.echomap.kqf.looper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;

/**
 * 
 * @author mkatz
 */
public class FileLooperHandlerOutline implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerOutline.class);

	private Writer fWriterOutline = null;
	private Writer fWriterAll = null;

	private BufferedWriter fWriterOutlineFile;
	private BufferedWriter fWriterSceneFile;
	private BufferedWriter fWriterNotUsedFile;

	private int levelledCount = 0;
	// private String lastLevelledText = null;

	final List<String> outlineTags = new ArrayList<String>();

	private final Map<String, List<DocTag>> coalateTextMap = new TreeMap<String, List<DocTag>>();
	private final List<DocTag> sceneDTList = new ArrayList<DocTag>();
	private final List<DocTag> unusedTagList = new ArrayList<DocTag>();

	private boolean inLongDocTag = false;
	// private StringBuilder longDocTagText = new StringBuilder();

	public FileLooperHandlerOutline() {
		//
	}

	// @Override
	// public String getWorkResult() {
	// return workResult;
	// }

	@Override
	public String getWorkType() {
		return "Outliner";
	}

	@Override
	public void preLine(FormatDao formatDao, LooperDao ldao) {
		//
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		final SimpleChapterDao chpt = ldao.getCurrentChapter();
		final CountDao cdao = ldao.getChaptCount();
		if (chpt.isChapter) {
			writeChapterData(formatDao, ldao, cdao);
			levelledCount = 0;
		} else {
			// TODO outlineLine(ldao, cdao, formatDao);
		}
		cdao.addOneToNumLines();
	}

	@Override
	public void handleDocTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.debug("CDTL: " + ldao.getCurrentDocTagLine());

		dttGL = ldao.getCurrentDocTagLine();
		final CountDao cdao = ldao.getChaptCount();

		if (dttGL.isHasDocTag()) {
			// if (dtt != TextBiz.DOCTAGTYPE.NONE) {
			final List<DocTag> docTags = dttGL.getDocTags();
			if (docTags != null) {
				for (final DocTag docTag : docTags) {
					boolean wroteTag = false;
					if (outlineTags.contains(docTag.getName())) {
						wroteTag = true;
						writeEntryToCSV(fWriterOutline, docTag, cdao, ldao, dttGL);
					}
					writeEntryToCSV(fWriterAll, docTag, cdao, ldao, dttGL);

					if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())
							|| formatDao.getDocTagsOutlineExpandTags().contains(docTag.getName())) {
						wroteTag = true;
						writeEntryToOutline(fWriterOutlineFile, docTag, cdao, formatDao);
					}
					if (formatDao.getDocTagsSceneTags().contains(docTag.getName())) {
						wroteTag = true;
						sceneDTList.add(docTag);
					}
					if (formatDao.getDocTagsSceneCoTags().contains(docTag.getName())) {
						wroteTag = true;
						addToCoalateTextMap(docTag, cdao, formatDao, ldao);
					}
					if (!wroteTag) {
						if (fWriterNotUsedFile != null) {
							fWriterNotUsedFile.write(docTag.getName());
							fWriterNotUsedFile.write(TextBiz.newLine);
						}
						unusedTagList.add(docTag);
					}
				}
			}
			dttGL = null;
		} else {
			// LOGGER.debug("not writing list -------> ");
		}

	}

	@Override
	public void handleDocTagNotTag(FormatDao formatDao, LooperDao ldao) throws IOException {

	}

	@Override
	public void handleDocTagMaybeTag(FormatDao formatDao, LooperDao ldao) throws IOException {

	}

	private void writeChapterData(final FormatDao formatDao, final LooperDao ldao, final CountDao cdao)
			throws IOException {
		// format chapter number
		final CountDao tdao = ldao.getTotalCount();
		if (fWriterOutlineFile != null && cdao != null) {
			// TODO only for non-first if (cdao.getChapterNumber() > -1)// &&
			// tdao.getChapterNumber() > -1)
			fWriterOutlineFile.write(TextBiz.newLine);
			int cnum = cdao.getChapterNumber();
			String cnumS = new Integer(cnum).toString();
			if (cnum < 0) {
				cnumS = cdao.getChapterTitle();
				cnum = 0;
			} else if (cnum < 10) {
				cnumS = "0" + new Integer(cnum).toString();
			}
			fWriterOutlineFile.write("-= Chapter " + cnum + " (1." + cnumS + ") =-");
			fWriterOutlineFile.write(TextBiz.newLine);
		}
		if (fWriterSceneFile != null) {
			writeToSceneFileCo(formatDao);
		}
		coalateTextMap.clear();

		// DocTag docTagLast = null;
		writeToSceneFileMain(formatDao);
		// boolean inScene = false;

		if (fWriterSceneFile != null && cdao != null) {
			// TODO only for non-first if (cdao.getChapterNumber() > -1)// &&
			// tdao.getChapterNumber() > -1)
			fWriterSceneFile.write(TextBiz.newLine);
			int cnum = cdao.getChapterNumber();
			String cnumS = new Integer(cnum).toString();
			if (cnum < 0) {
				cnumS = cdao.getChapterTitle();
				cnum = 0;
			} else if (cnum < 10) {
				cnumS = "0" + new Integer(cnum).toString();
			}
			fWriterSceneFile.write("-= Chapter " + cnum + " (1." + cnumS + ") =-");
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.flush();
		}
	}

	private void writeToSceneFileMain(final FormatDao formatDao) throws IOException {
		for (final DocTag docTag : sceneDTList) {
			// if (docTag.getName().compareTo("scene") != 0) inScene = true;
			// if (inScene && docTag.getName().compareTo("subscene") != 0) {
			// fWriterSceneFile.write(TextBiz.newLine);
			// inScene = false; }
			// TODO Sub scene list?
			if (fWriterSceneFile != null) {
				fWriterSceneFile.write(docTag.getName());
				fWriterSceneFile.write(": ");
				final String val = docTag.getValue();
				writeDataToFileWithCrop(val, formatDao, fWriterSceneFile, false);
			}

			// docTagLast = docTag;
		}
		sceneDTList.clear();
	}

	// Write all the coalted doctags to the start of the chapter
	private void writeToSceneFileCo(final FormatDao formatDao) throws IOException {
		for (Map.Entry<String, List<DocTag>> entry : coalateTextMap.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
			final String key = entry.getKey();
			final List<DocTag> vals = entry.getValue();
			fWriterSceneFile.write(key);
			fWriterSceneFile.write(":");
			fWriterSceneFile.write(TextBiz.newLine);

			for (final DocTag docTag : vals) {
				fWriterSceneFile.write("\t");
				fWriterSceneFile.write(docTag.getValue());
				fWriterSceneFile.write(TextBiz.newLine);
			}
		}
		// TODO use sublist of doctags to put sub(s) into mains

		if (!coalateTextMap.isEmpty())
			if (!StringUtils.isEmpty(formatDao.getSceneCoalateDivider())) {
				fWriterSceneFile.write(formatDao.getSceneCoalateDivider());
				fWriterSceneFile.write(TextBiz.newLine);
			}
	}

	private void writeDataToFileWithCrop(final String val, final FormatDao formatDao, final BufferedWriter fWriterFile2,
			final boolean pad) throws IOException {
		int maxLineLen = 70; // param
		maxLineLen = formatDao.getDocTagsMaxLineLength();
		// if (maxLineLen > 0 && val.length() > maxLineLen) {
		if (pad)
			fWriterFile2.write(TextBiz.newLine);

		String pre = (pad ? "\t" : "");
		String mid = "";
		StringBuilder strToWrite = new StringBuilder();

		final StringTokenizer st = new StringTokenizer(val, " \t");
		while (st.hasMoreTokens()) {
			final String wd = st.nextToken();

			// final String[] words = val.split(" \\t");
			// String pre = (pad ? "\t" : "");
			// String mid = "";
			// StringBuilder strToWrite = new StringBuilder();
			// for (int j = 0; j < words.length; j++) {
			// final String wd = words[j];
			if ("(+n)".compareTo(wd) == 0) {
				fWriterFile2.write(pre);
				fWriterFile2.write(strToWrite.toString());
				fWriterFile2.write(TextBiz.newLine);
				strToWrite.setLength(0);
				pre = "\t";
				// no strToWrite.append(wd);
			} else if ((strToWrite.length() + wd.length()) > maxLineLen) {
				fWriterFile2.write(pre);
				fWriterFile2.write(strToWrite.toString());
				fWriterFile2.write(TextBiz.newLine);
				strToWrite.setLength(0);
				pre = "\t";
				strToWrite.append(wd);
			} else {
				if (strToWrite.length() > 0)
					strToWrite.append(mid);
				strToWrite.append(wd);
			}
			mid = " ";
		}
		if (strToWrite.length() > 0) {
			fWriterFile2.write(pre);
			fWriterFile2.write(strToWrite.toString());
			fWriterFile2.write(TextBiz.newLine);
		}
		// } else {
		// fWriterFile2.write(val);// docTag.getValue());
		// fWriterFile2.write(TextBiz.newLine);
		// }
	}

	DocTagLine dttGL = null;

	/**
	 * Called from Handle Line
	 *
	 * private void outlineLine(final LooperDao ldao, final CountDao cdao, final
	 * FormatDao formatDao) throws IOException { // LOGGER.debug("LINE: '" +
	 * ldao.getCurrentLine() + "'"); DocTagLine dtt =
	 * TextBiz.isDocTag(ldao.getCurrentLine(), formatDao.getDocTagStart(),
	 * formatDao.getDocTagEnd()); // LOGGER.debug("DTLE: '" + dtt.getLine() +
	 * "'"); LOGGER.debug("IS: END: " + dtt.isEndDocTag() + " HAS: " +
	 * dtt.isHasDocTag() + " LNG: " + dtt.isLongDocTag() + " ONY: " +
	 * dtt.isOnlyDoctag()); if (dttGL != null) LOGGER.debug("GL: END: " +
	 * dttGL.isEndDocTag() + " HAS: " + dttGL.isHasDocTag() + " LNG: " +
	 * dttGL.isLongDocTag() + " ONY: " + dttGL.isOnlyDoctag());
	 * 
	 * LOGGER.debug("CDTL: " + ldao.getCurrentDocTagLine());
	 * 
	 * if (dtt.isLongDocTag() || inLongDocTag) { inLongDocTag = true; // final
	 * String longtext = //
	 * ldao.getCurrentLine().replace(formatDao.getDocTagEnd(), "") //
	 * .replace(formatDao.getDocTagStart(), ""); //
	 * longDocTagText.append(longtext);
	 * 
	 * if (dtt.isEndDocTag()) { inLongDocTag = false; // if (dtt.getDocTags() !=
	 * null) // dtt.getDocTags().clear(); // final DocTag dt = new //
	 * DocTag(longDocTagText.toString().trim()); // dtt.addDocTag(dt); //
	 * dtt.setHasDocTag(true); // dtt.setLongDocTag(false);
	 * 
	 * dttGL.addDocTag(dtt.getDocTags()); if (!dtt.isHasDocTag()) {
	 * dttGL.appendTextToLast(dtt.getLine()); } dttGL.setHasDocTag(true);
	 * dttGL.setLongDocTag(false); dtt = null; } else { if (dttGL == null) {
	 * dttGL = new DocTagLine(); } dttGL.addDocTag(dtt.getDocTags()); if
	 * (!dtt.isHasDocTag()) { dttGL.appendTextToLast(dtt.getLine()); } } } else
	 * dttGL = dtt;
	 * 
	 * if (dttGL.isHasDocTag() && !dttGL.isLongDocTag() && !inLongDocTag) { //
	 * if (dtt != TextBiz.DOCTAGTYPE.NONE) { final List<DocTag> docTags =
	 * dttGL.getDocTags(); if (docTags != null) { for (final DocTag docTag :
	 * docTags) { boolean wroteTag = false; if
	 * (outlineTags.contains(docTag.getName())) { wroteTag = true;
	 * writeEntryToCSV(fWriterOutline, docTag, cdao, ldao, dttGL); }
	 * writeEntryToCSV(fWriterAll, docTag, cdao, ldao, dttGL);
	 * 
	 * if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())
	 * || formatDao.getDocTagsOutlineExpandTags().contains(docTag.getName())) {
	 * wroteTag = true; writeEntryToOutline(fWriterOutlineFile, docTag, cdao,
	 * formatDao); } if
	 * (formatDao.getDocTagsSceneTags().contains(docTag.getName())) { wroteTag =
	 * true; sceneDTList.add(docTag); } if
	 * (formatDao.getDocTagsSceneCoTags().contains(docTag.getName())) { wroteTag
	 * = true; addToCoalateTextMap(docTag, cdao, formatDao, ldao); } if
	 * (!wroteTag) { if (fWriterNotUsedFile != null) {
	 * fWriterNotUsedFile.write(docTag.getName());
	 * fWriterNotUsedFile.write(TextBiz.newLine); } unusedTagList.add(docTag); }
	 * } } dttGL = null; } else { // LOGGER.debug("not writing list -------> ");
	 * } }
	 */

	private void addToCoalateTextMap(final DocTag docTag, final CountDao cdao, final FormatDao formatDao,
			final LooperDao ldao) {
		final String key = docTag.getName();
		List<DocTag> dts = coalateTextMap.get(key);
		if (dts == null)
			dts = new ArrayList<DocTag>();
		dts.add(docTag);
		coalateTextMap.put(key, dts);
	}

	private void writeEntryToOutline(final BufferedWriter fWriterL, final DocTag docTag, final CountDao cdao,
			final FormatDao formatDao) throws IOException {
		if (fWriterL == null)
			return;

		// Someday this will be better, for now it will have scene left aligned,
		// with subscenes, tabbed or dashed to the right.
		final String scenePrefix = formatDao.getDocTagsScenePrefix();
		final String subScenePrefix = formatDao.getDocTagsSubScenePrefix();
		if ("scene".compareToIgnoreCase(docTag.getName()) == 0
				|| "outline".compareToIgnoreCase(docTag.getName()) == 0) {
			this.levelledCount = 1;
		} else if (docTag.getName().startsWith("sub-") || docTag.getName().endsWith("-sub")
				|| "subscene".compareToIgnoreCase(docTag.getName()) == 0
				|| "scenesub".compareToIgnoreCase(docTag.getName()) == 0) {
			this.levelledCount = 2;
		}

		/*
		 * if
		 * (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())
		 * ) { this.levelledCount = 0; if
		 * ("scene".compareToIgnoreCase(docTag.getName()) == 0) {
		 * this.levelledCount = 0; } else if
		 * (docTag.getName().startsWith("sub-") ||
		 * docTag.getName().endsWith("-sub")) { this.levelledCount = 1; } else {
		 * // if ("subscene".compareToIgnoreCase(docTag.getName()) == 0 // ||
		 * "scenesub".compareToIgnoreCase(docTag.getName()) == 0) {
		 * this.levelledCount = 1; } }
		 */

		if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())) {
			if (levelledCount > 0) {
				fWriterL.write(scenePrefix);
				for (int i = 1; i < levelledCount; i++) {
					fWriterL.write(subScenePrefix);
				}
			}
			// if (docTag.getName().startsWith("sub-") ||
			// docTag.getName().endsWith("-sub")) {
			// // fWriterL.write("\t");
			// fWriterL.write("-");
			// // lastLevelledText = docTag.getName();
			// } else if (levelledCount == 1)
			// fWriterL.write("-");
			// else if (levelledCount == 2)
			// fWriterL.write("--");
			writeDataToFileWithCrop(docTag.getValue(), formatDao, fWriterL, false);
			// writeDataToFileWithCrop(" [" + levelledCount + "]", formatDao,
			// fWriterL, false);
			// fWriterL.write(docTag.getValue());
			// fWriterL.write(TextBiz.newLine);
		} else if (formatDao.getDocTagsOutlineExpandTags().contains(docTag.getName())) {
			fWriterL.write(docTag.getName());
			fWriterL.write(": ");
			writeDataToFileWithCrop(docTag.getValue(), formatDao, fWriterL, false);
			// writeDataToFileWithCrop(" [" + levelledCount + "]", formatDao,
			// fWriterL, false);
			// fWriterL.write(docTag.getValue());
			// fWriterL.write(TextBiz.newLine);
		}
		//
		// // TODO collect values for summary in outline per chapter?
		// if (docTag.getName().compareTo("outline") == 0) {
		// fWriterL.write(docTag.getValue());
		// fWriterL.write(TextBiz.newLine);
		// } else if (docTag.getName().compareTo("outlinedata") == 0 ||
		// docTag.getName().compareTo("suboutline") == 0
		// || docTag.getName().compareTo("outlinesub") == 0) {
		// fWriterL.write("\t");
		// fWriterL.write(docTag.getValue());
		// fWriterL.write(TextBiz.newLine);
		// } else if (docTag.getName().compareTo("scene") == 0) {
		// // fWriterL.write(docTag.getValue());
		// writeDataToFileWithCrop(docTag.getValue(), formatDao, fWriterL,
		// false);
		// // fWriterL.write(TextBiz.newLine);
		// } else if (docTag.getName().compareTo("scenedata") == 0 ||
		// docTag.getName().compareTo("subscene") == 0
		// || docTag.getName().compareTo("scenesub") == 0) {
		// fWriterL.write("\t");
		// fWriterL.write(docTag.getValue());
		// fWriterL.write(TextBiz.newLine);
		// } else {
		// fWriterL.write("\t");
		// fWriterL.write(docTag.getName());
		// fWriterL.write(": ");
		// fWriterL.write(docTag.getValue());
		// // fWriterL.write("");
		// fWriterL.write(TextBiz.newLine);
		// }
	}

	private void writeEntryToCSV(final Writer fWriterL, final DocTag docTag, final CountDao cdao, final LooperDao ldao,
			final DocTagLine docTagLine) throws IOException {
		if (fWriterL == null)
			return;
		fWriterL.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
		fWriterL.write(",");
		fWriterL.write(String.valueOf(docTagLine.getLineCount()));
		fWriterL.write(",");
		fWriterL.write("main");
		fWriterL.write(",");

		fWriterL.write("\"");
		fWriterL.write(docTag.getName());
		fWriterL.write("\",\"\",\"");
		fWriterL.write(docTag.getValue());
		fWriterL.write("\"");
		fWriterL.write(TextBiz.newLine);

		//
		if (docTag.getValue().indexOf(":") > -1) {
			// 1st:
			String pattern1 = "(.+:)\\s+(.+);\\s+(.*)";
			Pattern p = Pattern.compile(pattern1);
			Matcher m = p.matcher(docTag.getFullText());
			if (m.find()) {
				if (m.groupCount() > 2) {
					String gr1 = m.group(1);
					String gr2 = m.group(2);
					String gr3 = m.group(3);
					writeEntryToCSV2(fWriterL, String.valueOf(docTagLine.getLineCount()), cdao, docTag.getName(), gr2);
					writeEntryToCSV3(fWriterL, String.valueOf(docTagLine.getLineCount()), cdao, docTag.getName(), gr3);
				}
			} else
				writeEntryToCSV3(fWriterL, String.valueOf(docTagLine.getLineCount()), cdao, docTag.getName(),
						docTag.getValue());
		}
		//
	}

	private void writeEntryToCSV2(final Writer fWriterL, final String lineCount, final CountDao cdao,
			final String fullText1, final String fullText2) throws IOException {
		fWriterL.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
		fWriterL.write(",");
		fWriterL.write(lineCount);
		fWriterL.write(",");
		fWriterL.write("subm");
		fWriterL.write(",");

		fWriterL.write("\"");
		fWriterL.write(fullText1);
		fWriterL.write("\",\"\",\"");
		fWriterL.write(fullText2);
		fWriterL.write("\"");
		fWriterL.write(TextBiz.newLine);
	}

	private void writeEntryToCSV3(final Writer fWriterL, final String lineCount, final CountDao cdao,
			final String fullText1, String fullText2) throws IOException {
		String pattern1 = "(?<key>[^.:]*):(?<value>[^.:]+)*";// "([^.:]*:[^.:$]+)*";

		fullText2 = fullText2.replace("(+n)", "");

		Pattern p = Pattern.compile(pattern1);
		Matcher m = p.matcher(fullText2);

		// if (m.find()) {
		while (m.find()) {
			// for (int i = 0; i <= m.groupCount(); i++) {
			int i = 0;
			String gr = m.group(i);
			final String mKey = m.group("key");
			final String mVal = (m.group("value") == null ? "" : m.group("value"));
			LOGGER.debug("group #" + i + ": [" + (gr == null ? "null" : gr.trim()) + "]");
			if (gr != null && !StringUtils.isEmpty(mKey)) {
				fWriterL.write(String.valueOf(cdao.getChapterNumber()));
				fWriterL.write(",");
				fWriterL.write(lineCount);
				fWriterL.write(",");
				fWriterL.write("subt");
				fWriterL.write(",");

				fWriterL.write("\"");
				fWriterL.write(fullText1);
				fWriterL.write("\",\"");
				fWriterL.write(mKey.trim());
				fWriterL.write("\",\"");
				fWriterL.write(mVal.trim());
				fWriterL.write("\"");
				fWriterL.write(TextBiz.newLine);
			}
			// }
		}
		// }

	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao) {
		if (fWriterOutlineFile != null)
			try {
				fWriterOutlineFile.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public String postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {

		writeChapterData(formatDao, ldao, null);

		final StringBuilder sb = new StringBuilder();
		sb.append("Outline: ");
		sb.append("Unused#: ");
		sb.append(unusedTagList.size());
		if (unusedTagList.size() > 0) {
			sb.append("(");
			sb.append(getDoctagNameList(unusedTagList));
			sb.append(")");
		}

		if (fWriterAll != null) {
			fWriterAll.flush();
			fWriterAll.close();
		}
		if (fWriterOutline != null) {
			fWriterOutline.flush();
			fWriterOutline.close();
		}
		if (fWriterOutlineFile != null) {
			fWriterOutlineFile.flush();
			fWriterOutlineFile.close();
		}
		if (fWriterSceneFile != null) {
			fWriterSceneFile.flush();
			fWriterSceneFile.close();
		}
		if (fWriterNotUsedFile != null) {
			fWriterNotUsedFile.flush();
			fWriterNotUsedFile.close();
		}

		return sb.toString();
	}

	private Object getDoctagNameList(final List<DocTag> docTagList) {
		final StringBuilder sb = new StringBuilder();
		for (final DocTag docTag : docTagList) {
			sb.append(docTag.getName());
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {

		// Change encoding?
		final Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

		File outputFileO = null;
		if (!StringUtils.isBlank(formatDao.getOutputOutlineFile())) {
			outputFileO = new File(formatDao.getOutputOutlineFile());
			final File outputDirO = outputFileO.getParentFile();
			if (outputDirO != null) {
				outputDirO.getParentFile().mkdirs();
				outputDirO.mkdirs();
			}
		}
		File outputFileA = null;
		if (!StringUtils.isBlank(formatDao.getOutputOutlineFile1())) {
			outputFileA = new File(formatDao.getOutputOutlineFile1());
			final File outputDirA = outputFileA.getParentFile();
			if (outputDirA != null) {
				outputDirA.getParentFile().mkdirs();
				outputDirA.mkdirs();
			}
		}

		// LOGGER.info("Writing data to " + outputFile1);
		if (outputFileA != null) {
			fWriterAll = new OutputStreamWriter(new FileOutputStream(outputFileA), selCharSet);
			// fWriterAll = new FileWriter(outputFileA, false);
			fWriterAll.write("Chpt,Line,Type,TagName,SubTagName,TagValue");
			fWriterAll.write(TextBiz.newLine);
		}

		if (outputFileO != null) {
			fWriterOutline = new OutputStreamWriter(new FileOutputStream(outputFileO), selCharSet);
			// fWriterOutline = new FileWriter(outputFileO, false);
			fWriterOutline.write("Chpt,Line,Type,TagName,SubTagName,TagValue");
			fWriterOutline.write(TextBiz.newLine);
		}

		if (!StringUtils.isBlank(formatDao.getOutputDocTagsOutlineFile())) {
			final File outputFileO2 = new File(formatDao.getOutputDocTagsOutlineFile());
			final File outputDirO2 = outputFileO2.getParentFile();
			if (outputDirO2 != null) {
				outputDirO2.getParentFile().mkdirs();
				outputDirO2.mkdirs();
			}
			// TODO change encoding?
			// fWriterOutlineFile =
			// Files.newBufferedWriter(outputFileO2.toPath(), selCharSet,
			// StandardOpenOption.CREATE,
			// StandardOpenOption.TRUNCATE_EXISTING);
			fWriterOutlineFile = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFileO2.toPath(),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), selCharSet));

			// fWriterOutlineFile = new FileWriter(outputFileO2, false);
			//
			fWriterOutlineFile.write("-= Outline: \"");
			fWriterOutlineFile.write(formatDao.getStoryTitle1());
			fWriterOutlineFile.write("\"");
			if (!StringUtils.isBlank(formatDao.getStoryTitle2())) {
				fWriterOutlineFile.write(" : \"");
				fWriterOutlineFile.write(formatDao.getStoryTitle2());
				fWriterOutlineFile.write("\"");
			}
			fWriterOutlineFile.write(" =-");
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.write("Compressed Tags: ");
			fWriterOutlineFile.write(formatDao.getDocTagsOutlineCompressTagsAsString());
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.write("Expanded Tags: ");
			fWriterOutlineFile.write(formatDao.getDocTagsOutlineExpandTags());
			fWriterOutlineFile.write(TextBiz.newLine);
			// fWriterOutlineFile.write(TextBiz.newLine);
		}

		if (!StringUtils.isBlank(formatDao.getOutputDocTagsSceneFile())) {
			final File outputFileS2 = new File(formatDao.getOutputDocTagsSceneFile());
			final File outputDirS2 = outputFileS2.getParentFile();
			if (outputDirS2 != null) {
				outputDirS2.getParentFile().mkdirs();
				outputDirS2.mkdirs();
			}
			// fWriterSceneFile = Files.newBufferedWriter(outputFileS2.toPath(),
			// selCharSet, StandardOpenOption.CREATE,
			// StandardOpenOption.TRUNCATE_EXISTING);
			fWriterSceneFile = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFileS2.toPath(),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), selCharSet));

			// fWriterSceneFile = new BufferedWriter(outputFileS2, false);
			//
			fWriterSceneFile.write("-= Scenes: \"");
			fWriterSceneFile.write(formatDao.getStoryTitle1());
			fWriterSceneFile.write("\"");
			if (!StringUtils.isBlank(formatDao.getStoryTitle2())) {
				fWriterSceneFile.write(" : \"");
				fWriterSceneFile.write(formatDao.getStoryTitle2());
				fWriterSceneFile.write("\"");
			}
			fWriterSceneFile.write(" =-");
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write("Captured Tags: ");
			fWriterSceneFile.write(formatDao.getDocTagsSceneTags());
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write("Coalating Tags: ");
			fWriterSceneFile.write(formatDao.getDocTagsSceneCoTags());
			fWriterSceneFile.write(TextBiz.newLine);
			// fWriterSceneFile.write(TextBiz.newLine);
		}

		final File inFile = new File(formatDao.getInputFilename());
		final File inFilePath = inFile.getParentFile();
		final String filePrefix = formatDao.getFilePrefix();
		File notUsedFile = null;
		if (!StringUtils.isBlank(filePrefix))
			notUsedFile = new File(inFilePath, filePrefix + "DocTagsNotUsed.txt");
		else
			notUsedFile = new File(inFilePath, "DocTagsNotUsed.txt");
		// fWriterNotUsedFile = Files.newBufferedWriter(notUsedFile.toPath(),
		// selCharSet, StandardOpenOption.CREATE,
		// StandardOpenOption.TRUNCATE_EXISTING);
		fWriterNotUsedFile = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(notUsedFile.toPath(),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), selCharSet));

		// fWriterNotUsedFile = new BufferedWriter(notUsedFile, false);

		ldao.InitializeCount();

		outlineTags.add("time");
		outlineTags.add("loc");
		outlineTags.add("date");
		outlineTags.add("loc");
		outlineTags.add("eventnote");
	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

}
