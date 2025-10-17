package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ManageRequestsController {

    @FXML
    private ListView<JoinRequest> requestsListView;

    @FXML
    private TextArea requestDetailsArea;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Label statusLabel;

    private Trip trip;
    private TripService tripService;
    private ObservableList<JoinRequest> requests;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        requests = FXCollections.observableArrayList();

        requestsListView.setItems(requests);
        requestsListView.setCellFactory(lv -> new RequestListCell());
        requestsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> displayRequestDetails(newVal)
        );
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
        refreshRequests();
    }

    @FXML
    private void handleApprove() {
        JoinRequest selectedRequest = requestsListView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("No Selection", "Please select a request to approve.");
            return;
        }

        if (selectedRequest.getStatus() != JoinRequest.RequestStatus.PENDING) {
            showAlert("Invalid Action", "This request has already been processed.");
            return;
        }

        GroupChat groupChat = tripService.approveJoinRequest(trip.getId(), selectedRequest.getId());

        statusLabel.setText("✅ Request approved! Group chat created/updated.");
        statusLabel.setStyle("-fx-text-fill: green;");

        refreshRequests();
        displayRequestDetails(null);

        showAlert("Request Approved",
            String.format("User '%s' has been added to the trip!\nGroup Chat: %s",
                selectedRequest.getRequesterUsername(),
                groupChat != null ? groupChat.getChatName() : "N/A"));
    }

    @FXML
    private void handleReject() {
        JoinRequest selectedRequest = requestsListView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("No Selection", "Please select a request to reject.");
            return;
        }

        if (selectedRequest.getStatus() != JoinRequest.RequestStatus.PENDING) {
            showAlert("Invalid Action", "This request has already been processed.");
            return;
        }

        tripService.rejectJoinRequest(trip.getId(), selectedRequest.getId());

        statusLabel.setText("❌ Request rejected.");
        statusLabel.setStyle("-fx-text-fill: orange;");

        refreshRequests();
        displayRequestDetails(null);
    }

    @FXML
    private void handleRefresh() {
        refreshRequests();
        statusLabel.setText("✅ Refreshed!");
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) requestsListView.getScene().getWindow();
        stage.close();
    }

    private void refreshRequests() {
        requests.clear();
        if (trip != null) {
            requests.addAll(tripService.getPendingRequests(trip.getId()));
        }
    }

    private void displayRequestDetails(JoinRequest request) {
        if (request == null) {
            requestDetailsArea.setText("");
            approveButton.setDisable(true);
            rejectButton.setDisable(true);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Requester: ").append(request.getRequesterUsername()).append("\n");
        details.append("Status: ").append(request.getStatus()).append("\n");
        details.append("Request Time: ").append(request.getRequestTime()).append("\n");
        details.append("\nMessage:\n").append(request.getMessage());

        requestDetailsArea.setText(details.toString());

        boolean isPending = request.getStatus() == JoinRequest.RequestStatus.PENDING;
        approveButton.setDisable(!isPending);
        rejectButton.setDisable(!isPending);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Custom ListCell for displaying requests
    private static class RequestListCell extends ListCell<JoinRequest> {
        @Override
        protected void updateItem(JoinRequest request, boolean empty) {
            super.updateItem(request, empty);
            if (empty || request == null) {
                setText(null);
                setStyle("");
            } else {
                String statusIcon = switch (request.getStatus()) {
                    case PENDING -> "⏳";
                    case APPROVED -> "✅";
                    case REJECTED -> "❌";
                };
                setText(statusIcon + " " + request.getRequesterUsername() + " - " + request.getStatus());

                if (request.getStatus() == JoinRequest.RequestStatus.PENDING) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("-fx-opacity: 0.6;");
                }
            }
        }
    }
}

