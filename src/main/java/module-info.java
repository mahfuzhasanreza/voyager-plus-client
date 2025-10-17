module com.example.voyeger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    opens com.example.voyeger to javafx.fxml;
    exports com.example.voyeger;
}

