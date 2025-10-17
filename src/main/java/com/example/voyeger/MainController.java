package com.example.voyeger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    private TripService tripService;

    @FXML
    public void initialize() {
        tripService = TripService.getInstance();

        // Load news feed by default
        loadNewsFeed();
    }

    private void loadNewsFeed() {
        loadPage("NewsFeed.fxml");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();
            mainContainer.setCenter(page);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load page: " + fxmlFile);
        }
    }
}
