package com.echomap.kqf.view.ctrl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.KeyValuePair;
import com.echomap.kqf.profile.Profile;
import com.echomap.kqf.profile.ProfileManager;
import com.echomap.kqf.view.gui.ConfirmResult;

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
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * 
 * @author mkatz
 */
public class CtrlExternalIDs extends BaseCtrl implements Initializable {
	private final static Logger LOGGER = LogManager.getLogger(CtrlExternalIDs.class);

	protected static final int COL_KEY = 1;
	protected static final int COL_VALUE = 2;

	ProfileManager profileManager = null;

	Profile selectedProfile = null;
	KeyValuePair selectedOtherData = null;
	private int selectedRow = 0;

	private StringBuilder logText = new StringBuilder(5000);

	private List<KeyValuePair> cachedOutputs = null;

	@FXML
	private SplitPane outerMostContainer;
	@FXML
	private GridPane outerFirstContainer;
	@FXML
	private GridPane outerSecondContainer;

	@SuppressWarnings("rawtypes")
	@FXML
	private TableView inputTable;

	@FXML
	private Label profileDataChanged;
	@FXML
	private Label chosenDocTagsText;
	@FXML
	private Label overallDataChanged;
	@FXML
	private Label chosenProfileText;

	@FXML
	private Button btnCloseScreen;

	@FXML
	private TextField inputName;
	@FXML
	private TextField inputValue;

	@FXML
	private TextArea textAreaLog;

	@FXML
	private Pane areaKeyValue;

	/**
	 * 
	 */
	public CtrlExternalIDs() {
		// profileManager = new ProfileManager();
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

	@Override
	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage,
			final Map<String, Object> paramsMap) {
		super.setupController(props, appPreferences, primaryStage, paramsMap);
		// Parse Params
		paramsMap.put("appVersion", appVersion);
		final Object selectedProfileO = paramsMap.get("selectedProfile");
		if (selectedProfileO != null && selectedProfileO instanceof Profile) {
			selectedProfile = (Profile) selectedProfileO;
			chosenProfileText.setText(selectedProfile.getKey());
			cachedOutputs = selectedProfile.getExternalIDs();
		}
		final Object profileManagerO = paramsMap.get("profileManager");
		if (profileManagerO != null && selectedProfileO instanceof ProfileManager) {
			profileManager = (ProfileManager) profileManagerO;
		} else
			profileManager = new ProfileManager();
		// Load Data and setup GUI
		loadData();
		fixFocus();
	}

	@Override
	public void doCleanup() {
		// MAYBE: Anything to do here?
		// if(profileManager!=null)profileManager.close();
	}

	@Override
	void lockGui() {
		// mainButtonBar.setDisable(true);
		areaKeyValue.setVisible(false);
		hideAllInArea(areaKeyValue);
	}

	@Override
	void unlockGui() {
		// mainButtonBar.setDisable(false);
	}

	void lockNewInputArea() {
		// editProfilePane.setDisable(true);
		setProfileChangeMade(false);
		areaKeyValue.setVisible(true);
		hideAllInArea(areaKeyValue);
		fixFocus();
	}

	void unlockNewInputArea() {
		// editProfilePane.setDisable(false);
		areaKeyValue.setVisible(true);
		showAllInArea(areaKeyValue);
	}

	void lockTable() {
		// inputTable.setSelectionModel(null);
		// inputTable.setOnMouseEntered();
		// inputTable.getSelectionModel().selectLast();
	}

	void unlockTable() {
		// inputTable.setSelectionModel(inputTableSelectionModel);
	}

	class ConfirmResultDelete implements ConfirmResult {

		@Override
		public void actionConfirmed(final String title) {
			LOGGER.debug("actionConfirmed: Called");
			final List<KeyValuePair> listo = selectedProfile.getExternalIDs();
			for (final KeyValuePair otherData : listo) {
				if (otherData.getKey().compareTo(selectedOtherData.getKey()) == 0) {
					LOGGER.debug("actionConfirmed: Removed name=" + otherData.getKey());
					listo.remove(otherData);
					break;
				}
			}
			selectedOtherData = null;

			profileManager.saveProfileData(selectedProfile);
			cachedOutputs = selectedProfile.getExternalIDs();

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
		selectedOtherData = null;
		unlockNewInputArea();
		LOGGER.debug("handleNew: Done");
	}

	public void handleModifySave(final ActionEvent event) {
		LOGGER.debug("handleModifySave: Called");
		if (selectedOtherData == null) {
			selectedOtherData = new KeyValuePair();
			selectedProfile.addExternalIDs(selectedOtherData);
		}
		selectedOtherData.setKey(inputName.getText());
		selectedOtherData.setValue(inputValue.getText());

		setOverallChangeMade(true);
		setProfileChangeMade(false);
		unselectRow();
		unlockTable();
		refreshTable();
		lockNewInputArea();
		LOGGER.debug("handleModifySave: Done");
	}

