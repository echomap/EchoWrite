package com.echomap.kqf.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.data.ProfileExportObj;
import com.echomap.kqf.persist.Export;
import com.echomap.kqf.two.gui.GUIUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CtrlExport extends BaseCtrl implements Initializable {
	private final static Logger LOGGER = LogManager.getLogger(CtrlExport.class);

	ProfileManager profileManager = null;

	@FXML
	private Pane outerMostContainer;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableView inputTable;
	@FXML
	private TextField inputFile;

	/**
	 * 
	 */
	public CtrlExport() {
		// profileManager = new ProfileManager();
		// profileManager.loadProfileData();
	}

	/*
	 * SETUP Functions
	 */

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);
		lockGui();
		setupTable();
		fixFocus();
		loadData();
		LOGGER.debug("initialize: Done");
	}

	private void fixFocus() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	private void setupTable() {
		LOGGER.debug("setupTable: Called");
		final TableColumn<Object, Object> firstCol = new TableColumn<Object, Object>("Export?");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("export"));
		final TableColumn<Object, Object> keyCol = new TableColumn<Object, Object>("Key");
		keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
		final TableColumn<Object, Object> secondCol = new TableColumn<Object, Object>("Name");
		secondCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn<Object, Object> thirdCol = new TableColumn<Object, Object>("Input File");
		thirdCol.setCellValueFactory(new PropertyValueFactory<>("inputFile"));

		//
		inputTable.getColumns().clear();
		inputTable.getColumns().addAll(firstCol, keyCol, secondCol, thirdCol);

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
		LOGGER.debug("setupTable: Done");
	}

	private void loadData() {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unchecked")
	private void setupTableData() {
		LOGGER.debug("setupTableData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();

		if (profileManager != null) {
			final List<Profile> profiles = profileManager.getProfiles();
			for (final Profile profile : profiles) {
				final ProfileExportObj pobj = new ProfileExportObj();
				pobj.setExists(true);
				pobj.setExport(true);
				pobj.setImportable(false);
				pobj.setInputFile(profile.getInputFile());
				pobj.setName(profile.getMainTitle());
				pobj.setKey(profile.getKey());
				// pobj.setPayload(profile);
				pobj.setSeries(profile.getSeriesTitle());
				newList.add(pobj);
			}
		} else
			LOGGER.warn("setupTableData: ProfileManager is null");
		LOGGER.debug("setupTableData: items# = " + newList.size());
		inputTable.getItems().clear();
		inputTable.getItems().setAll(newList);
		inputTable.refresh();
		LOGGER.debug("setupTableData: Done");
	}

	@Override
	public void setupController(final Properties props, final Preferences appPreferences, Stage primaryStage,
			final Map<String, Object> paramsMap) {
		super.setupController(props, appPreferences, primaryStage, paramsMap);
		//
		paramsMap.put("appVersion", appVersion);
		final Object profileManagerO = paramsMap.get("profileManager");
		if (profileManagerO != null && profileManagerO instanceof ProfileManager) {
			profileManager = (ProfileManager) profileManagerO;
		}
		loadData();
		setupTableData();
	}

	@Override
	void doCleanup() {
		// TODO
	}

	@Override
	void lockGui() {
		// TODO
	}

	@Override
	void unlockGui() {
		// TODO
	}

	public void handleSelectAll(final ActionEvent event) {
		LOGGER.debug("handleSelectAll: Called");
		@SuppressWarnings("unchecked")
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(true);
			}
		}
		inputTable.refresh();
		LOGGER.debug("handleSelectAll: Done");
	}

	public void handleSelectNone(final ActionEvent event) {
		LOGGER.debug("handleSelectNone: Called");
		@SuppressWarnings("unchecked")
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(false);
			}
		}
		inputTable.refresh();
		LOGGER.debug("handleSelectNone: Done");
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

		try {
			// final Charset selCharSet = formatDao.getCharSet();
			final Export export1 = new Export();
			final File outputFilePlain = export1.doExportProfiles(inputFile.getText(), inputTable.getItems(),
					this.appProps, this.appPreferences, profileManager);
			showPopupMessage("Export Done!", "Export Done! Written to '" + outputFilePlain + "'", false);
		} catch (IOException e) {
			LOGGER.error(e);
			showPopupMessage("Export Error", e.getMessage(), true);
		}

		LOGGER.debug("handleExport: Done");
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// doCleanup();
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	public void handleBrowse(final ActionEvent event) {
		LOGGER.debug("handleBrowse: Called");
		chooseFile(event, "Export File", inputFile, "ProfileExport.json", "JSON");
		LOGGER.debug("handleBrowse: Done");
	}

}
