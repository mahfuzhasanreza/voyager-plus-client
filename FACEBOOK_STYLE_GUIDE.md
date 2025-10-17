# Voyager+ - Facebook-like Social Travel Platform

## 🎉 Complete Multi-Page Application

A comprehensive social travel platform with Facebook-like features including news feed, profile management, settings, and trip planning.

---

## 📱 Main Features

### 1. **Navigation System**
- **Top Navigation Bar** - Always visible with quick access to all pages
- **News Feed** (🏠) - View all trip posts from users
- **Plan Trip** (✈) - Create and manage your trips
- **Profile** (👤) - View your profile and posts
- **Settings** (⚙) - Update your profile information

### 2. **News Feed Page** (Like Facebook Feed)
- **Create Trip Posts** - Share your travel plans with everyone
- **Interactive Post Cards** with:
  - User avatar and name
  - Post timestamp (e.g., "2 hours ago")
  - Trip details card with route, date, budget
  - Like, Comment, and Join buttons
  - Real-time like counter
- **Left Sidebar** - Create post form with quick tips
- **Right Sidebar** - Trending destinations
- **Automatic Posting** - When you create a trip, it appears in everyone's feed
- **Request to Join** - Click join button on any group trip post

### 3. **Profile Page**
- **Cover Photo Banner** with gradient background
- **Profile Avatar Circle** with first letter of name
- **User Information**:
  - Display name
  - Username (@username)
  - Bio/About section
  - Email address
  - Join date
- **Statistics Cards**:
  - Total trips count
  - Total posts count
- **My Trips List** - All your created trips
- **My Posts Section** - Mini cards showing all your trip posts with stats

### 4. **Settings Page**
- **Profile Information Section**:
  - Change display name
  - Update email address
  - Edit bio/about text
- **Profile Images Section**:
  - Upload profile picture (with file browser)
  - Upload cover photo (with file browser)
- **Action Buttons**:
  - Save Settings - Updates your profile
  - Reset - Reverts to current values
- **Real-time Status Updates** - Success/error messages
- **Auto-clear Status** - Messages disappear after 3 seconds

### 5. **Trip Planner Page** (Enhanced)
- **Left Panel** - Create trip form
- **Center Panel** - My trips & Available trips lists
- **Right Panel** - Trip details view
- **Automatic News Feed Post** - Creates a post when trip is created
- **Manage Join Requests** - For group trip creators

---

## 🎨 Design Features

### Facebook-like Styling
- **Modern Color Scheme**:
  - Primary Blue: #1877f2 (Facebook blue)
  - Background: #f0f2f5 (Light gray)
  - White cards with subtle shadows
  - Gray text: #65676b
  
- **Card-based Layout**:
  - Elevated white cards
  - Rounded corners (8px radius)
  - Drop shadows for depth
  
- **Interactive Elements**:
  - Hover effects on buttons
  - Active state highlighting
  - Smooth transitions

### Responsive Components
- Avatar circles with user initials
- Emoji icons throughout UI
- Status indicators (Online, Posted, etc.)
- Time-ago format ("2 hours ago", "Just now")

---

## 🔄 User Workflow

### Creating and Sharing a Trip
1. Click **"Plan Trip"** in navigation OR use sidebar in News Feed
2. Fill in trip details (title, date, route, budget, description)
3. Choose **Solo** or **Group** trip type
4. Write a post message about your trip
5. Click **"Create Trip"**
6. Trip is automatically posted to News Feed for all users to see!

### Viewing News Feed
1. Click **"🏠 News Feed"** in navigation (default page)
2. Scroll through all trip posts from all users
3. Click **"❤ Like"** to like a post
4. Click **"✈ Join Trip"** to request joining a group trip
5. See trip details directly in the post card

### Managing Your Profile
1. Click **"👤 Profile"** in navigation
2. View your stats (trips count, posts count)
3. Browse your trip history
4. See all your posts with engagement metrics

### Updating Settings
1. Click **"⚙ Settings"** in navigation
2. Update your display name, email, bio
3. Browse and select profile picture/cover photo
4. Click **"💾 Save Settings"**
5. Changes are immediately visible in navigation bar

### Joining Group Trips
1. Browse News Feed or Trip Planner
2. Find a group trip (marked with 👥)
3. Click **"✈ Join Trip"** button
4. Enter a message explaining why you want to join
5. Wait for trip creator to approve
6. Get added to trip members and group chat!

### Managing Join Requests (Trip Creator)
1. Go to **"Plan Trip"** page
2. Select your group trip from **"My Trips"** list
3. Click **"Manage Requests"** button
4. Review pending requests with user messages
5. Click **"✅ Approve"** to add member (creates group chat)
6. Click **"❌ Reject"** to decline request

---

## 🗂 File Structure

### Java Controllers (8 files)
```
Main.java                      - Application entry point
MainController.java            - Navigation controller
NewsFeedController.java        - News feed page
ProfileController.java         - Profile page
SettingsController.java        - Settings page
TripPlannerController.java     - Trip planner page
ManageRequestsController.java  - Join requests dialog
CreateTripDialogController.java - Create trip dialog
```

