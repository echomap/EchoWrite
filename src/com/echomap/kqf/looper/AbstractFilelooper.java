package com.echomap.kqf.looper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.biz.TextParsingBiz;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.TreeTimeData;
import com.echomap.kqf.data.TreeTimeSubData;
import com.echomap.kqf.datamgr.DataManagerBiz;
import com.echomap.kqf.looper.data.NestedTreeData;

public class AbstractFilelooper {
	private final static Logger LOGGER = LogManager.getLogger(AbstractFilelooper.class);

	final DataManagerBiz DATA_MANAGER;
	final File inputFile;

	ResourceBundle messageBundle = null;
	final Locale sLocal = new Locale("en", "US");// TODO
	// private final static DataManagerBiz DATA_MANAGER =
	// DataManagerBiz.getDataManager();

	// that last time...
	protected String lastDateTime = null;
	// that last
	protected NestedTreeData lastSection = null;
	// that last
	protected NestedTreeData lastChapter = null;
	// scene tracker
	protected Integer sceneNumber = 1;

	// For errors
	protected final List<String> errorTagList = new ArrayList<>();

	/**
	 * 
	 */
	public AbstractFilelooper(final File inputFile) {
		this.messageBundle = ResourceBundle.getBundle("cwc2messages", sLocal);
		this.inputFile = inputFile;
		this.sceneNumber = 0;
		DATA_MANAGER = DataManagerBiz.getDataManager(inputFile);
	}

	String getLocalizedText(final String key, final String defaultText) {
		if (!messageBundle.containsKey(key)) {
			return defaultText;
		}
		return messageBundle.getString(key);
	}

	protected TreeTimeData findTreeTimeData(final String dateTime) {
		return findTreeTimeData(dateTime, false);
	}

	protected TreeTimeData findTreeTimeData(final String dateTime, final Boolean addtime) {
		TreeTimeData found = null;
		if (dateTime == null) {
			found = new TreeTimeData("marker: 0");
			final TreeTimeData found2 = DATA_MANAGER.findTimeDate(found.getTag());
			if (found2 == null)
				DATA_MANAGER.addTime(found, found.getTag());
			found = DATA_MANAGER.findTimeDate(found.getTag());
		} else {
			found = DATA_MANAGER.findTimeDate(dateTime);
		}
		if (found == null) {
			final TreeTimeSubData ttsd = new TreeTimeSubData();
			TextParsingBiz.parseNameValueAtDivided(dateTime, ttsd);
			String valm = ttsd.getData().get(EchoWriteConst.WORD_MARKER);
			if (!StringUtils.isEmpty(valm)) {
				valm = valm.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
				found = new TreeTimeData(EchoWriteConst.WORD_MARKER + ": " + valm);
			} else {
				final String dateTime2 = dateTime.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
				found = new TreeTimeData(dateTime2);
			}
			found.addDataParsed(ttsd);
			DATA_MANAGER.addTime(found, ttsd, dateTime);
			lastDateTime = found.getTag().trim();

		} else if (addtime) {
			final TreeTimeSubData ttsd = new TreeTimeSubData();
			TextParsingBiz.parseNameValueAtDivided(found.toString(), ttsd);
			if (ttsd.getData() != null) {
				try {
					String valm = ttsd.getData().get(EchoWriteConst.WORD_MARKER);// todo...
					if (valm != null)
						valm = valm.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
					Integer vali = Integer.valueOf(valm);
					vali = vali + 1;
					ttsd.getData().put(EchoWriteConst.WORD_MARKER, vali.toString());
					found = new TreeTimeData(ttsd.toString());
					TextParsingBiz.parseNameValueAtDivided(found.getTag(), ttsd);
					found.addDataParsed(ttsd);
					// datalistTimeDate.add(found);
					DATA_MANAGER.addTime(found, ttsd.toString());
					lastDateTime = found.getTag().trim();
				} catch (NumberFormatException e) {
					LOGGER.error("Failed to parse marker for +1 (" + ttsd.getData().get("marker") + ")");
					e.printStackTrace();
				}
			}
			LOGGER.debug("addtime to found");
		}
		return found;
	}

