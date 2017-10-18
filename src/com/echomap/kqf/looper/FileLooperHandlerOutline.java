package com.echomap.kqf.looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	private FileWriter fWriterOutlineFile;
	private FileWriter fWriterSceneFile;
	private FileWriter fWriterNotUsedFile;

	private int levelledCount = 0;
	private String lastLevelledText = null;

	final List<String> outlineTags = new ArrayList<String>();

	final Map<String, List<DocTag>> coalateTextMap = new TreeMap<String, List<DocTag>>();
	final List<DocTag> sceneDTList = new ArrayList<DocTag>();
	final List<DocTag> unusedTagList = new ArrayList<DocTag>();

	public FileLooperHandlerOutline() {
		//
	}

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
			outlineLine(ldao, cdao, formatDao);
		}
		cdao.addOneToNumLines();
	}

	private void writeChapterData(final FormatDao formatDao, final LooperDao ldao, final CountDao cdao)
			throws IOException {
		// format chapter number
		if (fWriterOutlineFile != null) {
			if (cdao.getChapterNumber() > 1)
				fWriterOutlineFile.write(TextBiz.newLine);

			fWriterOutlineFile
					.write("-= Chapter: " + cdao.getChapterNumber() + " (1." + cdao.getChapterNumber() + ") =-");
			fWriterOutlineFile.write(TextBiz.newLine);
		}
		if (fWriterSceneFile != null) {
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
		}
		coalateTextMap.clear();

		// TODO use sublist of doctags to put sub(s) into mains
		DocTag docTagLast = null;
		// boolean inScene = false;
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
				writeDataToFileWithCrop(val, formatDao, fWriterSceneFile, true);
			}

			docTagLast = docTag;
		}
		sceneDTList.clear();

		if (fWriterSceneFile != null) {
			if (cdao.getChapterNumber() > 1)
				fWriterSceneFile.write(TextBiz.newLine);

			fWriterSceneFile
					.write("-= Chapter: " + cdao.getChapterNumber() + " (1." + cdao.getChapterNumber() + ") =-");
			fWriterSceneFile.write(TextBiz.newLine);
		}
	}

	private void writeDataToFileWithCrop(final String val, final FormatDao formatDao, final FileWriter fWriterFile2,
			final boolean pad) throws IOException {
		int maxLineLen = 70; // param
		maxLineLen = formatDao.getDocTagsMaxLineLength();
		if (maxLineLen > 0 && val.length() > maxLineLen) {
			if (pad)
				fWriterFile2.write(TextBiz.newLine);

			final String[] words = val.split(" ");
			String pre = (pad ? "\t" : "");
			String mid = "";
			StringBuilder strToWrite = new StringBuilder();
			for (int j = 0; j < words.length; j++) {
				final String wd = words[j];
				if ((strToWrite.length() + wd.length()) > maxLineLen) {
					fWriterFile2.write(pre);
					fWriterFile2.write(strToWrite.toString());
					fWriterFile2.write(TextBiz.newLine);
					strToWrite.setLength(0);
					pre = "\t";
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
		} else {
			fWriterFile2.write(val);// docTag.getValue());
			fWriterFile2.write(TextBiz.newLine);
		}
	}

	private void outlineLine(final LooperDao ldao, final CountDao cdao, final FormatDao formatDao) throws IOException {
		DocTagLine dtt = TextBiz.isDocTag(ldao.getCurrentLine(), formatDao.getDocTagStart(), formatDao.getDocTagEnd());
		if (dtt.isHasDocTag()) {
			// if (dtt != TextBiz.DOCTAGTYPE.NONE) {
			final List<DocTag> docTags = dtt.getDocTags();
			if (docTags != null) {
				for (final DocTag docTag : docTags) {
					boolean wroteTag = false;
					if (outlineTags.contains(docTag.getName())) {
						wroteTag = true;
						writeEntryToCSV(fWriterOutline, docTag, cdao);
					}
					writeEntryToCSV(fWriterAll, docTag, cdao);

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
		}
	}

	private void addToCoalateTextMap(final DocTag docTag, final CountDao cdao, final FormatDao formatDao,
			final LooperDao ldao) {
		final String key = docTag.getName();
		List<DocTag> dts = coalateTextMap.get(key);
		if (dts == null)
			dts = new ArrayList<DocTag>();
		dts.add(docTag);
		coalateTextMap.put(key, dts);
	}

	private void writeEntryToOutline(final FileWriter fWriterL, final DocTag docTag, final CountDao cdao,
			final FormatDao formatDao) throws IOException {
		if (fWriterL == null)
			return;

		if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())) {
			if ("scene".compareToIgnoreCase(docTag.getName()) == 0) {
				this.levelledCount = 0;
			}
		}
		if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())) {
			if ("subscene".compareToIgnoreCase(docTag.getName()) == 0
					|| "scenesub".compareToIgnoreCase(docTag.getName()) == 0) {
				this.levelledCount = 1;
			}
		}

		if (formatDao.getDocTagsOutlineCompressTags().contains(docTag.getName())) {
			if (docTag.getName().startsWith("sub") || docTag.getName().endsWith("sub")) {
				fWriterL.write("\t");
				// lastLevelledText = docTag.getName();
			} else if (levelledCount == 1)
				fWriterL.write("\t\t");
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

	private void writeEntryToCSV(final Writer fWriterL, final DocTag docTag, final CountDao cdao) throws IOException {
		if (fWriterL == null)
			return;
		fWriterL.write("\"");
		fWriterL.write(docTag.getName());
		fWriterL.write("\",\"");
		fWriterL.write(docTag.getValue());
		fWriterL.write("\",");
		fWriterL.write(String.valueOf(cdao.getChapterNumber()));// chapterCount));
		fWriterL.write(",");
		fWriterL.write(String.valueOf(cdao.getNumLines()));// lineCount));
		fWriterL.write(TextBiz.newLine);
	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao) {

	}

	@Override
	public String postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {

		// todo formatDao.getVersion()
		// if (fWriterOutlineFile != null) {
		// fWriterOutlineFile.write(TextBiz.newLine);
		// fWriterOutlineFile.write(TextBiz.newLine);
		// if (unusedTagList.size() > 0) {
		// fWriterOutlineFile.write("Unused Tags: ");
		// for (DocTag dt1 : unusedTagList) {
		// fWriterOutlineFile.write(dt1.getName());
		// fWriterOutlineFile.write(",");
		// }
		// fWriterOutlineFile.write(TextBiz.newLine);
		// fWriterOutlineFile.write(TextBiz.newLine);
		// }
		// }

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
		return null;
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {

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
			fWriterAll = new OutputStreamWriter(new FileOutputStream(outputFileA), StandardCharsets.UTF_8);
			// fWriterAll = new FileWriter(outputFileA, false);
			fWriterAll.write("TagName,TagValue,Chpt,Line");
			fWriterAll.write(TextBiz.newLine);
		}

		if (outputFileO != null) {
			fWriterOutline = new OutputStreamWriter(new FileOutputStream(outputFileO), StandardCharsets.UTF_8);
			// fWriterOutline = new FileWriter(outputFileO, false);
			fWriterOutline.write("TagName,TagValue,Chpt,Line");
			fWriterOutline.write(TextBiz.newLine);
		}

		if (!StringUtils.isBlank(formatDao.getOutputDocTagsOutlineFile())) {
			final File outputFileO2 = new File(formatDao.getOutputDocTagsOutlineFile());
			final File outputDirO2 = outputFileO2.getParentFile();
			if (outputDirO2 != null) {
				outputDirO2.getParentFile().mkdirs();
				outputDirO2.mkdirs();
			}
			fWriterOutlineFile = new FileWriter(outputFileO2, false);
			//
			fWriterOutlineFile.write("-= Outline: ");
			fWriterOutlineFile.write(formatDao.getStoryTitle1());
			fWriterOutlineFile.write(" =-");
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.write("Compressed Tags: ");
			fWriterOutlineFile.write(formatDao.getDocTagsOutlineCompressTags());
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.write("Expanded Tags: ");
			fWriterOutlineFile.write(formatDao.getDocTagsOutlineExpandTags());
			fWriterOutlineFile.write(TextBiz.newLine);
			fWriterOutlineFile.write(TextBiz.newLine);
		}

		if (!StringUtils.isBlank(formatDao.getOutputDocTagsSceneFile())) {
			final File outputFileS2 = new File(formatDao.getOutputDocTagsSceneFile());
			final File outputDirS2 = outputFileS2.getParentFile();
			if (outputDirS2 != null) {
				outputDirS2.getParentFile().mkdirs();
				outputDirS2.mkdirs();
			}
			fWriterSceneFile = new FileWriter(outputFileS2, false);
			//
			fWriterSceneFile.write("-= Scenes: ");
			fWriterSceneFile.write(formatDao.getStoryTitle1());
			fWriterSceneFile.write(" =-");
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write("Captured Tags: ");
			fWriterSceneFile.write(formatDao.getDocTagsSceneTags());
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write("Coalating Tags: ");
			fWriterSceneFile.write(formatDao.getDocTagsSceneCoTags());
			fWriterSceneFile.write(TextBiz.newLine);
			fWriterSceneFile.write(TextBiz.newLine);
			//
		}

		final File inFile = new File(formatDao.getInputFilename());
		final File inFilePath = inFile.getParentFile();
		final File notUsedFile = new File(inFilePath, "DocTagsNotUsed.txt");
		fWriterNotUsedFile = new FileWriter(notUsedFile, false);

		ldao.InitializeCount();

		outlineTags.add("time");
		outlineTags.add("loc");
		outlineTags.add("Date");
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
