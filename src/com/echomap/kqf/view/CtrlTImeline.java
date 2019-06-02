package com.echomap.kqf.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.looper.data.TreeData;
import com.echomap.kqf.looper.data.TreeTimeData;
import com.echomap.kqf.looper.data.TreeTimeSubData;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public class CtrlTImeline extends BaseCtrl implements Initializable, WorkFinishedCallback {

	private final static Logger LOGGER = LogManager.getLogger(CtrlTImeline.class);
	static public final DateFormat myLogDateFormat = new SimpleDateFormat("HH:mm:ss:ms");

	@FXML
	private TreeView<TreeData> dataTimeTree;// <T>
	@FXML
	private TreeView<TreeData> dataActorTree;// <T>
	@FXML
	private TreeView<TreeData> dataItemTree;// <T>

	@FXML
	private TextArea loggingArea;

	private Profile selectedProfile = null;

	private ProfileManager profileManager = null;
	private Calendar myLogDateCalendar = null;
	private TreeItem<TreeData> rootTime = null;
	private TreeItem<TreeData> rootActors = null;
	private TreeItem<TreeData> rootThings = null;
	private MyWorkDoneNotify myWorkDoneNotify = null;

	@Override
	public void workFinished(String msg) {
		// this.unlockGui(msg);
	}

	@Override
	public void workFinished() {
		// this.unlockGui();
	}

	@Override
	public void workFinished(final Object payload) {
		// this.unlockGui(msg);
		if (payload != null) {
			final StringBuilder sbuf = new StringBuilder();
			sbuf.append("Time units:");
			final List<TreeTimeData> datalistTimeDate = (List<TreeTimeData>) payload;
			for (final TreeTimeData treeTimeData : datalistTimeDate) {
				sbuf.append(",");
				sbuf.append(treeTimeData.toString());
			}
			sbuf.append(".");
			writeToScreen(sbuf.toString());
			sbuf.setLength(0);

			// sbuf.append("Data units:");
			datalistTimeDate.stream().forEach((treeTimeData) -> {
				final TreeItem<TreeData> lastElementTime = new TreeItem<>(treeTimeData);
				rootTime.getChildren().add(lastElementTime);

				//
				final List<String> listActors = new ArrayList<>();
				final Map<String, List<TreeTimeSubData>> actorDataMap = new HashMap<>();
				final List<String> listThings = new ArrayList<>();
				final Map<String, List<TreeTimeSubData>> thingsDataMap = new HashMap<>();

				//
				final List<String> data = treeTimeData.getData();
				// final List<TreeTimeSubData> dataParsed =
				// treeTimeData.getDataParsed();
				for (final String ttData : data) {
					final TreeTimeSubData ttsd = new TreeTimeSubData();
					ttsd.addData("time", treeTimeData.getTag());
					// parse
					int idxA1 = ttData.indexOf("@");
					if (idxA1 > -1) {
						int idxA2 = ttData.indexOf("@", idxA1 + 1);
						while (idxA1 > -1 && idxA1 != ttData.length()) {
							if (idxA2 < 0)
								idxA2 = ttData.length();
							String str1 = ttData.substring(idxA1, idxA2);
							LOGGER.debug("Str2: '" + str1.trim() + "'");
							// name value , @verb=has
							parseNameValue(str1.trim(), ttsd);
							idxA1 = idxA2;
							idxA2 = ttData.indexOf("@", idxA1 + 1);
						}
						treeTimeData.addDataParsed(ttsd);
					} else {
						int idxB = 0;
						int idxE = ttData.indexOf(":");
						final String[] listData = { "char", "item", "verb" };
						int idx = 0;
						while (idxB > -1 && idxB < ttData.length()) {
							if (idxE < 0)
								idxE = ttData.length();
							String str1 = ttData.substring(idxB, idxE);
							LOGGER.debug("Str1: '" + str1.trim() + "'");
							ttsd.addData(listData[idx], str1.trim());
							treeTimeData.addDataParsed(ttsd);
							idx++;
							idxB = idxE + 1;
							idxE = ttData.indexOf(":", idxB);
						}
					}
					// add to tree item
					if (ttsd != null && !ttsd.isEmpty()) {
						lastElementTime.getChildren().add(new TreeItem<>(ttsd));
						sbuf.append("ttsd=" + ttsd.toSimpleString());

						if (ttsd.getData().containsKey("char")) {
							final String valActor = ttsd.getData().get("char");
							if (!listActors.contains(valActor)) {
								listActors.add(valActor);
							}
							List<TreeTimeSubData> listADM = actorDataMap.get(valActor);
							if (listADM == null) {
								listADM = new ArrayList<>();
								actorDataMap.put(valActor, listADM);
							}
							listADM.add(ttsd);
						}
						if (ttsd.getData().containsKey("item")) {
							final String valActor = ttsd.getData().get("item");
							if (!listThings.contains(valActor)) {
								listThings.add(valActor);
							}
							List<TreeTimeSubData> listADM = thingsDataMap.get(valActor);
							if (listADM == null) {
								listADM = new ArrayList<>();
								thingsDataMap.put(valActor, listADM);
							}
							listADM.add(ttsd);
						}

					} else {
						sbuf.append("Data is empty");
					}
					lastElementTime.setExpanded(true);
					writeToScreen(sbuf.toString());
					sbuf.setLength(0);
				}

				//
				// final List<TreeTimeSubData> dataParsed =
				// treeTimeData.getDataParsed();
				for (final String valActor : listActors) {
					final TreeItem<TreeData> lastElementActor = new TreeItem<>(new TreeTimeData(valActor));
					rootActors.getChildren().add(lastElementActor);

					final List<TreeTimeSubData> listADM = actorDataMap.get(valActor);
					for (final TreeTimeSubData ttsd : listADM) {
						lastElementActor.getChildren().add(new TreeItem<>(ttsd));
					}
					lastElementActor.setExpanded(true);
				}
				for (final String valThing : listThings) {
					final TreeItem<TreeData> lastElementThing = new TreeItem<>(new TreeTimeData(valThing));
					rootThings.getChildren().add(lastElementThing);

					final List<TreeTimeSubData> listADM = thingsDataMap.get(valThing);
					for (final TreeTimeSubData ttsd : listADM) {
						lastElementThing.getChildren().add(new TreeItem<>(ttsd));
					}
					lastElementThing.setExpanded(true);
				}

			});

		} else {
			writeToScreen("No data to write to log");
		}
	}

	private void parseNameValue(final String line, final TreeTimeSubData ttsd) {
		String mKey = null;
		String mValue = null;

		// Create a Pattern object
		// -=\s+(?<sname>Section)\s+((?<snum>\w+):\s+)?(?<stitle>.*)\s+=-
		final Pattern r = Pattern.compile("@(?<mKey>\\w+)=(?<mValue>\\w+)");
		// Now create matcher object.
		final Matcher matcher = r.matcher(line);
		if (matcher.find()) {
			try {
				mKey = matcher.group("mKey");
			} catch (Exception e) {
				LOGGER.error("mKey in text not found", e);
				e.printStackTrace();
			}
			try {
				mValue = matcher.group("mValue");
			} catch (Exception e) {
				LOGGER.error("mValue in text not found", e);
				e.printStackTrace();
			}
			ttsd.addData(mKey, mValue);
		}
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		// setTooltips(outerMostContainer);
		//
		lockGui();
		// fixFocus();
		//
		myWorkDoneNotify = new MyWorkDoneNotify(loggingArea, loggingArea, this);
		//
		setProfileChangeMade(false);

		myLogDateCalendar = Calendar.getInstance();
		myLogDateFormat.setTimeZone(myLogDateCalendar.getTimeZone());

		// Tree Setup: Events
		final TreeTimeData rte1 = new TreeTimeData("Time Events");
		rootTime = new TreeItem<TreeData>(rte1);
		dataTimeTree.setRoot(rootTime);
		rootTime.setExpanded(true);

		// Tree Setup: Actors
		final TreeTimeData rte2 = new TreeTimeData("Actor Events");
		rootActors = new TreeItem<TreeData>(rte2);
		dataActorTree.setRoot(rootActors);
		rootActors.setExpanded(true);

		// Tree Setup: Things
		final TreeTimeData rte3 = new TreeTimeData("Things Events");
		rootThings = new TreeItem<TreeData>(rte3);
		dataItemTree.setRoot(rootThings);
		rootThings.setExpanded(true);

		// loadData();
		LOGGER.debug("initialize: Done");
	}

	@Override
	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage,
			final Map<String, Object> paramsMap) {
		LOGGER.debug("setupController: Called");
		super.setupController(props, appPreferences, primaryStage, paramsMap);
		//
		paramsMap.put("appVersion", appVersion);
		final Object selectedProfileO = paramsMap.get("selectedProfile");
		if (selectedProfileO != null && selectedProfileO instanceof Profile) {
			selectedProfile = (Profile) selectedProfileO;
		}
		final Object profileManagerO = paramsMap.get("profileManager");
		if (profileManagerO != null && profileManagerO instanceof ProfileManager) {
			profileManager = (ProfileManager) profileManagerO;
		}
		loadData();
		LOGGER.debug("setupController: Done");
	}

	private void loadData() {
		LOGGER.debug("loadData: Called");

		if (selectedProfile != null)
			LOGGER.debug("loadData: selectedProfile set");
		else
			LOGGER.error("loadData: selectedProfile NOT set");

		writeToScreen("Loading Data...");
		// loadDefaults();
		// loadChoices();
		//
		// loadProfile();
		//
		// fixFocus();
		//
		// unlockGUIforProfile();

		// dataTimeTree

		// Load data
		final String inputFileName = selectedProfile.getInputFile();
		final File inputFile;
		// Parse data
		// loop
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, selectedProfile);
			final FileLooper fileLooper = new FileLooper(myWorkDoneNotify);
			fileLooper.timeline(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// baseCtrl.unlockGui();
			String fmtt = "Done running Timeline ( " + BaseCtrl.getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
			// setLastRunText(fmtt);
			// startTimerTask();
		}

		// Display data

		writeToScreen("Data Loaded.");
		LOGGER.debug("loadData: Done");

	}

	private void writeToScreen(final String msg) {
		final StringBuilder sbuf = new StringBuilder();
		final String txt = myLogDateFormat.format(myLogDateCalendar.getTime());
		sbuf.append(txt);
		sbuf.append(": ");
		sbuf.append(msg);
		if (!msg.endsWith("\n"))
			sbuf.append("\n");
		loggingArea.insertText(0, sbuf.toString());
	}

	@Override
	void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");
		// if (myTimerTask != null)
		// myTimerTask.cancel();
		// if (timer != null)
		// timer.cancel();
		// myTimerTask = null;
		// timer = null;
	}

	@Override
	void lockGui() {

	}

	@Override
	void unlockGui() {

	}

	public void handleClearLog(final ActionEvent event) {
		LOGGER.debug("handleClearLog: Called");
		// loggingText.clear();
		LOGGER.debug("handleClearLog: Done");
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// doCleanup();
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	public void handleSettingsClear(final ActionEvent event) {
	}

	public void handleHelpAbout(final ActionEvent event) {
	}

}
