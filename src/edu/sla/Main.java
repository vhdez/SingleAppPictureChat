package edu.sla;

import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class Main extends Application {

    // this queue is used for all communication between the GUI and the GUIUpdater
    static private SynchronizedQueue myQueue;
    static private ImageView RECEIVER_imageView;

    @Override
    public void start(final Stage stage) {
        stage.setTitle("My Picture Chat");

        final FileChooser fileChooser = new FileChooser();
        final ImageView SENDER_imageView = new ImageView();
        SENDER_imageView.setFitWidth(400);
        SENDER_imageView.setFitHeight(300);

        final Button SENDER_openButton = new Button("Open a Picture...");
        // This lambda is called whenever user presses the "Open a Picture..." button
        SENDER_openButton.setOnAction((event) -> {
            // Show a FileChooser
            File file = fileChooser.showOpenDialog(stage);

            // If user chose a file via FileChooser
            if (file != null) {
                Image newImage = new Image(file.toURI().toString());
                SENDER_imageView.setImage(newImage);
            }
        });

        final Button SENDER_sendButton = new Button("Send this picture");
        SENDER_sendButton.setOnAction((event) -> {
            Image imageToSend = SENDER_imageView.getImage();

            if (imageToSend != null) {
                while (!myQueue.put(imageToSend)) {
                    Thread.currentThread().yield();
                }
            }
        });

        RECEIVER_imageView.setFitWidth(400);
        RECEIVER_imageView.setFitHeight(300);

        VBox vertical = new VBox(12);
        HBox horizontal = new HBox(12);

        horizontal.getChildren().addAll(SENDER_openButton, SENDER_sendButton);
        vertical.getChildren().addAll(SENDER_imageView, horizontal, RECEIVER_imageView);

        stage.setScene(new Scene(vertical));
        stage.show();
    }

    public static void main(String[] args) {
        // Create the queue that will be used for communication
        // between the GUI thread that reacts to User Interaction and the GUIUpdater that updates the GUI
        myQueue = new SynchronizedQueue();

        RECEIVER_imageView = new ImageView();

        // Create and start the GUI updater thread
        GUIUpdater updater = new GUIUpdater(myQueue, RECEIVER_imageView);
        Thread updaterThread = new Thread(updater);
        updaterThread.start();

        // Start the GUI thread
        Application.launch(args);
    }

}