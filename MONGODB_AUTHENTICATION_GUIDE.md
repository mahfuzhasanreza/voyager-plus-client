# 🎉 Voyager+ Authentication System with MongoDB

## ✅ What Has Been Implemented

### 1. **Attractive Welcome/Landing Page**
- **Beautiful gradient background** with travel-themed imagery
- **Animated title and subtitle** (fade-in and slide-up effects)
- **Two prominent buttons**: "Create Account" and "Sign In"
- **Feature highlights** showcasing the platform benefits
- **Professional design** with modern styling

### 2. **Sign Up Page**
- **Split-screen design**:
  - Left panel: Gradient branding with benefits list
  - Right panel: Registration form
- **Form fields**:
  - Full Name
  - Username (validated: 3+ chars, alphanumeric + underscore)
  - Email (validated format)
  - Password (minimum 6 characters)
  - Confirm Password (match validation)
- **Real-time validation** with error messages
- **Loading indicator** during registration
- **Success feedback** with auto-redirect to Sign In
- **Data saved to MongoDB** with encrypted passwords

### 3. **Sign In Page**
- **Matching split-screen design** for consistency
- **Login with username OR email**
- **Password verification** using BCrypt
- **Remember Me** checkbox
- **Forgot Password** link (ready for implementation)
- **Auto-redirect to main app** after successful login

### 4. **MongoDB Integration**
- **DatabaseManager class** with singleton pattern
- **User registration** with duplicate checking
- **Password hashing** using BCrypt for security
- **User authentication** and login
- **Profile updates** integrated
- **Connection management** (auto-close on app exit)

---

## 🗄️ MongoDB Setup Instructions

### Step 1: Install MongoDB
1. Download MongoDB Community Server from: https://www.mongodb.com/try/download/community
2. Install with default settings
3. MongoDB will run on `localhost:27017` by default

### Step 2: Start MongoDB Service
**Windows:**
```bash
# MongoDB should start automatically after installation
# Or manually start the service:
net start MongoDB
```

**Check if MongoDB is running:**
```bash
# Open Command Prompt and type:
mongosh
# If it connects, MongoDB is running!
```

### Step 3: Create Database (Optional - Auto-created)
The application will automatically create:
- Database: `voyagerplus`
- Collection: `users`

You can verify it by running:
```bash
mongosh
use voyagerplus
db.users.find()
```

---

## 🚀 Running the Application

### Compile with MongoDB Dependencies
```bash
cd D:\VoyagerPlus\VoyagerPlus
mvnw.cmd clean compile
```

### Run the Application
```bash
mvnw.cmd javafx:run
```

---

## 🎨 User Flow

### First Time User Journey:
1. **Welcome Page** appears with beautiful background
2. Click **"Create Account"** button
3. Fill in registration form:
   - Full Name: "John Doe"
   - Username: "johndoe"
   - Email: "john@example.com"
   - Password: "******"
   - Confirm Password: "******"
4. Click **"Create Account"**
5. Loading spinner appears
6. ✅ Success message: "Registration successful! Redirecting..."
7. **Auto-redirected to Sign In page** after 2 seconds
8. Enter credentials and click **"Sign In"**
9. ✅ Success: "Login successful! Welcome John Doe"
10. **Redirected to main application** (News Feed)

### Returning User Journey:
1. **Welcome Page** appears
2. Click **"Sign In"** button
3. Enter username/email and password
4. Click **"Sign In"**
5. **Instantly redirected to main app**

---

## 🔐 Security Features

1. **Password Hashing**: BCrypt with salt (industry standard)
2. **Duplicate Prevention**: Checks for existing username/email
3. **Input Validation**: 
   - Username format (alphanumeric + underscore)
   - Email format validation
   - Password strength (6+ characters)
   - Password confirmation match
4. **Secure Storage**: Passwords never stored in plain text
5. **Session Management**: User object maintained in TripService

---

## 📊 MongoDB Schema

