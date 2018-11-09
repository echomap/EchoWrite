package com.echomap.kqf.biz;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.Profile;

public class ProfileManagerTest {

	// ProfileManager profileManagerG = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		final ProfileManager profileManager = new ProfileManager();
		final Profile profile = new Profile();
		profile.setMainTitle("TESTING TESTING TITLE ABC");
		profile.setKey("TESTING TESTING KEY ABC");
		profile.setSeriesTitle("TESTING");
		profileManager.saveProfileData(profile);
		final Exception ex = profileManager.getError();
		final List<String> msgs = profileManager.getMessages();
		final boolean isError = profileManager.isWasError();
		if (isError) {
			fail("Is error:" + ex);
		}
		if (ex != null) {
			fail("Is error:" + ex);
		}
		if (msgs == null || msgs.size() > 0) {
			fail("Has msgs");
		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			final ProfileManager profileManager = new ProfileManager();
			final Profile profile = new Profile();
			profile.setMainTitle("TESTING TESTING TITLE ABC");
			profile.setKey("TESTING TESTING KEY ABC");
			profile.setSeriesTitle("TESTING");
			profileManager.deleteProfile(profile);
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testProfileManager() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testSetAppVersion() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.setAppVersion("TESTINGAPPVERSION");
			// profileManager.get
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testGetMessages() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			final List<String> msgs = profileManager.getMessages();
			if (msgs == null) {

			}
			// profileManager.get
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testIsWasError() {
		final ProfileManager profileManager = new ProfileManager();
		final boolean isErr = profileManager.isWasError();
		if (isErr) {
			fail("Should not initilize as error");
		}
	}

	@Test
	public final void testGetError() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			final Exception ex = profileManager.getError();
			if (ex != null) {
				fail("Error=" + ex.getMessage());
			}
			// profileManager.get
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testGetProfiles() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testLoadProfileData() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();

			final List<Profile> profiles = profileManager.getProfiles();
			final Exception ex = profileManager.getError();
			final List<String> msgs = profileManager.getMessages();
			final boolean isError = profileManager.isWasError();

			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}
			if (profiles == null || profiles.size() < 1) {
				fail("Has no profiles");
			}
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testSetupDaoFormatDaoStringNOSEL() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			final FormatDao formatDao = new FormatDao();
			final String selectedProfileKey = "TEST 123";

