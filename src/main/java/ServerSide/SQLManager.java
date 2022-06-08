package ServerSide;

import DataStructures.DBMS.DatabaseConnector;

import java.sql.*;

class SQLManager {

    private final DatabaseConnector connector;

    private Connection con;
    private PreparedStatement stmt;

    protected SQLManager() throws SQLException {
        con = DriverManager.getConnection("kmes.db");
        connector = new DatabaseConnector("", 0, "kmes.db", "", "");

    }

    protected ResultSet onQuery(String statement, String[] set) throws SQLException {
        stmt = con.prepareStatement(statement);
        for (int i=0; i < set.length; i++)
        {
            stmt.setString(i+1, set[i]);
        }
        ResultSet rs = stmt.executeQuery();
        stmt.clearParameters();
        return rs;
    }

    protected void onExecute(String statement, String[] set) throws SQLException {
        stmt = con.prepareStatement(statement);
        for (int i=0; i < set.length; i++)
        {
            stmt.setString(i+1, set[i]);
        }
        stmt.execute();
        stmt.clearParameters();
    }
}

