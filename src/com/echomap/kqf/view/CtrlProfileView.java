package com.echomap.kqf.view;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.Profile;

import javafx.application.Platform;
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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CtrlProfileView extends BaseCtrl implements Initializable, WorkFinishedCallback {
	private static final String VIEW_WINDOW_H = Prefs.VIEW_WINDOW_H;
	private static final String VIEW_WINDOW_W = Prefs.VIEW_WINDOW_W;
	private static final String VIEW_WINDOW_Y = Prefs.VIEW_WINDOW_Y;
	private static final String VIEW_WINDOW_X = Prefs.VIEW_WINDOW_X;
	private static final String PREF_SPLIT_V = Prefs.VIEW_PREF_SPLIT_V;
	private static final String PREF_COL_S = Prefs.VIEW_PREF_COL_S;
	private static final String PREF_SPLIT_H = Prefs.VIEW_PREF_SPLIT_H;

	private final static Logger LOGGER = LogManager.getLogger(CtrlProfileView.class);
	final ProfileManager profileManager;

	// File lastSelectedDirectory = null;
	Profile selectedProfile = null;
	// final ProfileBiz profileBiz;
	// final Preferences userPrefs;
	// Properties appProps = null;
	// String appVersion = null;
	// private boolean runningMutex = false;
	private MyWorkDoneNotify myWorkDoneNotify = null;
	final Map<String, String> filters = new HashMap<>();
	Timer timer = new Timer();
	private MyFilterTimerTask myTimerTask;

	private static int COL_KEY = 1;
	private static int COL_SERIES = 2;
	private static int COL_MAINTITLE = 3;
	private static int COL_SUBTITLE = 4;
	private static int COL_KEYWORDS = 5;

	@FXML
	private BorderPane outerMostContainer;
	@FXML
	private TextArea loggingText;
	// @FXML
	// private TextArea lastRunText
	// @FXML
	// private TextArea summaryRunText;
	@FXML
	private SplitPane splitVert;
	@FXML
	private SplitPane splitHoriz;

	@SuppressWarnings("rawtypes")
	@FXML
	private TableView profileTable;

	// @FXML
	// private Label profileDataChanged;
	@FXML
	private Label chosenProfileText;

	@FXML
	private Button newProfileBtn;
	@FXML
	private Button editProfileBtn;
	@FXML
	private Button deleteProfileBtn;
	@FXML
	private Button clearProfileBtn;
	@FXML
	private Button renameProfileBtn;

	@FXML
	private Button btnRunWordCounter;
	@FXML
	private Button btnRunOutliner;
	@FXML
	private Button btnRunFormatter;
	@FXML
	private Button btnRunTimeline;
	@FXML
	private Button btnCmdBookLookup;

	@FXML
	private TextField filterTextKey;
	@FXML
	private TextField filterTextName;
	@FXML
	private TextField filterTextSeries;
	@FXML
	private TextField filterTextKeyword;

	@FXML
	private TextField inputFileText;
	@FXML
	private TextField mainTitleText;
	@FXML
	private TextField subTitleText;
	@FXML
	private TextField seriesTitleText;
	@FXML
	private TextField volumeText;
	@FXML
	private TextField keywordsText;
	@FXML
	private TextField inputProfileKey;

	/**
	 * 
	 */
	public CtrlProfileView() {
		profileManager = new ProfileManager();
		profileManager.loadProfileData();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage) {
		super.setupController(props, appPreferences, primaryStage);
		LOGGER.debug("setupController: Done");
		this.appPreferences = Preferences.userNodeForPackage(CtrlProfileView.class);
		profileManager.setAppVersion(this.appVersion);

		splitHoriz.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
			this.splitHorizChanged(newVal);
		});
		splitVert.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
			this.splitVertChanged(newVal);
		});

		if (appPreferences != null) {
			@SuppressWarnings("rawtypes")
			final ObservableList columns = profileTable.getColumns();

			final double splitH = appPreferences.getDouble(PREF_SPLIT_H, -1);
			final double splitV = appPreferences.getDouble(PREF_SPLIT_V, -1);
			LOGGER.debug("setupController: splitH=" + splitH);
			if (splitH > -1)
				splitHoriz.setDividerPositions(splitH);
			if (splitV > -1)
				splitVert.setDividerPositions(splitV);

			String key = "";
			key = String.format(PREF_COL_S, 1);
			setColumnWidth(columns, key, 0);
			key = String.format(PREF_COL_S, 2);
			setColumnWidth(columns, key, 1);
			key = String.format(PREF_COL_S, 3);
			setColumnWidth(columns, key, 2);
			key = String.format(PREF_COL_S, 4);
			setColumnWidth(columns, key, 3);
			key = String.format(PREF_COL_S, 5);
			setColumnWidth(columns, key, 4);
		}

		// final Locale list[] = DateFormat.getAvailableLocales();
		// for (Locale aLocale : list) {
		// showMessage("Available Local:" + aLocale.toString());
		// }

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);

		// lockGui();
		lockGuiPerNoProfile();
		fixFocus();

		filterTextKey.textProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("filterTextKey changed from " + oldValue + " to " + newValue);
			setupFilter("Key", newValue);
		});
		filterTextName.textProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("filterTextName changed from " + oldValue + " to " + newValue);
			setupFilter("Name", newValue);
		});
		filterTextSeries.textProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("filterTextSeries changed from " + oldValue + " to " + newValue);
			setupFilter("Series", newValue);
		});
		filterTextKeyword.textProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("filterTextKeyword changed from " + oldValue + " to " + newValue);
			setupFilter("Keyword", newValue);
		});

		myWorkDoneNotify = new MyWorkDoneNotify(loggingText, loggingText, this);

		//
		setProfileChangeMade(false);
		setupTable();
		loadTableData();
		LOGGER.debug("initialize: Done");
	}

	@Override
	void lockGui() {
		LOGGER.debug("lockGui: Called");
		// if (runningMutex) {
		// return;
		// }
		// Prevent Actions
		// lockGuiPerNoProfile();
		newProfileBtn.setDisable(true);
		editProfileBtn.setDisable(true);
		renameProfileBtn.setDisable(true);
		deleteProfileBtn.setDisable(true);
		clearProfileBtn.setDisable(true);
	}

	@Override
	void unlockGui() {
		LOGGER.debug("unlockGui: Called");
		newProfileBtn.setDisable(false);
		editProfileBtn.setDisable(false);
		renameProfileBtn.setDisable(false);
		deleteProfileBtn.setDisable(false);
		clearProfileBtn.setDisable(false);
	}

	void unlockGui(final String process) {
		if (!othersRunning())
			unlockGui();
		if (!StringUtils.isEmpty(process)) {
			if ("Counter".compareTo(process) == 0) {
				btnRunWordCounter.setDisable(false);
			} else if ("Outliner".compareTo(process) == 0) {
				btnRunOutliner.setDisable(false);
			} else if ("Formatter".compareTo(process) == 0) {
				btnRunFormatter.setDisable(false);
			} else if ("Timeline".compareTo(process) == 0 || "CtrlTImeline".compareTo(process) == 0) {
				btnRunTimeline.setDisable(false);
			}
		}
	}

	private boolean othersRunning() {
		int cnt = 0;
		if (btnRunWordCounter.isDisable())
			cnt++;
		if (btnRunOutliner.isDisable())
			cnt++;
		if (btnRunFormatter.isDisable())
			cnt++;
		// if (btnRunTimeline.isDisable())
		// cnt++;
		if (cnt > 1)
			return true;
		return false;
	}

	@Override
	public void workFinished(final String msg) {
		this.unlockGui(msg);
	}

	@Override
	public void workFinished() {
		this.unlockGui();
	}

	@Override
	public void workFinished(final Object payload) {
		this.unlockGui();
	}

	private void refreshData() {
		// refresh my data
		setProfileChangeMade(false);
		profileManager.loadProfileData();
		loadTableData();
		unselectProfile();
		fixFocus();
	}

	private void startTimerTask() {
		if (myTimerTask == null) {
			LOGGER.debug("TIMER restarted");
			this.myTimerTask = new MyFilterTimerTask();
			timer.cancel();
			timer = new Timer();
			timer.schedule(myTimerTask, 1000);
		}
	}

	private void endTimerTask() {
		LOGGER.debug("TIMER TASK stopped");
		myTimerTask = null;
	}

	class MyFilterTimerTask extends TimerTask {

		@Override
		public void run() {
			// textF.setText("");
			LOGGER.debug("TIMER TASK RUN");
			final Set<String> keys = filters.keySet();
			for (final String key : keys) {
				final String val = filters.get(key);
				LOGGER.debug("key='" + key + "' val='" + val + "'");
			}
			loadTableData();
			endTimerTask();
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupTable() {
		//
		final ObservableList<TableColumn<Profile, ?>> columns = profileTable.getColumns();

		final TableColumn colK = columns.get(0);
		colK.setCellValueFactory(new PropertyValueFactory<>("key"));
		final TableColumn colS = columns.get(1);
		colS.setCellValueFactory(new PropertyValueFactory<>("seriesTitle"));
		final TableColumn colM = columns.get(2);
		colM.setCellValueFactory(new PropertyValueFactory<>("mainTitle"));
		final TableColumn colT = columns.get(3);
		colT.setCellValueFactory(new PropertyValueFactory<>("subTitle"));
		final TableColumn colW = columns.get(4);
		colW.setCellValueFactory(new PropertyValueFactory<>("keywords"));

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
				columnWidthChanged(COL_SERIES, newValue);
			}
		});
		colM.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(COL_MAINTITLE, newValue);
			}
		});
		colT.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(COL_SUBTITLE, newValue);
			}
		});
		colW.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(COL_KEYWORDS, newValue);
			}
		});

		// Hack: align column headers to the center.
		BaseCtrl.alignColumnLabelsLeftHack(profileTable);

		profileTable.setRowFactory(new Callback<TableView<Profile>, TableRow<Profile>>() {
			@Override
			public TableRow<Profile> call(TableView<Profile> tableView) {
				final TableRow<Profile> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final Profile rowData = row.getItem();
							LOGGER.debug("rowData: left2: " + rowData);
							// inputName.setText(rowData.getName());
							// rowData.setExport(!rowData.isExport());
							selectProfile(rowData);
						} else if (event.isSecondaryButtonDown()) {
							// right click code here
							final Profile rowData = row.getItem();
							LOGGER.debug("rowData: right: " + rowData);
							// rowData.setExport(true);
						}
						profileTable.refresh();
					}
				});
				return row;
			}
		});
		profileTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		// GUIUtils.autoFitTable(profileTable);
	}

	/*
	 * LOADING Functions
	 */

	@SuppressWarnings("unchecked")
	private void loadTableData() {
		LOGGER.debug("loadTableData: Called");
		final ObservableList<Profile> newList = FXCollections.observableArrayList();
		final List<Profile> profiles = profileManager.getProfiles();
		for (Profile profile : profiles) {
			final Set<String> keys = filters.keySet();
			boolean dataFail = false;
			// boolean dataOk = true;
			// if (keys != null && keys.size() > 0)
			// dataOk = false;
			for (final String key : keys) {
				if (dataFail)
					continue;
				// dataFail = false;
				final String val = filters.get(key);
				// LOGGER.debug("loadTableData: checking for key:'" + key +
				// "'");
				if ("Key" == key && val != null && val.length() > 0) {
					if (!profile.getKey().toLowerCase().contains(val.toLowerCase()))
						dataFail = true;
				}
				if ("Name" == key && val != null && val.length() > 0 && !dataFail) {
					if (!profile.getMainTitle().toLowerCase().contains(val.toLowerCase())
							&& !profile.getSubTitle().toLowerCase().contains(val.toLowerCase()))
						dataFail = true;
				}
				if ("Series" == key && val != null && val.length() > 0 && !dataFail) {
					if (!profile.getSeriesTitle().toLowerCase().contains(val.toLowerCase()))
						dataFail = true;
				}
				if ("Keyword" == key && val != null && val.length() > 0 && !dataFail) {
					if (!profile.getKeywords().toLowerCase().contains(val.toLowerCase()))
						dataFail = true;
				}
				if (dataFail)
					continue;
			}
			if (dataFail)
				continue;
			// if (dataOk)
			newList.add(profile);
		}
		profileTable.getItems().clear();
		profileTable.getItems().setAll(newList);
		profileTable.refresh();
		LOGGER.debug("loadTableData: Done");
	}

	private void fixFocus() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				clearProfileBtn.requestFocus();
			}
		});
	}

	// private void unlockGui() {
	// // if (runningMutex) {
	// // return;
	// // }
	// // Prevent Actions
	// // deleteProfileBtn.setDisable(true);
	// unlockGuiPerProfile();
	// runningMutex = false;
	// }

	// Prevent Actions when there is no profile selected
	private void lockGuiPerNoProfile() {
		btnRunWordCounter.setDisable(true);
		btnRunOutliner.setDisable(true);
		btnRunFormatter.setDisable(true);
		btnRunTimeline.setDisable(true);
		// btnCmdBookLookup.setDisable(true);

		editProfileBtn.setDisable(true);
		renameProfileBtn.setDisable(true);
		deleteProfileBtn.setDisable(true);
		clearProfileBtn.setDisable(true);

		chosenProfileText.getStyleClass().clear();
		chosenProfileText.getStyleClass().add("chosenProfileNone");
	}

	// Allow Actions when there is no profile selected
	private void unlockGuiPerProfile() {
		btnRunWordCounter.setDisable(false);
		btnRunOutliner.setDisable(false);
		btnRunFormatter.setDisable(false);
		btnRunTimeline.setDisable(false);
		// btnCmdBookLookup.setDisable(false);

		editProfileBtn.setDisable(false);

		renameProfileBtn.setDisable(false);
		deleteProfileBtn.setDisable(false);
		clearProfileBtn.setDisable(false);
		btnRunOutliner.requestFocus();

		chosenProfileText.getStyleClass().clear();
		chosenProfileText.getStyleClass().add("chosenProfileSet");

	}

	// Reset all UI to base/empty settings
	// private void resetGui() {
	// // chosenProfileText.setText("NO PROFILE");
	// // chosenProfileText1.setText("NO PROFILE");
	// // titleComboText.getSelectionModel().clearSelection();
	// // seriesTitleComboText.getSelectionModel().clearSelection();
	//
	// lockGui();
	// fixFocus();
	// }

	/*
	 * HANDLE Functions
	 */
	public void handleClose(final ActionEvent event) {
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		persist();
		doCleanup();
		stage.close();
	}

	public void handleRunTimeline(final ActionEvent event) {
		LOGGER.debug("handleRunCounter: Called");
		// lockGui();
		try {
			btnRunTimeline.setDisable(true);
			// btnRunTimeline.setDisable(true);
			// final BaseRunner br = new BaseRunner();
			// br.handleRunTimeline(this, profileManager, this.selectedProfile,
			// loggingText, myWorkDoneNotify);

			if (selectedProfile == null) {
				LOGGER.debug("Please select a profile before running!!");
				loggingText.appendText("Please select a profile before running!!");
				// notifyCtrl.errorWithWork("Counter", "Please select a profile
				// before running!!");
				return;
			}

			final Map<String, Object> paramsMap = new HashMap<>();
			paramsMap.put("appVersion", appVersion);
			paramsMap.put("selectedProfile", selectedProfile);
			paramsMap.put("profileManager", profileManager);
			paramsMap.put("processDoneNotify", myWorkDoneNotify);
			// TODO extract
			final String WINDOW_TITLE_FMT = "EchoWrite: Timeline: (v%s)";
			final String windowTitle = String.format(WINDOW_TITLE_FMT, appProps.getProperty("version"));
			openNewWindow(BaseCtrl.WINDOWKEY_TIMELINE, windowTitle, loggingText, primaryStage, this, paramsMap, false);

		} catch (Exception e) {
			e.printStackTrace();
			// unlockGui();
			btnRunTimeline.setDisable(false);
		}
		LOGGER.debug("handleRunTimeline: Done");
	}

	public void handleRunCounter(final ActionEvent event) {
		LOGGER.debug("handleRunCounter: Called");
		lockGui();
		try {
			btnRunWordCounter.setDisable(true);
			final BaseRunner br = new BaseRunner();
			br.handleRunCounter(this, profileManager, this.selectedProfile, loggingText, myWorkDoneNotify);
		} catch (Exception e) {
			e.printStackTrace();
			// unlockGui();
			btnRunWordCounter.setDisable(false);
		}
		LOGGER.debug("handleRunCounter: Done");
	}

	public void handleRunOutliner(final ActionEvent event) {
		LOGGER.debug("handleRunOutliner: Called");
		lockGui();
		try {
			btnRunOutliner.setDisable(true);
			final BaseRunner br = new BaseRunner();
			br.handleRunOutliner(this, profileManager, this.selectedProfile, loggingText, myWorkDoneNotify);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("ERROR: " + e.getMessage(), false);
			btnRunOutliner.setDisable(false);
		}
		LOGGER.debug("handleRunOutliner: Done");
	}

	public void handleRunFormatter(final ActionEvent event) {
		LOGGER.debug("handleRunFormatter: Called");
		lockGui();
		try {
			btnRunFormatter.setDisable(true);
			final BaseRunner br = new BaseRunner();
			br.handleRunFormatter(this, profileManager, this.selectedProfile, loggingText, myWorkDoneNotify);
		} catch (Exception e) {
			e.printStackTrace();
			btnRunFormatter.setDisable(false);
		}
		LOGGER.debug("handleRunFormatter: Done");
	}

	public void handleProfileClear(final ActionEvent event) {
		LOGGER.debug("handleProfileClear: Called");
		unselectProfile();
		LOGGER.debug("handleProfileClear: Done");
	}

	public void handleProfileNew(final ActionEvent event) {
		LOGGER.debug("handleProfileNew: Called");
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("NEW", true);
		paramsMap.put("selectedProfile", null);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_PROFILE_NEW, windowTitle, loggingText, primaryStage, this, paramsMap);
		refreshData();
		LOGGER.debug("handleProfileNew: Done");
	}

	public void handleProfileDelete(final ActionEvent event) {
		LOGGER.debug("handleProfileDelete: Called");
		if (selectedProfile == null) {
			showMessage("No profile selected to delete", false);
			return;
		}

		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("DELETE", true);

		paramsMap.put("selectedProfile", selectedProfile);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_PROFILE_DELETE, windowTitle, loggingText, primaryStage, this, paramsMap);

		refreshData();
		LOGGER.debug("handleProfileDelete: Done");
	}

	public void handleProfileRenameEdit(final ActionEvent event) {
		LOGGER.debug("handleProfileRenameEdit: Called");
		//
		profileManager.renameProfile(selectedProfile, inputProfileKey.getText());
		refreshData();
		LOGGER.debug("handleProfileRenameEdit: Done");
	}

	// Handles Showing the Profile Details
	public void handleProfileEdit(final ActionEvent event) {
		LOGGER.debug("handleProfileEdit: Called");
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		setLastSelectedDirectory(selectedProfile.getInputFile());
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_PROFILE_EDIT, windowTitle, loggingText, primaryStage, this, paramsMap);
		// TODO reselect profile? final Profile selectedProfileL =
		// selectedProfile;
		refreshData();
		// selectProfile(profile);
		LOGGER.debug("handleProfileEdit: Done");
	}

	public void handleProfilesDeleteAll(final ActionEvent event) {
		LOGGER.debug("handleProfilesDeleteAll: Called");

		final ConfirmResultDeleteAllProfiles confirmResultDelete = new ConfirmResultDeleteAllProfiles();
		showConfirmDialog(getLocalizedText("text_deleteprofiles_title", "Delete ALL Profiles?"),
				getLocalizedText("text_deleteprofiles_text",
						"Really Delete all Saved Profiles Data, including Series/Titles/More Files?\r\nThis can not be undone."),
				confirmResultDelete);
		LOGGER.debug("handleDelete: Done");
		LOGGER.debug("handleProfilesDeleteAll: Done");
	}

	public void handleImport(final ActionEvent event) {
		LOGGER.debug("handleImport: Called");

		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_IMPORT, windowTitle, loggingText, primaryStage, this, paramsMap);

		LOGGER.debug("handleImport: Done");
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_EXPORT, windowTitle, loggingText, primaryStage, this, paramsMap);

		LOGGER.debug("handleExport: Done");
	}

	public void handleRefreshProfiles(final ActionEvent event) {
		LOGGER.debug("handleRefreshProfiles: Called");
		setProfileChangeMade(false);
		// setupTable();
		profileManager.loadProfileData();
		loadTableData();
		showMessage("--Refreshed Profile Data--", false);
		LOGGER.debug("handleRefreshProfiles: Done");
	}

	public void handleSettingsClear(final ActionEvent event) {
		LOGGER.debug("handleSettingsClear: Called");

		final ConfirmResultDelete confirmResultDelete = new ConfirmResultDelete();
		showConfirmDialog("Delete?", "Really Delete all Saved Settings Data?", confirmResultDelete);
		LOGGER.debug("handleDelete: Done");
		LOGGER.debug("handleSettingsClear: Done");
	}

	public void handleHelpAbout(final ActionEvent event) {
		LOGGER.debug("handleHelpAbout: Called");
		// TODO
		LOGGER.debug("handleHelpAbout: Done");
	}

	public void handleHelpTest1(final ActionEvent event) {
		LOGGER.debug("handleHelpTest1: Called");
		//
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_EXTERNALLINKS, windowTitle, loggingText, primaryStage, this, paramsMap);
		//
		LOGGER.debug("handleHelpTest1: Done");
	}

	public void handleClearLog(final ActionEvent event) {
		LOGGER.debug("handleClearLog: Called");
		loggingText.clear();
		LOGGER.debug("handleClearLog: Done");
	}

	// btnCmdBookLookup
	public void handleRunBookLookup(final ActionEvent event) {
		LOGGER.debug("handleRunBookLookup: Called");
		//
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_EXTERNALLINKS, windowTitle, loggingText, primaryStage, this, paramsMap);
		//
		LOGGER.debug("handleRunBookLookup: Done");
	}

	/*
	 * Work Functions
	 */
	public void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");
		if (myTimerTask != null)
			myTimerTask.cancel();
		if (timer != null)
			timer.cancel();
		myTimerTask = null;
		timer = null;
	}

	void showMessage(final String msg) {
		showMessage(msg, false, loggingText);
	}

	void showMessage(final String msg, final boolean clearPrevious) {
		showMessage(msg, clearPrevious, loggingText);
	}

	private void persist() {
		// getPrefs().put("lastSelectedDirectory",
		// lastSelectedDirectory.getAbsolutePath());
	}

	private void setupFilter(final String filterKey, String filterValue) {
		LOGGER.debug("setupFilter: filterKey='" + filterKey + "' filterValue='" + filterValue + "'");
		if (StringUtils.isEmpty(filterValue)) {
			filters.remove(filterKey);
			LOGGER.debug("setupFilter: removed filterKey='" + filterKey + "'");
		} else
			filters.put(filterKey, filterValue);
		startTimerTask();
	}

	private void selectProfile(final Profile profile) {
		LOGGER.debug("selectProfile: profile: " + profile);
		this.selectedProfile = profile;
		if (profile != null) {
			if (!StringUtils.isEmpty(profile.getSeriesTitle())) {
				final String msg = String.format("[%s] %s - %s (%s)", profile.getKey(), profile.getMainTitle(),
						profile.getSubTitle(), profile.getSeriesTitle());
				chosenProfileText.setText(msg);
			} else {
				final String msg = String.format("[%s] %s", profile.getKey(), profile.getMainTitle(),
						profile.getSeriesTitle());
				chosenProfileText.setText(msg);
			}
			//
			inputProfileKey.setText(profile.getKey());
			inputFileText.setText(profile.getInputFile());
			mainTitleText.setText(profile.getMainTitle());
			subTitleText.setText(profile.getSubTitle());
			seriesTitleText.setText(profile.getSeriesTitle());
			volumeText.setText(profile.getVolume());
			keywordsText.setText(profile.getKeywords());

			final String lastSelectedDirectoryStr = profile.getInputFile();
			if (!StringUtils.isEmpty(lastSelectedDirectoryStr)) {
				setLastSelectedDirectory(lastSelectedDirectoryStr);
			}
			//
			unlockGuiPerProfile();
			// showMessage("Selected Profile: <" + chosenProfileText.getText() +
			// ">");
			showMessage(String.format("Selected Profile: <%s> %s", chosenProfileText.getText(), "\n"));
		}
	}

	private void unselectProfile() {
		chosenProfileText.setText("NO PROFILE");
		inputProfileKey.setText("");
		inputFileText.setText("");
		mainTitleText.setText("");
		subTitleText.setText("");
		seriesTitleText.setText("");
		volumeText.setText("");
		keywordsText.setText("");
		lockGuiPerNoProfile();
	}

	void setProfileChangeMade(boolean b) {
		if (b) {
			// profileDataChanged.setText("Unsaved Changes");
			// profileDataChanged.getStyleClass().clear();
			// profileDataChanged.getStyleClass().add("highlightedOnOutlinedText");
			// btnCloseScreen.setText("C_ancel");
		} else {
			// profileDataChanged.setText("Up to date");
			// profileDataChanged.getStyleClass().clear();
			// profileDataChanged.getStyleClass().add("highlightedOffOutlinedText");
			// btnCloseScreen.setText("_Back");
		}
	}
	// private void setProfileChangeMade(boolean b) {
	// if (b) {
	// profileDataChanged.setText("Unsaved Changes");
	// // profileDataChanged1.setText("Unsaved Changes");
	// } else {
	// profileDataChanged.setText("");
	// // profileDataChanged1.setText("");
	// }
	// setProfileChangeMade(false);
	// }

	private void splitVertChanged(Number newVal) {
		if (appPreferences != null) {
			appPreferences.putDouble(PREF_SPLIT_V, newVal.doubleValue());
		}
	}

	private void splitHorizChanged(Number newVal) {
		if (appPreferences != null) {
			LOGGER.debug("splitHorizChanged: splitH=" + newVal.doubleValue());
			appPreferences.putDouble(PREF_SPLIT_H, newVal.doubleValue());
		}
	}

	private void columnWidthChanged(final int colNum, final Number newValue) {
		if (appPreferences != null) {
			final String key = String.format(PREF_COL_S, colNum);
			// LOGGER.debug("columnWidthChanged: col#" + colNum + " val=" +
			// newValue.doubleValue());
			appPreferences.putDouble(key, newValue.doubleValue());
		}
	}

	/*
	 * XYX Functions
	 */
	class ConfirmResultDeleteAllProfiles implements ConfirmResult {

		@Override
		public void actionConfirmed(String title) {
			LOGGER.debug("actionConfirmed: Called");

			// TODO

			refreshData();
			LOGGER.debug("actionConfirmed: Done");
		}

		@Override
		public void actionCancelled(String title) {
			LOGGER.debug("actionCancelled: Called");
			refreshData();
			LOGGER.debug("actionCancelled: Done");
		}

	}

	class ConfirmResultDelete implements ConfirmResult {

		@Override
		public void actionConfirmed(final String title) {
			LOGGER.debug("actionConfirmed: Called");

			appPreferences.remove(PREF_SPLIT_H);
			appPreferences.remove(PREF_SPLIT_V);

			appPreferences.remove(VIEW_WINDOW_X);
			appPreferences.remove(VIEW_WINDOW_Y);
			appPreferences.remove(VIEW_WINDOW_W);
			appPreferences.remove(VIEW_WINDOW_H);

			String key = String.format(PREF_COL_S, 1);
			appPreferences.remove(key);
			key = String.format(PREF_COL_S, 2);
			appPreferences.remove(key);
			key = String.format(PREF_COL_S, 3);
			appPreferences.remove(key);
			key = String.format(PREF_COL_S, 4);
			appPreferences.remove(key);
			key = String.format(PREF_COL_S, 5);
			appPreferences.remove(key);

			refreshData();
			LOGGER.debug("actionConfirmed: Done");
		}

		@Override
		public void actionCancelled(String title) {
			LOGGER.debug("actionCancelled: Called");
			refreshData();
			LOGGER.debug("actionCancelled: Done");
		}
	}

}