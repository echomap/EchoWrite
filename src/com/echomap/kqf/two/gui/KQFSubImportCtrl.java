package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
			final Stage stage) {
		this.profileData = child;
		this.formatDao = formatDao;
		this.appProps = appProps;
		this.primaryStage = stage;
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

	public void handleImportProfiles(final ActionEvent event) {
		LOGGER.debug("handleImportProfiles: Called");
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				if (data.isExport()) {
					LOGGER.debug("handleImportProfiles: importing profile: '" + data.getName() + "'");
					final JsonObject jo = data.getPayload();
					// final boolean export = jo.get("export").getAsBoolean();
					final String eName = jo.get("titleOne").getAsString();
					final String eInputFile = jo.get("inputFile").getAsString();
					LOGGER.debug("handleImportProfiles: loaded row: '" + eName + "'");
					// TODO IMPORT PROFILE
					final boolean eExists = jo.get("exists").getAsBoolean();
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
						showMessage("Imported Selected Profiles", false);
					} catch (BackingStoreException e) {
						showMessage("Error with ImportProfiles: " + e, false);
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
			showMessage("Import Error! No File set!", true);
			return;
		}
		if (!filePlain.exists()) {
			LOGGER.warn("No file exists, can't import!");
			showMessage("Import Error! File doesn't exist!", true);
			return;
		}
		// final String stringData = readFile(inputFile.getText(), selCharSet)
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
			showMessage("Export Error!" + e.getMessage(), true);
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
		//
		LOGGER.debug("handleImportFile: Done");
		loadTableData();
	}

	private void loadTableData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();
		final List<String> existingProfileNames = loadProfileData();

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

	private List<String> loadProfileData() {
		LOGGER.debug("loadProfileData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();
		final JsonArray existingProfileDataArray = createProfileData();
		final ArrayList<String> existingProfileNames = new ArrayList<>();
		for (int i = 0; i < existingProfileDataArray.size(); i++) {
			JsonElement je = existingProfileDataArray.get(i);
			JsonObject jo = je.getAsJsonObject();
			final String name = jo.get("titleOne").getAsString();
			LOGGER.debug("loadProfileData: loaded row: '" + name + "'");
			existingProfileNames.add(name);
		}
		LOGGER.debug("loadProfileData: Done");
		return existingProfileNames;
	}

	private JsonArray readProfiles(final JsonReader reader) throws IOException {
		LOGGER.debug("readProfiles: Called");
		final JsonArray exportProfiles = new JsonArray();
		reader.beginArray();
		while (reader.hasNext()) {
			// reader.beginObject();
			// while (reader.hasNext()) {
			// String objectNewsName = reader.nextName();
			final JsonObject jo = readProfile(reader);
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

	private JsonObject readProfile(JsonReader reader) throws IOException {
		LOGGER.debug("readProfile: Called");
		final JsonObject dataset = new JsonObject();
		reader.beginObject();
		while (reader.hasNext()) {
			String objectNewsDataName = reader.nextName();
			if (objectNewsDataName.equals("export")) {
				reader.skipValue();
			} else if (objectNewsDataName.equals("filePrefixCheckbox")) {
				dataset.addProperty(objectNewsDataName, reader.nextBoolean());
			} else {
				dataset.addProperty(objectNewsDataName, reader.nextString());
			}
		}
		reader.endObject();
		LOGGER.debug("readProfile: dataset = " + dataset);
		LOGGER.debug("readProfile: Done");
		return dataset;

	}

	private void readFieldImage(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			LOGGER.debug("NEWS: " + reader.nextString());
			// you will get the field image array content here
		}
	}

}
