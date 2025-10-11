package com.example.voyeger;

// Main.java
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        TripPlannerController controller = new TripPlannerController(); // our controller builds UI
        Parent root = controller.loadUI();

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Voyager+ — Travel & Tourism (Demo)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
