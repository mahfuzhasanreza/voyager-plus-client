package com.example.voyeger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class NotificationsController {

    @FXML
    private ListView<Notification> notificationsListView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label emptyStateLabel;

    private TripService tripService;
    private ObservableList<Notification> notifications;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        notifications = FXCollections.observableArrayList();

        notificationsListView.setItems(notifications);
        notificationsListView.setCellFactory(lv -> new NotificationListCell());

        // Handle click on notification
        notificationsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click
                Notification selected = notificationsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleNotificationClick(selected);
                }
            }
        });

        loadNotifications();
    }

    private void loadNotifications() {
        User currentUser = tripService.getCurrentUser();
        if (currentUser == null) {
            emptyStateLabel.setText("Please log in to view notifications");
            emptyStateLabel.setVisible(true);
            notificationsListView.setVisible(false);
            return;
        }

        // Fetch notifications from backend
        List<Notification> fetchedNotifications = TripApiClient.fetchNotifications(currentUser.getUsername());

        notifications.clear();
        notifications.addAll(fetchedNotifications);

        if (notifications.isEmpty()) {
            emptyStateLabel.setText("🔔 No new notifications");
            emptyStateLabel.setVisible(true);
            notificationsListView.setVisible(false);
        } else {
            emptyStateLabel.setVisible(false);
            notificationsListView.setVisible(true);
            titleLabel.setText("Notifications (" + notifications.size() + ")");
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Manual refresh triggered");
        loadNotifications();
        showTemporaryMessage("Refreshed!");
    }

    private void showTemporaryMessage(String message) {
        titleLabel.setText("Notifications - " + message);

        // Reset after 2 seconds
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (!notifications.isEmpty()) {
                        titleLabel.setText("Notifications (" + notifications.size() + ")");
                    } else {
                        titleLabel.setText("Notifications");
                    }
                });
            }
        }, 2000);
    }

    private void handleNotificationClick(Notification notification) {
        // Open the Manage Requests dialog for the specific trip
        try {
            // Find the trip
            String tripId = notification.getTripId();
            Trip trip = tripService.getTrip(tripId);

            if (trip == null) {
                // Try to fetch from backend if not in local cache
                showAlert("Trip Not Found", "Unable to find the trip. Please try refreshing.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ManageRequests.fxml"));
            Parent root = loader.load();

            ManageRequestsController controller = loader.getController();
            controller.setTrip(trip);

            Stage stage = new Stage();
            stage.setTitle("Manage Join Requests - " + trip.getTitle());
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

            // Refresh notifications after handling
            stage.setOnHidden(e -> loadNotifications());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open request management: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Custom ListCell for displaying notifications
    private static class NotificationListCell extends ListCell<Notification> {
        @Override
        protected void updateItem(Notification notification, boolean empty) {
            super.updateItem(notification, empty);
            if (empty || notification == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                // Create a formatted display
                Label mainLabel = new Label("🔔 " + notification.getDisplayText());
                mainLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                Label timeLabel = new Label(notification.getTimeAgo());
                timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

                Label tripLabel = new Label("📍 " + notification.getTripRoute());
                tripLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

                javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(5);
                container.getChildren().addAll(mainLabel, tripLabel, timeLabel);
                container.setStyle("-fx-padding: 10;");

                setGraphic(container);
                setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

                // Hover effect
                setOnMouseEntered(e -> setStyle("-fx-background-color: #e6f3ff; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
                setOnMouseExited(e -> setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;"));
            }
        }
    }
}
