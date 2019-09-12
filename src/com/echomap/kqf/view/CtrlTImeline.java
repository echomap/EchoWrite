package com.echomap.kqf.view;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.looper.WorkDoneNotify;
import com.echomap.kqf.looper.data.TreeData;
import com.echomap.kqf.looper.data.TreeTimeData;
import com.echomap.kqf.looper.data.TreeTimeSubData;
import com.echomap.kqf.looper.data.TreeTimeSubDataNoCharDisplay;
import com.echomap.kqf.looper.data.TreeTimeSubDataNoTimeDisplay;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CtrlTImeline extends BaseCtrl implements Initializable, WorkFinishedCallback {

	private final static Logger LOGGER = LogManager.getLogger(CtrlTImeline.class);
	static public final DateFormat myLogDateFormat = new SimpleDateFormat("HH:mm:ss:ms");

	@FXML
	private TabPane mainTabPane;
	@FXML
	private VBox paneButtons;
	@FXML
	private Button buttonRefresh;
	@FXML
	private TreeView<TreeData> dataTimeTree;// <T>
	@FXML
	private TreeView<TreeData> dataActorTree;// <T>
	@FXML
	private TreeView<TreeData> dataItemTree;// <T>
	@FXML
	private TreeView<TreeData> dataEndThingsTree;// <T>
	@SuppressWarnings("rawtypes")
	@FXML
	private TableView<TreeTimeSubData> dataThingsEndTable;// <T>

	@FXML
	private TextArea loggingArea;

	//
	private Profile selectedProfile = null;

	//
	private ProfileManager profileManager = null;
	private Calendar myLogDateCalendar = null;
	private TreeItem<TreeData> rootTime = null;
	private TreeItem<TreeData> rootActors = null;
	private TreeItem<TreeData> rootThings = null;
	private TreeItem<TreeData> rootEndThings = null;

	private MyWorkDoneNotify myWorkDoneNotify = null;
	private WorkDoneNotify processDoneNotify = null;

	private List<DocTag> metaDocTagList = null;

	@Override
	public void workFinished(final String msg) {
		// this.unlockGui(msg);
		LOGGER.warn("workFinished w/msg = " + msg);
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
			final StringBuilder sbuf = new StringBuilder();
			sbuf.append("Time units:");
			final Map<String, Object> mapp = (Map<String, Object>) payload;

			@SuppressWarnings("unchecked")
			final List<TreeTimeData> datalistTimeDate = (List<TreeTimeData>) mapp.get("datalistTimeDate");
			for (final TreeTimeData treeTimeData : datalistTimeDate) {
				sbuf.append(",");
				sbuf.append(treeTimeData.toString());
			}
			sbuf.append(".");
			writeToScreen(sbuf.toString());
			sbuf.setLength(0);

			//
			final List<String> listActors = new ArrayList<>();
			final Map<String, List<TreeTimeSubData>> actorDataMap = new TreeMap<>();
			//
			final List<String> listThings = new ArrayList<>();
			final Map<String, List<TreeTimeSubData>> thingsDataMap = new TreeMap<>();
			//
			final Map<String, Map<String, TreeTimeSubData>> actorThingsDataMap = new TreeMap<>();

			// sbuf.append("Data units:");
			datalistTimeDate.stream().forEach((treeTimeData) -> {
				final TreeItem<TreeData> lastElementTime = new TreeItem<>(treeTimeData);
				// if (lastElementTime != null && lastElementTime.getValue() !=
				// null
				// && lastElementTime.getChildren() != null)
				rootTime.getChildren().add(lastElementTime);

				// Loop through data (1)
				final List<String> data = treeTimeData.getData();
				for (final String ttData : data) {
					final TreeTimeSubData ttsd = new TreeTimeSubData();
					ttsd.addData("timemark", parseTimeDate(ttsd, treeTimeData.getTag()));
					// ttsd.addData("chapter",
					// treeTimeData.getChapterNumber());// TODO
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
					} // if

					// add to tree item
					addToTreeItem(ttsd, lastElementTime, sbuf, listActors, actorDataMap, listThings, thingsDataMap,
							actorThingsDataMap);

					writeToScreen(sbuf.toString());
					sbuf.setLength(0);
				} // for DATA

				//
				lastElementTime.setExpanded(true);

			});// datalistTimeDate.stream()

			// Loop and create tree items
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
				final TreeItem<TreeData> lastElementThing = findTreeItem(valThing, rootThings);
				rootThings.getChildren().add(lastElementThing);

				final List<TreeTimeSubData> listADM = thingsDataMap.get(valThing);
				for (final TreeTimeSubData ttsd : listADM) {
					lastElementThing.getChildren().add(new TreeItem<>(ttsd));
				}
				lastElementThing.setExpanded(true);
			}

			LOGGER.debug("loadTableData: Called");
			final ObservableList<TreeTimeSubData> newList = FXCollections.observableArrayList();
			dataThingsEndTable.getItems().clear();
			// inputTable.getItems().setAll(newList);

			//
			// Map<String, TreeTimeSubData>
			final Set<String> atdKeys = actorThingsDataMap.keySet();
			for (final Iterator<String> iter = atdKeys.iterator(); iter.hasNext();) {
				final String actorKey = (String) iter.next();

				final TreeItem<TreeData> lastElementThing = findTreeItem(actorKey, rootEndThings);
				rootEndThings.getChildren().add(lastElementThing);

				//
				final Map<String, TreeTimeSubData> listX = actorThingsDataMap.get(actorKey);
				final Map<String, TreeTimeSubData> listS = sortByValues(listX);
				//
				final Set<String> atdKeysX = listS.keySet();
				// Collections.sort(atdKeysX, new SortbyMarker());
				for (final Iterator<String> iterX = atdKeysX.iterator(); iterX.hasNext();) {
					final String thingKey = (String) iterX.next();
					final TreeTimeSubData ttsd = listX.get(thingKey);
					//
					if (ttsd == null) {
						LOGGER.error("Got back a null value from list for '" + thingKey + "'!");
						final TreeTimeSubData ttsd2 = listX.get(thingKey);
						continue;
					}
					final String count = ttsd.getData().get("count");
					final Integer countI = count == null ? 0 : Integer.valueOf(count);
					if (countI > 0) {
						final TreeTimeSubDataNoCharDisplay ttsdS = new TreeTimeSubDataNoCharDisplay(ttsd);
						lastElementThing.getChildren().add(new TreeItem<>(ttsdS));
						// lastElementThing.getChildren().add(new
						// TreeItem<>(ttsd));

						// TODO add to table
						newList.add(ttsd);
					} else {
						LOGGER.debug("Element has zero count");
					}
				}
				lastElementThing.setExpanded(true);

				//
				dataThingsEndTable.getItems().setAll(newList);
				dataThingsEndTable.refresh();
			} // FOR

			//
		} else {
			writeToScreen("No data to write to log");
		}
		unlockGui();
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

	private TreeItem<TreeData> findTreeItem(String valThing, final TreeItem<TreeData> rootItem) {
		final ObservableList<TreeItem<TreeData>> items = rootItem.getChildren();
		for (TreeItem<TreeData> treeItem : items) {
			final TreeData treeData = treeItem.getValue();
			final String treeDataS = treeData.toString();
			if (treeDataS.compareToIgnoreCase(valThing) == 0) {
				return treeItem;
			}
		}

		final TreeItem<TreeData> lastElementThing = new TreeItem<>(new TreeTimeData(valThing));
		return lastElementThing;
	}

	private String parseTimeDate(final TreeTimeSubData ttsd, final String tag) {
		// TODO
		// date: 1. day: 1. time: 1100. marker: 1
		ttsd.addData("timemark", tag);
		parseNameValue(tag.trim(), ttsd);
		return tag;
	}

	private void addToTreeItem(final TreeTimeSubData ttsd, final TreeItem<TreeData> lastElementTime,
			final StringBuilder sbuf, final List<String> listActors,
			final Map<String, List<TreeTimeSubData>> actorDataMap, final List<String> listThings,
			final Map<String, List<TreeTimeSubData>> thingsDataMap,
			final Map<String, Map<String, TreeTimeSubData>> actorThingsDataMap) {
		if (ttsd != null && !ttsd.isEmpty()) {
			final TreeTimeSubDataNoTimeDisplay ttsdS = new TreeTimeSubDataNoTimeDisplay(ttsd);
			lastElementTime.getChildren().add(new TreeItem<>(ttsdS));
			sbuf.append("ttsd=" + ttsd.toSimpleString());

			//
			String valActor = null;
			if (ttsd.getData().containsKey("char")) {
				valActor = ttsd.getData().get("char");
				valActor = valActor.trim();
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
			String valThing = null;
			if (ttsd.getData().containsKey("item")) {
				valThing = ttsd.getData().get("item");
			}
			if (ttsd.getData().containsKey("inv")) {
				valThing = ttsd.getData().get("inv");
			}
			if (!StringUtils.isEmpty(valThing)) {
				valThing = valThing.trim();
				// Things
				if (!listThings.contains(valThing)) {
					listThings.add(valThing);
				}
				List<TreeTimeSubData> listADM = thingsDataMap.get(valThing);
				if (listADM == null) {
					listADM = new ArrayList<>();
					thingsDataMap.put(valThing, listADM);
				}
				listADM.add(ttsd);

				// Things2Actors
				if (!StringUtils.isEmpty(valActor)) {
					Map<String, TreeTimeSubData> listATDM = actorThingsDataMap.get(valActor);
					if (listATDM == null) {
						listATDM = new TreeMap<String, TreeTimeSubData>();
						// new TreeMap<String, TreeTimeSubData>(new
						// Comparator<String>() {
						//
						// @Override
						// public int compare(String o1, String o2) {
						// return o2.compareTo(o1);
						// }
						// });
						actorThingsDataMap.put(valActor, listATDM);
					}
					// find the item
					TreeTimeSubData foundThing = listATDM.get(valThing);
					if (foundThing == null) {
						listATDM.put(valThing, ttsd);
						foundThing = ttsd;
					}
					// check a later time
					final String marker = ttsd.getData().get("marker");
					// final String count = ttsd.getData().get("count");

					final String markerN = foundThing.getData().get("marker");
					final String countN = foundThing.getData().get("count");

					final Integer markerI = marker == null ? null : Integer.valueOf(marker);
					final Integer markerNI = markerN == null ? null : Integer.valueOf(markerN);
					// final Integer countI = Integer.valueOf(count);
					final Integer countNI = Integer.valueOf(countN);

					// fix the counts
					if (markerI != null && markerNI != null && markerI <= markerNI) {
						LOGGER.debug("Older data, ignored");
					} else {
						// foundThing.getData().put("count",
						// String.valueOf(countN));
						foundThing = ttsd;
						listATDM.put(valThing, foundThing);
						// if (countNI == 0) {
						// // foundThing.getData().put("count", "0");
						// // listATDM.remove(valThing);
						// } else {
						// // int countNew = Integer.valueOf(countN);
						// foundThing.getData().put("count",
						// String.valueOf(countN));
						// // if (countNew < 1) {
						// // foundThing.getData().put("count", "0");
						// // listATDM.remove(valThing);
						// // }
						// }
					}
					actorThingsDataMap.put(valActor, listATDM);
					// Things2Actors
				}
			}
		} else {
			sbuf.append("Data is empty");
		}
	}

	// Generic function to sort Map by values using TreeMap
	// public static <K, V> Map<K, V> sortByValues(Map<K, V> map) {
	// Comparator<K> comparator = new CustomComparator(map);
	//
	// Map<K, V> sortedMap = new TreeMap<>(comparator);
	// sortedMap.putAll(map);
	//
	// return sortedMap;
	// }

	// Specifc function to sort Map by values using TreeMap
	private Map<String, TreeTimeSubData> sortMapByValues(final Map<String, TreeTimeSubData> listATDM,
			final String sortByType) {
		//
		// // final Comparator<K> comparator = new CustomComparator(listATDM,
		// // sortByType);
		// // TODO check sortByType for "marker"
		Comparator<Entry<String, TreeTimeSubData>> valueComparator = new Comparator<Entry<String, TreeTimeSubData>>() {

			@Override
			public int compare(Entry<String, TreeTimeSubData> e1, Entry<String, TreeTimeSubData> e2) {
				TreeTimeSubData v1 = e1.getValue();
				TreeTimeSubData v2 = e2.getValue();
				// return e1.getKey().compareTo(e2.getKey());

				// return v1.compareTo(v2);

				// final TreeTimeSubData o2t = (TreeTimeSubData) o2;

				final String markerA = v1.getData().get("marker");
				final String markerB = v2.getData().get("marker");

				final Integer markerAI = markerA == null ? null : Integer.valueOf(markerA);
				final Integer markerBI = markerB == null ? null : Integer.valueOf(markerB);

				if ((markerAI == null && markerBI == null) || markerAI == markerBI)
					return 0;
				if (markerAI != null && markerBI != null && markerAI < markerBI)
					return -1;
				return markerAI.compareTo(markerBI);

				// return v1.compareTo(v2, "marker");
			}
		};

		//
		//
		// Set<Entry<String, String>> entries = listATDM.entrySet();
		// List<Entry<String, String>> listOfEntries = new
		// ArrayList<Entry<String, String>>(entries);
		//
		// Collections.sort(listATDM, valueComparator);
		//
		// // final Comparator comparator = valueComparator;
		//// final Map<String, TreeTimeSubData> sortedMap = new TreeMap<String,
		// TreeTimeSubData>
		//// (valueComparator);
		//// sortedMap.putAll(listATDM);
		// return sortedMap;

		// final Set<String> keySet = listATDM.keySet();
		// for (final String key : keySet) {
		// final TreeTimeSubData ttsd = listATDM.get(key);
		// }

		return listATDM;
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
			if (markerAI == null)
				return ttsd1.getData().get("item").compareTo(ttsd2.getData().get("item"));
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
	class GenericComparator<K, V extends Comparable> implements Comparator<K> {
		private Map<K, V> map;

		public GenericComparator(Map<K, V> map) {
			this.map = new HashMap<>(map);
		}

		@Override
		public int compare(K s1, K s2) {
			return map.get(s1).compareTo(map.get(s2));
		}
	}

	// Generic function to sort Map by values using TreeMap
	public <K, V> Map<K, V> sortByValuesGeneric(final Map<K, V> map) {
		final Comparator<K> comparator = new GenericComparator(map);
		final Map<K, V> sortedMap = new TreeMap<>(comparator);
		sortedMap.putAll(map);
		return sortedMap;
	}

	// Custom function to sort Map by values using TreeMap
	public Map<String, TreeTimeSubData> sortByValues(final Map<String, TreeTimeSubData> mapp) {
		final Comparator comparator = new CustomComparator(mapp);
		final TreeMap<String, TreeTimeSubData> sortedMap = new TreeMap<>(comparator);
		sortedMap.putAll(mapp);
		return sortedMap;
	}

	private void parseNameValue(final String line, final TreeTimeSubData ttsd) {
		String mKey = null;
		String mValue = null;

		// Create a Pattern object
		// -=\s+(?<sname>Section)\s+((?<snum>\w+):\s+)?(?<stitle>.*)\s+=-
		final Pattern r2 = Pattern.compile("@(?<mKey>\\w+):(?<mValue>.*)");
		// Now create matcher object.
		final Matcher matcher2 = r2.matcher(line);
		boolean foundMatch = false;
		if (matcher2.find()) {
			try {
				mKey = matcher2.group("mKey");
			} catch (Exception e) {
				LOGGER.error("mKey in text not found", e);
				e.printStackTrace();
			}
			try {
				mValue = matcher2.group("mValue");
			} catch (Exception e) {
				LOGGER.error("mValue in text not found", e);
				e.printStackTrace();
			}
			ttsd.addData(mKey.trim(), mValue.trim());
			foundMatch = true;
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile("@(?<mKey>\\w+)=(?<mValue>.*)");
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
				ttsd.addData(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			// final Pattern r =
			// Pattern.compile("((?<mKey>\\w+):\\s+(?<mValue>.*)[\\.])*+");
			final Pattern r = Pattern.compile("(?:(?<mKey>\\w*):\\s+(?<mValue>\\d*)(?=\\.|$))");
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
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
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				ttsd.addData(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
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

		createRoots();

		// loadData();
		LOGGER.debug("initialize: Done");
	}

	private void createRoots() {
		myLogDateCalendar = Calendar.getInstance();
		myLogDateFormat.setTimeZone(myLogDateCalendar.getTimeZone());

		createTabs();
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

		// Tree Setup: Ending Active
		final TreeTimeData rte4 = new TreeTimeData("End Things");
		rootEndThings = new TreeItem<TreeData>(rte4);
		dataEndThingsTree.setRoot(rootEndThings);
		rootEndThings.setExpanded(true);

		//
		createTable();
	}

	private void createTabs() {

		final List<String> tablist = new ArrayList<>();
		final ObservableList<Tab> tabs = mainTabPane.getTabs();
		for (final Tab tab : tabs) {
			tablist.add(tab.getText());
		}
		if (!tablist.contains("Time/Date"))
			tabs.add(new Tab("Time/Date"));
		if (!tablist.contains("Actors"))
			tabs.add(new Tab("Actors"));
		if (!tablist.contains("Things"))
			tabs.add(new Tab("Things"));
		if (!tablist.contains("Things/End")) {
			final TreeView<TreeData> tv = createTreeViewTab(tabs, "Things/End");
			dataEndThingsTree = tv;
		}
	}

	// dataThingsEndTable
	@SuppressWarnings("unchecked")
	private void createTable() {
		final ObservableList<TableColumn<TreeTimeSubData, ?>> columns = dataThingsEndTable.getColumns();
		columns.clear();
		//
		final TableColumn<TreeTimeSubData, String> charCol = new TableColumn<TreeTimeSubData, String>("Char");
		final TableColumn<TreeTimeSubData, String> itemCol = new TableColumn<TreeTimeSubData, String>("Item");
		final TableColumn<TreeTimeSubData, Integer> markerCol = new TableColumn<TreeTimeSubData, Integer>("Marker");
		final TableColumn<TreeTimeSubData, Integer> countCol = new TableColumn<TreeTimeSubData, Integer>("#");
		final TableColumn<TreeTimeSubData, String> locCol = new TableColumn<TreeTimeSubData, String>("Loc");
		final TableColumn<TreeTimeSubData, String> textCol = new TableColumn<TreeTimeSubData, String>("Text");
		dataThingsEndTable.getColumns().addAll(charCol, markerCol, countCol, itemCol, locCol, textCol);

		//
		charCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().get("char")));
		itemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().get("item")));
		markerCol.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getDataIntByKey("marker")).asObject());
		countCol.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getDataIntByKey("count")).asObject());
		locCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().get("loc")));
		textCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().get("text")));

		// col1. resize handler
		itemCol.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// columnWidthChanged(COL_KEY, newValue);
			}
		});

		// Hack: align column headers to the center.
		BaseCtrl.alignColumnLabelsLeftHack(dataThingsEndTable);

		dataThingsEndTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		BaseCtrl.autoFitTable(dataThingsEndTable);
		dataThingsEndTable.refresh();
	}

	private TreeView<TreeData> createTreeViewTab(final ObservableList<Tab> tabs, final String text) {
		final Tab tab = new Tab(text);
		final Node node = tab.getContent();
		final AnchorPane apan = new AnchorPane();
		//
		final TreeView<TreeData> tv = new TreeView<>();
		apan.getChildren().add(tv);
		apan.setTopAnchor(tv, 0.0);
		apan.setLeftAnchor(tv, 0.0);
		apan.setRightAnchor(tv, 0.0);
		apan.setBottomAnchor(tv, 0.0);
		//
		tab.setContent(apan);
		//
		// dataTree2 = tv;
		tabs.add(tab);
		return tv;
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
		writeToScreen("Data Loaded.");
		// Display data
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
		if (processDoneNotify != null)
			processDoneNotify.finalResultFromWork("CtrlTImeline");
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
			loadData();
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
