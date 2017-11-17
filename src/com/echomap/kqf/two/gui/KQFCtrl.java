package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.looper.TextBiz;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class KQFCtrl implements Initializable, WorkDoneNotify {
	private final static Logger LOGGER = LogManager.getLogger(KQFCtrl.class);

	private File lastSelectedDirectory = null;
	private static final DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Properties appProps = new Properties();
	final Preferences userPrefs;
	// final Preferences userPrefs =
	// Preferences.userNodeForPackage(KQFCtrl.class);

	@FXML
	private TextArea lastRunText;
	@FXML
	private TextField fmtModeText;
	// @FXML
	// private TextField chpDivText;
	// @FXML
	// private TextField secDivText;
	@FXML
	private TextField regexpChapterText;
	@FXML
	private TextField regexpSectionText;
	@FXML
	private TextField docTagStartText;
	@FXML
	private TextField docTagEndText;
	@FXML
	private TextField chapterHeaderTag;
	@FXML
	private TextField sectionHeaderTag;
	@FXML
	private TextField titleTwoText;
	@FXML
	private TextField titleThreeText;
	@FXML
	private TextField inputFileText;
	@FXML
	private TextField outputFormatSingleFileText;
	@FXML
	private TextField outputFormatChpHtmlDirText;
	@FXML
	private TextField outputFormatChpTextDirText;
	@FXML
	private TextField outputEncoding;
	@FXML
	private TextField outputCountFileText;
	@FXML
	private TextField outlineFileText;
	@FXML
	private TextField outputOutlineFileText;
	@FXML
	private TextField outputOutlineFileText1;
	@FXML
	private TextField outputDocTagsMaxLineLength; // length for doctag output
	@FXML
	private TextField inputFilePrefixText;

	@FXML
	private TextField outputDocTagsOutlineFileText;
	@FXML
	private TextField outputDocTagsSceneFileText;
	@FXML
	private TextField outputDocTagsOutlineCTagsText;
	@FXML
	private TextArea outputDocTagsOutlineETagsText;
	@FXML
	private TextField outputDocTagsSceneTagsText;
	@FXML
	private TextField outputDocTagsSceneCoTags;

	@FXML
	private ComboBox<String> titleOneText;

	@FXML
	private HBox profileButtonBox;
	@FXML
	private GridPane sectionDocTag;
	@FXML
	private GridPane sectionFormatting;
	@FXML
	private GridPane sectionWordCount;
	@FXML
	private GridPane sectionMain;

	@FXML
	private Button inputFileBtn;
	@FXML
	private Button loadOutputDirBtn;
	@FXML
	private Button loadOutputDirBtn1;
	@FXML
	private Button loadOutputFileBtn;
	@FXML
	private Button countOutputBtn;
	@FXML
	private Button outlineOutputBtn;
	@FXML
	private Button outlineCSVOutputBtn;
	@FXML
	private Button outlineAllOutputBtn;
	@FXML
	private Button sceneOutputBtn;
	@FXML
	private Button clearProfileBtn;
	@FXML
	private Button deleteProfileBtn;
	@FXML
	private Button saveProfileBtn;
	@FXML
	private Button doFormatBtn;
	@FXML
	private Button doWordCountBtn;
	@FXML
	private Button doOutlineBtn;
	@FXML
	private Button doClearLogBtn;
	@FXML
	private Button computeFromFilePrefixBtn;

	@FXML
	private CheckBox cbRemoveDiv;
	@FXML
	private CheckBox cbCenterStars;
	@FXML
	private CheckBox cbDropCapChapters;
	@FXML
	private CheckBox cbCenterable;
	@FXML
	private CheckBox cbWantTextChptOutput;
	@FXML
	private CheckBox filePrefixCheckbox;

	@FXML
	private Label chosenProfileText;

	@FXML
	private ChoiceBox<String> counterDigitChoice;

	private File outputCountDir;
	// private File outputOutlineDir;
	// private File outputOutlineDir1;

	Timer timer = new Timer();
	private MyTimerTask myTimerTask;

	private boolean runningMutex = false;

	public KQFCtrl() {
		userPrefs = Preferences.userNodeForPackage(KQFCtrl.class);
	}

	@Override
	public void initialize(java.net.URL arg0, ResourceBundle arg1) {

		assert doWordCountBtn != null : "fx:id=\"wordCountBtn1\" was not injected: check your FXML file 'simple.fxml'.";
		assert lastRunText != null : "fx:id=\"lastRunText\" was not injected: check your FXML file 'simple.fxml'.";
		assert fmtModeText != null : "fx:id=\"fmtModeText\" was not injected: check your FXML file 'simple.fxml'.";
		// assert chpDivText != null : "fx:id=\"chpDivText\" was not injected:
		// check your FXML file 'simple.fxml'.";
		// assert secDivText != null : "fx:id=\"secDivText\" was not injected:
		// check your FXML file 'simple.fxml'.";

		assert docTagStartText != null : "fx:id=\"docTagStartText\" was not injected: check your FXML file 'simple.fxml'.";
		assert docTagEndText != null : "fx:id=\"docTagEndText\" was not injected: check your FXML file 'simple.fxml'.";

		assert titleOneText != null : "fx:id=\"titleOneText\" was not injected: check your FXML file 'simple.fxml'.";
		assert inputFileBtn != null : "fx:id=\"inputFileBtn\" was not injected: check your FXML file 'simple.fxml'.";
		assert loadOutputDirBtn != null : "fx:id=\"loadOutputDirBtn\" was not injected: check your FXML file 'simple.fxml'.";
		assert loadOutputFileBtn != null : "fx:id=\"loadOutputFileBtn\" was not injected: check your FXML file 'simple.fxml'.";

		assert inputFileText != null : "fx:id=\"inputFileText\" was not injected: check your FXML file 'simple.fxml'.";
		assert outputFormatSingleFileText != null : "fx:id=\"outputFileText\" was not injected: check your FXML file 'simple.fxml'.";
		assert outputFormatChpHtmlDirText != null : "fx:id=\"outputDirText\" was not injected: check your FXML file 'simple.fxml'.";
		assert outputEncoding != null : "fx:id=\"outputEncoding\" was not injected: check your FXML file 'simple.fxml'.";

		if (inputFileText.getText() != null && inputFileText.getText().length() > 0)
			lastSelectedDirectory = new File(inputFileText.getText()).getParentFile();
		else if (outputFormatSingleFileText.getText() != null && outputFormatSingleFileText.getText().length() > 0)
			lastSelectedDirectory = new File(outputFormatSingleFileText.getText());
		if (lastSelectedDirectory == null || !lastSelectedDirectory.isDirectory())
			lastSelectedDirectory = null;

		myTimerTask = new MyTimerTask(this.lastRunText);

		// tooltips
		loadChoices();
		loadProfiles();
		lockGui();
		fixFocus();

	}

	@Override
	public void finishedWithWork(final String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("Done running " + msg + " Process (" + getCurrentDateFmt() + ")", false);
				unlockGui();
				runningMutex = false;
			}
		});
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
	public void statusUpdateForWork(final String header, final String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("---" + header + " Process, " + msg, false);
			}
		});
	}

	private void loadChoices() {
		counterDigitChoice.setItems(FXCollections.observableArrayList("0", "1", "2", "3", "4"));
		// counterDigitChoice.getValue()
		counterDigitChoice.getSelectionModel().select(3);
	}

	protected void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");
		myTimerTask.cancel();
		timer.cancel();
		myTimerTask = null;
		timer = null;
	}

	// private void setLastRunText(String fmtt) {
	// lastRunText.setText(fmtt + "\r\n" + lastRunText.getText());
	// }

	public void handleProfileSelectAction(final ActionEvent event) {
		if (titleOneText.getSelectionModel().getSelectedIndex() > -1) {
			// showMessage("profile action idx: " +
			// titleOneText.getSelectionModel().getSelectedIndex(), false);
			// showMessage("profile action item: " +
			// titleOneText.getSelectionModel().getSelectedItem(), false);
			deleteProfileBtn.setDisable(false);
		}
	}

	/**
	 * Profile/Prefs
	 */
	public void handleDeletePrefs(final ActionEvent event) {
		boolean childDeleted = false;
		try {
			final Preferences userPrefs = getPrefs();
			if (titleOneText.getSelectionModel().getSelectedIndex() > -1) {
				final String key = titleOneText.getSelectionModel().getSelectedItem();
				final Preferences child = userPrefs.node(key);
				if (child != null) {// && child.keys().length > 0
					showMessage("Child '" + child + "'", false);
					child.removeNode();
					showMessage("Deleted profile '" + key + "'", false);
					childDeleted = true;
				}
			} else {
				showMessage("Select a profile name to DELETE.", false);
			}
			userPrefs.flush();
		} catch (BackingStoreException e) {
			showMessage("Error Deleting profile: " + e, false);
		}
		if (!childDeleted)
			showMessage("That profile doesn't exist.", false);

		clearSettings();
		loadProfiles();
	}

	/**
	 * Profile/Prefs
	 */
	public void handleProfileBeingEntered(final ActionEvent event) {
		saveProfileBtn.setDisable(false);
	}

	/**
	 * Profile/Prefs
	 */
	public void handleLoadPrefs(final ActionEvent event) {
		try {
			if (titleOneText.getSelectionModel().getSelectedIndex() > -1) {
				if (!getPrefs().nodeExists(titleOneText.getValue())) {
					showMessage("That profile doesn't exist.", false);
					return;
				}
				loadProps();
				showMessage("Loaded profile '" + titleOneText.getValue() + "'", true);
				chosenProfileText.setText(titleOneText.getSelectionModel().getSelectedItem());
			} else {
				showMessage("Enter or select a profile name to LOAD.", true);
			}
		} catch (BackingStoreException e) {
			showMessage("Error Deleting profile: " + e, false);
		}
	}

	/**
	 * Profile/Prefs
	 */
	public void handleSavePrefs(final ActionEvent event) {
		final String nowTextOne = titleOneText.getValue();
		if (nowTextOne != null && nowTextOne.length() > 0) {
			saveProps();
			loadProfiles();
			titleOneText.getSelectionModel().select(nowTextOne);
			showMessage("Saved Profile '" + nowTextOne + "'", false);
		}
	}

	private void clearSettings() {
		titleOneText.getSelectionModel().clearSelection();

		final List<TextField> foundComponents1 = getNodesOfType(sectionDocTag, TextField.class);
		if (foundComponents1 != null)
			for (TextField textField : foundComponents1) {
				textField.setText("");
			}
		outputDocTagsOutlineETagsText.setText("outline, scene, subscene, info");
		outputDocTagsOutlineCTagsText.setText("outline, scene, subscene, info");
		outputDocTagsSceneTagsText.setText("scene, subscene");
		outputDocTagsSceneCoTags.setText("description, changes, starttime");

		final List<TextField> foundComponents2 = getNodesOfType(sectionFormatting, TextField.class);
		if (foundComponents2 != null)
			for (TextField textField : foundComponents2) {
				textField.setText("");
			}

		final List<TextField> foundComponents3 = getNodesOfType(sectionWordCount, TextField.class);
		if (foundComponents3 != null)
			for (TextField textField : foundComponents3) {
				textField.setText("");
			}

		final List<TextField> foundComponents4 = getNodesOfType(sectionMain, TextField.class);
		if (foundComponents4 != null)
			for (TextField textField : foundComponents4) {
				textField.setText("");
			}

		chosenProfileText.setText("NO PROFILE");

		outputDocTagsMaxLineLength.setText("70");

		// -=\s+(?<cname>Chapter)\s+(?<cnum>\d+):\s+(?<ctitle>\w+)\s+=-
		regexpChapterText.setText("-=\\s+(?<cname>Chapter)\\s+(?<cnum>\\w+):\\s+(?<stitle>\\w+)\\s+=-");
		regexpSectionText.setText("-=\\s+(?<sname>Section):\\s+(?<stitle>\\w+)\\s+=-");

		lockGui();
		fixFocus();

		// showMessage("Cleared Profile Data", true);
	}

	/**
	 * Profile/Prefs
	 */
	public void handleClearSettings(final ActionEvent event) {
		clearSettings();
		showMessage("Cleared Profile Data", true);
	}

	public void handleInputFile(final ActionEvent event) {
		locateFile(event, "Open Input File", inputFileText);
		automaticFromInput(false);
	}

	public void handleLoadFormatChptHtmlOutputDir(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputFormatChpHtmlDirText);
		if (!outputFormatChpHtmlDirText.getText().endsWith("chapters")) {
			final File file = new File(outputFormatChpHtmlDirText.getText(), "chapters");
			outputFormatChpHtmlDirText.setText(file.getAbsolutePath());
		}
	}

	public void handleLoadFormatChptTextOutputDir(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputFormatChpTextDirText);
		if (!outputFormatChpTextDirText.getText().endsWith("chapters")) {
			final File file = new File(outputFormatChpTextDirText.getText(), "chapterstext");
			outputFormatChpTextDirText.setText(file.getAbsolutePath());
		}
	}

	public void handleLoadOutputFile(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputFormatSingleFileText);
		final String inFilename = inputFileText.getText();
		String outFilename = "";
		if (inFilename != null && inFilename.length() > 0) {
			final File inFile = new File(inFilename);
			final String filenameOnly = inFile.getName();
			final int extIdx = filenameOnly.lastIndexOf(".");
			String ext = "";
			if (extIdx >= 1) {
				ext = filenameOnly.substring(extIdx + 1);
				outFilename = filenameOnly.replaceAll(ext, "html");
			} else {
				outFilename = filenameOnly + ".html";
			}
		}
		final File nFile = new File(outputFormatSingleFileText.getText(), outFilename);
		outputFormatSingleFileText.setText(nFile.getAbsolutePath());
	}

	public void handleOutputCountFile(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputCountFileText);
		String outFilename = "\\ChapterCount1.csv";
		final File nFile = new File(outputCountFileText.getText(), outFilename);
		outputCountFileText.setText(nFile.getAbsolutePath());
		outputCountDir = nFile.getParentFile();
	}

	public void handleOutputOutlineFile(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputOutlineFileText);
		String outFilename = "/Outline1.csv";
		final File nFile = new File(outputOutlineFileText.getText(), outFilename);
		outputOutlineFileText.setText(nFile.getAbsolutePath());
	}

	public void handleOutputOutlineFile1(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputOutlineFileText1);
		String outFilename = "/Outline1.csv";
		final File nFile = new File(outputOutlineFileText1.getText(), outFilename);
		outputOutlineFileText1.setText(nFile.getAbsolutePath());
	}

	public void handleComputeFromFilePrefix(final ActionEvent event) {
		final String filePrefixText = inputFilePrefixText.getText();
		if (!StringUtils.isBlank(filePrefixText)) {
			automaticFromInput(true);
		}
	}

	public void handleClose(final ActionEvent event) {
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		doCleanup();
		stage.close();
	}

	public void handleDoCountAction(final ActionEvent event) {
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}
		lockGui();
		this.runningMutex = true;

		LOGGER.info("Running count action");
		showMessage("Running Count Process (" + getCurrentDateFmt() + ")", false);
		// timer.cancel();
		// timer = new Timer();
		try {
			final FormatDao formatDao = new FormatDao();
			setupDao(formatDao);

			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.count(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// unlockGui();
			// String fmtt = "Done running COUNT ( " + getCurrentDateFmt() +
			// ")";
			// LOGGER.debug("fmtt: " + fmtt);
			// setLastRunText(fmtt);
			// startTimerTask();
		}
	}

	public void handleDoOutlineAction(final ActionEvent event) {
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}
		lockGui();
		this.runningMutex = true;

		LOGGER.info("Running outline action");
		showMessage("Running Outline Process (" + getCurrentDateFmt() + ")", false);
		try {
			final FormatDao formatDao = new FormatDao();
			setupDao(formatDao);

			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.outline(formatDao);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// unlockGui();
			// setLastRunText("Done running OUTLINE (" + getCurrentDateFmt() +
			// ")");
			// startTimerTask();
		}
	}

	public void handleDoFormatAction(final ActionEvent event) {
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}
		lockGui();
		this.runningMutex = true;

		LOGGER.info("Running format action");
		showMessage("Running Format Process (" + getCurrentDateFmt() + ")", false);
		try {
			final FormatDao formatDao = new FormatDao();
			setupDao(formatDao);

			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.format(formatDao);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// unlockGui();
			// setLastRunText("Done running FORMAT (" + getCurrentDateFmt() +
			// ")");
			// startTimerTask();
		}
	}

	public void handleDoClearLogAction(final ActionEvent event) {
		this.lastRunText.clear();
		this.unlockGui();
	}

	private void setupDao(final FormatDao formatDao) {
		// Setup the argument passed
		formatDao.setInputFilename(inputFileText.getText());
		formatDao.setOutputFilename(outputFormatSingleFileText.getText());

		String filePrefixText = inputFilePrefixText.getText();

		if (filePrefixCheckbox.isSelected())
			filePrefixText = filePrefixText + "_";
		formatDao.setFilePrefix(filePrefixText);

		formatDao.setOutputCountFile(outputCountFileText.getText());
		formatDao.setOutputOutlineFile(outputOutlineFileText.getText());
		formatDao.setOutputOutlineFile1(outputOutlineFileText1.getText());

		formatDao.setWriteChapters(outputFormatChpHtmlDirText.getText());
		formatDao.setWriteChaptersText(outputFormatChpTextDirText.getText());

		formatDao.setStoryTitle1(titleTwoText.getText());
		formatDao.setStoryTitle2(titleThreeText.getText());
		formatDao.setFormatMode(fmtModeText.getText());// "Sigil"

		// formatDao.setChapterDivider(regexpChapterText.getText());
		// formatDao.setSectionDivider(regexpSectionText.getText());
		formatDao.setRegexpChapter(regexpChapterText.getText());
		formatDao.setRegexpSection(regexpSectionText.getText());

		formatDao.setDocTagStart(docTagStartText.getText());
		formatDao.setDocTagEnd(docTagEndText.getText());

		formatDao.setChapterHeaderTag(chapterHeaderTag.getText());
		formatDao.setSectionHeaderTag(sectionHeaderTag.getText());

		if (cbCenterStars.isSelected())
			formatDao.setCenterStars(true);
		if (cbDropCapChapters.isSelected())
			formatDao.setDropCapChapter(true);
		if (cbRemoveDiv.isSelected()) {
			formatDao.setRemoveChptDiv(true);
			formatDao.setRemoveSectDiv(true);
		}
		if (cbWantTextChptOutput.isSelected())
			formatDao.setWantTextChptOutput(true);

		if (outputEncoding.getText() != null && outputEncoding.getText().length() > 0)
			formatDao.setOutputEncoding(outputEncoding.getText());

		final Integer itm = new Integer((String) counterDigitChoice.getSelectionModel().getSelectedItem());
		formatDao.setOutputFormatDigits(itm);

		//
		formatDao.setOutputDocTagsOutlineFile(outputDocTagsOutlineFileText.getText());
		formatDao.setOutputDocTagsSceneFile(outputDocTagsSceneFileText.getText());
		formatDao.setDocTagsOutlineCompressTags(outputDocTagsOutlineCTagsText.getText());
		formatDao.setDocTagsOutlineExpandTags(outputDocTagsOutlineETagsText.getText());
		formatDao.setDocTagsSceneTags(outputDocTagsSceneTagsText.getText());
		formatDao.setDocTagsSceneCoTags(outputDocTagsSceneCoTags.getText());

		final String dtmllS = outputDocTagsMaxLineLength.getText();
		Integer dtmllI = 70;
		if (StringUtils.isBlank(dtmllS))
			dtmllI = -1;
		else
			dtmllI = Integer.parseInt(dtmllS);
		formatDao.setDocTagsMaxLineLength(dtmllI);

		formatDao.setVersion(appProps.getProperty("version"));
	}

	private void fixFocus() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				titleOneText.requestFocus();
			}
		});
	}

	private void lockGui() {
		if (runningMutex) {
			return;
		}
		deleteProfileBtn.setDisable(true);
		saveProfileBtn.setDisable(true);
		computeFromFilePrefixBtn.setDisable(true);
		clearProfileBtn.setDisable(true);

		doFormatBtn.setDisable(true);
		doWordCountBtn.setDisable(true);
		doOutlineBtn.setDisable(true);

		countOutputBtn.setDisable(true);

		outlineCSVOutputBtn.setDisable(true);
		outlineAllOutputBtn.setDisable(true);
		outlineOutputBtn.setDisable(true);
		sceneOutputBtn.setDisable(true);
		loadOutputFileBtn.setDisable(true);
		loadOutputDirBtn.setDisable(true);
		loadOutputDirBtn1.setDisable(true);

		filePrefixCheckbox.setDisable(true);
	}

	private void unlockGui() {
		deleteProfileBtn.setDisable(false);
		saveProfileBtn.setDisable(false);
		computeFromFilePrefixBtn.setDisable(false);
		clearProfileBtn.setDisable(false);

		doFormatBtn.setDisable(false);
		doWordCountBtn.setDisable(false);
		doOutlineBtn.setDisable(false);

		countOutputBtn.setDisable(false);

		outlineCSVOutputBtn.setDisable(false);
		outlineAllOutputBtn.setDisable(false);
		outlineOutputBtn.setDisable(false);
		sceneOutputBtn.setDisable(false);
		loadOutputFileBtn.setDisable(false);
		loadOutputDirBtn.setDisable(false);
		loadOutputDirBtn1.setDisable(false);

		filePrefixCheckbox.setDisable(false);
	}

	// private void startTimerTask() {
	// this.myTimerTask = new MyTimerTask(this.lastRunText);
	// timer.cancel();
	// timer = new Timer();
	// timer.schedule(myTimerTask, 5000);
	// }

	class MyTimerTask extends TimerTask {
		TextInputControl textF = null;

		@Override
		public void run() {
			// textF.setText("");
		}

		public MyTimerTask(final TextInputControl textf) {
			this.textF = textf;
		}
	}

	private Preferences getPrefs() {
		return userPrefs;
		// final Preferences userPrefs =
		// Preferences.userNodeForPackage(KQFCtrl.class);
		// return userPrefs;
	}

	private void loadProfiles() {
		titleOneText.getItems().clear();
		try {
			final String[] prefkeys = getPrefs().childrenNames();
			if (prefkeys != null && prefkeys.length > 0) {
				for (final String str1 : prefkeys) {
					if (!StringUtils.isBlank(str1))
						titleOneText.getItems().add(str1);
				}
			}
		} catch (BackingStoreException e) {
			showMessage("Error Deleting profile: " + e, false);
			e.printStackTrace();
		}
	}

	private void showMessage(final String msg, final boolean clearPrevious) {
		final Animation animation = new Transition() {
			{
				setCycleDuration(Duration.millis(1000));
				setInterpolator(Interpolator.EASE_OUT);
			}

			@Override
			protected void interpolate(double frac) {
				Color vColor = new Color(1, 0, 0, 1 - frac);
				lastRunText.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
			}
		};
		animation.play();

		if (clearPrevious) {
			lastRunText.setText(msg);
		} else {
			lastRunText.setText(msg + "\r\n" + lastRunText.getText());
		}
	}

	private String getCurrentDateFmt() {
		final Calendar cal = Calendar.getInstance();
		myDateFormat.setTimeZone(cal.getTimeZone());
		String txt = myDateFormat.format(cal.getTime());
		LOGGER.debug("date = " + txt);
		return txt;
	}

	private void automaticFromInput(final boolean keepPreexisting) {
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
			outputOutlineFileText.setText(outputDir + "\\" + filePrefixText + "Outline.csv");
			outputOutlineFileText1.setText(outputDir + "\\" + filePrefixText + "DocTags.csv");

			outputDocTagsOutlineFileText.setText(outputDir + "\\" + filePrefixText + "_outline.txt");
			outputDocTagsSceneFileText.setText(outputDir + "\\" + filePrefixText + "_scenes.txt");
		} else {
			outputOutlineFileText.setText(outputDir + "\\Outline1.csv");
			outputOutlineFileText1.setText(outputDir + "\\DocTags.csv");

			outputDocTagsOutlineFileText.setText(outputDir + "\\" + filenameOnly + "_outline.txt");
			outputDocTagsSceneFileText.setText(outputDir + "\\" + filenameOnly + "_scenes.txt");
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

	protected void loadProps() {
		final String key = titleOneText.getValue();
		final Preferences child = getPrefs().node(key);

		titleOneText.setValue(child.get("titleOne", key));
		titleTwoText.setText(child.get("titleTwo", ""));
		titleThreeText.setText(child.get("titleThree", ""));
		inputFileText.setText(child.get("inputFile", ""));
		outputFormatSingleFileText.setText(child.get("ouputFile", ""));
		outputFormatChpHtmlDirText.setText(child.get("outputDir", ""));
		outputFormatChpTextDirText.setText(child.get("outputFormatChpTextDirText", ""));

		inputFilePrefixText.setText(child.get("inputFilePrefix", ""));
		filePrefixCheckbox.setSelected(child.getBoolean("appendUnderscoreToPrefix", false));

		// chpDivText.setText(child.get("chpDiv", ""));
		// secDivText.setText(child.get("secDiv", ""));
		regexpChapterText.setText(child.get("regexpChapter", ""));
		regexpSectionText.setText(child.get("regexpSection", ""));

		if (regexpChapterText.getText().length() < 1)
			regexpChapterText.setText("-=\\s+(?<cname>Chapter)\\s+(?<cnum>\\w+):\\s+(?<ctitle>\\w+)\\s+=-");
		if (regexpSectionText.getText().length() < 1)
			regexpSectionText.setText("-=\\s+(?<sname>Section):\\s+(?<stitle>\\w+)\\s+=-");

		docTagStartText.setText(child.get("docTagStart", "[[*"));
		docTagEndText.setText(child.get("docTagEnd", "*]]"));

		fmtModeText.setText(child.get("fmtMode", ""));
		outputEncoding.setText(child.get("outputEncoding", ""));

		// outputCountFileText.setText(child.get("ouputCountFile", ""));

		// if (outputCountFileText.getText().length() < 1) {
		outputCountDir = new File(inputFileText.getText()).getParentFile();

		outputCountFileText.setText(child.get("ouputCountFile", outputCountDir + "\\ChapterCount1.csv"));

		outputOutlineFileText.setText(child.get("ouputOutlineFile", outputCountDir + "\\Outline1.csv"));

		outputOutlineFileText1.setText(child.get("ouputOutlineFile1", outputCountDir + "\\DocTags.csv"));

		final String filenameOnly = TextBiz.getFileNameOnly(new File(inputFileText.getText()).getName());

		outputDocTagsOutlineFileText
				.setText(child.get("outputDocTagsOutlineFile", outputCountDir + "\\" + filenameOnly + "_outline.txt"));
		outputDocTagsSceneFileText
				.setText(child.get("outputDocTagsSceneFile", outputCountDir + "\\" + filenameOnly + "_scenes.txt"));

		final String outC = "info, subscene, outlinedata, outlinesub, suboutline";
		final String outE = "outline, scene, pattern, date, time, loc";
		outputDocTagsOutlineCTagsText.setText(child.get("outputDocTagsOutlineCTags", outC));
		outputDocTagsOutlineETagsText.setText(child.get("outputDocTagsOutlineTags", outE));

		final String sceneM = "scene, subscene";
		final String sceneC = "description, changes, starttime, scene, pattern";
		outputDocTagsSceneTagsText.setText(child.get("outputDocTagsSceneTags", sceneM));
		outputDocTagsSceneCoTags.setText(child.get("outputDocTagsSceneCoTags", sceneC));

		final String cbDropCapChaptersSel = child.get("cbDropCapChapters", "1");
		if (!StringUtils.isBlank(cbDropCapChaptersSel) || cbDropCapChaptersSel.compareTo("selected") == 0)
			cbDropCapChapters.setSelected(true);
		else
			cbDropCapChapters.setSelected(false);

		final String cbWantTextChptOutputSel = child.get("cbWantTextChptOutput", "1");
		if (StringUtils.isBlank(cbWantTextChptOutputSel) || cbWantTextChptOutputSel.compareTo("selected") == 0)
			cbWantTextChptOutput.setSelected(true);
		else
			cbWantTextChptOutput.setSelected(false);

		counterDigitChoice.getSelectionModel().select(child.get("counterDigitChoice", "1"));

		final String outputDocTagsMaxLineLengthSel = child.get("outputDocTagsMaxLineLength", "-1");
		if (StringUtils.isBlank(outputDocTagsMaxLineLengthSel))
			outputDocTagsMaxLineLength.setText("");
		else
			outputDocTagsMaxLineLength.setText(outputDocTagsMaxLineLengthSel);

		unlockGui();
	}

	protected void saveProps() {
		final String key = titleOneText.getValue();
		if (!StringUtils.isBlank(key)) {
			final Preferences child = getPrefs().node(key);

			child.put("titleOne", key);
			child.put("titleTwo", titleTwoText.getText());
			child.put("titleThree", titleThreeText.getText());

			child.put("inputFile", inputFileText.getText());
			child.put("ouputFile", outputFormatSingleFileText.getText());
			child.put("outputDir", outputFormatChpHtmlDirText.getText());
			child.put("outputFormatChpHtmlDirText", outputFormatChpHtmlDirText.getText());
			child.put("outputFormatChpTextDirText", outputFormatChpTextDirText.getText());

			child.put("inputFilePrefix", inputFilePrefixText.getText());
			child.putBoolean("appendUnderscoreToPrefix", filePrefixCheckbox.isSelected());

			child.put("ouputCountFile", outputCountFileText.getText());
			child.put("ouputOutlineFile", outputOutlineFileText.getText());
			child.put("ouputOutlineFile1", outputOutlineFileText1.getText());

			// child.put("chpDiv", chpDivText.getText());
			// child.put("secDiv", secDivText.getText());
			child.put("regexpChapter", regexpChapterText.getText());
			child.put("regexpSection", regexpSectionText.getText());

			child.put("docTagStart", docTagStartText.getText());
			child.put("docTagEnd", docTagEndText.getText());

			child.put("fmtMode", fmtModeText.getText());
			child.put("outputEncoding", outputEncoding.getText());

			// counterDigitChoice.getSelectionModel().select(3);

			child.put("outputDocTagsOutlineFile", outputDocTagsOutlineFileText.getText());
			child.put("outputDocTagsSceneFile", outputDocTagsSceneFileText.getText());
			child.put("outputDocTagsOutlineCTags", outputDocTagsOutlineCTagsText.getText());
			child.put("outputDocTagsOutlineTags", outputDocTagsOutlineETagsText.getText());
			child.put("outputDocTagsSceneTags", outputDocTagsSceneTagsText.getText());
			child.put("outputDocTagsSceneCoTags", outputDocTagsSceneCoTags.getText());

			if (cbDropCapChapters.isSelected())
				child.put("cbDropCapChapters", "selected");
			else
				child.put("cbDropCapChapters", "");

			if (cbWantTextChptOutput.isSelected())
				child.put("cbWantTextChptOutput", "selected");
			else
				child.put("cbWantTextChptOutput", "");

			final String counterDigitChoiceSel = counterDigitChoice.getSelectionModel().getSelectedItem();
			child.put("counterDigitChoice", counterDigitChoiceSel);

			child.put("outputDocTagsMaxLineLength", outputDocTagsMaxLineLength.getText());

			try {
				child.flush();
			} catch (BackingStoreException e) {
				showMessage("Error Deleting profile: " + e, false);
				e.printStackTrace();
			}
		}
	}

	protected void locateFile(final ActionEvent event, final String title, final TextField textField) {
		final FileChooser chooser = new FileChooser();
		if (textField.getText() != null && textField.getText().length() > 0) {
			lastSelectedDirectory = new File(textField.getText());
			if (!lastSelectedDirectory.isDirectory())
				lastSelectedDirectory = lastSelectedDirectory.getParentFile();
			if (!lastSelectedDirectory.isDirectory())
				lastSelectedDirectory = null;
		}
		chooser.setInitialDirectory(lastSelectedDirectory);
		chooser.setTitle(title);
		chooser.setInitialFileName("ChapterCount1.csv");
		System.out.println("lastSelectedDirectory = '" + lastSelectedDirectory + "'");
		final File file = chooser.showOpenDialog(new Stage());
		if (file == null) {
			textField.setText("");
			// lastSelectedDirectory = null;
		} else {
			textField.setText(file.getAbsolutePath());
			// lastSelectedDirectory = file.getParentFile();
		}
	}

	protected void locateDir(final ActionEvent event, final String title, final TextInputControl textField) {
		final DirectoryChooser chooser = new DirectoryChooser();

		final File lastDir1 = new File(textField.getText());
		final File lastDir2 = lastSelectedDirectory;
		if (lastDir1 != null && lastDir1.exists()) {
			if (lastDir1.isDirectory())
				chooser.setInitialDirectory(lastDir1);
			else if (lastDir1.getParentFile() != null && lastDir1.getParentFile().exists()
					&& lastDir1.getParentFile().isDirectory())
				chooser.setInitialDirectory(lastDir1.getParentFile());
		} else if (lastDir2 != null && lastDir2.exists()) {
			if (lastDir2.isDirectory())
				chooser.setInitialDirectory(lastDir2);
			else if (lastDir2.getParentFile() != null && lastDir2.getParentFile().exists()
					&& lastDir2.getParentFile().isDirectory())
				chooser.setInitialDirectory(lastDir2.getParentFile());
		}

		// if (textField.getText() != null && textField.getText().length() > 0)
		// chooser.setInitialDirectory(new File(textField.getText()));
		// else
		// chooser.setInitialDirectory(lastSelectedDirectory);
		chooser.setTitle(title);
		final File file = chooser.showDialog(new Stage());
		if (file == null) {
			textField.setText("");
			lastSelectedDirectory = null;
		} else {
			textField.setText(file.getAbsolutePath());
			lastSelectedDirectory = file;
		}
	}

	private <T> List<T> getNodesOfType(Pane parent, Class<T> type) {
		if (parent == null)
			return null;
		List<T> elements = new ArrayList<>();
		for (Node node : parent.getChildren()) {
			if (node instanceof Pane) {
				elements.addAll(getNodesOfType((Pane) node, type));
			} else if (type.isAssignableFrom(node.getClass())) {
				// noinspection unchecked
				elements.add((T) node);
			}
		}
		return Collections.unmodifiableList(elements);
	}

	public void setProps(final Properties props) {
		this.appProps = props;
	}

	// private void automaticFromInput() {
	// if (outputDirText.getText() == null || outputDirText.getText().length() <
	// 1) {
	// final File asdf1 = new File(inputFileText.getText());
	// final File asdf2 = asdf1.getParentFile();
	// if (asdf2 != null && asdf2.exists() && asdf2.isDirectory()) {
	// final File asdf3a = new File(asdf2, "/archive/");
	// final File asdf3b = new File(asdf3a, "/chapters/");
	// outputDirText.setText(asdf3b.getAbsolutePath());
	// final String fileName = getFileName(asdf1);
	// final File asdf4 = new File(asdf3a, fileName + ".html");
	// outputFileText.setText(asdf4.getAbsolutePath());
	// }
	// }
	// }

	// private String getFileName(final File file) {
	// final StringBuffer sbuf = new StringBuffer(file.getName());
	// final int idx = sbuf.lastIndexOf(".");
	// if (idx >= 0) {
	// final int idxL = sbuf.length() - idx;
	// sbuf.setLength(sbuf.length() - idxL);
	// }
	// return sbuf.toString();
	// }

}
