package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SettingsController {

    @FXML
    private TextField displayNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea bioArea;

    @FXML
    private TextField profilePicField;

    @FXML
    private TextField coverPhotoField;

    @FXML
    private Label statusLabel;

    private TripService tripService;
    private User currentUser;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        currentUser = tripService.getCurrentUser();
        loadUserData();
    }

    private void loadUserData() {
        displayNameField.setText(currentUser.getDisplayName());
        emailField.setText(currentUser.getEmail());
        bioArea.setText(currentUser.getBio());
        profilePicField.setText(currentUser.getProfilePicturePath());
        coverPhotoField.setText(currentUser.getCoverPhotoPath());
    }

    @FXML
    private void handleBrowseProfilePic() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            profilePicField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseCoverPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            coverPhotoField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSaveSettings() {
        String displayName = displayNameField.getText().trim();
        String email = emailField.getText().trim();
        String bio = bioArea.getText().trim();

        if (displayName.isEmpty()) {
            showError("Display name cannot be empty!");
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            showError("Please enter a valid email address!");
            return;
        }

        // Update user object
        currentUser.setDisplayName(displayName);
        currentUser.setEmail(email);
        currentUser.setBio(bio);
        currentUser.setProfilePicturePath(profilePicField.getText());
        currentUser.setCoverPhotoPath(coverPhotoField.getText());

        // Save to service
        tripService.updateUser(currentUser);

        statusLabel.setText("✅ Settings saved successfully!");
        statusLabel.setStyle("-fx-text-fill: green;");

        // Auto-clear status after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> statusLabel.setText(""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleResetSettings() {
        loadUserData();
        statusLabel.setText("Settings reset to current values");
        statusLabel.setStyle("-fx-text-fill: #636e72;");
    }

    private void showError(String message) {
        statusLabel.setText("⚠ " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
}

