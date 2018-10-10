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

import com.echomap.kqf.biz.KqfBiz;
import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.OtherDocTagData;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.persist.Export;
import com.echomap.kqf.two.gui.GUIUtils;

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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CtrlMoreFiles extends BaseCtrl implements Initializable {
	private final static Logger LOGGER = LogManager.getLogger(CtrlMoreFiles.class);

	protected static final int COL_KEY = 1;
	protected static final int COL_FILE = 2;
	protected static final int COL_DOCTAGS = 3;

	final ProfileManager profileManager;
	Profile selectedProfile = null;
	OtherDocTagData selectedOtherData = null;
	private int selectedRow = 0;
	// Profile Changes in this case are changes made to a docTag element, where
	// this is overall
	// private boolean overallChangeMade = false;
	private List<OtherDocTagData> cachedOutputs = null;

	@FXML
	private GridPane outerMostContainer;
	@FXML
	private VBox editProfilePane;
	@FXML
	private HBox mainButtonBar;

	@SuppressWarnings("rawtypes")
	@FXML
	private TableView inputTable;

	@FXML
	private Label chosenProfileText;
	@FXML
	private Label profileDataChanged;

	@FXML
	private Label chosenDocTagsText;
	@FXML
	private Label overallDataChanged;

	@FXML
	private Button buttonExport;
	@FXML
	private Button buttonImport;
	@FXML
	private Button btnCloseScreen;

	@FXML
	private TextField inputName;
	@FXML
	private TextField inputFile;
	@FXML
	private TextArea inputDocTags;

	/**
	 * 
	 */
	public CtrlMoreFiles() {
		profileManager = new ProfileManager();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);
		//
		lockGui();
		lockNewInputArea();
		fixFocus();
		//
		// myWorkDoneNotify = new MyWorkDoneNotify(lastRunText, summaryRunText);
		//
		setProfileChangeMade(false);
		setOverallChangeMade(false);
		setupTable();
		loadData();
		fixFocus();
		unlockGui();
		LOGGER.debug("initialize: Done");
	}

	// @Override
	// public void setupController(final Properties props, final Preferences
	// appPreferences, final Stage primaryStage) {
	// super.setupController(props, appPreferences, primaryStage);
	// LOGGER.debug("setupController: Done");
	// this.appPreferences =
	// Preferences.userNodeForPackage(CtrlProfileView.class);
	// profileManager.setAppVersion(this.appVersion);
	// }

	@Override
	public void setupController(Properties props, Preferences appPreferences, Stage primaryStage,
			Map<String, Object> paramsMap) {
		super.setupController(props, appPreferences, primaryStage, paramsMap);
		//
		paramsMap.put("appVersion", appVersion);
		final Object selectedProfileO = paramsMap.get("selectedProfile");
		if (selectedProfileO != null && selectedProfileO instanceof Profile) {
			selectedProfile = (Profile) selectedProfileO;
			chosenProfileText.setText(selectedProfile.getKey());
			cachedOutputs = selectedProfile.getOutputs();
		}
		loadData();
		fixFocus();
	}

	@Override
	void doCleanup() {
		// TODO Auto-generated method stub
	}

	@Override
	void lockGui() {
		mainButtonBar.setDisable(true);
	}

	@Override
	void unlockGui() {
		mainButtonBar.setDisable(false);
	}

	void lockNewInputArea() {
		editProfilePane.setDisable(true);
		setProfileChangeMade(false);
	}

	void unlockNewInputArea() {
		editProfilePane.setDisable(false);
	}

	void lockTable() {
		// inputTable.setSelectionModel(null);
		// inputTable.setOnMouseEntered();
		// inputTable.getSelectionModel().selectLast();
	}

	void unlockTable() {
		// inputTable.setSelectionModel(inputTableSelectionModel);
	}

	// ->inputFile
	public void handleBrowse(final ActionEvent event) {
		LOGGER.debug("handleBrowse: Called");
		final String filePrefixText = selectedProfile.getInputFilePrefix();
		boolean success = locateDir(event, "Open Output Dir", inputFile, inputFile);
		if (success) {
			final String outFilename = filePrefixText == null ? "/" + inputName.getText() + ".txt"
					: filePrefixText + (selectedProfile.isAppendUnderscoreToPrefix() ? "_" : "") + inputName.getText()
							+ ".txt";
			final File nFile = new File(inputFile.getText(), outFilename);
			inputFile.setText(nFile.getAbsolutePath());
			// actionToCancel();
		}
		LOGGER.debug("handleBrowse: Done");
	}

	public void handleModifySave(final ActionEvent event) {
		LOGGER.debug("handleModifySave: Called");
		if (selectedOtherData == null) {
			selectedOtherData = new OtherDocTagData();
			selectedProfile.addOutput(selectedOtherData);
		}
		selectedOtherData.setDocTags(inputDocTags.getText());
		selectedOtherData.setFile(inputFile.getText());
		selectedOtherData.setName(inputName.getText());

		setOverallChangeMade(true);
		setProfileChangeMade(false);
		unselectRow();
		unlockTable();
		refreshTable();
		LOGGER.debug("handleModifySave: Done");
	}

	public void handleDelete(final ActionEvent event) {
		LOGGER.debug("handleDelete: Called");
		if (selectedOtherData == null) {
			showPopupMessage("Error", "Nothing selected to delete", true);
			return;
		}
		// TODO
		final ConfirmResultDelete confirmResultDelete = new ConfirmResultDelete();
		showConfirmDialog("Delete?", "Really Delete?", confirmResultDelete);
		LOGGER.debug("handleDelete: Done");
	}

	class ConfirmResultDelete implements ConfirmResult {

		@Override
		public void actionConfirmed(final String title) {
			LOGGER.debug("actionConfirmed: Called");
			final List<OtherDocTagData> listo = selectedProfile.getOutputs();
			for (final OtherDocTagData otherDocTagData : listo) {
				if (otherDocTagData.getName().compareTo(selectedOtherData.getName()) == 0) {
					LOGGER.debug("actionConfirmed: Removed name=" + otherDocTagData.getName());
					listo.remove(otherDocTagData);
					break;
				}
			}
			selectedOtherData = null;

			// profileManager.saveProfileData(selectedProfile);
			// cachedOutputs = selectedProfile.getOutputs();

			setOverallChangeMade(true);
			unselectRow();
			unlockTable();
			refreshTable();
		}

		@Override
		public void actionCancelled(String title) {
			LOGGER.debug("actionCancelled: Called");
			unselectRow();
			unlockTable();
		}
	}

	public void handleCancelChange(final ActionEvent event) {
		LOGGER.debug("handleCancelChange: Called");
		unselectRow();
		unlockTable();
		// setProfileChangeMade(false);
		LOGGER.debug("handleCancelChange: Done");
	}

	public void handleNew(final ActionEvent event) {
		LOGGER.debug("handleNew: Called");
		setProfileChangeMade(true);
		unlockNewInputArea();
		LOGGER.debug("handleNew: Done");
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

//		try {
			final File cFile = chooseFile(event, "Choose Export File", null, selectedProfile.getKey() + "_more.json",
					"JSON");
			if (cFile == null) {
				showPopupMessage("Failed", "No file selected.", false);
				return;
			}
//
//			// final Charset selCharSet = formatDao.getCharSet();
//			final Export export1 = new Export();
//			@SuppressWarnings("unchecked")
//			final File outputFilePlain = export1.doExportMoreFiles(inputFile.getText(), inputTable.getItems(),
//					this.appProps, this.appPreferences, profileManager);
//			showPopupMessage("Export Done!", "Export Done! Written to '" + outputFilePlain + "'", false);
//		} catch (IOException e) {
//			LOGGER.error(e);
//			showPopupMessage("Export Error", e.getMessage(), true);
//		}

		LOGGER.debug("handleExport: Done");
	}

	public void handleImport(final ActionEvent event) {
		LOGGER.debug("handleImport: Called");
		// TODO
		LOGGER.debug("handleImport: Done");
	}

	public void handleSave(final ActionEvent event) {
		LOGGER.debug("handleSave: Called");
		profileManager.saveProfileData(selectedProfile);
		cachedOutputs = selectedProfile.getOutputs();
		setOverallChangeMade(false);
		LOGGER.debug("handleSave: Done");
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		resetSelectedProfile();
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// persist();
		doCleanup();
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	@Override
	void setProfileChangeMade(boolean b) {
		super.setProfileChangeMade(b);
		if (b) {
			profileDataChanged.setText("Unsaved Changes");
		} else {
			profileDataChanged.setText("");
		}
	}

	void setOverallChangeMade(boolean b) {
		// overallChangeMade = b;
		if (b) {
			overallDataChanged.setText("Unsaved Changes");
			btnCloseScreen.setText("_Cancel");
		} else {
			overallDataChanged.setText("");
			btnCloseScreen.setText("_Close");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupTable() {
		//
		final ObservableList<TableColumn> columns = inputTable.getColumns();
		final TableColumn colK = columns.get(0);
		colK.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn colS = columns.get(1);
		colS.setCellValueFactory(new PropertyValueFactory<>("file"));
		final TableColumn colM = columns.get(2);
		colM.setCellValueFactory(new PropertyValueFactory<>("docTags"));

		// col1. resize handler
		colK.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(COL_KEY, newValue);
			}
		});
		colS.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(COL_FILE, newValue);
			}
		});
		colM.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(COL_DOCTAGS, newValue);
			}
		});

		// Hack: align column headers to the center.
		GUIUtils.alignColumnLabelsLeftHack(inputTable);

		inputTable.setRowFactory(new Callback<TableView<OtherDocTagData>, TableRow<OtherDocTagData>>() {
			@Override
			public TableRow<OtherDocTagData> call(TableView<OtherDocTagData> tableView) {
				final TableRow<OtherDocTagData> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(final MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final OtherDocTagData rowData = row.getItem();
							LOGGER.debug("rowData: left2: " + rowData);
							// inputName.setText(rowData.getName());
							// rowData.setExport(!rowData.isExport());
							if (!profileChangeMade) {
								selectRow(rowData);
								selectedRow = row.getIndex();
							} else {
								// TODO somehow select last row?
								inputTable.getSelectionModel().select(selectedRow);
							}
						} else if (event.isSecondaryButtonDown()) {
							// right click code here
							final OtherDocTagData rowData = row.getItem();
							LOGGER.debug("rowData: right: " + rowData);
							// rowData.setExport(true);
						}
						inputTable.refresh();
					}
				});
				return row;
			}
		});

		// inputTable.setRowFactory(param -> new TableRow<OtherDocTagData>() {
		// @Override
		// protected void updateItem(OtherDocTagData item, boolean empty) {
		// super.updateItem(item, empty);
		// if (!empty) {
		// disableProperty().bind(item.getFocusable().not());
		// }
		// }
		// });

		inputTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		// GUIUtils.autoFitTable(profileTable);
		// inputTableSelectionModel = inputTable.getSelectionModel();
	}

	private void columnWidthChanged(final int colNum, final Number newValue) {
		if (appPreferences != null) {
			// final String key = String.format(PREF_COL_S, colNum);
			// LOGGER.debug("columnWidthChanged: col#" + colNum + " val=" +
			// newValue.doubleValue());
			// appPreferences.putDouble(key, newValue.doubleValue());
		}
	}

	private void selectRow(final OtherDocTagData oodtd) {
		LOGGER.debug("selectProfile: OtherDocTagData: " + oodtd);
		if (oodtd == null) {
			return;
		}
		if (profileChangeMade) {
			return;
		}
		selectedOtherData = oodtd;
		chosenDocTagsText.setText(oodtd.getName());
		inputName.setText(oodtd.getName());
		inputFile.setText(oodtd.getFile());
		inputDocTags.setText(oodtd.getDocTags());
		//
		setProfileChangeMade(false);
		unlockNewInputArea();
		lockTable();
		LOGGER.debug("selectRow: Done");
	}

	private void unselectRow() {
		LOGGER.debug("unselectRow: Called");
		inputName.setText("");
		inputFile.setText("");
		inputDocTags.setText("");
		setProfileChangeMade(false);
		lockNewInputArea();
		LOGGER.debug("unselectRow: Done");
	}

	@SuppressWarnings("unchecked")
	private void loadData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<OtherDocTagData> newList = FXCollections.observableArrayList();
		inputTable.getItems().clear();
		if (selectedProfile != null) {

			final List<OtherDocTagData> listOO = selectedProfile.getOutputs();
			if (listOO != null) {
				for (final OtherDocTagData otherDocTagData : listOO) {
					newList.add(otherDocTagData);
				}
			}
			inputTable.getItems().clear();
			inputTable.getItems().setAll(newList);
		}
		inputTable.refresh();
		setDetectChanges(outerMostContainer);
		LOGGER.debug("loadTableData: Done");
	}

	private void refreshTable() {
		loadData();
	}

	private void resetSelectedProfile() {
		// profileManager.loadProfileData();
		selectedProfile.setOutputs(cachedOutputs);
	}

	private void fixFocus() {
		btnCloseScreen.requestFocus();
	}
}