### Models (6 files)
```
User.java                      - User profile data
Trip.java                      - Trip information
TripPost.java                  - News feed post
JoinRequest.java              - Join request data
GroupChat.java                - Group chat data
TripService.java              - Business logic service
```

### FXML Views (6 files)
```
Main.fxml                      - Main navigation layout
NewsFeed.fxml                 - News feed page
Profile.fxml                  - Profile page
Settings.fxml                 - Settings page
TripPlanner.fxml              - Trip planner page
ManageRequests.fxml           - Join requests dialog
CreateTripDialog.fxml         - Create trip dialog
```

### Styles (2 files)
```
Main.css                       - Modern Facebook-like styling
TripPlanner.css               - Additional trip planner styles
```

---

## 💡 Key Technical Features

### Service Layer (TripService)
- **Singleton Pattern** - One instance manages all data
- **In-memory Storage** - Maps for users, trips, posts
- **Demo Users** - Pre-loaded with 3 sample users
- **Methods**:
  - User management (get, update, current user)
  - Trip CRUD operations
  - News feed management
  - Join request handling
  - Like/Unlike functionality

### News Feed System
- **Automatic Post Creation** - Creates `TripPost` when trip is posted
- **Chronological Sorting** - Most recent posts first
- **Dynamic Card Generation** - Creates post cards on-the-fly
- **Interactive Actions** - Like, comment, join in real-time

### Navigation System
- **BorderPane Layout** - Top navbar, dynamic center content
- **Page Loading** - Loads FXML pages dynamically
- **Active State** - Highlights current page button
- **User Info Display** - Shows current user in navbar

### Profile System
- **User Model** - Complete user profile with bio, images, dates
- **Statistics Tracking** - Counts trips and posts
- **Post History** - Shows all user's trip posts
- **Mini Post Cards** - Compact view with engagement stats

---

## 🎯 User Experience Highlights

### Visual Feedback
- ✅ Success messages in green
- ⚠ Warning messages in red
- 🔄 Loading/refresh indicators
- Real-time counter updates

### Intuitive Design
- Icons for all actions (❤ 💬 ✈ 🏠 👤 ⚙)
- Clear section labels
- Breadcrumb-style information
- Hover effects on interactive elements

### Social Features
- Public trip posts visible to all
- Private solo trips (view-only)
- Group trip collaboration
- Like and engagement system
- Time-ago timestamps

---

## 🚀 Getting Started

### Run the Application
```bash
cd D:\VoyagerPlus\VoyagerPlus
mvnw.cmd clean compile
mvnw.cmd javafx:run
```

### Default User
- **Username**: user_demo
- **Display Name**: Demo User
- **Email**: demo@voyagerplus.com
- **Bio**: "Adventure seeker and travel enthusiast! 🌍✈️"

### Quick Test Flow
1. Launch app → **News Feed** loads by default
2. Use left sidebar to create a trip post
3. See it appear in the feed immediately
4. Click **Profile** to see your stats
5. Click **Settings** to update your name/bio
6. Click **Plan Trip** to create more trips
7. View other users' trips in Available Trips

---

## 📊 Data Models

### User
- username, displayName, email
- bio, profilePicturePath, coverPhotoPath
- joinedDate

### Trip
- title, date, route, budget, description
- type (SOLO/GROUP), status, creator
- approvedMembers[], joinRequests[]
- groupChatId

### TripPost
- trip reference, author (User)
- content, postedAt timestamp
- likes, comments, shares counters
- getTimeAgo() method

---

## 🎨 Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| Facebook Blue | #1877f2 | Primary buttons, links, accents |
| Light Blue | #e7f3ff | Active states, backgrounds |
| Light Gray | #f0f2f5 | Page background |
| Medium Gray | #65676b | Secondary text |
| Dark Text | #050505 | Primary text |
| White | #ffffff | Cards, surfaces |
| Border Gray | #e4e6eb | Borders, separators |

---

## 🔮 Future Enhancements

1. **Real Comments System** - Add comment threads to posts
2. **Friend System** - Add/remove friends, friend-only posts
3. **Notifications** - Real-time notifications for likes, comments, requests
4. **Search** - Search for users, trips, destinations
5. **Filters** - Filter news feed by trip type, date, budget
6. **Image Upload** - Actual image upload for profile/cover photos
7. **Trip Photos** - Add photo galleries to trips
8. **Real-time Chat** - Functional group chat with messages
9. **Trip Timeline** - Visual timeline of trip progress
10. **Database Integration** - Persist data with MySQL/PostgreSQL

---

## 📝 Notes

- All data is **in-memory** (resets on restart)
- Currently supports **3 demo users**
- Posts are **public to all users**
- **Group chats** created automatically on first approval
- **Solo trips** visible but not joinable

---

**Version**: 2.0  
**Date**: October 17, 2025  
**Platform**: JavaFX 17 + Maven  
**Style**: Facebook-inspired Social Network

