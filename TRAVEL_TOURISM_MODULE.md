# Travel & Tourism Module - VoyagerPlus

## Overview
This module implements a complete Travel & Tourism system with Solo Trip and Group Trip functionality, including trip planning, collaboration features, and group chat creation.

## Features Implemented

### 1. Trip Types

#### Solo Trip
- User creates a personal trip plan (not open for join requests)
- Visible in user's profile and feed (read-only for others)
- Includes: Hotel Booking, Expense Calculator, Notepad, Map features
- Other users can view but cannot request to join

#### Group Trip
- User creates a trip plan and marks it as "Group"
- Open for join requests from other users
- Trip creator can approve/reject join requests
- Automatic group chat creation when first member is approved
- All approved members get access to the group chat

### 2. Trip Planner Features

#### Create Trip
Users can create a travel plan with:
- **Title**: Trip name/destination
- **Date**: Travel date using DatePicker
- **Route**: Travel route (e.g., "Paris → Rome → Venice")
- **Budget**: Trip budget in USD
- **Description**: Detailed trip description
- **Type Selection**: Radio buttons for Solo or Group trip

#### Post Trip
- Trips are automatically posted to the system when created
- Posted trips are visible to other users in the "Available Group Trips" section
- Solo trips are visible but marked as non-joinable

### 3. Group Tour & Collaboration

#### Request to Join
- Users can browse available group trips
- Click "Request to Join" button
- Enter a message explaining why they want to join
- Request is sent to the trip creator

#### Manage Join Requests
- Trip creators can view all pending join requests
- See requester details, message, and request time
- **Approve**: Adds user to trip members, creates/updates group chat
- **Reject**: Declines the request

#### Automatic Group Chat Creation
- When the first join request is approved, a group chat is automatically created
- Group chat includes the trip creator and all approved members
- Chat is named after the trip (e.g., "Paris Adventure - Group Chat")
- New approved members are automatically added to existing chat

## Project Structure

### Model Classes

1. **Trip.java**
   - Main trip entity with all trip details
   - Enum: TripType (SOLO, GROUP)
   - Enum: TripStatus (DRAFT, POSTED, IN_PROGRESS, COMPLETED)
   - Manages list of join requests and approved members
   - Stores reference to associated group chat

2. **JoinRequest.java**
   - Represents a request to join a group trip
   - Enum: RequestStatus (PENDING, APPROVED, REJECTED)
   - Stores requester info, message, and timestamp
   - Methods to approve/reject requests

3. **GroupChat.java**
   - Represents a group chat for a trip
   - Contains list of members (usernames)
   - Stores chat messages with timestamps
   - Inner class: ChatMessage (sender, content, timestamp)

4. **TripService.java**
   - Singleton service managing all trip operations
   - Methods for CRUD operations on trips
   - Handles join request approval/rejection
   - Manages group chat creation and member addition
   - Simulates current user (default: "User_Demo")

### Controllers

1. **TripPlannerController.java**
   - Main trip planner interface controller
   - Handles trip creation with Solo/Group selection
   - Displays user's own trips in "My Trips" section
   - Shows available group trips for joining
   - Displays selected trip details
   - Manages request to join functionality
   - Opens "Manage Requests" dialog for group trips

2. **ManageRequestsController.java**
   - Dialog for managing join requests
   - Lists all pending/processed requests
   - Shows request details (username, message, time, status)
   - Approve/Reject buttons for pending requests
   - Real-time status updates
   - Visual indicators for request status (⏳ Pending, ✅ Approved, ❌ Rejected)

3. **LandingController.java**
   - Entry point controller (existing)
   - Navigates to Trip Planner

### Views (FXML)

1. **TripPlanner.fxml**
   - BorderPane layout with 5 sections:
     - **Top**: Header with title and subtitle
     - **Left**: Create Trip form with all input fields
     - **Center**: Two list views (My Trips, Available Group Trips)
     - **Right**: Trip details panel
   - Buttons: Create Trip, Manage Requests, Request to Join, Refresh

