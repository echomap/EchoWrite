package com.echomap.kqf.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.two.gui.AutoCompleteComboBoxListener;
import com.echomap.kqf.two.gui.KQFCtrl;
import com.echomap.kqf.two.gui.WorkDoneNotify;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CtrlStart extends BaseCtrl implements Initializable, WorkDoneNotify {
	private final static Logger LOGGER = LogManager.getLogger(CtrlStart.class);

	File lastSelectedDirectory = null;
	Stage primaryStage = null;
	final ProfileManager profileManager;
	// final ProfileBiz profileBiz;
	// final Preferences userPrefs;
	// Properties appProps = null;mothra1

	String appVersion = null;
	private boolean runningMutex = false;

	@SuppressWarnings("rawtypes")
	AutoCompleteComboBoxListener profileComboBoxListener = null;
	@SuppressWarnings("rawtypes")
	AutoCompleteComboBoxListener profileSeriesComboBoxListener = null;

	@FXML
	private Label chosenProfileText;
	@FXML
	private Label chosenProfileText1;
	@FXML
	private Label profileDataChanged;
	@FXML
	private Label profileDataChanged1;

	@FXML
	private Label versionLabel;
	@FXML
	private GridPane outerMostContainer;
	@FXML
	private TextArea loggingText;
	@FXML
	private ComboBox<String> titleComboText;
	@FXML
	private ComboBox<String> seriesTitleComboText;

	@FXML
	private Button newProfileBtn;
	@FXML
	private Button loadProfileBtn;
	@FXML
	private Button deleteProfileBtn;
	@FXML
	private Button importProfileBtn;
	@FXML
	private Button exportProfileBtn;

	@FXML
	private Button btnRunWordCounter;
	@FXML
	private Button btnRunOutliner;
	@FXML
	private Button btnRunFormatter;
	@FXML
	private Button handleProfileClear;

	public CtrlStart() {
		// super(outputCountDir, primaryStage, appProps)
		// userPrefs = Preferences.userNodeForPackage(CtrlStart.class);
		final Preferences userPrefsV1 = Preferences.userNodeForPackage(KQFCtrl.class);
		// profileBiz = new ProfileBiz(userPrefs2);
		profileManager = new ProfileManager();
	}

	@Override
	public void initialize(java.net.URL arg0, ResourceBundle arg1) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);
		// loadDefaults();
		// loadChoices();
		// loadProfiles();
		lockGui();
		fixFocus();
		// setDetectChanges(OuterMostContainer);
		setProfileChangeMade(false);
		profileComboBoxListener = new AutoCompleteComboBoxListener<>(titleComboText);
		profileSeriesComboBoxListener = new AutoCompleteComboBoxListener<>(seriesTitleComboText);

		seriesTitleComboText.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
				loadSeries();
			}
		});
		LOGGER.debug("initialize: Done");
	}

	// Called from change listener for seriesTitleComboText
	private void loadSeries() {
		LOGGER.debug("loadSeries: Called");
		// load all profiles into combobox that have this series?
		final String series = seriesTitleComboText.getSelectionModel().getSelectedItem();
		if (StringUtils.isBlank(series)) {
			// setupProfiles();
			loadProfiles();
		} else {
			loadProfilesBySeries(series);
		}
		LOGGER.debug("loadSeries: Done");
	}

	private void loadProfilesBySeries(final String series) {
		LOGGER.debug("loadProfilesBySeries: Called");
		// loadProfiles();
		if (StringUtils.isBlank(series)) {
			seriesTitleComboText.getSelectionModel().clearSelection();
			return;
		}
		final List<String> keepList = new ArrayList<>();
		final List<Profile> profiles = profileManager.getProfiles();
		for (Profile profile : profiles) {
			if (profile.getSeriesTitle() != null && series.compareTo(profile.getSeriesTitle()) == 0) {
				keepList.add(profile.getKey());
			}
		}
		titleComboText.getItems().clear();
		for (String e : keepList) {
			titleComboText.getItems().add(e);
		}
		// seriesTitleComboText.getSelectionModel().select(series);
		LOGGER.debug("loadProfilesBySeries: Done");
	}

	private void loadProfiles() {
		LOGGER.debug("loadProfiles: Called");

		titleComboText.getItems().clear();
		final List<Profile> profiles = profileManager.getProfiles();
		for (Profile profile : profiles) {
			titleComboText.getItems().add(profile.getKey());
		}
		// seriesTitleComboText.getSelectionModel().select(series);
		LOGGER.debug("loadProfiles: Done");
	}

	/*
	 * LOADING Functions
	 */

	private void setProfileChangeMade(final boolean b) {
		if (b) {
			profileDataChanged.setText("Unsaved Changes");
			profileDataChanged1.setText("Unsaved Changes");
		} else {
			profileDataChanged.setText("");
			profileDataChanged1.setText("");
		}
	}

	private void fixFocus() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				titleComboText.requestFocus();
			}
		});
	}

	void lockGui() {
		if (runningMutex) {
			return;
		}
		// Prevent Actions
		// deleteProfileBtn.setDisable(true);
		lockGuiPerNoProfile();
	}

	void unlockGui() {
		// if (runningMutex) {
		// return;
		// }
		// Prevent Actions
		// deleteProfileBtn.setDisable(true);
		unlockGuiPerProfile();
		runningMutex = false;
	}

	// Prevent Actions when there is no profile selected
	private void lockGuiPerNoProfile() {
		// Prevent Actions
		deleteProfileBtn.setDisable(true);

		btnRunWordCounter.setDisable(true);
		btnRunOutliner.setDisable(true);
		btnRunFormatter.setDisable(true);
		// handleProfileClear.setDisable(true);
	}

	// Allow Actions when there is no profile selected
	private void unlockGuiPerProfile() {
		deleteProfileBtn.setDisable(false);

		btnRunWordCounter.setDisable(false);
		btnRunOutliner.setDisable(false);
		btnRunFormatter.setDisable(false);
		// handleProfileClear.setDisable(false);
	}

	// Reset all UI to base/empty settings
	private void resetGui() {
		chosenProfileText.setText("NO PROFILE");
		chosenProfileText1.setText("NO PROFILE");
		titleComboText.getSelectionModel().clearSelection();
		seriesTitleComboText.getSelectionModel().clearSelection();

		lockGui();
		fixFocus();
	}

	private void setupProfiles() {
		LOGGER.debug("setupProfiles: Called ");
		titleComboText.getItems().clear();
		seriesTitleComboText.getSelectionModel().clearSelection();
		seriesTitleComboText.getItems().clear();

		final List<String> seriesTitles = new ArrayList<>();
		final List<Profile> profiles = profileManager.getProfiles();
		if (profiles != null && profiles.size() > 0) {
			for (final Profile profile : profiles) {
				titleComboText.getItems().add(profile.getMainTitle());
				if (profile.getSeriesTitle() != null && !seriesTitles.contains(profile.getSeriesTitle()))
					seriesTitles.add(profile.getSeriesTitle());
			}
		}
		for (final String series : seriesTitles) {
			seriesTitleComboText.getItems().add(series);
		}
		// final List<ProfileData> profiles = profileBiz.loadProfileData();
		// for (ProfileData profile : profiles) {
		// LOGGER.debug("loadProfiles: ProfileData: " + profile);
		// }
		// final String[] prefkeys = userPrefs.childrenNames();
		// if (prefkeys != null && prefkeys.length > 0) {
		// for (final String str1 : prefkeys) {
		// if (!StringUtils.isBlank(str1))
		// titleComboText.getItems().add(str1);
		// }
		// }
		// for (String key : titleComboText.getItems()) {
		// final Preferences child = userPrefs.node(key);
		// final String seriesTitle = child.get("seriesTitle", "");
		// if (!seriesTitleComboText.getItems().contains(seriesTitle)) {
		// seriesTitleComboText.getItems().add(seriesTitle);
		// }
		// }

		// } catch (BackingStoreException e) {
		// showMessage("Error Deleting profile: " + e, false, loggingText);
		// e.printStackTrace();
		// }
		LOGGER.debug("setupProfiles: Done ");
	}

	private void loadChoices() {
		LOGGER.debug("loadChoices: Called ");
		LOGGER.debug("loadChoices: Done ");
	}

	private void setTooltips(GridPane outerMostContainer2) {
		LOGGER.debug("setTooltips: Called ");
		LOGGER.debug("setTooltips: Done ");
	}

	/*
	 * WORK Functions
	 */
	@Override
	public void finalResultFromWork(String msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void finishedWithWork(String msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void errorWithWork(String msg, Exception e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void errorWithWork(String msg, Throwable e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void errorWithWork(String msg, String key) {
		// TODO Auto-generated method stub
	}

	@Override
	public void statusUpdateForWork(String header, String msg) {
		// TODO Auto-generated method stub
	}

	/*
	 * SETUP Functions
	 */

	// Called after initialize
	public void setupController(final Properties props) {
		LOGGER.debug("setupController: Called");
		this.appProps = props;
		this.appVersion = appProps.getProperty(PROP_KEY_VERSION);
		// profileBiz.setVersion(this.appVersion);
		profileManager.setAppVersion(appVersion);
		versionLabel.setText(this.appVersion);
		profileManager.loadProfileData();

		loadDefaults();
		loadChoices();
		setupProfiles();
		lockGui();
		fixFocus();

		LOGGER.debug("setupController: Done");
	}

	public void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");
		// myTimerTask.cancel();
		// timer.cancel();
		// myTimerTask = null;
		// timer = null;
	}

	private void loadDefaults() {
		LOGGER.debug("loadDefaults: Called");
		final String outC = loadPropFromAppOrDefault("outlineCompress",
				"subscene, outlinedata, outlinesub, suboutline");
		final String outE = loadPropFromAppOrDefault("outineExpand", "outline, scene");
		// outputDocTagsOutlineCTagsText.setText(outC);
		// outputDocTagsOutlineETagsText.setText(outE);

		final String sceneM = loadPropFromAppOrDefault("sceneMain", "scene, subscene");
		final String sceneC = loadPropFromAppOrDefault("sceneCoalate", "description, scene");
		// outputDocTagsSceneTagsText.setText(sceneM);
		// outputDocTagsSceneCoTags.setText(sceneC);
		// outputDocTagsOther1TagsText.setText(sceneM);

		final String regExpChp = loadPropFromAppOrDefault("regexpChapterText", "");
		final String regExpSec = loadPropFromAppOrDefault("regexpSectionText", "");
		// regexpChapterText.setText(regExpChp);
		// regexpSectionText.setText(regExpSec);

		// final String lastSelectedDirectoryStr =
		// userPrefs.get("lastSelectedDirectory", null);
		// if (!StringUtils.isEmpty(lastSelectedDirectoryStr)) {
		// lastSelectedDirectory = new File(lastSelectedDirectoryStr);
		// }
		LOGGER.debug("loadDefaults: Done");
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

	private void persist() {
		// getPrefs().put("lastSelectedDirectory",
		// lastSelectedDirectory.getAbsolutePath());
	}

	public void handleImportProfile(final ActionEvent event) {
		LOGGER.debug("handleImportProfile: Called");
		LOGGER.debug("handleImportProfile: Done");
	}

	public void handleExportProfile(final ActionEvent event) {
		LOGGER.debug("handleExportProfile: Called");
		LOGGER.debug("handleExportProfile: Done");
	}

	public void handleProfileSelectSeriesAction(final ActionEvent event) {
		LOGGER.debug("handleProfileSelectSeriesAction: Called");
		//
		LOGGER.debug("handleProfileSelectSeriesAction: Done");
	}

	public void handleProfileNew(final ActionEvent event) {
		LOGGER.debug("handleProfileNew: Called");
		LOGGER.debug("handleProfileNew: Done");
	}

	public void handleProfileSelectAction(final ActionEvent event) {
		LOGGER.debug("handleProfileSelectAction: Called");
		final String profileKey = titleComboText.getSelectionModel().getSelectedItem();
		final Profile selProfile = profileManager.selectProfile(profileKey);
		// check is not null
		if (!StringUtils.isEmpty(profileKey)) {
			if (selProfile != null && !StringUtils.isEmpty(selProfile.getSeriesTitle())) {
				final String msg = String.format("%s (%s)", profileKey, selProfile.getSeriesTitle());
				chosenProfileText.setText(msg);
				chosenProfileText1.setText(msg);
			} else {
				chosenProfileText.setText(profileKey);
				chosenProfileText1.setText(profileKey);
			}
			unlockGuiPerProfile();
		}
		LOGGER.debug("handleProfileSelectAction: Done");
	}

	public void handleProfileLoad(final ActionEvent event) {
		LOGGER.debug("handleProfileLoad: Called");
		LOGGER.debug("handleProfileLoad: Done");
	}

	public void handleProfileDelete(final ActionEvent event) {
		LOGGER.debug("handleProfileDelete: Called");
		LOGGER.debug("handleProfileDelete: Done");
	}

	public void handleRunCounter(final ActionEvent event) {
		LOGGER.debug("handleRunCounter: Called");
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}

		final String profileKey = titleComboText.getSelectionModel().getSelectedItem();
		final Profile selProfile = profileManager.selectProfile(profileKey);
		if (StringUtils.isEmpty(profileKey)) {
			LOGGER.debug("Please select a profile before running!!");
			return;
		}
		if (selProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			return;
		}

		lockGui();
		this.runningMutex = true;

		LOGGER.info("Running count action");
		showMessage("Running Count Process (" + getCurrentDateFmt() + ")", false, loggingText);
		// timer.cancel();
		// timer = new Timer();
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, profileKey);

			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.count(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlockGui();
			String fmtt = "Done running COUNT ( " + getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
			// setLastRunText(fmtt);
			// startTimerTask();
		}
		LOGGER.debug("handleRunCounter: Done");
	}

	public void handleRunOutliner(final ActionEvent event) {
		LOGGER.debug("handleRunOutliner: Called");
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}

		final String profileKey = titleComboText.getSelectionModel().getSelectedItem();
		final Profile selProfile = profileManager.selectProfile(profileKey);
		if (StringUtils.isEmpty(profileKey)) {
			LOGGER.debug("Please select a profile before running!!");
			return;
		}
		if (selProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			return;
		}

		lockGui();
		this.runningMutex = true;

		LOGGER.info("Running outline action");
		showMessage("Running Outline Process (" + getCurrentDateFmt() + ")", false, loggingText);
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, profileKey);

			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.outline(formatDao);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlockGui();
			String fmtt = "Done running COUNT ( " + getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
			// setLastRunText("Done running OUTLINE (" + getCurrentDateFmt() +
			// ")");
			// startTimerTask();
		}
		LOGGER.debug("handleRunOutliner: Done");
	}

	public void handleRunFormatter(final ActionEvent event) {
		LOGGER.debug("handleRunFormatter: Called");
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}

		final String profileKey = titleComboText.getSelectionModel().getSelectedItem();
		final Profile selProfile = profileManager.selectProfile(profileKey);
		if (StringUtils.isEmpty(profileKey)) {
			LOGGER.debug("Please select a profile before running!!");
			return;
		}
		if (selProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			return;
		}

		lockGui();
		this.runningMutex = true;

		LOGGER.info("Running format action");
		showMessage("Running Format Process (" + getCurrentDateFmt() + ")", false, loggingText);
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, profileKey);

			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.format(formatDao);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlockGui();
			String fmtt = "Done running FORMAT ( " + getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
			// startTimerTask();
		}
		LOGGER.debug("handleRunFormatter: Done");
	}

	/*
	 * XXX Functions
	 */

	/*
	 * XXX Functions
	 */

}
