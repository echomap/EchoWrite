package com.echomap.kqf.biz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.FileLooperHandlerFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class KqfBiz {
	private final static Logger LOGGER = LogManager.getLogger(KqfBiz.class);

	public static Properties initializeAppProperies() {
		Properties appProps = new Properties();
		InputStream asdf = null;
		try {
			asdf = FileLooperHandlerFormatter.class.getClassLoader().getResourceAsStream("cwc.properties");
			if (asdf != null)
				appProps.load(asdf);
		} catch (IOException e) {
			e.printStackTrace();
			appProps.setProperty("version", "0.0.0");
		} finally {
			try {
				asdf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("Version: " + appProps.getProperty("version"));
		return appProps;
	}

	public static File getInitialFolder(final Preferences profileDataPref, final FormatDao formatDao) {
		final String profile = profileDataPref.get("titleOne", "");

		final String filePrefixText = formatDao.getFilePrefix();
		final String iStrFile = profileDataPref.get("inputFile", null);
		final File inFile = (StringUtils.isBlank(iStrFile) ? null : new File(iStrFile));
		final File inPFile = (inFile != null ? inFile.getParentFile() : null);
		final String outFilename = filePrefixText == null ? "/" + profile + "_more.json"
				: filePrefixText + profile + "_more.json";
		final File oFile = new File(inPFile, outFilename);
		final File oFold = oFile.getParentFile();
		LOGGER.debug("handleExport: oFold: " + oFold);
		LOGGER.debug("handleExport: oFile: " + oFile);
		LOGGER.debug("handleExport: outFilename: " + outFilename);
		LOGGER.debug("handleExport: inPFile: " + inPFile);
		LOGGER.debug("handleExport: iStrFile: " + iStrFile);
		LOGGER.debug("handleExport: filePrefixText: " + filePrefixText);
		return oFold;
	}

	public static JsonArray readSimpleJsonList(final JsonReader reader) throws IOException {
		LOGGER.debug("readList: Called");
		final JsonArray exportProfiles = new JsonArray();
		reader.beginArray();
		while (reader.hasNext()) {
			//
			final JsonObject dataset = new JsonObject();
			reader.beginObject();
			while (reader.hasNext()) {
				String objectName = reader.nextName();
				LOGGER.debug("readList: objectName = " + objectName);
				dataset.addProperty(objectName, reader.nextString());
			}
			reader.endObject();
			//
			LOGGER.debug("readList: dataset = " + dataset);
			exportProfiles.add(dataset);
		}
		reader.endArray();
		LOGGER.debug("readList: Done");
		return exportProfiles;
	}

}
