package com.echomap.kqf.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.ProfileManager;
import com.echomap.kqf.data.BookDataObj;
import com.echomap.kqf.data.KeyValuePair;
import com.echomap.kqf.data.Profile;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CtrlExternalUrlView extends BaseCtrl implements Initializable {
	private final static Logger LOGGER = LogManager.getLogger(CtrlExternalUrlView.class);

	ProfileManager profileManager = null;

	private static int COL_NAME = 1;
	private static int COL_KEY = 2;
	private static int COL_ASIN = 3;
	// private static int COL_INPUTFILE = 4;

	//
	Profile selectedProfile = null;

	@FXML
	private Pane outerMostContainer;

	@SuppressWarnings("rawtypes")
	@FXML
	private WebView webview1;

	@FXML
	private ProgressBar progressBar1;

	@SuppressWarnings("rawtypes")
	@FXML
	private TableView inputTable;

	@FXML
	private HBox subLinkArea;

	@FXML
	private Button closeBtn;
	@FXML
	private Label textUrl;
	@FXML
	private Label textProfileLoaded;

	/**
	 * 
	 */
	public CtrlExternalUrlView() {
		super();
	}

	/*
	 * SETUP Functions
	 */

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("initialize: Called");
		setTooltips(outerMostContainer);
		lockGui();
		setupData();
		fixFocus();
		// loadData();
		LOGGER.debug("initialize: Done");
	}

	private void setupData() {
		setupTable();
		setupTableData();
		// setupWebView();
		clearSubLinkArea();
	}

	private void setupWebView() {

		final WebView mWebview = webview1;// new WebView();
		final WebEngine mWebEngine = mWebview.getEngine();

		// A Worker load the page
		final Worker<Void> worker = mWebEngine.getLoadWorker();

		// Listening to the status of worker
		worker.stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				// stateLabel.setText("Loading state: " + newValue.toString());
				if (newValue == Worker.State.SUCCEEDED) {
					// stage.setTitle(webEngine.getLocation());
					// stateLabel.setText("Finish!");
				}
			}
		});

		// Bind the progress property of ProgressBar
		// with progress property of Worker
		progressBar1.progressProperty().bind(worker.progressProperty());
	}

	private void createLinkButton(final String hdrText, final String linkFormat, final String linkData,
			final HBox subLinkArea2, final Insets inset1) {
		final Button btn = new Button();
		// btn.getStyleClass().add("");
		btn.setPadding(inset1);
		btn.setText(hdrText);
		btn.setOnAction(event -> {
			try {
				doloadExternalData(btn, linkFormat, linkData);
			} catch (Exception e) {
				// TODO LOG
				e.printStackTrace();
			}
		});
		subLinkArea.getChildren().add(btn);
	}

	@SuppressWarnings("unchecked")
	private void setupTable() {
		LOGGER.debug("setupTable: Called");

		final TableColumn<BookDataObj, Object> colKey = new TableColumn<>("Key");
		colKey.setCellValueFactory(new PropertyValueFactory<>("BookKey"));

		final TableColumn<Object, Object> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<>("BookTitle"));

		final TableColumn<Object, Object> colAsin = new TableColumn<>("ASIN");
		colAsin.setCellValueFactory(new PropertyValueFactory<>("asin"));

		colKey.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.EXPORT_PREF_COL_S, COL_KEY, newValue);
			}
		});

		// col1. resize handler
		colName.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.EXPORT_PREF_COL_S, COL_NAME, newValue);
			}
		});

		colAsin.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				columnWidthChanged(Prefs.EXPORT_PREF_COL_S, COL_ASIN, newValue);
			}
		});

		//
		inputTable.getColumns().clear();
		inputTable.getColumns().addAll(colKey, colName, colAsin);

		// Hack: align column headers to the center.
		BaseCtrl.alignColumnLabelsLeftHack(inputTable);

		inputTable.setRowFactory(new Callback<TableView<BookDataObj>, TableRow<BookDataObj>>() {
			@Override
			public TableRow<BookDataObj> call(TableView<BookDataObj> tableView) {
				final TableRow<BookDataObj> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final BookDataObj rowData = row.getItem();
							LOGGER.debug("rowData: left2: " + rowData);
							// inputName.setText(rowData.getName());
							setupSubLinkArea(rowData);
							// rowData.setExport(!rowData.isExport());
						} else if (event.isSecondaryButtonDown()) {
							// right click code here
							final BookDataObj rowData = row.getItem();
							LOGGER.debug("rowData: right: " + rowData);
							// rowData.setExport(true);
							setupSubLinkArea(rowData);
						}
						inputTable.refresh();
					}
				});
				return row;
			}
		});

		// Trying to set color of these cells
		// exportCol.setCellFactory(tc -> new TableCell<BookDataObj, Object>() {
		// @Override
		// protected void updateItem(final Boolean item, final boolean empty) {
		// super.updateItem(item, empty);
		// if (item == null || empty) {
		// setText(null);
		// setGraphic(null);
		// return;
		// }
		// if (item) {
		// this.setText("true");
		// this.setStyle("");
		// if (this.getTableRow() != null)
		// this.getTableRow().setStyle("");
		// } else {
		// this.setText("false");
		// // slategray / slategrey
		// this.setStyle("-fx-background-color: #2F4F4F;");
		// this.getTableRow().setStyle("-fx-background-color: #708090;");
		// // this.setStyle("-fx-border-color: red;");
		// }
		// }
		// });

		// set saved col widths
		if (appPreferences != null) {
			@SuppressWarnings("rawtypes")
			final ObservableList columns = inputTable.getColumns();
			String key = "";
			key = String.format(Prefs.EXPORT_PREF_COL_S, 1);
			setColumnWidth(columns, key, 0);
			key = String.format(Prefs.EXPORT_PREF_COL_S, 2);
			setColumnWidth(columns, key, 1);
			key = String.format(Prefs.EXPORT_PREF_COL_S, 3);
			setColumnWidth(columns, key, 2);
			// key = String.format(Prefs.EXPORT_PREF_COL_S, 4);
			// setColumnWidth(columns, key, 3);
		} else {
			LOGGER.warn("NO App preferences set");
		}

		//
		inputTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		BaseCtrl.autoFitTable(inputTable);

		LOGGER.debug("setupTable: Done");
	}

	private void fixFocus() {
		closeBtn.requestFocus();
	}

	private void doloadExternalData(final Button btn, final String stringMsg, final String rowData)
			throws MalformedURLException {
		final String urlStr = String.format(stringMsg, rowData);
		textUrl.setText(urlStr);

		final WebView mWebview = webview1;// new WebView();
		final WebEngine mWebEngine = mWebview.getEngine();
		// mWebEngine.load("https://www.amazon.com/dp/B07BNV325T#customerReviews");

		setupWebView();

		mWebEngine.load(urlStr);
	}

	@Override
	public void setupController(final Properties props, final Preferences appPreferences, Stage primaryStage,
			final Map<String, Object> paramsMap) {
		super.setupController(props, appPreferences, primaryStage, paramsMap);
		//
		paramsMap.put("appVersion", appVersion);
		final Object profileManagerO = paramsMap.get("profileManager");
		if (profileManagerO != null && profileManagerO instanceof ProfileManager) {
			profileManager = (ProfileManager) profileManagerO;
		}
		final Object selectedProfileO = paramsMap.get("selectedProfile");
		if (selectedProfileO != null && selectedProfileO instanceof Profile) {
			selectedProfile = (Profile) selectedProfileO;
			// textProfileLoaded.setText(selectedProfile.getKey());
		} else
			textProfileLoaded.setText("--None Loaded--");
		// loadData();
		setupData();
		// setupTableData();
	}

	private void clearSubLinkArea() {
		subLinkArea.getChildren().clear();
		textProfileLoaded.setText("--None Selected--");
		textUrl.setText("--none--");
	}

	private void clearWebArea() {
		// webview1.TODO Opne local page
	}

	private void setupSubLinkArea(final BookDataObj rowData) {
		// subLinkArea
		// TODO add buttons for each of the amazon locations, and goodreads?
		clearSubLinkArea();
		clearWebArea();
		//
		textProfileLoaded.setText(rowData.getBookTitle());
		//
		Button btn = null;

		// if it has an ASIN, do amazon link(s)
		// TODO put into config?
		//
		final Insets inset1 = new Insets(0, 0, 2, 0);
		//

		final String key = rowData.getBookKey();
		final String title = rowData.getBookTitle();
		final List<KeyValuePair> extList = rowData.getExternalIDlist();
		String asin = null;
		for (final KeyValuePair keyValuePair : extList) {
			if (keyValuePair.getKey().compareToIgnoreCase("asin") == 0) {
				asin = keyValuePair.getValue();
				break;
			}
		}
		if (asin != null) {
			createLinkButton("Amazon.com", "https://www.amazon.com/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.br", "https://www.amazon.com.br/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.jp", "https://www.amazon.co.jp/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.mx", "https://www.amazon.co.mx/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.de", "https://www.amazon.de/dp/%1$s", asin, subLinkArea, inset1);

			createLinkButton("Amazon.es", "https://www.amazon.es/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.fr", "https://www.amazon.fr/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.it", "https://www.amazon.it/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.nl", "https://www.amazon.nl/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.uk", "https://www.amazon.co.uk/dp/%1$s", asin, subLinkArea, inset1);

			createLinkButton("Amazon.au", "https://www.amazon.com.au/dp/%1$s", asin, subLinkArea, inset1);
			createLinkButton("Amazon.in", "https://www.amazon.in/dp/%1$s", asin, subLinkArea, inset1);
		}
		// if it has an XXX, do goodreads link(s)

	}

	@SuppressWarnings("unchecked")
	private void setupTableData() {
		LOGGER.debug("setupTableData: Called");
		final ObservableList<BookDataObj> newList = FXCollections.observableArrayList();

		//
		if (selectedProfile != null) {
			String asin = null;
			final List<KeyValuePair> extList = selectedProfile.getExternalIDs();
			if (extList != null)
				for (final KeyValuePair keyValuePair : extList) {
					if (keyValuePair.getKey().compareToIgnoreCase("asin") == 0) {
						asin = keyValuePair.getValue();
						break;
					}
				}

			BookDataObj tdata = new BookDataObj();
			tdata.setBookKey(selectedProfile.getKey());
			tdata.setBookTitle(selectedProfile.getMainTitle());
			tdata.setExternalIDlist(extList);
			tdata.setAsin(asin);
			newList.add(tdata);
		}

		// TODO all, from manager?
		if (profileManager != null) {
			final List<Profile> profiles = profileManager.getProfiles();
			for (final Profile profile : profiles) {
				String asin = null;
				final List<KeyValuePair> extList = profile.getExternalIDs();
				if (extList != null)
					for (final KeyValuePair keyValuePair : extList) {
						if (keyValuePair.getKey().compareToIgnoreCase("asin") == 0) {
							asin = keyValuePair.getValue();
							break;
						}
					}
				BookDataObj tdata = new BookDataObj();
				tdata.setBookKey(profile.getKey());
				tdata.setBookTitle(profile.getMainTitle());
				tdata.setExternalIDlist(extList);
				tdata.setAsin(asin);
				newList.add(tdata);
			}
		}

		// //
		// BookDataObj tdata = new BookDataObj();
		// tdata.setBookID("B01NC3KRUW");
		// tdata.setBookKey("test1");
		// tdata.setBookName("bookName1");
		// newList.add(tdata);
		// tdata = new BookDataObj();
		// tdata.setBookID("B07SHD1DLT");
		// tdata.setBookKey("test2");
		// tdata.setBookName("bookName2");
		// newList.add(tdata);

		// if (profileManager != null) {
		// final List<Profile> profiles = profileManager.getProfiles();
		// for (final Profile profile : profiles) {
		// final ProfileExportObj pobj = new ProfileExportObj();
		// pobj.setExists(true);
		// pobj.setExport(true);
		// pobj.setImportable(false);
		// pobj.setInputFile(profile.getInputFile());
		// pobj.setName(profile.getMainTitle());
		// pobj.setKey(profile.getKey());
		// pobj.setProfile(profile);
		// // pobj.setPayload(profile);
		// pobj.setSeries(profile.getSeriesTitle());
		// newList.add(pobj);
		// }
		// } else
		// LOGGER.warn("setupTableData: ProfileManager is null");

		LOGGER.debug("setupTableData: items# = " + newList.size());
		inputTable.getItems().clear();
		inputTable.getItems().setAll(newList);
		inputTable.refresh();
		LOGGER.debug("setupTableData: Done");
	}

	@Override
	void doCleanup() {
		// MAYBE: anything to cleanup?
	}

	@Override
	void lockGui() {
		// unlockAllButtons(outerMostContainer);
		// exportBtn.setDisable(true);
		// lockAllButtons(outerMostContainer);
		// closeBtn.setDisable(false);
		// browseBtn.setDisable(false);
	}

	@Override
	void unlockGui() {
		unlockAllButtons(outerMostContainer);
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// doCleanup();
		stage.close();
		LOGGER.debug("handleClose: Done");
	}

}
