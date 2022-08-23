module clientside.kmesrework {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens client to javafx.fxml;
    exports client;
    exports server;
    opens server to javafx.fxml;
}