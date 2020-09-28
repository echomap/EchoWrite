package com.echomap.kqf.profile.persist;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.OtherDocTagData;
import com.echomap.kqf.export.XferBiz;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author mkatz
 */
public class ProfileBiz {
	private final static Logger LOGGER = LogManager.getLogger(ProfileBiz.class);

	public static final String PROP_KEY_VERSION = "version";

	private final Preferences userPrefs;
	String appVersion = null;
	private final List<String> messages = new ArrayList<>();
	private boolean wasError = false;
	private List<ProfileData> profiles = new ArrayList<>();
	String currentVersion = null;

	protected ProfileBiz(final Preferences userPrefs) {
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

	public boolean isWasError() {
		return wasError;
	}

	public boolean loadProfileData() { // throws BackingStoreException {
		LOGGER.debug("loadProfileData: Called");
		boolean outOfDate = false;
		// try {

		// check errors // final String[] prefkeys = userPrefs.childrenNames();

		// check version
		final String currentVerS = userPrefs.get("version", null);
		LOGGER.debug("loadProfileData: currentVerS = " + currentVerS);
		if (!StringUtils.isBlank(currentVerS)) {
			LOGGER.debug("loadProfileData: currentVerS: '" + currentVerS + "'");
			final Integer currentVer = Integer.valueOf(currentVerS);
			final Integer appVer = Integer.valueOf(appVersion);
			if (currentVer < appVer) {
				outOfDate = true;
			}
		} else
			outOfDate = true;
		LOGGER.debug("loadProfileData: outOfDate = " + outOfDate);

		// if old version
		if (outOfDate)
			doImportFromOldVersion();
		loadSeries();
		loadProfiles();
		// } catch (BackingStoreException e) {
		// LOGGER.error("loadProfileData: Error: " + e, e);
		// e.printStackTrace();
		// throw e;
		// }
		LOGGER.debug("loadProfileData: Done");
		return wasError;
	}

	private void loadProfiles() {
		LOGGER.debug("loadProfiles: Called");
		try {
			final String[] prefkeys = userPrefs.childrenNames();
			if (prefkeys != null && prefkeys.length > 0) {
				for (final String str1 : prefkeys) {
					LOGGER.debug("loadProfiles: str1 = '" + str1 + "'");
					// final Object obj = userPrefs.nodeExists(pathName)
					if (!StringUtils.isBlank(str1)) {
						// final Profile profile = new Profile();
						// setupProfilePerPrefs(profile, asFdf);
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

	private void loadSeries() {
		LOGGER.debug("loadSeries: Called");

		LOGGER.debug("loadSeries: Done");
	}

	private void doImportFromOldVersion() {
		LOGGER.debug("doImportFromOldVersion: Called");
		// try {
		// final List<ProfileData> profiles = new ArrayList<ProfileData>();
		// final String[] prefkeys = userPrefs.childrenNames();
		// if (prefkeys != null && prefkeys.length > 0) {
		// for (final String str1 : prefkeys) {
		// if (!StringUtils.isBlank(str1)) {
		// final ProfileData profileData = loadOldProfileObject(str1);
		// profiles.add(profileData);
		// LOGGER.debug("Output Data: " + profileData.toString());
		// }
		// }
		// }
		// if (profiles.size() <= 0) {
		// reportObj.addMsg("No Profiles in old to import!");
		// } else {
		// reportObj.addMsg("Profiles in old format, " + profiles.size() + ".");
		// // TODO DELETE OLD ONES!
		//
		// // final Preferences baseTree = userPrefs.node("ProfileList");
		//
		// // for (ProfileData profileData : profiles) {
		// // // final String key = profileData.getKey();
		// // // final Preferences child = userPrefs.node(key);
		// // // currentVersion
		// // }
		//
		// }
		// } catch (BackingStoreException e) {
		// LOGGER.error("doImportFromOldVersion: Error: " + e, e);
		// e.printStackTrace();
		// }
		LOGGER.debug("doImportFromOldVersion: Done");
	}

	public ProfileData loadOldProfileObject(final String key) {
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

		// pd.setText("cbDropCapChapters", child.get("cbDropCapChapters",
		// "false"));
		pd.setText("cbRemoveDiv", child.get("cbRemoveDiv", "false"));
		pd.setText("cbCenterStars", child.get("cbCenterStars", "false"));
		// drop chap
		pd.setText("cbWantTextChptOutput", child.get("cbWantTextChptOutput", "false"));
		pd.setText("cbRemoveSectDiv", child.get("cbRemoveSectDiv", "false"));

		// pd.setSelected("cbDropCapChapters",
		// child.getBoolean("cbDropCapChapters", false));
		pd.setSelected("cbRemoveDiv", child.getBoolean("cbRemoveDiv", false));
		pd.setSelected("cbCenterStars", child.getBoolean("cbCenterStars", false));
		final String cbDropCapChaptersS = child.get("cbDropCapChapters", null);
		if (!StringUtils.isBlank(cbDropCapChaptersS) || cbDropCapChaptersS.compareTo("selected") == 0) {
			pd.setText("cbDropCapChapters", "true");
			pd.setSelected("cbDropCapChapters", true);
		} else {
			pd.setText("cbDropCapChapters", "false");
			pd.setSelected("cbDropCapChapters", false);
		}
		pd.setSelected("cbWantTextChptOutput", child.getBoolean("cbWantTextChptOutput", false));
		pd.setSelected("cbRemoveSectDiv", child.getBoolean("cbRemoveSectDiv", false));

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

		pd.setText("outputFileTextCharCard", child.get("outputFileTextCharCard", ""));

		//
		final List<OtherDocTagData> outputs = loadOutputs(child);
		pd.setOutputs(outputs);

		LOGGER.debug("loadOldProfileObject: Done");
		return pd;
	}

	public void setupDaoFromProfileData(final FormatDao formatDao, final ProfileData pr, final Properties appProps) {
		LOGGER.debug("setupDaoFromProfileData: Called");
		// Setup the argument passed
		formatDao.setProfileName(pr.getKey());// TitleOne
		formatDao.setInputFilename(pr.getText("inputFile"));
		formatDao.setOutputFilename(pr.getText("ouputFile"));
		formatDao.setFilePrefix(getFilePrefix(pr));// pr.getText("inputFilePrefix"));
		//
		formatDao.setOutputCountFile(pr.getText("ouputCountFile"));
		formatDao.setOutputOutlineFile(pr.getText("ouputOutlineFile"));
		formatDao.setOutputOutlineFile1(pr.getText("ouputOutlineFile1"));
		//
		formatDao.setWriteChapters(pr.getText("outputDir"));
		formatDao.setWriteChaptersText(pr.getText("outputFormatChpTextDir"));
		//
		formatDao.setStoryTitle1(pr.getText("titleOne"));
		formatDao.setStoryTitle2(pr.getText("titleTwo"));
		// formatDao.setStoryTitle3(pr.getText("titleThree"));
		formatDao.setSeriesTitle(pr.getText("seriesTitle"));
		formatDao.setVolume(pr.getText("volume"));

		formatDao.setFormatMode(pr.getText("fmtMode"));
		//
		// formatDao.setChapterDivider(pr.getText("chpDiv"));
		// formatDao.setSectionDivider(pr.getText("secDiv"));
		formatDao.setRegexpChapter(pr.getText("regexpChapter"));
		formatDao.setRegexpSection(pr.getText("regexpSection"));
		//
		formatDao.setDocTagStart(pr.getText("docTagStart"));
		formatDao.setDocTagEnd(pr.getText("docTagEnd"));
		//
		formatDao.setChapterHeaderTag(pr.getText("chapterHeaderTag"));
		formatDao.setSectionHeaderTag(pr.getText("sectionHeaderTag"));
		//
		// if (cbCenterStars.isSelected())
		formatDao.setCenterStars(true);
		// if (cbDropCapChapters.isSelected())
		formatDao.setDropCapChapter(true);
		// if (cbRemoveDiv.isSelected()) {
		formatDao.setRemoveChptDiv(true);
		formatDao.setRemoveSectDiv(true);
		// }
		// if (cbWantTextChptOutput.isSelected())
		formatDao.setWantTextChptOutput(true);
		//
		formatDao.setOutputEncoding(pr.getText("outputEncoding"));
		final Integer itm = new Integer(pr.getText("counterDigitChoice"));
		formatDao.setOutputFormatDigits(itm);

		//
		//
		formatDao.setOutputDocTagsOutlineFile(pr.getText("outputDocTagsOutlineFile"));
		formatDao.setOutputDocTagsSceneFile(pr.getText("outputDocTagsSceneFile"));
		// formatDao.setOutputDocTagsOther1File(pr.getText("outputDocTagsOther1File"));
		formatDao.setDocTagsOutlineCompressTags(pr.getText("outputDocTagsOutlineCTags"));
		formatDao.setDocTagsOutlineExpandTags(pr.getText("outputDocTagsOutlineTags"));

		formatDao.setDocTagsSceneTags(pr.getText("outputDocTagsSceneTags"));

		formatDao.setDocTagsSceneCoTags(pr.getText("outputDocTagsSceneCoTags"));

		formatDao.setDocTagsScenePrefix(pr.getText("outputDocTagsScenePrefix"));
		formatDao.setDocTagsSubScenePrefix(pr.getText("outputDocTagsSubScenePrefix"));
		//
		// formatDao.setDocTagsOther1Tags(pr.getText("outputDocTagsOther1Tags"));
		formatDao.setSceneCoalateDivider(pr.getText("sceneCoalateDiv"));
		//
		formatDao.setSceneCoalateDivider(pr.getText("sceneCoalateDiv"));

		formatDao.setOutputCharCardFile(pr.getText("outputFileTextCharCard"));

		final String dtmllS = pr.getText("outputDocTagsMaxLineLength");
		Integer dtmllI = 70;
		if (StringUtils.isBlank(dtmllS))
			dtmllI = -1;
		else
			dtmllI = Integer.parseInt(dtmllS);
		formatDao.setDocTagsMaxLineLength(dtmllI);

		//
		formatDao.setOutputs(pr.getOutputs());

		//
		formatDao.setVersion(appProps.getProperty(ProfileBiz.PROP_KEY_VERSION));
		LOGGER.debug("setupDao: Version = " + formatDao.getVersion());
		LOGGER.debug("setupDao: Done");
	}

	private String getFilePrefix(final ProfileData pr) {
		String filePrefixText = null;
		final String fp = pr.getText("inputFilePrefix");
		if (!StringUtils.isBlank(fp)) {
			filePrefixText = fp;
			if (pr.isSelected("appendUnderscoreToPrefix"))
				filePrefixText = filePrefixText + "_";
		}
		return filePrefixText;
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
