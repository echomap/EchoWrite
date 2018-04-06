package com.echomap.kqf.two.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.OtherDocTagData;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

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
	@FXML
	private TableView inputTable;
	@FXML
	private TextArea inputDocTags;
	@FXML
	private Button buttonSaveAdd;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	private Preferences profileData = null;
	private FormatDao formatDao = null;

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
	}

	private void clearFields() {
		inputName.setText("");
		inputName.setEditable(true);
		inputFile.setText("");
		inputDocTags.setText("");
	}

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
			}
		}
		clearFields();
		LOGGER.debug("handleDelete: Done");
	}

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
		LOGGER.debug("handleAddEdit: Done");
	}

	public void handleSave(final ActionEvent event) {
		LOGGER.debug("handleSave: Called");

		final ObservableList<OtherDocTagData> targetList = inputTable.getItems();
		if (targetList != null) {
			for (OtherDocTagData otherDocTagData : targetList) {
				LOGGER.debug("item: " + otherDocTagData);
			}
		}
		final Type listType = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		final Gson gson = new Gson();
		String json = gson.toJson(targetList, listType);
		profileData.put("profileData", json);
		LOGGER.debug("handleSave: Done");
	}

	public void handleImport(final ActionEvent event) {
		LOGGER.debug("handleImport: Called");

		final FileChooser chooser = new FileChooser();
		final File file = chooser.showOpenDialog(new Stage());
		if (file == null) {
			showPopupMessage("No file selected.", false);
			return;
		}

		LOGGER.info("Reading file from: " + file);
		if (!file.exists()) {
			LOGGER.warn("No file exists, can't import!");
			showPopupMessage("Import Error! File doesn't exist!", true);
			return;
		}
		// final String stringData = readFile(inputFile.getText(), selCharSet)
		JsonArray profileDataset = null;
		String version = null;
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader(file));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("version")) {
					version = reader.nextString();
					LOGGER.debug("version=" + version);
				} else if (name.equals("list")) {
					profileDataset = readList(reader);
				} else {
					reader.skipValue(); // avoid some unhandle events
				}
			}
			// Save to list!
			loadTableDataFromJson(profileDataset);
			showPopupMessage("Imported data", false);
		} catch (IOException e) {
			LOGGER.error(e);
			showPopupMessage("Export Error!" + e.getMessage(), true);
		} finally {
			if (reader != null) {
				try {
					reader.endObject();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//
		LOGGER.debug("handleImportFile: Done");
		// loadTableData();
	}

	private JsonArray readList(final JsonReader reader) throws IOException {
		LOGGER.debug("readList: Called");
		final JsonArray exportProfiles = new JsonArray();
		reader.beginArray();
		while (reader.hasNext()) {
			//
			final JsonObject dataset = new JsonObject();
			reader.beginObject();
			while (reader.hasNext()) {
				String objectName = reader.nextName();
				// LOGGER.debug("readList: objectName = " + objectName);
				dataset.addProperty(objectName, reader.nextString());
			}
			reader.endObject();
			//
			LOGGER.debug("readList: dataset = " + dataset);
			exportProfiles.add(dataset);
		}
		reader.endArray();
		LOGGER.debug("readList: Done");
		return exportProfiles;
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");

		final FileChooser chooser = new FileChooser();
		final File file = chooser.showSaveDialog(new Stage());
		if (file == null) {
			showPopupMessage("No file selected.", false);
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
			final JsonObject exportDataset = XferBiz.ProfileDataExportFromMemory(inputTable.getItems(),appProps);
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
		this.profileData = child;
		this.formatDao = formatDao;
		final String listString = profileData.get("profileData", "");
		loadTableDataFromJson(listString);
	}

	public void loadTableDataFromJson(final String listString) {
		final Gson gson = new Gson();

		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		List<OtherDocTagData> list2 = gson.fromJson(listString, listOfTestObject);
		if (list2 != null) {
			for (OtherDocTagData otherDocTagData : list2) {
				LOGGER.debug("item: " + otherDocTagData);
			}
		}

		inputTable.getItems().clear();
		if (list2 != null)
			inputTable.getItems().setAll(list2);
	}

	private void loadTableDataFromJson(final JsonArray profileDataset) {
		final ObservableList<OtherDocTagData> newList = FXCollections.observableArrayList();

		for (int i = 0; i < profileDataset.size(); i++) {
			final JsonElement je = profileDataset.get(i);
			final JsonObject jo = je.getAsJsonObject();

			final String name = jo.get("name").getAsString();
			final String inputFile = jo.get("file").getAsString();
			final String docTags = jo.get("docTags").getAsString();
			LOGGER.debug("loadTableData: loaded row: '" + name + "'");
			final OtherDocTagData obj = new OtherDocTagData();
			obj.setName(name);
			obj.setFile(inputFile);
			obj.setDocTags(docTags);
			newList.add(obj);
		}
		inputTable.getItems().clear();
		if (newList != null)
			inputTable.getItems().setAll(newList);
		inputTable.refresh();
		// inputTable.setColumnResizePolicy(callback);
	}

}
