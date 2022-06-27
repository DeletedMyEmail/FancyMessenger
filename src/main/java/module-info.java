module clientside.kmesrework {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens clientside to javafx.fxml;
    exports clientside;
    exports ServerSide;
    opens ServerSide to javafx.fxml;
}