	public void handleDelete(final ActionEvent event) {
		LOGGER.debug("handleDelete: Called");
		if (selectedOtherData == null) {
			showPopupMessage("Error", "Nothing selected to delete", true);
			return;
		}
		//
		final ConfirmResultDelete confirmResultDelete = new ConfirmResultDelete();
		showConfirmDialog("Delete?", "Really Delete?", confirmResultDelete);
		LOGGER.debug("handleDelete: Done");
	}

	public void handleSaveToProfile(final ActionEvent event) {
		LOGGER.debug("handleSaveToProfile: Called");
		profileManager.saveProfileData(selectedProfile);
		cachedOutputs = selectedProfile.getExternalIDs();
		setOverallChangeMade(false);
		LOGGER.debug("handleSaveToProfile: Done");
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
			profileDataChanged.getStyleClass().clear();
			profileDataChanged.getStyleClass().add("highlightedOnOutlinedText");
			// btnCloseScreen.setText("C_ancel");
		} else {
			profileDataChanged.setText("Up to date");
			profileDataChanged.getStyleClass().clear();
			profileDataChanged.getStyleClass().add("highlightedOffOutlinedText");
			// btnCloseScreen.setText("_Back");
		}
	}

	void setOverallChangeMade(boolean b) {
		// overallChangeMade = b;
		if (b) {
			overallDataChanged.setText("Unsaved Changes");
			btnCloseScreen.setText("_Cancel");
			btnCloseScreen.setStyle("-fx-background-color: ##F58E9D");
			// btnCloseScreen.getStyleClass().remove(1);
			// btnCloseScreen.getStyleClass().add("deletebutton");
		} else {
			overallDataChanged.setText("");
			btnCloseScreen.setText("_Close");
			btnCloseScreen.setStyle("-fx-background-color: #6473A4");
			// btnCloseScreen.getStyleClass().remove(1);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupTable() {
		//
		final ObservableList<TableColumn> columns = inputTable.getColumns();
		final TableColumn colK = columns.get(0);
		colK.setCellValueFactory(new PropertyValueFactory<>("key"));
		final TableColumn colS = columns.get(1);
		colS.setCellValueFactory(new PropertyValueFactory<>("value"));

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
				columnWidthChanged(COL_VALUE, newValue);
			}
		});

		// Hack: align column headers to the center.
		BaseCtrl.alignColumnLabelsLeftHack(inputTable);

		inputTable.setRowFactory(new Callback<TableView<KeyValuePair>, TableRow<KeyValuePair>>() {
			@Override
			public TableRow<KeyValuePair> call(TableView<KeyValuePair> tableView) {
				final TableRow<KeyValuePair> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(final MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final KeyValuePair rowData = row.getItem();
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
							final KeyValuePair rowData = row.getItem();
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

	private void selectRow(final KeyValuePair oodtd) {
		LOGGER.debug("selectRow: Data: " + oodtd);
		if (oodtd == null) {
			return;
		}
		if (profileChangeMade) {
			return;
		}
		selectedOtherData = oodtd;
		// chosenDocTagsText.setText(oodtd.getKey());
		inputName.setText(oodtd.getKey());
		inputValue.setText(oodtd.getValue());
		//
		setProfileChangeMade(false);
		unlockNewInputArea();
		lockTable();
		LOGGER.debug("selectRow: Done");
	}

	private void unselectRow() {
		LOGGER.debug("unselectRow: Called");
		inputName.setText("");
		inputValue.setText("");
		// inputDocTags.setText("");
		setProfileChangeMade(false);
		lockNewInputArea();
		LOGGER.debug("unselectRow: Done");
	}

	@SuppressWarnings("unchecked")
	private void loadData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<KeyValuePair> newList = FXCollections.observableArrayList();
		inputTable.getItems().clear();
		if (selectedProfile != null) {

			final List<KeyValuePair> listOO = selectedProfile.getExternalIDs();
			if (listOO != null) {
				for (final KeyValuePair otherData : listOO) {
					newList.add(otherData);
				}
			}
			inputTable.getItems().clear();
			inputTable.getItems().setAll(newList);
		}
		inputTable.refresh();
		setDetectChanges(outerMostContainer);
		setDetectChanges(outerMostContainer.getItems());
		// outerFirstContainer
		// outerSecondContainer
		LOGGER.debug("loadTableData: Done");
	}

	private void refreshTable() {
		loadData();
	}

	private void resetSelectedProfile() {
		// profileManager.loadProfileData();
		selectedProfile.setExternalIDs(cachedOutputs);
	}

	private void fixFocus() {
		btnCloseScreen.requestFocus();
	}

	private void writeLog(final String str) {
		logText.insert(0, str + System.lineSeparator());
		if (textAreaLog != null)
			textAreaLog.setText(logText.toString());
	}
}
