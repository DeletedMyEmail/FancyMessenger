module com.example.healthtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens de.gui to javafx.fxml;
    exports de.gui;
}