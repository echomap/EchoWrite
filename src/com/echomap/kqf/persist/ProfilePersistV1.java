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

	private ProfileData loadOldProfileObject(final String key) {
		LOGGER.debug("loadOldProfileObject: Called");
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

		pd.setText("cbCenterStars", child.get("cbCenterStars", "false"));
		pd.setText("cbDropCapChapters", child.get("cbDropCapChapters", "false"));
		pd.setText("cbRemoveSectDiv", child.get("cbRemoveSectDiv", "false"));
		pd.setText("cbRemoveDiv", child.get("cbRemoveDiv", "false"));
		pd.setText("wantTextChptOutput", child.get("wantTextChptOutput", "false"));

		pd.setSelected("cbCenterStars", child.getBoolean("cbWantTextChptOutput", false));
		pd.setSelected("cbDropCapChapters", child.getBoolean("cbDropCapChapters", false));
		pd.setSelected("cbRemoveSectDiv", child.getBoolean("cbRemoveSectDiv", false));
		pd.setSelected("cbRemoveDiv", child.getBoolean("cbRemoveDiv", false));
		pd.setSelected("wantTextChptOutput", child.getBoolean("wantTextChptOutput", false));

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

		pd.setText("cbDropCapChapters", child.get("cbDropCapChapters", ""));
		pd.setText("cbWantTextChptOutput", child.get("cbWantTextChptOutput", ""));
		pd.setSelected("cbDropCapChapters", child.getBoolean("cbDropCapChapters", false));
		pd.setSelected("cbWantTextChptOutput", child.getBoolean("cbWantTextChptOutput", false));

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
}
