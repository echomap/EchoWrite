package com.echomap.kqf.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.profile.persist.ProfileBiz;
import com.echomap.kqf.profile.persist.ProfileData;
import com.echomap.kqf.profile.persist.ProfilePersist;
import com.echomap.kqf.profile.persist.ProfilePersistV1;
import com.echomap.kqf.profile.persist.ProfilePersistV2;
import com.echomap.kqf.two.gui.EmptyTwoClass;

public class ProfileManager {
	private final static Logger LOGGER = LogManager.getLogger(ProfileManager.class);

	private final Preferences userPrefsV1;
	private final Preferences userPrefsV2;
	String appVersion = null;
	private final List<String> messages = new ArrayList<>();
	private Exception error = null;
	private boolean wasError = false;
	private List<Profile> profiles = new ArrayList<>();
	String currentVersion = null;

	public ProfileManager() {
		// try {
		// final Class oldPackage =
		// Class.forName("com.echomap.kqf.two.gui.KQFCtrl");
		// userPrefsV1 = Preferences.userNodeForPackage(oldPackage);
		// } catch (ClassNotFoundException e) {
		// userPrefsV1 = Preferences.userNodeForPackage(KQFCtrl.class);
		// e.printStackTrace();
		// }
		// userPrefsV1 = Preferences.userNodeForPackage(KQFCtrl.class);
		userPrefsV1 = Preferences.userNodeForPackage(EmptyTwoClass.class);
		userPrefsV2 = Preferences.userNodeForPackage(ProfileBiz.class);
		// this.appVersion = appVersion;
		resetData();
	}

	// public ProfileManager(final Preferences userPrefsV1) {
	// this.userPrefsV1 = userPrefsV1;
	// userPrefsV2 = Preferences.userNodeForPackage(ProfileBiz.class);
	// // this.appVersion = appVersion;
	// resetData();
	// }

	public ProfileManager(final Preferences userPrefsV1, final String appVersion) {
		this.userPrefsV1 = userPrefsV1;
		userPrefsV2 = Preferences.userNodeForPackage(ProfilePersist.class);
		this.appVersion = appVersion;
		resetData();
	}

	public void setAppVersion(final String appVersion) {
		this.appVersion = appVersion;
	}

	public List<String> getMessages() {
		return messages;
	}

	public boolean isWasError() {
		return wasError;
	}

