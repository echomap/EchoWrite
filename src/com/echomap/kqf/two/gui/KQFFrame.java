package com.echomap.kqf.two.gui;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class KQFFrame extends Application {

	private static final String FXML_FILE1 = "/mainform.fxml";
	private static final String FXML_FILE2 = "/mainform_flow.fxml";
	private static final String FXML_FILE = FXML_FILE2;

	public static void main(String[] args) {
		Application.launch(KQFFrame.class, args);
	}

	@Override
	public void start(final Stage primaryStage) {
		Parent parent = null;
		final FXMLLoader fxmlLoader = new FXMLLoader();
		try {
			// parent = fxmlLoader.load(CWCFrame.class.getResource(FXML_FILE));
			final URL location = getClass().getResource(FXML_FILE);
			if (location == null) {
				System.out.println("Failed to get location!!!!!!");
			}
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			parent = (Parent) fxmlLoader.load(location.openStream());

			primaryStage.setTitle("EBook Kindle Formatter (MYK)");
			// primaryStage.setWidth(1024);
			// primaryStage.setHeight(200);

			final Scene scene = new Scene(parent);
			primaryStage.setScene(scene);

			//
			final KQFCtrl myController = (KQFCtrl) fxmlLoader.getController();

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					System.out.println("Stage is cleaning up...");
					// myController.saveProps();
					myController.doCleanup();
					System.out.println("Stage is closing");
				}
			});

			primaryStage.show();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
