package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.KqfBiz;
import com.echomap.kqf.biz.XferBiz;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.OtherDocTagData;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class KQFSubCtrl extends KQFBaseCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFSubCtrl.class);

	@FXML
	private TextField inputName;
	@FXML
	private TextField inputFile;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableView inputTable;
	@FXML
	private TextArea inputDocTags;
	@FXML
	private Button buttonSaveAdd;
	@FXML
	private Button buttonClose;
	@FXML
	private Button buttonSave;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	private Preferences profileDataPref = null;
	private FormatDao formatDao = null;

	private List<OtherDocTagData> sourceDataList = null;

	@SuppressWarnings("unchecked")
	@FXML
	void initialize() {
		//
		final TableColumn<Object, Object> firstCol = new TableColumn<Object, Object>("Name");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn<Object, Object> secondCol = new TableColumn<Object, Object>("File");
		secondCol.setCellValueFactory(new PropertyValueFactory<>("file"));
		final TableColumn<Object, Object> thirdCol = new TableColumn<Object, Object>("DocTags");
		thirdCol.setCellValueFactory(new PropertyValueFactory<>("docTags"));
		//
		inputTable.getColumns().clear();
		inputTable.getColumns().addAll(firstCol, secondCol, thirdCol);

		//
		inputTable.getItems().clear();
		// inputTable.getItems().setAll(parseDataList());

		inputTable.setRowFactory(new Callback<TableView<OtherDocTagData>, TableRow<OtherDocTagData>>() {
			@Override
			public TableRow<OtherDocTagData> call(TableView<OtherDocTagData> tableView) {
				final TableRow<OtherDocTagData> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final OtherDocTagData rowData = row.getItem();
							LOGGER.debug("rowData: " + rowData);
							inputName.setText(rowData.getName());
							inputName.setEditable(false);
							inputFile.setText(rowData.getFile());
							inputDocTags.setText(rowData.getDocTags());

						} else if (event.isSecondaryButtonDown()) {
							// right click code here
						}
					}
				});
				return row;
			}
		});

		// inputTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// Works but only java8+
		// inputTable.setRowFactory(tv -> {
		// final TableRow<OtherDocTagData> row = new TableRow<>();
		// row.setOnMouseClicked(event -> {
		// if (event.getClickCount() == 2 && (!row.isEmpty())) {
		// OtherDocTagData rowData = row.getItem();
		// LOGGER.debug("rowData: " + rowData);
		// inputName.setText(rowData.getName());
		// inputName.setEditable(false);
		// inputFile.setText(rowData.getFile());
		// inputDocTags.setText(rowData.getDocTags());
		// }
		// });
		// return row;
		// });

	}

	// private List<OtherDocTagData> parseDataList() {
	// final ArrayList<OtherDocTagData> list = new ArrayList<OtherDocTagData>();
	// //
	// final OtherDocTagData o1 = new OtherDocTagData();
	// o1.setDocTags("doctag1, doctag2");
	// o1.setFile("Null File");
	// o1.setName("Test1");
	// list.add(o1);
	// //
	// return list;
	// }

	public void handleBrowse(final ActionEvent event) {
		LOGGER.debug("handleBrowse: Called");
		String newFile = null;
		String filePrefixText = formatDao.getFilePrefix();
		// final OtherDocTagData odtData = (OtherDocTagData)
		// inputTable.getSelectionModel().getSelectedItem();
		String fileData = inputFile.getText();
		// if (odtData != null) {
		if (fileData.trim().length() < 1)
			newFile = locateDir(event, "Open Output Dir ", fileData, formatDao.getInputFilename());
		else
			newFile = locateDir(event, "Open Output Dir ", fileData, fileData);
		if (newFile != null) {
			final String outFilename = filePrefixText == null ? "/" + inputName.getText() + ".txt"
					: filePrefixText + inputName.getText() + ".txt";
			final File nFile = new File(newFile, outFilename);
			inputFile.setText(nFile.getAbsolutePath());
			actionToCancel();
		}
	}

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// doCleanup();
		stage.close();
	}

	public void handleNew(final ActionEvent event) {
		LOGGER.debug("handleNew: Called");
		clearFields();
		actionToCancel();
	}

	@SuppressWarnings("unchecked")
	public void handleDelete(final ActionEvent event) {
		LOGGER.debug("handleDelete: Called");
		final OtherDocTagData selObj = (OtherDocTagData) inputTable.getSelectionModel().getSelectedItem();
		if (selObj != null) {
			final ObservableList<OtherDocTagData> newList = FXCollections.observableArrayList();
			final ObservableList<OtherDocTagData> targetList = inputTable.getItems();
			if (targetList != null) {
				for (OtherDocTagData otherDocTagData : targetList) {
					if (!selObj.getName().equals(otherDocTagData.getName()))
						newList.add(otherDocTagData);
				}
				// inputTable.setItems(newList);
				inputTable.getItems().setAll(newList);
				actionToCancel();
			}
		}
		clearFields();
		LOGGER.debug("handleDelete: Done");
	}

	@SuppressWarnings("unchecked")
	public void handleAddEdit(final ActionEvent event) {
		LOGGER.debug("handleAddEdit: Called");
		OtherDocTagData selObj = null;

		final ObservableList<OtherDocTagData> targetList = inputTable.getItems();
		if (targetList != null) {
			for (OtherDocTagData otherDocTagData : targetList) {
				if (otherDocTagData.getName().equals(inputName.getText()))
					selObj = otherDocTagData;
			}
		}
		if (selObj == null) {
			selObj = new OtherDocTagData();
			selObj.setName(inputName.getText());
			targetList.add(selObj);
		}
		selObj.setFile(inputFile.getText());
		selObj.setDocTags(inputDocTags.getText());

		for (OtherDocTagData otherDocTagData : targetList) {
			LOGGER.debug("item: " + otherDocTagData);
		}
		// inputTable.getItems().clear();
		// inputTable.getItems().setAll(targetList);
		inputTable.setItems(targetList);
		clearFields();
		inputTable.refresh();
		actionToCancel();
		LOGGER.debug("handleAddEdit: Done");
	}

	public void handleSave(final ActionEvent event) {
		LOGGER.debug("handleSave: Called");

		@SuppressWarnings("unchecked")
		final ObservableList<OtherDocTagData> targetList = inputTable.getItems();
		final List<OtherDocTagData> removeList = new ArrayList<>();
		if (targetList != null) {
			for (OtherDocTagData otherDocTagData : targetList) {
				LOGGER.debug("item: " + otherDocTagData);
				if (StringUtils.isEmpty(otherDocTagData.getName()))
					removeList.add(otherDocTagData);
				if (otherDocTagData.getFile() == null)
					removeList.add(otherDocTagData);
			}
		}
		for (OtherDocTagData otherDocTagData : removeList) {
			targetList.remove(otherDocTagData);
		}
		final Type listType = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		final Gson gson = new Gson();
		String json = gson.toJson(targetList, listType);
		LOGGER.debug("handleSave: json: '" + json + "'");
		profileDataPref.put(XferBiz.PROFILE_DATA, json);
		actionToNormal();
		LOGGER.debug("handleSave: Done");
	}

	// private File getInitialFolder() {
	// final String profile = this.profileData.get("titleOne", "");
	//
	// final String filePrefixText = formatDao.getFilePrefix();
	// final String iStrFile = this.profileData.get("inputFile", null);
	// final File inFile = (StringUtils.isBlank(iStrFile) ? null : new
	// File(iStrFile));
	// final File inPFile = (inFile != null ? inFile.getParentFile() : null);
	// final String outFilename = filePrefixText == null ? "/" + profile +
	// "_more.json"
	// : filePrefixText + profile + "_more.json";
	// final File oFile = new File(inPFile, outFilename);
	// final File oFold = oFile.getParentFile();
	// LOGGER.debug("handleExport: oFold: " + oFold);
	// LOGGER.debug("handleExport: oFile: " + oFile);
	// LOGGER.debug("handleExport: outFilename: " + outFilename);
	// LOGGER.debug("handleExport: inPFile: " + inPFile);
	// LOGGER.debug("handleExport: iStrFile: " + iStrFile);
	// LOGGER.debug("handleExport: filePrefixText: " + filePrefixText);
	// return oFold;
	// }

	public void handleImport(final ActionEvent event) {
		LOGGER.debug("handleImport: Called");

		final FileChooser chooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON", "*.json");
		chooser.getExtensionFilters().add(extFilter);
		final String profile = this.profileDataPref.get("titleOne", "");
		chooser.setInitialFileName(profile + "_more.json");
		final File oFold = KqfBiz.getInitialFolder(profileDataPref, formatDao);
		chooser.setInitialDirectory(oFold);

		// Show
		final File file = chooser.showOpenDialog(new Stage());
		if (file == null) {
			showPopupMessage("Failed", "No file selected.", false);
			return;
		}

		LOGGER.info("Reading file from: " + file);
		if (!file.exists()) {
			LOGGER.warn("No file exists, can't import!");
			showPopupMessage("Import Error! File doesn't exist!", true);
			return;
		}
		try {
			final JsonArray profileDataset = XferBiz.readMoreExport(file);
			// // final String stringData = readFile(inputFile.getText(),
			// selCharSet)
			// JsonArray profileDataset = null;
			// String version = null;
			// JsonReader reader = null;
			// try {
			// reader = new JsonReader(new FileReader(file));
			// reader.beginObject();
			// while (reader.hasNext()) {
			// String name = reader.nextName();
			// if (name.equals("version")) {
			// version = reader.nextString();
			// LOGGER.debug("version=" + version);
			// } else if (name.equals("list")) {
			// // profileDataset = KqfBiz.readSimpleJsonList(reader);
			// profileDataset = XferBiz.readMoreExport(reader);
			// } else {
			// reader.skipValue(); // avoid some unhandle events
			// }
			// }
			// Fix Data??
			// String newFile = null;
			// String filePrefixText = formatDao.getFilePrefix();
			// String fileData = inputFile.getText();
			// if (fileData.trim().length() < 1)
			// newFile = locateDir(event, "Open Output Dir ", fileData,
			// formatDao.getInputFilename());
			// else
			// newFile = locateDir(event, "Open Output Dir ", fileData,
			// fileData);
			// if (newFile != null) {
			// final String outFilename = filePrefixText == null ? "/" +
			// inputName.getText() + ".txt"
			// : filePrefixText + inputName.getText() + ".txt";
			// final File nFile = new File(newFile, outFilename);
			// newFile = nFile.getAbsolutePath();
			// }
			// Save to list!
			loadTableDataFromJson(profileDataset, true);
			showPopupMessage("Imported data, Remember to\n1)check the file names for each entry.\n2)to SAVE the table.",
					false);
			actionToCancel();
		} catch (IOException e) {
			LOGGER.error(e);
			showPopupMessage("Export Error!" + e.getMessage(), true);
			// } finally {
			// if (reader != null) {
			// try {
			// reader.endObject();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// try {
			// reader.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
		}
		//
		LOGGER.debug("handleImportFile: Done");
		// loadTableData();
	}

	// private JsonArray readList(final JsonReader reader) throws IOException {
	// LOGGER.debug("readList: Called");
	// final JsonArray exportProfiles = new JsonArray();
	// reader.beginArray();
	// while (reader.hasNext()) {
	// //
	// final JsonObject dataset = new JsonObject();
	// reader.beginObject();
	// while (reader.hasNext()) {
	// String objectName = reader.nextName();
	// // LOGGER.debug("readList: objectName = " + objectName);
	// dataset.addProperty(objectName, reader.nextString());
	// }
	// reader.endObject();
	// //
	// LOGGER.debug("readList: dataset = " + dataset);
	// exportProfiles.add(dataset);
	// }
	// reader.endArray();
	// LOGGER.debug("readList: Done");
	// return exportProfiles;
	// }

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

		final FileChooser chooser = new FileChooser();
		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON", "*.json");
		chooser.getExtensionFilters().add(extFilter);
		final String profile = this.profileDataPref.get("titleOne", "");
		chooser.setInitialFileName(profile + "_more.json");
		final File oFold = KqfBiz.getInitialFolder(profileDataPref, formatDao);
		chooser.setInitialDirectory(oFold);

		// show
		final File file = chooser.showSaveDialog(new Stage());
		if (file == null) {
			showPopupMessage("Failed", "No file selected.", false);
			return;
		}
		Writer fWriterPlain = null;
		try {
			LOGGER.info("Writing export file to: " + file);

			// open file
			final Charset selCharSet = formatDao.getCharSet();
			LOGGER.debug("handleExport: Charset chosen: " + selCharSet);
			fWriterPlain = new OutputStreamWriter(new FileOutputStream(file), selCharSet);

			//
			// final Gson gson = new Gson();
			final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
					.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			// final JsonObject exportDataset = new JsonObject();
			@SuppressWarnings("unchecked")
			final JsonObject exportDataset = XferBiz.ProfileDataExportFromMemory(inputTable.getItems(), appProps);
			LOGGER.debug("handleExport: exportDataset: " + exportDataset);
			// final JsonArray exportList = new JsonArray();
			// // write data
			// final ObservableList<OtherDocTagData> targetList =
			// inputTable.getItems();
			// if (targetList != null) {
			// for (OtherDocTagData odtd : targetList) {
			// final JsonObject dataset = new JsonObject();
			// dataset.addProperty("name", odtd.getName());
			// dataset.addProperty("file", odtd.getFile());
			// dataset.addProperty("docTags", odtd.getDocTags());
			// // exportList.add(gson2.toJson(odtd,OtherDocTagData.class));
			// exportList.add(dataset);
			// }
			// }
			// exportDataset.add("list", exportList);
			fWriterPlain.write(gson2.toJson(exportDataset));
			showPopupMessage("Exported data", false);
		} catch (IOException e) {
			LOGGER.error(e);
			showPopupMessage("Export Error!" + e.getMessage(), true);
		} finally {
			if (fWriterPlain != null)
				try {
					fWriterPlain.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void setProfileLoaded(final Preferences child, final FormatDao formatDao, final Properties appProps,
			final Stage stage) {
		this.appProps = appProps;
		this.primaryStage = stage;
		this.profileDataPref = child;
		this.formatDao = formatDao;
		final String listString = profileDataPref.get(XferBiz.PROFILE_DATA, "");
		loadTableDataFromJson(listString);

		// final String pdString = XferBiz.objectToJson(profileData);
		// LOGGER.debug("ProfileData: " + pdString);
	}

	@SuppressWarnings("unchecked")
	private void loadTableDataFromJson(final String listString) {
		LOGGER.debug("loadTableDataFromJson: Called");
		final Gson gson = new Gson();

		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		sourceDataList = gson.fromJson(listString, listOfTestObject);
		if (sourceDataList != null) {
			for (OtherDocTagData otherDocTagData : sourceDataList) {
				// otherDocTagData.getOptions();
				// LOGGER.debug("item: " + otherDocTagData);
				// LOGGER.debug("itemJson: " +
				// XferBiz.objectToJson(otherDocTagData));
			}
		}

		inputTable.getItems().clear();
		if (sourceDataList != null)
			inputTable.getItems().setAll(sourceDataList);
		LOGGER.debug("loadTableDataFromJson: Done");
	}

	private void actionToCancel() {
		buttonClose.setText("Cancel");
		buttonSave.setText("Save Data");
		buttonSave.setDisable(false);
	}

	private void actionToNormal() {
		buttonClose.setText("Close");
		buttonSave.setText("Save Data");
		buttonSave.setDisable(true);
	}

	private void clearFields() {
		inputName.setText("");
		inputName.setEditable(true);
		inputFile.setText("");
		inputDocTags.setText("");
	}

	@SuppressWarnings("unchecked")
	private void loadTableDataFromJson(final JsonArray profileDataset) {
		final ObservableList<OtherDocTagData> newList = XferBiz.readInOtherDocTags(profileDataset);
		//
		// final ObservableList<OtherDocTagData> newList =
		// FXCollections.observableArrayList();
		//
		// for (int i = 0; i < profileDataset.size(); i++) {
		// final JsonElement je = profileDataset.get(i);
		// final JsonObject jo = je.getAsJsonObject();
		//
		// final String name = jo.get("name").getAsString();
		// final String inputFile = jo.get("file").getAsString();
		// final String docTags = jo.get("docTags").getAsString();
		//
		// // final String optionsJson = jo.get("optionsJson").getAsString();
		// final JsonArray jsOptions = jo.get("options").getAsJsonArray();//
		//
		// // final JsonArray jsOptions = (JsonArray) jsOptionsE;
		// // final DocTagDataOption options
		//
		// LOGGER.debug("loadTableData: loaded row: '" + name + "'");
		// final OtherDocTagData obj = new OtherDocTagData();
		// obj.setName(name);
		// obj.setFile(inputFile);
		// obj.setDocTags(docTags);
		// for (int j = 0; j < jsOptions.size(); j++) {
		// final JsonElement elem = jsOptions.get(i);
		// // final String json = elem.getAsString();
		// final DocTagDataOption option = (DocTagDataOption)
		// XferBiz.loadDataFromJson(elem,
		// DocTagDataOption.class);
		// LOGGER.debug("loadTableData: option: " + option);
		// LOGGER.debug("loadTableData: obj: " + obj);
		//
		// obj.addOption(option);
		// }
		//
		// newList.add(obj);
		// }
		inputTable.getItems().clear();
		if (newList != null)
			inputTable.getItems().setAll(newList);
		inputTable.refresh();
		// inputTable.setColumnResizePolicy(callback);
	}

	@SuppressWarnings("unchecked")
	private void loadTableDataFromJson(final JsonArray profileDataset, final boolean fixPaths) {
		final ObservableList<OtherDocTagData> newList = XferBiz.readInOtherDocTags(profileDataset);
		if (fixPaths) {
			final String filePrefixText = formatDao.getFilePrefix();
			final String inFilename = formatDao.getInputFilename();
			final File inFile = new File(inFilename);
			final File parentDir = inFile.getParentFile();
			// Fix Data
			for (final OtherDocTagData odtd : newList) {
				final String oName = odtd.getName();
				LOGGER.debug("loadTableData: File Origin: " + odtd.getFile());
				final String outFilename = filePrefixText == null ? "/" + oName + ".txt"
						: filePrefixText + oName + ".txt";
				final File nFile = new File(parentDir, outFilename);
				odtd.setFile(nFile.getAbsolutePath());
				LOGGER.debug("loadTableData: File Final: " + odtd.getFile());
			}
		}
		//
		inputTable.getItems().clear();
		if (newList != null)
			inputTable.getItems().setAll(newList);
		inputTable.refresh();
	}

}