	public Exception getError() {
		return error;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	/**
	 * 
	 * @return
	 */
	public boolean loadProfileData() { // throws BackingStoreException {
		LOGGER.debug("loadProfileData: Called");
		resetData();

		LOGGER.debug("Check status of Profile V1");
		int v1Status = checkProfileStatus(userPrefsV1);
		LOGGER.debug("Check status of Profile V2");
		int v2Status = checkProfileStatus(userPrefsV2);
		LOGGER.info("Profile Data V1 Status = " + v1Status);
		LOGGER.info("Profile Data V2 Status = " + v2Status);

		// try to load ProfilePrefsV2, if nothing, load 1
		if (v2Status == 0) {
			// Load V1 data for testing
			// FUTURE: offer to import?
			final ProfilePersistV1 persist = new ProfilePersistV1(userPrefsV1);
			persist.loadProfiles();
			LOGGER.debug("Persist error? " + persist.isWasError());
			final List<String> mgs = persist.getMessages();
			if (mgs != null) {
				for (String msg : mgs) {
					LOGGER.debug("Persist msg?" + msg);
				}
			}
			final List<ProfileData> profileDataList = persist.getProfiles();
			for (ProfileData profileData : profileDataList) {
				LOGGER.debug("Persist profileData=" + profileData);
			}

			// Import from V1 to V2\
			final ProfilePersistV2 persist2 = new ProfilePersistV2();
			for (ProfileData profileData : profileDataList) {
				final Profile profile = persist2.convertFromV1toV2(profileData);
				profiles.add(profile);
			}
			return true;
		} else if (v2Status == 1) {
			LOGGER.info("Load V2 data");
			// Load V2
			final ProfilePersistV2 persist2 = new ProfilePersistV2();
			final List<Profile> profileDataList = persist2.getProfiles();
			for (Profile profile : profileDataList) {
				LOGGER.debug("Persist profile=" + profile);
				profiles.add(profile);
			}
			return true;
		}
		return false;
	}

	private int checkProfileStatus(final Preferences userPrefs) {
		boolean outOfDate = false;
		int status = 0;

		// Check Profile Version #1
		try {
			if (userPrefsV1 != null && userPrefs.keys() != null) {
				LOGGER.debug("loadProfileData: has userPrefs, checking");
				final String currentVerS = userPrefsV1.get("version", null);
				LOGGER.debug("loadProfileData: userPrefs: currentVerS = " + currentVerS);
				if (!StringUtils.isBlank(currentVerS)) {
					LOGGER.debug("loadProfileData: currentVerS: '" + currentVerS + "'");
					final Integer currentVer = Integer.valueOf(currentVerS);
					final Integer appVer = Integer.valueOf(appVersion);
					if (currentVer < appVer) {
						outOfDate = true;
						status = 0;
					}
				} else {
					outOfDate = true;
					status = 0;
				}

				final String[] prefkeys = userPrefs.childrenNames();
				if (prefkeys != null && prefkeys.length > 0)
					status = 1;

				if (prefkeys != null && prefkeys.length > 0) {
					for (final String str1 : prefkeys) {
						LOGGER.debug("loadProfiles: str1 = '" + str1 + "'");
					}
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.warn("Error in V1's version info='" + userPrefs.get("version", null) + "'");
			e.printStackTrace();
		} catch (BackingStoreException e) {
			LOGGER.warn("Error with loading userPrefs data");
			e.printStackTrace();
		}
		LOGGER.debug("loadProfileData: outOfDate = " + outOfDate);
		LOGGER.debug("loadProfileData: status = " + status);
		return status;
	}

	private void resetStatus() {
		this.wasError = false;
		this.messages.clear();
	}

	private void resetData() {
		this.wasError = false;
		this.profiles.clear();
		this.messages.clear();
	}

	public void setupDao(final FormatDao formatDao, final String selectedProfileKey) throws IOException {
		LOGGER.debug("setupDao: Called");
		final Profile selectedProfile = selectProfileByMainTitle(selectedProfileKey);
		setupDao(formatDao, selectedProfile);
	}

	public void setupDao(final FormatDao formatDao, final Profile selectedProfile) throws IOException {
		LOGGER.debug("setupDao: Called");

		if (selectedProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			//
			throw new IOException("Please select a profile before running!!");
			// return;
		}
		final ProfilePersistV2 persist = new ProfilePersistV2();
		persist.setupDao(formatDao, selectedProfile, appVersion);
	}

	public Profile selectProfileByMainTitle(final String profileKey) {
		Profile selectedProfile = null;
		final List<Profile> profiles = getProfiles();
		for (Profile profile : profiles) {
			if (profile.getMainTitle().trim().compareTo(profileKey.trim()) == 0) {
				selectedProfile = profile;
				break;
			}
		}
		return selectedProfile;
	}

	public Profile selectProfileByKey(final String profileKey) {
		Profile selectedProfile = null;
		final List<Profile> profiles = getProfiles();
		for (Profile profile : profiles) {
			if (profile.getKey().compareTo(profileKey) == 0) {
				selectedProfile = profile;
				break;
			}
		}
		return selectedProfile;
	}

	public void saveProfileData(final Profile profile) {
		LOGGER.debug("loadProfileData: Called");
		resetStatus();

		LOGGER.debug("Check status of Profile V1");
		int v1Status = checkProfileStatus(userPrefsV1);
		LOGGER.debug("Check status of Profile V2");
		int v2Status = checkProfileStatus(userPrefsV2);
		LOGGER.info("Profile Data V1 Status = " + v1Status);
		LOGGER.info("Profile Data V2 Status = " + v2Status);

		// try to load ProfilePrefsV2, if nothing, load 1
		if (v2Status == 0) {
			// Import from V1 to V2\
			final ProfilePersistV2 persist2 = new ProfilePersistV2();
			final ProfileData profile1 = persist2.convertFromV2toV1(profile);

			// Load V1 data for testing
			// FUTURE: offer to import?
			final ProfilePersistV1 persist = new ProfilePersistV1(userPrefsV1);
			try {
				persist.saveProfiles(profile1);
				LOGGER.debug("Persist error? " + persist.isWasError());
				final List<String> mgs = persist.getMessages();
				if (mgs != null) {
					for (String msg : mgs) {
						LOGGER.debug("Persist msg?" + msg);
					}
				}
			} catch (BackingStoreException e) {
				e.printStackTrace();
				this.wasError = true;
				this.messages.add(e.getMessage());
				this.error = e;
			}
		} else {
			// Load V2
			// Check Version
		}
		loadProfileData();// TODO ix so refresh not necessary
		LOGGER.debug("loadProfileData: Done");
	}

	public void deleteProfile(final Profile profile) {
		LOGGER.debug("deleteProfileByKey: Called");
		resetStatus();

		LOGGER.debug("Check status of Profile V1");
		int v1Status = checkProfileStatus(userPrefsV1);
		LOGGER.debug("Check status of Profile V2");
		int v2Status = checkProfileStatus(userPrefsV2);
		LOGGER.info("Profile Data V1 Status = " + v1Status);
		LOGGER.info("Profile Data V2 Status = " + v2Status);

		// try to load ProfilePrefsV2, if nothing, load 1
		if (v2Status == 0) {

			final ProfilePersistV1 persist = new ProfilePersistV1(userPrefsV1);
			try {
				persist.deleteProfile(profile.getKey());
				LOGGER.debug("Persist error? " + persist.isWasError());
				final List<String> mgs = persist.getMessages();
				if (mgs != null) {
					for (String msg : mgs) {
						LOGGER.debug("Persist msg?" + msg);
					}
				}
			} catch (BackingStoreException e) {
				e.printStackTrace();
				this.wasError = true;
				this.messages.add(e.getMessage());
				this.error = e;
			}
		} else {
			// Load V2
			// Check Version
		}
		loadProfileData();// TODO ix so refresh not necessary
		LOGGER.debug("deleteProfileByKey: Done");
	}

	public void renameProfile(final Profile profile, final String newKey) {
		LOGGER.debug("renameProfile: Called");
		resetStatus();

		LOGGER.debug("Check status of Profile V1");
		int v1Status = checkProfileStatus(userPrefsV1);
		LOGGER.debug("Check status of Profile V2");
		int v2Status = checkProfileStatus(userPrefsV2);
		LOGGER.info("Profile Data V1 Status = " + v1Status);
		LOGGER.info("Profile Data V2 Status = " + v2Status);

		// try to load ProfilePrefsV2, if nothing, load 1
		if (v2Status == 0) {

			final ProfilePersistV2 persist2 = new ProfilePersistV2();
			final ProfilePersistV1 persist = new ProfilePersistV1(userPrefsV1);
			try {
				final ProfileData profileData = persist2.convertFromV2toV1(profile);
				persist.renameProfile(profileData, profile.getKey(), newKey);
				LOGGER.debug("Persist error? " + persist.isWasError());
				final List<String> mgs = persist.getMessages();
				if (mgs != null) {
					for (final String msg : mgs) {
						LOGGER.debug("Persist msg?" + msg);
					}
				}
			} catch (BackingStoreException e) {
				e.printStackTrace();
				this.wasError = true;
				this.messages.add(e.getMessage());
				this.error = e;
			}
		} else {
			// Load V2
			// Check Version
		}
		loadProfileData();// TODO ix so refresh not necessary
		LOGGER.debug("renameProfile: Done");
	}

	//
	public Profile createDefaultProfile(final String inputKey, final Properties appProps) {
		LOGGER.debug("createDefaultProfile: Called ");
		final Profile profile = new Profile();
		profile.setKey(inputKey);
		//
		profile.setFmtMode(loadPropFromAppOrDefault("defaultOutputFormatting", "", appProps));
		profile.setOutputEncoding(loadPropFromAppOrDefault("defaultEncoding", "", appProps));

		final String sceneM = loadPropFromAppOrDefault("sceneMain", "scene, subscene", appProps);
		final String sceneC = loadPropFromAppOrDefault("sceneCoalate", "description, scene", appProps);
		final String outC = loadPropFromAppOrDefault("outlineCompress", "subscene, outlinedata, outlinesub, suboutline",
				appProps);
		final String outE = loadPropFromAppOrDefault("outineExpand", "outline, scene", appProps);
		profile.setDocTagsOutlineTags(outE);
		profile.setDocTagsOutlineCompressTags(outC);
		profile.setDocTagsSceneTags(sceneM);
		profile.setDocTagsSceneCompressTags(sceneC);

		final String regExpChp = loadPropFromAppOrDefault("regexpChapterText", "", appProps);
		final String regExpSec = loadPropFromAppOrDefault("regexpSectionText", "", appProps);
		profile.setRegexpChapter(regExpChp);
		profile.setRegexpSection(regExpSec);
		//
		LOGGER.debug("createDefaultProfile: Done ");
		return profile;
	}

	private String loadPropFromAppOrDefault(final String key, final String defaultValue, final Properties appProps) {
		if (appProps != null) {
			final String value = appProps.getProperty(key);
			return value;
		}
		return defaultValue;
	}

}
