module com.example.healthtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens de.clientside to javafx.fxml;
    exports de.clientside;
    exports ServerSide;
    opens ServerSide to javafx.fxml;
}