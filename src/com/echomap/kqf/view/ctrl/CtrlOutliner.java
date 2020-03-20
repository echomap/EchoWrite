package com.echomap.kqf.view.ctrl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.TreeData;
import com.echomap.kqf.data.TreeTimeData;
import com.echomap.kqf.data.TreeTimeSubData;
import com.echomap.kqf.datamgr.DataItem;
import com.echomap.kqf.datamgr.DataManagerBiz;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.looper.WorkDoneNotify;
import com.echomap.kqf.looper.data.NestedTreeData;
import com.echomap.kqf.profile.Profile;
import com.echomap.kqf.profile.ProfileManager;
import com.echomap.kqf.view.gui.MyWorkDoneNotify;
import com.echomap.kqf.view.gui.WorkFinishedCallback;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * @author mkatz
 */
public class CtrlOutliner extends BaseCtrl implements Initializable, WorkFinishedCallback {

	private final static Logger LOGGER = LogManager.getLogger(CtrlOutliner.class);

	// private static final DataManagerBiz DATA_MANAGER =
	// DataManagerBiz.getDataManager();

	@FXML
	private TabPane mainTabPane;
	@FXML
	private VBox paneButtons;
	@FXML
	private Button buttonRefresh;

	@FXML
	private TreeView<TreeData> scenesTree;// <T>
	private TreeItem<TreeData> rootScenes = null;
	@FXML
	private TreeView<TreeData> timeTree;// <T>
	private TreeItem<TreeData> rootTimes = null;

	@FXML
	private HBox allTabFilterHeader;
	@FXML
	private ScrollPane alltableScrollPane;
	@FXML
	private TextArea loggingArea;

	//
	//
	private Profile selectedProfile = null;
	private ProfileManager profileManager = null;

	//
	private Calendar myLogDateCalendar = null;

	private MyWorkDoneNotify myWorkDoneNotify = null;
	private WorkDoneNotify processDoneNotify = null;

	final List<NestedTreeData> listOfAllScenes = new ArrayList<>();
	// final List<TreeTimeData> listOfAllTimes = new ArrayList<>();

	final List<String> columnNameDataMap = new ArrayList<>();

	/**
	 * 
	 */
	@Override
	protected String worktype() {
		return EchoWriteConst.WINDOWKEY_OUTLINERGUI;
	}

	@Override
	public void workFinished(final String msg) {
		// this.unlockGui(msg);
		LOGGER.warn("workFinished w/msg = '" + msg + "'");
		// unlockGui();
		parseData();
	}

	@Override
	public void workFinished() {
		// this.unlockGui();
		LOGGER.warn("workFinished");
		unlockGui();
	}

	@Override
	public void workFinished(final Object payload) {
		// this.unlockGui(msg);
		if (payload != null) {
			parseData();
		} else {
			writeToScreen("No data to write to log");
		}
		unlockGui();
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
		final Object processDoneNotifyO = paramsMap.get("processDoneNotify");
		if (processDoneNotifyO != null && processDoneNotifyO instanceof WorkDoneNotify) {
			processDoneNotify = (WorkDoneNotify) processDoneNotifyO;
		}

		if (profileManager == null) {
			profileManager = new ProfileManager();
			profileManager.loadProfileData();
		}

		final Object preLoadProfileO = paramsMap.get("profilename");
		if (preLoadProfileO != null) {
			final String preLoadProfileKey = (String) preLoadProfileO;
			profileManager.selectProfileByKey(preLoadProfileKey);
			// final List<Profile> profiles = profileManager.getProfiles();
			// for (Profile profile : profiles) {
			// if (profile.getKey().compareTo(preLoadProfileKey) == 0)
			// selectedProfile = profile;
			// }
		}

		parseData();
		LOGGER.debug("setupController: Done");
	}

	class SortbyMarker implements Comparator<TreeTimeSubData> {
		// Used for sorting in ascending order of
		// roll number
		public int compare(TreeTimeSubData a, TreeTimeSubData b) {
			final String markerA = a.getData().get("marker");
			final String markerB = b.getData().get("marker");

			final Integer markerAI = markerA == null ? null : Integer.valueOf(markerA);
			final Integer markerBI = markerB == null ? null : Integer.valueOf(markerB);

			//
			if ((markerAI == null && markerBI == null) || markerAI == markerBI)
				return 0;
			if (markerAI != null && markerBI != null && markerAI < markerBI)
				return -1;
			return markerAI.compareTo(markerBI);
		}
	}

