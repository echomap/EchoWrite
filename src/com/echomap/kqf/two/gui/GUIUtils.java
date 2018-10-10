package com.echomap.kqf.two.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.javafx.scene.control.skin.TableViewSkin;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * https://stackoverflow.com/questions/14650787/javafx-column-in-tableview-auto-
 * fit-size
 * 
 * @author mkatz
 */
public class GUIUtils {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void autoFitTable(final TableView tableView) {
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
						columnToFitMethod.invoke(tableView.getSkin(), column, -1);
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public static void alignColumnLabelsLeftHack(final TableView inputTable) {
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

}
