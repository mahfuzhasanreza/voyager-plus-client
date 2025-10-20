package com.example.voyeger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PrivateChatController {

    @FXML
    private ListView<String> userListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label connectionStatusLabel;

    @FXML
    private BorderPane activeChatPane;

    @FXML
    private Label chatUserLabel;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private VBox emptyChatState;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String currentUsername;
    private String currentChatUser;
    private ObservableList<String> onlineUsers;
    private Map<String, List<ChatMessage>> messageHistory;
    private boolean connected = false;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    @FXML
    public void initialize() {
        onlineUsers = FXCollections.observableArrayList();
        messageHistory = new HashMap<>();
        userListView.setItems(onlineUsers);

        // Setup user list cell factory
        userListView.setCellFactory(lv -> new UserListCell());

        // Handle user selection
        userListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    openChatWithUser(newVal);
                }
            }
        );

        // Auto-scroll messages
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));

        // Send message on Enter
        messageInput.setOnAction(e -> handleSendMessage());

        // Connect to server
        connectToServer();
    }

    private void connectToServer() {
        TripService tripService = TripService.getInstance();
        if (tripService.getCurrentUser() != null) {
            currentUsername = tripService.getCurrentUser().getUsername();
        } else {
            currentUsername = "User" + new Random().nextInt(1000);
        }

        new Thread(() -> {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Wait for username prompt
                String prompt = in.readLine();
                if (prompt.equals("ENTER_USERNAME")) {
                    out.println(currentUsername);
                }

                // Wait for success
                String response = in.readLine();
                if (response.startsWith("SUCCESS:")) {
                    connected = true;
                    Platform.runLater(() -> {
                        connectionStatusLabel.setText("● Online");
                        connectionStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    });

                    // Start listening for messages
                    listenForMessages();
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    connectionStatusLabel.setText("● Offline");
                    connectionStatusLabel.setStyle("-fx-text-fill: #f44336;");
                    showAlert("Connection Error", "Unable to connect to chat server. Please try again later.");
                });
                System.err.println("Connection error: " + e.getMessage());
            }
        }).start();
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    System.out.println("Received: " + message);

                    if (message.startsWith("USERS:")) {
                        String usersList = message.substring(6);
                        updateOnlineUsers(usersList);
                    } else if (message.startsWith("MESSAGE:")) {
                        // Format: MESSAGE:sender:content
                        String[] parts = message.substring(8).split(":", 2);
                        if (parts.length == 2) {
                            String sender = parts[0];
                            String content = parts[1];
                            Platform.runLater(() -> receiveMessage(sender, content));
                        }
                    } else if (message.startsWith("SENT:")) {
                        // Format: SENT:recipient:content
                        String[] parts = message.substring(5).split(":", 2);
                        if (parts.length == 2) {
                            String recipient = parts[0];
                            String content = parts[1];
                            Platform.runLater(() -> addSentMessage(recipient, content));
                        }
                    } else if (message.startsWith("ERROR:")) {
                        String error = message.substring(6);
                        Platform.runLater(() -> showAlert("Error", error));
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    connectionStatusLabel.setText("● Disconnected");
                    connectionStatusLabel.setStyle("-fx-text-fill: #f44336;");
                });
                System.err.println("Listener error: " + e.getMessage());
            }
        }).start();
    }

    private void updateOnlineUsers(String usersList) {
        Platform.runLater(() -> {
            onlineUsers.clear();
            if (!usersList.isEmpty()) {
                String[] users = usersList.split(",");
                for (String user : users) {
                    if (!user.trim().isEmpty()) {
                        onlineUsers.add(user.trim());
                    }
                }
            }
        });
    }

    private void openChatWithUser(String username) {
        currentChatUser = username;
        chatUserLabel.setText(username);

        emptyChatState.setVisible(false);
        activeChatPane.setVisible(true);

        // Load message history
        loadMessageHistory();
    }

    private void loadMessageHistory() {
        messagesContainer.getChildren().clear();
        List<ChatMessage> messages = messageHistory.getOrDefault(currentChatUser, new ArrayList<>());
        for (ChatMessage msg : messages) {
            displayMessage(msg);
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty() || currentChatUser == null) {
            return;
        }

        if (!connected) {
            showAlert("Connection Error", "Not connected to chat server");
            return;
        }

        // Send to server
        out.println("PRIVATE:" + currentChatUser + ":" + message);
        messageInput.clear();
    }

    private void addSentMessage(String recipient, String content) {
        ChatMessage msg = new ChatMessage(currentUsername, content, LocalDateTime.now(), true);

        // Add to history
        messageHistory.computeIfAbsent(recipient, k -> new ArrayList<>()).add(msg);

        // Display if this is the current chat
        if (recipient.equals(currentChatUser)) {
            displayMessage(msg);
        }
    }

    private void receiveMessage(String sender, String content) {
        ChatMessage msg = new ChatMessage(sender, content, LocalDateTime.now(), false);

        // Add to history
        messageHistory.computeIfAbsent(sender, k -> new ArrayList<>()).add(msg);

        // Display if this is the current chat
        if (sender.equals(currentChatUser)) {
            displayMessage(msg);
        }

        // Highlight user in list if not current chat
        if (!sender.equals(currentChatUser)) {
            userListView.refresh();
        }
    }

    private void displayMessage(ChatMessage message) {
        VBox messageBox = new VBox(5);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.setMaxWidth(400);

        // Message content
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font("System", 14));
        contentLabel.setStyle("-fx-text-fill: white;");

        // Timestamp
        Label timeLabel = new Label(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(Font.font("System", 10));
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");

        messageBox.getChildren().addAll(contentLabel, timeLabel);

        // Style based on sender
        if (message.isSentByMe()) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setStyle("-fx-background-color: #1877f2; -fx-background-radius: 18; -fx-padding: 8 12 8 12;");
            HBox container = new HBox(messageBox);
            container.setAlignment(Pos.CENTER_RIGHT);
            container.setPadding(new Insets(2, 10, 2, 50));
            messagesContainer.getChildren().add(container);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.setStyle("-fx-background-color: #3a3b3c; -fx-background-radius: 18; -fx-padding: 8 12 8 12;");
            HBox container = new HBox(messageBox);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(2, 50, 2, 10));
            messagesContainer.getChildren().add(container);
        }
    }

    private void filterUsers(String query) {
        if (query == null || query.isEmpty()) {
            userListView.setItems(onlineUsers);
        } else {
            ObservableList<String> filtered = FXCollections.observableArrayList();
            for (String user : onlineUsers) {
                if (user.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(user);
                }
            }
            userListView.setItems(filtered);
        }
    }

    @FXML
    private void handleRefresh() {
        if (connected) {
            out.println("GET_USERS");
        }
    }

    @FXML
    private void handleBack() {
        disconnect();
        // Navigate back to main screen
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("Main.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = (javafx.stage.Stage) userListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            if (out != null) {
                out.println("DISCONNECT");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class for chat messages
    private static class ChatMessage {
        private String sender;
        private String content;
        private LocalDateTime timestamp;
        private boolean sentByMe;

        public ChatMessage(String sender, String content, LocalDateTime timestamp, boolean sentByMe) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.sentByMe = sentByMe;
        }

        public String getSender() { return sender; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isSentByMe() { return sentByMe; }
    }

    // Cell factory for user list
    private class UserListCell extends ListCell<String> {
        @Override
        protected void updateItem(String user, boolean empty) {
            super.updateItem(user, empty);

            if (empty || user == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                HBox container = new HBox(10);
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(10));

                // Online indicator
                Label statusDot = new Label("●");
                statusDot.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12;");

                // Username
                Label nameLabel = new Label(user);
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameLabel.setStyle("-fx-text-fill: #e4e6eb;");

                container.getChildren().addAll(statusDot, nameLabel);

                setGraphic(container);
                setStyle("-fx-background-color: #242526; -fx-cursor: hand;");

                setOnMouseEntered(e -> setStyle("-fx-background-color: #3a3b3c; -fx-cursor: hand;"));
                setOnMouseExited(e -> setStyle("-fx-background-color: #242526; -fx-cursor: hand;"));
            }
        }
    }
}

