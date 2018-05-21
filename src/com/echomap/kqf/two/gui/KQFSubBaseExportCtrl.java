package com.echomap.kqf.two.gui;

import java.util.prefs.Preferences;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.ProfileExportObj;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class KQFSubBaseExportCtrl extends KQFBaseCtrl {
	private final static Logger LOGGER = LogManager.getLogger(KQFSubBaseExportCtrl.class);

	@SuppressWarnings("rawtypes")
	@FXML
	TableView inputTable;
	@FXML
	TextField inputFile;

	Preferences profileDataPrefs = null;
	FormatDao formatDao = null;

	// protected KQFSubBaseExportCtrl(final File lastSelectedDirectory, final
	// Stage primaryStage,
	// final Properties appProps) {
	// this.lastSelectedDirectory = lastSelectedDirectory;
	// this.primaryStage = primaryStage;
	// this.appProps = appProps;
	// }

	public void handleClose(final ActionEvent event) {
		LOGGER.debug("handleClose: Called");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		// doCleanup();
		stage.close();
	}

	public void handleSelectAll(final ActionEvent event) {
		LOGGER.debug("handleSelectAll: Called");
		@SuppressWarnings("unchecked")
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(true);
			}
		}
		inputTable.refresh();
	}

	public void handleSelectNone(final ActionEvent event) {
		LOGGER.debug("handleSelectNone: Called");
		@SuppressWarnings("unchecked")
		final ObservableList<ProfileExportObj> targetList = inputTable.getItems();
		if (targetList != null) {
			for (ProfileExportObj data : targetList) {
				data.setExport(false);
			}
		}
		inputTable.refresh();
	}

	Preferences getPrefs() {
		return profileDataPrefs;
	}

}
