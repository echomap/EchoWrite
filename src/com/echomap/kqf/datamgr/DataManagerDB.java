package com.echomap.kqf.datamgr;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DataManagerDB {
	private final static Logger LOGGER = LogManager.getLogger(DataManagerDB.class);
	private static DataManagerDB DATA_MANAGER = new DataManagerDB();

	private final Map<File, Long> lastModifiedDate = new HashMap<File, Long>();

	private final Map<File, DataManagerDBData> lastFileData = new HashMap<File, DataManagerDBData>();

	/**
	 * 
	 */
	private DataManagerDB() {

	}

	public static DataManagerDB getDataManager() {
		return DATA_MANAGER;
	}

	public DataManagerDBData getDataForFile(final File inputFile) {
		initialize();
		DataManagerDBData data = lastFileData.get(inputFile);
		if (data == null)
			data = new DataManagerDBData(inputFile);
		else
			data.setFromCache(true);

		// TODO if cache is old, reinitialize data
		final Long lastDateProcessed = lastModifiedDate.get(inputFile);
		if (lastDateProcessed == null || lastDateProcessed != inputFile.lastModified()) {
			data.initialize();
		} else {

		}
		return data;
	}

	public void saveDataForFile(final DataManagerDBData data) {
		if (data == null)
			return;
		lastFileData.put(data.getInputFile(), data);
		lastModifiedDate.put(data.getInputFile(), data.getInputFile().lastModified());
	}

	public void clearDataForFile(final File inputFile) {
		lastFileData.remove(inputFile);
		lastModifiedDate.put(inputFile, inputFile.lastModified());
	}

	private void initialize() {
	}

}
