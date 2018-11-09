package com.echomap.kqf.persist;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.XferBiz;
import com.echomap.kqf.data.OtherDocTagData;
import com.echomap.kqf.data.ProfileData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProfilePersistV1 {
	private final static Logger LOGGER = LogManager.getLogger(ProfilePersistV1.class);

	private final Preferences userPrefs;
	String appVersion = null;
	private final List<String> messages = new ArrayList<>();
	private boolean wasError = false;
	private List<ProfileData> profiles = new ArrayList<>();
	String currentVersion = null;

	public ProfilePersistV1(final Preferences userPrefs) {
		this.userPrefs = userPrefs;
		this.appVersion = null;
		this.wasError = false;
		this.profiles.clear();
		this.messages.clear();
	}

	public void setVersion(final String appVersion) {
		this.appVersion = appVersion;
	}

	public List<String> getMessages() {
		return messages;
	}

	public List<ProfileData> getProfiles() {
		return profiles;
	}

	public boolean isWasError() {
		return wasError;
	}

	public void loadProfiles() {
		LOGGER.debug("loadProfiles: Called");
		try {
			final String[] prefkeys = userPrefs.childrenNames();
			if (prefkeys != null && prefkeys.length > 0) {
				for (final String key : prefkeys) {
					LOGGER.debug("loadProfiles: key = '" + key + "'");
					if (!StringUtils.isBlank(key)) {
						final ProfileData profile = loadOldProfileObject(key);
						profiles.add(profile);
					}
				}
			}
			// for (String key : titleOneText.getItems()) {
			// final Preferences child = userPrefs.node(key);
			// final String seriesTitle = child.get("seriesTitle", "");
			// if (!seriesTitleComboText.getItems().contains(seriesTitle)) {
			// seriesTitleComboText.getItems().add(seriesTitle);
			// }
			// }

		} catch (BackingStoreException e) {
			messages.add("Error LOADING profile: " + e);
			wasError = true;
			e.printStackTrace();
		}
		LOGGER.debug("loadProfiles: Done");
	}

	private boolean setupCBSelectedValueFromOld(final String selLabel, final String selValue) {
		boolean val = false;
		if (StringUtils.isBlank(selValue))
			val = false;
		else if (selValue.compareTo("selected") == 0)
			val = true;
		else if (selValue.compareTo("true") == 0)
			val = true;
		LOGGER.debug("setupCBSelectedValueFromOld: selLabel='" + selLabel + "' selValue='" + selValue + "' val=" + val);
		return val;
	}

	private ProfileData loadOldProfileObject(final String key) {
		LOGGER.debug("loadOldProfileObject: Called, key='" + key + "'");
		final ProfileData pd = new ProfileData();

		//
		final Preferences child = userPrefs.node(key);
		//
		pd.setKey(key);
		pd.setSeries(child.get("seriesTitle", ""));
		pd.setText("seriesTitle", child.get("seriesTitle", ""));
		pd.setText("volume", child.get("volume", "1"));
		pd.setText("titleOne", child.get("titleTwo", key));
		pd.setText("titleTwo", child.get("titleThree", ""));
		// pd.setText("titleThree", child.get("titleThree", ""));
		pd.setText("inputFile", child.get("inputFile", ""));
		pd.setText("ouputFile", child.get("ouputFile", ""));
		pd.setText("outputDir", child.get("outputDir", ""));
		pd.setText("outputFormatChpTextDirText", child.get("outputFormatChpTextDirText", ""));

		pd.setText("inputFilePrefix", child.get("inputFilePrefix", ""));
		pd.setSelected("appendUnderscoreToPrefix", child.getBoolean("appendUnderscoreToPrefix", false));
		pd.setText("appendUnderscoreToPrefix", child.get("appendUnderscoreToPrefix", ""));
		// chpDivText.setText(child.get("chpDiv", ""));
		// secDivText.setText(child.get("secDiv", ""));
		pd.setText("regexpChapter", child.get("regexpChapter", ""));
		pd.setText("regexpSection", child.get("regexpSection", ""));

		// Default REGEXP
		pd.setText("regexpChapterText", "-=\\s+(?<cname>Chapter)\\s+(?<cnum>\\w+):\\s+(?<ctitle>.*)\\s+=-");
		pd.setText("regexpSectionText", "-=\\s+(?<sname>Section):\\s+(?<stitle>\\w+)\\s+=-");

		pd.setText("docTagStart", child.get("docTagStart", "[[*"));
		pd.setText("docTagEnd", child.get("docTagEnd", "*]]"));

		pd.setText("chapterHeaderTag", child.get("chapterHeaderTag", "[[*"));
		pd.setText("sectionHeaderTag", child.get("sectionHeaderTag", "*]]"));

		pd.setText("cbRemoveDiv", child.get("cbRemoveDiv", "false"));
		pd.setText("cbCenterStars", child.get("cbCenterStars", "false"));
		pd.setText("cbDropCapChapters", child.get("cbDropCapChapters", "false"));
		pd.setText("cbWantTextChptOutput", child.get("cbWantTextChptOutput", "false"));
		pd.setText("cbRemoveSectDiv", child.get("cbRemoveSectDiv", "false"));
		// pd.setText("wantTextChptOutput", child.get("wantTextChptOutput",
		// "false"));

		pd.setSelected("cbRemoveDiv", setupCBSelectedValueFromOld("cbRemoveDiv", child.get("cbRemoveDiv", "false")));
		pd.setSelected("cbCenterStars",
				setupCBSelectedValueFromOld("cbCenterStars", child.get("cbCenterStars", "false")));
		pd.setSelected("cbDropCapChapters",
				setupCBSelectedValueFromOld("cbDropCapChapters", child.get("cbDropCapChapters", "false")));
		pd.setSelected("cbWantTextChptOutput",
				setupCBSelectedValueFromOld("cbWantTextChptOutput", child.get("cbWantTextChptOutput", "false")));
		pd.setSelected("cbRemoveSectDiv",
				setupCBSelectedValueFromOld("cbRemoveSectDiv", child.get("cbRemoveSectDiv", "false")));
		// pd.setSelected("wantTextChptOutput",
		// setupCBSelectedValueFromOld(child.get("wantTextChptOutput",
		// "false")));

		pd.setText("fmtMode", child.get("fmtMode", ""));
		pd.setText("outputEncoding", child.get("outputEncoding", ""));
		pd.setText("counterDigitChoice", child.get("counterDigitChoice", ""));

		pd.setText("ouputCountFile", child.get("ouputCountFile", ""));
		pd.setText("ouputOutlineFile", child.get("ouputOutlineFile", ""));
		pd.setText("ouputOutlineFile1", child.get("ouputOutlineFile1", ""));

		pd.setText("outputDocTagsOutlineFile", child.get("outputDocTagsOutlineFile", ""));
		pd.setText("outputDocTagsSceneFile", child.get("outputDocTagsSceneFile", ""));

		pd.setText("outputDocTagsOutlineCTags", child.get("outputDocTagsOutlineCTags", ""));
		pd.setText("outputDocTagsOutlineTags", child.get("outputDocTagsOutlineTags", ""));

		pd.setText("outputDocTagsSceneTags", child.get("outputDocTagsSceneTags", ""));
		pd.setText("outputDocTagsSceneCoTags", child.get("outputDocTagsSceneCoTags", ""));

		pd.setText("counterDigitChoice", child.get("counterDigitChoice", ""));
		pd.setText("outputDocTagsMaxLineLength", child.get("outputDocTagsMaxLineLength", ""));

		pd.setText("outputDocTagsScenePrefix", child.get("outputDocTagsScenePrefix", ""));
		pd.setText("outputDocTagsSubScenePrefix", child.get("outputDocTagsSubScenePrefix", ""));
		pd.setText("sceneCoalateDiv", child.get("sceneCoalateDiv", ""));

		//
		final List<OtherDocTagData> outputs = loadOutputs(child);
		pd.setOutputs(outputs);

		LOGGER.debug("loadOldProfileObject: Done");
		return pd;
	}

	private List<OtherDocTagData> loadOutputs(final Preferences child) {
		LOGGER.debug("loadOutputs: Called for child");

		final String listString = child.get(XferBiz.PROFILE_DATA, "");
		final Gson gson = new Gson();

		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		final List<OtherDocTagData> listODTD = gson.fromJson(listString, listOfTestObject);
		if (listODTD != null) {
			for (OtherDocTagData otherDocTagData : listODTD) {
				LOGGER.debug("listODTD item: " + otherDocTagData);
			}
			// Load Options
			// TODO Load options
			// final Preferences child1 = child.node(XferBiz.PROFILE_DATA);
			// final Preferences child2 = child.node("list");
			// child2.node(pathName)
			// final Preferences childO = child.node("options");
			// final String name = childO.get("name", "");
			// final String prefix = childO.get("prefix", "");
			// final boolean showCompress = childO.getBoolean("showCompress",
			// false);
			// final boolean showExpand = childO.getBoolean("showExpand", true);

		}
		return listODTD;
	}

	public void saveProfiles(final ProfileData pd) throws BackingStoreException {
		LOGGER.debug("saveProfiles: Called.");
		final String key = pd.getKey();
		LOGGER.debug("saveProfiles: key='" + key + "'");
		final Preferences child = userPrefs.node(key);

		if (child == null) {
			throw new BackingStoreException("No such Profile Found!");
		}
		//
		child.put("key", pd.getKey());
		child.put("titleOne", key);
		setValueIfNotNull(child, "seriesTitle", pd.getSeries());
		setValueIfNotNull(child, "volume", pd.getText("volume"));
		setValueIfNotNull(child, "titleOne", pd.getText("titleOne"));
		setValueIfNotNull(child, "titleTwo", pd.getText("titleTwo"));
		setValueIfNotNull(child, "titleThree", pd.getText("titleThree"));
		setValueIfNotNull(child, "inputFile", pd.getText("inputFile"));
		setValueIfNotNull(child, "ouputFile", pd.getText("ouputFile"));
		setValueIfNotNull(child, "outputDir", pd.getText("outputDir"));
		setValueIfNotNull(child, "outputFormatChpTextDirText", pd.getText("outputFormatChpTextDirText"));
		setValueIfNotNull(child, "inputFilePrefix", pd.getText("inputFilePrefix"));
		child.putBoolean("appendUnderscoreToPrefix", pd.isSelected("appendUnderscoreToPrefix"));
		// setValueIfNotNull(child, "appendUnderscoreToPrefix",
		// pd.getText("appendUnderscoreToPrefix"));

		setValueIfNotNull(child, "regexpChapter", pd.getText("regexpChapter"));
		setValueIfNotNull(child, "regexpSection", pd.getText("regexpSection"));

		// Default REGEXP
		setValueIfNotNull(child, "regexpChapterText", pd.getText("regexpChapterText"));
		setValueIfNotNull(child, "regexpSectionText", pd.getText("regexpSectionText"));

		setValueIfNotNull(child, "docTagStart", pd.getText("docTagStart"));
		setValueIfNotNull(child, "docTagEnd", pd.getText("docTagEnd"));

		setValueIfNotNull(child, "chapterHeaderTag", pd.getText("chapterHeaderTag"));
		setValueIfNotNull(child, "sectionHeaderTag", pd.getText("sectionHeaderTag"));

		setValueIfNotNull(child, "cbRemoveDiv", pd.getText("cbRemoveDiv"));
		setValueIfNotNull(child, "cbCenterStars", pd.getText("cbCenterStars"));
		setValueIfNotNull(child, "cbDropCapChapters", pd.getText("cbDropCapChapters"));
		setValueIfNotNull(child, "cbWantTextChptOutput", pd.getText("cbWantTextChptOutput"));
		setValueIfNotNull(child, "cbRemoveSectDiv", pd.getText("cbRemoveSectDiv"));

		setValueIfNotNull(child, "fmtMode", pd.getText("fmtMode"));
		setValueIfNotNull(child, "outputEncoding", pd.getText("outputEncoding"));
		setValueIfNotNull(child, "counterDigitChoice", pd.getText("counterDigitChoice"));

		setValueIfNotNull(child, "ouputCountFile", pd.getText("ouputCountFile"));
		setValueIfNotNull(child, "ouputOutlineFile", pd.getText("ouputOutlineFile"));
		setValueIfNotNull(child, "ouputOutlineFile1", pd.getText("ouputOutlineFile1"));

		setValueIfNotNull(child, "outputDocTagsOutlineFile", pd.getText("outputDocTagsOutlineFile"));
		setValueIfNotNull(child, "outputDocTagsSceneFile", pd.getText("outputDocTagsSceneFile"));

		setValueIfNotNull(child, "outputDocTagsOutlineCTags", pd.getText("outputDocTagsOutlineCTags"));
		setValueIfNotNull(child, "outputDocTagsOutlineTags", pd.getText("outputDocTagsOutlineTags"));

		setValueIfNotNull(child, "outputDocTagsSceneTags", pd.getText("outputDocTagsSceneTags"));
		setValueIfNotNull(child, "outputDocTagsSceneCoTags", pd.getText("outputDocTagsSceneCoTags"));

		setValueIfNotNull(child, "counterDigitChoice", pd.getText("counterDigitChoice"));
		setValueIfNotNull(child, "outputDocTagsMaxLineLength", pd.getText("outputDocTagsMaxLineLength"));

		setValueIfNotNull(child, "outputDocTagsScenePrefix", pd.getText("outputDocTagsScenePrefix"));
		setValueIfNotNull(child, "outputDocTagsSubScenePrefix", pd.getText("outputDocTagsSubScenePrefix"));
		setValueIfNotNull(child, "sceneCoalateDiv", pd.getText("sceneCoalateDiv"));

		// More Files - were saved in their own interface
		// final List<OtherDocTagData> outputs = loadOutputs(child);
		saveOutputs(child, pd);
		// pd.setOutputs(outzputs);

		child.flush();
	}

	void saveOutputs(final Preferences child, final ProfileData pd) {
		LOGGER.debug("handleSave: Called");
		final List<OtherDocTagData> outputs = pd.getOutputs();
		// final ObservableList<OtherDocTagData> targetList =
		// inputTable.getItems();
		final List<OtherDocTagData> removeList = new ArrayList<>();
		if (outputs != null) {
			for (OtherDocTagData otherDocTagData : outputs) {
				LOGGER.debug("item: " + otherDocTagData);
				if (StringUtils.isEmpty(otherDocTagData.getName()))
					removeList.add(otherDocTagData);
				if (otherDocTagData.getFile() == null)
					removeList.add(otherDocTagData);
			}
		}
		for (OtherDocTagData otherDocTagData : removeList) {
			outputs.remove(otherDocTagData);
		}
		final Type listType = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		final Gson gson = new Gson();
		String json = gson.toJson(outputs, listType);
		LOGGER.debug("handleSave: json: '" + json + "'");
		// Save
		child.put(XferBiz.PROFILE_DATA, json);
		//
		LOGGER.debug("handleSave: Done");
	}

	private void setValueIfNotNull(final Preferences child, final String key, String val) throws BackingStoreException {
		if (val == null) {
			if (child.nodeExists(key))
				child.remove(key);
		} else {
			child.put(key, val);
		}
	}

	public void deleteProfile(final String key) throws BackingStoreException {
		LOGGER.debug("deleteProfile: Called.");
		boolean childDeleted = false;

		final Preferences child = userPrefs.node(key);
		if (child != null) {// && child.keys().length > 0
			// showMessage("Child '" + child + "'", false);
			child.removeNode();
			// showMessage("Deleted profile '" + key + "'", false);
			childDeleted = true;
			// setProfileChangeMade(false);
		}
		userPrefs.flush();

		if (!childDeleted) {
			// showMessage("That profile doesn't exist.", false);
			throw new BackingStoreException("No such Profile found with the key <" + key + ">");
		}
		LOGGER.debug("deleteProfile: Done.");
	}

	public void renameProfile(final ProfileData profileData, final String oldKey, final String newKey)
			throws BackingStoreException {
		LOGGER.debug("renameProfile: Called. oldKey='" + oldKey + " newKey='" + newKey + "'");
		boolean childDeleted = false;

		final Preferences childOld = userPrefs.node(oldKey);
		if (childOld != null) {
			childOld.removeNode();
			childDeleted = true;
			profileData.setKey(newKey);
			this.saveProfiles(profileData);
		} else {
			LOGGER.warn("renameProfile: Child with old key not found!");
		}
		userPrefs.flush();

		if (!childDeleted) {
			throw new BackingStoreException("No such Profile found with the key <" + profileData + ">");
		}
		LOGGER.debug("renameProfile: Done.");
	}

}
