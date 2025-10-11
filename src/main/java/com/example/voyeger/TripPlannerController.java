package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private void handleCreateTrip() {
        String title = titleField.getText();
        String route = routeField.getText();
        String budget = budgetField.getText();
        String description = descriptionArea.getText();
        String type = soloTripRadio.isSelected() ? "Solo Trip" : "Group Trip";

        if (title.isEmpty() || route.isEmpty() || budget.isEmpty() || datePicker.getValue() == null) {
            statusLabel.setText("⚠ Please fill in all fields!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        statusLabel.setText("✅ " + type + " created successfully!");
        statusLabel.setStyle("-fx-text-fill: green;");
        System.out.println("Trip Created: " + title + " | " + type);
    }
}
