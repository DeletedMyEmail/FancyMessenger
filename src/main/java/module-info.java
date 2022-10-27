module clientside.kmesrework {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;
    requires javafx.swing;
    requires KLibrary.stable;


    opens client to javafx.fxml;
    exports client;
    exports server;
    opens server to javafx.fxml;
}