	// Custom Comparator to sort the map according to the natural
	// ordering of its values
	class CustomComparator implements Comparator<String> {
		private Map<String, TreeTimeSubData> map;
		// private String sortByType = "marker";

		public CustomComparator(final Map<String, TreeTimeSubData> map2) {
			// , final String sortByType) {
			this.map = new HashMap<>(map2);
			// this.sortByType = sortByType;
		}

		@Override
		public int compare(final String s1, final String s2) {

			TreeTimeSubData ttsd1 = map.get(s1);
			TreeTimeSubData ttsd2 = map.get(s2);
			final String markerA = ttsd1.getData().get("marker");
			final String markerB = ttsd2.getData().get("marker");

			final Integer markerAI = markerA == null ? null : Integer.valueOf(markerA);
			final Integer markerBI = markerB == null ? null : Integer.valueOf(markerB);

			if ((markerAI == null && markerBI == null) || markerAI.equals(markerBI))
				return ttsd1.getData().get("item").compareTo(ttsd2.getData().get("item"));
			// return 0;
			// if (markerAI == null)
			// return
			// ttsd1.getData().get("item").compareTo(ttsd2.getData().get("item"));
			if (markerBI == null)
				return ttsd1.getData().get("item").compareTo(ttsd2.getData().get("item"));
			if (markerBI > markerAI)
				return 1;
			return markerAI.compareTo(markerBI);
			// return map.get(s1).compareTo(map.get(s2));// , sortByType);
		}
	}

	// Custom Comparator to sort the map according to the natural
	// ordering of its values
	@SuppressWarnings("rawtypes")
	class GenericComparator<K, V extends Comparable> implements Comparator<K> {
		private Map<K, V> map;

		public GenericComparator(Map<K, V> map) {
			this.map = new HashMap<>(map);
		}

		@SuppressWarnings("unchecked")
		@Override
		public int compare(K s1, K s2) {
			return map.get(s1).compareTo(map.get(s2));
		}
	}

	// Custom function to sort Map by values using TreeMap
	public Map<String, TreeTimeSubData> sortByValues(final Map<String, TreeTimeSubData> mapp) {
		@SuppressWarnings("rawtypes")
		final Comparator comparator = new CustomComparator(mapp);
		@SuppressWarnings("unchecked")
		final TreeMap<String, TreeTimeSubData> sortedMap = new TreeMap<>(comparator);
		sortedMap.putAll(mapp);
		return sortedMap;
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

		createRoots();

		unlockAllButtons(paneButtons);

		// loadData();
		LOGGER.debug("initialize: Done");
	}

	private void createRoots() {
		myLogDateCalendar = Calendar.getInstance();
		EchoWriteConst.myLogDateFormat.setTimeZone(myLogDateCalendar.getTimeZone());

		createTabs();
		// Tree Setup: Events

		//
		final TreeTimeData rte1 = new TreeTimeData("Scene Events");
		rootScenes = new TreeItem<TreeData>(rte1);
		scenesTree.setRoot(rootScenes);
		rootScenes.setExpanded(true);

		final TreeTimeData rte2 = new TreeTimeData("Times");
		rootTimes = new TreeItem<TreeData>(rte2);
		timeTree.setRoot(rootTimes);
		rootTimes.setExpanded(true);

		// Tree Setup: Actors
		// final TreeTimeData rte2 = new TreeTimeData("Actor Events");
		// rootActors = new TreeItem<TreeData>(rte2);
		// dataActorTree.setRoot(rootActors);
		// rootActors.setExpanded(true);

		// Tree Setup: Things
		// final TreeTimeData rte3 = new TreeTimeData("Things Events");
		// rootThings = new TreeItem<TreeData>(rte3);
		// dataItemTree.setRoot(rootThings);
		// rootThings.setExpanded(true);

		// Tree Setup: Ending Active
		// final TreeTimeData rte4 = new TreeTimeData("End Things");
		// rootEndThings = new TreeItem<TreeData>(rte4);
		// dataEndThingsTree.setRoot(rootEndThings);
		// rootEndThings.setExpanded(true);

		//
		// createTableThingsEnd();
		// createTableAllThings();
	}

