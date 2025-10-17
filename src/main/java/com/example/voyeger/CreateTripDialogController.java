package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CreateTripDialogController {

    @FXML
    private TextField titleField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField routeField;

    @FXML
    private TextField budgetField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private RadioButton soloTripRadio;

    @FXML
    private RadioButton groupTripRadio;

    @FXML
    private DialogPane dialogPane;

    private TripService tripService;
    private Trip createdTrip;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();

        // Set up the dialog buttons
        ButtonType createButtonType = new ButtonType("Create & Post", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialogPane.getButtonTypes().addAll(createButtonType, cancelButtonType);

        // Get the create button and add event filter
        Button createButton = (Button) dialogPane.lookupButton(createButtonType);
        if (createButton != null) {
            createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!validateAndCreateTrip()) {
                    event.consume(); // Prevent dialog from closing
                }
            });
        }
    }

    public Trip getCreatedTrip() {
        return createdTrip;
    }

    private boolean validateAndCreateTrip() {
        String title = titleField.getText().trim();
        String route = routeField.getText().trim();
        String budgetStr = budgetField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty() || route.isEmpty() || budgetStr.isEmpty() || datePicker.getValue() == null) {
            showError("Please fill in all required fields!");
            return false;
        }

        try {
            double budget = Double.parseDouble(budgetStr);
            if (budget < 0) {
                showError("Budget cannot be negative!");
                return false;
            }

            Trip.TripType type = soloTripRadio.isSelected() ? Trip.TripType.SOLO : Trip.TripType.GROUP;
            createdTrip = tripService.createTrip(title, datePicker.getValue(), route, budget, description, type);

            return true;

        } catch (NumberFormatException e) {
            showError("Invalid budget format! Please enter a number.");
            return false;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
