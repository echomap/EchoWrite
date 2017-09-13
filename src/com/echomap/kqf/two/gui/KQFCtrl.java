package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.FileLooper;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class KQFCtrl implements Initializable {
	private final static Logger LOGGER = LogManager.getLogger(KQFCtrl.class);

	private File lastSelectedDirectory = null;
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	// final Preferences userPrefs =
	// Preferences.userNodeForPackage(KQFCtrl.class);

	@FXML
	private TextArea lastRunText;
	@FXML
	private TextField fmtModeText;
	@FXML
	private TextField chpDivText;
	@FXML
	private TextField secDivText;
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
	private TextField outputFileText;
	@FXML
	private TextField outputDirText;
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
	private ComboBox<String> titleOneText;

	@FXML
	private HBox profileButtonBox;

	@FXML
	private Button inputFileBtn;
	@FXML
	private Button loadOutputDirBtn;
	@FXML
	private Button loadOutputFileBtn;
	@FXML
	private Button wordCountBtn;
	@FXML
	private Button countOutputBtn;
	@FXML
	private Button outlineBtn;
	@FXML
	private Button outlineOutputBtn;
	@FXML
	private Button formatBtn;

	@FXML
	private CheckBox cbRemoveDiv;
	@FXML
	private CheckBox cbCenterStars;
	@FXML
	private CheckBox cbDropCapChapters;
	@FXML
	private CheckBox cbCenterable;
	@FXML
	private ChoiceBox<String> counterDigitChoice;

	private File outputCountDir;
	// private File outputOutlineDir;
	// private File outputOutlineDir1;

	Timer timer = new Timer();
	private MyTimerTask myTimerTask;

	@Override
	public void initialize(java.net.URL arg0, ResourceBundle arg1) {

		assert wordCountBtn != null : "fx:id=\"wordCountBtn1\" was not injected: check your FXML file 'simple.fxml'.";
		assert lastRunText != null : "fx:id=\"lastRunText\" was not injected: check your FXML file 'simple.fxml'.";
		assert fmtModeText != null : "fx:id=\"fmtModeText\" was not injected: check your FXML file 'simple.fxml'.";
		assert chpDivText != null : "fx:id=\"chpDivText\" was not injected: check your FXML file 'simple.fxml'.";
		assert secDivText != null : "fx:id=\"secDivText\" was not injected: check your FXML file 'simple.fxml'.";

		assert docTagStartText != null : "fx:id=\"docTagStartText\" was not injected: check your FXML file 'simple.fxml'.";
		assert docTagEndText != null : "fx:id=\"docTagEndText\" was not injected: check your FXML file 'simple.fxml'.";

		assert titleOneText != null : "fx:id=\"titleOneText\" was not injected: check your FXML file 'simple.fxml'.";
		assert inputFileBtn != null : "fx:id=\"inputFileBtn\" was not injected: check your FXML file 'simple.fxml'.";
		assert loadOutputDirBtn != null : "fx:id=\"loadOutputDirBtn\" was not injected: check your FXML file 'simple.fxml'.";
		assert loadOutputFileBtn != null : "fx:id=\"loadOutputFileBtn\" was not injected: check your FXML file 'simple.fxml'.";

		assert inputFileText != null : "fx:id=\"inputFileText\" was not injected: check your FXML file 'simple.fxml'.";
		assert outputFileText != null : "fx:id=\"outputFileText\" was not injected: check your FXML file 'simple.fxml'.";
		assert outputDirText != null : "fx:id=\"outputDirText\" was not injected: check your FXML file 'simple.fxml'.";
		assert outputEncoding != null : "fx:id=\"outputEncoding\" was not injected: check your FXML file 'simple.fxml'.";

		if (inputFileText.getText() != null && inputFileText.getText().length() > 0)
			lastSelectedDirectory = new File(inputFileText.getText()).getParentFile();
		else if (outputFileText.getText() != null && outputFileText.getText().length() > 0)
			lastSelectedDirectory = new File(outputFileText.getText());
		if (lastSelectedDirectory == null || !lastSelectedDirectory.isDirectory())
			lastSelectedDirectory = null;

		myTimerTask = new MyTimerTask(this.lastRunText);

		// tooltips
		loadChoices();
		loadProfiles();
		lockGui();
	}

	private void loadChoices() {
		counterDigitChoice.setItems(FXCollections.observableArrayList("0", "1", "2", "3", "4"));
		// counterDigitChoice.getValue()
		counterDigitChoice.getSelectionModel().select(3);
	}

	public void handleDeletePrefs(final ActionEvent event) {
		deleteSelectedProp();
	}

	public void handleLoadPrefs(final ActionEvent event) {
		if (titleOneText.getSelectionModel().getSelectedIndex() > -1) {
			loadProps();
			showMessage("Loaded profiles");
		}
	}

	public void handleSavePrefs(final ActionEvent event) {
		final String nowTextOne = titleOneText.getValue();
		if (nowTextOne != null && nowTextOne.length() > 0) {
			saveProps();
			loadProfiles();
			titleOneText.getSelectionModel().select(nowTextOne);
			showMessage("Saved Profile '" + nowTextOne + "'");
		}
	}

	public void handleInputFile(final ActionEvent event) {
		locateFile(event, "Open Input File", inputFileText);
		// automaticFromInput();
	}

	public void handleLoadOutputDir(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputDirText);
		if (!outputDirText.getText().endsWith("chapters")) {
			final File file = new File(outputDirText.getText(), "chapters");
			outputDirText.setText(file.getAbsolutePath());
		}
	}

	public void handleLoadOutputFile(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputFileText);
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
		final File nFile = new File(outputFileText.getText(), outFilename);
		outputFileText.setText(nFile.getAbsolutePath());
	}

	public void handleOutputCountFile(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputCountFileText);
		// final String inFilename = outputCountFileText.getText();
		String outFilename = "\\ChapterCount1.csv";
		// if (inFilename != null && inFilename.length() > 0) {
		// final File inFile = new File(inFilename);
		// final String filenameOnly = inFile.getName();
		// final int extIdx = filenameOnly.lastIndexOf(".");
		// String ext = "";
		// if (extIdx >= 1) {
		// ext = filenameOnly.substring(extIdx + 1);
		// outFilename = filenameOnly.replaceAll(ext, "html");
		// } else {
		// outFilename = filenameOnly + ".html";
		// }
		// }
		final File nFile = new File(outputCountFileText.getText(), outFilename);
		outputCountFileText.setText(nFile.getAbsolutePath());
		outputCountDir = nFile.getParentFile();
	}

	public void handleOutputOutlineFile(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputOutlineFileText);
		String outFilename = "/Outline1.csv";
		final File nFile = new File(outputOutlineFileText.getText(), outFilename);
		outputOutlineFileText.setText(nFile.getAbsolutePath());
		// outputOutlineDir = nFile.getParentFile();
	}

	public void handleOutputOutlineFile1(final ActionEvent event) {
		locateDir(event, "Open Output Dir ", outputOutlineFileText1);
		String outFilename = "/Outline1.csv";
		final File nFile = new File(outputOutlineFileText1.getText(), outFilename);
		outputOutlineFileText1.setText(nFile.getAbsolutePath());
		// outputOutlineDir1 = nFile.getParentFile();
	}

	public void handleClose(final ActionEvent event) {
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		doCleanup();
		stage.close();
	}

	protected void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");
		myTimerTask.cancel();
		timer.cancel();
		myTimerTask = null;
		timer = null;
	}

	private void setLastRunText(String fmtt) {
		lastRunText.setText(fmtt + "\r\n" + lastRunText.getText());
	}

	public void handleDoCountAction(final ActionEvent event) {
		// lastRunText.setText("Running word count...");
		setLastRunText("Running word count...");
		lockGui();
		// timer.cancel();
		// timer = new Timer();
		try {
			// final CountBiz biz = new CountBiz();
			final FormatDao formatDao = new FormatDao();
			setupDao(formatDao);
			// biz.format(inputFileText.getText(),
			// outputCountDir.getAbsolutePath(), outputCountFileText.getText(),
			// formatDao);
			final FileLooper fileLooper = new FileLooper();
			fileLooper.count(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlockGui();
			String fmtt = "Done running COUNT ( " + getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
			setLastRunText(fmtt);
			startTimerTask();
		}
	}

	public void handleDoOutlineAction(final ActionEvent event) {
		// lastRunText.setText("Running word count...");
		setLastRunText("Running OUTLINE...");
		lockGui();
		try {
			// final OutlineBiz biz = new OutlineBiz();
			final FormatDao formatDao = new FormatDao();
			setupDao(formatDao);
			//
			// biz.runOutline(inputFileText.getText(),
			// outputOutlineFileText.getText(), formatDao.getDocTagStart(),
			// formatDao.getDocTagEnd());
			final FileLooper fileLooper = new FileLooper();
			fileLooper.outline(formatDao);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlockGui();
			setLastRunText("Done running OUTLINE (" + getCurrentDateFmt() + ")");
			startTimerTask();
		}
	}

	public void handleDoFormatAction(final ActionEvent event) {
		LOGGER.info("That was easy, wasn't it?");
		setLastRunText("Running...");
		lockGui();
		try {
			// final FormatBiz biz = new FormatBiz();
			final FormatDao formatDao = new FormatDao();
			setupDao(formatDao);
			// biz.format(formatDao);
			final FileLooper fileLooper = new FileLooper();
			fileLooper.format(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlockGui();
			setLastRunText("Done running FORMAT (" + getCurrentDateFmt() + ")");
			startTimerTask();
		}
	}

	private void setupDao(final FormatDao formatDao) {
		// Setup the argument passed
		formatDao.setInputFilename(inputFileText.getText());
		formatDao.setOutputFilename(outputFileText.getText());

		formatDao.setOutputCountFile(outputCountFileText.getText());
		formatDao.setOutputOutlineFile(outputOutlineFileText.getText());
		formatDao.setOutputOutlineFile1(outputOutlineFileText1.getText());

		// formatDao.setou
		formatDao.setWriteChapters(outputDirText.getText());

		formatDao.setStoryTitle1(titleTwoText.getText());
		formatDao.setStoryTitle2(titleThreeText.getText());
		formatDao.setFormatMode(fmtModeText.getText());// "Sigil"

		formatDao.setChapterDivider(chpDivText.getText());// "-=");
		formatDao.setSectionDivider(secDivText.getText());// "-=");

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

		if (outputEncoding.getText() != null && outputEncoding.getText().length() > 0)
			formatDao.setOutputEncoding(outputEncoding.getText());

		final Integer itm = new Integer((String) counterDigitChoice.getSelectionModel().getSelectedItem());
		formatDao.setCountOutputDigits(itm);

	}

	private void lockGui() {
		inputFileBtn.setDisable(true);
		loadOutputDirBtn.setDisable(true);
		loadOutputFileBtn.setDisable(true);
		wordCountBtn.setDisable(true);
		countOutputBtn.setDisable(true);
		outlineBtn.setDisable(true);
		formatBtn.setDisable(true);
	}

	private void unlockGui() {
		inputFileBtn.setDisable(false);
		loadOutputDirBtn.setDisable(false);
		loadOutputFileBtn.setDisable(false);
		wordCountBtn.setDisable(false);
		countOutputBtn.setDisable(false);
		outlineBtn.setDisable(false);
		formatBtn.setDisable(false);
	}

	private void startTimerTask() {
		this.myTimerTask = new MyTimerTask(this.lastRunText);
		timer.cancel();
		timer = new Timer();
		timer.schedule(myTimerTask, 5000);
	}

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
		final Preferences userPrefs = Preferences.userNodeForPackage(KQFCtrl.class);
		return userPrefs;
	}

	private void loadProfiles() {
		titleOneText.getItems().clear();
		try {
			final String[] prefkeys = getPrefs().childrenNames();
			if (prefkeys != null && prefkeys.length > 0) {
				for (final String str1 : prefkeys) {
					titleOneText.getItems().add(str1);
				}
			}
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deleteSelectedProp() {
		final String key = titleOneText.getValue();
		// final Preferences child = userPrefs.node(titleOneText.getValue());
		getPrefs().remove(key);

	}

	private void showMessage(final String msg) {
		lastRunText.setText(msg);
	}

	private String getCurrentDateFmt() {
		final Calendar cal = Calendar.getInstance();
		dateFormat.setTimeZone(cal.getTimeZone());
		String txt = dateFormat.format(cal.getTime());
		LOGGER.debug("date = " + txt);
		return txt;
	}

	protected void loadProps() {
		final String key = titleOneText.getValue();
		final Preferences child = getPrefs().node(key);

		titleOneText.setValue(child.get("titleOne", ""));
		titleTwoText.setText(child.get("titleTwo", ""));
		titleThreeText.setText(child.get("titleThree", ""));
		inputFileText.setText(child.get("inputFile", ""));
		outputFileText.setText(child.get("ouputFile", ""));
		outputDirText.setText(child.get("outputDir", ""));

		outputOutlineFileText.setText(child.get("ouputOutlineFile", ""));
		outputOutlineFileText1.setText(child.get("ouputOutlineFile1", ""));

		chpDivText.setText(child.get("chpDiv", ""));
		secDivText.setText(child.get("secDiv", ""));

		docTagStartText.setText(child.get("docTagStart", "[[*"));
		docTagEndText.setText(child.get("docTagEnd", "*]]"));

		fmtModeText.setText(child.get("fmtMode", ""));
		outputEncoding.setText(child.get("outputEncoding", ""));

		outputCountFileText.setText(child.get("ouputCountFile", ""));

		if (outputCountFileText.getText().length() < 1) {
			outputCountDir = new File(inputFileText.getText()).getParentFile();
			outputCountFileText.setText(outputCountDir + "\\ChapterCount1.csv");
		} else {
			outputCountDir = new File(outputCountFileText.getText()).getParentFile();
		}
		if (outputOutlineFileText.getText().length() < 1) {
			// outputOutlineDir = new
			// File(inputFileText.getText()).getParentFile();
			outputOutlineFileText.setText(outputCountDir + "\\Outline1.csv");
		} else {
			// outputOutlineDir = new
			// File(outputOutlineFileText.getText()).getParentFile();
		}
		if (outputOutlineFileText1.getText().length() < 1) {
			// outputOutlineDir = new
			// File(inputFileText.getText()).getParentFile();
			outputOutlineFileText1.setText(outputCountDir + "\\DocTags.csv");
		} else {
			// outputOutlineDir = new
			// File(outputOutlineFileText.getText()).getParentFile();
		}
		// counterDigitChoice.getSelectionModel().select(3);
		unlockGui();
	}

	protected void saveProps() {
		final String key = titleOneText.getValue();
		if (key != null && key.trim().length() > 0) {
			final Preferences child = getPrefs().node(key);

			child.put("titleOne", key);
			child.put("titleTwo", titleTwoText.getText());
			child.put("titleThree", titleThreeText.getText());

			child.put("inputFile", inputFileText.getText());
			child.put("ouputFile", outputFileText.getText());
			child.put("outputDir", outputDirText.getText());

			child.put("ouputCountFile", outputCountFileText.getText());
			child.put("ouputOutlineFile", outputOutlineFileText.getText());
			child.put("ouputOutlineFile1", outputOutlineFileText1.getText());

			child.put("chpDiv", chpDivText.getText());
			child.put("secDiv", secDivText.getText());

			child.put("docTagStart", docTagStartText.getText());
			child.put("docTagEnd", docTagEndText.getText());

			child.put("fmtMode", fmtModeText.getText());
			child.put("outputEncoding", outputEncoding.getText());

			// counterDigitChoice.getSelectionModel().select(3);

			try {
				child.flush();
			} catch (BackingStoreException e) {
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

	protected void locateDir(final ActionEvent event, final String title, final TextField textField) {
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
