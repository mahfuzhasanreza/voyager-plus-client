module com.example.voyeger {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.voyeger to javafx.fxml;
    exports com.example.voyeger;
}