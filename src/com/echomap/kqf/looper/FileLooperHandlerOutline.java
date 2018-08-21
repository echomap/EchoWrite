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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.OtherDocTagData;
import com.echomap.kqf.looper.data.ChapterDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.SimpleChapterDao;
import com.echomap.kqf.looper.data.SimpleSectionDao;

/**
 * 
 * @author mkatz
 */
public class FileLooperHandlerOutline implements FileLooperHandler {
	private static final String START_STRING2 = " (";

	private static final String END_STRING2 = ") =-";

	private static final String END_STRING = " =-";

	private static final String SECTION = "-= Section ";

	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerOutline.class);

	private static final String CHAPTER = "-= Chapter ";

	private Writer fWriterOutlineCsv = null;
	private Writer fWriterAllTagsCsv = null;

	private BufferedWriter fWriterOutlineFile;
	private BufferedWriter fWriterSceneFile;
	private BufferedWriter fWriterNotUsedFile;
	private BufferedWriter fWriterDocTagReportFile;
	private BufferedWriter fWriterOther1File;
	private Map<String, BufferedWriter> fWriterOthersList = new HashMap<>();

	private int levelledCount = 0;
	// private String lastLevelledText = null;

	final List<String> outlineTags = new ArrayList<String>();

	private final Map<String, List<DocTag>> coalateTextMap = new TreeMap<String, List<DocTag>>();
	private final List<DocTag> sceneDTList = new ArrayList<DocTag>();
	private final List<String> unusedTagNameList = new ArrayList<String>();
	private final SortedMap<String, List<String>> usedTagFileList = new TreeMap<String, List<String>>();

	// private boolean inLongDocTag = false;
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
		if (ldao.getCurrentSection() != null && ldao.getCurrentSection().isSection) {
			writeChapterData(formatDao, ldao, null);
			writeSectionData(formatDao, ldao);
		}
		final SimpleChapterDao chpt = ldao.getCurrentChapter();
		final CountDao cdao = ldao.getChaptCount();
		if (chpt.isChapter) {
			writeChapterData(formatDao, ldao, cdao);
			levelledCount = 0;
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
			// Why would this have an empty rawline and be a thing?
			// -Well, long doctags might not have one right now...
			// -TODO test and check TEST1
			if (docTags != null && dttGL.isHasDocTag()) {
				// && !StringUtils.isBlank(dttGL.getRawLine())) {
				for (final DocTag docTag : docTags) {
					// if (StringUtils.isBlank(docTag.getName()))
					// continue;
					boolean wroteTag = false;
					if (outlineTags.contains(docTag.getName())) {
						wroteTag = true;
						writeEntryToCSV(fWriterOutlineCsv, docTag, cdao, ldao, dttGL);
						addToUsedTagFileList(dttGL, docTag, "outline");
					}
					writeEntryToCSV(fWriterAllTagsCsv, docTag, cdao, ldao, dttGL);

					if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())
							|| formatDao.getDocTagsOutlineExpandTags().contains(docTag.getName())) {
						if (!wroteTag)
							addToUsedTagFileList(dttGL, docTag, "outline");
						wroteTag = true;
						writeEntryToOutline(fWriterOutlineFile, docTag, cdao, formatDao);
					}
					if (formatDao.getDocTagsSceneTags().contains(docTag.getName())) {
						wroteTag = true;
						sceneDTList.add(docTag);
						addToUsedTagFileList(dttGL, docTag, "scene");
					}
					if (formatDao.getDocTagsSceneCoTags().contains(docTag.getName())) {
						if (!wroteTag)
							addToUsedTagFileList(dttGL, docTag, "scene");
						wroteTag = true;
						addToCoalateTextMap(dttGL, docTag, cdao, formatDao, ldao);
					}
					// if
					// (formatDao.getDocTagsOther1Tags().contains(docTag.getName()))
					// {
					// wroteTag = true;
					// writeEntryToFile(fWriterOther1File, docTag, cdao,
					// formatDao);
					// }
					final List<OtherDocTagData> odtdList = formatDao.getOutputs();
					if (odtdList != null) {
						for (final OtherDocTagData otherOutput : odtdList) {
							if (StringUtils.isBlank(otherOutput.getFile()))
								continue;
							// final File file1 = new
							// File(otherOutput.getFile());
							if (otherOutput.getDocTagsList().contains(docTag.getName())) {
								final BufferedWriter bw = fWriterOthersList.get(otherOutput.getName());
								writeEntryToFile(bw, docTag, cdao, formatDao);
								addToUsedTagFileList(dttGL, docTag, otherOutput.getName());
								wroteTag = true;
							}
						}
					}

					if (!wroteTag) {
						// if (fWriterNotUsedFile != null) {
						// fWriterNotUsedFile.write(docTag.getName());
						// fWriterNotUsedFile.write(TextBiz.newLine);
						// }
						if (!unusedTagNameList.contains(docTag.getName()))
							unusedTagNameList.add(docTag.getName());
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
		//
	}

	// @Override
	// public void handleDocTagMaybeTag(FormatDao formatDao, LooperDao ldao)
	// throws IOException {
	//
	// }

	private void writeSectionData(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		final SimpleSectionDao ssd = ldao.getCurrentSection();
		if (!ssd.isSection)
			return;
		// String nameS = ssd.sname;
		// String numS = ssd.snum;
		String titleS = ssd.title;

		if (fWriterOutlineFile != null) {
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.write(SECTION + titleS + END_STRING);
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.flush();
		}
		if (fWriterSceneFile != null) {
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write(SECTION + titleS + END_STRING);
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.flush();
		}
		if (fWriterOther1File != null) {
			fWriterOther1File.write(TextBiz.newLine);
			fWriterOther1File.write(SECTION + titleS + END_STRING);
			fWriterOther1File.write(TextBiz.newLine);
			fWriterOther1File.flush();
		}
		for (Map.Entry<String, BufferedWriter> entry : fWriterOthersList.entrySet()) {
			// System.out.println(entry.getKey() + ":" + entry.getValue());
			BufferedWriter bw = entry.getValue();
			if (bw != null) {
				bw.write(TextBiz.newLine);
				bw.write(SECTION + titleS + END_STRING);
				bw.write(TextBiz.newLine);
				bw.flush();
			}
		}
	}

	private void writeChapterData(final FormatDao formatDao, final LooperDao ldao, final CountDao cdao)
			throws IOException {
		// format chapter number
		// final CountDao tdao = ldao.getTotalCount();
		if (fWriterOutlineFile != null && cdao != null) {
			// TODO only for non-first if (cdao.getChapterNumber() > -1)// &&
			// tdao.getChapterNumber() > -1)
			fWriterOutlineFile.write(TextBiz.newLine);
			final String cnumS = cdao.getChapterNumber();
			final String volume = formatDao.getVolume();
			final String volStr = (volume == null ? "" : volume + ".");
			fWriterOutlineFile.write(CHAPTER + cnumS + START_STRING2 + volStr + cnumS + END_STRING2);
			fWriterOutlineFile.write(TextBiz.newLine);
		}
		if (fWriterSceneFile != null) {
			writeToSceneFileCo(formatDao);
		}
		coalateTextMap.clear();

		// DocTag docTagLast = null;
		writeToSceneFileMain(formatDao);
		// REMOVED per 'MoreFiles' Functions writeToOther1FileMain(formatDao);
		// boolean inScene = false;

		if (fWriterSceneFile != null && cdao != null) {
			// TODO only for non-first if (cdao.getChapterNumber() > -1)// &&
			// tdao.getChapterNumber() > -1)
			fWriterSceneFile.write(TextBiz.newLine);
			String cnumS = cdao.getChapterNumber();

			final String volume = formatDao.getVolume();
			final String volStr = (volume == null ? "" : volume + ".");
			fWriterSceneFile.write(CHAPTER + cnumS + START_STRING2 + volStr + cnumS + END_STRING2);
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.flush();
		}
		if (fWriterOther1File != null && cdao != null) {
			fWriterOther1File.write(TextBiz.newLine);
			final String cnumS = cdao.getChapterNumber();
			final String volume = formatDao.getVolume();
			final String volStr = (volume == null ? "" : volume + ".");
			fWriterOther1File.write(CHAPTER + cnumS + START_STRING2 + volStr + cnumS + END_STRING2);
			fWriterOther1File.write(TextBiz.newLine);
			fWriterOther1File.flush();
		}
		for (Map.Entry<String, BufferedWriter> entry : fWriterOthersList.entrySet()) {
			// System.out.println(entry.getKey() + ":" + entry.getValue());
			BufferedWriter bw = entry.getValue();
			if (bw != null && cdao != null) {
				bw.write(TextBiz.newLine);
				final String cnumS = cdao.getChapterNumber();
				final String volume = formatDao.getVolume();
				final String volStr = (volume == null ? "" : volume + ".");
				bw.write(CHAPTER + cnumS + START_STRING2 + volStr + cnumS + END_STRING2);
				bw.write(TextBiz.newLine);
				bw.flush();
			}
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
				writeDataToFileWithCrop(val, formatDao, fWriterSceneFile);
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
				String fullText2 = docTag.getValue();

				writeDataToFileWithCrop(fullText2, formatDao, fWriterSceneFile, "\t");

				// fullText2 = fullText2.replace("(+n)", "\n\t");
				// fullText2 = fullText2.replace("(+u)", "\t*");
				// // if (fullText2.startsWith("\n"))
				// // fullText2 = fullText2.substring(2, fullText2.length());
				// // if (fullText2.endsWith("\n\n"))
				// // fullText2 = fullText2.substring(0, fullText2.length() -
				// 4);
				// fullText2 = fullText2.trim();
				// fullText2 = fullText2.replace(" \n", "\n");
				//
				// fWriterSceneFile.write("\t");
				// fWriterSceneFile.write(fullText2);
				// fWriterSceneFile.write(TextBiz.newLine);
			}
		}
		// TODO use sublist of doctags to put sub(s) into mains

		if (!coalateTextMap.isEmpty())
			if (!StringUtils.isEmpty(formatDao.getSceneCoalateDivider())) {
				fWriterSceneFile.write(formatDao.getSceneCoalateDivider());
				fWriterSceneFile.write(TextBiz.newLine);
			}
	}

	private void writeDataToFileWithCrop(final String textData, final FormatDao formatDao,
			final BufferedWriter fWriterFile2) throws IOException {
		writeDataToFileWithCrop(textData, formatDao, fWriterFile2, null);
	}

	private void writeDataToFileWithCrop(final String textData, final FormatDao formatDao,
			final BufferedWriter fWriterFile2, final String basepad) throws IOException {
		int maxLineLen = 70; // param
		maxLineLen = formatDao.getDocTagsMaxLineLength();
		// if (maxLineLen > 0 && val.length() > maxLineLen) {
		// if (pad)
		// fWriterFile2.write(TextBiz.newLine);

		final String preBase = (basepad == null ? "" : basepad);
		String WORD_DIV = "";
		final StringBuilder strToWrite = new StringBuilder();

		StringBuilder textData2 = new StringBuilder();
		int idxU = textData.indexOf("(+u)");
		if (idxU > -1) {
			LOGGER.debug("Has embeded +U");
			// pre="\t\t";
			if (textData.indexOf("(+n)(+u)") > -1) {
				String textData3 = textData.replace("(+n)(+u)", "(+u)");
				textData2.append(textData3.replace("(+u)", "(+n)\t*"));
			} else if (textData.indexOf("(+n)\t(+u)") > -1) {
				String textData3 = textData.replace("(+n)\t(+u)", "(+u)");
				textData2.append(textData3.replace("(+u)", "(+n)\t*"));
			} else
				textData2.append(textData.replace("(+u)", "(+n)\t*"));
			// textData2.append("(+n)");
		} else {
			textData2.append(textData);
		}

		// boolean lineIsListItem = false;
		final StringTokenizer st = new StringTokenizer(textData2.toString(), " \t", false);
		boolean secondLvl = false;
		String pre2 = preBase;
		while (st.hasMoreTokens()) {
			String wd = st.nextToken();
			pre2 = (secondLvl ? preBase + "\t" : preBase);
			// if (wd.contains("\t")) {
			// if (strToWrite.length() > 0)
			// wd.replace("\t", "");
			// }
			if ("(+n)".compareTo(wd) == 0) {
				fWriterFile2.write(pre2);
				fWriterFile2.write(strToWrite.toString().trim());
				fWriterFile2.write(TextBiz.newLine);
				strToWrite.setLength(0);
				// pre += "\t";
				secondLvl = true;
				// no strToWrite.append(wd);
			} else if ((strToWrite.length() + wd.length()) > maxLineLen) {
				fWriterFile2.write(pre2);
				fWriterFile2.write(strToWrite.toString().trim());
				fWriterFile2.write(TextBiz.newLine);
				strToWrite.setLength(0);
				// pre += "\t";
				secondLvl = true;
				pre2 = (secondLvl ? preBase + "\t" : preBase);
				strToWrite.append(pre2);
				strToWrite.append(wd);
			} else {
				if (strToWrite.length() > 0)
					strToWrite.append(WORD_DIV);
				strToWrite.append(wd);
			}
			// int idx = strToWrite.indexOf("(+u)");
			// if (strToWrite.indexOf("(+u)") > -1) {
			// LOGGER.debug("Has embeded +U");
			// lineIsListItem = true;
			// String str1 = strToWrite.toString();
			// str1 = str1.replace("(+u)", "\t*");
			// strToWrite.setLength(0);
			// strToWrite.append(str1);
			// }
			int idx = strToWrite.indexOf("(+n)");
			if (idx > -1) {
				LOGGER.debug("Has embeded +N");
				String str1 = strToWrite.substring(0, idx + 4);
				str1 = str1.replace("(+n)", "");
				fWriterFile2.write(pre2);
				fWriterFile2.write(str1.trim());
				fWriterFile2.write(TextBiz.newLine);
				strToWrite.delete(0, idx + 4);
				// pre = "\t";
				secondLvl = true;
				pre2 = (secondLvl ? preBase + "\t" : preBase);
			}
			WORD_DIV = " ";
		}
		if (strToWrite.length() > 0) {
			fWriterFile2.write(pre2);
			fWriterFile2.write(strToWrite.toString().trim());
			fWriterFile2.write(TextBiz.newLine);
		}
		// else if (lineIsListItem)
		// fWriterFile2.write(TextBiz.newLine);
		// } else {
		// fWriterFile2.write(val);// docTag.getValue());
		// fWriterFile2.write(TextBiz.newLine);
		// }
	}

	DocTagLine dttGL = null;

	private void addToCoalateTextMap(final DocTagLine dttGL2, final DocTag docTag, final CountDao cdao,
			final FormatDao formatDao, final LooperDao ldao) {
		final String key = docTag.getName();
		if (StringUtils.isEmpty(key)) {
			LOGGER.error("Tried to add empty key to CoalateTextMap?<" + dttGL2 + ">");
		} else {
			List<DocTag> dts = coalateTextMap.get(key);
			if (dts == null)
				dts = new ArrayList<DocTag>();
			dts.add(docTag);
			coalateTextMap.put(key, dts);
		}
	}

	private void writeEntryToFile(final BufferedWriter fWriterL, final DocTag docTag, final CountDao cdao,
			final FormatDao formatDao) throws IOException {
		if (fWriterL == null)
			return;
		fWriterL.write(docTag.getName());
		fWriterL.write(": ");
		writeDataToFileWithCrop(docTag.getValue(), formatDao, fWriterL);

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
			writeDataToFileWithCrop(docTag.getValue(), formatDao, fWriterL);
			// writeDataToFileWithCrop(" [" + levelledCount + "]", formatDao,
			// fWriterL, false);
			// fWriterL.write(docTag.getValue());
			// fWriterL.write(TextBiz.newLine);
		} else if (formatDao.getDocTagsOutlineExpandTags().contains(docTag.getName())) {
			fWriterL.write(docTag.getName());
			fWriterL.write(": ");
			writeDataToFileWithCrop(docTag.getValue(), formatDao, fWriterL);
			// writeDataToFileWithCrop(" [" + levelledCount + "]", formatDao,
			// fWriterL, false);
			// fWriterL.write(docTag.getValue());
			// fWriterL.write(TextBiz.newLine);
		}
		//
		// //
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

		String fullText2 = docTag.getValue();

		fullText2 = fullText2.replace("(+n)", "");
		fullText2 = fullText2.replace("(+u)", "*");

		fWriterL.write("\"");
		fWriterL.write(docTag.getName().trim());
		fWriterL.write("\",\"\",\"");
		fWriterL.write(fullText2.trim());
		fWriterL.write("\"");
		fWriterL.write(TextBiz.newLine);

		final String lineCountStr = String.valueOf(docTagLine.getLineCount());
		//
		if (docTag.getValue().indexOf(":") > -1) {
			boolean matchWorked = false;
			// 1st:
			final String pattern1 = "(?<dname>.+:)(?<dhdr>.+?);(?<drest>.*)";
			// final String pattern2 = "(.+:)(.+?):(.*)";
			final Pattern p = Pattern.compile(pattern1);
			final Matcher m = p.matcher(docTag.getFullText());
			if (m.matches()) {
				if (m.groupCount() > 2) {
					String gr0 = m.group();
					String gr1 = m.group("dname");
					String gr2 = m.group("dhdr");
					String gr3 = m.group("drest");

					writeEntryToCSV2(fWriterL, lineCountStr, cdao, docTag.getName(), gr2);
					writeEntryToCSV2B(fWriterL, lineCountStr, cdao, docTag.getName(), gr2, gr3);
					writeEntryToCSV3(fWriterL, lineCountStr, cdao, docTag.getName(), gr3);
					matchWorked = true;
				}
			}
			// if (!matchWorked) {
			// final Pattern p2 = Pattern.compile(pattern2);
			// final Matcher m2 = p.matcher(docTag.getFullText());
			// if (m2.find()) {
			// if (m2.groupCount() > 2) {
			// String gr1 = m2.group(1);
			// String gr2 = m2.group(2);
			// String gr3 = m2.group(3);
			// writeEntryToCSV2(fWriterL,
			// lineCountStr, cdao,
			// docTag.getName(),
			// gr2);
			// writeEntryToCSV3(fWriterL,
			// lineCountStr, cdao,
			// docTag.getName(),
			// gr3);
			// matchWorked = true;
			// }
			// }
			// }
			if (!matchWorked)
				writeEntryToCSV3(fWriterL, lineCountStr, cdao, docTag.getName(), docTag.getValue());
		} else if (docTag.getValue().indexOf(";") > -1) {
			final String pattern1 = "(?<dhdr>.+?);(?<drest>.*)";
			final Pattern p = Pattern.compile(pattern1);
			final Matcher m = p.matcher(docTag.getValue());
			if (m.matches()) {
				if (m.groupCount() > 1) {
					String gr2 = m.group("dhdr");
					String gr3 = m.group("drest");

					writeEntryToCSV2B(fWriterL, lineCountStr, cdao, docTag.getName(), gr2, gr3);
					// writeEntryToCSV3(fWriterL, lineCountStr, cdao,
					// docTag.getName(), gr3);
				}
			}
		}

		//
	}

	private void writeEntryToCSV2(final Writer fWriterL, final String lineCount, final CountDao cdao,
			final String fullText1, final String fullText2) throws IOException {
		fWriterL.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
		fWriterL.write(",");
		fWriterL.write(lineCount);
		fWriterL.write(",");
		fWriterL.write("subm1");
		fWriterL.write(",");

		fWriterL.write("\"");
		fWriterL.write(fullText1.trim());
		fWriterL.write("\",\"\",\"");
		fWriterL.write(fullText2.trim());
		fWriterL.write("\"");
		fWriterL.write(TextBiz.newLine);
	}

	private void writeEntryToCSV2B(final Writer fWriterL, final String lineCount, final CountDao cdao,
			final String tagName, final String subTagName, final String tagValue) throws IOException {
		fWriterL.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
		fWriterL.write(",");
		fWriterL.write(lineCount);
		fWriterL.write(",");
		fWriterL.write("subm2");
		fWriterL.write(",");

		fWriterL.write("\"");
		fWriterL.write(tagName.trim());
		fWriterL.write("\",\"");
		fWriterL.write(subTagName.trim());
		fWriterL.write("\",\"");
		fWriterL.write(tagValue.trim());
		fWriterL.write("\"");
		fWriterL.write(TextBiz.newLine);
	}

	private void writeEntryToCSV3(final Writer fWriterL, final String lineCount, final CountDao cdao,
			final String fullText1, String fullText2) throws IOException {
		String pattern1 = "(?<key>[^.:]*):(?<value>[^.:]+)*";// "([^.:]*:[^.:$]+)*";

		fullText2 = fullText2.replace("(+n)", "");
		fullText2 = fullText2.replace("(+u)", "*");

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
				fWriterL.write(fullText1.trim());
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

		Collections.sort(unusedTagNameList);
		final StringBuilder sb = new StringBuilder();
		sb.append("Outline: ");
		sb.append("Unused#: ");
		sb.append(unusedTagNameList.size());
		if (unusedTagNameList.size() > 0) {
			if (fWriterNotUsedFile != null) {
				fWriterNotUsedFile.write(sb.toString());
				fWriterNotUsedFile.write(TextBiz.newLine);
				for (final String docTagName : unusedTagNameList) {
					fWriterNotUsedFile.write(docTagName);
					fWriterNotUsedFile.write(TextBiz.newLine);
				}
			}
			sb.append(" (");
			sb.append(fromListToCommaDelimString(unusedTagNameList));
			sb.append(")");
		}
		if (fWriterDocTagReportFile != null) {
			final List<String> allKeys = new ArrayList<>();
			final Set<String> keys = usedTagFileList.keySet();
			int maxkeylength = 0;
			for (String key : keys) {
				if (key.length() > maxkeylength)
					maxkeylength = key.length();
				allKeys.add(key);
			}
			for (final String docTagName : unusedTagNameList) {
				if (!allKeys.contains(docTagName))
					allKeys.add(docTagName);
			}
			Collections.sort(allKeys);
			fWriterDocTagReportFile.write("-= DocTags Report =-");
			fWriterDocTagReportFile.write(TextBiz.newLine);
			fWriterDocTagReportFile.write("(Used Tags #: ");
			fWriterDocTagReportFile.write(String.valueOf(keys.size()));
			fWriterDocTagReportFile.write(" ) ");
			fWriterDocTagReportFile.write("(Unused Tags #: ");
			fWriterDocTagReportFile.write(String.valueOf(unusedTagNameList.size()));
			fWriterDocTagReportFile.write(" )");
			fWriterDocTagReportFile.write(TextBiz.newLine);

			fWriterDocTagReportFile.write(TextBiz.newLine);
			fWriterDocTagReportFile.write("-= DocTags Report (Used) ");
			fWriterDocTagReportFile.write("Tags #: ");
			fWriterDocTagReportFile.write(String.valueOf(usedTagFileList.size()));
			fWriterDocTagReportFile.write(" =-");
			fWriterDocTagReportFile.write(TextBiz.newLine);

			for (String key : allKeys) {
				String padStr = StringUtils.rightPad(key, maxkeylength, " ");
				fWriterDocTagReportFile.write(padStr);
				fWriterDocTagReportFile.write(" = ");
				List<String> usedTagList = usedTagFileList.get(key);
				String str = "<NONE>";
				if (usedTagList != null) {
					Collections.sort(usedTagList);
					str = fromListToCommaDelimString(usedTagList);
				}
				fWriterDocTagReportFile.write(str);
				fWriterDocTagReportFile.write(TextBiz.newLine);
			}

			fWriterDocTagReportFile.write(TextBiz.newLine);
			fWriterDocTagReportFile.write("-= DocTags Report (Unused) ");
			fWriterDocTagReportFile.write("Tags #: ");
			fWriterDocTagReportFile.write(String.valueOf(unusedTagNameList.size()));
			fWriterDocTagReportFile.write(" =-");
			fWriterDocTagReportFile.write(TextBiz.newLine);
			if (fWriterNotUsedFile != null) {
				for (final String docTagName : unusedTagNameList) {
					fWriterDocTagReportFile.write(docTagName);
					fWriterDocTagReportFile.write(TextBiz.newLine);
				}
			}

			fWriterDocTagReportFile.write(TextBiz.newLine);
			fWriterDocTagReportFile.write("-= DocTags Report (All) ");
			fWriterDocTagReportFile.write("Tags #: ");
			fWriterDocTagReportFile.write(String.valueOf(allKeys.size()));
			fWriterDocTagReportFile.write(" =-");
			fWriterDocTagReportFile.write(TextBiz.newLine);
			for (final String docTagName : allKeys) {
				fWriterDocTagReportFile.write(docTagName);
				fWriterDocTagReportFile.write(",");
			}

		}

		if (fWriterAllTagsCsv != null) {
			fWriterAllTagsCsv.flush();
			fWriterAllTagsCsv.close();
		}
		if (fWriterOutlineCsv != null) {
			fWriterOutlineCsv.flush();
			fWriterOutlineCsv.close();
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
		if (fWriterDocTagReportFile != null) {
			fWriterDocTagReportFile.flush();
			fWriterDocTagReportFile.close();
		}
		if (fWriterOther1File != null) {
			fWriterOther1File.flush();
			fWriterOther1File.close();
		}
		for (Map.Entry<String, BufferedWriter> entry : fWriterOthersList.entrySet()) {
			// System.out.println(entry.getKey() + ":" + entry.getValue());
			BufferedWriter bw = entry.getValue();
			if (bw != null) {
				bw.flush();
				bw.close();
			}
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

	private String fromListToCommaDelimString(final List<String> docTagList) {
		final StringBuilder sb = new StringBuilder();
		for (final String docTag : docTagList) {
			sb.append(docTag);
			sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	private void addToUsedTagFileList(final DocTagLine dttGL2, final DocTag docTag, final String fileName) {
		final String key = docTag.getName();
		if (StringUtils.isEmpty(key)) {
			LOGGER.error("Tried to add empty key to used tag file list?<" + dttGL2.getRawLine() + ">");
		} else {
			List<String> list = null;
			if (usedTagFileList.containsKey(key)) {
				list = usedTagFileList.get(key);
			} else {
				list = new ArrayList<>();
			}
			if (!list.contains(fileName)) {
				list.add(fileName);
			}
			usedTagFileList.put(key, list);
		}
	}

	private List<String> getDocTagsFromStringBlobToList(String docTagsTextBlob) {
		if (StringUtils.isEmpty(docTagsTextBlob)) {
			return new ArrayList<>();
		}
		docTagsTextBlob = docTagsTextBlob.replace("\r", "");
		docTagsTextBlob = docTagsTextBlob.replace("\n", "");
		docTagsTextBlob = docTagsTextBlob.replace("\t", "");
		docTagsTextBlob = docTagsTextBlob.replace(" ", "");
		final String[] strs = StringUtils.split(docTagsTextBlob, ", ");
		return Arrays.asList(strs);
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {

		// Change encoding?
		final Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("preHandler: Charset chosen: " + selCharSet);

		File outputCSVFileO = null;
		if (!StringUtils.isBlank(formatDao.getOutputOutlineFile())) {
			outputCSVFileO = new File(formatDao.getOutputOutlineFile());
			final File outputDirO = outputCSVFileO.getParentFile();
			if (outputDirO != null) {
				outputDirO.getParentFile().mkdirs();
				outputDirO.mkdirs();
			}
		}
		File outputCSVFileAll = null;
		if (!StringUtils.isBlank(formatDao.getOutputOutlineFile1())) {
			outputCSVFileAll = new File(formatDao.getOutputOutlineFile1());
			final File outputDirA = outputCSVFileAll.getParentFile();
			if (outputDirA != null) {
				outputDirA.getParentFile().mkdirs();
				outputDirA.mkdirs();
			}
		}

		// LOGGER.info("Writing data to " + outputFile1);
		if (outputCSVFileAll != null) {
			fWriterAllTagsCsv = new OutputStreamWriter(new FileOutputStream(outputCSVFileAll), selCharSet);
			// fWriterAll = new FileWriter(outputFileA, false);
			fWriterAllTagsCsv.write("Chpt,Line,Type,TagName,SubTagName,TagValue");
			fWriterAllTagsCsv.write(TextBiz.newLine);
		}

		if (outputCSVFileO != null) {
			fWriterOutlineCsv = new OutputStreamWriter(new FileOutputStream(outputCSVFileO), selCharSet);
			// fWriterOutline = new FileWriter(outputFileO, false);
			fWriterOutlineCsv.write("Chpt,Line,Type,TagName,SubTagName,TagValue");
			fWriterOutlineCsv.write(TextBiz.newLine);
		}

		formatOutlineFile(formatDao, selCharSet);
		formatOutputsFiles(formatDao, selCharSet);
		formatSceneFile(formatDao, selCharSet);

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

		File docTagReportFile = null;
		if (!StringUtils.isBlank(filePrefix))
			docTagReportFile = new File(inFilePath, filePrefix + "DocTagsReport.txt");
		else
			docTagReportFile = new File(inFilePath, "DocTagsReport.txt");

		fWriterDocTagReportFile = new BufferedWriter(
				new OutputStreamWriter(Files.newOutputStream(docTagReportFile.toPath(), StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING), selCharSet));
		// fWriterNotUsedFile = new BufferedWriter(notUsedFile, false);

		ldao.InitializeCount();

		// outlineTags.add("time");
		// outlineTags.add("loc");
		// outlineTags.add("date");
		// outlineTags.add("loc");
		// outlineTags.add("eventnote");
		List listOT = getDocTagsFromStringBlobToList(formatDao.getDocTagsOutlineExpandTags());
		outlineTags.addAll(listOT);
	}

	private void formatSceneFile(final FormatDao formatDao, final Charset selCharSet) throws IOException {
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
			fWriterSceneFile.write(END_STRING);
			fWriterSceneFile.write(TextBiz.newLine);
			// fWriterSceneFile.write("Captured Tags: ");
			// fWriterSceneFile.write(formatDao.getDocTagsSceneTags());
			// fWriterSceneFile.write(TextBiz.newLine);
			// fWriterSceneFile.write("Coalating Tags: ");
			// fWriterSceneFile.write(formatDao.getDocTagsSceneCoTags());
			// fWriterSceneFile.write(TextBiz.newLine);
			// // fWriterSceneFile.write(TextBiz.newLine);

			fWriterSceneFile.write("Captured Tags: ");
			final List<String> captured = getDocTagsFromStringBlobToList(formatDao.getDocTagsSceneTags());
			fWriterSceneFile.write(fromListToCommaDelimString(captured));
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write("Coalating Tags: ");
			final List<String> coalating = getDocTagsFromStringBlobToList(formatDao.getDocTagsSceneCoTags());
			fWriterSceneFile.write(fromListToCommaDelimString(coalating));
			fWriterSceneFile.write(TextBiz.newLine);
		}
	}

	private void formatOutputsFiles(final FormatDao formatDao, final Charset selCharSet) throws IOException {
		if (formatDao.getOutputs() != null && formatDao.getOutputs().size() > 0) {
			for (OtherDocTagData odtData : formatDao.getOutputs()) {
				LOGGER.debug("Other(s) file: '" + odtData.getFile() + "'");
				final File outputFileS2 = new File(odtData.getFile());
				final File outputDirS2 = outputFileS2.getParentFile();
				if (outputDirS2 != null) {
					outputDirS2.getParentFile().mkdirs();
					outputDirS2.mkdirs();
				}
				final BufferedWriter fWriterOthersFile = new BufferedWriter(
						new OutputStreamWriter(Files.newOutputStream(outputFileS2.toPath(), StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING), selCharSet));
				fWriterOthersFile.write("-= ");
				fWriterOthersFile.write(odtData.getName());
				fWriterOthersFile.write(": \"");
				fWriterOthersFile.write(formatDao.getStoryTitle1());
				fWriterOthersFile.write("\"");
				if (!StringUtils.isBlank(formatDao.getStoryTitle2())) {
					fWriterOthersFile.write(" : \"");
					fWriterOthersFile.write(formatDao.getStoryTitle2());
					fWriterOthersFile.write("\"");
				}
				fWriterOthersFile.write(END_STRING);
				fWriterOthersFile.write(TextBiz.newLine);
				fWriterOthersFile.write("Captured Tags: ");
				// fWriterOthersFile.write(odtData.getDocTags());
				// fWriterOthersFile.write(TextBiz.newLine);
				// fWriterOthersList.put(odtData.getName(), fWriterOthersFile);

				final List<String> captured = getDocTagsFromStringBlobToList(odtData.getDocTags());
				fWriterOthersFile.write(fromListToCommaDelimString(captured));
				fWriterOthersFile.write(TextBiz.newLine);
				fWriterOthersList.put(odtData.getName(), fWriterOthersFile);
			}
		}
	}

	private void formatOutlineFile(final FormatDao formatDao, final Charset selCharSet) throws IOException {
		if (!StringUtils.isBlank(formatDao.getOutputDocTagsOutlineFile())) {
			final File outputFileO2 = new File(formatDao.getOutputDocTagsOutlineFile());
			final File outputDirO2 = outputFileO2.getParentFile();
			if (outputDirO2 != null) {
				outputDirO2.getParentFile().mkdirs();
				outputDirO2.mkdirs();
			}

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
			fWriterOutlineFile.write(END_STRING);
			fWriterOutlineFile.write(TextBiz.newLine);

			fWriterOutlineFile.write("Expanded Tags: ");
			final List<String> expanded = getDocTagsFromStringBlobToList(formatDao.getDocTagsOutlineExpandTags());
			fWriterOutlineFile.write(fromListToCommaDelimString(expanded));
			fWriterOutlineFile.write(TextBiz.newLine);

			fWriterOutlineFile.write("Compressed Tags: ");
			final List<String> compressed = getDocTagsFromStringBlobToList(
					formatDao.getDocTagsOutlineCompressTagsAsString());
			fWriterOutlineFile.write(fromListToCommaDelimString(compressed));
			fWriterOutlineFile.write(TextBiz.newLine);

			// fWriterOutlineFile.write(formatDao.getDocTagsOutlineCompressTagsAsString());
			// fWriterOutlineFile.write(formatDao.getDocTagsOutlineExpandTags());
			// fWriterOutlineFile.write(TextBiz.newLine);
		}
	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao) {
		// tdao.copy(cdao);
		ldao.getChapters().add(new ChapterDao(ldao.getChaptCount()));
		// tdao = new CountDao();
	}

}