	private void createTabs() {
		//
		final List<String> tablist = new ArrayList<>();
		final ObservableList<Tab> tabs = mainTabPane.getTabs();
		for (final Tab tab : tabs) {
			tablist.add(tab.getText());
		}
		//
		if (!tablist.contains("Scenes")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Scenes");
			scenesTree = tv;

			final BaseCtrl baseCtrl = this;
			final MenuItem item1 = new MenuItem("View Characters");
			item1.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					LOGGER.debug("handle item called");
					// final TreeItem<TreeData> treeSel =
					// scenesTree.getSelectionModel().getSelectedItem();
					final int idx = scenesTree.getSelectionModel().getSelectedIndex();
					// TODO
					final NestedTreeData thisItem = listOfAllScenes.get(idx - 1);
					final TreeTimeData lastTimeDate = thisItem.getLastDateTime();

					// TODO ?? parseTimeDate(lastTimeDate, "marker");

					// open a window with this data

					// take the time mark and send all data... in case of out of
					// order time?

					final Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("appVersion", appVersion);
					paramsMap.put("selectedProfile", selectedProfile);
					paramsMap.put("selectedProfileKey", selectedProfile.getKey());
					paramsMap.put("profileManager", profileManager);
					paramsMap.put("processDoneNotify", myWorkDoneNotify);
					paramsMap.put(EchoWriteConst.PARAMMAP_MODAL, false);
					paramsMap.put(EchoWriteConst.PARAMMAP_MODALMODE, 2);
					paramsMap.put("listOfAllScenes", listOfAllScenes);
					paramsMap.put("lastTimeDate", lastTimeDate);
					//

					final String WINDOW_TITLE_FMT = "EchoWrite: Character Viewer: (v%s)";
					final String windowTitle = String.format(WINDOW_TITLE_FMT, appProps.getProperty("version"));
					tryopenNewWindow(EchoWriteConst.WINDOWKEY_VIEWCHARS, windowTitle, loggingArea, null, baseCtrl,
							paramsMap);

				}
			});

			final MenuItem itemH1 = new MenuItem("Collapse Level");
			itemH1.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					LOGGER.debug("handle item called");
					final TreeItem<TreeData> treeSel = scenesTree.getSelectionModel().getSelectedItem();
					if (treeSel != null)
						if (treeSel.getParent() != null) {
							for (@SuppressWarnings("rawtypes")
							TreeItem tab : treeSel.getParent().getChildren()) {
								tab.setExpanded(false);
							}
						}
				}
			});
			final MenuItem itemH2 = new MenuItem("Expand Level");
			itemH2.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					LOGGER.debug("handle item called");
					final TreeItem<TreeData> treeSel = scenesTree.getSelectionModel().getSelectedItem();
					if (treeSel != null)
						if (treeSel.getParent() != null) {
							for (@SuppressWarnings("rawtypes")
							TreeItem tab : treeSel.getParent().getChildren()) {
								tab.setExpanded(true);
							}
						}
				}
			});
			ContextMenu treeContextMenu = new ContextMenu();
			treeContextMenu.getItems().add(item1);
			treeContextMenu.getItems().add(itemH1);
			treeContextMenu.getItems().add(itemH2);
			tv.setContextMenu(treeContextMenu);

			// tv.addEventHandler(EventType < T > eventtype, new
			// EventHandler<ActionEvent>() {
			// @Override
			// public void handle(final ActionEvent event) {
			// LOGGER.debug("Tree View Event");
			// }
			// });
		}
		if (!tablist.contains("Time")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Time");
			timeTree = tv;
		}
		// if (!tablist.contains("Actor Tree")) {
		// final TreeView<TreeData> tv = createTreeViewTab(tabs, "Actor Tree");
		// dataActorTree = tv;
		// }
		// if (!tablist.contains("Things Tree")) {
		// final TreeView<TreeData> tv = createTreeViewTab(tabs, "Things Tree");
		// dataItemTree = tv;
		// }
		// if (!tablist.contains("End Things Tree")) {
		// final TreeView<TreeData> tv = createTreeViewTab(tabs, "End Things
		// Tree");
		// dataEndThingsTree = tv;
		// }
	}

	private TreeView<TreeData> createTreeViewTab(final ObservableList<Tab> tabs, final String text) {
		final Tab tab = new Tab(text);
		// final Node node = tab.getContent();
		final AnchorPane apan = new AnchorPane();
		//
		final TreeView<TreeData> tv = new TreeView<>();
		apan.getChildren().add(tv);
		AnchorPane.setTopAnchor(tv, 0.0);
		AnchorPane.setLeftAnchor(tv, 0.0);
		AnchorPane.setRightAnchor(tv, 0.0);
		AnchorPane.setBottomAnchor(tv, 0.0);
		//
		tab.setContent(apan);
		//
		// dataTree2 = tv;
		tabs.add(tab);
		return tv;
	}

	private void parseData() {
		LOGGER.debug("parseData: Called");
		lockGui();

		if (selectedProfile == null)
			LOGGER.error("parseData: selectedProfile NOT set");
		else {
			LOGGER.debug("parseData: selectedProfile set");
			writeToScreen("Parsing data...");
			try {
				//
				// listOfAllTimes.clear();
				listOfAllScenes.clear();
				rootScenes.getChildren().clear();
				rootTimes.getChildren().clear();

				//
				final Image nodeImageChapter = new Image(getClass().getResourceAsStream("/book-icon.png"));
				final Image nodeImageSection = new Image(getClass().getResourceAsStream("/62863-books-icon.png"));

				//
				final List<DataItem> timeList = DataManagerBiz.getDataManager(selectedProfile.getInputFile())
						.getItemsByCategory("time");
				// final List<DataItem> timeList =
				// DATA_MANAGER.getItemsByCategory("time");
				for (final DataItem dataItem : timeList) {
					final TreeTimeData treeTimeData = new TreeTimeData(dataItem.getRawValue());
					// listOfAllTimes.add(treeTimeData);
					final TreeItem<TreeData> thisElementTime = new TreeItem<>(treeTimeData);
					rootTimes.getChildren().add(thisElementTime);
				}

				//
				// final String tagSectionTags = EchoWriteConst.WORD_SECTION;
				// final String tagChapterTags = EchoWriteConst.WORD_CHAPTER;
				final DataManagerBiz DATA_MANAGER = DataManagerBiz.getDataManager(selectedProfile.getInputFile());

				final List<String> tagSceneTags = DATA_MANAGER.getTagListScene();
				tagSceneTags.addAll(DATA_MANAGER.getTagListSubScenes());

				//
				// 0=none, 1=root, 2=section, 3=chapter, 4=other/item/actor/etc
				int lastCat = 0;
				int thisCat = 0;
				final Map<Integer, TreeItem<TreeData>> lastLevelUp = new HashMap<Integer, TreeItem<TreeData>>();
				lastLevelUp.put(1, rootScenes);

				// Sections
				final List<DataItem> dataList = DATA_MANAGER.getItems();
				for (final DataItem dataItem : dataList) {
					final String cat = dataItem.getCategory();
					final NestedTreeData treedata = new NestedTreeData(dataItem.getRawValue());

					//
					TreeItem<TreeData> thisElement;
					if (EchoWriteConst.WORD_CHAPTER.compareTo(cat) == 0 && nodeImageChapter != null) {
						thisElement = new TreeItem<>(treedata, new ImageView(nodeImageChapter));
						thisCat = 3;
					} else if (EchoWriteConst.WORD_SECTION.compareTo(cat) == 0 && nodeImageSection != null) {
						thisElement = new TreeItem<>(treedata, new ImageView(nodeImageSection));
						thisCat = 2;
					} else {
						thisElement = new TreeItem<>(treedata);
						thisCat = 4;
					}

					//
					TreeItem<TreeData> parent = lastLevelUp.get(thisCat - 1);
					if (lastCat == 0) {
						parent = rootScenes;
						thisCat = 1;
						lastLevelUp.put(1, thisElement);
					}
					if (parent == null) {
						parent = lastLevelUp.get(1);
					}
					parent.getChildren().add(thisElement);

					if (thisCat != lastCat) {
						lastLevelUp.put(thisCat, thisElement);
					}
					//
					lastCat = thisCat;
					thisElement.setExpanded(true);
				}
			} finally {
				// baseCtrl.unlockGui();
				// String fmtt = "Done running Timeline ( " +
				// BaseCtrl.getCurrentDateFmt() + ")";
				// LOGGER.debug("fmtt: " + fmtt);
				// setLastRunText(fmtt);
				// startTimerTask();
			}
			writeToScreen("Data Parsed.");
		}
		// Display data
		unlockGui();
		LOGGER.debug("parseData: Done");
	}

	private void reloadData() {
		LOGGER.debug("reloadData: Called");

		if (selectedProfile == null)
			LOGGER.error("loadData: selectedProfile NOT set");
		else {
			LOGGER.debug("loadData: selectedProfile set");

			writeToScreen("Loading Data...");
			try {
				final FormatDao formatDao = new FormatDao();
				profileManager.setupDao(formatDao, selectedProfile);
				final FileLooper fileLooper = new FileLooper(myWorkDoneNotify);
				fileLooper.outliner(formatDao);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// baseCtrl.unlockGui();
				String fmtt = "Done running Timeline ( " + BaseCtrl.getCurrentDateFmt() + ")";
				LOGGER.debug("fmtt: " + fmtt);
				// setLastRunText(fmtt);
				// startTimerTask();
			}
			writeToScreen("Data Loaded.");
		}
		// Display data
		LOGGER.debug("loadData: Done");
	}

	private void writeToScreen(final String msg) {
		final StringBuilder sbuf = new StringBuilder();
		final String txt = EchoWriteConst.myLogDateFormat.format(myLogDateCalendar.getTime());
		sbuf.append(txt);
		sbuf.append(": ");
		sbuf.append(msg);
		if (!msg.endsWith("\n"))
			sbuf.append("\n");
		loggingArea.insertText(0, sbuf.toString());
	}

	@Override
	public void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");
		// if (myTimerTask != null)
		// myTimerTask.cancel();
		// if (timer != null)
		// timer.cancel();
		// myTimerTask = null;
		// timer = null;
		if (processDoneNotify != null)
			processDoneNotify.finalResultFromWork(worktype());
	}

	@Override
	void lockGui() {
		buttonRefresh.setDisable(true);
	}

	@Override
	void unlockGui() {
		unlockAllButtons(paneButtons);
	}

	public void handleRefreshData(final ActionEvent event) {
		LOGGER.debug("handleRefreshData: Called");
		try {
			lockGui();
			createRoots();
			reloadData();
		} catch (Exception e) {
			unlockGui();
		}
		LOGGER.debug("handleRefreshData: Done");
	}

	public void handleClearLog(final ActionEvent event) {
		LOGGER.debug("handleClearLog: Called");
		loggingArea.clear();
		LOGGER.debug("handleClearLog: Done");
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		doCleanup();
		// stage.getOwner()
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	public void handleSettingsClear(final ActionEvent event) {
		LOGGER.debug("handleSettingsClear: Called");
		// TODO
		LOGGER.debug("handleSettingsClear: Done");
	}

	public void handleHelpAbout(final ActionEvent event) {
		LOGGER.debug("handleHelpAbout: Called");
		// TODO
		LOGGER.debug("handleHelpAbout: Done");
	}

	public void handleShowCharacters(final ActionEvent event) {
		LOGGER.debug("handleShowCharacters: Called");
		// TODO
		LOGGER.debug("handleShowCharacters: Done");
	}

	public void handleTreeColapse(final ActionEvent event) {
		LOGGER.debug("handleTreeColapse: Called");
		rootScenes.setExpanded(false);
		LOGGER.debug("handleTreeColapse: Done");
	}

	public void handleTreeExpand(final ActionEvent event) {
		LOGGER.debug("handleTreeExpand: Called");
		rootScenes.setExpanded(true);
		LOGGER.debug("handleTreeExpand: Done");
	}

	// public void handleTreeColapseHere(final ActionEvent event) {
	// LOGGER.debug("handleTreeColapseHere: Called");
	// final TreeItem<TreeData> treeSel =
	// scenesTree.getSelectionModel().getSelectedItem();
	// if (treeSel != null)
	// treeSel.setExpanded(false);
	// LOGGER.debug("handleTreeColapseHere: Done");
	// }
	//
	// public void handleTreeExpandHere(final ActionEvent event) {
	// LOGGER.debug("handleTreeExpandHere: Called");
	// final TreeItem<TreeData> treeSel =
	// scenesTree.getSelectionModel().getSelectedItem();
	// if (treeSel != null)
	// treeSel.setExpanded(true);
	// LOGGER.debug("handleTreeExpandHere: Done");
	// }
}
