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

public class NavbarController {

    @FXML
    private Label brandLabel;

    @FXML
    private Button homeBtn, tripsBtn, hotelBtn, toolsBtn, communityBtn, learnBtn, mapBtn, profileBtn;

    @FXML
    private Label notificationBadge;

    @FXML
    private HBox navbarContainer;

    private TripService tripService;
    private String currentPage = "home";

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        updateNotificationBadge();
        highlightCurrentPage(currentPage);
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        highlightCurrentPage(page);
    }

    private void highlightCurrentPage(String page) {
        // Remove active class from all buttons
        homeBtn.getStyleClass().remove("active-nav");
        tripsBtn.getStyleClass().remove("active-nav");
        hotelBtn.getStyleClass().remove("active-nav");
        toolsBtn.getStyleClass().remove("active-nav");
        communityBtn.getStyleClass().remove("active-nav");
        learnBtn.getStyleClass().remove("active-nav");
        mapBtn.getStyleClass().remove("active-nav");
        profileBtn.getStyleClass().remove("active-nav");

        // Add active class to current page
        switch (page.toLowerCase()) {
            case "home" -> homeBtn.getStyleClass().add("active-nav");
            case "trips" -> tripsBtn.getStyleClass().add("active-nav");
            case "hotel" -> hotelBtn.getStyleClass().add("active-nav");
            case "tools" -> toolsBtn.getStyleClass().add("active-nav");
            case "community" -> communityBtn.getStyleClass().add("active-nav");
            case "learn" -> learnBtn.getStyleClass().add("active-nav");
            case "map" -> mapBtn.getStyleClass().add("active-nav");
            case "profile" -> profileBtn.getStyleClass().add("active-nav");
        }
    }

    @FXML
    private void handleHome(ActionEvent event) {
        navigateToPage("NewsFeed.fxml", "Home - Voyager+", "home");
    }

    @FXML
    private void handleTrips(ActionEvent event) {
        showTripsMenu(event);
    }

    @FXML
    private void handleHotel(ActionEvent event) {
        navigateToPage("HotelBooking.fxml", "Hotel Booking - Voyager+", "hotel");
    }

    @FXML
    private void handleTools(ActionEvent event) {
        showToolsMenu(event);
    }

    @FXML
    private void handleCommunity(ActionEvent event) {
        showCommunityMenu(event);
    }

    @FXML
    private void handleLearn(ActionEvent event) {
        showLearnMenu(event);
    }

    @FXML
    private void handleMap(ActionEvent event) {
        navigateToPage("MapExplorer.fxml", "Map Explorer - Voyager+", "map");
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        showProfileMenu(event);
    }

    private void showTripsMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem createTrip = new MenuItem("✈ Create Trip");
        createTrip.setOnAction(e -> navigateToPage("TripPlanner.fxml", "Create Trip - Voyager+", "trips"));

        MenuItem myTrips = new MenuItem("📋 My Trips");
        myTrips.setOnAction(e -> navigateToPage("MyTrips.fxml", "My Trips - Voyager+", "trips"));

        MenuItem exploreTrips = new MenuItem("🔍 Explore Trips");
        exploreTrips.setOnAction(e -> navigateToPage("ExploreTrips.fxml", "Explore Trips - Voyager+", "trips"));

        menu.getItems().addAll(createTrip, myTrips, exploreTrips);
        menu.show((Node) event.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showToolsMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem expenseCalc = new MenuItem("💰 Expense Calculator");
        expenseCalc.setOnAction(e -> navigateToPage("ExpenseCalculator.fxml", "Expense Calculator - Voyager+", "tools"));

        MenuItem notepad = new MenuItem("📝 Notepad");
        notepad.setOnAction(e -> navigateToPage("Notepad.fxml", "Notepad - Voyager+", "tools"));

        MenuItem calculator = new MenuItem("🔢 Calculator");
        calculator.setOnAction(e -> navigateToPage("Calculator.fxml", "Calculator - Voyager+", "tools"));

        menu.getItems().addAll(expenseCalc, notepad, calculator);
        menu.show((Node) event.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showCommunityMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem chats = new MenuItem("💬 Chats");
        chats.setOnAction(e -> navigateToPage("Chats.fxml", "Chats - Voyager+", "community"));

        MenuItem friends = new MenuItem("👥 Friends");
        friends.setOnAction(e -> navigateToPage("Friends.fxml", "Friends - Voyager+", "community"));

        menu.getItems().addAll(chats, friends);
        menu.show((Node) event.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showLearnMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem quizzes = new MenuItem("🎯 Quizzes");
        quizzes.setOnAction(e -> navigateToPage("Quizzes.fxml", "Quizzes - Voyager+", "learn"));

        MenuItem games = new MenuItem("🎮 Mini Games");
        games.setOnAction(e -> navigateToPage("MiniGames.fxml", "Mini Games - Voyager+", "learn"));

        menu.getItems().addAll(quizzes, games);
        menu.show((Node) event.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
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
        // TODO: Update with actual notification count
        // For now, set to 0 (hidden)
        int notificationCount = 0;

        if (notificationCount > 0) {
            notificationBadge.setText(String.valueOf(notificationCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }

    public void refreshNotifications() {
        updateNotificationBadge();
    }
}
