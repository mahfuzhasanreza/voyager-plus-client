# 🎉 QUICK START GUIDE - MongoDB Authentication System

## ⚡ Ready to Run!

Everything is set up! Here's how to get started:

---

## 📋 Prerequisites

### 1. Install MongoDB (If not already installed)

**Download MongoDB Community Server:**
- Go to: https://www.mongodb.com/try/download/community
- Download for Windows
- Run the installer with default settings
- MongoDB will automatically start as a Windows Service

**Verify MongoDB is Running:**
```cmd
mongosh
```
If you see a connection message, MongoDB is ready! Type `exit` to close.

---

## 🚀 Running Your Application

### Step 1: Compile the Project
```cmd
cd D:\VoyagerPlus\VoyagerPlus
mvnw.cmd clean compile
```
This will download MongoDB and BCrypt dependencies (first time only).

### Step 2: Run the Application
```cmd
mvnw.cmd javafx:run
```

---

## 🎬 What You'll See

### 1️⃣ Welcome Screen (Landing Page)
- **Beautiful full-screen design** with gradient background
- Large "Voyager+" title with airplane emoji ✈
- Two prominent buttons:
  - **"Create Account"** - Blue button for new users
  - **"Sign In"** - Transparent button for existing users
- Feature highlights showing platform benefits
- Professional animations (fade-in effects)

### 2️⃣ Sign Up Page
When you click **"Create Account"**:
- **Split-screen design**:
  - **Left side**: Purple gradient with Voyager+ branding
  - **Right side**: Registration form
- Fill in:
  - Full Name (e.g., "John Doe")
  - Username (e.g., "johndoe") - must be unique
  - Email (e.g., "john@example.com")
  - Password (minimum 6 characters)
  - Confirm Password (must match)
- Click **"Create Account"**
- ⏳ Loading spinner appears
- ✅ Success! Data saved to MongoDB
- Auto-redirects to Sign In page

### 3️⃣ Sign In Page
After registration or clicking **"Sign In"**:
- Same beautiful split-screen design
- Enter your username (or email) and password
- Optional: Check "Remember me"
- Click **"Sign In"**
- ✅ Logged in! Redirects to main app (News Feed)

---

## 📊 Your Data in MongoDB

After signing up, your data is stored in MongoDB:

**Database:** `voyagerplus`  
**Collection:** `users`

**What's stored:**
- ✅ Username (unique)
- ✅ Email (unique)
- ✅ Password (BCrypt encrypted - super secure!)
- ✅ Full Name
- ✅ Profile information (bio, pictures)
- ✅ Timestamps (created, last login)

**View your data:**
```cmd
mongosh
use voyagerplus
db.users.find().pretty()
```

---

## 🎨 Features You Get

### Welcome Page Features:
- ✨ Animated title (fade-in effect)
- ✨ Slide-up subtitle animation
- 🎨 Beautiful gradient background
- 📱 Responsive button hover effects
- 🌟 Professional travel-themed design

### Sign Up Features:
- ✅ Real-time validation (username format, email, password strength)
- ✅ Duplicate checking (username and email)
- ✅ Password strength requirements (6+ characters)
- ✅ Confirm password matching
- ✅ Loading indicator during registration
- ✅ Success/error messages with color coding
- ✅ Auto-redirect after success
- ✅ Secure password hashing (BCrypt)

### Sign In Features:
- ✅ Login with username OR email
- ✅ Password verification
- ✅ Remember me option
- ✅ Forgot password link (ready for implementation)
- ✅ Loading indicator
- ✅ Auto-redirect to main app

---

## 🧪 Test It Out!

### Create Your First Account:
1. Run the app: `mvnw.cmd javafx:run`
2. Click **"Create Account"**
3. Fill in the form:
   ```
   Full Name: Test User
   Username: testuser
   Email: test@test.com
   Password: test123
   Confirm: test123
   ```
4. Click **"Create Account"**
5. ✅ Success! You'll be redirected to Sign In

