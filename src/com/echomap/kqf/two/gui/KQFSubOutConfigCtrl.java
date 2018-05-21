package com.echomap.kqf.two.gui;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.biz.XferBiz;
import com.echomap.kqf.data.DocTagDataOption;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.OtherDocTagData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.prism.impl.Disposer.Record;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

public class KQFSubOutConfigCtrl extends KQFBaseCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFSubOutConfigCtrl.class);

	@FXML
	private ComboBox<String> comboBox1;

	@SuppressWarnings("rawtypes")
	@FXML
	private TableView inputTable;
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

	private Preferences profileData = null;
	@SuppressWarnings("unused")
	private FormatDao formatDao = null;

	List<OtherDocTagData> comboBoxSourceData = null;

	@FXML
	void initialize() {
		//
		setupInputTable();

		comboBox1.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
				loadDataFromSelect();
			}
		});

	}

	@SuppressWarnings("unchecked")
	private void setupInputTable() {
		final TableColumn<Object, Object> firstCol = new TableColumn<Object, Object>("Tag");
		firstCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		final TableColumn<Object, Object> expandedCol = new TableColumn<Object, Object>("Expanded");
		expandedCol.setCellValueFactory(new PropertyValueFactory<>("showExpand"));
		final TableColumn<Object, Object> compressCol = new TableColumn<Object, Object>("Compress");
		compressCol.setCellValueFactory(new PropertyValueFactory<>("showCompress"));
		final TableColumn<Object, Object> prefixCol = new TableColumn<Object, Object>("Prefix");
		prefixCol.setCellValueFactory(new PropertyValueFactory<>("prefix"));
		//
		inputTable.getColumns().clear();
		inputTable.getColumns().addAll(firstCol, compressCol, expandedCol, prefixCol);

		//
		inputTable.getItems().clear();
		// inputTable.getItems().setAll(parseDataList());

		inputTable.setRowFactory(new Callback<TableView<DocTagDataOption>, TableRow<DocTagDataOption>>() {
			@Override
			public TableRow<DocTagDataOption> call(TableView<DocTagDataOption> tableView) {
				final TableRow<DocTagDataOption> row = new TableRow<>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							final Object rowData = row.getItem();
							LOGGER.debug("rowData: " + rowData);
							// TODO Enable buttons
							// inputName.setText(rowData.getName());
							// inputName.setEditable(false);
							// inputFile.setText(rowData.getFile());
							// inputDocTags.setText(rowData.getDocTags());
						} else if (event.isSecondaryButtonDown()) {
							// right click code here
						}
					}
				});
				return row;
			}
		});

		// Callback<TableColumn, TableCell> booleanCellFactory = new
		// Callback<TableColumn, TableCell>() {
		// @Override
		// public TableCell call(TableColumn p) {
		// MyBooleanTableCell cell = new MyBooleanTableCell();
		// cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
		// return cell;
		// }
		// };

		Callback<TableColumn<Object, Object>, TableCell<Object, Object>> booleanCellFactory = new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
			@Override
			public TableCell call(TableColumn<Object, Object> param) {
				LOGGER.debug("tableCellFact param: " + param.getId());
				MyBooleanTableCell cell = new MyBooleanTableCell();
				LOGGER.debug("created cell: " + cell.getItem());
				cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new MyBooleanFlipEventHandler());
				// cell.get
				return cell;
				// return null;
			}
		};

		// expandedCol
		// compressCol
		expandedCol.setCellFactory(booleanCellFactory);
		compressCol.setCellFactory(booleanCellFactory);

		// prefixCol

		inputTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	class MyBooleanTableCell extends TableCell<Record, Boolean> {

		@Override
		public void updateItem(Boolean item, boolean empty) {
			super.updateItem(item, empty);
			setText(empty ? null : getString());
			setGraphic(null);
			LOGGER.debug("item cell: " + getItem());
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	class MyBooleanFlipEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent t) {
			TableCell c = (TableCell) t.getSource();
			int index = c.getIndex();

			LOGGER.debug("item = " + c.getItem());
			// c.getContextMenu();
			LOGGER.debug("id = " + c.getId());
			// c.getParent();
			// c.getProperties();
			LOGGER.debug("text = " + c.getText());
			LOGGER.debug("col Text = " + c.getTableColumn().getText());
			LOGGER.debug("col ID= " + c.getTableColumn().getId());
			final TableColumn col = c.getTableColumn();

			@SuppressWarnings("unchecked")
			final ObservableList<DocTagDataOption> recordList = inputTable.getItems();
			DocTagDataOption thisRecord = recordList.get(index);
			LOGGER.debug("name = " + thisRecord.getName());
			LOGGER.debug("prefix = " + thisRecord.getPrefix());
			LOGGER.debug("expand = " + thisRecord.isShowExpand());
			LOGGER.debug("compress = " + thisRecord.isShowCompress());

			try {
				final Boolean origBool = (Boolean) c.getItem();
				LOGGER.debug("origBool = " + origBool);
				final Boolean newBool = !origBool;
				LOGGER.debug("newBool = " + newBool);
				// c.setItem(newBool);
				switch (col.getText()) {
				case "Compress":
					thisRecord.setShowCompress(newBool);
					break;
				case "Expanded":
					thisRecord.setShowExpand(newBool);
					break;
				}
				inputTable.refresh();
			} catch (Exception e) {
				LOGGER.error("Not a boolean value, can't flip as, '" + c.getItem() + "'", e);
				e.printStackTrace();
			}

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
		// final OtherDocTagData selObj = (OtherDocTagData)
		// inputTable.getSelectionModel().getSelectedItem();
		// if (selObj != null) {
		// final ObservableList<OtherDocTagData> newList =
		// FXCollections.observableArrayList();
		// final ObservableList<OtherDocTagData> targetList =
		// inputTable.getItems();
		// if (targetList != null) {
		// for (OtherDocTagData otherDocTagData : targetList) {
		// if (!selObj.getName().equals(otherDocTagData.getName()))
		// newList.add(otherDocTagData);
		// }
		// // inputTable.setItems(newList);
		// inputTable.getItems().setAll(newList);
		// actionToCancel();
		// }
		// }
		// clearFields();
		LOGGER.debug("handleDelete: Done");
	}

	@SuppressWarnings("unchecked")
	public void handleSave(final ActionEvent event) {
		LOGGER.debug("handleSave: Called");

		final ObservableList<DocTagDataOption> targetList = inputTable.getItems();
		final Map<String, DocTagDataOption> mapList = new HashMap<>();
		if (targetList != null) {
			for (DocTagDataOption otherDocTagData : targetList) {
				LOGGER.debug("item: " + otherDocTagData);
				mapList.put(otherDocTagData.getName(), otherDocTagData);
			}
		}
		final Type listType = new TypeToken<Map<String, DocTagDataOption>>() {
		}.getType();
		final Gson gson = new Gson();
		String json = gson.toJson(mapList, listType);
		LOGGER.debug("handleSave: json:'" + json + "'");
		// TODO profileData.put(XferBiz.PROFILEOPTION_DATA, json);
		actionToNormal();
		LOGGER.debug("handleSave: Done");
	}

	public void handleImport(final ActionEvent event) {
		LOGGER.debug("handleImport: Called");
		//
		LOGGER.debug("handleImportFile: Done");
	}

	public void handleExport(final ActionEvent event) {
		LOGGER.debug("handleExport: Called");
		//
		LOGGER.debug("handleExport: Done");
	}

	// Called after creation by Parent
	public void setProfileLoaded(final Preferences child, final FormatDao formatDao, final Properties appProps,
			final Stage stage) {
		this.appProps = appProps;
		this.primaryStage = stage;
		this.profileData = child;
		this.formatDao = formatDao;

		final String listString0 = profileData.get(XferBiz.PROFILE_DATA, "");
		loadComboBoxFromJson(listString0);
		// Select 0
		comboBox1.getSelectionModel().select(0);
		// Populate Table
		// final String listString1 = profileData.get(XferBiz.PROFILE_DATA, "");
		// loadTableDataFromJson(listString1);
	}

	private void clearFields() {
		// TODO disable buttons
		// inputName.setText("");
		// inputName.setEditable(true);
		// inputFile.setText("");
		// inputDocTags.setText("");
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

	@SuppressWarnings("unchecked")
	private void loadDataFromSelect() {
		LOGGER.debug("loadDataFromSelect: Called");

		// load table with DocTags for this MoreFiles entry
		final String key = comboBox1.getSelectionModel().getSelectedItem();

		OtherDocTagData selItem = null;
		for (OtherDocTagData otherDocTagData : comboBoxSourceData) {
			if (otherDocTagData.getName() == key) {
				selItem = otherDocTagData;
			}
		}
		LOGGER.debug("loadDataFromSelect: selItem = " + selItem);
		if (selItem != null) {
			// TODO!!
			final Map<String, DocTagDataOption> optionList = selItem.getOptions();
			//
			// loadMoreFilesOptionsFromPrefs(selItem);
			// for (DocTagDataOption option : optionList) {
			// //option.set
			// }

			// final List<String> dtList = selItem.getDocTagsList();
			// final List<DataItemMFC> dataList = new ArrayList<>();
			// for (String str : dtList) {
			// final DataItemMFC obj = new DataItemMFC();
			// obj.setTag(str);
			// // TODO read options!
			// obj.setExpand(false);
			// obj.setCompress(false);
			// obj.setPrefix("");
			// dataList.add(obj);
			// }

			inputTable.getItems().clear();
			if (optionList != null) {
				final Collection<DocTagDataOption> list = optionList.values();
				inputTable.getItems().setAll(list);
			}
		}
		LOGGER.debug("loadDataFromSelect: Done");
	}

	// private Map<String, DocTagDataOption> loadMoreFilesOptionsFromPrefs(final
	// OtherDocTagData selItem) {
	// final String listString = profileData.get(XferBiz.PROFILEOPTION_DATA,
	// "");
	// LOGGER.debug("loadMoreFilesOptionsFromPrefs: listString: " + listString);
	// final Gson gson = new Gson();
	//
	// final Type listOfTestObject = new TypeToken<Map<String,
	// DocTagDataOption>>() {
	// }.getType();
	//
	// Map<String, DocTagDataOption> list = gson.fromJson(listString,
	// listOfTestObject);
	// if (list != null && list.size() > 0) {
	// LOGGER.debug("loadMoreFilesOptionsFromPrefs: got a list from
	// preferences");
	// // for (DocTagDataOption dataitem : list) {
	// // LOGGER.debug("loadMoreFilesOptionsFromPrefs: dataitem: " +
	// // dataitem);
	// // }
	// } else {
	// LOGGER.debug("loadMoreFilesOptionsFromPrefs: adding defaults");
	// list = selItem.getOptions();
	// }
	//
	// // TODO defaults?
	// return list;
	// }

	private void loadComboBoxFromJson(final String listString) {
		LOGGER.debug("loadComboBoxFromJson: listString: " + listString);
		final Gson gson = new Gson();

		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		comboBoxSourceData = gson.fromJson(listString, listOfTestObject);
		if (comboBoxSourceData != null) {
			for (OtherDocTagData otherDocTagData : comboBoxSourceData) {
				LOGGER.debug("item: " + otherDocTagData);
			}
		}

		comboBox1.getItems().clear();
		comboBox1.getItems().add("Outline");
		comboBox1.getItems().add("Scene");
		if (comboBoxSourceData != null) {
			// final List<String> list = new ArrayList<>();
			for (OtherDocTagData odt : comboBoxSourceData) {
				comboBox1.getItems().add(odt.getName());
			}
		}
	}

	//
	// @SuppressWarnings("unchecked")
	// public void loadTableDataFromJson(final String listString) {
	// LOGGER.debug("loadTableDataFromJson: listString: " + listString);
	// final Gson gson = new Gson();
	//
	// final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
	// }.getType();
	// List<OtherDocTagData> list2 = gson.fromJson(listString,
	// listOfTestObject);
	// if (list2 != null) {
	// for (OtherDocTagData otherDocTagData : list2) {
	// LOGGER.debug("item: " + otherDocTagData);
	// }
	// }
	//
	// inputTable.getItems().clear();
	// if (list2 != null)
	// inputTable.getItems().setAll(list2);
	// inputTable.refresh();
	// // inputTable.getColumnResizePolicy()
	// inputTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	// }

}
