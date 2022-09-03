module clientside.kmesrework {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;


    opens client to javafx.fxml;
    exports client;
    exports server;
    opens server to javafx.fxml;
}