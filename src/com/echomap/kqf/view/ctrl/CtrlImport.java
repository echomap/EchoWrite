package com.echomap.kqf.view.ctrl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.Prefs;
import com.echomap.kqf.export.ProfileExportObj;
import com.echomap.kqf.profile.Import;
import com.echomap.kqf.profile.Profile;
import com.echomap.kqf.profile.ProfileManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CtrlImport extends BaseCtrl implements Initializable {
	private final static Logger LOGGER = LogManager.getLogger(CtrlImport.class);

	ProfileManager profileManager = null;

	File lastLoadedFile = null;

	// enum COLS { IMPORT,EXISTS,IMPORTABLE,SERIES,NAME,INPUTFILE};
	private static int COL_IMPORT = 1;
	private static int COL_EXISTS = 2;
	private static int COL_IMPORTABLE = 3;
	private static int COL_SERIES = 4;
	private static int COL_NAME = 5;
	private static int COL_INPUTFILE = 6;

	private static String COLORSTYLE_FALSE = "-fx-background-color: #abb8b8;";
	// #2F4F4F;");// slategray

	@FXML
	private Pane outerMostContainer;
	@FXML
	private TitledPane ImportFilePane;

	@FXML
	private TableView<ProfileExportObj> inputTable;

	@FXML
	private TextField inputFile;

	@FXML
	private Button closeBtn;
	@FXML
	private Button browseBtn;

	/**
	 * 
	 */
	public CtrlImport() {
		super();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);
		lockGui();
		// setupTable();
		fixFocus();
		// loadData();
		LOGGER.debug("initialize: Done");
	}

	@Override
	public void doCleanup() {
		// TODO Auto-generated method stub
	}

	@Override
	void lockGui() {
		lockAllButtons(outerMostContainer);
		closeBtn.setDisable(false);
		browseBtn.setDisable(false);
	}

	@Override
	void unlockGui() {
		unlockAllButtons(outerMostContainer);
	}

	@Override
	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage,
			final Map<String, Object> paramsMap) {
		super.setupController(props, appPreferences, primaryStage, paramsMap);
		//
		paramsMap.put("appVersion", appVersion);
		final Object profileManagerO = paramsMap.get("profileManager");
		if (profileManagerO != null && profileManagerO instanceof ProfileManager) {
			profileManager = (ProfileManager) profileManagerO;
		}
		// loadData();
		setupTable();
		// setupTableData();
	}

	private void loadData(final List<Profile> profilesImported) {
		LOGGER.debug("loadData: Called");
		final ObservableList<ProfileExportObj> newList = FXCollections.observableArrayList();

		final List<String> existingProfileNames = new ArrayList<>();
		final List<Profile> existingProfiles = profileManager.getProfiles();
		for (final Profile profile : existingProfiles) {
			existingProfileNames.add(profile.getKey());
		}

		final List<String> profileNamesInFile = new ArrayList<>();
		for (int i = 0; i < profilesImported.size(); i++) {
			final Profile profile = profilesImported.get(i);

			LOGGER.debug("loadTableData: loading row: '" + profile.getKey() + "'");
			profileNamesInFile.add(profile.getKey());
			final ProfileExportObj obj = new ProfileExportObj();
			obj.setExists(true);
			obj.setExport(true);
			obj.setName(profile.getKey());
			obj.setInputFile(profile.getInputFile());
			obj.setProfile(profile);
			obj.setImportable(true);
			obj.setSeries(profile.getSeriesTitle());
			// obj.setPayload(jo);

			if (!existingProfileNames.contains(profile.getKey())) {
				obj.setExists(false);
				obj.setExport(true);
			} else {
				obj.setExists(true);
			}
			newList.add(obj);
			LOGGER.debug("loadData: Done");
		}

		// Profiles that exist but aren't in the import file
		for (String profileName : existingProfileNames) {
			if (!profileNamesInFile.contains(profileName)) {
				final ProfileExportObj obj = new ProfileExportObj();
				obj.setExists(true);
				obj.setExport(false);
				obj.setName(profileName);
				obj.setInputFile(null);
				obj.setPayload(null);
				obj.setImportable(false);
				newList.add(obj);
			}
		}

		inputTable.getItems().clear();
		inputTable.getItems().setAll(newList);
		inputTable.refresh();
		LOGGER.debug("loadData: Done");
	}

	private void fixFocus() {
		closeBtn.requestFocus();
	}

	@SuppressWarnings("unchecked")
	private void setupTable() {
		LOGGER.debug("setupTable: Called");
		final TableColumn<ProfileExportObj, Boolean> firstCol = new TableColumn<>("Import?");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("export"));
		final TableColumn<ProfileExportObj, Boolean> secondCol = new TableColumn<>("Exists?");
		secondCol.setCellValueFactory(new PropertyValueFactory<>("exists"));
		final TableColumn<ProfileExportObj, Boolean> importableCol = new TableColumn<>("Importable?");
		importableCol.setCellValueFactory(new PropertyValueFactory<>("importable"));
		final TableColumn<ProfileExportObj, String> seriesCol = new TableColumn<>("Series");
		seriesCol.setCellValueFactory(new PropertyValueFactory<>("series"));
		final TableColumn<ProfileExportObj, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn<ProfileExportObj, String> forthCol = new TableColumn<>("Input File");
		forthCol.setCellValueFactory(new PropertyValueFactory<>("inputFile"));
		// forthCol.setStyle("table-view-column-header");
		// forthCol.getStyleClass().add("table-view-column-header");
		// forthCol.getStyleClass().add("top-left");

		// col1. resize handler
		firstCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.IMPORT_PREF_COL_S, COL_IMPORT, newValue);
			}
		});
		secondCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.IMPORT_PREF_COL_S, COL_EXISTS, newValue);
			}
		});
		importableCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.IMPORT_PREF_COL_S, COL_IMPORTABLE, newValue);
			}
		});
		seriesCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.IMPORT_PREF_COL_S, COL_SERIES, newValue);
			}
		});
		nameCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.IMPORT_PREF_COL_S, COL_NAME, newValue);
			}
		});
		forthCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.IMPORT_PREF_COL_S, COL_INPUTFILE, newValue);
			}
		});

		//
		inputTable.getColumns().clear();
		inputTable.getColumns().addAll(firstCol, secondCol, importableCol, seriesCol, nameCol, forthCol);

		// Hack: align column headers to the center.
		BaseCtrl.alignColumnLabelsLeftHack(inputTable);

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

		// Trying to set color of these cells
		firstCol.setCellFactory(tc -> new TableCell<ProfileExportObj, Boolean>() {
			@Override
			protected void updateItem(final Boolean item, final boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
					setGraphic(null);
					return;
				}
				if (item) {
					this.setText("true");
					this.setStyle("");
					if (this.getTableRow() != null)
						this.getTableRow().setStyle("");
				} else {
					this.setText("false");
					// this.setStyle("-fx-background-color: #2F4F4F;");//
					// slategray
					this.setStyle(COLORSTYLE_FALSE);
					// if (this.getTableRow() != null)
					// this.getTableRow().setStyle("-fx-background-color:
					// #708090;");
				}
			}
		});

		secondCol.setCellFactory(tc -> new TableCell<ProfileExportObj, Boolean>() {
			@Override
			protected void updateItem(final Boolean item, final boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
					setGraphic(null);
					return;
				}
				if (item) {
					this.setText("true");
					this.setStyle("");
					// if (this.getTableRow() != null)
					// this.getTableRow().setStyle("");
				} else {
					this.setText("false");// TODO change color
					// this.setStyle("-fx-background-color: #2F4F4F;");//
					// slategray
					this.setStyle(COLORSTYLE_FALSE);
					// if (this.getTableRow() != null)
					// this.getTableRow().setStyle("-fx-background-color:
					// #708090;");
				}
			}
		});

		// set saved col widths
		if (appPreferences != null) {
			@SuppressWarnings("rawtypes")
			final ObservableList columns = inputTable.getColumns();
			String key = "";
			key = String.format(Prefs.IMPORT_PREF_COL_S, 1);
			setColumnWidth(columns, key, 0);
			key = String.format(Prefs.IMPORT_PREF_COL_S, 2);
			setColumnWidth(columns, key, 1);
			key = String.format(Prefs.IMPORT_PREF_COL_S, 3);
			setColumnWidth(columns, key, 2);
			key = String.format(Prefs.IMPORT_PREF_COL_S, 4);
			setColumnWidth(columns, key, 3);
			key = String.format(Prefs.IMPORT_PREF_COL_S, 5);
			setColumnWidth(columns, key, 4);
			key = String.format(Prefs.IMPORT_PREF_COL_S, 6);
			setColumnWidth(columns, key, 5);
		} else {
			LOGGER.warn("NO App preferences set");
		}

		//
		inputTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		BaseCtrl.autoFitTable(inputTable);
		LOGGER.debug("setupTable: Done");

	}

	private void readImportFile() {
		LOGGER.debug("readImportFile: Called");
		try {
			final Import export1 = new Import();
			final List<Profile> profilesImported = export1.readProfilesFromFile(inputFile.getText(), this.appProps,
					this.appPreferences, profileManager);
			// import into table
			loadData(profilesImported);
			unlockGui();
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
			// todo clear table
			showPopupMessage("Import Error", "Check Log file for: " + e.getMessage(), true);
		}
		LOGGER.debug("readImportFile: Done");
	}

	public void handleSelectAll(final ActionEvent event) {
		LOGGER.debug("handleSelectAll: Called");
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
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(false);
			}
		}
		inputTable.refresh();
		LOGGER.debug("handleSelectNone: Done");
	}

	public void handleImportProfiles(final ActionEvent event) {
		LOGGER.debug("handleImportProfiles: Called");
		if (lastLoadedFile != null && lastLoadedFile.getAbsolutePath().compareTo(inputFile.getText()) != 0) {
			final File cfile = new File(inputFile.getText());
			if (cfile != null && cfile.exists()) {
				readImportFile();
				lastLoadedFile = cfile;
			}
		}
		final Import importBiz = new Import();
		final List<String> errors = importBiz.doImportProfiles(inputTable.getItems(), appProps, appPreferences,
				profileManager);
		if (errors.size() > 0) {
			final StringBuilder sb = new StringBuilder();
			for (final String str : errors) {
				sb.append(str);
				sb.append("\n");
			}
			showPopupMessage("Import Errors", sb.toString(), true);
		} else {
			final StringBuilder sb = new StringBuilder();
			for (final ProfileExportObj data : inputTable.getItems()) {
				if (data.isExport()) {
					sb.append("-");
					sb.append(data.getName());// Key());
					sb.append("\n");
				}
			}
			showPopupMessage("Import Done!", "Import Done! Imported:\n" + sb.toString(), false);
		}
		LOGGER.debug("handleImportProfiles: Done");
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		doCleanup();
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	// 1
	public void handleBrowse(final ActionEvent event) {
		LOGGER.debug("handleBrowse: Called");
		lockGui();
		final File cfile = locateFile(event, "Export File", inputFile, "ProfileExport.json",
				EchoWriteConst.FILTERTYPE.JSON);
		if (cfile != null && cfile.exists()) {
			readImportFile();
			lastLoadedFile = cfile;
		}
		LOGGER.debug("handleBrowse: Done");
	}
}
