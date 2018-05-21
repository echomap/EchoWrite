/**
 * 
 */
package com.echomap.kqf.two;

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.KqfBiz;
import com.echomap.kqf.biz.ProfileBiz;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.ProfileData;
import com.echomap.kqf.looper.FileLooper;
import com.echomap.kqf.two.gui.KQFCtrl;
import com.echomap.kqf.two.gui.WorkDoneNotify;

/**
 * 
 */
public class FormatCli implements WorkDoneNotify {
	private final static Logger LOGGER = LogManager.getLogger(FormatCli.class);
	final Properties appProps;

	public FormatCli() {
		appProps = KqfBiz.initializeAppProperies();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info("KQF Cli started....");

		final FormatCli biz = new FormatCli();
		biz.run(args);

		System.out.println("Format CLI 2 Done");

	}

	private void run(String[] args) {

		try {
			final Options options = setupOptions();
			final CommandLineParser parser = new PosixParser();
			final CommandLine line = parser.parse(options, args);
			final String profileName = line.getOptionValue("p");
			if (!line.hasOption("p")) {
				throw new ParseException("Option p(profilename) is required");
			}

			final Preferences userPrefs = Preferences.userNodeForPackage(KQFCtrl.class);
			final ProfileBiz profileBiz = new ProfileBiz(userPrefs);

			// Setup the argument passed
			final ProfileBiz.ReportObj reportObj = profileBiz.loadProfileData();
			for (String str : reportObj.msgs) {
				LOGGER.info("msg = " + str);
			}

			final ProfileData pr = profileBiz.loadOldProfileObject(profileName);
			final FormatDao formatDao = new FormatDao();
			profileBiz.setupDaoFromProfileData(formatDao, pr, appProps);
			LOGGER.info("FormatDao = " + formatDao);

			doCountAction(formatDao);
			doOutlineAction(formatDao);

		} catch (ParseException e) {
			e.printStackTrace();
			// } catch (IOException e) {
			// e.printStackTrace();
		}
	}

	private Options setupOptions() {
		// create Options object
		final Options options = new Options();
		// Add options
		options.addOption("p", "profilename", true, "profile name");
		return options;
	}

	// private String getCurrentDateFmt() {
	// final Calendar cal = Calendar.getInstance();
	// KQFBaseCtrl.myDateFormat.setTimeZone(cal.getTimeZone());
	// String txt = KQFBaseCtrl.myDateFormat.format(cal.getTime());
	// LOGGER.debug("date = " + txt);
	// return txt;
	// }

	private void doCountAction(final FormatDao formatDao) {
		LOGGER.info("Running count action");
		try {
			// final FormatDao formatDao = new FormatDao();
			// setupDao(formatDao, final ProfileData pr);
			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.count(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//
		}
	}

	private void doOutlineAction(final FormatDao formatDao) {
		LOGGER.info("Running outline action");
		// showMessage("Running Outline Process (" + getCurrentDateFmt() + ")",
		// false);
		try {
			final FileLooper fileLooper = new FileLooper(this);
			fileLooper.outline(formatDao);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//
		}
	}

	@Override
	public void finalResultFromWork(String msg) {
		LOGGER.info("Final result from process " + msg);
	}

	@Override
	public void finishedWithWork(String msg) {
		LOGGER.info("Done with process " + msg);
	}

	@Override
	public void errorWithWork(String msg, Exception e) {
		LOGGER.error("Errorwith process " + msg);
		LOGGER.info(e);
	}

	@Override
	public void errorWithWork(String msg, Throwable e) {
		LOGGER.error("Errorwith process " + msg);
		LOGGER.info(e);
	}

	@Override
	public void statusUpdateForWork(String header, String msg) {
		LOGGER.info("----process " + header + ", " + msg);
	}

}
