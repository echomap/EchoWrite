package com.echomap.kqf.view.ctrl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.TreeData;
import com.echomap.kqf.data.TreeTimeData;
import com.echomap.kqf.datamgr.DataItem;
import com.echomap.kqf.datamgr.DataManagerBiz;
import com.echomap.kqf.datamgr.DataSubItem;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.looper.WorkDoneNotify;
import com.echomap.kqf.looper.data.NestedTreeData;
import com.echomap.kqf.profile.Profile;
import com.echomap.kqf.profile.ProfileManager;
import com.echomap.kqf.view.gui.MyWorkDoneNotify;
import com.echomap.kqf.view.gui.WorkFinishedCallback;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CtrlTImeline extends BaseCtrl implements Initializable, WorkFinishedCallback {

	private final static Logger LOGGER = LogManager.getLogger(CtrlTImeline.class);

	// private static final DataManagerBiz DATA_MANAGER =
	// DataManagerBiz.getDataManager();

	@FXML
	private TabPane mainTabPane;
	@FXML
	private VBox paneButtons;
	@FXML
	private Button buttonRefresh;

	@FXML
	private TreeView<TreeData> dataTimeTree;// <T>
	@FXML
	private TreeView<TreeData> scenesTree;// <T>
	@FXML
	private TreeView<TreeData> dataActorTree;// <T>
	@FXML
	private TreeView<TreeData> dataItemTree;// <T>
	@FXML
	private TableView<DataItem> dataAllThingsTable;// <T>
	@FXML
	private TableView<DataItem> dataThingsEndTable;// <T>
	@FXML
	private TableView<DataItem> dataThingsTable;// <T>
	@FXML
	private TableView<DataItem> dataActorsTable;// <T>

	@FXML
	private HBox allTabFilterHeader;
	@FXML
	private HBox thingsTabFilterHeader;
	@FXML
	private HBox actorsTabFilterHeader;
	@FXML
	private HBox endThingsTabFilterHeader;

	@FXML
	private ScrollPane alltableScrollPane;
	@FXML
	private BorderPane endtableScrollPane;

	@FXML
	private TextArea loggingArea;

	@FXML
	private TextField filterTextChar;
	@FXML
	private TextField filterTextMarker;
	@FXML
	private TextField filterTextNum;
	@FXML
	private TextField filterTextItem;
	@FXML
	private TextField filterTextLoc;
	@FXML
	private TextField filterTextText;

	//
	//
	private Profile selectedProfile = null;
	private ProfileManager profileManager = null;

	//
	private Calendar myLogDateCalendar = null;

	private MyWorkDoneNotify myWorkDoneNotify = null;
	private WorkDoneNotify processDoneNotify = null;

	//
	final Map<String, String> filtersEndthings = new HashMap<>();
	final Map<String, String> filtersAllThings = new HashMap<>();
	final Map<String, String> filtersThings = new HashMap<>();
	final Map<String, String> filtersActors = new HashMap<>();

	//
	final List<String> columnNameDataMapAll = new ArrayList<>();
	final List<String> columnNameDataMapThings = new ArrayList<>();
	final List<String> columnNameDataMapEndThings = new ArrayList<>();
	final List<String> columnNameDataMapActors = new ArrayList<>();

	// private List<DocTag> metaDocTagList = new ArrayList<>();

	//
	// final Map<Integer, List<DataItem>> endThingsListByMarker = new
	// HashMap<>();
	// final Map<String, Map<Integer, List<DataItem>>> endThingsListByItem = new
	// HashMap<>();
	// private List<DataItem> actorList = new ArrayList<>();
	// private List<DataItem> thingList = new ArrayList<>();

	// TIME Tree
	private TreeItem<TreeData> rootTimes = null;
	private TreeItem<TreeData> rootScenes = null;
	private TreeItem<TreeData> rootActors = null;
	private TreeItem<TreeData> rootItems = null;
	// private TreeItem<TreeData> rootEndThings = null;

	//
	private final TimeList timeList = new TimeList();
	// Integer lastTimeMarker = null;
	// final List<Integer> timeList = new ArrayList<>();

	//
	final ObservableList<DataItem> dataALLList = FXCollections.observableArrayList();

	public CtrlTImeline() {

	}

	private class TimeList {
		//
		private Integer lastTimeMarker = null;
		//
		private final List<Integer> timeList = new ArrayList<>();

		public Integer getLastTimeMarker() {
			return lastTimeMarker;
		}

		public void trackTime(final Integer time) {
			timeList.add(time);
			// if (lastTimeMarker == Fnull || time > lastTimeMarker)
			// lastTimeMarker = time;
			if (lastTimeMarker == null)
				lastTimeMarker = time;
			else if (lastTimeMarker < time)
				lastTimeMarker = time;
		}

		public Integer getTimeAfter(final Integer markerI2) {
			Collections.sort(timeList);
			final Integer idx = timeList.indexOf(markerI2);
			if (idx < 0)
				return (markerI2 + 1);
			final Integer val = timeList.get((idx + 1));
			return (val);
		}
	}

	/**
	 * 
	 */
	@Override
	protected String worktype() {
		return EchoWriteConst.WINDOWKEY_TIMELINE;
	}

	@Override
	public void workFinished(final String msg) {
		LOGGER.warn("workFinished w/msg = " + msg);
		parseData();
	}

	@Override
	public void workFinished() {
		LOGGER.warn("workFinished");
		unlockGui();
	}

	@Override
	public void workFinished(final Object payload) {
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
		final String keyP = this.getClass().getSimpleName();
		loadPreferencesForWindow(keyP, primaryStage);

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
			final String preLoadProfile = (String) preLoadProfileO;
			final List<Profile> profiles = profileManager.getProfiles();
			for (Profile profile : profiles) {
				if (profile.getKey().compareTo(preLoadProfile) == 0)
					selectedProfile = profile;
			}
		}

		parseData();
		LOGGER.debug("setupController: Done");
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

	@Override
	public void doSceneHiding(final Stage stage) {

		final String keyP = this.getClass().getSimpleName();
		savePreferencesForWindow(keyP, stage);

		super.doSceneHiding(stage);
	}

	private void createRoots() {
		myLogDateCalendar = Calendar.getInstance();
		EchoWriteConst.myLogDateFormat.setTimeZone(myLogDateCalendar.getTimeZone());

		//
		createTabs();
		//
		createTrees();
		//
		setupTables();
		//
		updateTableAllThingsColumns();
		updateTableThingsColumns();
		updateTableEndThingsColumns();
	}

	private void setupTables() {
		//
		final MenuItem menuItemDumpItemTextAA = new MenuItem("Dump Item Text");
		menuItemDumpItemTextAA.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataThingsEndTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					writeToScreen("Output: " + treeSel);
				}
			}
		});

		final MenuItem menuItemDumpItemTextST1 = new MenuItem("Select time");
		menuItemDumpItemTextST1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataThingsEndTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					final String marker = treeSel.getSubByKeyString(EchoWriteConst.WORD_MARKER);
					final TextField filterfield = getEndThingFilter("marker");
					if (filterfield != null) {
						String markerI = marker;
						if (!StringUtils.isEmpty(marker))
							markerI = marker.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
						filterfield.setText(markerI);
					}
				}
			}
		});

		final MenuItem menuItemDumpItemTextSTB = new MenuItem("Select time before");
		menuItemDumpItemTextSTB.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataThingsEndTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					final String marker = treeSel.getSubByKeyString(EchoWriteConst.WORD_MARKER);
					final TextField filterfield = getEndThingFilter("marker");
					if (filterfield != null) {
						String markerI = marker;
						if (!StringUtils.isEmpty(marker))
							markerI = marker.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
						Integer markerI2 = Integer.valueOf(markerI);
						markerI2--;
						filterfield.setText(markerI2.toString());
					}
				}
			}
		});
		final MenuItem menuItemDumpItemTextSTA = new MenuItem("Select time after");
		menuItemDumpItemTextSTA.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataThingsEndTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					final String marker = treeSel.getSubByKeyString(EchoWriteConst.WORD_MARKER);
					final TextField filterfield = getEndThingFilter("marker");
					if (filterfield != null) {
						String markerI = marker;
						if (!StringUtils.isEmpty(marker))
							markerI = marker.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
						Integer markerI2 = Integer.valueOf(markerI);
						markerI2++;
						markerI2 = timeList.getTimeAfter(markerI2);
						filterfield.setText(markerI2.toString());
					}
				}
			}
		});
		final MenuItem menuItemDumpItemTextDT = new MenuItem("Dump table to text");
		menuItemDumpItemTextDT.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				writeToScreen("<<<<End things table output>>>");
				final ObservableList<DataItem> dateItems = dataThingsEndTable.getItems();
				for (final DataItem dataItem : dateItems) {
					writeToScreen(dataItem.toDocTagString(), false);
				}
				writeToScreen("<<<<End things table output>>>");
			}
		});
		//
		//
		final ContextMenu treeContextMenuA = new ContextMenu();
		treeContextMenuA.getItems().add(menuItemDumpItemTextAA);
		treeContextMenuA.getItems().add(menuItemDumpItemTextST1);
		treeContextMenuA.getItems().add(menuItemDumpItemTextSTA);
		treeContextMenuA.getItems().add(menuItemDumpItemTextSTB);
		treeContextMenuA.getItems().add(menuItemDumpItemTextDT);
		dataThingsEndTable.setContextMenu(treeContextMenuA);
		//

		//
		final MenuItem menuItemDumpItemText2 = new MenuItem("Dump Item Text");
		menuItemDumpItemText2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataAllThingsTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					writeToScreen("Output: " + treeSel);
				}
			}
		});
		//
		final ContextMenu treeContextMenu2 = new ContextMenu();
		treeContextMenu2.getItems().add(menuItemDumpItemText2);
		dataAllThingsTable.setContextMenu(treeContextMenu2);
		//

		//
		final MenuItem menuItemDumpItemTextT = new MenuItem("Dump Item Text");
		menuItemDumpItemTextT.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataThingsTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					writeToScreen("Output: " + treeSel);
				}
			}
		});
		//
		final ContextMenu treeContextMenuT = new ContextMenu();
		treeContextMenuT.getItems().add(menuItemDumpItemTextT);
		dataThingsTable.setContextMenu(treeContextMenuT);
		//

		//
		final MenuItem menuItemDumpItemTextA2 = new MenuItem("Dump Actor Text");
		menuItemDumpItemTextA2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataActorsTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					writeToScreen("Output: " + treeSel);
				}
			}
		});
		//
		final ContextMenu treeContextMenuA2 = new ContextMenu();
		treeContextMenuA2.getItems().add(menuItemDumpItemTextA2);
		dataActorsTable.setContextMenu(treeContextMenuA2);
		//
	}

	private void createTrees() {
		//
		final TreeTimeData rte1 = new TreeTimeData("Time Events");
		rootTimes = new TreeItem<TreeData>(rte1);
		dataTimeTree.setRoot(rootTimes);
		rootTimes.setExpanded(true);

		//
		final TreeTimeData rteE = new TreeTimeData("Scene Events");
		rootScenes = new TreeItem<TreeData>(rteE);
		scenesTree.setRoot(rootScenes);
		rootScenes.setExpanded(true);

		//
		// Tree Setup: Actors
		final TreeTimeData rteA = new TreeTimeData("Actor Events");
		rootActors = new TreeItem<TreeData>(rteA);
		dataActorTree.setRoot(rootActors);
		rootActors.setExpanded(true);

		// Tree Setup: Things
		final TreeTimeData rteT = new TreeTimeData("Things Events");
		rootItems = new TreeItem<TreeData>(rteT);
		dataItemTree.setRoot(rootItems);
		rootItems.setExpanded(true);
		//
	}

	// Time/Date ... All Table ... End Table ... Things Table ..
	private void createTabs() {
		//
		final List<String> tablist = new ArrayList<>();
		final ObservableList<Tab> tabs = mainTabPane.getTabs();
		for (final Tab tab : tabs) {
			tablist.add(tab.getText());
		}
		//
		if (!tablist.contains("Time/Date")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Time/Date");
			dataTimeTree = tv;
		}
		if (!tablist.contains("Actor Tree")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Actor Tree");
			dataActorTree = tv;
		}
		if (!tablist.contains("Things Tree")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Things Tree");
			dataItemTree = tv;
		}

		//
		if (!tablist.contains("Scenes")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Scenes");
			scenesTree = tv;
		}
		if (tablist.contains("Scenes")) {
			final TreeView<TreeData> tv = scenesTree;

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
			//
			final MenuItem itemH3 = new MenuItem("Print Out Item");
			itemH3.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					LOGGER.debug("handle item called");
					final TreeItem<TreeData> treeSel = scenesTree.getSelectionModel().getSelectedItem();
					if (treeSel != null) {
						writeToScreen("Output: " + treeSel);
					}

				}
			});
			//
			final MenuItem itemH4 = new MenuItem("Select EndThings Here");
			itemH4.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					LOGGER.debug("handle item called");
					String dataMessage = "Action canceled per no pertinent data found.";
					final TreeItem<TreeData> treeSel = scenesTree.getSelectionModel().getSelectedItem();
					if (treeSel == null) {
						dataMessage = "Selection failure";
					} else {
						final TreeData td = treeSel.getValue();
						if (td == null || !(td instanceof NestedTreeData)) {
							dataMessage = "Data not found";
						} else {
							final TextField filterfield = getEndThingFilter("marker");
							if (filterfield == null) {
								dataMessage = "Can't find filter element.";
							} else {
								final NestedTreeData tdd = (NestedTreeData) treeSel.getValue();
								final TreeTimeData markerTTD = tdd.getLastDateTime();
								if (markerTTD == null) {
									dataMessage = "No marker data found";
								} else {
									String markerI = markerTTD.getTag();
									if (StringUtils.isEmpty(markerI)) {
										dataMessage = "Marker data not found";
									} else {
										markerI = markerI.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
										filterfield.setText(markerI);
										dataMessage = "End things filter set to: '" + markerI + "' ("
												+ markerTTD.getTag() + ")";
									}
								}
							}
						}
					}
					if (dataMessage != null)
						writeToScreen(dataMessage);
				}
			});
			//
			ContextMenu treeContextMenu = new ContextMenu();
			// treeContextMenu.getItems().add(item1);
			treeContextMenu.getItems().add(itemH1);
			treeContextMenu.getItems().add(itemH2);
			treeContextMenu.getItems().add(itemH3);
			treeContextMenu.getItems().add(itemH4);

			tv.setContextMenu(treeContextMenu);
		}

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

	//
	private List<String> sortColumns(final List<String> columnNames) {
		boolean hasmarker = false;
		final List<String> columnNamesReorder1 = new ArrayList<>();
		final List<String> columnNamesReorder2 = new ArrayList<>();
		final List<String> columnNamesReorder3 = new ArrayList<>();
		final List<String> columnNamesReorder4 = new ArrayList<>();
		if (columnNames.contains("marker")) {
			hasmarker = true;
		}
		// TODO do this better
		int idxM = -1;
		for (final String str : columnNames) {
			if (str.compareToIgnoreCase(EchoWriteConst.WORD_TIME) == 0
					|| str.compareToIgnoreCase(EchoWriteConst.WORD_DAY) == 0
					|| str.compareToIgnoreCase(EchoWriteConst.WORD_DATE) == 0) {
				columnNamesReorder4.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_MARKER) == 0) {
				columnNamesReorder1.add(0, str);
				idxM = 0;
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_NAME) == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_CHAR) == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_COUNT) == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_ID) == 0) {
				columnNamesReorder4.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_STATUS) == 0) {
				columnNamesReorder2.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_LOC) == 0) {
				columnNamesReorder2.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_SLOT) == 0) {
				columnNamesReorder2.add(str);
			} else {
				columnNamesReorder3.add(str);
			}
		}
		//
		columnNames.clear();
		if (hasmarker) {
			columnNames.addAll(columnNamesReorder1);
			columnNames.addAll(columnNamesReorder2);
			columnNames.addAll(columnNamesReorder3);
			columnNames.addAll(columnNamesReorder4);
		} else {
			columnNames.addAll(columnNamesReorder2);
			columnNames.addAll(columnNamesReorder1);
			columnNames.addAll(columnNamesReorder3);
			columnNames.addAll(columnNamesReorder4);
		}
		//
		return columnNames;
	}

	private void updateTableEndThingsColumns() {
		//
		final ObservableList<TableColumn<DataItem, ?>> columns = dataThingsEndTable.getColumns();
		columns.clear();
		sortColumns(columnNameDataMapEndThings);

		// Reorder Columns
		@SuppressWarnings("unchecked")
		final TableColumn<DataItem, ?>[] tableColumnList = new TableColumn[columnNameDataMapEndThings.size()];
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapEndThings.get(i);
			final TableColumn<DataItem, ?> thisCol = new TableColumn<DataItem, Object>(tableColumnName);
			tableColumnList[i] = (thisCol);
			if (tableColumnName.compareTo("marker") == 0) {
				thisCol.setSortType(TableColumn.SortType.ASCENDING);
				dataThingsEndTable.getSortOrder().add(thisCol);
			}
			thisCol.getStyleClass().add("all-table-column-header");
			if (tableColumnName.compareTo(EchoWriteConst.WORD_TYPE) == 0) {
				thisCol.setVisible(false);
			}
			if (tableColumnName.compareTo(EchoWriteConst.WORD_ITEM) == 0) {
				thisCol.setVisible(false);
			}
		}
		//
		dataThingsEndTable.getColumns().clear();
		dataThingsEndTable.getColumns().addAll(tableColumnList);
		BaseCtrl.alignColumnLabelsLeftHack(dataThingsEndTable);
		dataThingsEndTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		dataThingsEndTable.sort();

		// Data Factories
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapEndThings.get(i);
			if ("Marker".compareToIgnoreCase(tableColumnName) == 0) {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, Integer> column2 = (TableColumn<DataItem, Integer>) tableColumnList[i];
				column2.setCellValueFactory(
						cellData -> new SimpleIntegerProperty(cellData.getValue().getSubByKeyInteger("marker"))
								.asObject());
			} else {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, String> column1 = (TableColumn<DataItem, String>) tableColumnList[i];
				column1.setCellValueFactory(
						cellData -> new SimpleStringProperty(cellData.getValue().getSubByKeyString(tableColumnName)));
			}
		}
		// Col1. resize handler

		//
		BaseCtrl.alignColumnLabelsLeftHack(dataThingsEndTable);
		dataThingsEndTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// Filters
		endThingsTabFilterHeader.getChildren().clear();
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapEndThings.get(i);
			if (tableColumnName.compareTo(EchoWriteConst.WORD_TYPE) == 0) {
				continue;
			}
			if (tableColumnName.compareTo(EchoWriteConst.WORD_ITEM) == 0) {
				continue;
			}

			final TextField tf = new TextField();
			tf.setPromptText(tableColumnName);
			tf.setId("EndThingFilter_" + tableColumnName);
			endThingsTabFilterHeader.getChildren().add(tf);

			tf.textProperty().addListener((observable, oldValue, newValue) -> {
				LOGGER.debug("filterText changed from '" + oldValue + "' to '" + newValue + "'");
				setupFilterEndThings(tableColumnName, newValue);
			});
		}
		//
		endtableScrollPane.requestLayout();
	}

	private TextField getEndThingFilter(final String tableColumnName) {
		TextField retVal = null;
		final String key = "EndThingFilter_" + tableColumnName;
		final ObservableList<Node> columns = endThingsTabFilterHeader.getChildren();
		for (Node node : columns) {
			if (key.equals(node.getId()))
				if (node instanceof TextField) {
					retVal = (TextField) node;
					break;
				}
		}
		return retVal;
	}

	// dataActorsTable
	private void updateTableActorsColumns() {
		//
		final ObservableList<TableColumn<DataItem, ?>> columns = dataActorsTable.getColumns();
		columns.clear();
		sortColumns(columnNameDataMapActors);

		// Reorder Columns
		@SuppressWarnings("unchecked")
		final TableColumn<DataItem, ?>[] tableColumnList = new TableColumn[columnNameDataMapActors.size()];
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapActors.get(i);
			final TableColumn<DataItem, ?> thisCol = new TableColumn<DataItem, Object>(tableColumnName);
			tableColumnList[i] = (thisCol);
			if (tableColumnName.compareTo("marker") == 0) {
				thisCol.setSortType(TableColumn.SortType.ASCENDING);
				dataActorsTable.getSortOrder().add(thisCol);
			}
			if (tableColumnName.compareTo(EchoWriteConst.WORD_TYPE) == 0) {
				thisCol.setVisible(false);
			}
			if (tableColumnName.compareTo(EchoWriteConst.WORD_CHAR) == 0) {
				thisCol.setVisible(false);
			}
			thisCol.getStyleClass().add("all-table-column-header");
		}
		//
		dataActorsTable.getColumns().clear();
		dataActorsTable.getColumns().addAll(tableColumnList);
		BaseCtrl.alignColumnLabelsLeftHack(dataActorsTable);
		dataActorsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		dataActorsTable.sort();

		// Data Factories
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapActors.get(i);
			if ("Marker".compareToIgnoreCase(tableColumnName) == 0) {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, Integer> column2 = (TableColumn<DataItem, Integer>) tableColumnList[i];
				column2.setCellValueFactory(
						cellData -> new SimpleIntegerProperty(cellData.getValue().getSubByKeyInteger("marker"))
								.asObject());
			} else {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, String> column1 = (TableColumn<DataItem, String>) tableColumnList[i];
				column1.setCellValueFactory(
						cellData -> new SimpleStringProperty(cellData.getValue().getSubByKeyString(tableColumnName)));
			}
		}
		// Col1. resize handler

		//
		BaseCtrl.alignColumnLabelsLeftHack(dataActorsTable);
		dataActorsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// Filters
		actorsTabFilterHeader.getChildren().clear();
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapActors.get(i);
			if (tableColumnName.compareTo(EchoWriteConst.WORD_TYPE) == 0) {
				continue;
			}
			if (tableColumnName.compareTo(EchoWriteConst.WORD_CHAR) == 0) {
				continue;
			}
			final TextField tf = new TextField();
			tf.setPromptText(tableColumnName);
			actorsTabFilterHeader.getChildren().add(tf);

			tf.textProperty().addListener((observable, oldValue, newValue) -> {
				LOGGER.debug("filterText changed from '" + oldValue + "' to '" + newValue + "'");
				setupFilterActors(tableColumnName, newValue);
			});
		}
		//
	}// updateTableActorsColumns

	private void updateTableThingsColumns() {
		//
		final ObservableList<TableColumn<DataItem, ?>> columns = dataThingsTable.getColumns();
		columns.clear();
		sortColumns(columnNameDataMapThings);

		// Reorder Columns
		@SuppressWarnings("unchecked")
		final TableColumn<DataItem, ?>[] tableColumnList = new TableColumn[columnNameDataMapThings.size()];
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapThings.get(i);
			final TableColumn<DataItem, ?> thisCol = new TableColumn<DataItem, Object>(tableColumnName);
			tableColumnList[i] = (thisCol);
			if (tableColumnName.compareTo("marker") == 0) {
				thisCol.setSortType(TableColumn.SortType.ASCENDING);
				dataThingsTable.getSortOrder().add(thisCol);
			}
			if (tableColumnName.compareTo(EchoWriteConst.WORD_TYPE) == 0) {
				thisCol.setVisible(false);
			}
			thisCol.getStyleClass().add("all-table-column-header");
		}
		//
		dataThingsTable.getColumns().clear();
		dataThingsTable.getColumns().addAll(tableColumnList);
		BaseCtrl.alignColumnLabelsLeftHack(dataThingsTable);
		dataThingsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		dataThingsTable.sort();

		// Data Factories
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapThings.get(i);
			if ("Marker".compareToIgnoreCase(tableColumnName) == 0) {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, Integer> column2 = (TableColumn<DataItem, Integer>) tableColumnList[i];
				column2.setCellValueFactory(
						cellData -> new SimpleIntegerProperty(cellData.getValue().getSubByKeyInteger("marker"))
								.asObject());
			} else {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, String> column1 = (TableColumn<DataItem, String>) tableColumnList[i];
				column1.setCellValueFactory(
						cellData -> new SimpleStringProperty(cellData.getValue().getSubByKeyString(tableColumnName)));
			}
		}
		// Col1. resize handler

		//
		BaseCtrl.alignColumnLabelsLeftHack(dataThingsTable);
		dataThingsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// Filters
		thingsTabFilterHeader.getChildren().clear();
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapThings.get(i);
			if (tableColumnName.compareTo(EchoWriteConst.WORD_TYPE) == 0) {
				continue;
			}
			final TextField tf = new TextField();
			tf.setPromptText(tableColumnName);
			thingsTabFilterHeader.getChildren().add(tf);

			tf.textProperty().addListener((observable, oldValue, newValue) -> {
				LOGGER.debug("filterText changed from '" + oldValue + "' to '" + newValue + "'");
				setupFilterThings(tableColumnName, newValue);
			});
		}
		//
	}

	/**
	 * 
	 */
	private void updateTableAllThingsColumns() {
		//
		final ObservableList<TableColumn<DataItem, ?>> columns = dataAllThingsTable.getColumns();
		columns.clear();
		//
		boolean hasmarker = false;
		final List<String> columnNamesReorder0 = new ArrayList<>();
		final List<String> columnNamesReorder1 = new ArrayList<>();
		final List<String> columnNamesReorder2 = new ArrayList<>();
		if (columnNameDataMapAll.contains("marker")) {
			hasmarker = true;
		}
		//
		for (final String str : columnNameDataMapAll) {
			if (str.compareToIgnoreCase(EchoWriteConst.WORD_TIME) == 0
					|| str.compareToIgnoreCase(EchoWriteConst.WORD_DAY) == 0
					|| str.compareToIgnoreCase(EchoWriteConst.WORD_DATE) == 0) {
				columnNamesReorder2.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_MARKER) == 0) {
				columnNamesReorder0.add(0, str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_TYPE) == 0) {
				columnNamesReorder0.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_NAME) == 0) {
				columnNamesReorder1.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_ACTOR) == 0) {
				columnNamesReorder1.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_COUNT) == 0) {
				columnNamesReorder1.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_NUMBER) == 0) {
				columnNamesReorder1.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_ID) == 0) {
				columnNamesReorder2.add(str);
			} else if (str.compareToIgnoreCase(EchoWriteConst.WORD_DESC) == 0) {
				columnNamesReorder2.add(0, str);
			} else {
				columnNamesReorder2.add(str);
			}
		}
		//
		columnNameDataMapAll.clear();
		if (hasmarker) {
			columnNameDataMapAll.addAll(columnNamesReorder0);
			columnNameDataMapAll.addAll(columnNamesReorder1);
			columnNameDataMapAll.addAll(columnNamesReorder2);
		} else {
			columnNameDataMapAll.addAll(columnNamesReorder2);
			columnNameDataMapAll.addAll(columnNamesReorder1);
			columnNameDataMapAll.addAll(columnNamesReorder0);
		}

		@SuppressWarnings("unchecked")
		final TableColumn<DataItem, ?>[] tableColumnList = new TableColumn[columnNameDataMapAll.size()];
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapAll.get(i);
			final TableColumn<DataItem, ?> thisCol = new TableColumn<DataItem, Object>(tableColumnName);
			tableColumnList[i] = (thisCol);
			if (tableColumnName.compareTo(EchoWriteConst.WORD_MARKER) == 0) {
				thisCol.setSortType(TableColumn.SortType.ASCENDING);
				dataAllThingsTable.getSortOrder().add(thisCol);
			}
			thisCol.getStyleClass().add("all-table-column-header");
		}
		//
		dataAllThingsTable.getColumns().clear();
		dataAllThingsTable.getColumns().addAll(tableColumnList);
		BaseCtrl.alignColumnLabelsLeftHack(dataAllThingsTable);
		dataAllThingsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		dataAllThingsTable.sort();

		// Data Factories
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapAll.get(i);
			if (EchoWriteConst.WORD_MARKER.compareToIgnoreCase(tableColumnName) == 0) {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, Integer> column2 = (TableColumn<DataItem, Integer>) tableColumnList[i];
				column2.setCellValueFactory(cellData -> new SimpleIntegerProperty(
						cellData.getValue().getSubByKeyInteger(EchoWriteConst.WORD_MARKER)).asObject());
				// TOOD can throw exception
			} else {
				@SuppressWarnings("unchecked")
				final TableColumn<DataItem, String> column1 = (TableColumn<DataItem, String>) tableColumnList[i];
				column1.setCellValueFactory(
						cellData -> new SimpleStringProperty(cellData.getValue().getSubByKeyString(tableColumnName)));
			}
		}
		// Col1. resize handler
		// itemCol.widthProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable,
		// Number oldValue, Number newValue) {
		// // columnWidthChanged(COL_KEY, newValue);
		// }
		// });

		//
		BaseCtrl.alignColumnLabelsLeftHack(dataAllThingsTable);
		dataAllThingsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// Filters
		allTabFilterHeader.getChildren().clear();
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapAll.get(i);
			final TextField tf = new TextField();
			tf.setPromptText(tableColumnName);
			allTabFilterHeader.getChildren().add(tf);

			tf.textProperty().addListener((observable, oldValue, newValue) -> {
				LOGGER.debug("filterText changed from '" + oldValue + "' to '" + newValue + "'");
				setupFilterAllThings(tableColumnName, newValue);
			});
		}

		// allTabFilterHeader.autosize();
		alltableScrollPane.requestLayout();
	}

	/**
	 * 
	 *
	 */
	private class TimeLoopLocal {
		int lastCat = 0;
		int thisCat = 0;
		final Map<Integer, TreeItem<TreeData>> lastLevelUp = new HashMap<Integer, TreeItem<TreeData>>();
		// DataItem lastTime = null;
		TreeItem<TreeData> rootTime = null;
		TreeItem<TreeData> rootTimeNode = null;
		final Map<String, TreeItem<TreeData>> actorLookupMap = new HashMap<>();
		final Map<String, TreeItem<TreeData>> InventoryLookupMap = new HashMap<>();

		public TimeLoopLocal() {
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		public int getLastCat() {
			return lastCat;
		}

		public void setLastCat(int lastCat) {
			this.lastCat = lastCat;
		}

		public int getThisCat() {
			return thisCat;
		}

		public void setThisCat(int thisCat) {
			this.thisCat = thisCat;
		}

		public void setLastLevelUp(final int idx, final TreeItem<TreeData> item) {
			lastLevelUp.put(idx, item);
		}

		public TreeItem<TreeData> getLastLevelUp() {
			return lastLevelUp.get(thisCat - 1);
		}

		public TreeItem<TreeData> getLastLevelUp(int i) {
			return lastLevelUp.get(i);
		}

		// public void setLastTime(DataItem dataItem) {
		// this.lastTime = dataItem;
		// }

		// public DataItem getLastTime() {
		// return lastTime;
		// }

		public void setRootTime(final TreeItem<TreeData> rootTime) {
			this.rootTime = rootTime;
		}

		public void setRootTimeNode(final TreeItem<TreeData> rootTimeNode) {
			this.rootTimeNode = rootTimeNode;
		}

		public TreeItem<TreeData> getRootTimeNode() {
			return rootTimeNode;
		}

		public TreeItem<TreeData> getRootTime() {
			return rootTime;
		}

		public Map<String, TreeItem<TreeData>> getActorLookupMap() {
			return actorLookupMap;
		}

		public Map<String, TreeItem<TreeData>> getInventoryLookupMap() {
			return InventoryLookupMap;
		}

	}

	private void refreshTrees() {
		// 0=none, 1=root, 2=section, 3=chapter, 4=other/item/actor/etc
		final TimeLoopLocal tll = new TimeLoopLocal();
		tll.setLastLevelUp(1, rootScenes);
		tll.setRootTime(rootTimes);

		// clear old data
		rootActors.getChildren().clear();
		rootItems.getChildren().clear();
		rootScenes.getChildren().clear();
		rootTimes.getChildren().clear();

		// Images
		final Image nodeImageChapter = new Image(getClass().getResourceAsStream("/book-icon.png"));
		final Image nodeImageSection = new Image(getClass().getResourceAsStream("/62863-books-icon.png"));
		final Image nodeImageInv = new Image(getClass().getResourceAsStream("/book-icon.png"));

		//
		final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile()).getItems();
		for (final DataItem dataItem : dataList) {
			final String cat = dataItem.getCategory();
			// boolean noMatchFilter = false;
			//
			final NestedTreeData treedata = new NestedTreeData(dataItem.getRawValue());
			final String marker = dataItem.getSubByKeyString(EchoWriteConst.WORD_MARKER);
			if (marker != null) {
				TreeTimeData ttd = new TreeTimeData(marker);
				treedata.setLastDateTime(ttd);
			}

			// TIME/SCENES/OTHER
			if (EchoWriteConst.WORD_TIME.compareTo(cat) == 0) {
				final TreeTimeData treeTimeData = new TreeTimeData(dataItem.getRawValue());
				final TreeItem<TreeData> thisElementTime = new TreeItem<>(treeTimeData);
				tll.getRootTime().getChildren().add(thisElementTime);
				tll.setRootTimeNode(thisElementTime);
			} else if (EchoWriteConst.WORD_SCENE.compareTo(cat) == 0) {
				final TreeTimeData treeTimeData = new TreeTimeData("-" + dataItem.getRawValue());
				final TreeItem<TreeData> thisElementTime = new TreeItem<>(treeTimeData);
				tll.getRootTimeNode().getChildren().add(thisElementTime);
				tll.getRootTimeNode().setExpanded(true);
			} else if (EchoWriteConst.WORD_OTHER.compareTo(cat) == 0) {
				final TreeTimeData treeTimeData = new TreeTimeData(dataItem.getName() + ": " + dataItem.getRawValue());
				final TreeItem<TreeData> thisElementTime = new TreeItem<>(treeTimeData);
				tll.getRootTimeNode().getChildren().add(thisElementTime);
				tll.getRootTimeNode().setExpanded(true);
			}

			// CHAPTER/SECTION/SCENE/OTHER
			TreeItem<TreeData> thisElement = null;
			if (EchoWriteConst.WORD_CHAPTER.compareTo(cat) == 0 && nodeImageChapter != null) {
				thisElement = new TreeItem<>(treedata, new ImageView(nodeImageChapter));
				tll.setThisCat(3);
			} else if (EchoWriteConst.WORD_SECTION.compareTo(cat) == 0 && nodeImageSection != null) {
				thisElement = new TreeItem<>(treedata, new ImageView(nodeImageSection));
				tll.setThisCat(2);
			} else if (EchoWriteConst.WORD_SCENE.compareTo(cat) == 0
					|| EchoWriteConst.WORD_SUBSCENE.compareTo(cat) == 0) {
				thisElement = new TreeItem<>(treedata);
				tll.setThisCat(4);
			} else {
				thisElement = new TreeItem<>(treedata);
				tll.setThisCat(5);
			}
			// if (EchoWriteConst.WORD_MARKER.compareTo(cat) == 0) {
			// tll.setLastTime(dataItem);
			// }

			//
			if (thisElement != null) {
				TreeItem<TreeData> parent = tll.getLastLevelUp();
				if (tll.getLastCat() == 0) {
					parent = rootScenes;
					tll.setThisCat(1);
					tll.setLastLevelUp(1, thisElement);
				}
				if (parent == null) {
					parent = tll.getLastLevelUp(1);// lastLevelUp.get(1);
				}
				parent.getChildren().add(thisElement);

				// 20200229 if (tll.getThisCat() != tll.getLastCat()) {
				tll.setLastLevelUp(tll.getThisCat(), thisElement);
				// }
				//
				tll.setLastCat(tll.getThisCat());
				thisElement.setExpanded(true);
			}

			//
			final String sMarker = dataItem.getSubByKeyString(EchoWriteConst.WORD_MARKER);
			try {
				final Integer iMarker = Integer.valueOf(sMarker);
				// LOGGER.debug("iMarker == " + sMarker);
				timeList.trackTime(iMarker);
				// if (lastTimeMarker == null)
				// lastTimeMarker = iMarker;
				// else if (lastTimeMarker < iMarker)
				// lastTimeMarker = iMarker;
			} catch (NumberFormatException e) {
				LOGGER.warn("DataItem has invalid sMarker, " + dataItem.toString());
				// e.printStackTrace();
			}

			//
			if (EchoWriteConst.WORD_ACTOR.compareTo(cat) == 0) {
				final TreeItem<TreeData> tElement = new TreeItem<>(new TreeTimeData(dataItem.getRawValue()));
				final String sName = dataItem.getSubByKeyString(EchoWriteConst.WORD_NAME);
				TreeItem<TreeData> actorNode = tll.getActorLookupMap().get(sName.trim());
				if (actorNode == null) {
					actorNode = new TreeItem<TreeData>(new TreeTimeData(sName), new ImageView(nodeImageInv));
					tll.getActorLookupMap().put(sName, actorNode);
					rootActors.getChildren().add(actorNode);
					actorNode.getChildren().add(tElement);
				} else {
					actorNode.getChildren().add(tElement);
				}
				actorNode.setExpanded(true);
			}
			//
			if (EchoWriteConst.WORD_INVENTORY.compareTo(cat) == 0) {
				final TreeItem<TreeData> tElement = new TreeItem<>(new TreeTimeData(dataItem.getRawValue()));
				final String sName = dataItem.getSubByKeyString(EchoWriteConst.WORD_NAME);
				TreeItem<TreeData> invNode = tll.getInventoryLookupMap().get(sName);
				if (invNode == null) {
					invNode = new TreeItem<TreeData>(new TreeTimeData(sName), new ImageView(nodeImageInv));
					tll.getInventoryLookupMap().put(sName, invNode);
					rootItems.getChildren().add(invNode);
					invNode.getChildren().add(tElement);
				} else {
					invNode.getChildren().add(tElement);
				}
				invNode.setExpanded(true);
			}

			//
			//
		}
	}

	class SortbyMarker implements Comparator<DataItem> {
		// Used for sorting in ascending order of marker
		public int compare(DataItem a, DataItem b) {
			final String markerA = a.getSubByKeyString(EchoWriteConst.WORD_MARKER);
			final String markerB = b.getSubByKeyString(EchoWriteConst.WORD_MARKER);

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

	// TODO
	private void refreshTableEndThings() {
		final ObservableList<DataItem> newList = FXCollections.observableArrayList();
		newList.sort(new SortbyMarker());
		parseDataFilterEndThings(newList);
		//
		dataThingsEndTable.getItems().clear();
		// updateTableThingsColumns();
		dataThingsEndTable.getItems().setAll(newList);
		dataThingsEndTable.refresh();
		// dataThingsEndTable.sort();

		//
		final List<String> filterListColumn = new ArrayList<String>();
		filterListColumn.add(EchoWriteConst.WORD_TIMEMARK);
		for (final DataItem dataItem : newList) {
			for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
				final String key = dataSubItem.getName();
				String keyT = key.trim().toLowerCase();
				keyT = keyT.replace(EchoWriteConst.DOCTAG_LIST, "").replace(EchoWriteConst.DOCTAG_NEWLINE, "");
				if (!columnNameDataMapEndThings.contains(keyT) && !filterListColumn.contains(keyT))
					columnNameDataMapEndThings.add(keyT);
			}
		}
		//
		dataThingsEndTable.refresh();
		dataThingsEndTable.sort();
	}

	private void refreshTableActors() {
		final ObservableList<DataItem> newList = FXCollections.observableArrayList();
		parseDataFilterActors(newList);
		//
		dataActorsTable.getItems().clear();
		// updateTableActorsColumns();
		dataActorsTable.getItems().setAll(newList);
		dataActorsTable.refresh();
		// dataActorsTable.sort();

		//
		final List<String> filterListColumn = new ArrayList<String>();
		filterListColumn.add(EchoWriteConst.WORD_TIMEMARK);
		for (final DataItem dataItem : newList) {
			for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
				final String key = dataSubItem.getName();
				String keyT = key.trim().toLowerCase();
				// final String cat = dataSubItem.get
				keyT = keyT.replace(EchoWriteConst.DOCTAG_LIST, "").replace(EchoWriteConst.DOCTAG_NEWLINE, "");
				if (!columnNameDataMapActors.contains(keyT) && !filterListColumn.contains(keyT))
					columnNameDataMapActors.add(keyT);
			}
		}
		//
	}

	private void refreshTableThings() {
		final ObservableList<DataItem> newList = FXCollections.observableArrayList();
		parseDataFilterThings(newList);
		//
		dataThingsTable.getItems().clear();
		// updateTableThingsColumns();
		dataThingsTable.getItems().setAll(newList);
		dataThingsTable.refresh();
		// dataThingsTable.sort();

		//
		final List<String> filterListColumn = new ArrayList<String>();
		filterListColumn.add(EchoWriteConst.WORD_TIMEMARK);
		for (final DataItem dataItem : newList) {
			for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
				final String key = dataSubItem.getName();
				String keyT = key.trim().toLowerCase();
				keyT = keyT.replace(EchoWriteConst.DOCTAG_LIST, "").replace(EchoWriteConst.DOCTAG_NEWLINE, "");
				if (!columnNameDataMapThings.contains(keyT) && !filterListColumn.contains(keyT))
					columnNameDataMapThings.add(keyT);
			}
		}
		//
	}

	private void refreshTableDataAll() {
		final ObservableList<DataItem> newList = FXCollections.observableArrayList();
		parseDataFilterAll(newList);
		//
		dataAllThingsTable.getItems().clear();
		// updateTableAllThingsColumns();
		dataAllThingsTable.getItems().setAll(newList);
		dataAllThingsTable.refresh();
		// dataAllThingsTable.sort();
		//
	}

	private void parseData() {
		LOGGER.debug("parseData: Called");
		lockGui();

		filtersEndthings.clear();
		filtersAllThings.clear();
		filtersThings.clear();
		filtersActors.clear();
		// columnNameDataMap.clear();
		// endThingsListByMarker.clear();
		// endThingsListByItem.clear();
		// actorList.clear();
		// thingList.clear();
		// lastTimeMarker = null;

		if (selectedProfile == null)
			LOGGER.error("parseData: selectedProfile NOT set");
		else {
			LOGGER.debug("parseData: selectedProfile set");
			writeToScreen("Parsing data...");
			try {
				// Clear
				dataALLList.clear();
				columnNameDataMapAll.clear();
				columnNameDataMapThings.clear();
				columnNameDataMapEndThings.clear();
				columnNameDataMapActors.clear();

				// Meta
				final List<DataItem> metadataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile())
						.getMetaItems();
				// TODO

				// Create
				final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile())
						.getItems();
				for (final DataItem dataItem : dataList) {
					dataALLList.add(dataItem);

					final List<String> filterListColumn = new ArrayList<String>();
					filterListColumn.add("timemark");
					// if (dataItem.getCategory() != EchoWriteConst.WORD_META
					// && dataItem.getCategory() != EchoWriteConst.WORD_SECTION
					// && dataItem.getCategory() != EchoWriteConst.WORD_CHAPTER)
					// {
					for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
						final String key = dataSubItem.getName();
						String keyT = key.trim().toLowerCase();
						keyT = keyT.replace(EchoWriteConst.DOCTAG_LIST, "").replace(EchoWriteConst.DOCTAG_NEWLINE, "");
						if (!columnNameDataMapAll.contains(keyT) && !filterListColumn.contains(keyT))
							columnNameDataMapAll.add(keyT);
					}

					//
					final Integer marker = dataItem.getSubByKeyInteger(EchoWriteConst.WORD_MARKER);
					timeList.trackTime(marker);
					// if (marker != null && lastTimeMarker == null)
					// lastTimeMarker = marker;
					// else if (marker != null && marker > lastTimeMarker)
					// lastTimeMarker = marker;
					// else {
					// }
				} // for

				//
				// TODO Warn if timemark is off?

				// final List<String> tagSceneTags =
				// DATA_MANAGER.getTagListScene();
				// tagSceneTags.addAll(DATA_MANAGER.getTagListSubScenes());F

				refreshTrees();
				refreshTableDataAll();
				refreshTableThings();
				refreshTableEndThings();
				refreshTableActors();
				//
				updateTableAllThingsColumns();
				updateTableThingsColumns();
				updateTableEndThingsColumns();
				updateTableActorsColumns();
				//
			} catch (Exception e) {
				writeToScreen("Error! " + e);
				e.printStackTrace();
			} finally {
				// baseCtrl.unlockGui();
			}
			writeToScreen("Data Parsed.");
		}

		// Display data
		unlockGui();
		LOGGER.debug("parseData: Done");
	}

	// private void writeCharCardFile(final Map<String, List<DataItem>>
	// actorDataMap, final List<String> listActors) {
	// BufferedWriter fWriterFile = null;
	// try {
	// final FormatDao formatDao = new FormatDao();
	// profileManager.setupDao(formatDao, selectedProfile);
	// final Charset selCharSet = formatDao.getCharSet();
	//
	// File outputFileS2 = null;
	// if (!StringUtils.isEmpty(selectedProfile.getOutputCharCardFile())) {
	// outputFileS2 = new File(selectedProfile.getOutputCharCardFile());
	// }
	// if (outputFileS2 != null) {
	// // final File outputFileS0 = new
	// // File(formatDao.getInputFilename());
	// // final File outputFileS1 = outputFileS0.getParentFile();
	// // outputFileS2 = new File(outputFileS1,
	// // formatDao.getFilePrefix() + "CharFile.txt");
	// // TODO TEMP
	// int padding = 6;
	// if (StringUtils.isBlank(selectedProfile.getTimelineTimePadding())) {
	// try {
	// padding = Integer.valueOf(padding);
	// } catch (Exception e) {
	// padding = 6;
	// e.printStackTrace();
	// }
	// }
	//
	// fWriterFile = new BufferedWriter(new
	// OutputStreamWriter(Files.newOutputStream(outputFileS2.toPath(),
	// StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
	// selCharSet));
	// for (final String valActor : listActors) {
	// if (valActor != null) {
	// fWriterFile.write("==");
	// fWriterFile.write(valActor);
	// fWriterFile.write("==");
	// fWriterFile.write("\n");
	//
	// final List<String> filterList = new ArrayList<String>();
	// filterList.add("date");
	// filterList.add("timemark");
	// filterList.add("marker");
	// filterList.add("char");
	// final List<TreeTimeSubData> listADM = actorDataMap.get(valActor);
	// if (listADM != null)
	// for (final TreeTimeSubData ttsd : listADM) {
	// String marker = ttsd.getDataByKey("marker");
	// if (marker == null)
	// marker = "n/a";
	// marker = StringUtils.leftPad(marker, padding);
	// fWriterFile.write(marker);
	// fWriterFile.write(",");
	// ttsd.setFilterList(filterList);
	// fWriterFile.write(ttsd.toSimpleString());
	// fWriterFile.write("\n");
	// ttsd.setFilterList(null);
	// }
	// }
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// if (fWriterFile != null)
	// try {
	// fWriterFile.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private boolean analyzeMarker(final String filterVal, final String dataVal, final Map<String, String> filters) {
		//
		boolean noMatchFilter = false;
		if (filterVal.startsWith(">")) {
			// filterTextMarker < and > ?? TODO
			LOGGER.debug("filterTextMarker: >");
			final String filterValS = filterVal.substring(1);
			try {
				final int filterValSI = Integer.valueOf(filterValS);
				final int dataValI = Integer.valueOf(dataVal);
				if (dataValI < filterValSI)
					noMatchFilter = true;
			} catch (final NumberFormatException e) {
				e.printStackTrace();
				filters.put(EchoWriteConst.WORD_MARKER, "");
			}
		} else if (filterVal.startsWith("<")) {
			LOGGER.debug("filterTextMarker: <");
			final String filterValS = filterVal.substring(1);
			try {
				final int filterValSI = Integer.valueOf(filterValS);
				final int dataValI = Integer.valueOf(dataVal);
				if (dataValI > filterValSI)
					noMatchFilter = true;
			} catch (final NumberFormatException e) {
				e.printStackTrace();
				filters.put(EchoWriteConst.WORD_MARKER, "");
			}
		} else if (filterVal.startsWith("=")) {
			final String filterValS = filterVal.substring(1);
			if (!dataVal.toLowerCase().contains(filterValS.toLowerCase()))
				noMatchFilter = true;
		} else {
			LOGGER.debug("filterTextMarker: <");
			final String filterValS = filterVal == null ? filterVal
					: filterVal.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
			try {
				final int filterValSI = Integer.valueOf(filterValS);
				final int dataValI = Integer.valueOf(dataVal);
				if (dataValI > filterValSI)
					noMatchFilter = true;
			} catch (final NumberFormatException e) {
				e.printStackTrace();
				filters.put(EchoWriteConst.WORD_MARKER, "");
			}
		}
		return noMatchFilter;
	}

	private void parseDataFilterEndThings(final ObservableList<DataItem> newList) {
		//
		final Map<String, DataItem> endListItem = new HashMap<>();
		final Map<String, Integer> endListMarker = new HashMap<>();

		//
		final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile()).getItems();
		for (final DataItem dataItem : dataList) {
			final String cat = dataItem.getCategory();
			boolean noMatchFilter = false;

			boolean okByCat = false;
			if (EchoWriteConst.WORD_INVENTORY.compareTo(cat) == 0 || EchoWriteConst.WORD_ITEM.compareTo(cat) == 0)
				okByCat = true;
			//
			final Integer marker = dataItem.getSubByKeyInteger(EchoWriteConst.WORD_MARKER);
			final String sName = dataItem.getSubByKeyString(EchoWriteConst.WORD_NAME);
			final String sActor = dataItem.getSubByKeyString(EchoWriteConst.WORD_ACTOR);
			String matchName = sName;
			if (!StringUtils.isEmpty(sActor))
				matchName = String.format("%s:%s", sName, sActor);
			// final Integer iCount =
			// dataItem.getSubByKeyInteger(EchoWriteConst.WORD_COUNT);

			if (okByCat) {
				//
				if (marker == null) {
					// noMatchFilter = true;
				} else {
					if (marker > timeList.getLastTimeMarker()) {
						// if (marker > lastTimeMarker) {
						noMatchFilter = true;
					}
				}

				if (!noMatchFilter) {
					//
					final Set<String> atdKeys = filtersEndthings.keySet();

					//
					for (final Iterator<String> iter = atdKeys.iterator(); iter.hasNext();) {
						final String filterKey = (String) iter.next();
						final String filterVal = filtersEndthings.get(filterKey);
						final String dataVal = dataItem.getSubByKeyString(filterKey);

						if (!StringUtils.isBlank(filterVal)) {
							if (StringUtils.isEmpty(dataVal))
								noMatchFilter = true;
							else if (filterKey.compareToIgnoreCase(EchoWriteConst.WORD_MARKER) == 0) {
								// MARKER
								noMatchFilter = analyzeMarker(filterVal, dataVal, filtersEndthings);
								// MARKER
							} else {
								if (!dataVal.toLowerCase().contains(filterVal.toLowerCase()))
									noMatchFilter = true;
							}
						} // blank filterVal
					} // for
					if (!noMatchFilter) {
						final Integer tMarker = endListMarker.get(matchName);
						// final DataItem tDataItem =
						// endListItem.get(iName);
						if (tMarker == null || tMarker < marker) {
							endListItem.put(matchName, dataItem);
							endListMarker.put(matchName, tMarker);
						}

					}
				} // noMatchFilter
			} // okByCat
		}
		// for

		//
		LOGGER.debug("Map lastTimeMarker: " + timeList.getLastTimeMarker());// lastTimeMarker);

		final Set<String> etlbmSet = endListItem.keySet();
		// final Iterator<String> etlbmIter = etlbmSet.iterator();
		for (Iterator<String> iterator = etlbmSet.iterator(); iterator.hasNext();) {
			final String etlI = iterator.next();
			LOGGER.debug("Map for marker: '" + etlI + "'");
			final DataItem etlD = endListItem.get(etlI);
			LOGGER.debug(" dataItem2: " + etlD);
			final Integer iCount = etlD.getSubByKeyInteger(EchoWriteConst.WORD_COUNT);
			if (iCount > 0) {
				newList.add(etlD);
			}
		}

		// final Set<Integer> etlbmSet = endThingsListByMarker.keySet();
		// final Iterator<Integer> etlbmIter = etlbmSet.iterator();
		// for (Iterator<Integer> iterator = etlbmSet.iterator();
		// iterator.hasNext();) {
		// final Integer etlI = iterator.next();
		// LOGGER.debug("Map for marker: " + etlI);
		// final List<DataItem> dlist = endThingsListByMarker.get(etlI);
		// for (final DataItem dataItem2 : dlist) {
		// LOGGER.debug(" dataItem2: " + dataItem2);
		// }
		// }
	}

	private void parseDataFilterActors(final ObservableList<DataItem> newList) {
		//
		final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile()).getItems();
		for (final DataItem dataItem : dataList) {
			final String cat = dataItem.getCategory();
			boolean noMatchFilter = false;

			//
			final Set<String> atdKeys = filtersActors.keySet();

			boolean okByCat = false;
			if (EchoWriteConst.WORD_ACTOR.compareTo(cat) == 0 || EchoWriteConst.WORD_CHAR.compareTo(cat) == 0)
				okByCat = true;

			if (okByCat) {
				//
				for (final Iterator<String> iter = atdKeys.iterator(); iter.hasNext();) {
					final String filterKey = (String) iter.next();
					final String filterVal = filtersActors.get(filterKey);
					final String dataVal = dataItem.getSubByKeyString(filterKey);

					if (!StringUtils.isBlank(filterVal)) {
						if (StringUtils.isEmpty(dataVal))
							noMatchFilter = true;
						else if (filterKey.compareToIgnoreCase(EchoWriteConst.WORD_MARKER) == 0) {
							// MARKER
							noMatchFilter = analyzeMarker(filterVal, dataVal, filtersActors);
							// MARKER
						} else if (!dataVal.toLowerCase().contains(filterVal.toLowerCase()))
							noMatchFilter = true;
					}
				}

				//
				if (!noMatchFilter)
					newList.add(dataItem);
			}
		}
		//
	}

	private void parseDataFilterThings(final ObservableList<DataItem> newList) {
		//
		final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile()).getItems();
		for (final DataItem dataItem : dataList) {
			final String cat = dataItem.getCategory();
			boolean noMatchFilter = false;

			//
			final Set<String> atdKeys = filtersThings.keySet();

			boolean okByCat = false;
			if (EchoWriteConst.WORD_INVENTORY.compareTo(cat) == 0 || EchoWriteConst.WORD_ITEM.compareTo(cat) == 0)
				okByCat = true;

			if (okByCat) {
				//
				for (final Iterator<String> iter = atdKeys.iterator(); iter.hasNext();) {
					final String filterKey = (String) iter.next();
					final String filterVal = filtersThings.get(filterKey);
					final String dataVal = dataItem.getSubByKeyString(filterKey);

					if (!StringUtils.isBlank(filterVal)) {
						if (StringUtils.isEmpty(dataVal))
							noMatchFilter = true;
						else if (filterKey.compareToIgnoreCase(EchoWriteConst.WORD_MARKER) == 0) {
							// MARKER
							noMatchFilter = analyzeMarker(filterVal, dataVal, filtersThings);
							// MARKER
						} else if (!dataVal.toLowerCase().contains(filterVal.toLowerCase()))
							noMatchFilter = true;
					}
				}

				//
				if (!noMatchFilter)
					newList.add(dataItem);
			}
		}
		//
	}

	private void parseDataFilterAll(final ObservableList<DataItem> newALLList) {
		//
		final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile()).getItems();
		for (final DataItem dataItem : dataList) {
			final String cat = dataItem.getCategory();
			boolean noMatchFilter = false;

			// All TABlE
			if (EchoWriteConst.WORD_CHAPTER.compareTo(cat) != 0 && EchoWriteConst.WORD_SECTION.compareTo(cat) != 0) {
				for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
					if (!columnNameDataMapAll.contains(dataSubItem.getName())) {
						columnNameDataMapAll.add(dataSubItem.getName());
					}
				}
			}

			//
			final Set<String> atdKeys = filtersAllThings.keySet();

			boolean okByCat = false;
			if (EchoWriteConst.WORD_TIME.compareTo(cat) != 0)
				// if (EchoWriteConst.WORD_TIME.compareTo(cat) != 0 &&
				// EchoWriteConst.WORD_SECTION.compareTo(cat) != 0
				// && EchoWriteConst.WORD_CHAPTER.compareTo(cat) != 0)
				okByCat = true;

			if (okByCat) {
				//
				for (final Iterator<String> iter = atdKeys.iterator(); iter.hasNext();) {
					final String filterKey = (String) iter.next();
					final String filterVal = filtersAllThings.get(filterKey);
					final String dataVal = dataItem.getSubByKeyString(filterKey);

					if (!StringUtils.isBlank(filterVal)) {
						if (StringUtils.isEmpty(dataVal))
							noMatchFilter = true;
						else if (filterKey.compareToIgnoreCase(EchoWriteConst.WORD_MARKER) == 0) {
							//
							if (filterVal.startsWith(">")) {
								// filterTextMarker < and > ?? TODO
								LOGGER.debug("filterTextMarker: >");
								final String filterValS = filterVal.substring(1);
								try {
									final int filterValSI = Integer.valueOf(filterValS);
									final int dataValI = Integer.valueOf(dataVal);
									if (dataValI < filterValSI)
										noMatchFilter = true;
								} catch (final NumberFormatException e) {
									e.printStackTrace();
									filtersAllThings.put(EchoWriteConst.WORD_MARKER, "");
								}
							} else if (filterVal.startsWith("<")) {
								LOGGER.debug("filterTextMarker: <");
								final String filterValS = filterVal.substring(1);
								try {
									final int filterValSI = Integer.valueOf(filterValS);
									final int dataValI = Integer.valueOf(dataVal);
									if (dataValI > filterValSI)
										noMatchFilter = true;
								} catch (final NumberFormatException e) {
									e.printStackTrace();
									filtersAllThings.put(EchoWriteConst.WORD_MARKER, "");
								}
							} else if (!dataVal.toLowerCase().contains(filterVal.toLowerCase()))
								noMatchFilter = true;
							//
						} else if (!dataVal.toLowerCase().contains(filterVal.toLowerCase()))
							noMatchFilter = true;
					}
				}

				//
				if (!noMatchFilter)
					newALLList.add(dataItem);
			}
		}
	}

	private void reloadData() {
		LOGGER.debug("reloadData: Called");

		if (selectedProfile == null)
			LOGGER.error("loadData: selectedProfile NOT set");
		else {
			LOGGER.debug("loadData: selectedProfile set");

			final File inputFile = new File(selectedProfile.getInputFile());
			DataManagerBiz.getDataManager(inputFile).clearDataForFile(inputFile);

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

	private void setupFilterAllThings(final String filterKey, String filterValue) {
		LOGGER.debug("setupFilter: filterKey='" + filterKey + "' filterValue='" + filterValue + "'");
		if (StringUtils.isEmpty(filterValue)) {
			filtersAllThings.remove(filterKey);
			LOGGER.debug("setupFilter: removed filterKey='" + filterKey + "'");
		} else
			filtersAllThings.put(filterKey, filterValue);
		// startTimerTask();
		final Set<String> keys = filtersAllThings.keySet();
		for (final String key : keys) {
			final String val = filtersAllThings.get(key);
			LOGGER.debug("filter: key='" + key + "' val='" + val + "'");
		}
		refreshTableDataAll();
	}

	private void setupFilterActors(final String filterKey, String filterValue) {
		LOGGER.debug("setupFilterActors: filterKey='" + filterKey + "' filterValue='" + filterValue + "'");
		if (StringUtils.isEmpty(filterValue)) {
			filtersActors.remove(filterKey);
			LOGGER.debug("setupFilterActors: removed filterKey='" + filterKey + "'");
		} else
			filtersActors.put(filterKey, filterValue);
		// startTimerTask();
		final Set<String> keys = filtersActors.keySet();
		for (final String key : keys) {
			final String val = filtersActors.get(key);
			LOGGER.debug("setupFilterActors: filter: key='" + key + "' val='" + val + "'");
		}
		refreshTableActors();
	}

	private void setupFilterThings(final String filterKey, String filterValue) {
		LOGGER.debug("setupFilterThings: filterKey='" + filterKey + "' filterValue='" + filterValue + "'");
		if (StringUtils.isEmpty(filterValue)) {
			filtersThings.remove(filterKey);
			LOGGER.debug("setupFilterThings: removed filterKey='" + filterKey + "'");
		} else
			filtersThings.put(filterKey, filterValue);
		// startTimerTask();
		final Set<String> keys = filtersThings.keySet();
		for (final String key : keys) {
			final String val = filtersThings.get(key);
			LOGGER.debug("setupFilterThings: filter: key='" + key + "' val='" + val + "'");
		}
		refreshTableThings();
	}

	private void setupFilterEndThings(final String filterKey, String filterValue) {
		LOGGER.debug("setupFilter: filterKey='" + filterKey + "' filterValue='" + filterValue + "'");
		if (StringUtils.isEmpty(filterValue)) {
			filtersEndthings.remove(filterKey);
			LOGGER.debug("setupFilter: removed filterKey='" + filterKey + "'");
		} else
			filtersEndthings.put(filterKey, filterValue);
		// startTimerTask();
		final Set<String> keys = filtersEndthings.keySet();
		for (final String key : keys) {
			final String val = filtersEndthings.get(key);
			LOGGER.debug("filter: key='" + key + "' val='" + val + "'");
		}
		refreshTableEndThings();
	}

	private void writeToScreen(final String msg) {
		writeToScreen(msg, true);
	}

	private void writeToScreen(final String msg, boolean incTime) {
		final StringBuilder sbuf = new StringBuilder();
		if (incTime) {
			sbuf.append(EchoWriteConst.myLogDateFormat.format(myLogDateCalendar.getTime()));
			sbuf.append(": ");
		}
		sbuf.append(msg);
		if (!msg.endsWith("\n"))
			sbuf.append("\n");
		loggingArea.insertText(0, sbuf.toString());
	}

	@Override
	public void doCleanup() {
		LOGGER.info("Ctrl is cleaning up...");

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

}
