package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        // Clear previous status
        statusLabel.setText("");
        statusLabel.setStyle("");

        // Get input values
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("⚠ All fields are required!");
            return;
        }

        if (username.length() < 3) {
            showError("⚠ Username must be at least 3 characters!");
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError("⚠ Username can only contain letters, numbers, and underscores!");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("⚠ Please enter a valid email address!");
            return;
        }

        if (password.length() < 6) {
            showError("⚠ Password must be at least 6 characters!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("⚠ Passwords do not match!");
            return;
        }

        // Show loading
        loadingIndicator.setVisible(true);

        // Register user in background thread
        new Thread(() -> {
            boolean success = dbManager.registerUser(username, email, password, fullName);

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                loadingIndicator.setVisible(false);

                if (success) {
                    showSuccess("✅ Registration successful! Redirecting to login...");

                    // Wait 2 seconds then navigate to sign in
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            javafx.application.Platform.runLater(() -> navigateToSignIn(event));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();

                } else {
                    showError("❌ Registration failed! Username or email already exists.");
                }
            });
        }).start();
    }

    @FXML
    private void handleBackToWelcome(ActionEvent event) {
        navigateToWelcome(event);
    }

    @FXML
    private void handleGoToSignIn(ActionEvent event) {
        navigateToSignIn(event);
    }

    private void navigateToWelcome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Welcome.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 800);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Welcome");
            stage.setResizable(true);
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignIn.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyager+ - Sign In");
            stage.setResizable(true);
            stage.setMaximized(true);

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
