package com.echomap.kqf.two.gui;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.ProfileExportObj;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class KQFSubBaseExportCtrl extends KQFBaseCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFSubBaseExportCtrl.class);

	@FXML
	TableView inputTable;
	@FXML
	TextField inputFile;

	Preferences profileData = null;
	FormatDao formatDao = null;

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// doCleanup();
		stage.close();
	}

	public void handleBrowse(final ActionEvent event) {
		LOGGER.debug("handleBrowse: Called");
		String newFile = null;
		newFile = locateDir(event, "Open Output Dir ", "", formatDao.getInputFilename());
		if (newFile != null) {
			final String fileNameDefault = "profileexport.json";
			final String outFilename = "/" + fileNameDefault;
			final File nFile = new File(newFile, outFilename);
			inputFile.setText(nFile.getAbsolutePath());
		}
	}

	public void handleSelectAll(final ActionEvent event) {
		LOGGER.debug("handleSelectAll: Called");
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(true);
			}
		}
		inputTable.refresh();
	}

	public void handleSelectNone(final ActionEvent event) {
		LOGGER.debug("handleSelectNone: Called");
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(false);
			}
		}
		inputTable.refresh();
	}

	Preferences getPrefs() {
		return profileData;
	}

	JsonArray createProfileData() {
		LOGGER.debug("createProfileData: Called");
		final JsonObject albums = new JsonObject();
		if (appProps != null)
			albums.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));

		// create
		final JsonArray profileDataArray = new JsonArray();
		try {
			final String[] prefkeys = getPrefs().childrenNames();
			if (prefkeys != null && prefkeys.length > 0) {
				for (final String str1 : prefkeys) {
					if (StringUtils.isBlank(str1))
						continue;
					final JsonObject dataset = new JsonObject();
					loadProfile(str1, dataset);
					profileDataArray.add(dataset);
					LOGGER.debug("createProfileData: loaded profile: '" + str1 + "'");
					// gson.toJson( )
				}
			}
		} catch (BackingStoreException e) {
			showMessage("Error createProfileData: " + e, false);
			e.printStackTrace();
		}
		LOGGER.debug("createProfileData: Done");
		return profileDataArray;
	}

	void loadProfile(final String nodeKey, final JsonObject dataset) {
		final Preferences child = getPrefs().node(nodeKey);
		dataset.addProperty("export", true);

		try {
			final String[] ckeys = child.keys();
			for (final String ckey : ckeys) {
				final String cval = child.get(ckey, "");
				dataset.addProperty(ckey, cval);
				LOGGER.debug("loadProfile: added '" + ckey + "'='" + cval + "'");
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
			LOGGER.error("Error in loadProfile", e);
		}

		// add the property
//		dataset.addProperty("titleOneText", child.get("titleOne", nodeKey));
//		dataset.addProperty("titleTwoText", child.get("titleTwo", ""));
//		dataset.addProperty("titleThreeText", child.get("titleThree", ""));
//
//		dataset.addProperty("inputFileText", child.get("inputFile", ""));
//		dataset.addProperty("outputFormatSingleFileText", child.get("ouputFile", ""));
//		dataset.addProperty("outputFormatChpHtmlDirText", child.get("outputDir", ""));
//		dataset.addProperty("outputFormatChpTextDirText", child.get("outputFormatChpTextDirText", ""));
//
//		dataset.addProperty("inputFilePrefixText", child.get("inputFilePrefix", ""));
//		dataset.addProperty("filePrefixCheckbox", child.getBoolean("appendUnderscoreToPrefix", false));
//
//		dataset.addProperty("regexpChapterText", child.get("regexpChapter", ""));
//		dataset.addProperty("regexpSectionText", child.get("regexpSection", ""));
//
//		// Default REGEXP
//		dataset.addProperty("regexpChapterText", child.get("regexpChapter", ""));
//		dataset.addProperty("regexpSectionText", child.get("regexpSection", ""));
//
//		dataset.addProperty("docTagStartText", child.get("docTagStart", ""));
//		dataset.addProperty("docTagEndText", child.get("docTagEnd", ""));
//
//		dataset.addProperty("fmtModeText", child.get("fmtMode", ""));
//		dataset.addProperty("outputEncoding", child.get("outputEncoding", ""));
//
//		dataset.addProperty("outputCountFileText", child.get("ouputCountFile", ""));
//		dataset.addProperty("outputOutlineFileText", child.get("ouputOutlineFile", ""));
//		dataset.addProperty("outputOutlineFileText1", child.get("ouputOutlineFile1", ""));
//
//		dataset.addProperty("outputDocTagsOutlineFileText", child.get("outputDocTagsOutlineFile", ""));
//		dataset.addProperty("outputDocTagsSceneFileText", child.get("outputDocTagsSceneFile", ""));
//
//		dataset.addProperty("outputDocTagsOutlineCTagsText", child.get("outputDocTagsOutlineCTags", ""));
//		dataset.addProperty("outputDocTagsOutlineETagsText", child.get("outputDocTagsOutlineTags", ""));
//
//		dataset.addProperty("outputDocTagsSceneTagsText", child.get("outputDocTagsSceneTags", ""));
//		dataset.addProperty("outputDocTagsSceneCoTags", child.get("outputDocTagsSceneCoTags", ""));
//
//		dataset.addProperty("cbDropCapChaptersSel", child.get("cbDropCapChapters", ""));
//		dataset.addProperty("cbWantTextChptOutputSel", child.get("cbWantTextChptOutput", ""));
//		dataset.addProperty("counterDigitChoice", child.get("counterDigitChoice", ""));
//		dataset.addProperty("outputDocTagsMaxLineLengthSel", child.get("outputDocTagsMaxLineLength", ""));
//
//		dataset.addProperty("outputDocTagsScenePrefix", child.get("outputDocTagsScenePrefix", ""));
//		dataset.addProperty("outputDocTagsSubScenePrefix", child.get("outputDocTagsSubScenePrefix", ""));
//		dataset.addProperty("sceneCoalateDiv", child.get("sceneCoalateDiv", ""));
	}

}