	protected boolean compareMetaTags(final String mtag, final String mval) {

		if ("bookstarttimedata".compareToIgnoreCase(mtag) == 0) {
			// lastDateTime = mval;
			findTreeTimeData(mval, false);
			return true;
		}
		if (EchoWriteConst.META_LIST_TIMEDATE.compareToIgnoreCase(mtag) == 0) {
			DATA_MANAGER.getTagListTimeDate().add(mval);
			return true;
		}
		if (EchoWriteConst.META_LIST_ACTORS.compareToIgnoreCase(mtag) == 0) {
			DATA_MANAGER.getTagListActors().add(mval);
			return true;
		}
		if (EchoWriteConst.META_LIST_ITEMS.compareToIgnoreCase(mtag) == 0) {
			DATA_MANAGER.getTagListItems().add(mval);
			return true;
		}
		if (EchoWriteConst.META_LIST_SCENE.compareToIgnoreCase(mtag) == 0) {
			DATA_MANAGER.getTagListScene().add(mval);
			return true;
		}
		if (EchoWriteConst.META_LIST_SUBSCENE.compareToIgnoreCase(mtag) == 0) {
			DATA_MANAGER.getTagListSubScenes().add(mval);
		}

		return false;
	}

	// add data to DB
	// protected void addToTreeTimeDataAll(final DocTag docTag) {
	// // parse the tag / find the date tag
	// LOGGER.debug("Lookup tag for time: " + lastDateTime);
	// TreeTimeData timeDate = findTreeTimeData(lastDateTime);
	// if (timeDate == null) {
	// // TODO
	// LOGGER.error("TimeData not found!");
	// }
	// // add to that date tag
	// final TreeTimeSubData ttsd = new TreeTimeSubData();
	// TextParsingBiz.parseNameValueAtDivided(docTag.getValue(), ttsd);
	// DATA_MANAGER.add(ttsd);
	// timeDate.addDataParsed(ttsd);
	// }

	// add TIME data to DB
	protected void addToTreeTimeData(final DocTag docTag) {
		// check if already on list
		// final TreeTimeData found = findTreeTimeData(docTag);
		// found.addData(docTag.getValue());
		lastDateTime = docTag.getValue().trim();

		//
		boolean hasMarker = false;
		final Map<String, String> map = TextParsingBiz.parseNameValueAtDivided(docTag.getFullText());
		if (map.containsKey(EchoWriteConst.WORD_MARKER)) {
			hasMarker = true;
		}
		if (!hasMarker) {
			String valTimeMarker = null;
			for (final String treeTimeData : DATA_MANAGER.getTagListTimeDate()) {
				final String val = map.get(treeTimeData);
				LOGGER.debug("treeTimeData: '" + val + "'");
				valTimeMarker = val;
			}
			if (valTimeMarker == null)// TODO
				if (map.containsKey(EchoWriteConst.WORD_DATE)) {
					valTimeMarker = map.get(EchoWriteConst.WORD_DATE);
				}
			if (valTimeMarker != null)
				lastDateTime = lastDateTime + " marker: " + valTimeMarker;
		}

		findTreeTimeData(lastDateTime);
	}

	// add TIME data to DB

