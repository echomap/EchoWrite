package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.ProfileExportObj;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

public class KQFSubExportCtrl extends KQFSubBaseExportCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFSubExportCtrl.class);

	private JsonArray profileDataArray = null;

	public void setExportData(final Preferences child, final FormatDao formatDao, final Properties appProps,
			final Stage stage) {
		this.profileData = child;
		this.formatDao = formatDao;
		this.appProps = appProps;
		this.primaryStage = stage;
		profileDataArray = createProfileData();
		loadTableData();
	}

	@SuppressWarnings("unchecked")
	@FXML
	void initialize() {
		LOGGER.debug("initialize: Called");
		final TableColumn<Object, Object> firstCol = new TableColumn<Object, Object>("Export?");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("export"));
		final TableColumn<Object, Object> secondCol = new TableColumn<Object, Object>("Name");
		secondCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn<Object, Object> thirdCol = new TableColumn<Object, Object>("Input File");
		thirdCol.setCellValueFactory(new PropertyValueFactory<>("inputFile"));

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

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

		final Charset selCharSet = formatDao.getCharSet();
		LOGGER.debug("handleExport: Charset chosen: " + selCharSet);
		Writer fWriterPlain = null;
		try {
			final File outputFilePlain = new File(inputFile.getText());
			LOGGER.info("Writing export file to: " + outputFilePlain);
			if (inputFile.getText() == null || inputFile.getText().length() < 1) {
				LOGGER.warn("No file set, can't export!");
				showMessage("Export Error! No File set!", true);
				return;
			}
			// fWriterPlain = new FileWriter(outputFilePlain, false);
			fWriterPlain = new OutputStreamWriter(new FileOutputStream(outputFilePlain), selCharSet);

			final ObservableList<ProfileExportObj> targetList = inputTable.getItems();

			final JsonObject exportDataset = new JsonObject();
			exportDataset.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));
			LOGGER.debug("handleExport: exportDataset: " + exportDataset);
			final JsonArray exportProfiles = new JsonArray();
			if (targetList != null) {
				for (ProfileExportObj data : targetList) {
					if (data.isExport()) {
						LOGGER.debug("handleExport: exporting profile: '" + data.getName() + "'");
						final JsonObject dataset = new JsonObject();
						loadProfile(data.getName(), dataset);
						LOGGER.debug("handleExport: dataset: " + dataset);
						exportProfiles.add(dataset);
					}
				}
			}
			exportDataset.add("profiles", exportProfiles);

			// create the gson using the GsonBuilder. Set pretty printing on.
			// Allow
			// serializing null and set all fields to the Upper Camel Case
			final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
					.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			// System.out.println(gson2.toJson(exportDataset));
			// LOGGER.debug(gson2.toJson(exportDataset));
			fWriterPlain.write(gson2.toJson(exportDataset));
			// LOGGER.debug(gson2.toJson(exportProfiles));
			// final Gson gson = new Gson();
			// LOGGER.debug(gson.toJson(exportDataset));
			showMessage("Export Done! Written to '" + outputFilePlain + "'", false);
		} catch (IOException e) {
			LOGGER.error(e);
			showMessage("Export Error!" + e.getMessage(), true);
		} finally {
			if (fWriterPlain != null)
				try {
					fWriterPlain.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void loadTableData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();

		for (int i = 0; i < profileDataArray.size(); i++) {
			JsonElement je = profileDataArray.get(i);
			JsonObject jo = je.getAsJsonObject();
			final boolean export = jo.get("export").getAsBoolean();
			final String name = jo.get("titleOneText").getAsString();
			final String inputFile = jo.get("inputFileText").getAsString();
			// inputFileText
			LOGGER.debug("loadTableData: loaded row: '" + name + "'");
			final ProfileExportObj obj = new ProfileExportObj();
			obj.setExport(export);
			obj.setName(name);
			obj.setInputFile(inputFile);
			newList.add(obj);
		}

		inputTable.getItems().clear();
		inputTable.getItems().setAll(newList);
		LOGGER.debug("loadTableData: Done");
	}

}
