# 🌍 Voyager+ — Java Travel Companion App

---

**Team Name:** Team ByteVoyagers  
**Project Name:** Voyager+  
**Team Members:**  
- Mahfuz Hasan Reza  
- Md. Al-Jubaer  

📘 [Now Work (Notion Link)](https://www.notion.so/Now-Work-28ff6da505048097af7bdc4daf01520d?pvs=21)  
📘 [Some Important Information](https://www.notion.so/Some-Important-Information-279f6da5050480a5906cd0d56a4046cb?pvs=21)

---

## 🚀 Overview

**Voyager+** is a Java-based travel planner and companion system designed to make trip management, expense tracking, hotel booking, and communication seamless for travelers.  
It integrates **JavaFX UI**, **OpenCV-based authentication**, and **Map API routes**, blending productivity with entertainment through quizzes and mini-games.

---

## 🧩 Features (Initial)

### 🔐 Authentication & Security
- User Registration & Login (with profile info & friend list)
- Optional **Face Detection Login** (via OpenCV)
- Profile Management

---

### ✈️ Travel & Tourism Module

#### 🧍 Solo Trip
- Private trip planner visible only to the creator
- Includes hotel booking, expense calculator, notepad, and map view

#### 👥 Group Trip
- Public trip creation (visible in feed)
- Other users can request to join
- On approval → automatic **Group Chat** creation

#### 🌐 Trip Planner
- Create plans (title, date, route, budget, description)
- Post plans (visible to friends or public)

#### 🏨 Hotel Booking
- Booking system (manual or mock data)
- Redeem **reward points** earned from quizzes

#### 💸 Expense Calculator
- Track actual vs. planned expenses
- Split bills among group members

---

### 💬 Communication
- **1-to-1 Chat** (friend-based messaging)
- **Group Chat** (auto-created for each group trip)

---

### 🧠 Productivity Tools
- **Notepad** – store lists or notes
- **Calculator** – quick travel cost calculations

---

### 🎯 Learning & Fun
- **Quiz Section** – earn reward points for travel/general quizzes
- **Mini Games** – Snake, Puzzle, etc., with optional rewards

---

### 🗺️ Smart Feature – Maps Integration
- Visualize routes like `Dhaka → Sreemangal → Sylhet`
- Integrated with **Google Maps API / OpenStreetMap**
- Auto-calculates **distance & estimated cost**

---

## ⚙️ Tech Stack

### 🖥️ Client Side (JavaFX App)
- **JavaFX + SceneBuilder** → UI
- **CSS** → Styling
- **OpenCV** → Face Detection
- **Google Maps API / Leaflet** → Map Integration
- **Java Sockets** → Real-time chat
- **Threads** → For background operations
- **JDBC (MySQL/SQLite)** → Local DB connection

### 🖧 Server Side (Java Backend)
- **Java Socket Server** → Chat & Group Trip communication
- **JDBC** → Database management (trip data, users, chats)
- **Thread Handling** → Multiple client connections

---

## 🧠 Must or Maybe Features

| Feature | Status |
|----------|---------|
| Thread concept implementation | ✅ Must |
| Travel Blog | ✅ Must |
| Face Detection Login (OpenCV) | 🟡 Maybe |
| AI Travel Assistant (Gemini API) | 🟡 Maybe |

---

## 🧭 User Flow

1. **Create Trip**
   - Choose SOLO or GROUP
   - Add route, dates, budget, description
   - SOLO → Private; GROUP → Public & Joinable

2. **Join Requests**
   - Group trip posts visible in feed
   - Others request to join → trip owner approves → Group Chat auto-created

3. **Map Integration**
   - Route visualization for both SOLO & GROUP
   - Distance & time calculation

---

## 🧱 Project Structure

