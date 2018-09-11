package com.echomap.kqf.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.two.gui.KQFBaseCtrl;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
	private final static Logger LOGGER = LogManager.getLogger(KQFBaseCtrl.class);
	public static final String PROP_KEY_VERSION = "version";
	static public final DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	static public final String WINDOWKEY_MAINWINDOW = "MainWindow";
	static public final String WINDOWKEY_PROFILE_NEW = "NEWProfile";
	static public final String WINDOWKEY_PROFILE_EDIT = "EDITProfile";
	static public final String WINDOWKEY_PROFILE_DELETE = "DELProfile";

	static public final String WINDOWKEY_MOREFILES = "MoreFiles";

	static public final String WINDOWKEY_IMPORT = "Import";
	static public final String WINDOWKEY_EXPORT = "Export";

	Preferences appPreferences = null;
	Properties appProps = null;
	String appVersion = null;
	Stage primaryStage = null;
	boolean profileChangeMade = false;

	// String lastNofificationMsg = null;

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

	void setLastSelectedDirectory(final File lastDir) {
		if (appPreferences != null) {
			appPreferences.put("LastSelectedDirectory", lastDir.getAbsolutePath());
		}
	}

	void setLastSelectedDirectory(final String lastDir) {
		if (appPreferences != null) {
			appPreferences.put("LastSelectedDirectory", lastDir);
		}
	}

	File getLastSelectedDirectory() {
		File fnF = null;
		if (appPreferences != null) {
			final String fn = appPreferences.get("LastSelectedDirectory", null);
			fnF = (fn == null ? null : new File(fn));
		}
		return fnF;
	}

	public static String getCurrentDateFmt() {
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

	void showPopupMessage(final String msg1, final String msg2, final boolean error) {
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

	void openNewWindow(String windowName, String windowTitle, final TextArea reportArea, Stage owner,
			final BaseCtrl callingCtrl, final Map<String, Object> paramsMap) {
		LOGGER.debug("openNewWindow: Called w/windowName='" + windowName + "'");
		// Parent root;
		if (StringUtils.isEmpty(windowName))
			windowName = "MainWindow";
		if (StringUtils.isEmpty(windowTitle))
			windowTitle = "MainWindow";
		try {
			final String fxmlFile = MainFrame.fxmlFrames.get(windowName);
			final Class fxmlCtrl = MainFrame.fxmlCtrl.get(windowName);
			LOGGER.debug("openNewWindow: fxmlFile=" + fxmlFile + " fxmlCtrl=" + fxmlCtrl);
			// check fxmlFile
			final FXMLLoader fxmlLoader = new FXMLLoader();
			final URL location = getClass().getResource(fxmlFile);
			if (location == null) {
				LOGGER.error("Failed to get location!!!!!!");
				showMessage("ERROR loading FXML file for '" + windowTitle + "' screen", false, reportArea);
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
			if (owner == null)
				owner = primaryStage;
			if (owner == null)
				LOGGER.warn("OWNER Is null for this window");
			stage.initOwner(owner);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(final WindowEvent we) {
					LOGGER.debug("SubStage is cleaning up...");
					// myController.saveProps();
					// myController.doCleanup();
					// if (owner != null)
					// TODO callback owner.subviewBackToMe();
					LOGGER.debug("SubStage is closing");
					stage.close();
				}

			});
			//
			// final String key = titleOneText.getValue();
			// LOGGER.debug("Key = '" + key + "'");
			// final Preferences child = getPrefs().node(key);
			final BaseCtrl myController = (BaseCtrl) fxmlLoader.getController();
			myController.setupController(appProps, appPreferences, owner, paramsMap);

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

	public void setupController(final Properties props, final Preferences appPreferences, final Stage primaryStage,
			final Map<String, Object> paramsMap) {
		setupController(props, appPreferences, primaryStage);
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

	// todo working?
	void setTooltips(final Pane pane) {
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

	protected boolean locateDir(final ActionEvent event, final String title, final TextInputControl textFieldToSet,
			final TextInputControl textFieldDefault) {
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

	protected void locateFile(final ActionEvent event, final String title, final TextField textField) {
		locateFile(event, title, textField, null, null);
	}

	protected void locateFile(final ActionEvent event, final String title, final TextField textField,
			final String defaultName, final String defaultExtension) {
		final FileChooser chooser = new FileChooser();
		File startDir = null;
		if (!StringUtils.isEmpty(textField.getText())) {
			startDir = new File(textField.getText());
			if (!startDir.isDirectory())
				startDir = startDir.getParentFile();
			if (!startDir.isDirectory())
				startDir = null;
		}
		if (startDir == null)
			startDir = getLastSelectedDirectory();
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
		// chooser.setInitialFileName("ChapterCount1.csv");
		// System.out.println("lastSelectedDirectory = '" +
		// lastSelectedDirectory + "'");

		final File file = chooser.showOpenDialog(new Stage());
		if (file == null) {
			textField.setText("");
			// lastSelectedDirectory = null;
		} else {
			textField.setText(file.getAbsolutePath());
			// lastSelectedDirectory = file.getParentFile();
		}
	}

	// For probably new files, not existing ones
	File chooseFile(final ActionEvent event, final String title, final TextField textField, final String defaultName,
			final String defaultExtension) {
		final FileChooser chooser = new FileChooser();
		File startDir = null;
		if (!StringUtils.isEmpty(textField.getText())) {
			startDir = new File(textField.getText());
			if (!startDir.isDirectory())
				startDir = startDir.getParentFile();
			if (!startDir.isDirectory())
				startDir = null;
		}
		if (startDir == null)
			startDir = getLastSelectedDirectory();
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
		if (!StringUtils.isEmpty(defaultExtension)) {
			if ("JSON".compareTo(defaultExtension) == 0)
				chooser.setSelectedExtensionFilter(new ExtensionFilter("JSON", "json"));
			else if ("HTML".compareTo(defaultExtension) == 0)
				chooser.setSelectedExtensionFilter(new ExtensionFilter("HTML", "html"));
			else if ("TXT".compareTo(defaultExtension) == 0)
				chooser.setSelectedExtensionFilter(new ExtensionFilter("TEXT", "txt"));
		}

		final File file = chooser.showSaveDialog(new Stage());
		if (file == null) {
			// textField.setText("");
			// lastSelectedDirectory = null;
		} else {
			textField.setText(file.getAbsolutePath());
			// lastSelectedDirectory = file.getParentFile();
		}
		return file;
	}

//	void chooseDirectory() {
//		// TODO chooseDirectory
//	}

	void setDetectChanges(final Pane pane) {
		for (Node node : pane.getChildren()) {
			if (node instanceof TextField) {
				final TextField tf = (TextField) node;
				// LOGGER.debug("setDetectChanges: adding to field=" +
				// tf.getId());
				tf.textProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue,
							String newValue) {
						LOGGER.debug("setDetectChanges: newValue='" + newValue + "'");
						if (newValue.compareTo(oldValue) != 0)
							setProfileChangeMade(true);
					}
				});
			} else if (node instanceof TextArea) {
				final TextArea tf = (TextArea) node;
				// LOGGER.debug("setDetectChanges: adding to field=" +
				// tf.getId());
				tf.textProperty().addListener(new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue,
							String newValue) {
						LOGGER.debug("setDetectChanges: newValue='" + newValue + "'");
						if (newValue.compareTo(oldValue) != 0)
							setProfileChangeMade(true);
					}
				});
				//
			} else if (node instanceof Pane) {
				setDetectChanges((Pane) node);
			} else if (node instanceof TitledPane) {

				final Node nd2 = ((TitledPane) node).getContent();
				if (nd2 instanceof Pane) {
					setDetectChanges((Pane) nd2);
				}
			}
		}
	}

	void setProfileChangeMade(boolean b) {
		profileChangeMade = b;
	}

	abstract void doCleanup();

	abstract void lockGui();

	abstract void unlockGui();

}
