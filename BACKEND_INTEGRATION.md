# 🚀 Backend API Integration Guide

## ✅ What's Done

I've updated the Java application to work with your Node.js backend at `http://localhost:5000`

### Updated Files:
1. **DatabaseManager.java** - Now uses HTTP requests to your API
2. **pom.xml** - Removed MongoDB direct connection, kept BCrypt for password hashing

---

## 📡 Required Backend Endpoints

You currently have:
```javascript
app.post('/user', async (req, res) => {
  const user = req.body;
  console.log(user);
  const result = await usersCollection.insertOne(user);
  res.send(result);
});
```

### ✅ This endpoint works perfectly for signup!

### Additional Endpoints Needed for Login:

Add these to your Node.js backend:

```javascript
// Get user by username or email (for login)
app.get('/user', async (req, res) => {
  try {
    const identifier = req.query.identifier;
    
    // Search by username or email
    const user = await usersCollection.findOne({
      $or: [
        { username: identifier },
        { email: identifier }
      ]
    });
    
    if (!user) {
      return res.status(404).send({ message: 'User not found' });
    }
    
    res.send(user);
  } catch (error) {
    res.status(500).send({ message: 'Server error' });
  }
});

// Update user profile (for settings page)
app.put('/user/update', async (req, res) => {
  try {
    const { username, displayName, email, bio, profilePicturePath, coverPhotoPath } = req.body;
    
    const result = await usersCollection.updateOne(
      { username: username },
      { 
        $set: { 
          displayName, 
          email, 
          bio, 
          profilePicturePath, 
          coverPhotoPath 
        } 
      }
    );
    
    res.send(result);
  } catch (error) {
    res.status(500).send({ message: 'Update failed' });
  }
});
```

---

## 🎯 How the Signup Works

### When user clicks "Create Account":

1. **Java app collects data** from the form:
   - Full Name
   - Username
   - Email
   - Password

2. **Password is hashed** using BCrypt (secure!)
   ```java
   String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
   ```

3. **JSON is created** with all user data:
   ```json
   {
     "username": "johndoe",
     "email": "john@example.com",
     "password": "$2a$10$...", 
     "fullName": "John Doe",
     "displayName": "John Doe",
     "bio": "",
     "profilePicturePath": "/default-avatar.png",
     "coverPhotoPath": "/default-cover.jpg",
     "createdAt": "2025-10-17T10:30:00",
     "lastLogin": "2025-10-17T10:30:00"
   }
   ```

4. **HTTP POST request** sent to:
   ```
   POST http://localhost:5000/user
   Content-Type: application/json
   ```

5. **Your backend receives** the data in `req.body`

6. **MongoDB stores** the user with `usersCollection.insertOne(user)`

7. **Success response** returned to Java app

8. **User sees** success message and redirects to login!

---

## 🧪 Testing the Integration

### Step 1: Make sure your Node.js backend is running
```bash
node server.js
# Should show: Server running on port 5000
```

### Step 2: Run the Java application
```bash
cd D:\VoyagerPlus\VoyagerPlus
mvnw.cmd clean compile
mvnw.cmd javafx:run
```

### Step 3: Test Signup
1. Click **"Create Account"**
2. Fill in the form:
   - Full Name: Test User
   - Username: testuser
   - Email: test@test.com
   - Password: test123
   - Confirm: test123
3. Click **"Create Account"**
4. Check your Node.js console - you should see the user data logged!
5. Check MongoDB - the user should be inserted!

---

## 📊 What Gets Sent to Your Backend

### Signup POST Request:
```json
{
  "username": "testuser",
  "email": "test@test.com",
  "password": "$2a$10$eHmH9j8K...", // BCrypt hashed
  "fullName": "Test User",
  "displayName": "Test User",
  "bio": "",
  "profilePicturePath": "/default-avatar.png",
  "coverPhotoPath": "/default-cover.jpg",
  "createdAt": "2025-10-17T14:25:30.123456",
  "lastLogin": "2025-10-17T14:25:30.123456"
}
```

---

## 🔐 Security Features

1. **Password Hashing**:
   - Passwords are hashed in Java BEFORE sending to backend
   - Uses BCrypt with automatic salt generation
   - Never sends plain text passwords

2. **HTTP Communication**:
   - Uses Java's built-in HttpClient
   - Proper Content-Type headers
   - Error handling for network issues

3. **Validation**:
   - All form fields validated before sending
   - Username format check
   - Email format check
   - Password length check
   - Password confirmation match

---

## 🎨 The Signup Form

Your signup form includes:

### Form Fields:
- ✅ **Full Name** - User's real name
- ✅ **Username** - Unique identifier (3+ chars, alphanumeric + underscore)
- ✅ **Email** - Valid email format required
- ✅ **Password** - Minimum 6 characters
- ✅ **Confirm Password** - Must match password

### Validation Rules:
- All fields required
- Username: minimum 3 characters, only letters, numbers, underscore
- Email: must contain @ and valid format
- Password: minimum 6 characters
- Passwords must match

### Visual Features:
- Beautiful split-screen design
- Left panel: Purple gradient with branding
- Right panel: Clean white form
- Loading spinner during submission
- Color-coded status messages (green for success, red for errors)
- Smooth transitions and hover effects

---

## 🔧 Console Output

When you run the app, you'll see:

```
✅ Connected to backend server: http://localhost:5000
```

When signup happens:
```
Server response code: 200
Server response body: {"acknowledged":true,"insertedId":"..."}
✅ User registered successfully: testuser
```

---

## 🚨 Troubleshooting

### Error: Connection refused
**Problem**: Backend not running  
**Solution**: Start your Node.js server first
```bash
node server.js
```

### Error: 404 Not Found
**Problem**: Endpoint doesn't match  
**Solution**: Make sure your endpoint is `POST /user` (not `/users`)

### Backend receives but doesn't insert
**Problem**: MongoDB connection issue in backend  
**Solution**: Check your MongoDB connection string in Node.js

### Password verification fails on login
**Problem**: Need to add the GET /user endpoint  
**Solution**: Add the login endpoint I provided above

---

## ✅ Quick Checklist

- [x] DatabaseManager updated to use HTTP API
- [x] POST request sends to http://localhost:5000/user
- [x] Password hashed with BCrypt before sending
- [x] JSON formatted correctly for your API
- [x] Form validation working
- [x] Success/error handling
- [x] Console logging for debugging

---

## 🎉 You're Ready!

1. Start your Node.js backend: `node server.js`
2. Run Java app: `mvnw.cmd javafx:run`
3. Click "Create Account"
4. Fill form and submit
5. Watch user get inserted into MongoDB!

The signup form will properly insert data into your database via the `/user` endpoint! 🚀

