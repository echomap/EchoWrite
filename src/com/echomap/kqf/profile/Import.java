package com.echomap.kqf.profile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.OtherDocTagData;
import com.echomap.kqf.export.ProfileExportObj;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import javafx.collections.ObservableList;

public class Import {
	private final static Logger LOGGER = LogManager.getLogger(Import.class);
	final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
			.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	// public static final String PROFILE_DATA = "profileData";
	// public static final String PROFILE_DATA_OPTIONS = "options";

	/**
	 * 
	 */
	public Import() {
		//
	}

	public List<String> doImportProfiles(final ObservableList<ProfileExportObj> items, final Properties appProps,
			final Preferences appPreferences, final ProfileManager profileManager) {
		LOGGER.debug("doImportProfiles: Called");
		final List<String> errorList = new ArrayList<>();

		if (items == null) {
			errorList.add("No list found");
			return errorList;
		}
		for (final ProfileExportObj data : items) {
			if (data.isExport()) {
				LOGGER.debug("doImportProfiles: importing profile: '" + data.getName() + "'");
				final Profile pObj = data.getProfile();
				LOGGER.debug("doImportProfiles: loaded row: '" + pObj.getKey() + "'");
				// validateProfile(pObj,errorList);
				if (StringUtils.isEmpty(pObj.getKey())) {
					errorList.add("Invalid Profile in import file: " + pObj);
					continue;
				}
				// IMPORT PROFILE
				Profile eProfile = profileManager.selectProfileByKey(pObj.getKey());
				if (eProfile != null) {
					LOGGER.debug("doImportProfiles: importing data into existing profile");
					// ?? copyDataIntoProfile(pObj, eProfile);
					eProfile = pObj;
				} else {
					eProfile = pObj;
				}
				// SAVE
				profileManager.saveProfileData(eProfile);
				if (profileManager.isWasError()) {
					errorList.addAll(profileManager.getMessages());
				}
			}
		}
		LOGGER.debug("doImportProfiles: Done");
		return errorList;
	}

	public List<Profile> readProfilesFromFile(final String filepath, final Properties appProps,
			final Preferences appPreferences, final ProfileManager profileManager) throws IOException {
		LOGGER.debug("doImportProfiles: Called");

		final Charset selCharSet = StandardCharsets.UTF_8;
		LOGGER.debug("doImportProfiles: Charset chosen: " + selCharSet);

		final File filePlain = new File(filepath);
		LOGGER.info("Reading file from: " + filePlain);
		if (filepath == null || filepath.length() < 1) {
			LOGGER.warn("No file set, can't import!");
			throw new IOException("Import Error! No File set!");
		}
		if (!filePlain.exists()) {
			LOGGER.warn("No file exists, can't import!");
			throw new IOException("Import Error! File doesn't exist!");
		}
		final JsonArray profileDataset = readInputData(filepath);
		final List<Profile> list = convertJSONToProfile(profileDataset);
		// TODO ?? Update from V1
		// for (final Profile profile : list) {
		// if(profile.getKey()==null)
		// profile.setKey(profile.gettit);
		// }
		LOGGER.debug("doImportProfiles: Done");
		// loadTableData();
		return list;
	}

	// private String listToJson(JsonArray pdList) {
	// return gson2.toJson(pdList);
	// }

	// //
	// https://stackoverflow.com/questions/47193364/using-gson-convert-java-object-into-jsonobject
	// private JsonObject convertProfileToJSON(final Profile selProfile) {
	// // final Gson gson2 = new
	// // GsonBuilder().setPrettyPrinting().serializeNulls()
	// // .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	// // Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'
	// // 'HH:mm:ss").create();
	// JsonElement jsonElement = gson2.toJsonTree(selProfile);
	// JsonObject jsonObject = (JsonObject) jsonElement;
	// return jsonObject;
	// }
	// //
	// https://stackoverflow.com/questions/47193364/using-gson-convert-java-object-into-jsonobject
	// private JsonObject convertProfileFromJSON(final JsonArray profileDataset)
	// {
	// JsonElement jsonElement = gson2.fromJson(json,
	// typeOfT)toJsonTree(selProfile);
	// JsonObject jsonObject = (JsonObject) jsonElement;
	// return jsonObject;
	// }