	// add ACTOR/CHAR data to DB
	protected void addToTreeTimeDataTime(final DocTag docTag) {
		// parse the tag
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		// ttsd.addData("char", docTag.getValue());
		// timeDate.addDataParsed(ttsd);// docTag.getValue());
		TextParsingBiz.parseNameValueAtDivided(docTag.getValue(), ttsd);
		// Name
		final String valMark = ttsd.getData().get(EchoWriteConst.WORD_MARKER);
		final String valName = ttsd.getData().get(EchoWriteConst.WORD_NAME);
		final String valChar = ttsd.getData().get(EchoWriteConst.WORD_DATE);
		if (!StringUtils.isEmpty(valName) && !StringUtils.isEmpty(valName.trim()))
			ttsd.getData().put(docTag.getName(), valName.trim());
		else if (StringUtils.isEmpty(valName) && !StringUtils.isEmpty(valMark))
			ttsd.getData().put(EchoWriteConst.WORD_NAME, valMark);
		else if (StringUtils.isEmpty(valName) && !StringUtils.isEmpty(valChar))
			ttsd.getData().put(EchoWriteConst.WORD_NAME, valChar);
		// DESC
		final String valDesc = ttsd.getData().get(EchoWriteConst.WORD_DESC);
		if (StringUtils.isEmpty(valDesc) && !StringUtils.isEmpty(docTag.getValue()))
			ttsd.getData().put(EchoWriteConst.WORD_DESC, docTag.getValue().trim());
		//
		// addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_MARKER);
		DATA_MANAGER.addTime(ttsd, docTag.getValue());
		//
		TreeTimeData timeDate = findTreeTimeData(lastDateTime);
		if (timeDate == null) {
			// TODO
			LOGGER.error("TimeData not found!");
		}
		timeDate.addDataParsed(ttsd);
	}

