package com.echomap.kqf.view;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.Profile;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.two.gui.WorkDoneNotify;

import javafx.scene.control.TextArea;

public class BaseRunner {
	private final static Logger LOGGER = LogManager.getLogger(BaseRunner.class);
	boolean runningMutex = false;
	// ProfileManager profileManager = null;
	// ComboBox<String> titleComboText = null;
	// TextArea loggingText = null;
	// WorkDoneNotify notifyCtrl = null;

	public void handleRunCounter(final BaseCtrl baseCtrl, final ProfileManager profileManager,
			final Profile selectedProfile, final TextArea loggingText, final WorkDoneNotify notifyCtrl) {
		LOGGER.debug("handleRunCounter: Called");
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}

		if (selectedProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			loggingText.appendText("Please select a profile before running!!");
			// notifyCtrl.errorWithWork("Counter", "Please select a profile
			// before running!!");
			return;
		}

		baseCtrl.lockGui();
		this.runningMutex = true;

		LOGGER.info("Running COUNT action");
		baseCtrl.showMessage("Running COUNT Process (" + BaseCtrl.getCurrentDateFmt() + ")", false, loggingText);
		// timer.cancel();
		// timer = new Timer();
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, selectedProfile);
			final FileLooper fileLooper = new FileLooper(notifyCtrl);
			fileLooper.count(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// baseCtrl.unlockGui();
			String fmtt = "Done running COUNT ( " + BaseCtrl.getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
			// setLastRunText(fmtt);
			// startTimerTask();
		}
		LOGGER.debug("handleRunCounter: Done");
	}

	public void handleRunOutliner(final BaseCtrl baseCtrl, final ProfileManager profileManager,
			final Profile selectedProfile, final TextArea loggingText, final WorkDoneNotify notifyCtrl) {
		LOGGER.debug("handleRunOutliner: Called");
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}

		if (selectedProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			loggingText.appendText("Please select a profile before running!!");
			// notifyCtrl.errorWithWork("Counter", "Please select a profile
			// before running!!");
			return;
		}

		baseCtrl.lockGui();
		this.runningMutex = true;

		LOGGER.info("Running OUTLINE action");
		baseCtrl.showMessage("Running OUTLINE Process (" + BaseCtrl.getCurrentDateFmt() + ")", false, loggingText);
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, selectedProfile);
			final FileLooper fileLooper = new FileLooper(notifyCtrl);
			fileLooper.outline(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// baseCtrl.unlockGui();
			String fmtt = "Done running OUTLINE ( " + BaseCtrl.getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
		}
		LOGGER.debug("handleRunOutliner: Done");
	}

	public void handleRunFormatter(final BaseCtrl baseCtrl, final ProfileManager profileManager,
			final Profile selectedProfile, final TextArea loggingText, final WorkDoneNotify notifyCtrl) {
		LOGGER.debug("handleRunFormatter: Called");
		if (runningMutex) {
			LOGGER.debug("Something is already running!!");
			return;
		}

		if (selectedProfile == null) {
			LOGGER.debug("Please select a profile before running!!");
			loggingText.appendText("Please select a profile before running!!");
			// notifyCtrl.errorWithWork("Counter", "Please select a profile
			// before running!!");
			return;
		}

		baseCtrl.lockGui();
		this.runningMutex = true;

		LOGGER.info("Running FORMAT action");
		baseCtrl.showMessage("Running FORMAT Process (" + BaseCtrl.getCurrentDateFmt() + ")", false, loggingText);
		try {
			final FormatDao formatDao = new FormatDao();
			profileManager.setupDao(formatDao, selectedProfile);
			final FileLooper fileLooper = new FileLooper(notifyCtrl);
			fileLooper.format(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// baseCtrl.unlockGui();
			String fmtt = "Done running FORMAT ( " + BaseCtrl.getCurrentDateFmt() + ")";
			LOGGER.debug("fmtt: " + fmtt);
		}
		LOGGER.debug("handleRunFormatter: Done");
	}

}