	// https://stackoverflow.com/questions/47193364/using-gson-convert-java-object-into-jsonobject
	private List<Profile> convertJSONToProfile(final JsonArray profileDataset) {
		final Type listOfTestObject = new TypeToken<List<Profile>>() {
		}.getType();
		final List<Profile> list = gson2.fromJson(profileDataset, listOfTestObject);
		return list;
	}

	// public static <T> List<T> loadDataListFromJson(final String listString,
	// final Class T) {
	// final Gson gson = new Gson();
	// final Type listOfTestObject = new TypeToken<List<T>>() {
	// }.getType();
	// List<T> list2 = gson.fromJson(listString, listOfTestObject);
	// return list2;
	// }

	// Read input data from file
	private JsonArray readInputData(final String filepath) throws IOException {
		JsonArray profileDataset = null;
		String version = null;
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader(filepath));
			// reader.setLenient(true);
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("version")) {
					version = reader.nextString();
					LOGGER.debug("version=" + version);
				} else if (name.equals("profiles")) {
					profileDataset = readProfiles(reader);
				} else {
					reader.skipValue(); // avoid some unhandle events
				}
			}
			return profileDataset;
			// } catch (IOException e) {
			// LOGGER.error(e);
			// showPopupMessage("Export Error!" + e.getMessage(), true);
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

	// Read PROFILE section, from input (file)
	private JsonArray readProfiles(final JsonReader reader) throws IOException {
		LOGGER.debug("readProfiles: Called");
		final JsonArray exportProfiles = new JsonArray();

		JsonParser parser = new JsonParser();
		JsonElement tree = parser.parse(reader);
		JsonArray array = tree.getAsJsonArray();
		for (JsonElement element : array) {
			if (element.isJsonObject()) {
				JsonObject car = element.getAsJsonObject();
				System.out.println("********************");
				System.out.println(car.get("Key").getAsString());
				System.out.println(car.get("MainTitle").getAsString());
				System.out.println(car.get("InputFile").getAsString());
				if (car.has("Outputs") && !car.get("Outputs").isJsonNull()) {
					final JsonArray cols = car.getAsJsonArray("Outputs");
					cols.forEach(col -> {
						System.out.println(col);
					});
				}
				exportProfiles.add(element);
			}
		}
		//
		// final JsonArray exportProfiles = new JsonArray();
		// reader.beginArray();
		// while (reader.hasNext()) {
		// // reader.beginObject();
		// // while (reader.hasNext()) {
		// // String objectNewsName = reader.nextName();
		// final JsonObject jo = readProfileItem(reader);
		// jo.addProperty("exists", false);
		// jo.addProperty("import", false);
		// exportProfiles.add(jo);
		// // }
		// // reader.endObject();
		// }
		// reader.endArray();
		LOGGER.debug("readProfiles: Done");
		return exportProfiles;
	}

	// TODO return data
	public Profile doImportMoreFiles(final String filepath, final Profile selectedProfile, final Properties appProps,
			final Preferences appPreferences, final ProfileManager profileManager) throws IOException {
		LOGGER.debug("doExpodoImportMoreFilesrtProfiles: Called");
		// List<Profile> list = null;
		// File outputFilePlain = null;
		// TODO set CharSet as param from prefs?
		final Charset selCharSet = StandardCharsets.UTF_8;
		LOGGER.debug("importProfiles: Charset chosen: " + selCharSet);

		final File filePlain = new File(filepath);
		LOGGER.info("Reading file from: " + filePlain);
		if (filepath == null || filepath.length() < 1) {
			LOGGER.warn("No file set, can't import!");
			throw new IOException("Import Error! No File set!");
		}
		if (!filePlain.exists()) {
			LOGGER.warn("No file exists, can't import!");
			throw new IOException("Import Error! File doesn't exist!");
		}

		//
		JsonReader reader = null;
		String version = null;
		JsonArray moreDataset = new JsonArray();
		try {
			reader = new JsonReader(new FileReader(filepath));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("version")) {
					version = reader.nextString();
					LOGGER.debug("version=" + version);
				} else if (name.equals("Outputs")) {
					moreDataset = readOutputs(reader);
				} else {
					reader.skipValue(); // avoid some unhandle events
				}
			}
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

		// final List<OtherDocTagData> outputlist =
		// selectedProfile.getOutputs();

		for (final JsonElement jsonElementMore : moreDataset) {
			JsonObject jsomobj = jsonElementMore.getAsJsonObject();
			final OtherDocTagData newOther = new OtherDocTagData();
			newOther.setDocTags(jsomobj.get("DocTags").getAsString());
			newOther.setFile(jsomobj.get("File").getAsString());
			newOther.setName(jsomobj.get("Name").getAsString());
			selectedProfile.addOutput(newOther);
		}

		// final JsonArray profileDataset = readInputData(filepath);
		// final List<Profile> list = convertJSONToProfile(profileDataset);
		// TODO ?? Update from V1
		// for (final Profile profile : list) {
		// if(profile.getKey()==null)
		// profile.setKey(profile.gettit);
		// }
		LOGGER.debug("doImportProfiles: Done");
		// loadTableData();
		return selectedProfile;
	}

	private JsonArray readOutputs(final JsonReader reader) {
		final JsonArray exportProfiles = new JsonArray();

		JsonParser parser = new JsonParser();
		JsonElement tree = parser.parse(reader);
		JsonArray array = tree.getAsJsonArray();
		for (JsonElement element : array) {
			if (element.isJsonObject()) {
				JsonObject car = element.getAsJsonObject();
				System.out.println("********************");
				if (car.get("Name") != null)
					System.out.println(car.get("Name").getAsString());
				if (car.get("File") != null)
					System.out.println(car.get("File").getAsString());
				if (car.get("DocTags") != null)
					System.out.println(car.get("DocTags").getAsString());
				// if (car.has("Outputs") && !car.get("Outputs").isJsonNull()) {
				// final JsonArray cols = car.getAsJsonArray("Outputs");
				// cols.forEach(col -> {
				// System.out.println(col);
				// });
				// }
				exportProfiles.add(element);
			}
		}
		return exportProfiles;
	}

	// Read PROFILE section items, from input (file)
	// private JsonObject readProfileItem(final JsonReader reader) throws
	// IOException {
	// LOGGER.debug("readProfileItem: Called");
	// final JsonObject dataset = new JsonObject();
	// reader.beginObject();
	// while (reader.hasNext()) {
	// final String objectNewsDataName = reader.nextName();
	// LOGGER.debug("readProfileItem: objectNewsDataName: '" +
	// objectNewsDataName + "'");
	// if (reader.peek() == JsonToken.NULL) {
	// LOGGER.debug("readProfileItem: value is null, skipping");
	// // TODO ?? dataset.addProperty(objectNewsDataName, null);
	// reader.nextNull(); // note on this
	// } else if (objectNewsDataName.equals("export")) {
	// reader.skipValue();
	// } else if (objectNewsDataName.equals("filePrefixCheckbox")) {
	// dataset.addProperty(objectNewsDataName, reader.nextBoolean());
	// } else if (objectNewsDataName.equals("AppendUnderscoreToPrefix")) {
	// dataset.addProperty(objectNewsDataName, reader.nextBoolean());
	// } else if (objectNewsDataName.startsWith("Cb")) {
	// dataset.addProperty(objectNewsDataName, reader.nextBoolean());
	// } else if (objectNewsDataName.startsWith("Want")) {
	// dataset.addProperty(objectNewsDataName, reader.nextBoolean());
	// } else if (objectNewsDataName.equals("profileData")) {
	// final JsonObject pdata = readProfileDataSection(reader);
	// final JsonArray pdList = pdata.getAsJsonArray("list");
	// // final JsonArray profileDataset =
	// // readMoreExport2(reader);
	// final String joString = listToJson(pdList);
	// dataset.addProperty("profileData", joString);
	// } else if (objectNewsDataName.equals("Outputs")) {
	// final JsonArray profileDataset = readMoreExport2(reader);
	// dataset.add("outputs", profileDataset);
	// } else {
	// dataset.addProperty(objectNewsDataName, reader.nextString());
	// }
	// }
	// LOGGER.debug("readProfileItem: done loop");
	// reader.endObject();
	// LOGGER.debug("readProfileItem: dataset = " + dataset);
	// LOGGER.debug("readProfileItem: Done");
	// return dataset;
	// }

	// private JsonObject readProfileDataSection(JsonReader reader) throws
	// IOException {
	// LOGGER.debug("readProfileData: Called");
	//
	// final JsonObject profileDataObj = new JsonObject();
	// reader.beginObject();
	// while (reader.hasNext()) {
	// String name = reader.nextName();
	// if (name.equals("version")) {
	// final String version = reader.nextString();
	// LOGGER.debug("version=" + version);
	// profileDataObj.addProperty("version", version);
	// } else if (name.equals("list")) {
	// // profileDataset = readProfileDataItem(reader);
	// // final JsonArray pdList = pdata.getAsJsonArray("list");
	// final JsonArray profileDataset = readMoreExport2(reader);
	// profileDataObj.add("list", profileDataset);
	// } else {
	// reader.skipValue(); // avoid some unhandle events
	// }
	// }
	// reader.endObject();
	// LOGGER.debug("readProfileData: dataset = " + profileDataObj);
	// LOGGER.debug("readProfileData: Done");
	// return profileDataObj;
	// }

	// From PROFILEDATA name, ie: version and list
	// private JsonArray readMoreExport2(final JsonReader reader) throws
	// IOException {
	// LOGGER.debug("readMoreExport2: Called");
	// final JsonArray exportProfiles = new JsonArray();
	// reader.beginArray();
	// while (reader.hasNext()) {
	// //
	// final JsonObject dataset = new JsonObject();
	// reader.beginObject();
	// while (reader.hasNext()) {
	// String objectName = reader.nextName();
	// LOGGER.debug("readMoreExport2: objectName = " + objectName);
	// if ("options".equalsIgnoreCase(objectName)) {
	// final JsonArray dsOptions = readMoreExport3(reader);
	// dataset.add("options", dsOptions);
	// } else
	// dataset.addProperty(objectName, reader.nextString());
	// }
	// reader.endObject();
	// //
	// LOGGER.debug("readMoreExport2: dataset = " + dataset);
	// exportProfiles.add(dataset);
	// }
	// reader.endArray();
	// LOGGER.debug("readMoreExport2: Done");
	// return exportProfiles;
	// }

	// fix when OPTIONS is an array
	// private JsonArray readMoreExport3(final JsonReader reader) throws
	// IOException {
	// LOGGER.debug("readMoreExport3: Called");
	// final JsonArray exportProfiles = new JsonArray();
	// reader.beginArray();
	// while (reader.hasNext()) {
	// final JsonObject jo = readMoreExport4(reader);
	// exportProfiles.add(jo);
	// }
	// reader.endArray();
	// LOGGER.debug("readProfiles: Done");
	// return exportProfiles;
	// }

	// Read OPTIONS items, from input (file)
	// private JsonObject readMoreExport4(final JsonReader reader) throws
	// IOException {
	// LOGGER.debug("readMoreExport4: Called");
	// final JsonObject dsOptions = new JsonObject();
	// reader.beginObject();
	// while (reader.hasNext()) {
	// String objectName = reader.nextName();
	// LOGGER.debug("readMoreExport4: objectName = " + objectName);
	// if ("showCompress".equalsIgnoreCase(objectName) ||
	// "showExpand".equalsIgnoreCase(objectName)) {
	// dsOptions.addProperty(objectName, reader.nextBoolean());
	// } else
	// dsOptions.addProperty(objectName, reader.nextString());
	// }
	// reader.endObject();
	// LOGGER.debug("readMoreExport4: dsOptions = " + dsOptions);
	// LOGGER.debug("readMoreExport4: Done");
	// return dsOptions;
	// }

}
