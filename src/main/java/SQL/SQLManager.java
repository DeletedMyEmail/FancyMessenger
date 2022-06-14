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

    protected void print_db() throws SQLException {
        ResultSet rs = onQuery("SELECT Username FROM User", null);
        while (rs.next())
        {
            System.out.println(rs.getString(1));
        }
    }

    public static void main(String[] args) {
        try {
            SQLManager sqlm = new SQLManager("C:\\Users\\derdi\\Documents\\Dev\\KMesRework\\src\\main\\java\\SQL\\kmes.db");
            sqlm.print_db();
        } catch (Exception ex) { System.out.println(ex.toString());}
    }
}

