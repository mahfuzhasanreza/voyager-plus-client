package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

// TripPlannerController.java
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

// TripPlannerController.java
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

public class TripPlannerController {

    // ---------- Data Models ----------
    public static class Trip {
        String id, title, route, description, owner;
        LocalDate date;
        double budget;
        boolean isGroup;

        Trip(String title, LocalDate date, String route, double budget, String description, boolean isGroup, String owner) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.date = date;
            this.route = route;
            this.budget = budget;
            this.description = description;
            this.isGroup = isGroup;
            this.owner = owner;
        }

        @Override
        public String toString() {
            return title + " — " + date + (isGroup ? " [Group]" : " [Solo]");
        }
    }

    public static class JoinRequest {
        String id, tripId, fromUser;

        JoinRequest(String tripId, String fromUser) {
            this.id = UUID.randomUUID().toString();
            this.tripId = tripId;
            this.fromUser = fromUser;
        }

        @Override
        public String toString() {
            return fromUser;
        }
    }

    // ---------- Observable Lists ----------
    private final ObservableList<Trip> postedTrips = FXCollections.observableArrayList();
    private final ObservableList<JoinRequest> pendingRequests = FXCollections.observableArrayList();
    private final ObservableList<String> groupChats = FXCollections.observableArrayList();

    // ---------- UI Controls ----------
    @FXML private TextField titleField, routeField, budgetField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descArea;
    @FXML private CheckBox groupCheck;
    @FXML private Button postBtn, requestJoinBtn, approveBtn;
    @FXML private ListView<Trip> tripsList;
    @FXML private ListView<JoinRequest> requestsList;
    @FXML private ListView<String> chatList;
    @FXML private Label statusLabel;

    private final String currentUser = "mahfuz_demo_user";

    // ---------- UI Loader ----------
    public Parent loadUI() throws Exception {
        String fxml = getFXMLString();
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        ByteArrayInputStream stream = new ByteArrayInputStream(fxml.getBytes(StandardCharsets.UTF_8));
        Parent root = loader.load(stream);
        initializeAfterLoad();
        return root;
    }

    private void initializeAfterLoad() {
        tripsList.setItems(postedTrips);
        requestsList.setItems(pendingRequests);
        chatList.setItems(groupChats);
        datePicker.setValue(LocalDate.now().plusDays(7));
        budgetField.setText("0.0");

        postBtn.setOnAction(e -> handlePostTrip());
        requestJoinBtn.setOnAction(e -> handleRequestJoin());
        approveBtn.setOnAction(e -> handleApprove());
        tripsList.setOnMouseClicked(this::onTripSelected);
    }

    // ---------- Logic ----------
    private void handlePostTrip() {
        String title = titleField.getText().trim();
        String route = routeField.getText().trim();
        String desc = descArea.getText().trim();
        LocalDate date = datePicker.getValue();

        if (title.isEmpty() || route.isEmpty() || date == null) {
            statusLabel.setText("⚠ Please fill in Title, Route, and Date.");
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠ Invalid budget value.");
            return;
        }

        boolean isGroup = groupCheck.isSelected();
        Trip trip = new Trip(title, date, route, budget, desc, isGroup, currentUser);
        postedTrips.add(0, trip);
        clearForm();
        statusLabel.setText("✅ Trip posted: " + title + (isGroup ? " (Group)" : " (Solo)"));
    }

    private void handleRequestJoin() {
        Trip selected = tripsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠ Select a trip first.");
            return;
        }
        if (!selected.isGroup) {
            statusLabel.setText("❌ Cannot join a Solo trip.");
            return;
        }

        JoinRequest req = new JoinRequest(selected.id, currentUser + "_guest");
        pendingRequests.add(req);
        statusLabel.setText("✅ Join request sent for " + selected.title);
    }

    private void handleApprove() {
        JoinRequest req = requestsList.getSelectionModel().getSelectedItem();
        if (req == null) {
            statusLabel.setText("⚠ Select a join request first.");
            return;
        }

        Trip trip = postedTrips.stream().filter(t -> t.id.equals(req.tripId)).findFirst().orElse(null);
        if (trip == null) {
            pendingRequests.remove(req);
            statusLabel.setText("⚠ Trip not found.");
            return;
        }

        String chatRoom = "GroupChat: " + trip.title;
        if (!groupChats.contains(chatRoom))
            groupChats.add(chatRoom);
        groupChats.add(chatRoom + " — " + req.fromUser + " joined");
        pendingRequests.remove(req);

        statusLabel.setText("✅ Approved " + req.fromUser + " for " + trip.title);
    }

    private void onTripSelected(MouseEvent e) {
        Trip t = tripsList.getSelectionModel().getSelectedItem();
        if (t != null)
            statusLabel.setText("📅 " + t.title + " | Route: " + t.route + " | Budget: " + t.budget);
    }

    private void clearForm() {
        titleField.clear();
        routeField.clear();
        descArea.clear();
        groupCheck.setSelected(false);
        budgetField.setText("0.0");
        datePicker.setValue(LocalDate.now().plusDays(7));
    }

    // ---------- Embedded FXML ----------
    private String getFXMLString() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.geometry.*?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<VBox spacing="12" xmlns="http://javafx.com/javafx" xmlns:fx="http://javafx.com/fxml">
    <padding>
        <Insets top="12" right="12" bottom="12" left="12"/>
    </padding>

    <Label text="Voyager+ — Travel &amp; Tourism Module (Demo)" style="-fx-font-size:18px; -fx-font-weight:bold;"/>

    <HBox spacing="12">
        <!-- Left: Create Trip -->
        <VBox spacing="8" prefWidth="400" style="-fx-border-color:#ccc; -fx-border-radius:8; -fx-padding:10;">
            <Label text="Create Trip" style="-fx-font-size:14px; -fx-font-weight:bold;"/>
            <GridPane hgap="6" vgap="6">
                <columnConstraints>
                    <ColumnConstraints percentWidth="30"/>
                    <ColumnConstraints percentWidth="70"/>
                </columnConstraints>

                <Label text="Title:" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
                <TextField fx:id="titleField" promptText="Trip title" GridPane.rowIndex="0" GridPane.columnIndex="1"/>

                <Label text="Date:" GridPane.rowIndex="1" GridPane.columnIndex="0"/>
                <DatePicker fx:id="datePicker" GridPane.rowIndex="1" GridPane.columnIndex="1"/>

                <Label text="Route:" GridPane.rowIndex="2" GridPane.columnIndex="0"/>
                <TextField fx:id="routeField" promptText="From → To" GridPane.rowIndex="2" GridPane.columnIndex="1"/>

                <Label text="Budget:" GridPane.rowIndex="3" GridPane.columnIndex="0"/>
                <TextField fx:id="budgetField" promptText="Budget (e.g., 1500.0)" GridPane.rowIndex="3" GridPane.columnIndex="1"/>

                <Label text="Group Trip?" GridPane.rowIndex="4" GridPane.columnIndex="0"/>
                <CheckBox fx:id="groupCheck" GridPane.rowIndex="4" GridPane.columnIndex="1"/>

                <Label text="Description:" GridPane.rowIndex="5" GridPane.columnIndex="0"/>
                <TextArea fx:id="descArea" prefRowCount="4" GridPane.rowIndex="5" GridPane.columnIndex="1"/>
            </GridPane>

            <HBox spacing="8" alignment="CENTER_RIGHT">
                <Button fx:id="postBtn" text="Post Trip" />
            </HBox>
        </VBox>

        <!-- Right: Trip List, Requests, Chat -->
        <VBox spacing="8" HBox.hgrow="ALWAYS">
            <Label text="Posted Trips" style="-fx-font-size:14px; -fx-font-weight:bold;"/>
            <ListView fx:id="tripsList" prefHeight="180"/>

            <HBox spacing="8">
                <VBox spacing="6" HBox.hgrow="ALWAYS" style="-fx-border-color:#ddd; -fx-padding:8;">
                    <Label text="Pending Join Requests" style="-fx-font-weight:bold;"/>
                    <ListView fx:id="requestsList" prefHeight="120"/>
                    <HBox spacing="6">
                        <Button fx:id="requestJoinBtn" text="Request to Join" />
                        <Button fx:id="approveBtn" text="Approve Request" />
                    </HBox>
                </VBox>

                <VBox spacing="6" prefWidth="260" style="-fx-border-color:#ddd; -fx-padding:8;">
                    <Label text="Group Chats" style="-fx-font-weight:bold;"/>
                    <ListView fx:id="chatList" prefHeight="200"/>
                </VBox>
            </HBox>

            <Label fx:id="statusLabel" text="Status: Ready" style="-fx-font-style:italic;" />
        </VBox>
    </HBox>
</VBox>
""";
    }
}