	// add SUBSCENE data to DB
	protected void addToTreeTimeDataSubScene(final DocTag docTag, final Boolean addtime) {
		LOGGER.debug("Lookup tag for sub scene's lastDateTime: " + lastDateTime);
		final TreeTimeData timeDate = findTreeTimeData(lastDateTime, addtime);
		if (timeDate == null) {
			LOGGER.error("TimeData not found!");
			throw new RuntimeException("TimeData not found!");
		}
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		TextParsingBiz.parseNameValueAtDivided(docTag.getValue(), ttsd);
		final String valActor = ttsd.getData().get(EchoWriteConst.WORD_SUBSCENE);
		if (!StringUtils.isEmpty(valActor) && !StringUtils.isEmpty(valActor.trim()))
			ttsd.getData().put(EchoWriteConst.WORD_SUBSCENE, valActor.trim());
		else {
			ttsd.addData(EchoWriteConst.WORD_NAME, EchoWriteConst.WORD_SUBSCENE);
			ttsd.addData(EchoWriteConst.WORD_DESC, docTag.getValue());
		}
		// datalistScene.add(ttsd);
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_SUBSCENE);
		addToData(ttsd, EchoWriteConst.WORD_NUMBER, sceneNumber);
		sceneNumber++;
		DATA_MANAGER.addSubScene(ttsd, docTag.getValue());
		timeDate.addDataParsed(ttsd);
	}

	//
	protected void addToTreeTimeDataOther(final DocTag docTag) {
		LOGGER.debug("Lookup tag for OTHER lastDateTime: " + lastDateTime);
		final TreeTimeData timeDate = findTreeTimeData(lastDateTime);
		if (timeDate == null) {
			LOGGER.error("TimeData not found!");
			throw new RuntimeException("TimeData not found!");
		}
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		final String fmtStr = TextBiz.cleanFormatTags(docTag.getValue());
		TextParsingBiz.parseNameValueAtDivided(fmtStr, ttsd);
		final String valActor = ttsd.getData().get(EchoWriteConst.WORD_NAME);
		if (!StringUtils.isEmpty(valActor) && !StringUtils.isEmpty(valActor.trim()))
			ttsd.getData().put(EchoWriteConst.WORD_OTHER, valActor);
		else {
			ttsd.addData(EchoWriteConst.WORD_NAME, docTag.getName());
			ttsd.addData(EchoWriteConst.WORD_DESC, docTag.getValue());
		}
		//
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_OTHER);
		DATA_MANAGER.addMisc(ttsd, docTag.getValue());
		timeDate.addDataParsed(ttsd);
	}

	// add SCENE data to DB
	protected void addToTreeTimeDataScene(final DocTag docTag, final Boolean addtime) {
		LOGGER.debug("Lookup tag for scene's lastDateTime: " + lastDateTime);
		final TreeTimeData timeDate = findTreeTimeData(lastDateTime, addtime);
		if (timeDate == null) {
			LOGGER.error("TimeData not found!");
			throw new RuntimeException("TimeData not found!");
		}
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		TextParsingBiz.parseNameValueAtDivided(docTag.getValue(), ttsd);
		final String valActor = ttsd.getData().get(EchoWriteConst.WORD_SCENE);
		if (!StringUtils.isEmpty(valActor) && !StringUtils.isEmpty(valActor.trim()))
			ttsd.getData().put(EchoWriteConst.WORD_SCENE, valActor.trim());
		else {
			ttsd.addData(EchoWriteConst.WORD_NAME, EchoWriteConst.WORD_SCENE);
			ttsd.addData(EchoWriteConst.WORD_DESC, docTag.getValue());
		}
		//
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_SCENE);
		addToData(ttsd, EchoWriteConst.WORD_NUMBER, sceneNumber);
		sceneNumber++;
		DATA_MANAGER.addScene(ttsd, docTag.getValue());
		timeDate.addDataParsed(ttsd);
	}

	protected void addToData(final TreeTimeSubData ttsd, final String type, final Integer value) {
		ttsd.addData(type, value.toString());
	}

	protected void addToData(final TreeTimeSubData ttsd, final String type, final String value) {
		ttsd.addData(type, value);
	}

	protected void addIDToData(final TreeTimeSubData ttsd, final String type) {
		ttsd.addData(EchoWriteConst.WORD_TYPE, type);

		String id = "";
		if (EchoWriteConst.WORD_CHAPTER == type || EchoWriteConst.WORD_SECTION == type) {
			id = ttsd.getDataByKey(EchoWriteConst.WORD_NAME);
			// }else if(EchoWriteConst.WORD_CHAPTER==type){
		} else {
			// final Map<String, String> data = ttsd.getData();
			String val = ttsd.getDataByKey(EchoWriteConst.WORD_NAME);
			id = val;
		}
		ttsd.addData(EchoWriteConst.WORD_ID, id);
	}

	protected void addIDToData(final NestedTreeData ttsd, final String type) {
		final NestedTreeData ntd1 = new NestedTreeData(EchoWriteConst.WORD_TYPE, type);
		ttsd.addData(ntd1);// EchoWriteConst.WORD_MARKER, valMarker2);

		String id = "";
		if (EchoWriteConst.WORD_CHAPTER == type || EchoWriteConst.WORD_SECTION == type) {
			id = ttsd.getTag();
			// }else if(EchoWriteConst.WORD_CHAPTER==type){
		} else {
			String val = null;
			final List<NestedTreeData> data = ttsd.getData();
			for (final NestedTreeData nestedTreeData : data) {
				if (EchoWriteConst.WORD_NAME == nestedTreeData.getTag())
					val = nestedTreeData.getValue();
			}
			id = val;
		}
		final NestedTreeData ntd2 = new NestedTreeData(EchoWriteConst.WORD_ID, id);
		ttsd.addData(ntd2);

	}

	protected void addMarkerToData(final NestedTreeData ttsd, final TreeTimeData timeDate) {
		if (timeDate == null)
			return;
		String valMarker = null;
		final List<NestedTreeData> data = ttsd.getData();
		for (final NestedTreeData nestedTreeData : data) {
			if (EchoWriteConst.WORD_MARKER == nestedTreeData.getTag())
				valMarker = nestedTreeData.getValue();
		}

		if (StringUtils.isEmpty(valMarker)) {
			String valMarker2 = timeDate.getDataParsedByName(EchoWriteConst.WORD_MARKER);
			if (valMarker2 != null)
				valMarker2 = valMarker2.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
			final NestedTreeData ntd = new NestedTreeData(EchoWriteConst.WORD_MARKER, valMarker2);
			ttsd.addData(ntd);// EchoWriteConst.WORD_MARKER, valMarker2);
		}
	}

	protected void addMarkerToData(final TreeTimeSubData ttsd, final TreeTimeData timeDate) {
		if (timeDate == null)
			return;
		final String valMarker = ttsd.getData().get(EchoWriteConst.WORD_MARKER);
		if (StringUtils.isEmpty(valMarker)) {
			String valMarker2 = timeDate.getDataParsedByName(EchoWriteConst.WORD_MARKER);
			if (valMarker2 != null)
				valMarker2 = valMarker2.replaceAll(EchoWriteConst.regExpReplaceSpecialChars, "");
			ttsd.addData(EchoWriteConst.WORD_MARKER, valMarker2);
		}
	}

	// add ACTOR/CHAR data to DB
	protected void addToTreeTimeDataActor(final DocTag docTag) {
		// parse the tag
		// find the date tag
		LOGGER.debug("Lookup tag for actor: " + lastDateTime);
		TreeTimeData timeDate = findTreeTimeData(lastDateTime);
		if (timeDate == null) {
			// TODO
			LOGGER.error("TimeData not found!");
		}
		// add to that date tag
		// final TreeTimeData ttd = new TreeTimeData("char");
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		// ttsd.addData("char", docTag.getValue());
		// timeDate.addDataParsed(ttsd);// docTag.getValue());
		TextParsingBiz.parseNameValueAtDivided(docTag.getValue(), ttsd);
		final String valName = ttsd.getData().get(EchoWriteConst.WORD_NAME);
		final String valChar = ttsd.getData().get(EchoWriteConst.WORD_CHAR);
		if (!StringUtils.isEmpty(valName) && !StringUtils.isEmpty(valName.trim()))
			ttsd.getData().put(docTag.getName(), valName.trim());
		if (StringUtils.isEmpty(valName) && !StringUtils.isEmpty(valChar))
			ttsd.getData().put(EchoWriteConst.WORD_NAME, valChar);

		//
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_ACTOR);
		DATA_MANAGER.addActor(ttsd, docTag.getValue());
		timeDate.addDataParsed(ttsd);
	}

	// add ITEM data to DB
	protected void addToTreeTimeDataItem(final DocTag docTag) {
		// parse the tag
		if (lastDateTime == null) {
			lastDateTime = "0";
		}
		// find the date tag
		LOGGER.debug("Lookup tag for date: " + lastDateTime);
		TreeTimeData timeDate = findTreeTimeData(lastDateTime);
		if (timeDate == null) {
			// TODO
			LOGGER.error("TimeData not found!");
		}
		// add to that date tag
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		TextParsingBiz.parseNameValueAtDivided(docTag.getValue(), ttsd);
		if (ttsd.getData().size() < 1)
			ttsd.addData(docTag.getValue(), docTag.getValue());

		String valActor = ttsd.getData().get(EchoWriteConst.WORD_NAME);
		// If Name is null, put in item
		if (StringUtils.isEmpty(valActor)) {
			ttsd.getData().put(EchoWriteConst.WORD_NAME, ttsd.getData().get(EchoWriteConst.WORD_ITEM));
			valActor = ttsd.getData().get(EchoWriteConst.WORD_NAME);
		}
		// If desc is empty, put in item
		if (StringUtils.isEmpty(ttsd.getData().get(EchoWriteConst.WORD_DESC)))
			ttsd.getData().put(EchoWriteConst.WORD_DESC, ttsd.getData().get(EchoWriteConst.WORD_ITEM));

		// if char is null, put name there?
		final String valChar = ttsd.getData().get(EchoWriteConst.WORD_CHAR);
		if (StringUtils.isEmpty(valChar) && !StringUtils.isEmpty(valActor))
			ttsd.getData().put(EchoWriteConst.WORD_CHAR, valActor);
		//
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_ITEM);
		timeDate.addData(docTag.getValue());
		DATA_MANAGER.addItem(ttsd, docTag.getValue());
	}

}
