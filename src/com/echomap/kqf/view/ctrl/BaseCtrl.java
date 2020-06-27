package com.echomap.kqf.view.ctrl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.looper.WorkDoneNotify;
import com.echomap.kqf.view.MainFrame;
import com.echomap.kqf.view.gui.ConfirmResult;
import com.sun.javafx.scene.control.skin.TableViewSkin;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public abstract class BaseCtrl {
	static private final Logger LOGGER = LogManager.getLogger(BaseCtrl.class);

	static private int windowCount = 0;

	/** Loaded from file "cwc2.properties" */
	Properties appProps;
	/** Loaded from OS, userNodeForPackage(MainFrame.class), like width, etc */
	Preferences appPreferences = null;

	/** config params */
	Map<String, Object> paramsMap;
	//
	ResourceBundle messageBundle = null;

	//
	String appVersion = null;
	Stage primaryStage = null;
	boolean profileChangeMade = false;

	// String lastNofificationMsg = null;
	private static Method columnToFitMethod;

	static {
		try {
			columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class,
					int.class);
			columnToFitMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public BaseCtrl() {
		// appPreferences = Preferences.userNodeForPackage(BaseCtrl.class);
		// messageBundle = cwc2messages.properties
	}

	final public static int getWindowCount() {
		return windowCount;
	}

	final void loadPreferencesForWindow(final String keyP, final Stage stage) {
		//
		final String keyY = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_Y);
		final double sceneY = appPreferences.getDouble(keyY, -1);
		LOGGER.debug("loadPreferences: keyY='" + keyY + "' val=" + sceneY);
		if (sceneY > -1) {
			stage.setY(sceneY);
		}
		//
		final String keyX = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_X);
		final double sceneX = appPreferences.getDouble(keyX, -1);
		LOGGER.debug("loadPreferences: keyX='" + keyX + "' val=" + sceneX);
		if (sceneX > -1) {
			stage.setX(sceneX);
		}
		//
		final String keyH = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_HEIGHT);
		final double sceneHeight = appPreferences.getDouble(keyH, -1);
		LOGGER.debug("loadPreferences: keyH='" + keyH + "' val=" + sceneHeight);
		if (sceneHeight > -1) {
			stage.setHeight(sceneHeight);
		}
		//
		final String keyW = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_WIDTH);
		final double sceneWidth = appPreferences.getDouble(keyW, -1);
		LOGGER.debug("loadPreferences: keyW='" + keyW + "' val=" + sceneWidth);
		if (sceneWidth > -1) {
			stage.setWidth(sceneWidth);
		}
	}

	final void savePreferencesForWindow(final String keyP, final Stage stage) {
		//
		final double sceneX = stage.getX();
		final String keyX = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_X);
		if (sceneX > 0) {
			LOGGER.debug("savePreferences: keyX='" + keyX + "' X=" + sceneX);
			if (appPreferences != null)
				appPreferences.putDouble(keyX, sceneX);
		}
		//
		final double sceneY = stage.getY();
		final String keyY = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_Y);
		if (sceneY > 0) {
			LOGGER.debug("savePreferences: keyY='" + keyY + "' Y=" + sceneY);
			if (appPreferences != null)
				appPreferences.putDouble(keyY, sceneY);
		}
		//
		final double sceneWidth = stage.getWidth();
		final String keyW = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_WIDTH);
		if (sceneWidth > 0) {
			LOGGER.debug("savePreferences: keyW='" + keyW + "' width=" + sceneWidth);
			if (appPreferences != null)
				appPreferences.putDouble(keyW, sceneWidth);
		}
		//
		final double sceneHeight = stage.getHeight();
		final String keyH = String.format("%s/%s", keyP, EchoWriteConst.WINDOW_PREF_HEIGHT);
		if (sceneHeight > 0) {
			LOGGER.debug("savePreferences: keyH='" + keyH + "' height=" + sceneHeight);
			if (appPreferences != null)
				appPreferences.putDouble(keyH, sceneHeight);
		}
	}

	final String loadPropFromAppOrDefault(final String key, final String defaultValue) {
		if (appProps != null && appProps.containsKey(EchoWriteConst.PROP_KEY_VERSION)) {
			final String value = appProps.getProperty(key);
			return value;
		}
		return defaultValue;
	}

	final void setLastSelectedDirectory(final File lastDir) {
		if (appPreferences != null) {
			appPreferences.put("LastSelectedDirectory", lastDir.getAbsolutePath());
		}
	}

	final void setLastSelectedDirectory(final String lastDir) {
		if (appPreferences != null) {
			File tFile = new File(lastDir);
			if (tFile.exists())
				if (tFile.isDirectory())
					appPreferences.put("LastSelectedDirectory", lastDir);
				else {
					tFile = tFile.getParentFile();
					if (!tFile.isDirectory())
						appPreferences.put("LastSelectedDirectory", lastDir);
				}
		}
	}

	final File getLastSelectedDirectory() {
		File fnF = null;
		if (appPreferences != null) {
			final String fn = appPreferences.get("LastSelectedDirectory", null);
			fnF = (fn == null ? null : new File(fn));
		}
		if (fnF == null || !fnF.exists() || !fnF.isDirectory()) {
			// Get input file's directory? Or outputs?
		}
		return fnF;
	}

	final public static String getCurrentDateFmt() {
		final Calendar cal = Calendar.getInstance();
		EchoWriteConst.myDateFormat.setTimeZone(cal.getTimeZone());
		String txt = EchoWriteConst.myDateFormat.format(cal.getTime());
		LOGGER.debug("date = " + txt);
		return txt;
	}

	// TODO different call for Error? w/colors?
	final void showMessage(final String msg, final boolean clearPrevious, final TextArea outputArea) {
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

	final void showPopupMessage(final String msg1, final String msg2, final boolean error) {
		final Stage dialog = new Stage();
		dialog.setTitle("KQF Message Dialog");
		dialog.setResizable(true);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setWidth(420);
		dialog.setHeight(280);
		if (primaryStage != null)
			dialog.initOwner(primaryStage);

		final Button closeButton = new Button();
		closeButton.setText("_Close");
		closeButton.setMnemonicParsing(true);
		// closeButton.setStyle("-fx-padding: 8; -fx-margin: 8;");
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final Node source = (Node) event.getSource();
				final Stage stage = (Stage) source.getScene().getWindow();
				// doCleanup();
				stage.close();
			}
		});
		closeButton.setDefaultButton(true);

		final Label text1;
		if (msg1 != null) {
			text1 = new Label(msg1);
			final StringBuilder cssBorder = new StringBuilder();
			cssBorder.append("-fx-padding: 4 12 4 12;");
			cssBorder.append("-fx-border-style: solid inside;");
			cssBorder.append("-fx-border-width: 2;");
			cssBorder.append("-fx-border-insets: 5;");
			cssBorder.append("-fx-border-radius: 5;");
			cssBorder.append("-fx-border-color: black;");
			text1.setStyle(cssBorder.toString());
			text1.autosize();
		} else
			text1 = null;

		final TextArea text = new TextArea();
		text.appendText(msg2);
		text.setWrapText(true);
		text.setEditable(false);
		text.autosize();
		text.setFocusTraversable(false);
		text.setStyle(
				"-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ");
		// text.setStyle("-fx-background-color: #EEEEA4;");
		// final Text text = new Text(msg);
		// text.autosize();

		final VBox dialogVbox = new VBox(10);
		// dialogVbox.setAlignment(Pos.CENTER);
		dialogVbox.setStyle(
				"-fx-border-color: #2e8b57;&#10;-fx-border-width: 2px;&#10;-fx-border-insets: 5;&#10;-fx-border-style: solid;&#10; ");
		VBox.setMargin(closeButton, new Insets(4, 8, 8, 4));

		if (text1 != null)
			dialogVbox.getChildren().add(text1);
		dialogVbox.getChildren().add(text);
		dialogVbox.getChildren().add(closeButton);
		dialogVbox.autosize();
		final Scene dialogScene = new Scene(dialogVbox, 300, 75);
		// dialogScene.
		dialog.setScene(dialogScene);
		// dialog.sizeToScene();
		dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(final WindowEvent we) {
				LOGGER.debug("SubStage is cleaning up...");
				LOGGER.debug("SubStage is closing");
				dialog.close();
			}
		});
		closeButton.requestFocus();
		dialog.show();
	}

	final void showConfirmDialog(final String msg1, final String msg2, final ConfirmResult confirmResult) {
		final Stage dialog = new Stage();
		dialog.setTitle("KQF Message Dialog");
		dialog.setResizable(true);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setWidth(420);
		dialog.setHeight(280);
		if (primaryStage != null)
			dialog.initOwner(primaryStage);

		final Button confirmButton = new Button();
		confirmButton.setText("C_onfirm");
		confirmButton.setMnemonicParsing(true);
		// closeButton.setStyle("-fx-padding: 8; -fx-margin: 8;");
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				LOGGER.debug("Action Confirmed");
				final Node source = (Node) event.getSource();
				final Stage stage = (Stage) source.getScene().getWindow();
				confirmResult.actionConfirmed(msg1);
				stage.close();
			}
		});
		final Button closeButton = new Button();
		closeButton.setText("_Cancel");
		closeButton.setMnemonicParsing(true);
		// closeButton.setStyle("-fx-padding: 8; -fx-margin: 8;");
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				LOGGER.debug("Action Cancelled");
				final Node source = (Node) event.getSource();
				final Stage stage = (Stage) source.getScene().getWindow();
				confirmResult.actionCancelled(msg1);
				stage.close();
			}
		});
		closeButton.setDefaultButton(true);

		final Label text1;
		if (msg1 != null) {
			text1 = new Label(msg1);
			final StringBuilder cssBorder = new StringBuilder();
			cssBorder.append("-fx-padding: 4 12 4 12;");
			cssBorder.append("-fx-border-style: solid inside;");
			cssBorder.append("-fx-border-width: 2;");
			cssBorder.append("-fx-border-insets: 5;");
			cssBorder.append("-fx-border-radius: 5;");
			cssBorder.append("-fx-border-color: black;");
			text1.setStyle(cssBorder.toString());
			text1.autosize();
		} else
			text1 = null;

		final TextArea text = new TextArea();
		text.appendText(msg2);
		text.setWrapText(true);
		text.setEditable(false);
		text.autosize();
		text.setFocusTraversable(false);
		text.setStyle(
				"-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ");
		// text.setStyle("-fx-background-color: #EEEEA4;");
		// final Text text = new Text(msg);
		// text.autosize();

		final VBox dialogVbox = new VBox(10);
		// dialogVbox.setAlignment(Pos.CENTER);
		dialogVbox.setStyle(
				"-fx-border-color: #2e8b57;&#10;-fx-border-width: 2px;&#10;-fx-border-insets: 5;&#10;-fx-border-style: solid;&#10; ");
		// VBox.setMargin(dialogVboxI, new Insets(4, 8, 8, 4));
		final HBox dialogVboxI = new HBox(10);
		HBox.setMargin(confirmButton, new Insets(4, 8, 8, 4));
		HBox.setMargin(closeButton, new Insets(4, 8, 8, 4));

		if (text1 != null)
			dialogVbox.getChildren().add(text1);
		dialogVbox.getChildren().add(text);
		dialogVbox.getChildren().add(dialogVboxI);
		dialogVboxI.getChildren().add(confirmButton);
		dialogVboxI.getChildren().add(closeButton);
		dialogVboxI.autosize();
		dialogVbox.autosize();
		final Scene dialogScene = new Scene(dialogVbox, 300, 75);
		// dialogScene.
		dialog.setScene(dialogScene);
		// dialog.sizeToScene();
		dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(final WindowEvent we) {
				LOGGER.debug("SubStage is cleaning up...");
				LOGGER.debug("SubStage is closing");
				dialog.close();
			}
		});
		closeButton.requestFocus();
		dialog.show();
	}

	final boolean getSafeBooleanOrFalse(final Object val) {
		if (val == null)
			return false;

		if (val instanceof Boolean) {
			return (Boolean) val;
		}
		if (val instanceof String) {
			final String valS = (String) val;
			if (!StringUtils.isEmpty(valS))
				return Boolean.valueOf(valS);
		}
		return false;
	}

	final boolean getSafeBooleanOrTrue(final Object val) {
		if (val == null)
			return true;

		if (val instanceof Boolean) {
			return (Boolean) val;
		}
		if (val instanceof String) {
			final String valS = (String) val;
			if (!StringUtils.isEmpty(valS))
				return Boolean.valueOf(valS);
		}
		return true;
	}

	// void openNewWindow(String windowName, String windowTitle, final TextArea
	// reportArea, Stage owner,
	// final BaseCtrl callingCtrl, final Map<String, Object> paramsMap) {
	// openNewWindow(windowName, windowTitle, reportArea, owner, callingCtrl,
	// paramsMap, true);
	// }
	final void tryopenNewWindow(String windowName, String windowTitle, final TextArea reportArea, Stage owner,
			final BaseCtrl callingCtrl, final Map<String, Object> paramsMap) {
		try {
			openNewWindow(windowName, windowTitle, reportArea, owner, callingCtrl, null, paramsMap);
		} catch (IOException e) {
			e.printStackTrace();
			showMessage(e.getMessage(), false, reportArea);
		}
	}

	final void openNewWindow(String windowName, String windowTitle, final TextArea reportArea, Stage owner,
			final BaseCtrl callingCtrl, final Map<String, Object> paramsMap) throws IOException {
		openNewWindow(windowName, windowTitle, reportArea, owner, callingCtrl, null, paramsMap);
	}

	final void openNewWindow(String windowName, String windowTitle, final TextArea reportArea, Stage owner,
			final BaseCtrl callingCtrl, final WorkDoneNotify notifyCtrl, final Map<String, Object> paramsMap)
			throws IOException {
		LOGGER.debug("openNewWindow: Called w/windowName='" + windowName + "'");
		// Parent root;
		if (StringUtils.isEmpty(windowName))
			windowName = "MainWindow";
		if (StringUtils.isEmpty(windowTitle))
			windowTitle = "MainWindow";
		paramsMap.put(EchoWriteConst.PARAM_FRAMENAME, windowName);
		try {
			final String fxmlFile = MainFrame.getFxmlFrames().get(windowName);
			@SuppressWarnings("rawtypes")
			final Class fxmlCtrl = MainFrame.getFxmlCtrl().get(windowName);
			LOGGER.debug("openNewWindow: fxmlFile=" + fxmlFile + " fxmlCtrl=" + fxmlCtrl);
			// check fxmlFile
			final FXMLLoader fxmlLoader = new FXMLLoader();
			final URL location = getClass().getResource(fxmlFile);
			if (location == null) {
				LOGGER.error("Failed to get location!!!!!!");
				showMessage("ERROR loading FXML file for the <" + fxmlFile + "> screen", false, reportArea);
				// throw new Exception("Failed to get location!!!!!!")
				return;
			}
			fxmlLoader.setLocation(location);
			if (fxmlCtrl != null) {
				try {
					Object obj = fxmlCtrl.newInstance();
					fxmlLoader.setController(obj);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			final Parent root = (Parent) fxmlLoader.load(location.openStream());

			final Stage stage = new Stage();
			// check windowTitle
			stage.setTitle(windowTitle);
			stage.setScene(new Scene(root));
			if (owner == null) {
				LOGGER.warn("OWNER Is null for this window");
				// owner = primaryStage;
			}
			stage.initOwner(owner);
			stage.setUserData(paramsMap);
			final boolean modal = getSafeBooleanOrTrue(paramsMap.get(EchoWriteConst.PARAMMAP_MODAL));
			if (modal) {
				stage.initModality(Modality.APPLICATION_MODAL);
			} else {
				stage.initModality(Modality.NONE);
				if (owner != null)
					stage.setX(owner.getX() + owner.getWidth());
			}

			// TODO Change ICON
			final Image appIcon = new Image(getClass().getResourceAsStream("/62863-books-icon.png"));
			stage.getIcons().add(appIcon);

			//
			// final String key = titleOneText.getValue();
			// LOGGER.debug("Key = '" + key + "'");
			// final Preferences child = getPrefs().node(key);
			final BaseCtrl myController = (BaseCtrl) fxmlLoader.getController();
			myController.setupController(appProps, appPreferences, stage, paramsMap);

			// final FormatDao formatDao = new FormatDao();
			// setupDao(formatDao);
			// myController.setProfileLoaded(child, formatDao, appProps, stage);

			stage.setOnShown(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					String sName = (String) ((Map) stage.getUserData()).get(EchoWriteConst.PARAM_FRAMENAME);
					if (sName == null)
						sName = stage.getTitle();
					LOGGER.info("Stage is being shown...(" + sName + ")");
					myController.doSceneShown(stage);
					LOGGER.info("Stage is shown");
				}
			});
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					String sName = (String) ((Map) stage.getUserData()).get(EchoWriteConst.PARAM_FRAMENAME);
					if (sName == null)
						sName = stage.getTitle();
					LOGGER.info("Stage has been requested to close...(" + sName + ")");
					// myController.saveProps();
					myController.doCleanup();
					if (notifyCtrl != null)
						notifyCtrl.finishedWithWork(myController.worktype());
					// if (owner != null)
					// TODO callback owner.subviewBackToMe();
					myController.doSceneCloseRequested();
					LOGGER.debug("SubStage is closing");
					stage.close();
				}
			});
			stage.setOnHidden(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					String sName = (String) ((Map) stage.getUserData()).get(EchoWriteConst.PARAM_FRAMENAME);
					if (sName == null)
						sName = stage.getTitle();
					LOGGER.info("Stage is hidding...(" + sName + ")");
					myController.doSceneHidden();
					LOGGER.info("Stage is hidden");

					LOGGER.debug("windowCount=" + BaseCtrl.getWindowCount());
					if (BaseCtrl.getWindowCount() <= 0) {
						LOGGER.info("Closing platform");
						// TODO DataManagerBiz.getDataManager().close();
						Platform.exit();
					}
				}
			});
			stage.setOnHiding(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					String sName = (String) ((Map) stage.getUserData()).get(EchoWriteConst.PARAM_FRAMENAME);
					if (sName == null)
						sName = stage.getTitle();
					LOGGER.info("Stage is being hidden...(" + sName + ")");
					myController.doSceneHiding(stage);
				}
			});
			stage.setOnShowing(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					String sName = (String) ((Map) stage.getUserData()).get(EchoWriteConst.PARAM_FRAMENAME);
					if (sName == null)
						sName = stage.getTitle();
					LOGGER.info("Stage is being shown...(" + sName + ")");
					myController.doSceneShowing(stage);
					LOGGER.info("Stage is shown...(" + sName + ")");
				}
			});

			stage.showAndWait();
			// stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e);
			throw e;// TODO
		}
	}

	// Virtual, for others to overwrite if they want to use the paramsMap
	// public void setupController(final Properties props, final Preferences
	// appPreferences, final Stage primaryStage,
	// final Map<String, Object> paramsMap) {
	// setupController(props, appPreferences, primaryStage);
	// }

	protected String worktype() {
		return EchoWriteConst.PROCESS_NONE;
	}

	// virtual
	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage,
			final Map<String, Object> paramsMap) {
		LOGGER.debug("setupController: Called");
		this.appProps = props;
		this.appPreferences = appPreferences;
		this.paramsMap = paramsMap;
		this.primaryStage = primaryStage;
		this.appVersion = appProps.getProperty(EchoWriteConst.PROP_KEY_VERSION);
		// final String local1 = appPreferences.get("localization1", "en");
		// final String local2 = appPreferences.get("localization2", "US");
		final Locale sLocal = new Locale("en", "US");// local1, local2);
		this.messageBundle = ResourceBundle.getBundle("cwc2messages", sLocal);

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
		// if (appPreferences != null) {
		// String prefKey = String.format("view/%s/%s",
		// paramsMap.get("FRAMENAME"), Prefs.VIEW_WINDOW_Y);
		// final double windowY = appPreferences.getDouble(prefKey, -1);
		// prefKey = String.format("view/%s/%s", paramsMap.get("FRAMENAME"),
		// Prefs.VIEW_WINDOW_X);
		// final double windowX = appPreferences.getDouble(prefKey, -1);
		// prefKey = String.format("view/%s/%s", paramsMap.get("FRAMENAME"),
		// Prefs.VIEW_WINDOW_W);
		// final double windowW = appPreferences.getDouble(prefKey, -1);
		// prefKey = String.format("view/%s/%s", paramsMap.get("FRAMENAME"),
		// Prefs.VIEW_WINDOW_H);
		// final double windowH = appPreferences.getDouble(prefKey, -1);
		// if (windowY > -1)
		// primaryStage.setY(windowY);
		// if (windowX > -1)
		// primaryStage.setX(windowX);
		// if (windowW > -1)
		// primaryStage.setWidth(windowW);
		// if (windowH > -1)
		// primaryStage.setHeight(windowH);
		// }
		LOGGER.debug("setupController: Done");
		this.doControllerSetupDone();
	}

	@SuppressWarnings("rawtypes")
	final public static void alignColumnLabelsLeftHack(final TableView inputTable) {
		// Hack: align column headers to the center.

		inputTable.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, final Number t, final Number t1) {
				Platform.runLater(new Runnable() {
					public void run() {
						// System.out.print(listerColumn.getText() + " ");
						// System.out.println(t1);
						if (t != null && t.intValue() > 0)
							return; // already aligned
						for (Node node : inputTable.lookupAll(".column-header > .label")) {
							if (node instanceof Label)
								((Label) node).setAlignment(Pos.TOP_LEFT);
						}
					}
				});
			};
		});

		// TODO when I make this Java 8 or like whatever
		// inputTable.widthProperty().addListener((src, o, n) ->
		// Platform.runLater(() -> {
		// if (o != null && o.intValue() > 0)
		// return; // already aligned
		// for (Node node : inputTable.lookupAll(".column-header > .label")) {
		// if (node instanceof Label)
		// ((Label) node).setAlignment(Pos.TOP_LEFT);
		// }
		// }));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	final public static void autoFitTable(final TableView tableView) {
		tableView.getItems().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(Change<?> c) {
				for (final Object column : tableView.getColumns()) {
					try {
						if (column == null)
							continue;
						if (tableView == null)
							continue;
						if (tableView.getSkin() == null)
							continue;
						// if (columnToFitMethod == null) continue;
						if (tableView.getSkin() != null && column != null)
							columnToFitMethod.invoke(tableView.getSkin(), column, -1);
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	final String getLocalizedText(final String key, final String defaultText) {
		if (!messageBundle.containsKey(key)) {
			return defaultText;
		}
		return messageBundle.getString(key);
	}

	// todo working/needed?
	final void setTooltips(final Pane pane) {
		// LOGGER.debug("setTooltips: Called ");
		if (pane == null)
			return;
		for (Node node : pane.getChildren()) {
			if (node instanceof TextField) {
				final TextField tf = (TextField) node;
				if (tf.getTooltip() == null && !StringUtils.isBlank(tf.getPromptText())) {
					tf.setTooltip(new Tooltip(tf.getPromptText()));
				}
			} else if (node instanceof Pane) {
				setTooltips((Pane) node);
			} else if (node instanceof TitledPane) {
				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					setTooltips((Pane) nd2);
				}
			}
		}
		// LOGGER.debug("setTooltips: Done ");
	}// todo working?

	final void setTooltips(final SplitPane pane) {
		// LOGGER.debug("setTooltips: Called ");
		if (pane == null)
			return;
		for (Node node : pane.getItems()) {
			if (node instanceof TextField) {
				final TextField tf = (TextField) node;
				if (tf.getTooltip() == null && !StringUtils.isBlank(tf.getPromptText())) {
					tf.setTooltip(new Tooltip(tf.getPromptText()));
				}
			} else if (node instanceof Pane) {
				setTooltips((Pane) node);
			} else if (node instanceof TitledPane) {
				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					setTooltips((Pane) nd2);
				}
			}
		}
		// LOGGER.debug("setTooltips: Done ");
	}

	final private void windowYChanged(final Number newVal) {
		// if (appPreferences != null) {
		// final String prefKey = String.format("view/%s/%s",
		// paramsMap.get("FRAMENAME"), Prefs.VIEW_WINDOW_Y);
		// appPreferences.putDouble(prefKey, newVal.doubleValue());
		// }
	}

	final private void windowXChanged(final Number newVal) {
		// if (appPreferences != null) {
		// final String prefKey = String.format("view/%s/%s",
		// paramsMap.get("FRAMENAME"), Prefs.VIEW_WINDOW_X);
		// appPreferences.putDouble(prefKey, newVal.doubleValue());
		// }
	}

	final private void windowHeightChanged(final Number newVal) {
		// if (appPreferences != null) {
		// final String prefKey = String.format("view/%s/%s",
		// paramsMap.get("FRAMENAME"), Prefs.VIEW_WINDOW_H);
		// appPreferences.putDouble(prefKey, newVal.doubleValue());
		// }
	}

	final private void windowWidthChanged(final Number newVal) {
		// if (appPreferences != null) {
		// final String prefKey = String.format("view/%s/%s",
		// paramsMap.get("FRAMENAME"), Prefs.VIEW_WINDOW_W);
		// appPreferences.putDouble(prefKey, newVal.doubleValue());
		// }
	}

	// protected boolean findAndSetFieldByInputFile(final String windowTitle,
	// final String inText,
	// final String prevOutText, final TextField outputTextField, final String
	// namePattern,
	// final String nameExtension) {
	// boolean success = false;
	// final File initDirectroy = locateDir(windowTitle, inText);
	// if (initDirectroy != null) {
	// final File inFile = new File(inputFileText.getText());
	//
	// String outFilename = "\\ChapterCount1.csv";
	// final File nFile = new File(outputFormatSingleFileText.getText(),
	// outFilename);
	// outputCountFileText.setText(nFile.getAbsolutePath());
	// outputCountDir = nFile.getParentFile();
	// } else {
	// outputTextField.setText(prevOutText);
	// }
	// return false;
	// }
	//
	// protected File locateDir(final String title, final String
	// textForInitialDir) {
	// final DirectoryChooser chooser = new DirectoryChooser();
	// final File lastDir1 = new File(textForInitialDir);
	//
	// final File lastDir2 = getLastSelectedDirectory();
	// if (lastDir1 != null && lastDir1.exists()) {
	// if (lastDir1.isDirectory())
	// chooser.setInitialDirectory(lastDir1);
	// else if (lastDir1.getParentFile() != null &&
	// lastDir1.getParentFile().exists()
	// && lastDir1.getParentFile().isDirectory())
	// chooser.setInitialDirectory(lastDir1.getParentFile());
	// } else if (lastDir2 != null && lastDir2.exists()) {
	// if (lastDir2.isDirectory())
	// chooser.setInitialDirectory(lastDir2);
	// else if (lastDir2.getParentFile() != null &&
	// lastDir2.getParentFile().exists()
	// && lastDir2.getParentFile().isDirectory())
	// chooser.setInitialDirectory(lastDir2.getParentFile());
	// }
	// chooser.setTitle(title);
	// final File file = chooser.showDialog(new Stage());
	// // textFieldToSet.setText(file.getAbsolutePath());
	// // setLastSelectedDirectory(file);
	// return file;
	// }

	final protected boolean locateDir(final ActionEvent event, final String title,
			final TextInputControl textFieldToSet, final TextInputControl textFieldDefault) {
		final DirectoryChooser chooser = new DirectoryChooser();

		TextInputControl textFieldForInitialDir;
		if (textFieldDefault != null && StringUtils.isBlank(textFieldToSet.getText()))
			textFieldForInitialDir = textFieldDefault;
		else
			textFieldForInitialDir = textFieldToSet;
		final File lastDir1 = new File(textFieldForInitialDir.getText());
		final File lastDir2 = getLastSelectedDirectory();
		if (lastDir1 != null && lastDir1.exists()) {
			if (lastDir1.isDirectory())
				chooser.setInitialDirectory(lastDir1);
			else if (lastDir1.getParentFile() != null && lastDir1.getParentFile().exists()
					&& lastDir1.getParentFile().isDirectory())
				chooser.setInitialDirectory(lastDir1.getParentFile());
		} else if (lastDir2 != null && lastDir2.exists()) {
			if (lastDir2.isDirectory())
				chooser.setInitialDirectory(lastDir2);
			else if (lastDir2.getParentFile() != null && lastDir2.getParentFile().exists()
					&& lastDir2.getParentFile().isDirectory())
				chooser.setInitialDirectory(lastDir2.getParentFile());
		}

		// if (textField.getText() != null && textField.getText().length() > 0)
		// chooser.setInitialDirectory(new File(textField.getText()));
		// else
		// chooser.setInitialDirectory(lastSelectedDirectory);
		chooser.setTitle(title);
		final File file = chooser.showDialog(new Stage());
		if (file == null) {
			// textField.setText("");
			// lastSelectedDirectory = null;
			return false;
		} else {
			textFieldToSet.setText(file.getAbsolutePath());
			setLastSelectedDirectory(file);
			return true;
		}
	}

	final protected void locateFile(final ActionEvent event, final String title, final TextField textField) {
		locateFile(event, title, textField, null, null);
	}

	final protected File locateFile(final ActionEvent event, final String title, final TextField textField,
			final String defaultName, final EchoWriteConst.FILTERTYPE defaultExtension) {
		return locateFile(event, title, textField, defaultName, defaultExtension, null);
	}

	final protected File locateFile(final ActionEvent event, final String title, final TextField textField,
			final String defaultName, final EchoWriteConst.FILTERTYPE defaultExtension, File startDir) {
		LOGGER.debug("locateFile: Called");
		final FileChooser chooser = new FileChooser();
		if (textField != null && !StringUtils.isEmpty(textField.getText())) {
			startDir = new File(textField.getText());
			if (!startDir.isDirectory())
				startDir = startDir.getParentFile();
			if (!startDir.isDirectory())
				startDir = null;
		}
		if (startDir == null)
			startDir = getLastSelectedDirectory();
		if (startDir == null) {
			final String sfhome = (String) appProps.get("home");
			if (sfhome != null)
				startDir = new File(sfhome);
		}
		if (startDir != null) {
			if (!startDir.isDirectory())
				startDir = startDir.getParentFile();
			if (!startDir.isDirectory())
				startDir = null;
		}
		if (startDir == null) {
			final String sfhome = (String) appProps.get("home");
			if (sfhome != null)
				startDir = new File(sfhome);
		}
		if (startDir != null) {
			chooser.setInitialDirectory(startDir);
			setLastSelectedDirectory(startDir);
		}
		chooser.setTitle(title);
		LOGGER.debug("locateFile: defaultName='" + defaultName + "'");
		if (!StringUtils.isEmpty(defaultName))
			chooser.setInitialFileName(defaultName);
		// chooser.setInitialFileName("ChapterCount1.csv");
		// System.out.println("lastSelectedDirectory = '" +
		// lastSelectedDirectory + "'");

		LOGGER.debug("locateFile: defaultExtension=" + defaultExtension);
		if (EchoWriteConst.FILTERTYPE.JSON == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("JSON", "*.json"));
		if (EchoWriteConst.FILTERTYPE.YAML == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("YAML", "*.yaml"));
		if (EchoWriteConst.FILTERTYPE.HTML == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("HTML", "*.html"));
		if (EchoWriteConst.FILTERTYPE.TEXT == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("TEXT", "*.txt"));
		if (EchoWriteConst.FILTERTYPE.CSV == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV", "*.csv"));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("ALL", "*.*"));

		final File file = chooser.showOpenDialog(new Stage());
		if (file == null) {
			if (textField != null)
				textField.setText("");
			// lastSelectedDirectory = null;
		} else {
			if (textField != null)
				textField.setText(file.getAbsolutePath());
			// lastSelectedDirectory = file.getParentFile();
		}
		LOGGER.debug("locateFile: Done");
		return file;
	}

	// For probably new files, not existing ones
	// https://docs.oracle.com/javase/8/javafx/api/javafx/stage/FileChooser.html#setSelectedExtensionFilter-javafx.stage.FileChooser.ExtensionFilter-
	final File chooseFile(final ActionEvent event, final String title, final TextField textField,
			final String defaultName, final EchoWriteConst.FILTERTYPE defaultExtension) {
		final FileChooser chooser = new FileChooser();
		File startDir = null;
		if (textField != null && !StringUtils.isEmpty(textField.getText())) {
			startDir = new File(textField.getText());
			if (!startDir.isDirectory())
				startDir = startDir.getParentFile();
			if (!startDir.isDirectory())
				startDir = null;
		}
		if (startDir == null)
			startDir = getLastSelectedDirectory();
		if (startDir == null) {
			final String sfhome = (String) appProps.get("home");
			startDir = new File(sfhome);
		}
		if (startDir != null) {
			if (!startDir.isDirectory())
				startDir = startDir.getParentFile();
			if (!startDir.isDirectory())
				startDir = null;
		}
		if (startDir != null) {
			chooser.setInitialDirectory(startDir);
			setLastSelectedDirectory(startDir);
		}
		chooser.setTitle(title);
		if (!StringUtils.isEmpty(defaultName))
			chooser.setInitialFileName(defaultName);

		LOGGER.debug("locateFile: defaultExtension=" + defaultExtension);
		if (EchoWriteConst.FILTERTYPE.JSON == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("JSON", "*.json"));
		if (EchoWriteConst.FILTERTYPE.YAML == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("YAML", "*.yaml"));
		if (EchoWriteConst.FILTERTYPE.HTML == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("HTML", "*.html"));
		if (EchoWriteConst.FILTERTYPE.TEXT == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("TEXT", "*.txt"));
		if (EchoWriteConst.FILTERTYPE.CSV == (defaultExtension))
			chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV", "*.csv"));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("ALL", "*.*"));

		final File file = chooser.showSaveDialog(new Stage());
		if (file == null) {
			// textField.setText("");
			// lastSelectedDirectory = null;
		} else {
			if (textField != null)
				textField.setText(file.getAbsolutePath());
			// lastSelectedDirectory = file.getParentFile();
		}
		return file;
	}

	// void chooseDirectory() {
	// // TODO chooseDirectory
	// }

	final void setDetectChangesNodeElem(final Node node) {
		if (node instanceof TextField) {
			final TextField tf = (TextField) node;
			// LOGGER.debug("setDetectChanges: adding to field=" +
			// tf.getId());
			tf.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					LOGGER.debug("setDetectChanges: newValue='" + newValue + "'");
					if (oldValue == null || newValue.compareTo(oldValue) != 0)
						setProfileChangeMade(true);
				}
			});
		} else if (node instanceof TextArea) {
			final TextArea tf = (TextArea) node;
			// LOGGER.debug("setDetectChanges: adding to field=" +
			// tf.getId());
			tf.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					LOGGER.debug("setDetectChanges: newValue='" + newValue + "'");
					if (oldValue == null || newValue.compareTo(oldValue) != 0)
						setProfileChangeMade(true);
				}
			});
			//
		} else if (node instanceof CheckBox) {
			final CheckBox cb = (CheckBox) node;
			cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					LOGGER.debug("setDetectChanges: newValue='" + newValue + "'");
					if (newValue.compareTo(oldValue) != 0)
						setProfileChangeMade(true);
				}
			});
		} else if (node instanceof Pane) {

			setDetectChanges((Pane) node);
		} else if (node instanceof TitledPane)

		{

			final Node nd2 = ((TitledPane) node).getContent();
			if (nd2 instanceof Pane) {

				setDetectChanges((Pane) nd2);
			}
		}
	}

	final void setDetectChanges(final Pane pane) {
		for (Node node : pane.getChildren()) {
			setDetectChangesNodeElem(node);
		}
	}

	final void setDetectChanges(final ObservableList<Node> nodes) {
		for (Node node : nodes) {
			setDetectChangesNodeElem(node);
		}
	}

	final void setDetectChanges(final Control pane) {
		for (Node node : pane.getChildrenUnmodifiable()) {
			setDetectChangesNodeElem(node);
		}
	}

	void setProfileChangeMade(boolean b) {
		profileChangeMade = b;
	}

	// void setColumnWidth(final ObservableList<TableColumn<?, ?>> columns,
	// final String key, final int coli) {
	// LOGGER.debug("setColumnWidth: Called w/key=" + key + " for col#" + coli);
	// final double colW = appPreferences.getDouble(key, -1);
	// LOGGER.debug("setColumnWidth: colW1=" + colW);
	// if (colW > -1) {
	// final TableColumn<?, ?> col = columns.get(coli);
	// col.setPrefWidth(colW);
	// }
	// }

	@SuppressWarnings("rawtypes")
	final void setColumnWidth(final ObservableList<TableColumn> columns, final String key, final int coli) {
		LOGGER.debug("setColumnWidth: Called w/key=" + key + " for col#" + coli);
		final double colW = appPreferences.getDouble(key, -1);
		LOGGER.debug("setColumnWidth: colW1=" + colW);
		if (colW > -1) {
			final TableColumn<?, ?> col = columns.get(coli);
			col.setPrefWidth(colW);
		}
	}

	final void columnWidthChanged(final String MYPREFSKEY, final int colNum, final Number newValue) {
		if (appPreferences != null && MYPREFSKEY != null) {
			final String key = String.format(MYPREFSKEY, colNum);
			// LOGGER.debug("columnWidthChanged: col#" + colNum + " val=" +
			// newValue.doubleValue());
			appPreferences.putDouble(key, newValue.doubleValue());
		} else
			LOGGER.warn("NO app preferences set");
	}

	final void lockAllButtons(final Pane pane) {
		if (pane == null)
			return;
		for (Node node : pane.getChildren()) {
			if (node instanceof Button) {
				final Button tf = (Button) node;
				tf.setDisable(true);
			} else if (node instanceof Pane) {
				lockAllButtons((Pane) node);
			} else if (node instanceof TitledPane) {
				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					lockAllButtons((Pane) nd2);
				}
			}
		}
	}

	final void unlockAllButtons(final Pane pane) {
		for (Node node : pane.getChildren()) {
			if (node instanceof Button) {
				final Button tf = (Button) node;
				tf.setDisable(false);
			} else if (node instanceof Pane) {
				unlockAllButtons((Pane) node);
			} else if (node instanceof TitledPane) {
				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					unlockAllButtons((Pane) nd2);
				}
			}
		}
	}

	final void hideAllInArea(final Pane pane) {
		if (pane == null)
			return;
		for (Node node : pane.getChildren()) {
			node.setVisible(false);
			if (node instanceof Button) {
				// final Button tf = (Button) node;
				// tf.setDisable(true);
			} else if (node instanceof Pane) {
				hideAllInArea((Pane) node);
			} else if (node instanceof TitledPane) {
				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					hideAllInArea((Pane) nd2);
				}
			}
		}
	}

	final void showAllInArea(final Pane pane) {
		if (pane == null)
			return;
		for (Node node : pane.getChildren()) {
			node.setVisible(true);
			if (node instanceof Button) {
				// final Button tf = (Button) node;
				// tf.setDisable(true);
			} else if (node instanceof Pane) {
				showAllInArea((Pane) node);
			} else if (node instanceof TitledPane) {
				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					showAllInArea((Pane) nd2);
				}
			}
		}
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		doCleanup();
		// stage.getOwner()
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

	public abstract void doCleanup();

	abstract void lockGui();

	abstract void unlockGui();

	public void doControllerSetupDone() {

	}

	public void doSceneHidden() {
		windowCount -= 1;
		LOGGER.debug("windowCount=" + windowCount);
	}

	public void doSceneHiding(final Stage stage) {
	}

	public void doSceneShowing(final Stage stage) {
		windowCount += 1;
		LOGGER.debug("windowCount=" + windowCount);
	}

	public void doSceneCloseRequest() {
	}

	public void doSceneCloseRequested() {
	}

	/**
	 * Called after Scene is showing
	 * 
	 * @param stage
	 */
	public void doSceneShown(final Stage stage) {
	}

}
