package com.echomap.kqf.view;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.datamgr.DataManagerBiz;
import com.echomap.kqf.view.ctrl.BaseCtrl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainFrame extends Application {
	private final static Logger LOGGER = LogManager.getLogger(MainFrame.class);

	final static Map<String, String> fxmlFrames = new HashMap<>();
	@SuppressWarnings("rawtypes")
	final static Map<String, Class> fxmlCtrl = new HashMap<>();
	/** "cwc2.properties" */
	final static Properties appProps = new Properties();
	/** userNodeForPackage(MainFrame.class); like width, etc */
	Preferences appPreferences = null;
	//
	public final static String WINDOW_TITLE_FMT = "EchoWrite: A simple parser for safely documenting writing inside the source text (v%s)";
	// a Quick Text Formatter and Markup Aid for Writing
	// "Kindle (Ebook) Quick Formatter (MYKFEQF v%s)";

	// private static DataManagerBiz DATA_MANAGER =
	// DataManagerBiz.getDataManager();

	public static void main(String[] args) {
		Application.launch(MainFrame.class, args);
	}

	public final static Map<String, String> getFxmlFrames() {
		return fxmlFrames;
	}

	public final static Map<String, Class> getFxmlCtrl() {
		return fxmlCtrl;
	}

	public MainFrame() {
		fxmlFrames.put(Base.WINDOWKEY_MAINWINDOW, Base.FXML_START);
		fxmlFrames.put(Base.WINDOWKEY_PROFILE_NEW, Base.FXML_NEWPROFILE);
		fxmlFrames.put(Base.WINDOWKEY_PROFILE_EDIT, Base.FXML_EDITPROFILE);
		fxmlFrames.put(Base.WINDOWKEY_PROFILE_DELETE, Base.FXML_DELETEPROFILE);
		fxmlFrames.put(Base.WINDOWKEY_EXPORT, Base.SUB_EXPORT);
		fxmlFrames.put(Base.WINDOWKEY_IMPORT, Base.SUB_IMPORT);
		fxmlFrames.put(Base.WINDOWKEY_MOREFILES, Base.FXML_MOREFILES);
		fxmlFrames.put(Base.WINDOWKEY_TIMELINE, Base.FXML_TIMELINE);
		fxmlFrames.put(Base.WINDOWKEY_OUTLINERGUI, Base.FXML_OUTLINERGUI);
		fxmlFrames.put(Base.WINDOWKEY_VIEWCHARS, Base.FXML_VIEWCHARS);

		fxmlFrames.put(Base.WINDOWKEY_EXTERNALLINKS, Base.FXML_EXTERNALLINKS);
		fxmlFrames.put(Base.WINDOWKEY_EXTERNALIDS, Base.FXML_EXTERNALIDS);

		fxmlCtrl.put(Base.WINDOWKEY_PROFILE_NEW, com.echomap.kqf.view.ctrl.CtrlProfileEdit.class);
		fxmlCtrl.put(Base.WINDOWKEY_PROFILE_EDIT, com.echomap.kqf.view.ctrl.CtrlProfileEdit.class);
		fxmlCtrl.put(Base.WINDOWKEY_PROFILE_DELETE, com.echomap.kqf.view.ctrl.CtrlProfileEdit.class);
		// fxmlCtrl.put(key, value)

		appPreferences = Preferences.userNodeForPackage(MainFrame.class);

		InputStream propertyStreamLoad = null;
		try {
			propertyStreamLoad = MainFrame.class.getClassLoader().getResourceAsStream("cwc2.properties");
			if (propertyStreamLoad != null)
				appProps.load(propertyStreamLoad);
		} catch (IOException e) {
			e.printStackTrace();
			appProps.setProperty("version", "0.0.0");
		} finally {
			try {
				propertyStreamLoad.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("Version: " + appProps.getProperty("version"));
		// TODO DATA_MANAGER.setVersion(appProps.getProperty("version"));
	}

	@Override
	public void start(final Stage primaryStage) {
		Parent parent = null;
		final FXMLLoader fxmlLoader = new FXMLLoader();
		try {
			// parent = fxmlLoader.load(CWCFrame.class.getResource(FXML_FILE));

			final Parameters appParameters = getParameters();
			final Map<String, String> namedParams = appParameters.getNamed();
			final String modeStartup = namedParams.containsKey("mode") ? namedParams.get("mode") : null;

			final URL location;
			if (modeStartup == null)
				location = getClass().getResource(fxmlFrames.get(Base.WINDOWKEY_MAINWINDOW));
			else if (modeStartup.compareToIgnoreCase(Base.START_PARAM_TIMELINE) == 0)
				location = getClass().getResource(fxmlFrames.get(Base.WINDOWKEY_TIMELINE));
			else if (modeStartup.compareToIgnoreCase(Base.START_PARAM_OUTLINE) == 0)
				location = getClass().getResource(fxmlFrames.get(Base.WINDOWKEY_OUTLINERGUI));
			else if (modeStartup.compareToIgnoreCase(Base.START_PARAM_VIEWCHARS) == 0)
				location = getClass().getResource(fxmlFrames.get(Base.WINDOWKEY_VIEWCHARS));

			else
				location = getClass().getResource(fxmlFrames.get(Base.WINDOWKEY_MAINWINDOW));

			if (location == null) {
				LOGGER.error("Failed to get location!!!!!!");
			}
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			parent = (Parent) fxmlLoader.load(location.openStream());

			primaryStage.setTitle(String.format(WINDOW_TITLE_FMT, appProps.getProperty("version")));
			// "Kindle (Ebook) Quick Formatter (MYKFEQF v" +
			// appProps.getProperty("version") + ")");
			// primaryStage.setWidth(1024);
			// primaryStage.setHeight(200);

			final Scene scene = new Scene(parent);
			primaryStage.setScene(scene);

			//
			final Map<String, Object> namedParams2 = new HashMap<>();
			final Set<String> nSet = namedParams.keySet();
			final Iterator<String> iter = nSet.iterator();
			while (iter.hasNext()) {
				final String key = iter.next();
				final String val = namedParams.get(key);
				namedParams2.put(key, val);
			}
			namedParams2.put("FRAMENAME", modeStartup == null ? "Main" : modeStartup);
			final BaseCtrl myController = (BaseCtrl) fxmlLoader.getController();
			myController.setupController(appProps, appPreferences, primaryStage, namedParams2);

			// Setup event callbacks for scene
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					LOGGER.info("Stage is cleaning up...");
					myController.doSceneCloseRequest();
					myController.doCleanup();
					LOGGER.info("Stage is closing");
				}
			});

			primaryStage.setOnShown(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					LOGGER.info("Stage is being shown...");
					myController.doSceneShown();
					LOGGER.info("Stage is shown");
				}
			});
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					LOGGER.info("Stage has been requested to close...");
					myController.doSceneCloseRequested();
				}
			});
			primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					LOGGER.info("Stage is hidding...");
					myController.doSceneHidden();
					LOGGER.info("Stage is hidden");

					LOGGER.debug("windowCount=" + BaseCtrl.getWindowCount());
					if (BaseCtrl.getWindowCount() <= 0) {
						LOGGER.info("Closing platform");
						DataManagerBiz.close();//DATA_MANAGER.close();
						Platform.exit();
					}
				}
			});
			primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					LOGGER.info("Stage is being hidden...");
					myController.doSceneHiding();
				}
			});
			primaryStage.setOnShowing(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					LOGGER.info("Stage is being shown...");
					myController.doSceneShowing();
					LOGGER.info("Stage is shown");
				}
			});
			//
			primaryStage.show();
			//
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Platform.exit();
		} catch (Throwable e) {
			e.printStackTrace();
			Platform.exit();
		}
	}
}
