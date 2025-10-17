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
        // Handle different notification types
        if ("JOIN_REQUEST".equals(notification.getType())) {
            // Show a modal dialog to approve/decline the request directly
            showJoinRequestModal(notification);
        } else if ("REQUEST_APPROVED".equals(notification.getType()) || "REQUEST_REJECTED".equals(notification.getType())) {
            // Show a dialog with the message and offer to dismiss
            showResponseNotificationDialog(notification);
        }
    }

    private void showJoinRequestModal(Notification notification) {
        try {
            // Fetch the specific request details from backend
            String tripId = notification.getTripId();
            String requestId = notification.getId();

            User currentUser = tripService.getCurrentUser();
            if (currentUser == null) return;

            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Join Request");
            dialog.setHeaderText("Request from " + notification.getRequesterUsername());

            // Create dialog content
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
            content.setStyle("-fx-padding: 20;");

            Label tripLabel = new Label("Trip: " + notification.getTripTitle());
            tripLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label routeLabel = new Label("Route: " + notification.getTripRoute());
            routeLabel.setStyle("-fx-font-size: 14px;");

            Label requesterLabel = new Label("Requester: " + notification.getRequesterUsername());
            requesterLabel.setStyle("-fx-font-size: 14px;");

            Label messageHeaderLabel = new Label("Message:");
            messageHeaderLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

            TextArea messageArea = new TextArea(notification.getDisplayMessage());
            messageArea.setEditable(false);
            messageArea.setWrapText(true);
            messageArea.setPrefRowCount(3);
            messageArea.setMaxHeight(80);

            content.getChildren().addAll(
                tripLabel,
                routeLabel,
                requesterLabel,
                messageHeaderLabel,
                messageArea
            );

            dialog.getDialogPane().setContent(content);

            // Add buttons
            ButtonType approveButton = new ButtonType("✅ Approve", ButtonBar.ButtonData.OK_DONE);
            ButtonType declineButton = new ButtonType("❌ Decline", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialog.getDialogPane().getButtonTypes().addAll(approveButton, declineButton, cancelButton);

            // Handle button clicks
            dialog.showAndWait().ifPresent(response -> {
                if (response == approveButton) {
                    handleApproveRequest(tripId, requestId, currentUser.getUsername(), notification);
                } else if (response == declineButton) {
                    handleDeclineRequest(tripId, requestId, currentUser.getUsername(), notification);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to display request details: " + e.getMessage());
        }
    }

    private void handleApproveRequest(String tripId, String requestId, String responderUsername, Notification notification) {
        try {
            // Call backend API to approve the request
            boolean success = respondToRequest(tripId, requestId, "approve", responderUsername);

            if (success) {
                showAlert("Request Approved",
                    "You have approved " + notification.getRequesterUsername() + "'s request to join \"" + notification.getTripTitle() + "\"");
                loadNotifications(); // Refresh the list
            } else {
                showAlert("Error", "Failed to approve the request. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to approve request: " + e.getMessage());
        }
    }

    private void handleDeclineRequest(String tripId, String requestId, String responderUsername, Notification notification) {
        try {
            // Confirm decline action
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Decline");
            confirmAlert.setHeaderText("Decline Join Request");
            confirmAlert.setContentText("Are you sure you want to decline " + notification.getRequesterUsername() + "'s request?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Call backend API to reject the request
                    boolean success = respondToRequest(tripId, requestId, "reject", responderUsername);

                    if (success) {
                        showAlert("Request Declined",
                            "You have declined " + notification.getRequesterUsername() + "'s request to join \"" + notification.getTripTitle() + "\"");
                        loadNotifications(); // Refresh the list
                    } else {
                        showAlert("Error", "Failed to decline the request. Please try again.");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to decline request: " + e.getMessage());
        }
    }

    private boolean respondToRequest(String tripId, String requestId, String action, String responderUsername) {
        try {
            String url = "http://localhost:5000/trips/" + tripId + "/requests/" + requestId + "/respond";
            System.out.println("📤 Responding to request: " + url);
            System.out.println("   Action: " + action);

            // Create JSON payload
            String jsonPayload = "{" +
                "\"action\":\"" + action + "\"," +
                "\"responderUsername\":\"" + escapeJson(responderUsername) + "\"" +
                "}";

            java.net.URL urlObj = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Write the payload
            conn.getOutputStream().write(jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            int responseCode = conn.getResponseCode();
            System.out.println("📡 HTTP Response Code: " + responseCode);

            if (responseCode == 200) {
                System.out.println("✅ Request responded successfully");
                return true;
            } else {
                // Read error response
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                System.err.println("❌ Error response: " + errorResponse.toString());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error responding to request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private void showResponseNotificationDialog(Notification notification) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trip Request Response");
        alert.setHeaderText(notification.getDisplayText());
        alert.setContentText(notification.getDisplayMessage());

        // Add dismiss button
        ButtonType dismissButton = new ButtonType("Dismiss", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(dismissButton, closeButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == dismissButton) {
                // Dismiss the notification
                dismissNotification(notification);
            }
        });
    }

    private void dismissNotification(Notification notification) {
        User currentUser = tripService.getCurrentUser();
        if (currentUser == null) return;

        boolean success = TripApiClient.dismissNotification(currentUser.getUsername(), notification.getId());
        if (success) {
            System.out.println("✅ Notification dismissed");
            loadNotifications(); // Refresh the list
        } else {
            showAlert("Error", "Failed to dismiss notification. Please try again.");
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