### Users Collection:
```json
{
  "_id": ObjectId("..."),
  "username": "johndoe",
  "email": "john@example.com",
  "password": "$2a$10$...", // BCrypt hashed
  "fullName": "John Doe",
  "displayName": "John Doe",
  "bio": "",
  "profilePicturePath": "/default-avatar.png",
  "coverPhotoPath": "/default-cover.jpg",
  "createdAt": "2025-10-17T10:30:00",
  "lastLogin": "2025-10-17T10:35:00"
}
```

---

## 🎨 Design Features

### Welcome Page:
- **Full-screen gradient background** (purple to violet)
- **80px airplane emoji** as logo
- **72px bold title** "Voyager+"
- **28px subtitle** with tagline
- **Feature bullets** in white text
- **Prominent action buttons** with hover effects

### Auth Pages:
- **Split-screen layout** (400px left panel, rest for form)
- **Gradient left panel** with brand identity
- **Clean white form area** with ample spacing
- **Styled input fields** with focus effects
- **Primary button** in brand purple (#667eea)
- **Loading indicator** during async operations
- **Status messages** color-coded (green/red)

---

## 📝 Code Structure

### Controllers:
- `WelcomeController.java` - Landing page with animations
- `SignUpController.java` - Registration logic + validation
- `SignInController.java` - Authentication logic
- `DatabaseManager.java` - MongoDB operations

### FXML Views:
- `Welcome.fxml` - Attractive landing page
- `SignUp.fxml` - Registration form
- `SignIn.fxml` - Login form

### Stylesheets:
- `Welcome.css` - Landing page styling
- `Auth.css` - Sign up/in page styling

---

## 🧪 Testing the System

### Test Registration:
1. Run the app
2. Click "Create Account"
3. Enter details:
   - Full Name: Test User
   - Username: testuser
   - Email: test@test.com
   - Password: test123
   - Confirm: test123
4. Check MongoDB:
```bash
mongosh
use voyagerplus
db.users.find().pretty()
```

### Test Login:
1. Use the credentials you just created
2. Login with either username or email
3. Verify you're redirected to main app

### Test Validation:
- Try duplicate username → Error message
- Try duplicate email → Error message
- Try short password → Error message
- Try mismatched passwords → Error message
- Try invalid email → Error message

---

## 🔧 Configuration

### Change MongoDB Connection:
Edit `DatabaseManager.java` line 19:
```java
mongoClient = MongoClients.create("mongodb://localhost:27017");
// Change to your MongoDB URI if different
```

### Change Database Name:
Edit `DatabaseManager.java` line 20:
```java
database = mongoClient.getDatabase("voyagerplus");
// Change "voyagerplus" to your preferred name
```

---

## 🎯 Key Features Summary

✅ **Attractive landing page** with travel theme  
✅ **Smooth animations** (fade-in, slide-up)  
✅ **Sign Up form** with full validation  
✅ **Sign In page** with username/email login  
✅ **MongoDB integration** with auto-connection  
✅ **BCrypt password hashing** for security  
✅ **Duplicate checking** (username & email)  
✅ **Real-time feedback** with status messages  
✅ **Loading indicators** during async operations  
✅ **Auto-navigation** after success  
✅ **Professional design** matching Facebook style  
✅ **Responsive layout** with split-screen design  

---

## 🚨 Troubleshooting

### MongoDB Connection Error:
```
❌ Failed to connect to MongoDB: Connection refused
```
**Solution**: Make sure MongoDB service is running
```bash
net start MongoDB
```

### Registration Fails:
- Check MongoDB is running
- Verify database name is correct
- Check console for detailed error messages

### Login Fails:
- Verify user was registered successfully
- Check password is correct (case-sensitive)
- Try using email instead of username (or vice versa)

---

## 📈 Next Steps

The authentication system is fully functional and integrated with:
- News Feed
- Profile Page
- Settings Page (updates MongoDB)
- Trip Management

All user data persists in MongoDB and survives app restarts! 🎉

---

**Ready to use!** Just make sure MongoDB is running and compile with Maven.

