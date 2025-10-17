package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class ProfileController {

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label bioLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label joinedDateLabel;

    @FXML
    private Label tripCountLabel;

    @FXML
    private Label postCountLabel;

    @FXML
    private VBox myPostsContainer;

    @FXML
    private ListView<Trip> myTripsListView;

    private TripService tripService;
    private User currentUser;
    private ObservableList<Trip> myTrips;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        currentUser = tripService.getCurrentUser();
        myTrips = FXCollections.observableArrayList();

        loadUserProfile();
        loadUserTrips();
        loadUserPosts();
    }

    private void loadUserProfile() {
        displayNameLabel.setText(currentUser.getDisplayName());
        usernameLabel.setText("@" + currentUser.getUsername());
        bioLabel.setText(currentUser.getBio().isEmpty() ? "No bio yet" : currentUser.getBio());
        emailLabel.setText(currentUser.getEmail());

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        joinedDateLabel.setText("Joined " + currentUser.getJoinedDate().format(formatter));

        int tripCount = tripService.getUserTrips(currentUser.getUsername()).size();
        int postCount = tripService.getUserPosts(currentUser.getUsername()).size();

        tripCountLabel.setText(String.valueOf(tripCount));
        postCountLabel.setText(String.valueOf(postCount));
    }

    private void loadUserTrips() {
        myTrips.clear();
        myTrips.addAll(tripService.getUserTrips(currentUser.getUsername()));
        myTripsListView.setItems(myTrips);
        myTripsListView.setCellFactory(lv -> new TripListCell());
    }

    private void loadUserPosts() {
        myPostsContainer.getChildren().clear();
        var posts = tripService.getUserPosts(currentUser.getUsername());

        if (posts.isEmpty()) {
            Label emptyLabel = new Label("You haven't posted any trips yet");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20px;");
            myPostsContainer.getChildren().add(emptyLabel);
        } else {
            for (TripPost post : posts) {
                myPostsContainer.getChildren().add(createMiniPostCard(post));
            }
        }
    }

    private VBox createMiniPostCard(TripPost post) {
        VBox card = new VBox(8);
        card.getStyleClass().add("mini-post-card");
        card.setPadding(new Insets(12));

        Trip trip = post.getTrip();
        String typeIcon = trip.isGroupTrip() ? "👥" : "🚶";

        Label tripTitle = new Label(typeIcon + " " + trip.getTitle());
        tripTitle.getStyleClass().add("mini-trip-title");

        Label tripInfo = new Label("📍 " + trip.getRoute() + " | 📅 " + trip.getDate());
        tripInfo.getStyleClass().add("mini-trip-info");

        HBox stats = new HBox(15);
        stats.setAlignment(Pos.CENTER_LEFT);
        Label likes = new Label("❤ " + post.getLikes());
        Label comments = new Label("💬 " + post.getComments());
        Label time = new Label("🕒 " + post.getTimeAgo());
        stats.getChildren().addAll(likes, comments, time);

        card.getChildren().addAll(tripTitle, tripInfo, stats);
        return card;
    }

    @FXML
    private void handleRefreshProfile() {
        loadUserProfile();
        loadUserTrips();
        loadUserPosts();
    }

    // Custom ListCell for displaying trips
    private static class TripListCell extends ListCell<Trip> {
        @Override
        protected void updateItem(Trip trip, boolean empty) {
            super.updateItem(trip, empty);
            if (empty || trip == null) {
                setText(null);
            } else {
                String typeIcon = trip.isGroupTrip() ? "👥" : "🚶";
                String status = trip.getStatus() == Trip.TripStatus.POSTED ? "✅" : "📝";
                setText(status + " " + typeIcon + " " + trip.getTitle() + " - " + trip.getDate());
            }
        }
    }
}

