package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class NewsFeedController {

    @FXML
    private VBox feedContainer;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label tripCountLabel;

    @FXML
    private Label rewardPointsLabel;

    @FXML
    private Label upcomingTripsLabel;

    @FXML
    private Label communityLabel;

    @FXML
    private TextArea createPostArea;

    @FXML
    private VBox quickLinksContainer;

    @FXML
    private VBox notificationsContainer;

    @FXML
    private ScrollPane feedScrollPane;

    private TripService tripService;
    private ObservableList<TripPost> posts;
    private Map<String, Boolean> likedPosts;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        posts = FXCollections.observableArrayList();
        likedPosts = new HashMap<>();

        updateWelcomeBanner();
        setupQuickLinks();
        loadNewsFeedWithSections();
    }

    private void updateWelcomeBanner() {
        User currentUser = tripService.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getDisplayName() + "! 👋");

            // Update statistics
            int tripCount = tripService.getUserTrips(currentUser.getUsername()).size();
            tripCountLabel.setText(String.valueOf(tripCount));

            // TODO: Implement reward points system
            rewardPointsLabel.setText("0");

            // Count upcoming trips (trips with future dates)
            long upcomingCount = tripService.getUserTrips(currentUser.getUsername()).stream()
                    .filter(trip -> trip.getDate().isAfter(java.time.LocalDate.now()))
                    .count();
            upcomingTripsLabel.setText(String.valueOf(upcomingCount));

            // Community count (total users or group members)
            communityLabel.setText("1.2K");
        } else {
            welcomeLabel.setText("Welcome, Guest! 👋");
            tripCountLabel.setText("0");
            rewardPointsLabel.setText("0");
            upcomingTripsLabel.setText("0");
            communityLabel.setText("0");
        }
    }

    private void setupQuickLinks() {
        // This would be populated from the FXML, but we can add dynamic behavior here if needed
    }

    @FXML
    private void handleCreatePost() {
        String content = createPostArea.getText().trim();
        if (content.isEmpty()) {
            showAlert("Empty Post", "Please write something about your trip!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateTripDialog.fxml"));
            DialogPane dialogPane = loader.load();

            CreateTripDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Create Trip");
            dialog.setDialogPane(dialogPane);

            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    Trip trip = controller.getCreatedTrip();
                    if (trip != null) {
                        TripPost post = tripService.postTrip(trip.getId(), content);
                        if (post != null) {
                            createPostArea.clear();
                            loadNewsFeedWithSections();
                            showAlert("Success", "Your trip has been posted to the news feed!");
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to create trip post: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshFeed() {
        loadNewsFeedWithSections();
        updateWelcomeBanner();
    }

    @FXML
    private void handleMyTrips() {
        navigateToPage("MyTrips.fxml", "My Trips - Voyager+");
    }

    @FXML
    private void handleCreateTrip() {
        navigateToPage("TripPlanner.fxml", "Create Trip - Voyager+");
    }

    @FXML
    private void handleExpenseCalc() {
        navigateToPage("ExpenseCalculator.fxml", "Expense Calculator - Voyager+");
    }

    @FXML
    private void handleNotepad() {
        navigateToPage("Notepad.fxml", "Notepad - Voyager+");
    }

    @FXML
    private void handleQuizzes() {
        navigateToPage("Quizzes.fxml", "Quizzes - Voyager+");
    }

    @FXML
    private void handleGames() {
        navigateToPage("MiniGames.fxml", "Mini Games - Voyager+");
    }

    private void loadNewsFeedWithSections() {
        feedContainer.getChildren().clear();

        List<TripPost> soloTrips = tripService.getSoloTripPosts();
        List<TripPost> groupTrips = tripService.getGroupTripPosts();

        if (soloTrips.isEmpty() && groupTrips.isEmpty()) {
            VBox emptyState = createEmptyState();
            feedContainer.getChildren().add(emptyState);
            return;
        }

        // Add Solo Trips Section
        if (!soloTrips.isEmpty()) {
            VBox soloSection = createTripSection("🚶 Solo Travel Adventures",
                "Explore journeys of independent travelers",
                soloTrips,
                "#667eea");
            feedContainer.getChildren().add(soloSection);
        }

        // Add spacing between sections
        if (!soloTrips.isEmpty() && !groupTrips.isEmpty()) {
            Region spacer = new Region();
            spacer.setPrefHeight(30);
            feedContainer.getChildren().add(spacer);
        }

        // Add Group Trips Section
        if (!groupTrips.isEmpty()) {
            VBox groupSection = createTripSection("👥 Group Adventures",
                "Join exciting trips with fellow travelers",
                groupTrips,
                "#764ba2");
            feedContainer.getChildren().add(groupSection);
        }
    }

    private VBox createTripSection(String title, String subtitle, List<TripPost> tripPosts, String accentColor) {
        VBox section = new VBox(20);
        section.getStyleClass().add("trip-section");
        section.setPadding(new Insets(0, 0, 20, 0));

        // Section Header
        VBox header = new VBox(5);
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, " + accentColor + ", " + accentColor + "aa); " +
                       "-fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label(subtitle + " • " + tripPosts.size() + " posts");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Posts Container
        VBox postsContainer = new VBox(20);
        postsContainer.setPadding(new Insets(20, 0, 0, 0));

        for (TripPost post : tripPosts) {
            VBox tripCard = createTripCard(post);
            postsContainer.getChildren().add(tripCard);
        }

        section.getChildren().addAll(header, postsContainer);
        return section;
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        emptyBox.getStyleClass().add("empty-state");

        Label icon = new Label("✈️");
        icon.setStyle("-fx-font-size: 64px;");

        Label title = new Label("No trips yet!");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Be the first to share your travel adventure");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button createBtn = new Button("Create Your First Trip");
        createBtn.getStyleClass().add("create-first-trip-btn");
        createBtn.setOnAction(e -> handleCreatePost());

        emptyBox.getChildren().addAll(icon, title, subtitle, createBtn);
        return emptyBox;
    }

    private VBox createTripCard(TripPost post) {
        VBox card = new VBox(12);
        card.getStyleClass().add("trip-card");
        card.setPadding(new Insets(20));

        // Header: User info and time
        HBox header = createCardHeader(post);

        // Content
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("post-content");

        // Trip Details Section
        VBox tripDetails = createTripDetailsSection(post.getTrip());

        // Activity/Stats Bar
        HBox statsBar = createStatsBar(post);

        // Action Buttons
        HBox actions = createActionButtons(post);

        // Add all sections to card
        card.getChildren().addAll(header, contentLabel, tripDetails, new Separator(), statsBar, actions);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    private HBox createCardHeader(TripPost post) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        Label avatarLabel = new Label(post.getAuthor().getDisplayName().substring(0, 1).toUpperCase());
        avatarLabel.getStyleClass().add("avatar-circle");

        // User info
        VBox userInfo = new VBox(2);
        Label nameLabel = new Label(post.getAuthor().getDisplayName());
        nameLabel.getStyleClass().add("post-author");

        Label timeLabel = new Label(post.getTimeAgo() + " • " + post.getTrip().getType());
        timeLabel.getStyleClass().add("post-meta");

        userInfo.getChildren().addAll(nameLabel, timeLabel);

        // Trip type badge
        Label typeBadge = new Label(post.getTrip().isGroupTrip() ? "👥 Group" : "🚶 Solo");
        typeBadge.getStyleClass().add("trip-type-badge");
        if (post.getTrip().isGroupTrip()) {
            typeBadge.getStyleClass().add("group-badge");
        } else {
            typeBadge.getStyleClass().add("solo-badge");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatarLabel, userInfo, spacer, typeBadge);
        return header;
    }

    private VBox createTripDetailsSection(Trip trip) {
        VBox detailsBox = new VBox(10);
        detailsBox.getStyleClass().add("trip-details-section");
        detailsBox.setPadding(new Insets(15));

        // Trip title
        Label titleLabel = new Label(trip.getTitle());
        titleLabel.getStyleClass().add("trip-detail-title");

        // Info grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(8);

        // Route
        Label routeIcon = new Label("📍");
        Label routeLabel = new Label(trip.getRoute());
        routeLabel.getStyleClass().add("trip-detail-info");

        // Date
        Label dateIcon = new Label("📅");
        Label dateLabel = new Label(trip.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.getStyleClass().add("trip-detail-info");

        // Budget
        Label budgetIcon = new Label("💰");
        Label budgetLabel = new Label("$" + String.format("%.2f", trip.getBudget()));
        budgetLabel.getStyleClass().add("trip-detail-info");

        // Members (for group trips)
        if (trip.isGroupTrip()) {
            Label membersIcon = new Label("👥");
            Label membersLabel = new Label(trip.getApprovedMembers().size() + " members");
            membersLabel.getStyleClass().add("trip-detail-info");

            infoGrid.add(routeIcon, 0, 0);
            infoGrid.add(routeLabel, 1, 0);
            infoGrid.add(dateIcon, 0, 1);
            infoGrid.add(dateLabel, 1, 1);
            infoGrid.add(budgetIcon, 2, 0);
            infoGrid.add(budgetLabel, 3, 0);
            infoGrid.add(membersIcon, 2, 1);
            infoGrid.add(membersLabel, 3, 1);
        } else {
            infoGrid.add(routeIcon, 0, 0);
            infoGrid.add(routeLabel, 1, 0);
            infoGrid.add(dateIcon, 0, 1);
            infoGrid.add(dateLabel, 1, 1);
            infoGrid.add(budgetIcon, 2, 0);
            infoGrid.add(budgetLabel, 3, 0);
        }

        // Description (if exists)
        if (!trip.getDescription().isEmpty()) {
            Label descLabel = new Label(trip.getDescription());
            descLabel.setWrapText(true);
            descLabel.getStyleClass().add("trip-description");
            descLabel.setMaxWidth(Double.MAX_VALUE);
            detailsBox.getChildren().addAll(titleLabel, infoGrid, descLabel);
        } else {
            detailsBox.getChildren().addAll(titleLabel, infoGrid);
        }

        return detailsBox;
    }

    private HBox createStatsBar(TripPost post) {
        HBox statsBar = new HBox(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.getStyleClass().add("stats-bar");

        Label likesLabel = new Label("❤ " + post.getLikes() + " likes");
        likesLabel.getStyleClass().add("stat-label");

        Label commentsLabel = new Label("💬 " + post.getComments() + " comments");
        commentsLabel.getStyleClass().add("stat-label");

        Label sharesLabel = new Label("🔄 " + post.getShares() + " shares");
        sharesLabel.getStyleClass().add("stat-label");

        statsBar.getChildren().addAll(likesLabel, commentsLabel, sharesLabel);
        return statsBar;
    }

    private HBox createActionButtons(TripPost post) {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getStyleClass().add("action-buttons");

        // Like button
        Button likeBtn = new Button(likedPosts.getOrDefault(post.getId(), false) ? "❤ Liked" : "🤍 Like");
        likeBtn.getStyleClass().add("action-btn");
        likeBtn.setOnAction(e -> handleLikePost(post, likeBtn));

        // Comment button
        Button commentBtn = new Button("💬 Comment");
        commentBtn.getStyleClass().add("action-btn");
        commentBtn.setOnAction(e -> handleComment(post));

        // View Trip button
        Button viewBtn = new Button("👁 View Trip");
        viewBtn.getStyleClass().add("action-btn");
        viewBtn.setOnAction(e -> handleViewTrip(post.getTrip()));

        // Join button (only for group trips)
        if (post.getTrip().isGroupTrip() &&
            !post.getTrip().getCreatorUsername().equals(tripService.getCurrentUser().getUsername())) {
            Button joinBtn = new Button("✈ Request to Join");
            joinBtn.getStyleClass().addAll("action-btn", "action-btn-primary");
            joinBtn.setOnAction(e -> handleJoinTrip(post.getTrip()));
            actions.getChildren().addAll(likeBtn, commentBtn, viewBtn, joinBtn);
        } else {
            actions.getChildren().addAll(likeBtn, commentBtn, viewBtn);
        }

        return actions;
    }

    private void handleLikePost(TripPost post, Button likeBtn) {
        boolean isLiked = likedPosts.getOrDefault(post.getId(), false);

        if (isLiked) {
            tripService.unlikePost(post.getId());
            likedPosts.put(post.getId(), false);
            likeBtn.setText("🤍 Like");
        } else {
            tripService.likePost(post.getId());
            likedPosts.put(post.getId(), true);
            likeBtn.setText("❤ Liked");
        }

        // Refresh the feed to update like count
        loadNewsFeedWithSections();
    }

    private void handleComment(TripPost post) {
        // TODO: Implement comment functionality
        showAlert("Coming Soon", "Comment feature will be available soon!");
    }

    private void handleViewTrip(Trip trip) {
        // TODO: Implement trip details view
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trip Details");
        alert.setHeaderText(trip.getTitle());
        alert.setContentText(
            "Route: " + trip.getRoute() + "\n" +
            "Date: " + trip.getDate() + "\n" +
            "Budget: $" + String.format("%.2f", trip.getBudget()) + "\n" +
            "Type: " + trip.getType() + "\n" +
            "Creator: " + trip.getCreatorUsername()
        );
        alert.showAndWait();
    }

    private void handleJoinTrip(Trip trip) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request to Join");
        dialog.setHeaderText("Request to join: " + trip.getTitle());
        dialog.setContentText("Enter your message:");

        dialog.showAndWait().ifPresent(message -> {
            JoinRequest request = tripService.requestToJoin(trip.getId(), message);
            if (request != null) {
                showAlert("Request Sent", "Your join request has been sent to " + trip.getCreatorUsername() + "!");
            }
        });
    }

    private void navigateToPage(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) feedContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle(title);
            stage.setResizable(true);
            stage.setMaximized(true);

        } catch (Exception e) {
            System.err.println("Failed to load page: " + fxmlFile);
            showAlert("Coming Soon", "This feature is under development!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
