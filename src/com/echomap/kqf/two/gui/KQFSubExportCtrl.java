package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
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
			final Stage stage, final File lastSelectedDirectory) {
		this.profileDataPrefs = child;
		this.formatDao = formatDao;
		this.appProps = appProps;
		this.lastSelectedDirectory = lastSelectedDirectory;
		this.primaryStage = stage;
		// profileDataArray = createProfileData();
		try {
			profileDataArray = XferBiz.createJsonFromPrefsProfileData(child, appProps);
		} catch (BackingStoreException e) {
			showPopupMessage("Error createProfileData: " + e, false);
			e.printStackTrace();
		}
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

		// Hack: align column headers to the center.
		GUIUtils.alignColumnLabelsLeftHack(inputTable);

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
		chooseFile(event, "Export File", inputFile, "ProfileExport.json");
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

		try {
			final Charset selCharSet = formatDao.getCharSet();
			@SuppressWarnings("unchecked")
			final File outputFilePlain = XferBiz.exportProfiles(selCharSet, inputFile.getText(), inputTable.getItems(),
					profileDataPrefs, appProps);
			showPopupMessage("Export Done!", "Export Done! Written to '" + outputFilePlain + "'", false);
		} catch (IOException e) {
			LOGGER.error(e);
			showPopupMessage("Export Error", e.getMessage(), true);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadTableData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();

		for (int i = 0; i < profileDataArray.size(); i++) {
			final JsonElement je = profileDataArray.get(i);
			final JsonObject jo = je.getAsJsonObject();
			if (jo.has("export") && jo.has("titleOne") && jo.has("inputFile")) {
				LOGGER.debug("loadTableData: loaded jo: '" + jo + "'");
				final boolean export = jo.get("export").getAsBoolean();
				final String name = jo.get("titleOne").getAsString();
				final String inputFile = jo.get("inputFile").getAsString();
				LOGGER.debug("loadTableData: loaded row: '" + name + "'");
				final ProfileExportObj obj = new ProfileExportObj();
				obj.setExport(export);
				obj.setName(name);
				obj.setInputFile(inputFile);
				newList.add(obj);
			} else {
				LOGGER.warn("Bad row?" + jo);
			}
		}

		inputTable.getItems().clear();
		inputTable.getItems().setAll(newList);
		inputTable.refresh();
		LOGGER.debug("loadTableData: Done");
	}

}
