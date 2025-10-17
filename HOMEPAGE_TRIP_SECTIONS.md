# Homepage Trip Sections Feature

## Overview
This feature displays trip posts in two distinct sections on the homepage/news feed:
1. **Solo Travel Adventures** - Shows all solo trip posts
2. **Group Adventures** - Shows all group trip posts

## Implementation Details

### Backend Changes (Node.js Server)

#### New API Endpoints
Located in: `voyager-plus-server/index.js`

1. **GET /trips/solo** - Fetches all solo trip posts
   ```javascript
   GET http://localhost:5000/trips/solo
   ```

2. **GET /trips/group** - Fetches all group trip posts
   ```javascript
   GET http://localhost:5000/trips/group
   ```

3. **GET /trips** - Fetches all trips (existing endpoint)
   ```javascript
   GET http://localhost:5000/trips
   ```

### Frontend Changes (JavaFX)

#### TripService.java
Added new methods to filter trip posts by type:

- `getSoloTripPosts()` - Returns list of solo trip posts sorted by date
- `getGroupTripPosts()` - Returns list of group trip posts sorted by date
- `getNewsFeed()` - Returns all trip posts (existing method)

#### NewsFeedController.java
Major updates to display trips in sections:

1. **New Method: `loadNewsFeedWithSections()`**
   - Replaces the old `loadNewsFeed()` method
   - Fetches solo and group trips separately
   - Creates distinct visual sections for each trip type
   - Shows empty state if no trips exist

2. **New Method: `createTripSection()`**
   - Creates a visually appealing section with:
     - Gradient header with custom accent color
     - Section title and subtitle
     - Post count
     - Container for trip cards
   - Solo section uses purple gradient (#667eea)
   - Group section uses violet gradient (#764ba2)

## Features

### Visual Design
- **Gradient Headers**: Each section has a beautiful gradient background
  - Solo trips: Blue-purple gradient
  - Group trips: Purple-violet gradient
- **Section Information**: Shows title, description, and post count
- **Spacing**: Clean separation between sections
- **Responsive**: Adapts to content size

### User Experience
1. When user logs in or visits homepage, they see:
   - Welcome banner with statistics
   - Create post section
   - **Solo Travel Adventures section** (if any solo trips exist)
   - **Group Adventures section** (if any group trips exist)

2. Each section displays:
   - Section header with emoji icon
   - Descriptive subtitle
   - Number of posts
   - All trip cards for that type

3. Each trip card shows:
   - Author information with avatar
   - Post content
   - Trip details (title, route, date, budget)
   - Member count (for group trips)
   - Like, comment, and share stats
   - Action buttons (Like, Comment, View, Join for groups)

### Empty State
If no trips exist, users see:
- Large airplane emoji
- "No trips yet!" message
- "Create Your First Trip" button

## How It Works

1. **On Page Load**:
   ```java
   initialize() → loadNewsFeedWithSections()
   ```

2. **Fetch Data**:
   ```java
   List<TripPost> soloTrips = tripService.getSoloTripPosts();
   List<TripPost> groupTrips = tripService.getGroupTripPosts();
   ```

3. **Display Sections**:
   - If solo trips exist → Create solo section
   - Add spacer between sections
   - If group trips exist → Create group section
   - If both empty → Show empty state

4. **Refresh**:
   - User can click refresh button to reload feed
   - After creating a new trip, feed automatically refreshes

## Database Integration

The backend server connects to MongoDB and filters trips by type:
- Solo trips: `{ type: 'SOLO' }`
- Group trips: `{ type: 'GROUP' }`

All trips are stored in the `trips` collection with the following structure:
```javascript
{
  id: "uuid",
  title: "Trip Title",
  type: "SOLO" | "GROUP",
  route: "City A → City B",
  date: ISODate,
  budget: Number,
  description: "Trip description",
  creatorUsername: "username",
  status: "POSTED",
  approvedMembers: ["username"],
  joinRequests: []
}
```

## Testing

### Test Solo Trips Section
1. Create a solo trip post
2. Navigate to homepage
3. Verify it appears in "Solo Travel Adventures" section

### Test Group Trips Section
1. Create a group trip post
2. Navigate to homepage
3. Verify it appears in "Group Adventures" section

### Test Both Sections
1. Create both solo and group trips
2. Navigate to homepage
3. Verify both sections appear with correct trips
4. Verify proper spacing between sections

### Test Empty State
1. Start with no trips in database
2. Navigate to homepage
3. Verify empty state appears with create button

## Benefits

1. **Better Organization**: Users can easily distinguish between solo and group trips
2. **Visual Appeal**: Gradient sections with distinct colors make the UI more attractive
3. **User-Friendly**: Clear categorization helps users find trips they're interested in
4. **Scalable**: Easy to add more sections in the future (e.g., Popular, Trending)

## Future Enhancements

Possible additions:
- Filter buttons to show/hide specific sections
- Sort options (by date, popularity, budget)
- Search within sections
- Pagination for large datasets
- "Featured" or "Popular" sections
- Location-based sections

