package com.echomap.kqf.datamgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.TreeData;
import com.echomap.kqf.data.TreeTimeData;

public class DataManagerDBData {
	//
	private final File inputFile;
	// all the outliner data
	private final List<TreeData> datalist = new ArrayList<>();

	// all the INTERNAL data
	private final List<DataItem> dataItemList = new ArrayList<>();

	// all the meta doctags
	private final List<DocTag> metaDocTagList = new ArrayList<>();

	// all the dates //TODO need this? TreeTimeData? and Nested Tree?
	private final List<TreeTimeData> datalistTimeDate = new ArrayList<>();

	// all the lists
	private final List<String> tagListScene = new ArrayList<>();
	private final List<String> tagListSubScenes = new ArrayList<>();
	private final List<String> tagListTimeDate = new ArrayList<>();
	private final List<String> tagListActors = new ArrayList<>();
	private final List<String> tagListItems = new ArrayList<>();

	private boolean initialized = false;
	private boolean fromCache = false;

	public DataManagerDBData(final File inputFile) {
		this.inputFile = inputFile;
	}

	public void initialize() {
		this.dataItemList.clear();
		this.datalist.clear();
		this.datalistTimeDate.clear();
		this.metaDocTagList.clear();
		this.tagListActors.clear();
		this.tagListItems.clear();
		this.tagListScene.clear();
		this.tagListSubScenes.clear();
		this.tagListTimeDate.clear();
		initialized = true;
	}

	public List<String> getTagListScene() {
		return tagListScene;
	}

	public List<String> getTagListSubScenes() {
		return tagListSubScenes;
	}

	public List<String> getTagListTimeDate() {
		return tagListTimeDate;
	}

	public List<String> getTagListActors() {
		return tagListActors;
	}

	public List<String> getTagListItems() {
		return tagListItems;
	}

	public List<TreeData> getDatalist() {
		return datalist;
	}

	public List<DataItem> getDataItemList() {
		return dataItemList;
	}

	public List<DocTag> getMetaDocTagList() {
		return metaDocTagList;
	}

	public List<TreeTimeData> getDatalistTimeDate() {
		return datalistTimeDate;
	}

	public boolean isFromCache() {
		return fromCache;
	}

	public void setFromCache(boolean fromCache) {
		this.fromCache = fromCache;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public File getInputFile() {
		return inputFile;
	}

}
