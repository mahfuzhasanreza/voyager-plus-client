package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class NavbarController {

    @FXML
    private Label brandLabel;

    @FXML
    private Button homeBtn, profileBtn;

    @FXML
    private Label notificationBadge;

    @FXML
    private HBox navbarContainer;

    private TripService tripService;
    private String currentPage = "home";
    private java.util.Timer notificationTimer;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        highlightCurrentPage(currentPage);

        // Initial update
        updateNotificationBadge();

        // Start auto-refresh timer (every 30 seconds)
        startNotificationTimer();
    }

    private void startNotificationTimer() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }

        notificationTimer = new java.util.Timer(true);
        notificationTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateNotificationBadge());
            }
        }, 10000, 30000); // First update after 10 seconds, then every 30 seconds
    }

    public void stopNotificationTimer() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer = null;
        }
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        highlightCurrentPage(page);
    }

    private void highlightCurrentPage(String page) {
        // Remove active class from all buttons
        homeBtn.getStyleClass().remove("active-nav");
        profileBtn.getStyleClass().remove("active-nav");

        // Add active class to current page
        switch (page.toLowerCase()) {
            case "home" -> homeBtn.getStyleClass().add("active-nav");
            case "profile" -> profileBtn.getStyleClass().add("active-nav");
        }
    }

    @FXML
    private void handleHome(ActionEvent event) {
        navigateToPage("NewsFeed.fxml", "Home - Voyager+", "home");
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        showProfileMenu(event);
    }

    @FXML
    private void handleNotifications(ActionEvent event) {
        navigateToPage("Notifications.fxml", "Notifications - Voyager+", "home");
    }

    private void showProfileMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem viewProfile = new MenuItem("👤 View/Edit Profile");
        viewProfile.setOnAction(e -> navigateToPage("Profile.fxml", "Profile - Voyager+", "profile"));

        MenuItem rewardPoints = new MenuItem("🏆 My Reward Points");
        rewardPoints.setOnAction(e -> navigateToPage("RewardPoints.fxml", "Reward Points - Voyager+", "profile"));

        MenuItem settings = new MenuItem("⚙ Settings");
        settings.setOnAction(e -> navigateToPage("Settings.fxml", "Settings - Voyager+", "profile"));

        MenuItem logout = new MenuItem("🚪 Logout");
        logout.setOnAction(e -> handleLogout(event));

        menu.getItems().addAll(viewProfile, rewardPoints, new SeparatorMenuItem(), settings, new SeparatorMenuItem(), logout);
        menu.show((Node) event.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void navigateToPage(String fxmlFile, String title, String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) navbarContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle(title);

            setCurrentPage(pageName);

        } catch (Exception e) {
            System.err.println("Failed to load page: " + fxmlFile);
            e.printStackTrace();

            // Show alert if page doesn't exist yet
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Coming Soon");
            alert.setHeaderText(title);
            alert.setContentText("This feature is under development and will be available soon!");
            alert.showAndWait();
        }
    }

    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be redirected to the welcome page.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Clear current user
                    tripService.setCurrentUser(null);

                    // Navigate to welcome page
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Welcome.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) navbarContainer.getScene().getWindow();
                    stage.setScene(new Scene(root, 1400, 800));
                    stage.setTitle("Voyager+ - Welcome");
                    stage.setResizable(true);
                    stage.setMaximized(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateNotificationBadge() {
        // Fetch real notification count from backend
        User currentUser = tripService.getCurrentUser();
        if (currentUser != null) {
            int notificationCount = TripApiClient.fetchNotificationCount(currentUser.getUsername());

            if (notificationCount > 0) {
                notificationBadge.setText(String.valueOf(notificationCount));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        } else {
            notificationBadge.setVisible(false);
        }
    }

    public void refreshNotifications() {
        updateNotificationBadge();
    }
}
