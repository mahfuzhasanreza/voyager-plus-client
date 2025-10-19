package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class MessengerController implements Initializable {

    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Button backButton;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executor;
    private String currentRoom;
    private String username;
    private Main mainApp;
    private Stage currentStage;
    private Scene previousScene;
    private boolean intentionalDisconnect = false;

    private static final int[] SERVER_PORTS = {8081, 8082, 8083, 8084, 8085};
    private int currentServerIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executor = Executors.newSingleThreadExecutor();
        setupChat();

        // Add Enter key support for sending messages
        messageInput.setOnAction(event -> sendMessage());

        // Setup back button if it exists
        if (backButton != null) {
            backButton.setOnAction(event -> handleBackToDashboard());
        }

        // Clear only the dummy messages from the chat container
        clearDummyMessages();
    }

    private void clearDummyMessages() {
        // This will clear only the message bubbles (dummy messages)
        // but keep the header, participants panel, and input area intact
        chatContainer.getChildren().clear();
    }

    public void setRoomInfo(String roomName, String username) {
        this.currentRoom = roomName;
        this.username = username;
        connectToServer();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    public void setPreviousScene(Scene previousScene) {
        this.previousScene = previousScene;
    }

    private void setupChat() {
        // Auto-scroll to bottom when new messages are added
        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void handleBackToDashboard() {
        // Set flag to indicate intentional disconnect
        intentionalDisconnect = true;
        shutdown();

        if (mainApp != null) {
//            mainApp.showOfficerDashboardFromMessenger();
        } else if (previousScene != null && currentStage != null) {
            currentStage.setScene(previousScene);
            currentStage.setTitle("Officer Dashboard");
        } else {
            if (currentStage != null) {
                currentStage.close();
            }
        }
    }

    private void connectToServer() {
        // Reset intentional disconnect flag when connecting
        intentionalDisconnect = false;

        // Try all servers until one connects
        while (currentServerIndex < SERVER_PORTS.length) {
            try {
                int port = SERVER_PORTS[currentServerIndex];
                System.out.println("Attempting to connect to server on port: " + port);

                clientSocket = new Socket("localhost", port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Send join message
                String joinMessage = "JOIN:" + username + ":" + currentRoom;
                out.println(joinMessage);
                System.out.println("Connected to server on port: " + port + " and sent: " + joinMessage);

                // Start message listener thread
                executor.execute(this::listenForMessages);

                // REMOVED: Connection success message to user
                // No "Connected to server on port X" message in chat
                return;

            } catch (IOException e) {
                System.out.println("Failed to connect to port " + SERVER_PORTS[currentServerIndex] + ": " + e.getMessage());
                currentServerIndex++;

                if (currentServerIndex < SERVER_PORTS.length) {
                    // REMOVED: Trying next server message to user
                }
            }
        }

        // If all servers failed
        showError("Cannot connect to any chat servers! Please make sure servers are running.");
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (intentionalDisconnect) {
                    break;
                }

                System.out.println("Received from server: " + message);
                String finalMessage = message;
                javafx.application.Platform.runLater(() -> handleIncomingMessage(finalMessage));
            }
        } catch (IOException e) {
            if (!intentionalDisconnect) {
                javafx.application.Platform.runLater(() -> {
                    showError("Connection lost: " + e.getMessage());
                    // REMOVED: Disconnected message to user
                    reconnectToServer();
                });
            } else {
                System.out.println("Intentional disconnect - no error message needed");
            }
        } finally {
            System.out.println("Message listener thread stopped");
        }
    }

    private void reconnectToServer() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reset and try to connect again
        currentServerIndex = 0;
        connectToServer();
    }

    private void handleIncomingMessage(String message) {
        System.out.println("Processing message: " + message);

        if (message.startsWith("MESSAGE:")) {
            String[] parts = message.substring(8).split(":", 3);
            if (parts.length == 3) {
                String sender = parts[0];
                String timestamp = parts[1];
                String content = parts[2];

                if (!sender.equals(username)) {
                    addMessageToChat(sender, timestamp, content);
                }
            }
        } else if (message.startsWith("FILE:")) {
            String[] parts = message.substring(5).split(":", 3);
            if (parts.length == 3) {
                String sender = parts[0];
                String timestamp = parts[1];
                String filename = parts[2];

                if (!sender.equals(username)) {
                    addFileMessage(sender, timestamp, filename);
                }
            }
        } else if (message.startsWith("USER_JOIN:")) {
            String[] parts = message.substring(10).split(":");
            if (parts.length == 2) {
                String user = parts[0];
                String room = parts[1];
                if (!user.equals(username)) {
                    addSystemMessage(user + " joined the room");
                }
            }
        } else if (message.startsWith("USER_LEAVE:")) {
            String user = message.substring(11);
            if (!user.equals(username)) {
                addSystemMessage(user + " left the room");
            }
        }
    }

    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty() && out != null) {
            String timestamp = getCurrentTime();
            String fullMessage = "MESSAGE:" + username + ":" + timestamp + ":" + messageText;

            out.println(fullMessage);
            System.out.println("Sent to server: " + fullMessage);

            addMessageToChat(username, timestamp, messageText);
            messageInput.clear();
        }
    }

    @FXML
    private void attachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Share");
        File file = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (file != null && out != null) {
            String timestamp = getCurrentTime();
            String fileMessage = "FILE:" + username + ":" + timestamp + ":" + file.getName();

            out.println(fileMessage);
            System.out.println("Sent file info: " + fileMessage);

            addFileMessage(username, timestamp, file.getName());
            addSystemMessage("File '" + file.getName() + "' shared successfully");
        }
    }

    private void addMessageToChat(String sender, String time, String content) {
        VBox messageBubble = new VBox(5);
        messageBubble.getStyleClass().add("message-bubble");

        if (sender.equals(username)) {
            messageBubble.getStyleClass().add("my-message");
        } else if (sender.equals("Admin")) {
            messageBubble.getStyleClass().add("admin-message");
        }

        HBox header = new HBox(10);
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("sender-name");

        if (sender.equals("Admin")) {
            senderLabel.getStyleClass().add("admin-name");
        } else if (sender.equals(username)) {
            senderLabel.getStyleClass().add("my-name");
        }

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("message-time");

        header.getChildren().addAll(senderLabel, timeLabel);

        Label messageLabel = new Label(content);
        messageLabel.getStyleClass().add("message-text");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(600);

        messageBubble.getChildren().addAll(header, messageLabel);
        chatContainer.getChildren().add(messageBubble);

        javafx.application.Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void addFileMessage(String sender, String time, String filename) {
        VBox messageBubble = new VBox(5);
        messageBubble.getStyleClass().addAll("message-bubble");

        if (sender.equals("Admin")) {
            messageBubble.getStyleClass().add("admin-message");
        }

        HBox header = new HBox(10);
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("sender-name");

        if (sender.equals("Admin")) {
            senderLabel.getStyleClass().add("admin-name");
        }

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("message-time");

        header.getChildren().addAll(senderLabel, timeLabel);

        Label messageLabel = new Label("Shared a file:");
        messageLabel.getStyleClass().add("message-text");

        HBox fileAttachment = new HBox(10);
        fileAttachment.getStyleClass().add("file-attachment");

        Label fileIcon = new Label("📎");
        fileIcon.getStyleClass().add("file-icon");

        Label fileLabel = new Label(filename);
        fileLabel.getStyleClass().add("file-name");

        fileAttachment.getChildren().addAll(fileIcon, fileLabel);
        messageBubble.getChildren().addAll(header, messageLabel, fileAttachment);
        chatContainer.getChildren().add(messageBubble);

        javafx.application.Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void addSystemMessage(String content) {
        javafx.application.Platform.runLater(() -> {
            HBox systemBox = new HBox();
            systemBox.setAlignment(javafx.geometry.Pos.CENTER);

            Label systemMessage = new Label(content);
            systemMessage.getStyleClass().add("system-message");
            systemMessage.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-padding: 5 0 5 0;");

            systemBox.getChildren().add(systemMessage);
            chatContainer.getChildren().add(systemBox);

            chatScrollPane.setVvalue(1.0);
        });
    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return String.format("%02d:%02d", hour, minute);
    }

    private void showError(String message) {
        if (!intentionalDisconnect) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public void shutdown() {
        try {
            if (out != null) {
                out.println("LEAVE:" + username);
                out.flush();
            }
            if (executor != null) {
                executor.shutdownNow();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            if (!intentionalDisconnect) {
                e.printStackTrace();
            }
        }
    }
}