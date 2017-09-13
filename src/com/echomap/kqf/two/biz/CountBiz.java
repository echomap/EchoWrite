package com.echomap.kqf.two.biz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.two.data.FormatDao;

public class CountBiz extends BaseBiz {
	private final static Logger LOGGER = LogManager.getLogger(CountBiz.class);
	final static Properties props = new Properties();
	final static String newLine = System.getProperty("line.separator");
	final static String special1 = "* * * * * * * *";

	String docTagStart = "[[";
	String docTagEnd = "]]";

	protected CountBiz() {
		try {
			props.load(FormatBiz.class.getClassLoader().getResourceAsStream("cwc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			props.setProperty("version", "0.0.0");
		}
		LOGGER.info("Version: " + props.getProperty("version"));
	}

	boolean inSpecial = false;
	String chapterDivider = null;

	private void format(final String text, final String absolutePath, final String outputCountFile,
			final FormatDao formatDao) throws IOException {
		docTagStart = formatDao.getDocTagStart();
		docTagEnd = formatDao.getDocTagEnd();
		format(text, absolutePath, formatDao.getStoryTitle1(), formatDao.getStoryTitle2(),
				formatDao.getChapterDivider(), outputCountFile, true); // noseperate
	}

	private void format(final String inputFilename, final String outputDirName, final String storyTitle1,
			final String storyTitle2, final String chapterDivider, final String summaryOut, final boolean noseperate)
					throws IOException {
		LOGGER.info("Formatter...>");
		LOGGER.info("InputFilename:\t" + wrapString(inputFilename));
		LOGGER.info("OutputDirName:\t" + wrapString(outputDirName));
		LOGGER.info("StoryTitle1: \t" + wrapString(storyTitle1));
		LOGGER.info("StoryTitle2: \t" + wrapString(storyTitle2));
		LOGGER.info("ChapterDivider: " + wrapString(chapterDivider));
		LOGGER.info("summaryOut: \t" + wrapString(summaryOut));
		LOGGER.info("noseperate: \t'" + noseperate + "'");
		LOGGER.info("..>");

		final File outputDir = (noseperate ? null : new File(outputDirName));
		if (outputDir != null) {
			outputDir.getParentFile().mkdirs();
			outputDir.mkdirs();
		}

		final File inputFile;
		File inputFileTemp = new File(inputFilename);
		if (!inputFileTemp.exists()) {
			final int idx = inputFileTemp.getAbsolutePath().lastIndexOf(".");
			if (idx > 0) {
				String nFilename = inputFileTemp.getAbsolutePath().substring(0, idx);
				inputFile = new File(nFilename);
				LOGGER.info("InputFilename:\t" + wrapString(inputFile.getAbsolutePath()));
			} else
				inputFile = null;
		} else
			inputFile = inputFileTemp;

		if (inputFile == null || !inputFile.exists()) {
			throw new IOException("Input file " + wrapString(inputFile == null ? "null" : inputFile.getAbsolutePath())
					+ " does not exist!");
		}

		FileWriter fWriter = null;
		BufferedReader reader = null;
		try {
			if (!inputFile.exists() && inputFile.length() < 0) {
				LOGGER.error("The specified file does not exist");
			} else {
				final FileReader fr = new FileReader(inputFile);
				reader = new BufferedReader(fr);
				String st = "";
				inSpecial = false;
				int chapterCount = 1;
				int totalWordCount = 0;

				File outputFile = null;
				if (!noseperate) {
					String outFilename = String.valueOf(chapterCount) + ".txt";
					outputFile = new File(outputDir, outFilename);
					fWriter = new FileWriter(outputFile, false);
					// outputHeader(fWriter);
					fWriter.flush();
				}

				final List<CountDao> chapters = new ArrayList<CountDao>();
				CountDao tdao = new CountDao();
				final CountDao cdao = new CountDao();
				// System.out.println("Chapter\t\t\tWords\tTitle");
				while ((st = reader.readLine()) != null) {
					final ChapterDaoLoc chpt = isChapter(st);
					if (chpt.isChapter) {
						if (cdao.getChapterName() != null && cdao.getChapterName().length() > 0) {
							// System.out.println(cdao.getChapterName() + "\t\t"
							// + cdao.getNumWords() + "\t" + chpt.title);
							totalWordCount += cdao.getNumWords();
							tdao.copy(cdao);
							chapters.add(tdao);
							tdao = new CountDao();
							cdao.clear();
							chapterCount++;
						}
						cdao.setChapterName(chpt.name);
						cdao.setChapterTitle(chpt.title);
						cdao.setChapterNumber(chapterCount);

						if (fWriter != null) {
							fWriter.flush();
							fWriter.close();
							outputFile = new File(outputDir, String.valueOf(chapterCount) + ".txt");
							fWriter = new FileWriter(outputFile, false);
							fWriter.flush();
						}
					} else {
						countWords(st, cdao);
					}
					// st = st.replace("<", "&lt;");
					if (fWriter != null) {
						fWriter.write(st);
						fWriter.write(newLine);
					}
					// parseLine(st, fWriter, chapterDivider, storyTitle1,
					// storyTitle2);
					cdao.addOneToNumLines();
				}
				LOGGER.info("Total Words: " + totalWordCount);
				LOGGER.info("Version: " + props.getProperty("version"));

				tdao.copy(cdao);
				chapters.add(tdao);
				tdao = new CountDao();
				// SummaryOutputFile
				outputSummaryOutputFile(summaryOut, outputDir, chapters);

			}
		} finally {
			if (fWriter != null) {
				try {
					fWriter.flush();
				} catch (IOException e) {
				}
				fWriter.close();
			}
			if (reader != null) {
				reader.close();
			}
		}

	}

	private String wrapString(final String str) {
		if (null == str)
			return "<null>";
		return "'" + str + "'";
	}

	private void outputSummaryOutputFile(final String summaryOut, final File outputDir, final List<CountDao> chapters)
			throws IOException {
		FileWriter fWriter = null;
		try {
			Integer totalWords = 0;
			File summaryOutputFile = null;
			if (null != summaryOut && summaryOut.length() > 0) {
				summaryOutputFile = new File(summaryOut);
			} else {
				summaryOutputFile = new File(outputDir, "Chapter Count.csv");
			}
			LOGGER.info("Writing summary data to " + summaryOutputFile);
			fWriter = new FileWriter(summaryOutputFile, false);
			fWriter.write("Chapter,Words,Title,Chars,Lines");
			fWriter.write(newLine);
			for (CountDao countDao : chapters) {
				fWriter.write(countDao.getChapterName() + "," + countDao.getNumWords() + ","
						+ countDao.getChapterTitle() + "," + countDao.getNumChars() + "," + countDao.getNumLines());
				fWriter.write(newLine);
				totalWords += countDao.getNumWords();
			}
			fWriter.write(newLine);
			fWriter.write(newLine);
			fWriter.write(newLine);
			fWriter.write("TotalWords:,");
			fWriter.write(totalWords.toString());
			fWriter.write(newLine);
			fWriter.write("Version:, " + props.getProperty("version"));
		} finally {
			if (fWriter != null) {
				fWriter.flush();
				fWriter.close();
			}
		}
		LOGGER.debug("Chapter\t\t\tWords\tTitle");
		for (CountDao countDao : chapters) {
			LOGGER.debug(
					countDao.getChapterName() + "\t\t" + countDao.getNumWords() + "\t" + countDao.getChapterTitle());
		}

		// System.out.println(cdao.getChapterName() + "\t\t"
		// + cdao.getNumWords() + "\t" + chpt.title);
	}

	// enum LINETYPE {
	// PLAIN, CHAPTER, SECTION
	// };
	//
	// enum SECTIONTYPE {
	// PLAIN, INSPECIAL, NOTSPECIAL
	// };

	class ChapterDaoLoc {
		boolean isChapter = false;
		String name = null;
		int numerical = 0;
		String title = null;
	}

	private ChapterDaoLoc parseChapterName(final String line, final String div) {
		final ChapterDaoLoc dao = new ChapterDaoLoc();
		final int idxColon = line.indexOf(":");
		// String ret = line;
		StringBuffer divBuf = new StringBuffer();
		divBuf.append(div);
		StringBuffer divBufR = divBuf.reverse();
		String divR = divBufR.toString();
		String pre = line;
		String post = null;
		Integer num = null;
		if (idxColon != -1) {
			pre = line.substring(0, idxColon);
			post = line.substring(idxColon + 1);
		}
		if (pre == null) {
			pre = line;
		}

		if (div != null) {
			if (pre.startsWith(div))
				pre = pre.substring(div.length());
			else if (pre.indexOf(div) > -1)
				pre = pre.substring(pre.indexOf(div) + div.length());
			if (post.endsWith(divR))
				post = post.substring(0, post.length() - divR.length());
		}
		if (pre != null)
			pre = pre.trim();
		if (post != null)
			post = post.trim();

		final int idxSpcePre = pre.lastIndexOf(" ");
		if (idxSpcePre >= 1) {
			String temp = pre.substring(idxSpcePre);
			if (temp != null)
				temp = temp.trim();

			if (temp != null) {
				try {
					num = Integer.valueOf(temp);
				} catch (NumberFormatException e) {
					// e.printStackTrace();
					num = 0;
				}
			}
		}

		dao.name = pre;
		dao.isChapter = true;
		dao.numerical = num;
		dao.title = post;

		// return ret;
		return dao;
	}

	protected ChapterDaoLoc isChapter(final String line) {
		ChapterDaoLoc dao = new ChapterDaoLoc();
		if (line.startsWith("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, null);
			dao = parseChapterName(line, null);
			return dao;
		}
		if (line.contains("--") && line.contains("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, "--");
			dao = parseChapterName(line, "--");
			return dao;
		}
		if (line.contains("-=") && line.contains("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, "-=");
			dao = parseChapterName(line, "-=");
			return dao;
		}
		if (chapterDivider != null && line.contains(chapterDivider) && line.contains("Chapter")) {
			dao.isChapter = true;
			// dao.name = parseChapterName(line, chapterDivider);
			dao = parseChapterName(line, chapterDivider);
			return dao;
		}
		dao.isChapter = false;
		dao.name = null;
		return dao;
	}

	private void countWords(final String text, final CountDao dao) {
		boolean inWord = false;

		String text2 = cleanPlainText(text, "[[", "]]");

		final int len = text2.length();
		for (int i = 0; i < len; i++) {
			final char c = text2.charAt(i);
			dao.addOneToNumChars();
			switch (c) {
			case '\n':
			case '\t':
				break;
			case ' ':
				if (inWord) {
					dao.addOneToNumWords();
					inWord = false;
				}
				break;
			default:
				inWord = true;
			}
		}
		if (inWord)
			dao.addOneToNumWords();
	}

	// private String quote(final String st) {
	// return "\"" + st + "\"";
	// }

}
