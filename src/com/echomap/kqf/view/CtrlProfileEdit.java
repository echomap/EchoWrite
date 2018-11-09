package com.echomap.kqf.view;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.looper.FileLooperHandlerCount;
import com.echomap.kqf.looper.FileLooperHandlerFormatter;
import com.echomap.kqf.looper.FileLooperHandlerOutline;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CtrlProfileEdit extends BaseCtrl implements Initializable, WorkFinishedCallback {
	private final static Logger LOGGER = LogManager.getLogger(CtrlProfileView.class);

	final ProfileManager profileManager;
	// private boolean runningMutex = false;
	private MyWorkDoneNotify myWorkDoneNotify = null;
	Profile selectedProfile = null;
	private boolean modeNew = false;
	private boolean modeDelete = false;

	// private File lastSelectedDirectory = null;
	private File outputCountDir;

	@FXML
	private GridPane outerMostContainer;
	@FXML
	private Label chosenProfileText;

	@FXML
	private Button btnCloseScreen;

	@FXML
	private TextArea lastRunText;
	@FXML
	private TextArea summaryRunText;

	@FXML
	private Label profileDataChanged;

	@FXML
	private Button btnRunWordCounter;
	@FXML
	private Button btnRunOutliner;
	@FXML
	private Button btnRunFormatter;

	@FXML
	private TextField regexpChapterText;
	@FXML
	private TextField regexpSectionText;

	@FXML
	private ChoiceBox<String> counterDigitChoice;

	@FXML
	private TextField inputFileText;
	@FXML
	private TextField inputFilePrefixText;
	@FXML
	private TextField mainTitleText;// titleTwoText
	@FXML
	private TextField subTitleText;// titleThreeText
	@FXML
	private TextField seriesTitleText;
	@FXML
	private TextField volumeText;
	@FXML
	private CheckBox filePrefixCheckbox;
	@FXML
	private TextField inputKeyText;

	@FXML
	private TextField outputCountFileText;

	@FXML
	private TextField outputFormatSingleFileText;
	@FXML
	private TextField outputFormatChpHtmlDirText;
	@FXML
	private TextField outputFormatChpTextDirText;
	// @FXML
	// private TextField outputOutlineFileText;
	// @FXML
	// private TextField outputOutlineFileText1;

	@FXML
	private CheckBox cbDropCapChapters;
	@FXML
	private CheckBox cbWantTextChptOutput;
	@FXML
	private CheckBox cbCenterStars;
	@FXML
	private CheckBox cbRemoveDiv;

	@FXML
	private TextField outputOutlineCSVFileText;// outputOutlineFileText;
	@FXML
	private TextField outputOutlineAllCSVFileText;// outputOutlineFileText1;
	@FXML
	private TextField outputDocTagsOutlineFileText;
	@FXML
	private TextField outputDocTagsSceneFileText;

	@FXML
	private TextField outputDocTagsMaxLineLength; // length for doctag output
	@FXML
	private TextArea outputDocTagsOutlineCTagsText;
	@FXML
	private TextArea outputDocTagsOutlineETagsText;
	@FXML
	private TextArea outputDocTagsSceneCoTags;
	@FXML
	private TextField outputDocTagsScenePrefix;
	@FXML
	private TextArea outputDocTagsSceneTagsText;
	@FXML
	private TextField outputDocTagsSubScenePrefix;
	@FXML
	private TextField sceneCoalateDiv;

	@FXML
	private TextField outputEncoding;
	@FXML
	private TextField chapterHeaderTag;
	@FXML
	private TextField sectionHeaderTag;
	@FXML
	private TextField fmtModeText;
	@FXML
	private TextField docTagStartText;
	@FXML
	private TextField docTagEndText;

	@FXML
	private Button saveProfileBtn;
	@FXML
	private Button deleteProfileBtn;
	@FXML
	private Button importProfileBtn;
	@FXML
	private Button exportProfileBtn;

	/**
	 * 
	 */
	public CtrlProfileEdit() {
		profileManager = new ProfileManager();
		// profileManager.loadProfileData();
	}

	/*
	 * SETUP Functions
	 */

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
		}
		final Object selNewO = paramsMap.get("NEW");
		if (selNewO != null && selNewO instanceof Boolean) {
			modeNew = (Boolean) selNewO;
		}
		final Object selDelO = paramsMap.get("DELETE");
		if (selDelO != null && selDelO instanceof Boolean) {
			modeDelete = (Boolean) selDelO;
		}

		loadData();
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);
		//
		lockGui();
		fixFocus();
		//
		myWorkDoneNotify = new MyWorkDoneNotify(lastRunText, summaryRunText, this);
		//
		setProfileChangeMade(false);
		loadData();
		LOGGER.debug("initialize: Done");
	}

	@Override
	void doCleanup() {
		if (primaryStage != null) {
			// primaryStage.refreshData();
		}
	}

	@Override
	void lockGui() {
		// lockAllButtons(outerMostContainer);
		btnCloseScreen.setDisable(false);
	}

	@Override
	void unlockGui() {
		btnCloseScreen.setDisable(false);
	}

	void unlockGui(final String process) {
		unlockGui();
		if (!StringUtils.isEmpty(process)) {
			if (FileLooperHandlerCount.WORKTYPE.compareTo(process) == 0) {
				btnRunWordCounter.setDisable(false);
			} else if (FileLooperHandlerOutline.WORKTYPE.compareTo(process) == 0) {
				btnRunOutliner.setDisable(false);
			} else if (FileLooperHandlerFormatter.WORKTYPE.compareTo(process) == 0) {
				btnRunFormatter.setDisable(false);
			}
		}

	}

	@Override
	public void workFinished(final String msg) {
		this.unlockGui(msg);
	}

	void showMessage(final String msg, final boolean clearPrevious) {
		showMessage(msg, clearPrevious, lastRunText);
	}

	private void fixFocus() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// profileTable.requestFocus();
				if (inputKeyText != null)
					inputKeyText.requestFocus();
			}
		});
	}

	void setProfileChangeMade(boolean b) {
		if (b) {
			profileDataChanged.setText("Unsaved Changes");
		} else {
			profileDataChanged.setText("");
		}
	}

	/*
	 * HANDLE Functions
	 */

	public void handleRunCounter(final ActionEvent event) {
		LOGGER.debug("handleRunCounter: Called");
		lockGui();
		try {
			btnRunWordCounter.setDisable(true);
			final BaseRunner br = new BaseRunner();
			br.handleRunCounter(this, profileManager, this.selectedProfile, lastRunText, myWorkDoneNotify);
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
			br.handleRunOutliner(this, profileManager, this.selectedProfile, lastRunText, myWorkDoneNotify);
		} catch (Exception e) {
			e.printStackTrace();
			btnRunOutliner.setDisable(false);
		}
		LOGGER.debug("handleRunOutliner: Done");
	}

	public void handleRunFormatter(final ActionEvent event) {
		LOGGER.debug("handleRunFormatter: Called");
		// lockGui();
		try {
			btnRunFormatter.setDisable(true);
			final BaseRunner br = new BaseRunner();
			br.handleRunFormatter(this, profileManager, this.selectedProfile, lastRunText, myWorkDoneNotify);
		} catch (Exception e) {
			e.printStackTrace();
			btnRunFormatter.setDisable(false);
		}
		LOGGER.debug("handleRunFormatter: Done");
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// persist();
		doCleanup();
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	public void handleDoClearLogAction(final ActionEvent event) {
		LOGGER.debug("handleDoClearLogAction: Called");
		this.lastRunText.clear();
		this.summaryRunText.clear();
		LOGGER.debug("handleDoClearLogAction: Done");
	}

	public void handleSaveNewProfile(final ActionEvent event) {
		LOGGER.debug("handleSaveNewProfile: Called");

		final Profile profile = new Profile();
		profile.setKey(inputKeyText.getText());
		profileManager.saveProfileData(profile);
		if (profileManager.isWasError()) {
			showMessage("Error=" + profileManager.getError(), false);
			final List<String> list = profileManager.getMessages();
			for (String str : list) {
				if (!StringUtils.isEmpty(str))
					showMessage(str, false);
			}
		} else {
			showMessage("Profile <" + profile.getKey() + "> was saved", false);
			setProfileChangeMade(false);
			// saveProfileBtn.setDisable(true);
			// lastNofificationMsg = "Profile <" + profile.getKey() + "> was
			// saved";
		}
		LOGGER.debug("handleSaveNewProfile: Done");
	}

	public void handleSaveProfile(final ActionEvent event) {
		LOGGER.debug("handleSaveProfile: Called");
		//
		if (selectedProfile == null) {
			LOGGER.debug("loadData: selectedProfile set");
			showMessage("Can't save profile as nothing selected", false);
			return;
		}
		//
		saveProfile();
		//
		LOGGER.debug("handleSaveProfile: Done");
	}

	public void handleDeleteProfile(final ActionEvent event) {
		LOGGER.debug("handleDeleteProfile: Called");

		profileManager.deleteProfile(selectedProfile);
		if (profileManager.isWasError()) {
			showMessage("Error=" + profileManager.getError(), false);
			final List<String> list = profileManager.getMessages();
			for (String str : list) {
				if (!StringUtils.isEmpty(str))
					showMessage(str, false);
			}
		} else {
			showMessage("Profile <" + selectedProfile.getKey() + "> was deleted", false);
			setProfileChangeMade(false);
			deleteProfileBtn.setDisable(true);
			// lastNofificationMsg = "Profile <" + selectedProfile.getKey() + ">
			// was deleted";
		}
		LOGGER.debug("handleDeleteProfile: Done");
	}

	public void handleClearProfile(final ActionEvent event) {
		LOGGER.debug("handleClearProfile: Called");
		// TODO clear?
		// clearSettings();
		showMessage("Cleared Profile Data", true);
		setProfileChangeMade(false);
		// clearSummaryMessage();
		LOGGER.debug("handleClearProfile: Done");
	}

	public void handleImportProfile(final ActionEvent event) {
		LOGGER.debug("handleImportProfile: Called");
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_IMPORT, windowTitle, lastRunText, primaryStage, this, paramsMap);
		LOGGER.debug("handleImportProfile: Done");
	}

	public void handleExportProfile(final ActionEvent event) {
		LOGGER.debug("handleExportProfile: Called");
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_EXPORT, windowTitle, lastRunText, primaryStage, this, paramsMap);
		LOGGER.debug("handleExportProfile: Done");
	}

	public void handleInputFile(final ActionEvent event) {
		LOGGER.debug("handleInputFile: Called");
		locateFile(event, "Open Input File", inputFileText);
		automaticFromInput(false);
		unlockGui();
		LOGGER.debug("handleInputFile: Done");
	}

	public void handleComputeFromFilePrefix(final ActionEvent event) {
		LOGGER.debug("handleComputeFromFilePrefix: Called");
		final String filePrefixText = inputFilePrefixText.getText();
		if (!StringUtils.isBlank(filePrefixText)) {
			automaticFromInput(true);
		} else {
			showMessage("Needs the fileprefix to be filled to work", false);
		}
		LOGGER.debug("handleComputeFromFilePrefix: Done");
	}

	// countOutputBtn
	public void handleBrowseCountFile(final ActionEvent event) {
		LOGGER.debug("handleBrowseCountFile: Called");
		boolean success = false;
		if (outputCountFileText.getText().trim().length() < 1)
			success = locateDir(event, "Open Output Dir ", outputCountFileText, inputFileText);
		else
			success = locateDir(event, "Open Output Dir ", outputCountFileText, outputCountFileText);
		if (success) {
			String outFilename = "\\ChapterCount1.csv";
			final File nFile = new File(outputCountFileText.getText(), outFilename);
			outputCountFileText.setText(nFile.getAbsolutePath());
			outputCountDir = nFile.getParentFile();
		}
		LOGGER.debug("handleBrowseCountFile: Done");
	}

	// formatSIngleFileOutputBtn->outputFormatSingleFileText
	public void handleBrowseFmtSingleFile(final ActionEvent event) {
		LOGGER.debug("handleBrowseFmtSingleFile: Called");
		final String inFileName = getInputFileName("html");
		chooseFile(event, "Single HTML Format File", outputFormatSingleFileText, inFileName, FILTERTYPE.HTML);
		LOGGER.debug("handleBrowseFmtSingleFile: Done");
	}

	// formatHtmlDirOutputBtn->outputFormatChpHtmlDirText
	public void handleBrowseFmtHTMLFile(final ActionEvent event) {
		LOGGER.debug("handleBrowseFmtHTMLFile: Called");
		boolean success = false;
		if (outputFormatChpHtmlDirText.getText().trim().length() < 1)
			success = locateDir(event, "Open Output Dir ", outputFormatChpHtmlDirText, inputFileText);
		else
			success = locateDir(event, "Open Output Dir ", outputFormatChpHtmlDirText, outputFormatChpHtmlDirText);
		if (success) {
		}
		LOGGER.debug("handleBrowseFmtHTMLFile: Done");
	}

	// loadOutputDirBtn1->outputFormatChpTextDirText
	public void handleBrowseFmtTextFile(final ActionEvent event) {
		LOGGER.debug("handleBrowseFmtTextFile: Called");
		boolean success = false;
		if (outputFormatChpTextDirText.getText().trim().length() < 1)
			success = locateDir(event, "Open Output Dir ", outputFormatChpTextDirText, inputFileText);
		else
			success = locateDir(event, "Open Output Dir ", outputFormatChpTextDirText, outputFormatChpTextDirText);
		if (success) {
		}
		LOGGER.debug("handleBrowseFmtTextFile: Done");
	}

	// outlineCSVOutputBtn->outputOutlineCSVFileText
	public void handleOutlineOutlineCSV(final ActionEvent event) {
		LOGGER.debug("handleOutlineOutlineCSV: Called");
		final String inFileName = getInputFileName("Outline", "csv");
		chooseFile(event, "Output All, CSV File", outputOutlineCSVFileText, inFileName, FILTERTYPE.CSV);
		LOGGER.debug("handleOutlineOutlineCSV: Done");
	}

	// outlineAllOutputBtn->outputOutlineAllCSVFileText
	public void handleOutputOutlineCSVAll(final ActionEvent event) {
		LOGGER.debug("handleOutputOutlineCSVAll: Called");
		final String inFileName = getInputFileName("DocTags", "csv");
		chooseFile(event, "Output All, CSV File", outputOutlineAllCSVFileText, inFileName, FILTERTYPE.CSV);
		LOGGER.debug("handleOutputOutlineCSVAll: Done");
	}

	// outlineOutputBtn->outputDocTagsOutlineFileText
	public void handleOutputOutlineFile(final ActionEvent event) {
		LOGGER.debug("handleOutputOutlineFile: Called");
		final String inFileName = getInputFileName("outline", "txt");
		chooseFile(event, "Output All, CSV File", outputDocTagsOutlineFileText, inFileName, FILTERTYPE.TEXT);
		LOGGER.debug("handleOutputOutlineFile: Done");
	}

	// sceneOutputBtn->outputDocTagsSceneFileText
	public void handleOutputSceneFile(final ActionEvent event) {
		LOGGER.debug("handleOutputSceneFile: Called");
		final String inFileName = getInputFileName("scenes", "txt");
		chooseFile(event, "Output All, CSV File", outputDocTagsSceneFileText, inFileName, FILTERTYPE.TEXT);
		LOGGER.debug("handleOutputSceneFile: Done");
	}

	//
	public void handleMoreFiles(final ActionEvent event) {
		LOGGER.debug("handleMoreFiles: Called");
		final Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("appVersion", appVersion);
		paramsMap.put("selectedProfile", selectedProfile);
		paramsMap.put("profileManager", profileManager);
		final String windowTitle = String.format(MainFrame.WINDOW_TITLE_FMT, appProps.getProperty("version"));
		openNewWindow(BaseCtrl.WINDOWKEY_MOREFILES, windowTitle, lastRunText, primaryStage, this, paramsMap);
		LOGGER.debug("handleMoreFiles: Done");
	}

	// buttonMoreOptions->
	public void handleShowScreenOutputConfig(final ActionEvent event) {
		LOGGER.debug("handleShowScreenOutputConfig: Called");
		// TODO
		LOGGER.debug("handleShowScreenOutputConfig: Done");
	}

	/*
	 * DATA Functions
	 */
	private void saveProfile() {
		LOGGER.debug("saveProfile: Called");
		final Profile profile = setProfileFromFields();
		profileManager.saveProfileData(profile);
		if (profileManager.isWasError()) {
			showMessage("Profile '" + profile.getKey() + "' Save, Failed.", false);
			showMessage(profileManager.getError().toString(), false);
		} else {
			// setMetaFromProfile();
			setProfileChangeMade(false);
			showMessage("Profile '" + profile.getKey() + "' Save, Successful.", false);
		}
		unlockGUIforProfile();
		unlockGui();
		LOGGER.debug("saveProfile: Done");
	}

	private void loadData() {
		LOGGER.debug("loadData: Called");
		if (selectedProfile != null)
			LOGGER.debug("loadData: selectedProfile set");
		else if (modeNew) {
			LOGGER.debug("loadData: is mode NEW");
		} else if (modeDelete) {

			LOGGER.debug("loadData: is mode DELETE");
		} else {
			LOGGER.debug("loadData: selectedProfile NOT set");
			LOGGER.debug("loadData: Done");
			return;
		}
		//
		if (!modeNew && !modeDelete) {
			loadDefaults();
			loadChoices();
		}
		loadProfile();
		fixFocus();
		unlockGUIforProfile();
		//
		LOGGER.debug("loadData: Done");
	}

	private void unlockGUIforProfile() {
		unlockAllButtons(outerMostContainer);
	}

	private void automaticFromInput(final boolean keepPreexisting) {
		LOGGER.debug("automaticFromInput: Called w/keepPreexisting=" + keepPreexisting);
		final File inputFile = new File(inputFileText.getText());
		final File inputDir = inputFile.getParentFile();
		final File outputDir = inputDir;
		final String fileName = inputFile.getName();
		final String filenameOnly = TextBiz.getFileNameOnly(fileName);
		final String filePrefixTextOrig = inputFilePrefixText.getText();
		String filePrefixText = filePrefixTextOrig;
		final boolean filePrefixExists = !StringUtils.isBlank(filePrefixText);
		if (filePrefixCheckbox.isSelected())
			filePrefixText = filePrefixText + "_";
		LOGGER.debug("automaticFromInput: filePrefixExists=" + filePrefixExists);
		// final File oldFile = new File(oldText);
		// final File oldDir = oldFile.getParentFile();
		// final File newFile = new File(oldDir, filePrefixText+"");

		// TODO keepPreexisting

		// Count
		outputCountDir = outputDir;
		if (filePrefixExists)
			outputCountFileText.setText(outputDir + "\\" + filePrefixText + "ChapterCount.csv");
		else
			outputCountFileText.setText(outputDir + "\\ChapterCount1.csv");

		// Outline
		if (filePrefixExists) {
			outputOutlineCSVFileText.setText(outputDir + "\\" + filePrefixText + "Outline.csv");
			outputOutlineAllCSVFileText.setText(outputDir + "\\" + filePrefixText + "DocTags.csv");

			outputDocTagsOutlineFileText.setText(outputDir + "\\" + filePrefixText + "outline.txt");
			outputDocTagsSceneFileText.setText(outputDir + "\\" + filePrefixText + "scenes.txt");
			// outputDocTagsOther1FileText.setText(outputDir + "\\" +
			// filePrefixText + "other1.txt");
		} else {
			outputOutlineCSVFileText.setText(outputDir + "\\Outline1.csv");
			outputOutlineAllCSVFileText.setText(outputDir + "\\DocTags.csv");

			outputDocTagsOutlineFileText.setText(outputDir + "\\" + filenameOnly + "_outline.txt");
			outputDocTagsSceneFileText.setText(outputDir + "\\" + filenameOnly + "_scenes.txt");
			// outputDocTagsOther1FileText.setText(outputDir + "\\" +
			// filenameOnly + "_other1.txt");
		}

		// Format
		if (filePrefixExists) {
			outputFormatSingleFileText
					.setText(outputDir + "\\ebook\\sigil\\" + filePrefixTextOrig + "\\" + filenameOnly + ".html");
			outputFormatChpHtmlDirText.setText(outputDir + "\\ebook\\sigil\\" + filePrefixTextOrig + "\\chapters\\");
			outputFormatChpTextDirText
					.setText(outputDir + "\\ebook\\sigil\\" + filePrefixTextOrig + "\\chapterstext\\");
		} else {
			outputFormatSingleFileText.setText(outputDir + "\\ebook\\sigil\\src1\\" + filenameOnly + ".html");
			outputFormatChpHtmlDirText.setText(outputDir + "\\ebook\\sigil\\src1\\chapters\\");
			outputFormatChpTextDirText.setText(outputDir + "\\ebook\\sigil\\src1\\chapterstext\\");
		}
	}

	private void loadProfile() {
		LOGGER.debug("loadProfile: Called");
		// load profile
		setFieldsFromProfile();
		setMetaFromProfile();
		// show messages
		setDetectChanges(outerMostContainer);
		setProfileChangeMade(false);

		unlockGUIforProfile();
		unlockGui();
		LOGGER.debug("loadProfile: Done");
	}

	private void setMetaFromProfile() {
		if (selectedProfile == null) {
			return;
		}
		chosenProfileText.setText(selectedProfile.getKey());
	}

	private void setFieldsFromProfile() {
		LOGGER.debug("setFieldsFromProfile: Called");

		final Profile child = selectedProfile;
		if (child == null) {
			return;
		}
		//
		// final String key = titleOneText.getValue();
		// final Preferences child = getPrefs().node(key);

		// final String seriesTitle = child.getSeriesTitle();
		mainTitleText.setText(child.getMainTitle());
		subTitleText.setText(child.getSubTitle());
		seriesTitleText.setText(child.getSeriesTitle());
		volumeText.setText(child.getVolume());
		inputFileText.setText(child.getInputFile());

		if (modeDelete) {
			inputKeyText.setText(child.getKey());
			return;
		}

		outputFormatSingleFileText.setText(child.getOutputFormatSingleFile());
		outputFormatChpHtmlDirText.setText(child.getOutputFormatChpHtmlDir());
		outputFormatChpTextDirText.setText(child.getOutputFormatChpTextDir());

		inputFilePrefixText.setText(child.getInputFilePrefix());
		filePrefixCheckbox.setSelected(child.isAppendUnderscoreToPrefix());

		// chpDivText.setText(child.get("chpDiv", ""));
		// secDivText.setText(child.get("secDiv", ""));
		regexpChapterText.setText(child.getRegexpChapter());
		regexpSectionText.setText(child.getRegexpSection());

		// Default REGEXP
		if (regexpChapterText.getText().length() < 1)
			regexpChapterText.setText("-=\\s+(?<cname>Chapter)\\s+(?<cnum>\\w+):\\s+(?<ctitle>.*)\\s+=-");
		if (regexpSectionText.getText().length() < 1)
			regexpSectionText.setText("-=\\s+(?<sname>Section):\\s+(?<stitle>\\w+)\\s+=-");

		docTagStartText.setText(child.getDocTagStart());
		docTagEndText.setText(child.getDocTagEnd());

		// TODO formatDao.setChapterHeaderTag(chapterHeaderTag.getText());
		// TODO formatDao.setSectionHeaderTag(sectionHeaderTag.getText());

		fmtModeText.setText(child.getFmtMode());
		outputEncoding.setText(child.getOutputEncoding());

		counterDigitChoice.getSelectionModel().select(child.getCounterDigitChoice());

		// outputCountFileText.setText(child.get("ouputCountFile", ""));

		// if (outputCountFileText.getText().length() < 1) {
		outputCountDir = new File(inputFileText.getText()).getParentFile();

		outputCountFileText.setText(child.getOutputCountFile());

		outputOutlineCSVFileText.setText(child.getOutputCSVOutlineFile());

		outputOutlineAllCSVFileText.setText(child.getOutputCSVAllFile());

		// final String filenameOnly = TextBiz.getFileNameOnly(new
		// File(inputFileText.getText()).getName());

		outputDocTagsOutlineFileText.setText(child.getOutputDocTagsOutlineFile());
		outputDocTagsSceneFileText.setText(child.getOutputDocTagsSceneFile());
		//
		// outputDocTagsOther1FileText.setText(child.getoutputDocTags);
		// ""));
		//
		// final String outC = loadPropFromAppOrDefault("outlineCompress",
		// "subscene, outlinedata, outlinesub, suboutline");
		// final String outE = loadPropFromAppOrDefault("outineExpand",
		// "outline, scene");
		outputDocTagsOutlineCTagsText.setText(child.getDocTagsOutlineCompressTags());
		outputDocTagsOutlineETagsText.setText(child.getDocTagsOutlineTags());

		// final String sceneM = loadPropFromAppOrDefault("sceneMain", "scene,
		// subscene");
		// final String sceneC = loadPropFromAppOrDefault("sceneCoalate",
		// "description, scene");
		// final String sceneO = loadPropFromAppOrDefault("sceneMainO1",
		// "outline, scene, subscene");
		outputDocTagsSceneTagsText.setText(child.getDocTagsSceneTags());
		outputDocTagsSceneCoTags.setText(child.getDocTagsSceneCompressTags());
		// outputDocTagsOther1TagsText.setText(child.get("outputDocTagsOther1Tags");

		cbRemoveDiv.setSelected(child.isCbRemoveDiv());
		cbCenterStars.setSelected(child.isCbCenterStars());
		cbDropCapChapters.setSelected(child.isCbDropCapChapters());
		cbWantTextChptOutput.setSelected(child.isWantTextChptOutput());
		// cbCenterable.setSelected(child.isCbCenterable());

		final Number num = child.getOutputDocTagsMaxLineLength();
		outputDocTagsMaxLineLength.setText(num.toString());
		// final String outputDocTagsMaxLineLengthSel =
		// child.get("outputDocTagsMaxLineLength", "-1");
		// if (StringUtils.isBlank(outputDocTagsMaxLineLengthSel))
		// outputDocTagsMaxLineLength.setText("");
		// else
		// outputDocTagsMaxLineLength.setText(outputDocTagsMaxLineLengthSel);

		outputDocTagsScenePrefix.setText(child.getOutputDocTagsScenePrefix());
		outputDocTagsSubScenePrefix.setText(child.getOutputDocTagsSubScenePrefix());
		sceneCoalateDiv.setText(child.getSceneCoalateDiv());

		//
		// final List<OtherDocTagData> thisProfileOutputList =
		// XferBiz.loadOutputsFromPrefs(titleOneText.getValue(), getPrefs());
		// formatDao.setOutputs(thisProfileOutputList);
		// // loadOutputs();
		//// profileLoadedEvent();
		// // unlockGui();
		LOGGER.debug("setFieldsFromProfile(): Done");
	}

	private Profile setProfileFromFields() {
		LOGGER.debug("setProfileFromFields: Called");

		final Profile child = selectedProfile;
		//
		child.setMainTitle(mainTitleText.getText());
		child.setSubTitle(subTitleText.getText());
		child.setSeriesTitle(seriesTitleText.getText());
		child.setVolume(volumeText.getText());

		child.setInputFile(inputFileText.getText());
		child.setOutputFormatSingleFile(outputFormatSingleFileText.getText());
		child.setOutputFormatChpHtmlDir(outputFormatChpHtmlDirText.getText());
		child.setOutputFormatChpTextDir(outputFormatChpTextDirText.getText());

		child.setInputFilePrefix(inputFilePrefixText.getText());
		child.setAppendUnderscoreToPrefix(filePrefixCheckbox.isSelected());

		// chpDivText.setText(child.get("chpDiv", ""));
		// secDivText.setText(child.get("secDiv", ""));
		child.setRegexpChapter(regexpChapterText.getText());
		child.setRegexpSection(regexpSectionText.getText());

		// Default REGEXP TODO?
		// if (regexpChapterText.getText().length() < 1)
		// regexpChapterText.setText("-=\\s+(?<cname>Chapter)\\s+(?<cnum>\\w+):\\s+(?<ctitle>.*)\\s+=-");
		// if (regexpSectionText.getText().length() < 1)
		// regexpSectionText.setText("-=\\s+(?<sname>Section):\\s+(?<stitle>\\w+)\\s+=-");

		child.setDocTagStart(docTagStartText.getText());
		child.setDocTagEnd(docTagEndText.getText());

		// TODO formatDao.setChapterHeaderTag(chapterHeaderTag.getText());
		// TODO formatDao.setSectionHeaderTag(sectionHeaderTag.getText());

		child.setFmtMode(fmtModeText.getText());
		child.setOutputEncoding(outputEncoding.getText());
		child.setCounterDigitChoice(counterDigitChoice.getSelectionModel().getSelectedIndex());
		// counterDigitChoice.getSelectionModel().select(child.getCounterDigitChoice());
		// outputCountFileText.setText(child.get("ouputCountFile", ""));
		// if (outputCountFileText.getText().length() < 1) {
		// outputCountDir = new File(inputFileText.getText()).getParentFile();

		child.setOutputCountFile(outputCountFileText.getText());
		child.setOutputCSVOutlineFile(outputOutlineCSVFileText.getText());
		child.setOutputCSVAllFile(outputOutlineAllCSVFileText.getText());

		// final String filenameOnly = TextBiz.getFileNameOnly(new
		// File(inputFileText.getText()).getName());

		child.setOutputDocTagsOutlineFile(outputDocTagsOutlineFileText.getText());
		child.setOutputDocTagsSceneFile(outputDocTagsSceneFileText.getText());

		child.setDocTagsOutlineCompressTags(outputDocTagsOutlineCTagsText.getText());
		child.setDocTagsOutlineTags(outputDocTagsOutlineETagsText.getText());

		child.setDocTagsSceneTags(outputDocTagsSceneTagsText.getText());
		child.setDocTagsSceneCompressTags(outputDocTagsSceneCoTags.getText());

		child.setCbRemoveDiv(cbRemoveDiv.isSelected());
		child.setCbCenterStars(cbCenterStars.isSelected());
		child.setCbDropCapChapters(cbDropCapChapters.isSelected());
		child.setWantTextChptOutput(cbWantTextChptOutput.isSelected());
		// cbCenterable.setSelected(child.isCbCenterable());

		final String str = outputDocTagsMaxLineLength.getText();
		final Integer num = Integer.valueOf(str);
		child.setOutputDocTagsMaxLineLength(num);

		child.setOutputDocTagsScenePrefix(outputDocTagsScenePrefix.getText());
		child.setOutputDocTagsSubScenePrefix(outputDocTagsSubScenePrefix.getText());
		child.setSceneCoalateDiv(sceneCoalateDiv.getText());

		LOGGER.debug("setProfileFromFields: Done");
		return child;
	}

	// private void profileLoadedEvent() {
	// // TODO Auto-generated method stub
	//
	// }

	private void loadChoices() {
		counterDigitChoice.setItems(FXCollections.observableArrayList("0", "1", "2", "3", "4"));
		// counterDigitChoice.getValue()
		counterDigitChoice.getSelectionModel().select(3);
	}

	private void loadDefaults() {
		LOGGER.debug("loadDefaults: Called ");
		final String outC = loadPropFromAppOrDefault("outlineCompress",
				"subscene, outlinedata, outlinesub, suboutline");
		final String outE = loadPropFromAppOrDefault("outineExpand", "outline, scene");
		outputDocTagsOutlineCTagsText.setText(outC);
		outputDocTagsOutlineETagsText.setText(outE);

		final String sceneM = loadPropFromAppOrDefault("sceneMain", "scene, subscene");
		final String sceneC = loadPropFromAppOrDefault("sceneCoalate", "description, scene");
		outputDocTagsSceneTagsText.setText(sceneM);
		outputDocTagsSceneCoTags.setText(sceneC);
		// outputDocTagsOther1TagsText.setText(sceneM);

		final String regExpChp = loadPropFromAppOrDefault("regexpChapterText", "");
		final String regExpSec = loadPropFromAppOrDefault("regexpSectionText", "");
		regexpChapterText.setText(regExpChp);
		regexpSectionText.setText(regExpSec);

		final String defaultEncoding = loadPropFromAppOrDefault("defaultEncoding", "");
		outputEncoding.setText(defaultEncoding);

		final String lastSelectedDirectoryStr = appPreferences.get("lastSelectedDirectory", null);
		if (!StringUtils.isEmpty(lastSelectedDirectoryStr)) {
			setLastSelectedDirectory(lastSelectedDirectoryStr);
		}
	}

	private File getInputAsFile() {
		final String inString = inputFileText.getText().trim();
		if (inString.length() > 0) {
			final File inFile = new File(inString);
			if (inFile != null) {
				// inFile.getName();
				// inFile.getParentFile();
				return inFile;
			}
		}
		final File lastDir = getLastSelectedDirectory();
		if (lastDir != null) {
			return lastDir;
		}
		return new File("");
	}

	private String getInputFileName(final String filePostfix, final String newExtension) {
		String inFileName = "";
		final File inFile = getInputAsFile();
		final boolean addUnder = selectedProfile.isAppendUnderscoreToPrefix();
		if (inFile != null && inFile.getName().length() > 0) {
			inFileName = inFile.getName();// can contain ext
			if (!StringUtils.isBlank(newExtension)) {
				int extIdx = inFileName.lastIndexOf(".");
				// String ext = "";
				String pre = "";
				if (extIdx >= 1) {
					// ext = inFileName.substring(extIdx + 1);
					pre = inFileName.substring(0, extIdx);
					// inFileName = inFileName.replaceAll(ext, newExtension);
				} else {
					// ext = "";
					pre = inFileName;
					// inFileName = inFileName + "." + newExtension;
				}
				inFileName = pre + (addUnder ? "_" : "") + filePostfix + "." + newExtension;
			}
		}
		// final File nFile = new File(xx, outFilename);
		return inFileName;
	}

	private String getInputFileName(final String newExtension) {
		String inFileName = "";
		final File inFile = getInputAsFile();
		if (inFile != null && inFile.getName().length() > 0) {
			inFileName = inFile.getName();
			if (!StringUtils.isBlank(newExtension)) {
				final int extIdx = inFileName.lastIndexOf(".");
				String ext = "";
				if (extIdx >= 1) {
					ext = inFileName.substring(extIdx + 1);
					inFileName = inFileName.replaceAll(ext, newExtension);
				} else {
					inFileName = inFileName + "." + newExtension;
				}
			}
		}
		// final File nFile = new File(xx, outFilename);
		return inFileName;
	}

}
