package com.example.voyeger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("PrivateChat.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Welcome.fxml"));
        Scene scene = new Scene(loader.load(), 1400, 800);
        stage.setTitle("Voyager+ - Welcome");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        // Close MongoDB connection when app closes
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch();
    }
}