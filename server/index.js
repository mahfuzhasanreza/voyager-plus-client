// Minimal Express server for Notepad endpoints
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { MongoClient, ObjectId, ServerApiVersion } = require('mongodb');

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json());

// Mongo connection
const MONGO_URI = process.env.MONGO_URI || process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017';
const DB_NAME = process.env.DB_NAME || 'voyagerPlus';

const client = new MongoClient(MONGO_URI, {
  serverApi: {
    version: ServerApiVersion.v1,
    strict: true,
    deprecationErrors: true,
  },
});

let notesCol;

async function init() {
  await client.connect();
  const db = client.db(DB_NAME);
  notesCol = db.collection('notepads');
  await db.command({ ping: 1 });
  console.log(`✅ Connected to MongoDB at ${MONGO_URI}, DB=${DB_NAME}`);
}

// Health check
app.get('/', (_req, res) => {
  res.send('Voyager+ Notepad API is running');
});

// Create a new note
app.post('/notepad', async (req, res) => {
  try {
    const { username, title, content } = req.body || {};
    if (!username || !title) {
      return res.status(400).send({ message: 'Username and title are required' });
    }
    const doc = {
      username,
      title,
      content: content || '',
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    const result = await notesCol.insertOne(doc);
    return res.status(201).send({ insertedId: result.insertedId });
  } catch (err) {
    console.error('❌ POST /notepad error:', err);
    return res.status(500).send({ message: 'Server error' });
  }
});

// Get all notes for a user
app.get('/notepad/:username', async (req, res) => {
  try {
    const { username } = req.params;
    const notes = await notesCol
      .find({ username })
      .sort({ updatedAt: -1 })
      .toArray();
    return res.send(notes);
  } catch (err) {
    console.error('❌ GET /notepad/:username error:', err);
    return res.status(500).send({ message: 'Server error' });
  }
});

// Update a note by id
app.put('/notepad/:noteId', async (req, res) => {
  try {
    const { noteId } = req.params;
    const { title, content } = req.body || {};
    const update = {
      ...(typeof title === 'string' ? { title } : {}),
      ...(typeof content === 'string' ? { content } : {}),
      updatedAt: new Date(),
    };
    const result = await notesCol.updateOne(
      { _id: new ObjectId(noteId) },
      { $set: update }
    );
    if (result.matchedCount === 0) {
      return res.status(404).send({ message: 'Note not found' });
    }
    return res.send({ message: 'Note updated' });
  } catch (err) {
    console.error('❌ PUT /notepad/:noteId error:', err);
    return res.status(500).send({ message: 'Server error' });
  }
});

// Delete a note by id
app.delete('/notepad/:noteId', async (req, res) => {
  try {
    const { noteId } = req.params;
    const result = await notesCol.deleteOne({ _id: new ObjectId(noteId) });
    if (result.deletedCount === 0) {
      return res.status(404).send({ message: 'Note not found' });
    }
    return res.send({ message: 'Note deleted' });
  } catch (err) {
    console.error('❌ DELETE /notepad/:noteId error:', err);
    return res.status(500).send({ message: 'Server error' });
  }
});

init()
  .then(() => {
    app.listen(PORT, () => console.log(`🚀 Notepad API listening on http://localhost:${PORT}`));
  })
  .catch((err) => {
    console.error('❌ Failed to initialize server:', err);
    process.exit(1);
  });

process.on('SIGINT', async () => {
  try {
    await client.close();
    console.log('✅ MongoDB connection closed');
  } finally {
    process.exit(0);
  }
});

