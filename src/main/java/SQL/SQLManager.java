package SQL;

import java.sql.*;

public class SQLManager {

    private Connection con;
    private PreparedStatement stmt;

    public SQLManager(String db_name) throws SQLException, ClassNotFoundException {
        con = DriverManager.getConnection("jdbc:sqlite:"+db_name);
    }

    public ResultSet onQuery(String statement, String[] set) throws SQLException {
        stmt = con.prepareStatement(statement);
        if (set != null)
        {
            for (int i = 0; i < set.length; i++) {
                stmt.setString(i + 1, set[i]);
            }
        }
        ResultSet rs = stmt.executeQuery();
        stmt.clearParameters();
        return rs;
    }

    public void onExecute(String statement, String[] set) throws SQLException {
        stmt = con.prepareStatement(statement);
        if (set != null)
        {
            for (int i=0; i < set.length; i++)
            {
                stmt.setString(i+1, set[i]);
            }
        }
        stmt.execute();
        con.commit();
        stmt.clearParameters();
    }

    public boolean check_login(String username, String hashed_password)
    {
        try
        {
            ResultSet rs = onQuery("SELECT username FROM User WHERE username=? AND password_hash=?",
                    new String[]{ username, hashed_password});

            return !(rs.isClosed() || rs.getString(1) == "");

        } catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        SQLManager sqlmnanager = new SQLManager("src/main/java/SQL/kmes.db");
        System.out.println(sqlmnanager.onQuery("SELECT username FROM User WHERE username=? AND password_hash=?",
                new String[]{ "Admin", "fddsnfse"}).getString(1));
    }
}