			profileManager.setupDao(formatDao, selectedProfileKey);
			fail("Should have failed!");
		} catch (Exception e) {
			// fail("Ex=" + e.getMessage());
			// e.printStackTrace();
		}
	}

	@Test
	public final void testSetupDaoFormatDaoString() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();
			final FormatDao formatDao = new FormatDao();
			final String selectedProfileKey = "TESTING TESTING TITLE ABC";

			profileManager.setupDao(formatDao, selectedProfileKey);
			Assert.assertEquals("Title 1 not right", "TESTING TESTING TITLE ABC", formatDao.getStoryTitle1());
			Assert.assertEquals("Title 2 not right", "", formatDao.getStoryTitle2());
			Assert.assertEquals("Series not right", "TESTING", formatDao.getSeriesTitle());
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testSetupDaoFormatDaoProfile() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();
			final FormatDao formatDao = new FormatDao();
			final Profile profile = new Profile();
			profile.setMainTitle("TESTING TESTING TITLE ABC");
			profile.setKey("TESTING TESTING KEY ABC");
			profile.setSeriesTitle("TESTING");
			// final String selectedProfileKey = "TESTING TESTING ABC";

			profileManager.setupDao(formatDao, profile);
			Assert.assertEquals("Title 1 not right", "TESTING TESTING TITLE ABC", formatDao.getStoryTitle1());
			Assert.assertEquals("Title 2 not right", null, formatDao.getStoryTitle2());
			Assert.assertEquals("Series not right", "TESTING", formatDao.getSeriesTitle());
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testSelectProfileByMainTitle() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();
			final String profileKey = "TESTING TESTING TITLE ABC";
			final Profile profile = profileManager.selectProfileByMainTitle(profileKey);

			Assert.assertEquals("Title 1 not right", "TESTING TESTING TITLE ABC", profile.getMainTitle());
			Assert.assertEquals("Title 2 not right", "", profile.getSubTitle());
			Assert.assertEquals("Series not right", "TESTING", profile.getSeriesTitle());
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testSelectProfileByKey() {
		try {
			final ProfileManager profileManager = new ProfileManager();
			profileManager.loadProfileData();
			final String profileKey = "TESTING TESTING KEY ABC";
			final Profile profile = profileManager.selectProfileByKey(profileKey);

			Assert.assertEquals("Title 1 not right", "TESTING TESTING TITLE ABC", profile.getMainTitle());
			Assert.assertEquals("Title 2 not right", "", profile.getSubTitle());
			Assert.assertEquals("Series not right", "TESTING", profile.getSeriesTitle());
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public final void testSaveProfileData() {
		ProfileManager profileManager = null;
		Profile profile = null;
		try {
			profileManager = new ProfileManager();
			profileManager.loadProfileData();

			// Listing
			List<Profile> profiles = profileManager.getProfiles();
			Exception ex = profileManager.getError();
			List<String> msgs = profileManager.getMessages();
			boolean isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}
			if (profiles == null || profiles.size() < 1) {
				fail("Has no profiles");
			}

			// Add profile
			profile = new Profile();
			profile.setMainTitle("TESTING TESTING TITLE 234");
			profile.setKey("TESTING TESTING KEY 234");
			profile.setSeriesTitle("TESTING");
			profileManager.saveProfileData(profile);
			profiles = profileManager.getProfiles();
			ex = profileManager.getError();
			msgs = profileManager.getMessages();
			isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}

			boolean foundProfile = false;
			for (final Profile profile2 : profiles) {
				if ("TESTING TESTING KEY 234".equals(profile2.getKey()))
					foundProfile = true;
			}
			if (!foundProfile) {
				fail("Failed to find NEW profile!");
			}

			//
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (profileManager != null) {
				profile.setKey("TESTING TESTING KEY 234");
				profileManager.deleteProfile(profile);
				final List<Profile> profiles = profileManager.getProfiles();
				final Exception ex = profileManager.getError();
				final List<String> msgs = profileManager.getMessages();
				final boolean isError = profileManager.isWasError();
				if (isError) {
					fail("Is error:" + ex);
				}
				if (ex != null) {
					fail("Is error:" + ex);
				}
				if (msgs == null || msgs.size() > 0) {
					fail("Has msgs");
				}
				if (profiles == null || profiles.size() < 1) {
					fail("Has no profiles");
				}
				boolean foundProfile = false;
				for (final Profile profile2 : profiles) {
					if ("TESTING TESTING KEY 234".equals(profile2.getKey()))
						foundProfile = true;
				}
				if (foundProfile) {
					fail("FOUND NEW profile!");
				}
			} else {
				fail("profileManager is null!");
			}
		}
	}

	@Test
	public final void testDeleteProfile() {
		ProfileManager profileManager = null;
		Profile profile = null;
		try {
			profileManager = new ProfileManager();
			profileManager.loadProfileData();

			// Listing
			List<Profile> profiles = profileManager.getProfiles();
			Exception ex = profileManager.getError();
			List<String> msgs = profileManager.getMessages();
			boolean isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}
			if (profiles == null || profiles.size() < 1) {
				fail("Has no profiles");
			}

			// Add profile
			profile = new Profile();
			profile.setMainTitle("TESTING TESTING TITLE 234");
			profile.setKey("TESTING TESTING KEY 234");
			profile.setSeriesTitle("TESTING");
			profileManager.saveProfileData(profile);
			profiles = profileManager.getProfiles();
			ex = profileManager.getError();
			msgs = profileManager.getMessages();
			isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}

			boolean foundProfile = false;
			for (final Profile profile2 : profiles) {
				if ("TESTING TESTING KEY 234".equals(profile2.getKey()))
					foundProfile = true;
			}
			if (!foundProfile) {
				fail("Failed to find NEW profile!");
			}

			profileManager.deleteProfile(profile);
			profiles = profileManager.getProfiles();
			ex = profileManager.getError();
			msgs = profileManager.getMessages();
			isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}
			foundProfile = false;
			for (final Profile profile2 : profiles) {
				if ("TESTING TESTING KEY 234".equals(profile2.getKey()))
					foundProfile = true;
			}
			if (foundProfile) {
				fail("FOUND NEW profile!");
			}
			//
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (profileManager != null) {
				profile.setKey("TESTING TESTING KEY 234");
				profileManager.deleteProfile(profile);
				final List<Profile> profiles = profileManager.getProfiles();
				final Exception ex = profileManager.getError();
				final List<String> msgs = profileManager.getMessages();
				final boolean isError = profileManager.isWasError();
				if (isError) {
					fail("Is error:" + ex);
				}
				if (ex != null) {
					fail("Is error:" + ex);
				}
				if (msgs == null || msgs.size() > 0) {
					fail("Has msgs");
				}
				if (profiles == null || profiles.size() < 1) {
					fail("Has no profiles");
				}
				boolean foundProfile = false;
				for (final Profile profile2 : profiles) {
					if ("TESTING TESTING KEY 234".equals(profile2.getKey()))
						foundProfile = true;
				}
				if (foundProfile) {
					fail("FOUND NEW profile!");
				}
			} else {
				fail("profileManager is null!");
			}
		}
	}

	@Test
	public final void testRenameProfile() {
		ProfileManager profileManager = null;
		Profile profile = null;
		try {
			profileManager = new ProfileManager();
			profileManager.loadProfileData();

			List<Profile> profiles = profileManager.getProfiles();
			Exception ex = profileManager.getError();
			List<String> msgs = profileManager.getMessages();
			boolean isError = profileManager.isWasError();

			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}
			if (profiles == null || profiles.size() < 1) {
				fail("Has no profiles");
			}

			profile = new Profile();
			profile.setMainTitle("TESTING TESTING 123");
			profile.setKey("TESTING TESTING 123");
			profile.setSeriesTitle("TESTING");
			profileManager.saveProfileData(profile);
			ex = profileManager.getError();
			msgs = profileManager.getMessages();
			isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}

			profileManager.renameProfile(profile, "TESTING TESTING 321");
			profile.setKey("TESTING TESTING 321");
			final List<Profile> profiles2 = profileManager.getProfiles();
			ex = profileManager.getError();
			msgs = profileManager.getMessages();
			isError = profileManager.isWasError();
			if (isError) {
				fail("Is error:" + ex);
			}
			if (ex != null) {
				fail("Is error:" + ex);
			}
			if (msgs == null || msgs.size() > 0) {
				fail("Has msgs");
			}
			if (profiles2 == null || profiles2.size() < 1) {
				fail("Has no profiles");
			}

			boolean foundNewProfile = false;
			boolean foundOldProfile = false;
			for (final Profile profile2 : profiles2) {
				if ("TESTING TESTING 321".equals(profile2.getKey())) {
					foundNewProfile = true;
				}
				if ("TESTING TESTING 123".equals(profile2.getKey())) {
					foundOldProfile = true;
				}
			}
			if (!foundNewProfile) {
				fail("Failed to find renamed profile!");
			}
			if (foundOldProfile) {
				fail("Found old profile!");
			}
		} catch (Exception e) {
			fail("Ex=" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (profileManager != null) {
				profile.setKey("TESTING TESTING 321");
				profileManager.deleteProfile(profile);
				final List<Profile> profiles = profileManager.getProfiles();
				final Exception ex = profileManager.getError();
				final List<String> msgs = profileManager.getMessages();
				final boolean isError = profileManager.isWasError();
				if (isError) {
					fail("Is error:" + ex);
				}
				if (ex != null) {
					fail("Is error:" + ex);
				}
				if (msgs == null || msgs.size() > 0) {
					fail("Has msgs");
				}
				if (profiles == null || profiles.size() < 1) {
					fail("Has no profiles");
				}
				boolean foundNewProfile = false;
				boolean foundOldProfile = false;
				for (final Profile profile2 : profiles) {
					if ("TESTING TESTING 321".equals(profile2.getKey())) {
						foundNewProfile = true;
					}
					if ("TESTING TESTING 123".equals(profile2.getKey())) {
						foundOldProfile = true;
					}
				}
				if (foundNewProfile) {
					fail("Found renamed profile!");
				}
				if (foundOldProfile) {
					fail("Found old profile!");
				}
			} else {
				fail("profileManager is null!");
			}
		}
	}

}
