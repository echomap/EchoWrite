package com.echomap.kqf.view;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.two.gui.KQFBaseCtrl;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public abstract class BaseCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFBaseCtrl.class);
	public static final String PROP_KEY_VERSION = "version";
	static public final DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	Preferences appPreferences = null;
	Properties appProps = null;
	String appVersion = null;
	// public Window parentWindow = null;
	private Stage primaryStage = null;

	public BaseCtrl() {
		// appPreferences = Preferences.userNodeForPackage(BaseCtrl.class);
	}

	String loadPropFromAppOrDefault(final String key, final String defaultValue) {
		if (appProps != null && appProps.containsKey(PROP_KEY_VERSION)) {
			final String value = appProps.getProperty(key);
			return value;
		}
		return defaultValue;
	}

	String getCurrentDateFmt() {
		final Calendar cal = Calendar.getInstance();
		myDateFormat.setTimeZone(cal.getTimeZone());
		String txt = myDateFormat.format(cal.getTime());
		LOGGER.debug("date = " + txt);
		return txt;
	}

	void showMessage(final String msg, final boolean clearPrevious, TextArea outputArea) {
		final Animation animation = new Transition() {
			{
				setCycleDuration(Duration.millis(2000));
				setInterpolator(Interpolator.EASE_OUT);
			}

			@Override
			protected void interpolate(double frac) {
				Color vColor = new Color(1, 0, 0, 1 - frac);
				outputArea.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
			}
		};
		animation.play();

		if (clearPrevious) {
			outputArea.setText(msg);
		} else {
			outputArea.setText(msg + "\r\n" + outputArea.getText());
		}
		LOGGER.info(msg);
	}

	void openNewWindow(String windowName, String windowTitle, final TextArea reportArea, Window owner) {
		LOGGER.debug("openNewWindow: Called");
		// Parent root;
		if (StringUtils.isEmpty(windowName))
			windowName = "MainWindow";
		if (StringUtils.isEmpty(windowTitle))
			windowTitle = "MainWindow";
		try {
			final String fxmlFile = MainFrame.fxmlFrames.get(windowName);
			LOGGER.debug("openNewWindow: fxmlFile=" + fxmlFile);
			// check fxmlFile
			final FXMLLoader fxmlLoader = new FXMLLoader();
			final URL location = getClass().getResource(fxmlFile);
			if (location == null) {
				LOGGER.error("Failed to get location!!!!!!");
				showMessage("ERROR loading FXML file for '" + windowTitle + "' screen", false, reportArea);
				return;
			}
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			final Parent root = (Parent) fxmlLoader.load(location.openStream());

			final Stage stage = new Stage();
			// check windowTitle
			stage.setTitle(windowTitle);
			stage.setScene(new Scene(root));
			if (owner == null)
				owner = primaryStage;
			stage.initOwner(owner);// ((Node)
									// (event.getSource())).getScene().getWindow());
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(final WindowEvent we) {
					LOGGER.debug("SubStage is cleaning up...");
					// myController.saveProps();
					// myController.doCleanup();
					LOGGER.debug("SubStage is closing");
					stage.close();
				}
			});
			//
			// final String key = titleOneText.getValue();
			// LOGGER.debug("Key = '" + key + "'");
			// final Preferences child = getPrefs().node(key);
			//
			final BaseCtrl myController = (BaseCtrl) fxmlLoader.getController();
			// myController.setupController(props);

			// final FormatDao formatDao = new FormatDao();
			// setupDao(formatDao);
			// myController.setProfileLoaded(child, formatDao, appProps, stage);

			stage.showAndWait();
			// stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e);
		}
	}

	// virtual
	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage) {
		this.appProps = props;
		this.appPreferences = appPreferences;
		this.primaryStage = primaryStage;
		this.appVersion = appProps.getProperty(PROP_KEY_VERSION);
		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			this.windowWidthChanged(newVal);
		});
		primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
			this.windowHeightChanged(newVal);
		});
		primaryStage.xProperty().addListener((obs, oldVal, newVal) -> {
			this.windowXChanged(newVal);
		});
		primaryStage.yProperty().addListener((obs, oldVal, newVal) -> {
			this.windowYChanged(newVal);
		});
		if (appPreferences != null) {
			final double windowY = appPreferences.getDouble("view/WindowY", -1);
			final double windowX = appPreferences.getDouble("view/WindowX", -1);
			final double windowW = appPreferences.getDouble("view/WindowW", -1);
			final double windowH = appPreferences.getDouble("view/WindowH", -1);
			if (windowY > -1)
				primaryStage.setY(windowY);
			if (windowX > -1)
				primaryStage.setX(windowX);
			if (windowW > -1)
				primaryStage.setWidth(windowW);
			if (windowH > -1)
				primaryStage.setHeight(windowH);
		}
	}

	private void windowYChanged(final Number newVal) {
		if (appPreferences != null) {
			appPreferences.putDouble("view/WindowY", newVal.doubleValue());
		}
	}

	private void windowXChanged(final Number newVal) {
		if (appPreferences != null) {
			appPreferences.putDouble("view/WindowX", newVal.doubleValue());
		}
	}

	private void windowHeightChanged(final Number newVal) {
		if (appPreferences != null) {
			appPreferences.putDouble("view/WindowH", newVal.doubleValue());
		}
	}

	private void windowWidthChanged(final Number newVal) {
		if (appPreferences != null) {
			appPreferences.putDouble("view/WindowW", newVal.doubleValue());
		}
	}

	abstract void doCleanup();

	abstract void lockGui();

	abstract void unlockGui();

}
