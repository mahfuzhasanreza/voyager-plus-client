package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Button newsFeedBtn, tripPlannerBtn, profileBtn, settingsBtn;

    private TripService tripService;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();

        // Check if user is logged in
        if (tripService.getCurrentUser() == null) {
            currentUserLabel.setText("Guest");
        } else {
            updateUserInfo();
        }

        loadNewsFeed(); // Load news feed by default
    }

    private void updateUserInfo() {
        User currentUser = tripService.getCurrentUser();
        if (currentUser != null) {
            currentUserLabel.setText(currentUser.getDisplayName());
        }
    }

    @FXML
    private void loadNewsFeed() {
        loadPage("NewsFeed.fxml");
        highlightButton(newsFeedBtn);
    }

    @FXML
    private void loadTripPlanner() {
        loadPage("TripPlanner.fxml");
        highlightButton(tripPlannerBtn);
    }

    @FXML
    private void loadProfile() {
        loadPage("Profile.fxml");
        highlightButton(profileBtn);
    }

    @FXML
    private void loadSettings() {
        loadPage("Settings.fxml");
        highlightButton(settingsBtn);
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();
            mainContainer.setCenter(page);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load page: " + fxmlFile);
        }
    }

    private void highlightButton(Button activeButton) {
        // Reset all buttons
        newsFeedBtn.getStyleClass().remove("active-nav-btn");
        tripPlannerBtn.getStyleClass().remove("active-nav-btn");
        profileBtn.getStyleClass().remove("active-nav-btn");
        settingsBtn.getStyleClass().remove("active-nav-btn");

        // Highlight active button
        if (!activeButton.getStyleClass().contains("active-nav-btn")) {
            activeButton.getStyleClass().add("active-nav-btn");
        }
    }
}
