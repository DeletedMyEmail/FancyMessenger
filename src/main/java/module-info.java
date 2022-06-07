module com.example.healthtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.example.healthtracker to javafx.fxml;
    exports com.example.healthtracker;
}