2. **ManageRequests.fxml**
   - BorderPane dialog layout:
     - **Top**: Header
     - **Left**: List of join requests
     - **Center**: Request details and action buttons
   - Buttons: Approve, Reject, Refresh, Close

3. **Landing.fxml** (existing)
   - Welcome screen with Get Started button

### Styling

**TripPlanner.css**
- Modern gradient backgrounds
- Professional color scheme (blues, grays, whites)
- Styled buttons (primary, secondary, icon buttons)
- Custom list cell styling with hover effects
- Form input styling with focus effects
- Card-style sections with shadows
- Responsive layout support

## How to Use

### Creating a Solo Trip
1. Launch the application
2. Click "Get Started" on landing page
3. Fill in trip details (title, date, route, budget, description)
4. Select "🚶 Solo Trip" radio button
5. Click "Create Trip"
6. Trip appears in "My Trips" section
7. Others can view but cannot join

### Creating a Group Trip
1. Follow steps 1-3 above
2. Select "👥 Group Trip" radio button
3. Click "Create Trip"
4. Trip appears in "My Trips" and in other users' "Available Group Trips"
5. Click "Manage Requests" to handle join requests

### Joining a Group Trip
1. Browse "Available Group Trips" section
2. Click on a trip to view details
3. Click "Request to Join" button
4. Enter your message (why you want to join)
5. Wait for trip creator to approve

### Managing Join Requests (Trip Creator)
1. Select your group trip from "My Trips"
2. Click "Manage Requests" button
3. Review pending requests in the list
4. Click on a request to see details
5. Click "✅ Approve" to accept (creates group chat)
6. Click "❌ Reject" to decline
7. Approved members are added to the trip and group chat

## API Overview

### TripService Methods

```java
// Create a new trip
Trip createTrip(String title, LocalDate date, String route, double budget, String description, TripType type)

// Post a trip (make visible)
void postTrip(String tripId)

// Get user's trips
List<Trip> getUserTrips(String username)

// Get posted trips (public)
List<Trip> getPostedTrips()

// Request to join a trip
JoinRequest requestToJoin(String tripId, String message)

// Approve join request (creates/updates group chat)
GroupChat approveJoinRequest(String tripId, String requestId)

// Reject join request
void rejectJoinRequest(String tripId, String requestId)

// Get pending requests for a trip
List<JoinRequest> getPendingRequests(String tripId)
```

## Visual Indicators

- 🚶 Solo Trip icon
- 👥 Group Trip icon
- ⏳ Pending request
- ✅ Approved request/action
- ❌ Rejected request/action
- ⚠ Warning/error message
- 🔄 Refresh action

## Future Enhancements

1. **User Authentication**: Replace simulated user with real login system
2. **Database Integration**: Persist trips, requests, and chats to database
3. **Real-time Chat**: Implement functional group chat with real-time messaging
4. **Hotel Booking**: Integrate hotel booking API and UI
5. **Expense Calculator**: Add expense tracking and splitting features
6. **Map Integration**: Add interactive map with route planning
7. **Notepad**: Add collaborative notepad for trip planning
8. **Notifications**: Send notifications when requests are approved/rejected
9. **Trip Feed**: Social feed showing all public trips
10. **Friend System**: Add friend connections to filter visible trips

## Technical Details

- **Framework**: JavaFX 17
- **Build Tool**: Maven
- **Java Version**: 25
- **Architecture**: MVC (Model-View-Controller)
- **Design Pattern**: Singleton (TripService)
- **UI Pattern**: FXML + CSS styling

## Running the Application

```bash
# Navigate to project directory
cd D:\VoyagerPlus\VoyagerPlus

# Compile the project
mvnw clean compile

# Run the application
mvnw javafx:run
```

## Notes

- Default current user is "User_Demo"
- All data is stored in-memory (resets on restart)
- Group chats are created automatically on first approval
- Solo trips are read-only for other users
- Trip creators cannot request to join their own trips

---

**Version**: 1.0  
**Last Updated**: October 17, 2025  
**Module**: Travel & Tourism - Voyager+

