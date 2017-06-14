package com.echomap.kqf.biz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FormatBiz {

	final static String newLine = System.getProperty("line.separator");

	final static String special1 = "* * * * * * * *";
	// final static String special2 = "* * * * * * * *";

	boolean inSpecial = false;

	public void format(final String inputFilename, final String outputFilename,
			final String storyTitle1, final String storyTitle2)
			throws IOException {
		this.format(inputFilename, outputFilename, storyTitle1, storyTitle2,
				null, null);
	}

	public void format(final String inputFilename, final String outputFilename,
			final String storyTitle1, final String storyTitle2,
			final String chapterDivider, final String centerablevalues)
			throws IOException {
		System.out.println("Formatter...>");
		System.out.println("InputFilename: '" + inputFilename + "'");
		System.out.println("OutputFilename: '" + outputFilename + "'");
		System.out.println("StoryTitle1: '" + storyTitle1 + "'");
		System.out.println("StoryTitle2: '" + storyTitle2 + "'");
		System.out.println("ChapterDivider: '" + chapterDivider + "'");
		System.out.println("Centerablevalues: '" + centerablevalues + "'");
		System.out.println("..>");

		final File inputFile = new File(inputFilename);
		final File outputFile = new File(outputFilename);

		// Create dirs
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		FileWriter fWriter = null;
		BufferedReader reader = null;
		try {
			fWriter = new FileWriter(outputFile, false);

			if (!inputFile.exists() && inputFile.length() < 0) {
				System.out.println("The specified file does not exist");
			} else {

				outputHeader(fWriter);
				fWriter.flush();

				final FileReader fr = new FileReader(inputFile);
				reader = new BufferedReader(fr);
				String st = "";
				inSpecial = false;
				while ((st = reader.readLine()) != null) {
					writeLine(st, fWriter, storyTitle1, storyTitle2);
				}

				outputFooter(fWriter);
				fWriter.flush();
			}
		} finally {
			if (fWriter != null) {
				fWriter.flush();
				fWriter.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

	private String filterLine(final String st, final String storyTitle1) {

		if (st.indexOf("@TITLE@") > -1) {
			return st.replace("@TITLE@", storyTitle1);
		}
		return st;
	}

	enum LINETYPE {
		PLAIN, CHAPTER, SECTION
	};

	enum SECTIONTYPE {
		PLAIN, INSPECIAL, NOTSPECIAL
	};

	private boolean isChapter(final String line) {
		if (line.startsWith("Chapter"))
			return true;
		if (line.contains("--") && line.contains("Chapter"))
			return true;
		if (line.contains("-=") && line.contains("Chapter"))
			return true;
		return false;
	}

	private SECTIONTYPE isSection(final String line) {
		if (line.startsWith("Section")) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoSection>";
			return SECTIONTYPE.PLAIN;
		} else if (line.startsWith(special1) && !inSpecial) {
			// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoPlainText>";
			return SECTIONTYPE.NOTSPECIAL;
		} else if (line.indexOf("Section") > -1 && inSpecial) {
			// preTag = "<p class=MsoSection>";
			return SECTIONTYPE.INSPECIAL;
		}
		return null;
	}

	private void writeLine(String st, final FileWriter fWriter,
			final String storyTitle1, final String storyTitle2)
			throws IOException {
		st = st.replace("<", "&lt;");
		String preTag = "";
		String postTag = null;
		// String postTag = "";
		if (isChapter(st)) {
			preTag = "<mbp:pagebreak/>\n" + "<p class=\"MsoChapter\">";
			postTag = "<p class=\"MsoPlainText\">&nbsp;</p>";
		} else {
			SECTIONTYPE sectionType = isSection(st);
			if (sectionType != null)
				System.out.println("sectionType: " + sectionType
						+ " for line: " + st);
			if (sectionType != null) {
				switch (sectionType) {
				case PLAIN:
					preTag = "<mbp:pagebreak/>\n" + "<p class=\"MsoSection\">";
					break;
				case INSPECIAL:
					preTag = "<p class=MsoSection>";
					break;
				case NOTSPECIAL:
					preTag = "<mbp:pagebreak/>\n"
							+ "<p class=\"MsoPlainText\">";
					break;
				default:
					break;
				}
			}
		}
		if (st.compareTo(storyTitle1) == 0 || st.compareTo(storyTitle2) == 0) {
			preTag = "<p class=\"MsoTitle\">";
		}
		if (preTag == null || preTag.length() < 1) {
			preTag = "<p class=\"MsoPlainText\">";
		}

		// if (isChapter(st)) {
		// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoChapter>";
		// } else if (st.startsWith("Section")) {
		// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoSection>";
		// } else if (st.startsWith(special1) && !inSpecial) {
		// preTag = "<mbp:pagebreak/>\n" + "<p class=MsoPlainText>";
		// } else if (st.indexOf("Section") > -1 && inSpecial) {
		// preTag = "<p class=MsoSection>";
		// } else {
		// preTag = "<p class=MsoPlainText>";
		// }
		// if (st.compareTo(storyTitle1) == 0 || st.compareTo(storyTitle2) == 0)
		// {
		// preTag = "<p class=MsoTitle>";
		// }
		filterLine(st, storyTitle1);

		if (st.startsWith(special1)) {
			if (inSpecial) {
				System.out.println("inSpecial flipped to false on: " + st);
				// System.out.println("preTag: " + preTag);
				inSpecial = false;
			} else {
				inSpecial = true;
				System.out.println("inSpecial flipped to true on: " + st);
			}
		}
		// System.out.println("inSpecial: " +inSpecial);

		fWriter.write(preTag);
		fWriter.write(st);
		fWriter.write("&nbsp;</p>");
		fWriter.write(newLine);
		if (postTag != null) {
			fWriter.write(postTag);
			fWriter.write(newLine);
		}
	}

	private boolean outputFooter(final FileWriter fWriter) {
		System.out.println("Outputting file: footer.html");
		return outputFromFile(fWriter, "/footer.html");
	}

	private boolean outputHeader(final FileWriter fWriter) {
		System.out.println("Outputting file: header.html");
		return outputFromFile(fWriter, "/header.html");
	}

	private boolean outputFromFile(final FileWriter fWriter,
			final String cpFilename) {
		try {
			final InputStream inputStream = this.getClass()
					.getResourceAsStream(cpFilename);
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				fWriter.write(sCurrentLine);
				fWriter.write(newLine);
			}
			fWriter.flush();
			br.close();
			inputStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
