package com.example.voyeger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotepadController {
    @FXML private ListView<Pair<String, String>> notesListView;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;

    private final ObservableList<Pair<String, String>> notes = FXCollections.observableArrayList();
    private String selectedNoteId = null;
    private String username = "demoUser"; // default fallback

    @FXML
    public void initialize() {
        // Use logged-in user if available
        TripService ts = TripService.getInstance();
        if (ts != null && ts.getCurrentUser() != null && ts.getCurrentUser().getUsername() != null) {
            username = ts.getCurrentUser().getUsername();
        }

        notesListView.setItems(notes);
        notesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getValue());
            }
        });
        notesListView.setOnMouseClicked(this::onNoteSelected);
        fetchNotes();
    }

    private void fetchNotes() {
        notes.clear();
        try {
            URL url = new URL("http://localhost:5000/notepad/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String json = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
                for (SimpleNote n : parseNotes(json)) {
                    notes.add(new Pair<>(n.id, n.title));
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to fetch notes: " + e.getMessage());
        }
    }

    @FXML
    private void onNoteSelected(MouseEvent event) {
        Pair<String, String> selected = notesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedNoteId = selected.getKey();
            fetchNoteContent(selectedNoteId);
        }
    }

    private void fetchNoteContent(String noteId) {
        try {
            URL url = new URL("http://localhost:5000/notepad/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String json = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
                for (SimpleNote n : parseNotes(json)) {
                    if (n.id.equals(noteId)) {
                        titleField.setText(n.title);
                        contentArea.setText(n.content);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to fetch note content: " + e.getMessage());
        }
    }

    @FXML
    private void onNewNote(ActionEvent event) {
        selectedNoteId = null;
        titleField.clear();
        contentArea.clear();
        notesListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSaveNote(ActionEvent event) {
        String title = titleField.getText();
        String content = contentArea.getText();
        if (title == null || title.isEmpty()) {
            showAlert("Validation", "Title cannot be empty.");
            return;
        }
        try {
            URL url = new URL("http://localhost:5000/notepad");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String json = String.format("{\"username\":\"%s\",\"title\":\"%s\",\"content\":\"%s\"}", username, escapeJson(title), escapeJson(content));
            conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                fetchNotes();
                showAlert("Success", "Note saved.");
            } else {
                showAlert("Error", "Failed to save note. HTTP " + responseCode);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to save note: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateNote(ActionEvent event) {
        if (selectedNoteId == null) {
            showAlert("Validation", "Select a note to update.");
            return;
        }
        String title = titleField.getText();
        String content = contentArea.getText();
        try {
            URL url = new URL("http://localhost:5000/notepad/" + selectedNoteId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String json = String.format("{\"title\":\"%s\",\"content\":\"%s\"}", escapeJson(title), escapeJson(content));
            conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                fetchNotes();
                showAlert("Success", "Note updated.");
            } else {
                showAlert("Error", "Failed to update note. HTTP " + responseCode);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to update note: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteNote(ActionEvent event) {
        Pair<String, String> selected = notesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "Select a note to delete.");
            return;
        }
        try {
            URL url = new URL("http://localhost:5000/notepad/" + selected.getKey());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                fetchNotes();
                titleField.clear();
                contentArea.clear();
                showAlert("Success", "Note deleted.");
            } else {
                showAlert("Error", "Failed to delete note. HTTP " + responseCode);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to delete note: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // Minimal JSON parsing helpers (avoid external libs)
    private static class SimpleNote {
        final String id; final String title; final String content;
        SimpleNote(String id, String title, String content) { this.id = id; this.title = title; this.content = content; }
    }

    private List<SimpleNote> parseNotes(String jsonArray) {
        List<SimpleNote> list = new ArrayList<>();
        if (jsonArray == null || jsonArray.isEmpty()) return list;
        // Match objects { ... } in a top-level array
        Matcher m = Pattern.compile("\\{[^}]*} ").matcher(jsonArray + " ");
        int lastEnd = 0;
        while (m.find(lastEnd)) {
            String obj = m.group().trim();
            String id = extractJsonString(obj, "_id");
            if (id == null) id = extractJsonString(obj, "id");
            String title = extractJsonString(obj, "title");
            String content = extractJsonString(obj, "content");
            list.add(new SimpleNote(id != null ? id : "", title != null ? title : "", content != null ? content : ""));
            lastEnd = m.end();
        }
        return list;
    }

    private String extractJsonString(String json, String key) {
        // Matches "key":"value" where value may contain escaped quotes
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:[^\\\\\\\"]|\\\\.)*)\\\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return unescapeJson(m.group(1));
        }
        return null;
    }

    private String unescapeJson(String s) {
        return s.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
    }
}
