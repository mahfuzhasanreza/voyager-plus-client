package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LandingController {

    @FXML
    private void handleGetStarted(ActionEvent event) {
        // Navigate to Trip Planner (or Registration page)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TripPlanner.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Trip Planner");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Trip Planner: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        // Navigate to Login page (create Login.fxml later)
        // For now, also navigate to Trip Planner
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TripPlanner.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Sign In");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Sign In: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}