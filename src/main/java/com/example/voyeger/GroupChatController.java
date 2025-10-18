package com.example.voyeger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class GroupChatController {

    @FXML
    private Label chatTitleLabel;

    @FXML
    private Label membersLabel;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextArea messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button closeButton;

    @FXML
    private Label statusLabel;

    private GroupChat groupChat;
    private String currentUsername;
    private Timer refreshTimer;
    private TripService tripService;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();

        // Auto-scroll to bottom when new messages appear
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });

        // Style the input area
        messageInput.setStyle("-fx-font-size: 14px;");
    }

    public void setGroupChat(GroupChat groupChat, String currentUsername) {
        this.groupChat = groupChat;
        this.currentUsername = currentUsername;

        if (groupChat != null) {
            chatTitleLabel.setText(groupChat.getChatName());
            updateMembersLabel();
            loadMessages();
            startAutoRefresh();
        }
    }

    private void updateMembersLabel() {
        if (groupChat != null && groupChat.getMembers() != null) {
            String members = String.join(", ", groupChat.getMembers());
            membersLabel.setText(members + " (" + groupChat.getMembers().size() + ")");
        }
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();

        if (groupChat == null || groupChat.getMessages() == null) {
            addSystemMessage("No messages yet. Start the conversation!");
            return;
        }

        if (groupChat.getMessages().isEmpty()) {
            addSystemMessage("Welcome to the group chat! 👋");
        } else {
            for (GroupChat.ChatMessage msg : groupChat.getMessages()) {
                addMessageBubble(msg);
            }
        }
    }

    private void addSystemMessage(String text) {
        Label systemLabel = new Label(text);
        systemLabel.setStyle(
            "-fx-text-fill: #888; " +
            "-fx-font-style: italic; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 10px;"
        );
        systemLabel.setWrapText(true);
        systemLabel.setMaxWidth(450);

        HBox container = new HBox(systemLabel);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5, 0, 5, 0));

        messagesContainer.getChildren().add(container);
    }

    private void addMessageBubble(GroupChat.ChatMessage message) {
        boolean isOwnMessage = message.getSender().equals(currentUsername);

        // Create message bubble
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setMaxWidth(400);

        // Sender name (only for other people's messages)
        if (!isOwnMessage) {
            Label senderLabel = new Label(message.getSender());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            senderLabel.setStyle("-fx-text-fill: #555;");
            bubble.getChildren().add(senderLabel);
        }

        // Message content
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font(14));
        contentLabel.setStyle("-fx-text-fill: " + (isOwnMessage ? "white" : "#333") + ";");
        bubble.getChildren().add(contentLabel);

        // Timestamp
        Label timeLabel = new Label(formatTimestamp(message.getTimestamp()));
        timeLabel.setFont(Font.font(10));
        timeLabel.setStyle("-fx-text-fill: " + (isOwnMessage ? "#E0E0E0" : "#888") + ";");
        bubble.getChildren().add(timeLabel);

        // Style the bubble
        if (isOwnMessage) {
            bubble.setStyle(
                "-fx-background-color: #0084FF; " +
                "-fx-background-radius: 15px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
        } else {
            bubble.setStyle(
                "-fx-background-color: #E4E6EB; " +
                "-fx-background-radius: 15px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
        }

        // Create container to align bubble
        HBox container = new HBox(bubble);
        container.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.setPadding(new Insets(5, 10, 5, 10));

        messagesContainer.getChildren().add(container);
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) return "";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            return timeFormatter.format(timestamp);
        } else {
            return dateFormatter.format(timestamp);
        }
    }

    @FXML
    private void handleSend() {
        String content = messageInput.getText().trim();

        if (content.isEmpty()) {
            showStatus("Please enter a message", "warning");
            return;
        }

        if (groupChat == null) {
            showStatus("Group chat not loaded", "error");
            return;
        }

        // Create and add message locally
        GroupChat.ChatMessage newMessage = new GroupChat.ChatMessage(currentUsername, content);
        groupChat.addMessage(newMessage);

        // Add to UI immediately
        addMessageBubble(newMessage);

        // Clear input
        messageInput.clear();

        // Send to backend (in-memory storage)
        sendMessageToBackend(content);

        showStatus("Message sent ✓", "success");
    }

    private void sendMessageToBackend(String content) {
        // This will be implemented to call the backend API
        // For now, it's stored locally
        new Thread(() -> {
            try {
                boolean success = TripApiClient.sendChatMessage(
                    groupChat.getTripId(),
                    currentUsername,
                    content
                );

                if (success) {
                    System.out.println("✅ Message sent to backend");
                } else {
                    System.err.println("⚠️ Failed to send message to backend (using local only)");
                }
            } catch (Exception e) {
                System.err.println("❌ Error sending message: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        // Send message on Ctrl+Enter or Cmd+Enter
        if (event.getCode() == KeyCode.ENTER && (event.isControlDown() || event.isMetaDown())) {
            handleSend();
            event.consume();
        }
    }

    @FXML
    private void handleClose() {
        stopAutoRefresh();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void startAutoRefresh() {
        // Refresh messages every 5 seconds
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshMessages();
            }
        }, 5000, 5000); // Start after 5 seconds, repeat every 5 seconds
    }

    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void refreshMessages() {
        if (groupChat == null) return;

        // Fetch updated group chat from backend
        new Thread(() -> {
            try {
                GroupChat updatedChat = TripApiClient.fetchGroupChat(groupChat.getTripId());

                if (updatedChat != null && updatedChat.getMessages().size() > groupChat.getMessages().size()) {
                    // New messages available
                    Platform.runLater(() -> {
                        groupChat.setMessages(updatedChat.getMessages());
                        loadMessages();
                    });
                }
            } catch (Exception e) {
                System.err.println("❌ Error refreshing messages: " + e.getMessage());
            }
        }).start();
    }

    private void showStatus(String message, String type) {
        Platform.runLater(() -> {
            statusLabel.setText(message);

            switch (type) {
                case "success":
                    statusLabel.setStyle("-fx-text-fill: green;");
                    break;
                case "error":
                    statusLabel.setStyle("-fx-text-fill: red;");
                    break;
                case "warning":
                    statusLabel.setStyle("-fx-text-fill: orange;");
                    break;
                default:
                    statusLabel.setStyle("-fx-text-fill: #666;");
            }

            // Clear status after 3 seconds
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> statusLabel.setText(""));
                }
            }, 3000);
        });
    }
}

