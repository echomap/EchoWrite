package com.echomap.kqf.view.ctrl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.echomap.kqf.view.Base;
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
	private TreeView<TreeData> scenesTree;
	@FXML
	private TreeView<TreeData> dataActorTree;// <T>
	@FXML
	private TreeView<TreeData> dataItemTree;// <T>
	@FXML
	private TableView<DataItem> dataThingsEndTable;// <T>
	@FXML
	private TableView<DataItem> dataAllThingsTable;// <T>
	@FXML
	private TableView<DataItem> dataThingsTable;// <T>

	@FXML
	private HBox allTabFilterHeader;
	@FXML
	private HBox thingsTabFilterHeader;
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

	//
	final List<String> columnNameDataMapAll = new ArrayList<>();
	final List<String> columnNameDataMapThings = new ArrayList<>();
	final List<String> columnNameDataMapEndThings = new ArrayList<>();

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
	Integer lastTimeMarker = null;

	//
	final ObservableList<DataItem> dataALLList = FXCollections.observableArrayList();

	public CtrlTImeline() {

	}

	/**
	 * 
	 */
	@Override
	protected String worktype() {
		return Base.WINDOWKEY_TIMELINE;
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

	private void createRoots() {
		myLogDateCalendar = Calendar.getInstance();
		myLogDateFormat.setTimeZone(myLogDateCalendar.getTimeZone());

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
		final MenuItem menuItemDumpItemTextA = new MenuItem("Dump Item Text");
		menuItemDumpItemTextA.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				LOGGER.debug("handle item called");
				final DataItem treeSel = dataThingsEndTable.getSelectionModel().getSelectedItem();
				if (treeSel != null) {
					writeToScreen("Output: " + treeSel);
				}
			}
		});
		//
		final ContextMenu treeContextMenuA = new ContextMenu();
		treeContextMenuA.getItems().add(menuItemDumpItemTextA);
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
			ContextMenu treeContextMenu = new ContextMenu();
			// treeContextMenu.getItems().add(item1);
			treeContextMenu.getItems().add(itemH1);
			treeContextMenu.getItems().add(itemH2);
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
		if (columnNames.contains("marker")) {
			hasmarker = true;
		}
		//
		int idxM = -1;
		for (final String str : columnNames) {
			if (str.compareToIgnoreCase("time") == 0 || str.compareToIgnoreCase("day") == 0
					|| str.compareToIgnoreCase("date") == 0) {
				columnNamesReorder2.add(str);
			} else if (str.compareToIgnoreCase("marker") == 0) {
				columnNamesReorder1.add(0, str);
				idxM = 0;
			} else if (str.compareToIgnoreCase("name") == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase("char") == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase("count") == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase("id") == 0) {
				columnNamesReorder2.add(str);
			} else {
				columnNamesReorder1.add(str);
			}
		}
		//
		columnNames.clear();
		if (hasmarker) {
			columnNames.addAll(columnNamesReorder1);
			columnNames.addAll(columnNamesReorder2);
		} else {
			columnNames.addAll(columnNamesReorder2);
			columnNames.addAll(columnNamesReorder1);
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
			final TextField tf = new TextField();
			tf.setPromptText(tableColumnName);
			endThingsTabFilterHeader.getChildren().add(tf);

			tf.textProperty().addListener((observable, oldValue, newValue) -> {
				LOGGER.debug("filterText changed from '" + oldValue + "' to '" + newValue + "'");
				setupFilterEndThings(tableColumnName, newValue);
			});
		}
		//
		endtableScrollPane.requestLayout();
	}

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
		final List<String> columnNamesReorder1 = new ArrayList<>();
		final List<String> columnNamesReorder2 = new ArrayList<>();
		if (columnNameDataMapAll.contains("marker")) {
			hasmarker = true;
		}
		//
		int idxM = -1;
		for (final String str : columnNameDataMapAll) {
			if (str.compareToIgnoreCase("time") == 0 || str.compareToIgnoreCase("day") == 0
					|| str.compareToIgnoreCase("date") == 0) {
				columnNamesReorder2.add(str);
			} else if (str.compareToIgnoreCase("marker") == 0) {
				columnNamesReorder1.add(0, str);
				idxM = 0;
			} else if (str.compareToIgnoreCase("name") == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase("char") == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase("count") == 0) {
				columnNamesReorder1.add(idxM + 1, str);
			} else if (str.compareToIgnoreCase("id") == 0) {
				columnNamesReorder2.add(str);
			} else {
				columnNamesReorder1.add(str);
			}
		}
		//
		columnNameDataMapAll.clear();
		if (hasmarker) {
			columnNameDataMapAll.addAll(columnNamesReorder1);
			columnNameDataMapAll.addAll(columnNamesReorder2);
		} else {
			columnNameDataMapAll.addAll(columnNamesReorder2);
			columnNameDataMapAll.addAll(columnNamesReorder1);
		}

		@SuppressWarnings("unchecked")
		final TableColumn<DataItem, ?>[] tableColumnList = new TableColumn[columnNameDataMapAll.size()];
		for (int i = 0; i < tableColumnList.length; i++) {
			final String tableColumnName = columnNameDataMapAll.get(i);
			final TableColumn<DataItem, ?> thisCol = new TableColumn<DataItem, Object>(tableColumnName);
			tableColumnList[i] = (thisCol);
			if (tableColumnName.compareTo("marker") == 0) {
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
		// tll.setRootTimeNode(null);
		//
		final Image nodeImageChapter = new Image(getClass().getResourceAsStream("/book-icon.png"));
		final Image nodeImageSection = new Image(getClass().getResourceAsStream("/62863-books-icon.png"));
		final Image nodeImageInv = new Image(getClass().getResourceAsStream("/book-icon.png"));

		//
		// final Set<String> atdKeys = filtersAllThings.keySet();

		//
		final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile()).getItems();
		for (final DataItem dataItem : dataList) {
			final String cat = dataItem.getCategory();
			// boolean noMatchFilter = false;
			//
			final NestedTreeData treedata = new NestedTreeData(dataItem.getRawValue());

			// TIME/SCENES
			if (EchoWriteConst.WORD_TIME.compareTo(cat) == 0) {
				final TreeTimeData treeTimeData = new TreeTimeData(dataItem.getRawValue());
				final TreeItem<TreeData> thisElementTime = new TreeItem<>(treeTimeData);
				tll.getRootTime().getChildren().add(thisElementTime);
				tll.setRootTimeNode(thisElementTime);
			} else if (EchoWriteConst.WORD_SCENE.compareTo(cat) == 0) {
				final TreeTimeData treeTimeData = new TreeTimeData(dataItem.getRawValue());
				final TreeItem<TreeData> thisElementTime = new TreeItem<>(treeTimeData);
				tll.getRootTimeNode().getChildren().add(thisElementTime);
				tll.getRootTimeNode().setExpanded(true);
			}

			//
			TreeItem<TreeData> thisElement;
			if (EchoWriteConst.WORD_CHAPTER.compareTo(cat) == 0 && nodeImageChapter != null) {
				thisElement = new TreeItem<>(treedata, new ImageView(nodeImageChapter));
				tll.setThisCat(3);
			} else if (EchoWriteConst.WORD_SECTION.compareTo(cat) == 0 && nodeImageSection != null) {
				thisElement = new TreeItem<>(treedata, new ImageView(nodeImageSection));
				tll.setThisCat(2);
			} else {
				thisElement = new TreeItem<>(treedata);
				tll.setThisCat(4);
			}
			// if (EchoWriteConst.WORD_MARKER.compareTo(cat) == 0) {
			// tll.setLastTime(dataItem);
			// }

			//
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

			if (tll.getThisCat() != tll.getLastCat()) {
				tll.setLastLevelUp(tll.getThisCat(), thisElement);
			}
			//
			tll.setLastCat(tll.getThisCat());
			thisElement.setExpanded(true);

			//
			final String sMarker = dataItem.getSubByKeyString(EchoWriteConst.WORD_MARKER);
			try {
				final Integer iMarker = Integer.valueOf(sMarker);
				// LOGGER.debug("iMarker == " + sMarker);
				if (lastTimeMarker == null)
					lastTimeMarker = iMarker;
				else if (lastTimeMarker < iMarker)
					lastTimeMarker = iMarker;
			} catch (NumberFormatException e) {
				LOGGER.warn("DataItem has invalid sMarker, " + dataItem.toString());
				// e.printStackTrace();
			}

			//
			if (EchoWriteConst.WORD_ACTOR.compareTo(cat) == 0) {
				final TreeItem<TreeData> tElement = new TreeItem<>(new TreeTimeData(dataItem.getRawValue()));
				final String sName = dataItem.getSubByKeyString(EchoWriteConst.WORD_NAME);
				TreeItem<TreeData> actorNode = tll.getActorLookupMap().get(sName);
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
			final String markerA = a.getSubByKeyString("marker");
			final String markerB = b.getSubByKeyString("marker");

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
		filterListColumn.add("timemark");
		for (final DataItem dataItem : newList) {
			for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
				final String key = dataSubItem.getName();
				String keyT = key.trim().toLowerCase();
				keyT = keyT.replace("(+u)", "").replace("(+n)", "");
				if (!columnNameDataMapEndThings.contains(keyT) && !filterListColumn.contains(keyT))
					columnNameDataMapEndThings.add(keyT);
			}
		}
		//
		dataThingsEndTable.refresh();
		dataThingsEndTable.sort();
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
		filterListColumn.add("timemark");
		for (final DataItem dataItem : newList) {
			for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
				final String key = dataSubItem.getName();
				String keyT = key.trim().toLowerCase();
				keyT = keyT.replace("(+u)", "").replace("(+n)", "");
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

		// filtersEndthings.clear();
		// filtersAllThings.clear();
		// filtersThings.clear();
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

				// Create
				final List<DataItem> dataList = DataManagerBiz.getDataManager(selectedProfile.getInputFile())
						.getItems();
				for (final DataItem dataItem : dataList) {
					dataALLList.add(dataItem);

					final List<String> filterListColumn = new ArrayList<String>();
					filterListColumn.add("timemark");

					for (final DataSubItem dataSubItem : dataItem.getDataSubItems()) {
						final String key = dataSubItem.getName();
						String keyT = key.trim().toLowerCase();
						keyT = keyT.replace("(+u)", "").replace("(+n)", "");
						if (!columnNameDataMapAll.contains(keyT) && !filterListColumn.contains(keyT))
							columnNameDataMapAll.add(keyT);
					}

					//
					final Integer marker = dataItem.getSubByKeyInteger(EchoWriteConst.WORD_MARKER);
					if (marker != null && lastTimeMarker == null)
						lastTimeMarker = marker;
					else if (marker != null && marker > lastTimeMarker)
						lastTimeMarker = marker;
					else {
					}
				} // for

				//
				// final List<String> tagSceneTags =
				// DATA_MANAGER.getTagListScene();
				// tagSceneTags.addAll(DATA_MANAGER.getTagListSubScenes());F

				refreshTrees();
				refreshTableDataAll();
				refreshTableThings();
				refreshTableEndThings();
				//
				updateTableAllThingsColumns();
				updateTableThingsColumns();
				updateTableEndThingsColumns();
				//
			} catch (Exception e) {
				writeToScreen("Error! " + e);
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
			final String filterValS = filterVal;
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
			final String iName = dataItem.getSubByKeyString(EchoWriteConst.WORD_NAME);
			// final Integer iCount =
			// dataItem.getSubByKeyInteger(EchoWriteConst.WORD_COUNT);

			if (okByCat) {
				//
				if (marker == null) {
					// noMatchFilter = true;
				} else {
					if (marker > lastTimeMarker) {
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
						final Integer tMarker = endListMarker.get(iName);
						// final DataItem tDataItem =
						// endListItem.get(iName);
						if (tMarker == null || tMarker < marker) {
							endListItem.put(iName, dataItem);
							endListMarker.put(iName, tMarker);
						}

					}
				} // noMatchFilter
			} // okByCat
		}
		// for

		//
		LOGGER.debug("Map lastTimeMarker: " + lastTimeMarker);

		final Set<String> etlbmSet = endListItem.keySet();
		final Iterator<String> etlbmIter = etlbmSet.iterator();
		for (Iterator<String> iterator = etlbmSet.iterator(); iterator.hasNext();) {
			final String etlI = iterator.next();
			LOGGER.debug("Map for marker: " + etlI);
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
			if (EchoWriteConst.WORD_TIME.compareTo(cat) != 0 && EchoWriteConst.WORD_SECTION.compareTo(cat) != 0
					&& EchoWriteConst.WORD_CHAPTER.compareTo(cat) != 0)
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
