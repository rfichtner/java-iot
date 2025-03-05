package com.example.subscribefx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class HelloApplication extends Application {

    static Button startButton;

    @Override
    public void start(Stage stage) throws IOException {
        VBox holder = new VBox();
        TextArea textArea = new TextArea();
        ImageView imageView = new ImageView();
        FlowPane flowPane = new FlowPane();
        Button loadPhonesButton = new Button("Load Phones");
        Region smallRegion = new Region();
        Button clearPhonesButton = new Button("Clear Phones");
        Region smallRegion2 = new Region();
        PasswordField removePhoneField = new PasswordField();
        Button removePhoneButton = new Button("Remove Phone");
        Region bigRegion = new Region();
        startButton = new Button("Start");
        Region region = new Region();
        Button stopButton = new Button("Quit");
        Region bigRegion2 = new Region();
        Label ipLabel = new Label();
        Scene scene = new Scene(holder);

        textArea.setFont(Font.font("Times New Roman", 32));
        textArea.appendText("Phones NOT loaded yet.\nStart button NOT pressed yet.\n");
        holder.getChildren().add(textArea);

        Image image = new Image("file:///Users/bburd/Documents/IoT_Java" +
                "/SubscribeFX/src/main/resources/com/example/subscribefx" +
                "/Laptop.png");
        imageView.setImage(image);
        holder.getChildren().add(imageView);

        loadPhonesButton.setOnAction(e -> {
            Subscribe.loadPhones(textArea);
        });
        flowPane.getChildren().add(loadPhonesButton);

        smallRegion.setMaxHeight(10.0);
        smallRegion.setMinWidth(30.0);
        flowPane.getChildren().add(smallRegion);

        clearPhonesButton.setOnAction(e -> {Subscribe.dialNumbers.clear();});
        flowPane.getChildren().add(clearPhonesButton);

        smallRegion2.setMaxHeight(10.0);
        smallRegion2.setMinWidth(30.0);
        flowPane.getChildren().add(smallRegion2);

        removePhoneField.setMinWidth(50.0);
        flowPane.getChildren().add(removePhoneField);

        removePhoneButton.setOnAction(e -> {
            String currentPhone = removePhoneField.getText();
            Subscribe.dialNumbers.remove("+1" + currentPhone);
            textArea.appendText("Removed ###-###-" + currentPhone.substring(6) + "\n");
            removePhoneField.clear();
        });
        flowPane.getChildren().add(removePhoneButton);

        bigRegion.setMaxHeight(10.0);
        bigRegion.setMinWidth(100.0);
        flowPane.getChildren().add(bigRegion);

        startButton.setOnAction(e -> {
            new Subscribe(textArea);
        });
        startButton.setDisable(true);
        flowPane.getChildren().add(startButton);

        region.setMaxHeight(10.0);
        region.setMinWidth(30.0);
        flowPane.getChildren().add(region);

        stopButton.setOnAction(e -> {
            System.exit(0);
        });
        flowPane.getChildren().add(stopButton);

        bigRegion2.setMaxHeight(10.0);
        bigRegion2.setMinWidth(100.0);
        flowPane.getChildren().add(bigRegion2);

        try {
            ipLabel.setText(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        flowPane.getChildren().add(ipLabel);

        holder.getChildren().add(flowPane);
        stage.setTitle("Subscribe");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}