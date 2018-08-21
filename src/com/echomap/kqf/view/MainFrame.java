package com.echomap.kqf.view;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.looper.FileLooperHandlerFormatter;

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

	private static final String FXML_START = "/viewstart2.fxml";
	private static final String NEWProfile = "/viewstart2.fxml";
	final static Map<String, String> fxmlFrames = new HashMap<>();

	final static Properties appProps = new Properties();
	Preferences appPreferences = null;

	public static void main(String[] args) {
		Application.launch(MainFrame.class, args);
	}

	public MainFrame() {
		fxmlFrames.put("MainWindow", FXML_START);
		fxmlFrames.put("NEWProfile", NEWProfile);

		appPreferences = Preferences.userNodeForPackage(MainFrame.class);

		InputStream asdf = null;
		try {
			asdf = FileLooperHandlerFormatter.class.getClassLoader().getResourceAsStream("cwc.properties");
			if (asdf != null)
				appProps.load(asdf);
		} catch (IOException e) {
			e.printStackTrace();
			appProps.setProperty("version", "0.0.0");
		} finally {
			try {
				asdf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("Version: " + appProps.getProperty("version"));
	}

	@Override
	public void start(final Stage primaryStage) {
		Parent parent = null;
		final FXMLLoader fxmlLoader = new FXMLLoader();
		try {
			// parent = fxmlLoader.load(CWCFrame.class.getResource(FXML_FILE));
			final URL location = getClass().getResource(FXML_START);
			if (location == null) {
				System.out.println("Failed to get location!!!!!!");
			}
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			parent = (Parent) fxmlLoader.load(location.openStream());

			primaryStage.setTitle("Kindle (Ebook) Quick Formatter (MYKFEQF v" + appProps.getProperty("version") + ")");
			// primaryStage.setWidth(1024);
			// primaryStage.setHeight(200);

			final Scene scene = new Scene(parent);
			primaryStage.setScene(scene);

			//
			final BaseCtrl myController = (BaseCtrl) fxmlLoader.getController();
			myController.setupController(appProps, appPreferences, primaryStage);

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					System.out.println("Stage is cleaning up...");
					// myController.saveProps();
					myController.doCleanup();
					System.out.println("Stage is closing");
				}
			});

			primaryStage.show();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Platform.exit();
		} catch (Throwable e) {
			e.printStackTrace();
			Platform.exit();
		}
	}
}
