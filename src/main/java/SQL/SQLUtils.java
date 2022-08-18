package SQL;

import java.sql.*;

/**
 * This class provides methods for secure SQL statements (preventing SQL-injection)<br>
 * A part of the KLibrary (https://github.com/KaitoKunTatsu/KLibrary)
 *
 * @version 11.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class SQLUtils {

    private final Connection con;
    private PreparedStatement stmt;

    public SQLUtils(String pDBPath) throws SQLException {
        con = DriverManager.getConnection("jdbc:sqlite:"+pDBPath);
        con.setAutoCommit(false);
    }

    public ResultSet onQuery(String pStatement, String[] set) throws SQLException {
        stmt = con.prepareStatement(pStatement);
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

    public ResultSet onQuery(String pStatement) throws SQLException {
        return con.createStatement().executeQuery(pStatement);
    }

    public void onExecute(String pStatement, String[] set) throws SQLException {
        stmt = con.prepareStatement(pStatement);
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

    public void onExecute(String pStatement) throws SQLException {
        con.createStatement().execute(pStatement);
        con.commit();
    }
}
