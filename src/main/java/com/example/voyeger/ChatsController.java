package com.example.voyeger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatsController {

    @FXML
    private ListView<GroupChat> chatListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox emptyChatState;

    @FXML
    private BorderPane activeChatPane;

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
    private Label chatStatusLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button chatInfoButton;

    private TripService tripService;
    private GroupChat currentChat;
    private String currentUsername;
    private ObservableList<GroupChat> allChats;
    private Timer refreshTimer;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();
        currentUsername = tripService.getCurrentUser() != null ?
                         tripService.getCurrentUser().getUsername() : "Unknown";

        allChats = FXCollections.observableArrayList();
        chatListView.setItems(allChats);

        // Setup chat list cell factory
        chatListView.setCellFactory(lv -> new ChatListCell());

        // Handle chat selection
        chatListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadChat(newVal);
                }
            }
        );

        // Auto-scroll messages
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterChats(newVal));

        // Load all chats
        loadAllChats();

        // Start auto-refresh
        startAutoRefresh();
    }

    private void loadAllChats() {
        allChats.clear();

        try {
            System.out.println("🔍 Loading all group chats for user: " + currentUsername);

            // Fetch all group trips from backend (where user is creator or member)
            List<Trip> allGroupTrips = new ArrayList<>();

            // Get trips from backend
            try {
                // Fetch all group trips (excluding current user's to avoid duplicates)
                List<Trip> backendGroupTrips = TripApiClient.fetchGroupTrips(null); // Get all group trips

                // Filter to only trips where current user is creator or member
                for (Trip trip : backendGroupTrips) {
                    if (trip.getCreatorUsername().equals(currentUsername) ||
                        trip.getApprovedMembers().contains(currentUsername)) {
                        allGroupTrips.add(trip);
                        // Cache the trip locally
                        tripService.cacheTrip(trip);
                    }
                }

                System.out.println("✅ Found " + allGroupTrips.size() + " group trips for current user");
            } catch (Exception e) {
                System.err.println("❌ Error fetching trips from backend: " + e.getMessage());
                e.printStackTrace();
            }

            // Also get locally cached trips
            List<Trip> userTrips = tripService.getUserTrips(currentUsername);
            List<Trip> memberTrips = tripService.getUserMemberTrips(currentUsername);

            // Merge with backend trips (avoid duplicates)
            Set<String> processedTripIds = new HashSet<>();
            for (Trip trip : allGroupTrips) {
                if (trip.isGroupTrip()) {
                    processedTripIds.add(trip.getId());
                }
            }

            for (Trip trip : userTrips) {
                if (trip.isGroupTrip() && !processedTripIds.contains(trip.getId())) {
                    allGroupTrips.add(trip);
                    processedTripIds.add(trip.getId());
                }
            }

            for (Trip trip : memberTrips) {
                if (trip.isGroupTrip() && !processedTripIds.contains(trip.getId())) {
                    allGroupTrips.add(trip);
                    processedTripIds.add(trip.getId());
                }
            }

            System.out.println("📊 Total group trips to process: " + allGroupTrips.size());

            // Load chats from all group trips
            for (Trip trip : allGroupTrips) {
                try {
                    GroupChat chat = null;

                    // First try to get from local cache
                    if (trip.getGroupChatId() != null) {
                        chat = tripService.getGroupChat(trip.getGroupChatId());
                    }

                    // If not in cache, try to fetch from backend
                    if (chat == null) {
                        System.out.println("🔍 Fetching chat for trip: " + trip.getTitle());
                        chat = TripApiClient.fetchGroupChat(trip.getId());
                        if (chat != null) {
                            tripService.cacheGroupChat(chat);
                            trip.setGroupChatId(chat.getId());
                            System.out.println("✅ Loaded chat: " + chat.getChatName());
                        } else {
                            System.out.println("⚠️ No chat found for trip: " + trip.getTitle() + " (ID: " + trip.getId() + ")");
                        }
                    }

                    if (chat != null) {
                        allChats.add(chat);
                        System.out.println("✅ Added chat to list: " + chat.getChatName());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error loading chat for trip " + trip.getTitle() + ": " + e.getMessage());
                }
            }

            System.out.println("📊 Total chats loaded: " + allChats.size());

            if (allChats.isEmpty()) {
                statusLabel.setText("No group chats yet. Approve join requests to create group chats!");
                statusLabel.setStyle("-fx-text-fill: #888;");
            } else {
                statusLabel.setText(allChats.size() + " chat(s) available");
                statusLabel.setStyle("-fx-text-fill: #666;");
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading chats: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error loading chats");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void filterChats(String query) {
        if (query == null || query.trim().isEmpty()) {
            chatListView.setItems(allChats);
            return;
        }

        String lowerQuery = query.toLowerCase();
        ObservableList<GroupChat> filtered = FXCollections.observableArrayList();

        for (GroupChat chat : allChats) {
            if (chat.getChatName().toLowerCase().contains(lowerQuery) ||
                chat.getMembers().stream().anyMatch(m -> m.toLowerCase().contains(lowerQuery))) {
                filtered.add(chat);
            }
        }

        chatListView.setItems(filtered);
    }

    private void loadChat(GroupChat chat) {
        this.currentChat = chat;

        // Hide empty state, show chat interface
        emptyChatState.setVisible(false);
        activeChatPane.setVisible(true);

        // Update header
        chatTitleLabel.setText(chat.getChatName());
        updateMembersLabel(chat);

        // Load messages
        loadMessages(chat);

        System.out.println("✅ Loaded chat: " + chat.getChatName());
    }

    private void updateMembersLabel(GroupChat chat) {
        if (chat != null && chat.getMembers() != null) {
            String members = String.join(", ", chat.getMembers());
            membersLabel.setText(members + " (" + chat.getMembers().size() + ")");
        }
    }

    private void loadMessages(GroupChat chat) {
        messagesContainer.getChildren().clear();

        if (chat == null || chat.getMessages() == null) {
            addSystemMessage("No messages yet. Start the conversation!");
            return;
        }

        if (chat.getMessages().isEmpty()) {
            addSystemMessage("Welcome to the group chat! 👋");
        } else {
            for (GroupChat.ChatMessage msg : chat.getMessages()) {
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

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setMaxWidth(400);

        if (!isOwnMessage) {
            Label senderLabel = new Label(message.getSender());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            senderLabel.setStyle("-fx-text-fill: #555;");
            bubble.getChildren().add(senderLabel);
        }

        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font(14));
        contentLabel.setStyle("-fx-text-fill: " + (isOwnMessage ? "white" : "#333") + ";");
        bubble.getChildren().add(contentLabel);

        Label timeLabel = new Label(formatTimestamp(message.getTimestamp()));
        timeLabel.setFont(Font.font(10));
        timeLabel.setStyle("-fx-text-fill: " + (isOwnMessage ? "#E0E0E0" : "#888") + ";");
        bubble.getChildren().add(timeLabel);

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
            showChatStatus("Please enter a message", "warning");
            return;
        }

        if (currentChat == null) {
            showChatStatus("No chat selected", "error");
            return;
        }

        GroupChat.ChatMessage newMessage = new GroupChat.ChatMessage(currentUsername, content);
        currentChat.addMessage(newMessage);

        addMessageBubble(newMessage);
        messageInput.clear();

        sendMessageToBackend(content);
        showChatStatus("Message sent ✓", "success");
    }

    private void sendMessageToBackend(String content) {
        new Thread(() -> {
            try {
                boolean success = TripApiClient.sendChatMessage(
                    currentChat.getTripId(),
                    currentUsername,
                    content
                );

                if (success) {
                    System.out.println("✅ Message sent to backend");
                } else {
                    System.err.println("⚠️ Failed to send message to backend");
                }
            } catch (Exception e) {
                System.err.println("❌ Error sending message: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && (event.isControlDown() || event.isMetaDown())) {
            handleSend();
            event.consume();
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllChats();
        if (currentChat != null) {
            refreshCurrentChat();
        }
        showStatus("Refreshed! ✓", "success");
    }

    private void refreshCurrentChat() {
        if (currentChat == null) return;

        new Thread(() -> {
            try {
                GroupChat updatedChat = TripApiClient.fetchGroupChat(currentChat.getTripId());

                if (updatedChat != null && updatedChat.getMessages().size() > currentChat.getMessages().size()) {
                    Platform.runLater(() -> {
                        currentChat.setMessages(updatedChat.getMessages());
                        loadMessages(currentChat);
                    });
                }
            } catch (Exception e) {
                System.err.println("❌ Error refreshing chat: " + e.getMessage());
            }
        }).start();
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentChat != null) {
                    refreshCurrentChat();
                }
            }
        }, 5000, 5000);
    }

    private void showStatus(String message, String type) {
        Platform.runLater(() -> {
            statusLabel.setText(message);

            switch (type) {
                case "success" -> statusLabel.setStyle("-fx-text-fill: green;");
                case "error" -> statusLabel.setStyle("-fx-text-fill: red;");
                case "warning" -> statusLabel.setStyle("-fx-text-fill: orange;");
                default -> statusLabel.setStyle("-fx-text-fill: #666;");
            }

            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> statusLabel.setText(allChats.size() + " chat(s) available"));
                }
            }, 3000);
        });
    }

    private void showChatStatus(String message, String type) {
        Platform.runLater(() -> {
            chatStatusLabel.setText(message);

            switch (type) {
                case "success" -> chatStatusLabel.setStyle("-fx-text-fill: green;");
                case "error" -> chatStatusLabel.setStyle("-fx-text-fill: red;");
                case "warning" -> chatStatusLabel.setStyle("-fx-text-fill: orange;");
                default -> chatStatusLabel.setStyle("-fx-text-fill: #666;");
            }

            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> chatStatusLabel.setText(""));
                }
            }, 3000);
        });
    }

    // Custom ListCell for chat list
    private class ChatListCell extends ListCell<GroupChat> {
        @Override
        protected void updateItem(GroupChat chat, boolean empty) {
            super.updateItem(chat, empty);

            if (empty || chat == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                VBox content = new VBox(5);
                content.setPadding(new Insets(10));

                Label nameLabel = new Label(chat.getChatName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameLabel.setStyle("-fx-text-fill: #333;");

                String membersText = chat.getMembers().size() + " members";
                Label membersLabel = new Label(membersText);
                membersLabel.setFont(Font.font(11));
                membersLabel.setStyle("-fx-text-fill: #888;");

                int messageCount = chat.getMessages() != null ? chat.getMessages().size() : 0;
                Label msgCountLabel = new Label(messageCount + " messages");
                msgCountLabel.setFont(Font.font(10));
                msgCountLabel.setStyle("-fx-text-fill: #0084FF;");

                content.getChildren().addAll(nameLabel, membersLabel, msgCountLabel);
                setGraphic(content);

                setStyle("-fx-background-color: white; -fx-border-color: #e4e6eb; -fx-border-width: 0 0 1 0;");

                setOnMouseEntered(e -> setStyle("-fx-background-color: #f5f6f7; -fx-border-color: #e4e6eb; -fx-border-width: 0 0 1 0;"));
                setOnMouseExited(e -> setStyle("-fx-background-color: white; -fx-border-color: #e4e6eb; -fx-border-width: 0 0 1 0;"));
            }
        }
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
}
