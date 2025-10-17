# 🚀 HOW TO RUN VOYAGER+ APPLICATION

## Prerequisites
1. ✅ Node.js backend running on http://localhost:5000
2. ✅ MongoDB running (if your backend needs it)

---

## 🎯 EASIEST METHOD - Double Click to Run

Simply **double-click** this file:
```
D:\VoyagerPlus\VoyagerPlus\run.bat
```

This will:
1. Download all dependencies (including BCrypt)
2. Compile the project
3. Launch the application
4. Show the Welcome screen

---

## 💻 MANUAL METHOD - Using Command Prompt

### Step 1: Open Command Prompt
- Press `Windows + R`
- Type: `cmd`
- Press Enter

### Step 2: Navigate to project
```bash
cd D:\VoyagerPlus\VoyagerPlus
```

### Step 3: Clean and Compile (downloads dependencies)
```bash
mvnw.cmd clean compile
```
Wait for this to complete (1-2 minutes first time)

### Step 4: Run the application
```bash
mvnw.cmd javafx:run
```

---

## ✨ WHAT YOU'LL SEE

1. **Welcome Screen** appears with:
   - Beautiful gradient background
   - Large "Voyager+" title
   - Two buttons: "Create Account" and "Sign In"

2. **Click "Create Account"** to test signup:
   - Fill in the form
   - Password gets hashed with BCrypt
   - Data posted to http://localhost:5000/user
   - User inserted into your MongoDB!

3. **Click "Sign In"** to login (after signup)

---

## 🔍 TROUBLESHOOTING

### Error: "package org.mindrot.jbcrypt does not exist"
**Solution**: Run the compile command again
```bash
mvnw.cmd clean compile
```
This downloads BCrypt library

### Error: "Connection refused" when signing up
**Solution**: Start your Node.js backend first
```bash
node server.js
```

### Application doesn't start
**Solution**: Make sure Java is installed
```bash
java -version
```
Should show Java 17 or higher

---

## 📝 QUICK TEST

1. Make sure Node.js backend is running: `node server.js`
2. Run: Double-click `run.bat` OR run `mvnw.cmd javafx:run`
3. Click "Create Account"
4. Fill form:
   - Full Name: Test User
   - Username: testuser
   - Email: test@test.com
   - Password: test123
   - Confirm: test123
5. Click "Create Account"
6. Check Node.js console - you'll see the user data!
7. Check MongoDB - user is inserted!

---

## 🎉 YOU'RE READY!

Just double-click `run.bat` and the application will start!

