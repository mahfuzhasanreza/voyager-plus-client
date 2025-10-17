package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TripPlannerController {

    @FXML
    private TextField titleField, routeField, budgetField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private DatePicker datePicker;

    @FXML
    private RadioButton soloTripRadio, groupTripRadio;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Trip> myTripsListView;

    @FXML
    private ListView<Trip> availableTripsListView;

    @FXML
    private TextArea tripDetailsArea;

    private TripService tripService;
    private ObservableList<Trip> myTrips;
    private ObservableList<Trip> availableTrips;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        myTrips = FXCollections.observableArrayList();
        availableTrips = FXCollections.observableArrayList();

        if (myTripsListView != null) {
            myTripsListView.setItems(myTrips);
            myTripsListView.setCellFactory(lv -> new TripListCell());
            myTripsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> displayTripDetails(newVal)
            );
        }

        if (availableTripsListView != null) {
            availableTripsListView.setItems(availableTrips);
            availableTripsListView.setCellFactory(lv -> new TripListCell());
            availableTripsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> displayTripDetails(newVal)
            );
        }

        refreshTrips();
    }

    @FXML
    private void handleCreateTrip() {
        String title = titleField.getText();
        String route = routeField.getText();
        String budgetStr = budgetField.getText();
        String description = descriptionArea.getText();
        Trip.TripType type = soloTripRadio.isSelected() ? Trip.TripType.SOLO : Trip.TripType.GROUP;

        if (title.isEmpty() || route.isEmpty() || budgetStr.isEmpty() || datePicker.getValue() == null) {
            statusLabel.setText("⚠ Please fill in all fields!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);
            Trip trip = tripService.createTrip(title, datePicker.getValue(), route, budget, description, type);
            tripService.postTrip(trip.getId());

            statusLabel.setText("✅ " + type + " trip created and posted successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Clear form
            clearForm();
            refreshTrips();

        } catch (NumberFormatException e) {
            statusLabel.setText("⚠ Invalid budget format!");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleRequestToJoin() {
        Trip selectedTrip = availableTripsListView.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            showAlert("No Selection", "Please select a trip to join.");
            return;
        }

        if (!selectedTrip.isGroupTrip()) {
            showAlert("Cannot Join", "This is a solo trip. You cannot request to join.");
            return;
        }

        if (selectedTrip.getCreatorUsername().equals(tripService.getCurrentUser())) {
            showAlert("Cannot Join", "You are the creator of this trip.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request to Join");
        dialog.setHeaderText("Request to join: " + selectedTrip.getTitle());
        dialog.setContentText("Enter a message:");

        dialog.showAndWait().ifPresent(message -> {
            JoinRequest request = tripService.requestToJoin(selectedTrip.getId(), message);
            if (request != null) {
                showAlert("Request Sent", "Your join request has been sent to the trip creator!");
            }
        });
    }

    @FXML
    private void handleManageRequests() {
        Trip selectedTrip = myTripsListView.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            showAlert("No Selection", "Please select one of your trips to manage requests.");
            return;
        }

        if (!selectedTrip.isGroupTrip()) {
            showAlert("Solo Trip", "This is a solo trip. There are no join requests.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ManageRequests.fxml"));
            Parent root = loader.load();

            ManageRequestsController controller = loader.getController();
            controller.setTrip(selectedTrip);

            Stage stage = new Stage();
            stage.setTitle("Manage Join Requests - " + selectedTrip.getTitle());
            stage.setScene(new Scene(root, 600, 400));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open request management: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshTrips();
        statusLabel.setText("✅ Refreshed!");
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    private void refreshTrips() {
        myTrips.clear();
        myTrips.addAll(tripService.getUserTrips(tripService.getCurrentUser()));

        availableTrips.clear();
        availableTrips.addAll(tripService.getPostedTrips().stream()
                .filter(trip -> !trip.getCreatorUsername().equals(tripService.getCurrentUser()))
                .toList());
    }

    private void displayTripDetails(Trip trip) {
        if (trip == null || tripDetailsArea == null) {
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(trip.getTitle()).append("\n");
        details.append("Type: ").append(trip.getType()).append("\n");
        details.append("Date: ").append(trip.getDate()).append("\n");
        details.append("Route: ").append(trip.getRoute()).append("\n");
        details.append("Budget: $").append(String.format("%.2f", trip.getBudget())).append("\n");
        details.append("Description: ").append(trip.getDescription()).append("\n");
        details.append("Creator: ").append(trip.getCreatorUsername()).append("\n");
        details.append("Status: ").append(trip.getStatus()).append("\n");

        if (trip.isGroupTrip()) {
            details.append("\nMembers: ").append(trip.getApprovedMembers().size()).append("\n");
            details.append("Pending Requests: ").append(
                trip.getJoinRequests().stream()
                    .filter(r -> r.getStatus() == JoinRequest.RequestStatus.PENDING)
                    .count()
            ).append("\n");
        }

        tripDetailsArea.setText(details.toString());
    }

    private void clearForm() {
        titleField.clear();
        routeField.clear();
        budgetField.clear();
        descriptionArea.clear();
        datePicker.setValue(null);
        soloTripRadio.setSelected(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
                setText(typeIcon + " " + trip.getTitle() + " - " + trip.getDate());
            }
        }
    }
}