### Login:
1. Enter: `testuser` (or `test@test.com`)
2. Password: `test123`
3. Click **"Sign In"**
4. 🎉 You're in the main app!

---

## 🔐 Security Features

Your application has enterprise-level security:

1. **BCrypt Password Hashing**
   - Passwords never stored in plain text
   - Industry-standard encryption
   - Salted hashing (prevents rainbow table attacks)

2. **Input Validation**
   - Username: 3+ chars, alphanumeric + underscore
   - Email: Valid format check
   - Password: Minimum 6 characters
   - All fields required

3. **Duplicate Prevention**
   - Username must be unique
   - Email must be unique
   - Real-time checking during registration

4. **Session Management**
   - User state maintained after login
   - Proper logout handling
   - Auto-close MongoDB connection on exit

---

## 🎯 Complete User Flow

```
Start App
   ↓
Welcome Page (Choose: Sign Up or Sign In)
   ↓
┌─────────────────┐         ┌──────────────────┐
│   Sign Up       │         │    Sign In       │
│  (New Users)    │         │ (Existing Users) │
└────────┬────────┘         └────────┬─────────┘
         ↓                            ↓
   Fill Form                    Enter Credentials
         ↓                            ↓
   Validate Input               Verify Password
         ↓                            ↓
   Save to MongoDB              Load User Data
         ↓                            ↓
   Redirect to Sign In ────────────→ │
                                     ↓
                            Main App (News Feed)
                                     ↓
                     Access All Features (Profile, Settings, etc.)
```

---

## 🛠️ Troubleshooting

### "Failed to connect to MongoDB"
**Problem:** MongoDB service not running  
**Solution:**
```cmd
net start MongoDB
```

### "Username already exists"
**Problem:** Someone already used that username  
**Solution:** Try a different username

### "Invalid email format"
**Problem:** Email doesn't have @ symbol  
**Solution:** Enter a valid email (e.g., user@example.com)

### "Passwords do not match"
**Problem:** Password and confirm password are different  
**Solution:** Make sure both fields have the same password

---

## 📁 Files Created

### Java Controllers (4 new files):
- `DatabaseManager.java` - MongoDB connection & operations
- `WelcomeController.java` - Landing page controller
- `SignUpController.java` - Registration logic
- `SignInController.java` - Login logic

### FXML Views (3 new files):
- `Welcome.fxml` - Landing page layout
- `SignUp.fxml` - Registration form layout
- `SignIn.fxml` - Login form layout

### Stylesheets (2 new files):
- `Welcome.css` - Landing page styling
- `Auth.css` - Sign up/in styling

### Configuration:
- `pom.xml` - Updated with MongoDB & BCrypt dependencies

---

## 🎨 Design Highlights

### Color Scheme:
- **Primary**: #667eea (Beautiful purple)
- **Gradient**: Purple to violet (#667eea → #764ba2)
- **Success**: #27ae60 (Green)
- **Error**: #e74c3c (Red)
- **Background**: White and light grays

### Typography:
- **Welcome Title**: 72px bold
- **Form Titles**: 36px bold
- **Input Fields**: 15px
- **Buttons**: 16-20px bold

### Effects:
- Drop shadows for depth
- Hover effects on buttons
- Focus effects on inputs
- Smooth transitions
- Fade-in animations

---

## ✅ What's Working

✅ Beautiful animated welcome page  
✅ Sign up form with MongoDB storage  
✅ Password encryption (BCrypt)  
✅ Email and username validation  
✅ Duplicate checking  
✅ Sign in with username or email  
✅ Password verification  
✅ Loading indicators  
✅ Success/error messages  
✅ Auto-navigation flow  
✅ Integration with main app  
✅ Profile data persistence  
✅ Settings page updates MongoDB  

---

## 🎉 You're All Set!

Just run:
```cmd
mvnw.cmd javafx:run
```

And enjoy your fully functional authentication system with MongoDB! 🚀

---

**Need Help?**
- Check MongoDB is running: `mongosh`
- View saved users: `db.users.find().pretty()`
- Check logs in console for detailed error messages

