package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.XferBiz;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.ProfileExportObj;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

public class KQFSubImportCtrl extends KQFSubBaseExportCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFSubImportCtrl.class);

	private JsonArray profileDataset = null;
	private String version = null;

	public void setImportData(final Preferences child, final FormatDao formatDao, final Properties appProps,
			final Stage stage, final File lastSelectedDirectory) {
		this.profileDataPrefs = child;
		this.formatDao = formatDao;
		this.appProps = appProps;
		this.primaryStage = stage;
		this.lastSelectedDirectory = lastSelectedDirectory;
		// createProfileData();
		// loadTableData();
	}

	@SuppressWarnings("unchecked")
	@FXML
	void initialize() {
		LOGGER.debug("initialize: Called");
		final TableColumn<Object, Object> firstCol = new TableColumn<Object, Object>("Import?");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("export"));
		final TableColumn<Object, Object> secondCol = new TableColumn<Object, Object>("Exists?");
		secondCol.setCellValueFactory(new PropertyValueFactory<>("exists"));
		final TableColumn<Object, Object> thirdCol = new TableColumn<Object, Object>("Name");
		thirdCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn<Object, Object> forthCol = new TableColumn<Object, Object>("Input File");
		forthCol.setCellValueFactory(new PropertyValueFactory<>("inputFile"));

		//
		inputTable.getColumns().clear();
		inputTable.getColumns().addAll(firstCol, secondCol, thirdCol);

		inputTable.setRowFactory(new Callback<TableView<ProfileExportObj>, TableRow<ProfileExportObj>>() {
			@Override
			public TableRow<ProfileExportObj> call(TableView<ProfileExportObj> tableView) {
				final TableRow<ProfileExportObj> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final ProfileExportObj rowData = row.getItem();
							LOGGER.debug("rowData: left2: " + rowData);
							// inputName.setText(rowData.getName());
							rowData.setExport(!rowData.isExport());
						} else if (event.isSecondaryButtonDown()) {
							// right click code here
							final ProfileExportObj rowData = row.getItem();
							LOGGER.debug("rowData: right: " + rowData);
							rowData.setExport(true);
						}
						inputTable.refresh();
					}
				});
				return row;
			}
		});

		LOGGER.debug("initialize: Done");
	}

	public void handleBrowse(final ActionEvent event) {
		LOGGER.debug("handleBrowse: Called");
		locateFile(event, "Import File", inputFile);
		// chooseFile(event, "Export File", inputFile, "ProfileExport.json");
	}

	public void handleImportProfiles(final ActionEvent event) {
		LOGGER.debug("handleImportProfiles: Called");
		@SuppressWarnings("unchecked")
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				if (data.isExport()) {
					LOGGER.debug("handleImportProfiles: importing profile: '" + data.getName() + "'");
					final JsonObject jo = data.getPayload();
					// final boolean export = jo.get("export").getAsBoolean();
					final String eName = jo.get("titleOne").getAsString();
					// final String eInputFile =
					// jo.get("inputFile").getAsString();
					LOGGER.debug("handleImportProfiles: loaded row: '" + eName + "'");
					// TODO IMPORT PROFILE
					// final boolean eExists = jo.get("exists").getAsBoolean();
					// final boolean eIimport = jo.get("import").getAsBoolean();
					try {
						Preferences child = null;
						child = getPrefs().node(eName);
						final Set<String> keys = jo.keySet();
						for (final String key : keys) {
							final JsonElement je = jo.get(key);
							final String val = je.getAsString();
							child.put(key, val);
						}
						LOGGER.info("handleImportProfiles: saved profile: '" + eName + "'");
						getPrefs().flush();
						showPopupMessage("Imported Selected Profiles", false);
					} catch (BackingStoreException e) {
						showPopupMessage("Error with ImportProfiles: " + e, false);
						e.printStackTrace();
					}
				}
			}
		}
		LOGGER.debug("handleImportProfiles: Done");
	}

	public void handleImportFile(final ActionEvent event) {
		LOGGER.debug("handleImportFile: Called");

		final Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("handleImportFile: Charset chosen: " + selCharSet);

		final File filePlain = new File(inputFile.getText());
		LOGGER.info("Reading file from: " + filePlain);
		if (inputFile.getText() == null || inputFile.getText().length() < 1) {
			LOGGER.warn("No file set, can't import!");
			showPopupMessage("Import Error! No File set!", true);
			return;
		}
		if (!filePlain.exists()) {
			LOGGER.warn("No file exists, can't import!");
			showPopupMessage("Import Error! File doesn't exist!", true);
			return;
		}
		readInputData();

		//
		LOGGER.debug("handleImportFile: Done");
		loadTableData();
	}

	// Read input data from file
	private void readInputData() {
		profileDataset = null;
		version = null;
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader(inputFile.getText()));
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
		} catch (IOException e) {
			LOGGER.error(e);
			showPopupMessage("Export Error!" + e.getMessage(), true);
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
		reader.beginArray();
		while (reader.hasNext()) {
			// reader.beginObject();
			// while (reader.hasNext()) {
			// String objectNewsName = reader.nextName();
			final JsonObject jo = readProfileItem(reader);
			jo.addProperty("exists", false);
			jo.addProperty("import", false);
			exportProfiles.add(jo);
			// }
			// reader.endObject();
		}
		reader.endArray();
		LOGGER.debug("readProfiles: Done");
		return exportProfiles;
	}

	// Read PROFILE section items, from input (file)
	private JsonObject readProfileItem(final JsonReader reader) throws IOException {
		LOGGER.debug("readProfile: Called");
		final JsonObject dataset = new JsonObject();
		reader.beginObject();
		while (reader.hasNext()) {
			final String objectNewsDataName = reader.nextName();
			LOGGER.debug("readProfile: objectNewsDataName: '" + objectNewsDataName + "'");
			if (objectNewsDataName.equals("export")) {
				reader.skipValue();
			} else if (objectNewsDataName.equals("filePrefixCheckbox")) {
				dataset.addProperty(objectNewsDataName, reader.nextBoolean());
			} else if (objectNewsDataName.equals(XferBiz.PROFILE_DATA)) {
				// TODO Really WORKING? XXX
				// dataset.add(XferBiz.PROFILE_DATA, reader.nex);
				final JsonObject pdata = readProfileDataSection(reader);
				final JsonArray pdList = pdata.getAsJsonArray("list");
				final String joString = XferBiz.listToJson(pdList);
				dataset.addProperty(XferBiz.PROFILE_DATA, joString);
			} else {
				dataset.addProperty(objectNewsDataName, reader.nextString());
			}
		}
		reader.endObject();
		LOGGER.debug("readProfile: dataset = " + dataset);
		LOGGER.debug("readProfile: Done");
		return dataset;
	}

	// Read PROFILE DATA section, from input (file)
	private JsonObject readProfileDataSection(JsonReader reader) throws IOException {
		LOGGER.debug("readProfileData: Called");

		final JsonObject profileDataObj = new JsonObject();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("version")) {
				version = reader.nextString();
				LOGGER.debug("version=" + version);
				profileDataObj.addProperty("version", version);
			} else if (name.equals("list")) {
				profileDataset = readProfileDataItem(reader);
				profileDataObj.add("list", profileDataset);
			} else {
				reader.skipValue(); // avoid some unhandle events
			}
		}
		reader.endObject();
		LOGGER.debug("readProfileData: dataset = " + profileDataObj);
		LOGGER.debug("readProfileData: Done");
		return profileDataObj;
	}

	// Read PROFILE DATA section items, from input (file)
	private JsonArray readProfileDataItem(JsonReader reader) throws IOException {
		final JsonArray exportProfiles = new JsonArray();
		reader.beginArray();
		while (reader.hasNext()) {

			final JsonObject dataset = new JsonObject();
			reader.beginObject();
			while (reader.hasNext()) {
				String objectNewsDataName = reader.nextName();
				LOGGER.debug("readProfileData: objectNewsDataName: '" + objectNewsDataName + "'");
				dataset.addProperty(objectNewsDataName, reader.nextString());
			}
			reader.endObject();
			exportProfiles.add(dataset);
		}
		reader.endArray();
		LOGGER.debug("readProfileData: Done");
		return exportProfiles;
	}

	@SuppressWarnings("unchecked")
	private void loadTableData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();
		List<String> existingProfileNames = null;
		try {
			existingProfileNames = XferBiz.existingProfileNamesFromPrefsToList(profileDataPrefs, appProps);
			// loadProfileData();
		} catch (BackingStoreException e) {
			showPopupMessage("Error createProfileData: " + e, false);
			e.printStackTrace();
		}

		for (int i = 0; i < profileDataset.size(); i++) {
			JsonElement je = profileDataset.get(i);
			JsonObject jo = je.getAsJsonObject();
			final boolean exists = jo.get("exists").getAsBoolean();
			final boolean export = jo.get("import").getAsBoolean();
			final String name = jo.get("titleOne").getAsString();
			final String inputFile = jo.get("inputFile").getAsString();
			LOGGER.debug("loadTableData: loaded row: '" + name + "'");
			final ProfileExportObj obj = new ProfileExportObj();
			obj.setExists(exists);
			obj.setExport(export);
			obj.setName(name);
			obj.setInputFile(inputFile);
			obj.setPayload(jo);
			if (!existingProfileNames.contains(name)) {
				obj.setExists(false);
				obj.setExport(true);
			} else {
				obj.setExists(true);
			}
			newList.add(obj);
		}

		inputTable.getItems().clear();
		inputTable.getItems().setAll(newList);
		LOGGER.debug("loadTableData: Done");
	}

}
