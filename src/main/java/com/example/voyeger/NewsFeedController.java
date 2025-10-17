package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.HashMap;
import java.util.Map;

public class NewsFeedController {

    @FXML
    private VBox feedContainer;

    @FXML
    private TextArea createPostArea;

    @FXML
    private ComboBox<String> tripTypeCombo;

    private TripService tripService;
    private ObservableList<TripPost> posts;
    private Map<String, Boolean> likedPosts;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        posts = FXCollections.observableArrayList();
        likedPosts = new HashMap<>();

        tripTypeCombo.setItems(FXCollections.observableArrayList("Solo Trip", "Group Trip"));
        tripTypeCombo.setValue("Group Trip");

        loadNewsFeed();
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
                            loadNewsFeed();
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
        loadNewsFeed();
    }

    private void loadNewsFeed() {
        feedContainer.getChildren().clear();
        posts.clear();
        posts.addAll(tripService.getNewsFeed());

        if (posts.isEmpty()) {
            Label emptyLabel = new Label("No posts yet. Be the first to share your trip! ✈️");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 40px;");
            feedContainer.getChildren().add(emptyLabel);
        } else {
            for (TripPost post : posts) {
                feedContainer.getChildren().add(createPostCard(post));
            }
        }
    }

    private VBox createPostCard(TripPost post) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        card.setPadding(new Insets(15));

        // Header: User info and time
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatarLabel = new Label(post.getAuthor().getDisplayName().substring(0, 1).toUpperCase());
        avatarLabel.getStyleClass().add("avatar-circle");

        VBox userInfo = new VBox(2);
        Label nameLabel = new Label(post.getAuthor().getDisplayName());
        nameLabel.getStyleClass().add("post-author");
        Label timeLabel = new Label(post.getTimeAgo());
        timeLabel.getStyleClass().add("post-time");
        userInfo.getChildren().addAll(nameLabel, timeLabel);

        header.getChildren().addAll(avatarLabel, userInfo);

        // Content
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("post-content");

        // Trip Details Card
        VBox tripCard = new VBox(8);
        tripCard.getStyleClass().add("trip-details-card");
        tripCard.setPadding(new Insets(12));

        Trip trip = post.getTrip();
        String typeIcon = trip.isGroupTrip() ? "👥" : "🚶";
        Label tripTitle = new Label(typeIcon + " " + trip.getTitle());
        tripTitle.getStyleClass().add("trip-title");

        Label tripRoute = new Label("📍 " + trip.getRoute());
        Label tripDate = new Label("📅 " + trip.getDate());
        Label tripBudget = new Label("💰 $" + String.format("%.2f", trip.getBudget()));

        if (!trip.getDescription().isEmpty()) {
            Label tripDesc = new Label(trip.getDescription());
            tripDesc.setWrapText(true);
            tripDesc.getStyleClass().add("trip-description");
            tripCard.getChildren().addAll(tripTitle, tripRoute, tripDate, tripBudget, tripDesc);
        } else {
            tripCard.getChildren().addAll(tripTitle, tripRoute, tripDate, tripBudget);
        }

        // Action buttons
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getStyleClass().add("post-actions");

        Button likeBtn = new Button("❤ Like (" + post.getLikes() + ")");
        likeBtn.getStyleClass().add("action-btn");
        likeBtn.setOnAction(e -> handleLikePost(post, likeBtn));

        Button commentBtn = new Button("💬 Comment (" + post.getComments() + ")");
        commentBtn.getStyleClass().add("action-btn");

        Button joinBtn = new Button("✈ Join Trip");
        joinBtn.getStyleClass().add("action-btn-primary");
        joinBtn.setOnAction(e -> handleJoinTrip(post.getTrip()));

        if (!trip.isGroupTrip() || trip.getCreatorUsername().equals(tripService.getCurrentUser().getUsername())) {
            joinBtn.setDisable(true);
        }

        actions.getChildren().addAll(likeBtn, commentBtn, joinBtn);

        // Add all to card
        card.getChildren().addAll(header, contentLabel, tripCard, new Separator(), actions);

        return card;
    }

    private void handleLikePost(TripPost post, Button likeBtn) {
        boolean isLiked = likedPosts.getOrDefault(post.getId(), false);

        if (isLiked) {
            tripService.unlikePost(post.getId());
            likedPosts.put(post.getId(), false);
        } else {
            tripService.likePost(post.getId());
            likedPosts.put(post.getId(), true);
        }

        likeBtn.setText("❤ Like (" + post.getLikes() + ")");
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
