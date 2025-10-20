# Private Chat System - One-to-One Socket-Based Chat

## Overview
This is a completely independent one-to-one chat system using Java sockets. It allows users to have private conversations in real-time.

## Features
- ✅ Real-time messaging using Java sockets
- ✅ User online status tracking
- ✅ Private one-to-one conversations
- ✅ Message history per conversation
- ✅ Clean, modern UI with dark theme
- ✅ Easy access via navbar button
- ✅ Independent from group chat functionality

## Architecture

### 1. **PrivateChatServer.java**
   - Socket server running on port 8888
   - Handles multiple client connections
   - Routes private messages between users
   - Maintains list of online users
   - Thread-safe communication

### 2. **PrivateChatController.java**
   - JavaFX controller for the chat UI
   - Connects to the server via socket
   - Handles sending/receiving messages
   - Manages chat history
   - Displays online users

### 3. **PrivateChat.fxml**
   - User interface layout
   - Split view: user list on left, chat on right
   - Search functionality
   - Message input area

### 4. **PrivateChat.css**
   - Dark theme styling
   - Facebook Messenger-inspired design
   - Responsive layout

## How to Use

### Step 1: Start the Chat Server
Before using the chat, you need to start the server:

**Option A: Using Command Line**
```cmd
cd D:\VoyagerPlus\VoyagerPlus
javac -d target\classes src\main\java\com\example\voyeger\PrivateChatServer.java
java -cp target\classes com.example.voyeger.PrivateChatServer
```

**Option B: Run from IDE**
- Open `PrivateChatServer.java`
- Right-click → Run 'PrivateChatServer.main()'
- Keep this running in the background

You should see:
```
Private Chat Server starting on port 8888
Server is running and waiting for connections...
```

### Step 2: Access the Chat
1. Launch the VoyagerPlus application
2. Log in to your account
3. Click the **"💬 Chat"** button in the navbar
4. The private chat window will open

### Step 3: Start Chatting
1. Wait for the connection status to show "● Online" (green)
2. The left panel shows all online users
3. Click on any user to start a conversation
4. Type your message in the bottom text field
5. Press Enter or click "Send" to send the message
6. Messages appear in real-time

## Server Protocol

The server uses a simple text-based protocol:

### Client → Server
- `PRIVATE:recipient:message` - Send a private message
- `GET_USERS` - Request list of online users
- `DISCONNECT` - Disconnect from server

### Server → Client
- `ENTER_USERNAME` - Request username
- `SUCCESS:message` - Connection successful
- `USERS:user1,user2,user3` - List of online users
- `MESSAGE:sender:content` - Incoming message
- `SENT:recipient:content` - Message sent confirmation
- `ERROR:message` - Error notification

## Configuration

### Change Server Port
Edit `PrivateChatServer.java`:
```java
private static final int PORT = 8888; // Change to your desired port
```

And update `PrivateChatController.java`:
```java
private static final int SERVER_PORT = 8888; // Match the server port
```

### Change Server Host (for remote server)
Edit `PrivateChatController.java`:
```java
private static final String SERVER_HOST = "localhost"; // Change to server IP
```

## Testing

### Test with Multiple Users
1. Start the server once
2. Run the application multiple times (different users)
3. Log in with different accounts
4. Open the Private Chat on each instance
5. All users should see each other in the online users list
6. Start chatting!

### Test Locally
You can test with one user by:
1. Opening multiple instances of the app
2. Each instance will connect with a different username
3. Chat between the instances

## Troubleshooting

### Connection Error
**Problem:** "Unable to connect to chat server"
**Solution:**
- Make sure the server is running
- Check if port 8888 is not blocked by firewall
- Verify the server is running on localhost:8888

### User Not Showing Online
**Problem:** Other users don't appear in the list
**Solution:**
- Click the refresh button (🔄)
- Check if both users are connected (green status)
- Restart the server and reconnect

### Messages Not Sending
**Problem:** Messages don't appear in chat
**Solution:**
- Check connection status (should be green "● Online")
- Make sure recipient is online
- Check server console for error messages

### Port Already in Use
**Problem:** Server says "Address already in use"
**Solution:**
- Another instance of the server is running
- Close it or change the port number
- On Windows: `netstat -ano | findstr :8888` to find the process

## Running Server as Background Service

### Windows
Create a batch file `start-chat-server.bat`:
```batch
@echo off
start "Chat Server" java -cp target\classes com.example.voyeger.PrivateChatServer
```

### Keep Server Running
For production, consider:
1. Running server on a dedicated machine
2. Using a process manager (e.g., PM2 with Java)
3. Setting up as a Windows Service

## Features to Add (Future)

- [ ] File sharing
- [ ] Voice messages
- [ ] Read receipts
- [ ] Typing indicators
- [ ] User avatars
- [ ] Message reactions
- [ ] Message deletion
- [ ] Chat history persistence (database)
- [ ] Encryption for security

## Security Notes

⚠️ **Current Implementation:**
- Messages are sent in plain text
- No encryption
- No authentication beyond username

📌 **For Production:**
- Implement SSL/TLS encryption
- Add proper authentication
- Validate all inputs
- Use secure password storage
- Implement rate limiting

## File Structure
```
src/main/java/com/example/voyeger/
├── PrivateChatServer.java      # Socket server
├── PrivateChatController.java  # UI controller
└── NavbarController.java       # Updated with chat button

src/main/resources/com/example/voyeger/
├── PrivateChat.fxml           # UI layout
├── PrivateChat.css            # Styling
└── Navbar.fxml                # Updated navbar
```

## Quick Start Commands

**Compile and Run Server:**
```cmd
cd D:\VoyagerPlus\VoyagerPlus
mvn clean compile
java -cp target\classes com.example.voyeger.PrivateChatServer
```

**Run Application:**
```cmd
mvn javafx:run
```

Or use the existing batch files:
```cmd
run.bat
```

## Support

For issues or questions:
1. Check the server console for error messages
2. Check the application console for client errors
3. Verify network connectivity
4. Ensure Java version compatibility (Java 11+)

---

**Enjoy chatting! 💬**

