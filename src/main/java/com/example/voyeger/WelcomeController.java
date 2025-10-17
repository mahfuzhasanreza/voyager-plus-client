package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class WelcomeController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    public void initialize() {
        // Add fade-in animation to title
        if (titleLabel != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), titleLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }

        // Add slide-up animation to subtitle
        if (subtitleLabel != null) {
            TranslateTransition slideUp = new TranslateTransition(Duration.seconds(1), subtitleLabel);
            slideUp.setFromY(50);
            slideUp.setToY(0);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), subtitleLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            fadeIn.play();
            slideUp.play();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Sign Up");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load SignUp page: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignIn.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Sign In");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load SignIn page: " + e.getMessage());
        }
    }
}

