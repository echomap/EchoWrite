package com.echomap.kqf.biz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.OtherDocTagData;
import com.echomap.kqf.data.ProfileExportObj;
import com.echomap.kqf.two.gui.KQFCtrl;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class XferBiz {
	private final static Logger LOGGER = LogManager.getLogger(XferBiz.class);
	public static final String PROFILE_DATA = "profileData";
	public static final String PROFILE_DATA_OPTIONS = "options";
	// public static final String PROFILEOPTION_DATA = "profileOptionData";

	public static <T> List<T> loadDataListFromJson(final String listString, final Class T) {
		final Gson gson = new Gson();
		final Type listOfTestObject = new TypeToken<List<T>>() {
		}.getType();
		List<T> list2 = gson.fromJson(listString, listOfTestObject);
		// if (list2 != null) {
		// for (OtherDocTagData otherDocTagData : list2) {
		// LOGGER.debug("item: " + otherDocTagData);
		// }
		// }
		return list2;
	}

	@SuppressWarnings("unchecked")
	public static <T> Object loadDataFromJson(final JsonElement elem, final Class T) {
		// 1
		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		final String json = gson2.toJson(elem);
		LOGGER.debug("loadDataFromJson: json: " + json);
		// 2
		final Gson gson = new Gson();
		return (T) gson.fromJson(json, T);
	}

	@SuppressWarnings("unchecked")
	public static <T> Object loadDataFromJson(final String listString, final Class T) {
		final Gson gson = new Gson();
		return (T) gson.fromJson(listString, T);
	}

	public static List<OtherDocTagData> loadTableDataFromJson(final String listString) {
		final Gson gson = new Gson();
		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		List<OtherDocTagData> list2 = gson.fromJson(listString, listOfTestObject);
		if (list2 != null) {
			for (OtherDocTagData otherDocTagData : list2) {
				LOGGER.debug("item: " + otherDocTagData);
			}
		}
		return list2;
	}

	public static String loadTableDataToJson(final List<OtherDocTagData> listData) {
		// final Gson gson = new Gson();
		// final Type listOfTestObject = new TypeToken<String>() {
		// }.getType();
		// String list2 = gson.toJson(listData, listOfTestObject);
		// return list2;

		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// System.out.println(gson2.toJson(exportDataset));
		// LOGGER.debug(gson2.toJson(exportDataset));
		return gson2.toJson(listData);

	}

	public static JsonObject ProfileDataExportFromMemory(List<OtherDocTagData> targetList, final Properties appProps) {
		final JsonObject exportDataset = new JsonObject();
		if (appProps != null)
			exportDataset.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));
		LOGGER.debug("handleExport: exportDataset: " + exportDataset);
		final JsonArray exportList = new JsonArray();
		if (targetList != null) {
			for (OtherDocTagData odtd : targetList) {
				final JsonObject dataset = new JsonObject();
				dataset.addProperty("name", odtd.getName());
				dataset.addProperty("file", odtd.getFile());
				dataset.addProperty("docTags", odtd.getDocTags());

				/*
				 * TODO OPTIONS final JsonArray dsOptions = new JsonArray();
				 * final SortedMap<String, DocTagDataOption> options =
				 * odtd.getOptions(); final Set<String> keys = options.keySet();
				 * for (final String key : keys) { final DocTagDataOption dtdo =
				 * options.get(key);
				 * 
				 * final JsonObject dsOption = new JsonObject();
				 * dsOption.addProperty("name", dtdo.getName());
				 * dsOption.addProperty("prefix", dtdo.getPrefix());
				 * dsOption.addProperty("showCompress", dtdo.isShowCompress());
				 * dsOption.addProperty("showExpand", dtdo.isShowExpand());
				 * 
				 * dsOptions.add(dsOption); } dataset.add("options", dsOptions);
				 */
				// exportList.add(gson2.toJson(odtd,OtherDocTagData.class));
				exportList.add(dataset);
			}
		}
		exportDataset.add("list", exportList);
		return exportDataset;
	}

	// Used in export to File
	public static JsonArray ProfileDataOptionsExportFromMemory(List<OtherDocTagData> targetList,
			final Properties appProps) {
		final JsonArray exportList = new JsonArray();
		if (targetList != null) {
			for (OtherDocTagData odtd : targetList) {
				final JsonObject dsOptions = new JsonObject();
				dsOptions.addProperty("listname", odtd.getName());
				final JsonArray list = new JsonArray();
				dsOptions.add("list", list);

//				final SortedMap<String, DocTagDataOption> options = odtd.getOptions();
//				// final String joString = XferBiz.objectListToJson(options);
//				// dsOption.addProperty("options", joString);
//				final Set<String> keys = options.keySet();
//				for (final String key : keys) {
//					final JsonObject dsOption = new JsonObject();
//					final DocTagDataOption dtdo = options.get(key);
//					dsOption.addProperty("name", dtdo.getName());
//					dsOption.addProperty("prefix", dtdo.getPrefix());
//					dsOption.addProperty("showCompress", dtdo.isShowCompress());
//					dsOption.addProperty("showExpand", dtdo.isShowExpand());
//					// dsOption.addProperty("rawjson", joString);
//					list.add(dsOption);
//				}
//				exportList.add(dsOptions);
			}
		}
		return exportList;
	}

	public static String objectToJson(final Object obj) {
		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		String json = gson2.toJson(obj);
		return json;
	}

	@SuppressWarnings("rawtypes")
	public static String objectListToJson(final List objList) {
		// Convert the object to a JSON string
		String json = new Gson().toJson(objList);
		return json;
	}

	@SuppressWarnings("rawtypes")
	public static String objectListToJson(final Map objList) {
		// Convert the object to a JSON string
		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		String json = gson2.toJson(objList);
		return json;
	}

	public static String listToJson(JsonArray pdList) {
		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// System.out.println(gson2.toJson(exportDataset));
		// LOGGER.debug(gson2.toJson(exportDataset));
		return gson2.toJson(pdList);
		// for (int i = 0; i < pdList.size(); i++) {
		// JsonElement je = pdList.get(i);
		// JsonObject jo = je.getAsJsonObject();
		// final Gson gson = new Gson();
		// gson.to
		// }
		// return null;
	}

	// Create JSonArray of Profiles from Preferences
	public static JsonArray createJsonFromPrefsProfileData(final Preferences child, final Properties appProps)
			throws BackingStoreException {
		LOGGER.debug("createJsonFromPrefsProfileData: Called");
		final JsonObject albums = new JsonObject();
		if (appProps != null)
			albums.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));

		// create array
		final JsonArray profileDataArray = new JsonArray();
		try {
			// Profile names loop
			final String[] prefkeys = child.childrenNames();
			if (prefkeys != null && prefkeys.length > 0) {
				for (final String str1 : prefkeys) {
					if (StringUtils.isBlank(str1))
						continue;
					final JsonObject dataset = new JsonObject();
					loadProfileFromStringIntoDataset(child, appProps, str1, dataset);
					if (dataset.size() > 1) {
						// default export=true
						profileDataArray.add(dataset);
						LOGGER.debug("createJsonFromPrefsProfileData: loaded profile: '" + str1 + "'");
					} else
						LOGGER.warn("createJsonFromPrefsProfileData: skipped profile: '" + str1 + "'");
				}
			}
		} catch (

		BackingStoreException e) {
			// showPopupMessage("Error createProfileData: " + e, false);
			e.printStackTrace();
			throw e;
		}
		LOGGER.debug("createJsonFromPrefsProfileData: Done");
		return profileDataArray;
	}

	public static void loadProfileFromStringIntoDataset(final Preferences profilePref, final Properties appProps,
			final String nodeKey, final JsonObject dataset) {
		//
		final Preferences profile = profilePref.node(nodeKey);
		dataset.addProperty("export", true);

		try {
			final String[] ckeys = profile.keys();
			for (final String ckey : ckeys) {
				final String cval = profile.get(ckey, "");
				if (PROFILE_DATA.equals(ckey)) {
					LOGGER.debug("loadProfile: added profileData '" + ckey + "'='" + cval + "'");
					final List<OtherDocTagData> targetList = XferBiz.loadTableDataFromJson(cval);
					final JsonObject exportDataset = XferBiz.ProfileDataExportFromMemory(targetList, appProps);
					LOGGER.debug("loadProfile: exportProfileDataset: " + exportDataset);

					final JsonArray exportDataOptionsSet = XferBiz.ProfileDataOptionsExportFromMemory(targetList,
							appProps);
					LOGGER.debug("loadProfile: exportProfileDatasetOptions: " + exportDataOptionsSet);
					// TODO EXPORT LIST
					// exportDataset.add("optionslist", exportDataOptionsSet);
					dataset.add(PROFILE_DATA, exportDataset);
				} else {
					dataset.addProperty(ckey, cval);
					LOGGER.debug("loadProfile: added '" + ckey + "'='" + cval + "'");
				}
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
			LOGGER.error("Error in loadProfile", e);
		}
	}

	public static List<String> existingProfileNamesFromPrefsToList(final Preferences child, final Properties appProps)
			throws BackingStoreException {
		LOGGER.debug("loadProfileData: Called");
		// final ObservableList<ProfileExportObj> newList =
		// FXCollections.observableArrayList();
		JsonArray existingProfileDataArray = null;// createProfileData();
		// try {
		existingProfileDataArray = XferBiz.createJsonFromPrefsProfileData(child, appProps);
		// } catch (BackingStoreException e) {
		// showPopupMessage("Error createProfileData: " + e, false);
		// e.printStackTrace();
		// }

		final ArrayList<String> existingProfileNames = new ArrayList<>();
		for (int i = 0; i < existingProfileDataArray.size(); i++) {
			JsonElement je = existingProfileDataArray.get(i);
			JsonObject jo = je.getAsJsonObject();
			if (jo == null) {
				LOGGER.warn("loadProfileData: missing  '" + je + "'");
				continue;
			}
			if (!jo.has("titleOne")) {
				LOGGER.warn("loadProfileData: missing titleOne '" + jo + "'");
				continue;
			}
			final String name = jo.get("titleOne").getAsString();
			LOGGER.debug("loadProfileData: loaded row: '" + name + "'");
			existingProfileNames.add(name);
		}
		LOGGER.debug("loadProfileData: Done");
		return existingProfileNames;
	}

	public static File exportProfiles(final Charset selCharSet, final String fileName,
			final ObservableList<ProfileExportObj> targetList, final Preferences profilePref, final Properties appProps)
			throws IOException {
		LOGGER.debug("exportProfiles: Called");

		LOGGER.debug("exportProfiles: Charset chosen: " + selCharSet);
		Writer fWriterPlain = null;
		try {
			final File outputFilePlain = new File(fileName);
			LOGGER.info("Writing export file to: " + outputFilePlain);
			if (StringUtils.isEmpty(fileName)) {
				LOGGER.warn("No file set, can't export!");
				throw new IOException("No File set!");
			}
			// fWriterPlain = new FileWriter(outputFilePlain, false);
			fWriterPlain = new OutputStreamWriter(new FileOutputStream(outputFilePlain), selCharSet);

			final JsonObject exportDataset = new JsonObject();
			exportDataset.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));
			LOGGER.debug("exportProfiles: exportDataset: " + exportDataset);
			final JsonArray exportProfiles = new JsonArray();
			if (targetList != null) {
				for (ProfileExportObj data : targetList) {
					if (data.isExport()) {
						LOGGER.debug("exportProfiles: exporting profile: '" + data.getName() + "'");
						final JsonObject dataset = new JsonObject();
						// loadProfile(data.getName(), dataset);
						XferBiz.loadProfileFromStringIntoDataset(profilePref, appProps, data.getName(), dataset);
						LOGGER.debug("exportProfiles: dataset: " + dataset);
						exportProfiles.add(dataset);
					}
				}
			}
			exportDataset.add("profiles", exportProfiles);

			// create the gson using the GsonBuilder. Set pretty printing on.
			// Allow serializing null and set all fields to the Upper Camel Case
			final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
					.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			// System.out.println(gson2.toJson(exportDataset));
			// LOGGER.debug(gson2.toJson(exportDataset));
			fWriterPlain.write(gson2.toJson(exportDataset));
			// LOGGER.debug(gson2.toJson(exportProfiles));
			// final Gson gson = new Gson();
			// LOGGER.debug(gson.toJson(exportDataset));
			LOGGER.debug("exportProfiles: Done");
			return outputFilePlain;
			// showPopupMessage("Export Done! Written to '" + outputFilePlain +
			// "'", false);
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
	}

	public static List<OtherDocTagData> loadOutputsFromPrefs(final String key, final Preferences parent) {
		LOGGER.debug("loadOutputsFromPrefs: Called");
		List<OtherDocTagData> listODTD = null;
		if (key != null && parent != null) {
			LOGGER.debug("loadOutputsFromPrefs: key='" + key + "'");
			final Preferences child = parent.node(key);
			if (child != null) {
				listODTD = loadOutputsFromPrefs(child);
			}
		}
		LOGGER.debug("loadOutputsFromPrefs: Done");
		return listODTD;
	}

	private static List<OtherDocTagData> loadOutputsFromPrefs(final Preferences child) {
		LOGGER.debug("loadOutputsFromPrefs: Called for child");

		final String listString = child.get(PROFILE_DATA, "");
		final Gson gson = new Gson();

		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<OtherDocTagData> listODTD2 = new ArrayList();
		final List<OtherDocTagData> listODTD = gson.fromJson(listString, listOfTestObject);
		if (listODTD != null) {
			for (OtherDocTagData otherDocTagData : listODTD) {
				// LOGGER.debug("listODTD item: " + otherDocTagData);
				if (otherDocTagData.getName() != null && otherDocTagData.dataCheck()) {
					listODTD2.add(otherDocTagData);
					LOGGER.debug("listODTD item: " + otherDocTagData);
				} else
					LOGGER.debug("listODTD item: (BAD) " + otherDocTagData);
			}
		}
		return listODTD2;
	}

	public static ObservableList<OtherDocTagData> readInOtherDocTags(final JsonArray profileDataset) {
		final ObservableList<OtherDocTagData> newList = FXCollections.observableArrayList();

		for (int ii = 0; ii < profileDataset.size(); ii++) {
			final JsonElement je = profileDataset.get(ii);
			final JsonObject jo = je.getAsJsonObject();

			final String name = jo.get("name").getAsString();
			final String inputFile = jo.get("file").getAsString();
			final String docTags = jo.get("docTags").getAsString();

			// final String optionsJson = jo.get("optionsJson").getAsString();
			final JsonArray jsOptions = jo.get("options").getAsJsonArray();// TODO
			// final JsonArray jsOptions = (JsonArray) jsOptionsE;
			// final DocTagDataOption options

			LOGGER.debug("loadTableData: loaded row: '" + name + "'");
			final OtherDocTagData obj = new OtherDocTagData();
			obj.setName(name);
			obj.setFile(inputFile);
			obj.setDocTags(docTags);
			for (int j = 0; j < jsOptions.size(); j++) {
				final JsonElement elem = jsOptions.get(j);
				// final String json = elem.getAsString();
//				final DocTagDataOption option = (DocTagDataOption) XferBiz.loadDataFromJson(elem,
//						DocTagDataOption.class);
//				LOGGER.debug("loadTableData: option: " + option);
//				LOGGER.debug("loadTableData: obj: " + obj);

//				obj.addOption(option);
			}

			newList.add(obj);
		}
		return newList;
	}

	public static JsonArray readMoreExport(final File file) throws IOException {
		// final String stringData = readFile(inputFile.getText(), selCharSet)
		JsonArray profileDataset = null;
		String version = null;
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader(file));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("version")) {
					version = reader.nextString();
					LOGGER.debug("version=" + version);
				} else if (name.equals("list")) {
					// profileDataset = KqfBiz.readSimpleJsonList(reader);
					profileDataset = XferBiz.readMoreExport2(reader);
				} else {
					reader.skipValue(); // avoid some unhandle events
				}
			}

			return profileDataset;
		} finally {
			if (reader != null) {
				try {
					reader.endObject();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// From PROFILEDATA name, ie: version and list
	public static JsonArray readMoreExport2(final JsonReader reader) throws IOException {
		LOGGER.debug("readMoreExport2: Called");
		final JsonArray exportProfiles = new JsonArray();
		reader.beginArray();
		while (reader.hasNext()) {
			//
			final JsonObject dataset = new JsonObject();
			reader.beginObject();
			while (reader.hasNext()) {
				String objectName = reader.nextName();
				LOGGER.debug("readMoreExport2: objectName = " + objectName);
				if ("options".equalsIgnoreCase(objectName)) {
					final JsonArray dsOptions = readMoreExport3(reader);
					dataset.add("options", dsOptions);
				} else
					dataset.addProperty(objectName, reader.nextString());
			}
			reader.endObject();
			//
			LOGGER.debug("readMoreExport2: dataset = " + dataset);
			exportProfiles.add(dataset);
		}
		reader.endArray();
		LOGGER.debug("readMoreExport2: Done");
		return exportProfiles;
	}

	// TODO fix when OPTIONS is an array
	private static JsonArray readMoreExport3(final JsonReader reader) throws IOException {
		LOGGER.debug("readMoreExport3: Called");
		final JsonArray exportProfiles = new JsonArray();
		reader.beginArray();
		while (reader.hasNext()) {
			// reader.beginObject();
			// while (reader.hasNext()) {
			// String objectNewsName = reader.nextName();
			final JsonObject jo = readMoreExport4(reader);
			exportProfiles.add(jo);
			// }
			// reader.endObject();
		}
		reader.endArray();
		LOGGER.debug("readProfiles: Done");
		return exportProfiles;
	}

	// Read OPTIONS items, from input (file)
	private static JsonObject readMoreExport4(final JsonReader reader) throws IOException {
		LOGGER.debug("readMoreExport4: Called");
		final JsonObject dsOptions = new JsonObject();
		reader.beginObject();
		while (reader.hasNext()) {
			String objectName = reader.nextName();
			LOGGER.debug("readMoreExport4: objectName = " + objectName);
			if ("showCompress".equalsIgnoreCase(objectName) || "showExpand".equalsIgnoreCase(objectName)) {
				dsOptions.addProperty(objectName, reader.nextBoolean());
			} else
				dsOptions.addProperty(objectName, reader.nextString());
		}
		reader.endObject();
		LOGGER.debug("readMoreExport4: dsOptions = " + dsOptions);
		LOGGER.debug("readMoreExport4: Done");
		return dsOptions;
	}

	public static String getStringOrNullFromJsonObject(final JsonObject jo, final String key) {
		final JsonElement je = jo.get(key);
		if (je == null)
			return null;
		else
			return je.getAsString();
	}

	public static Boolean getBooleanOrNullFromJsonObject(final JsonObject jo, final String key) {
		final JsonElement je = jo.get(key);
		if (je == null)
			return null;
		else
			return je.getAsBoolean();
	}
}
