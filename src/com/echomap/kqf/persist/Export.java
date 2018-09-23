package com.echomap.kqf.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.data.ProfileExportObj;
import com.echomap.kqf.two.gui.KQFCtrl;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.collections.ObservableList;

public class Export {
	private final static Logger LOGGER = LogManager.getLogger(Export.class);
	// final ProfileManager profileManager;

	// public Export(final ProfileManager profileManager) {
	// this.profileManager = profileManager;
	// }
	final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
			.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

	public File doExportProfiles(final String fileName, final ObservableList<ProfileExportObj> exportMetadata,
			final Properties appProps, final Preferences appPreferences, final ProfileManager profileManager)
					throws IOException {
		LOGGER.debug("doExportProfiles: Called");

		File outputFilePlain = null;
		// TODO set CharSet as param from prefs?
		final Charset selCharSet = StandardCharsets.UTF_8;
		LOGGER.debug("exportProfiles: Charset chosen: " + selCharSet);
		Writer fWriterPlain = null;
		try {
			outputFilePlain = new File(fileName);
			LOGGER.info("Writing export file to: " + outputFilePlain);
			if (StringUtils.isEmpty(fileName)) {
				LOGGER.warn("No file set, can't export!");
				throw new IOException("No File set!");
			}
			fWriterPlain = new OutputStreamWriter(new FileOutputStream(outputFilePlain), selCharSet);

			final JsonObject exportDataset = new JsonObject();
			exportDataset.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));
			LOGGER.debug("exportProfiles: exportDataset: " + exportDataset);
			final JsonArray exportProfiles = new JsonArray();
			if (exportMetadata != null) {
				for (ProfileExportObj data : exportMetadata) {
					if (data.isExport()) {
						LOGGER.debug("exportProfiles: exporting profile: '" + data.getName() + "'");
						// final JsonObject dataset = new JsonObject();
						final Profile selProfile = profileManager.selectProfileByKey(data.getKey());
						final JsonObject dataset = convertProfileToJSON(selProfile);
						// // loadProfile(data.getName(), dataset);
						// XferBiz.loadProfileFromStringIntoDataset(profilePref,
						// appProps, data.getName(), dataset);
						// LOGGER.debug("exportProfiles: dataset: " + dataset);
						exportProfiles.add(dataset);
					}
				}
			}
			exportDataset.add("profiles", exportProfiles);

			// create the gson using the GsonBuilder. Set pretty printing on.
			// Allow serializing null and set all fields to the Upper Camel Case
			// final Gson gson2 = new
			// GsonBuilder().setPrettyPrinting().serializeNulls()
			// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			// System.out.println(gson2.toJson(exportDataset));
			// LOGGER.debug(gson2.toJson(exportDataset));
			fWriterPlain.write(gson2.toJson(exportDataset));
			//
		} catch (IOException e) {
			LOGGER.error(e);
			throw e;
			// showPopupMessage("Export Error!" + e.getMessage(), true);
		} finally {
			if (fWriterPlain != null)
				try {
					fWriterPlain.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		LOGGER.debug("doExportProfiles: Done");
		return outputFilePlain;
	}

	//https://stackoverflow.com/questions/47193364/using-gson-convert-java-object-into-jsonobject
	private JsonObject convertProfileToJSON(final Profile selProfile) {
		// final Gson gson2 = new
		// GsonBuilder().setPrettyPrinting().serializeNulls()
		// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'
		// 'HH:mm:ss").create();
		JsonElement jsonElement = gson2.toJsonTree(selProfile);
		JsonObject jsonObject = (JsonObject) jsonElement;
		return jsonObject;
	}

	// private <T> List<T> jsonStringToJsonObject(final String listString, final
	// Class T) {
	// final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
	// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	// final Type listOfTestObject = new TypeToken<List<T>>() {
	// }.getType();
	// final List<T> list2 = gson2.fromJson(listString, listOfTestObject);
	// return list2;
	// }
	//
	// private <T> List<T> objectToJsonObject(final Object obj, final Class T) {
	// final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
	// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	// String jsonString = gson2.toJson(obj);
	// final List<T> list = jsonStringToJsonObject(jsonString, T);
	// return list;
	// }
	//
	// private String objectToJsonString(final Object obj) {
	// final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
	// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	// String json = gson2.toJson(obj);
	// return json;
	// }

}
