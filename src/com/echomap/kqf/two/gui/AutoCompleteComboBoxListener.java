package com.echomap.kqf.two.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

//https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx
// call with 
//new AutoCompleteComboBoxListener<>(comboBox);
public class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {

	@SuppressWarnings("rawtypes")
	private ComboBox comboBox;
	// private StringBuilder sb;
	private ObservableList<T> data;
	private ObservableList<T> dataOriginal;
	private boolean moveCaretToPos = false;
	private int caretPos;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AutoCompleteComboBoxListener(final ComboBox comboBox) {
		this.comboBox = comboBox;
		// sb = new StringBuilder();
		data = comboBox.getItems();
		dataOriginal = comboBox.getItems();

		this.comboBox.setEditable(true);
		this.comboBox.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent t) {
				comboBox.hide();
			}
		});
		this.comboBox.setOnKeyReleased(AutoCompleteComboBoxListener.this);
	}

	@SuppressWarnings("unchecked")
	public void reset() {
		data = dataOriginal;
		comboBox.setItems(data);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void handle(KeyEvent event) {

		// Codes for Editing purposes
		if (event.getCode() == KeyCode.UP) {
			caretPos = -1;
			moveCaret(comboBox.getEditor().getText().length());
			return;
		} else if (event.getCode() == KeyCode.DOWN) {
			if (!comboBox.isShowing()) {
				comboBox.show();
			}
			caretPos = -1;
			moveCaret(comboBox.getEditor().getText().length());
			return;
		} else if (event.getCode() == KeyCode.BACK_SPACE) {
			moveCaretToPos = true;
			caretPos = comboBox.getEditor().getCaretPosition();
		} else if (event.getCode() == KeyCode.DELETE) {
			moveCaretToPos = true;
			caretPos = comboBox.getEditor().getCaretPosition();
		}

		// These codes should just be ignored to let the user do what they want
		if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT || event.isControlDown()
				|| event.getCode() == KeyCode.HOME || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB
				|| event.getCode() == KeyCode.CONTROL || event.getCode() == KeyCode.SHIFT
				|| event.getCode() == KeyCode.ESCAPE) {
			return;
		}

		// Process List
		final ObservableList list = FXCollections.observableArrayList();
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).toString().toLowerCase()
					.contains(AutoCompleteComboBoxListener.this.comboBox.getEditor().getText().toLowerCase())) {
				list.add(data.get(i));
			}
		}
		final String t = comboBox.getEditor().getText();
		comboBox.setItems(list);
		comboBox.getEditor().setText(t);
		if (!moveCaretToPos) {
			caretPos = -1;
		}
		moveCaret(t.length());
		if (!list.isEmpty()) {
			comboBox.show();
		}
	}

	private void moveCaret(final int textLength) {
		if (caretPos == -1) {
			comboBox.getEditor().positionCaret(textLength);
		} else {
			comboBox.getEditor().positionCaret(caretPos);
		}
		moveCaretToPos = false;
	}

}