package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class SignInController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private CheckBox rememberMeCheckBox;

    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        // Clear previous status
        statusLabel.setText("");
        statusLabel.setStyle("");

        // Get input values
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showError("⚠ Please enter username/email and password!");
            return;
        }

        // Show loading
        loadingIndicator.setVisible(true);

        // Login in background thread
        new Thread(() -> {
            User user = dbManager.loginUser(usernameOrEmail, password);

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                loadingIndicator.setVisible(false);

                if (user != null) {
                    showSuccess("✅ Login successful! Welcome " + user.getDisplayName());

                    // Set current user in TripService
                    TripService.getInstance().setCurrentUser(user);

                    // Wait 1 second then navigate to main app
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            javafx.application.Platform.runLater(() -> navigateToMainApp(event));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();

                } else {
                    showError("❌ Invalid username/email or password!");
                }
            });
        }).start();
    }

    @FXML
    private void handleBackToWelcome(ActionEvent event) {
        navigateToWelcome(event);
    }

    @FXML
    private void handleGoToSignUp(ActionEvent event) {
        navigateToSignUp(event);
    }

    private void navigateToWelcome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Welcome.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 800);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Welcome");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Sign Up");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToMainApp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 800);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Travel Together");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px; -fx-font-weight: bold;");
    }
}

