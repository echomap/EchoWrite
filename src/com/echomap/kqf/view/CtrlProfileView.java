package com.echomap.kqf.view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
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
import com.echomap.kqf.two.gui.GUIUtils;
import com.echomap.kqf.two.gui.WorkDoneNotify;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class CtrlProfileView extends BaseCtrl implements Initializable { // ,
	// WorkDoneNotify // {
	private final static Logger LOGGER = LogManager.getLogger(CtrlProfileView.class);
	final ProfileManager profileManager;

	File lastSelectedDirectory = null;
	Stage primaryStage = null;
	Profile selectedProfile = null;
	// final ProfileBiz profileBiz;
	// final Preferences userPrefs;
	// Properties appProps = null;
	// String appVersion = null;
	private boolean runningMutex = false;
	private MyWorkDoneNotify myWorkDoneNotify = null;
	final Map<String, String> filters = new HashMap<>();
	Timer timer = new Timer();
	private MyFilterTimerTask myTimerTask;

	@FXML
	private BorderPane outerMostContainer;
	@FXML
	private TextArea loggingText;
	@FXML
	private SplitPane splitVert;
	@FXML
	private SplitPane splitHoriz;

	@FXML
	private Label profileDataChanged;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableView profileTable;
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
	private Button btnRunWordCounter;
	@FXML
	private Button btnRunOutliner;
	@FXML
	private Button btnRunFormatter;

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

	/**
	 * 
	 */
	public CtrlProfileView() {
		profileManager = new ProfileManager();
		profileManager.loadProfileData();
	}

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
			final double splitH = appPreferences.getDouble("view/SplitH", -1);
			final double splitV = appPreferences.getDouble("view/SplitV", -1);
			LOGGER.debug("setupController: splitH=" + splitH);
			if (splitH > -1)
				splitHoriz.setDividerPositions(splitH);
			if (splitV > -1)
				splitVert.setDividerPositions(splitV);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);

		lockGui();
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

		myWorkDoneNotify = new MyWorkDoneNotify(loggingText, loggingText);

		//
		setProfileChangeMade(false);
		setupTable();
		loadTableData();
		LOGGER.debug("initialize: Done");
	}

	@Override
	void lockGui() {
		if (runningMutex) {
			return;
		}
		// Prevent Actions
		// lockGuiPerNoProfile();
		newProfileBtn.setDisable(true);
		editProfileBtn.setDisable(true);

		// btnRunWordCounter
		// btnRunOutliner
		// btnRunFormatter

	}

	void unlockGui(final String process) {
		unlockGui();
		if (!StringUtils.isEmpty(process)) {
			if ("Counter".compareTo(process) == 0) {
				btnRunWordCounter.setDisable(false);
			} else if ("Outliner".compareTo(process) == 0) {
				btnRunOutliner.setDisable(false);
			} else if ("Formattre".compareTo(process) == 0) {
				btnRunFormatter.setDisable(false);
			}
		}
	}

	@Override
	void unlockGui() {
		newProfileBtn.setDisable(false);
		editProfileBtn.setDisable(false);
	}

	private void startTimerTask() {
		if (myTimerTask == null) {
			LOGGER.debug("TIMER restarted");
			this.myTimerTask = new MyFilterTimerTask();
			timer.cancel();
			timer = new Timer();
			timer.schedule(myTimerTask, 2000);
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
		final ObservableList<TableColumn> columns = profileTable.getColumns();
		final TableColumn col1 = columns.get(0);
		col1.setCellValueFactory(new PropertyValueFactory<>("key"));
		final TableColumn col2 = columns.get(1);
		col2.setCellValueFactory(new PropertyValueFactory<>("mainTitle"));
		final TableColumn col3 = columns.get(2);
		col3.setCellValueFactory(new PropertyValueFactory<>("seriesTitle"));
		final TableColumn col4 = columns.get(3);
		col4.setCellValueFactory(new PropertyValueFactory<>("keywords"));

		// Hack: align column headers to the center.
		GUIUtils.alignColumnLabelsLeftHack(profileTable);

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
				dataFail = true;
				final String val = filters.get(key);
				// LOGGER.debug("loadTableData: checking for key:'" + key +
				// "'");
				if ("Key" == key && val != null && val.length() > 0) {
					if (profile.getKey().toLowerCase().contains(val.toLowerCase()))
						dataFail = false;
				} else if ("Name" == key && val != null && val.length() > 0) {
					if (profile.getMainTitle().toLowerCase().contains(val.toLowerCase()))
						dataFail = false;
				} else if ("Series" == key && val != null && val.length() > 0) {
					if (profile.getSeriesTitle().toLowerCase().contains(val.toLowerCase()))
						dataFail = false;
				} else if ("Keyword" == key && val != null && val.length() > 0) {
					if (profile.getKeywords().toLowerCase().contains(val.toLowerCase()))
						dataFail = false;
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
				profileTable.requestFocus();
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
		// Prevent Actions
		deleteProfileBtn.setDisable(true);

		btnRunWordCounter.setDisable(true);
		btnRunOutliner.setDisable(true);
		btnRunFormatter.setDisable(true);
		clearProfileBtn.setDisable(true);
	}

	// Allow Actions when there is no profile selected
	private void unlockGuiPerProfile() {
		deleteProfileBtn.setDisable(false);

		btnRunWordCounter.setDisable(false);
		btnRunOutliner.setDisable(false);
		btnRunFormatter.setDisable(false);
		clearProfileBtn.setDisable(false);
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

	private void setTooltips(Pane outerMostContainer2) {
		LOGGER.debug("setTooltips: Called ");
		// TODO
		LOGGER.debug("setTooltips: Done ");
	}

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

	public void handleClose2(final ActionEvent event) {
		final MenuItem source = (MenuItem) event.getSource();
		final Stage stage = primaryStage;
		// (Stage) source.getScene().getWindow();
		persist();
		doCleanup();
		stage.close();
	}

	public void handleProfileNew(final ActionEvent event) {
		LOGGER.debug("handleProfileNew: Called");
		LOGGER.debug("handleProfileNew: Done");
	}

	public void handleProfileLoad(final ActionEvent event) {
		LOGGER.debug("handleProfileLoad: Called");
		LOGGER.debug("handleProfileLoad: Done");
	}

	public void handleProfileDelete(final ActionEvent event) {
		LOGGER.debug("handleProfileDelete: Called");
		LOGGER.debug("handleProfileDelete: Done");
	}

	class MyWorkDoneNotify implements WorkDoneNotify {

		final List<String> errorsReportedKeys = new ArrayList<>();
		private TextArea summaryReportArea = null;
		private TextArea loggingReportArea = null;

		public MyWorkDoneNotify(final TextArea summaryReportArea, final TextArea loggingReportArea) {
			this.summaryReportArea = summaryReportArea;
			this.loggingReportArea = loggingReportArea;
		}

		private void showSummaryMessage(final String msg, final boolean clearPrevious) {
			if (msg == null || StringUtils.isBlank(msg))
				return;
			if (summaryReportArea == null)
				return;
			final Animation animation = new Transition() {
				{
					setCycleDuration(Duration.millis(2000));
					setInterpolator(Interpolator.EASE_OUT);
				}

				@Override
				protected void interpolate(double frac) {
					Color vColor = new Color(1, 0, 0, 1 - frac);
					summaryReportArea
							.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
				}
			};
			animation.play();

			if (clearPrevious) {
				summaryReportArea.setText(msg);
			} else {
				summaryReportArea.setText(msg + "\r\n" + summaryReportArea.getText());
			}
		}

		private void showMessage(final String msg, final boolean clearPrevious) {
			if (loggingReportArea == null)
				return;
			final Animation animation = new Transition() {
				{
					setCycleDuration(Duration.millis(2000));
					setInterpolator(Interpolator.EASE_OUT);
				}

				@Override
				protected void interpolate(double frac) {
					Color vColor = new Color(1, 0, 0, 1 - frac);
					loggingReportArea
							.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
				}
			};
			animation.play();

			if (clearPrevious) {
				loggingReportArea.setText(msg);
			} else {
				loggingReportArea.setText(msg + "\r\n" + loggingReportArea.getText());
			}
			LOGGER.info(msg);
		}

		@Override
		public void finalResultFromWork(String msg) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					showSummaryMessage(msg, false);
					unlockGui();
					runningMutex = false;
				}
			});
		}

		@Override
		public void finishedWithWork(String msg) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					showMessage("Done running " + msg + " Process (" + getCurrentDateFmt() + ")", false);
					// TODO done type, mapping to enable buttons
					// Counter , Outliner , Form.,..?
					unlockGui(msg);
					runningMutex = false;
				}
			});
		}

		@Override
		public void errorWithWork(final String msg, final String key) {
			if (!errorsReportedKeys.contains(key)) {
				errorsReportedKeys.add(key);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						showMessage("Error running " + msg + " Process (" + getCurrentDateFmt() + ")\n" + msg, false);
						LOGGER.error(msg);
						showSummaryMessage(msg, false);
						unlockGui();
						runningMutex = false;
					}
				});
			}
		}

		@Override
		public void errorWithWork(final String msg, final Exception e) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					showMessage("Error running " + msg + " Process (" + getCurrentDateFmt() + ")\n" + e, false);
					LOGGER.error(e);
					unlockGui();
					runningMutex = false;
				}
			});
		}

		@Override
		public void errorWithWork(final String msg, final Throwable e) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					showMessage("Error running " + msg + " Process (" + getCurrentDateFmt() + ")\n" + e, false);
					LOGGER.error(e);
					unlockGui();
					runningMutex = false;
				}
			});
		}

		@Override
		public void statusUpdateForWork(String header, String msg) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					showMessage("---" + header + " Process, " + msg, false);
				}
			});
		}
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
			unlockGui();
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

	public void handleProfileEdit(final ActionEvent event) {
		LOGGER.debug("handleProfileEdit: Called");
		// TODO test
		openNewWindow("NEWProfile", "windowTitle1", loggingText, primaryStage);
		LOGGER.debug("handleProfileEdit: Done");
	}

	public void handleInputFile(final ActionEvent event) {
		LOGGER.debug("handleInputFile: Called");
		LOGGER.debug("handleInputFile: Done");
	}

	public void handleImport(final ActionEvent event) {
		LOGGER.debug("handleSettingsClear: Called");
		LOGGER.debug("handleSettingsClear: Done");
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleSettingsClear: Called");
		LOGGER.debug("handleSettingsClear: Done");
	}

	public void handleSettingsClear(final ActionEvent event) {
		LOGGER.debug("handleSettingsClear: Called");
		appPreferences.remove("view/SplitV");
		appPreferences.remove("view/SplitH");

		appPreferences.remove("view/WindowX");
		appPreferences.remove("view/WindowY");
		appPreferences.remove("view/WindowW");
		appPreferences.remove("view/WindowH");

		LOGGER.debug("handleSettingsClear: Done");
	}

	public void handleHelpAbout(final ActionEvent event) {
		LOGGER.debug("handleHelpAbout: Called");
		LOGGER.debug("handleHelpAbout: Done");
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
				final String msg = String.format("[%s] %s (%s)", profile.getKey(), profile.getMainTitle(),
						profile.getSeriesTitle());
				chosenProfileText.setText(msg);
			} else {
				final String msg = String.format("[%s] %s", profile.getKey(), profile.getMainTitle(),
						profile.getSeriesTitle());
				chosenProfileText.setText(msg);
			}
			//
			inputFileText.setText(profile.getInputFile());
			mainTitleText.setText(profile.getMainTitle());
			subTitleText.setText(profile.getSubTitle());
			seriesTitleText.setText(profile.getSeriesTitle());
			volumeText.setText(profile.getVolume());
			//
			unlockGuiPerProfile();
		}
	}

	private void unselectProfile() {
		chosenProfileText.setText("NO PROFILE");
		inputFileText.setText("");
		mainTitleText.setText("");
		subTitleText.setText("");
		seriesTitleText.setText("");
		volumeText.setText("");
		lockGuiPerNoProfile();
	}

	private void setProfileChangeMade(boolean b) {
		if (b) {
			profileDataChanged.setText("Unsaved Changes");
		} else {
			profileDataChanged.setText("");
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
			appPreferences.putDouble("view/SplitV", newVal.doubleValue());
		}
	}

	private void splitHorizChanged(Number newVal) {
		if (appPreferences != null) {
			LOGGER.debug("splitHorizChanged: splitH=" + newVal.doubleValue());
			appPreferences.putDouble("view/SplitH", newVal.doubleValue());
		}
	}

	/*
	 * XXX Functions
	 */
}