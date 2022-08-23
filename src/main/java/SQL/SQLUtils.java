package SQL;

import java.sql.*;
import java.util.Arrays;

/**
 * This class provides methods for secure SQL statements (preventing SQL-injection)<br>
 * A part of the KLibrary (https://github.com/KaitoKunTatsu/KLibrary)
 *
 * @version	v1.0.0 | last edit: 19.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 */
public class SQLUtils {

    private final Connection con;
    private PreparedStatement stmt;

    public SQLUtils(String pDBPath) throws SQLException {
        con = DriverManager.getConnection("jdbc:sqlite:"+pDBPath);
        con.setAutoCommit(false);
    }

    /**
     * @param pStatement SQL statement
     * @param pSet each ? in pStatement will be replaced with the content of this array
     * @return a {@link ResultSet} containing the result of your SQL statement
     * */
    public ResultSet onQuery(String pStatement, Object... pSet) throws SQLException {
        stmt = con.prepareStatement(pStatement);
        if (pSet != null)
        {
            for (int i = 0; i < pSet.length; i++) {
                if (Blob.class.equals(pSet[i].getClass())) stmt.setBlob(i + 1, (Blob) pSet[i]);
                else if (byte[].class.equals(pSet[i].getClass())) stmt.setBytes(i + 1, (byte[]) pSet[i]);
                else if (byte.class.equals(pSet[i].getClass())) stmt.setByte(i + 1, (byte) pSet[i]);
                else if (String.class.equals(pSet[i].getClass())) stmt.setString(i + 1, (String) pSet[i]);
                else if (Integer.class.equals(pSet[i].getClass())) stmt.setInt(i + 1, (Integer) pSet[i]);
                else if (Boolean.class.equals(pSet[i].getClass())) stmt.setBoolean(i + 1, (Boolean) pSet[i]);
                else if (Double.class.equals(pSet[i].getClass())) stmt.setDouble(i + 1, (Double) pSet[i]);
                else if (java.sql.Date.class.equals(pSet[i].getClass())) stmt.setDate(i + 1, (java.sql.Date) pSet[i]);
            }
        }
        ResultSet rs = stmt.executeQuery();
        stmt.clearParameters();
        return rs;
    }

    /**
     * @param pStatement SQL statement
     * @return a {@link ResultSet} containing the result of your SQL statement
     * */
    public ResultSet onQuery(String pStatement) throws SQLException {
        return con.createStatement().executeQuery(pStatement);
    }

    /**
     * @param pStatement SQL statement you want to execute
     * @param pSet each ? will be replaced with the content of this array
     * */
    public void onExecute(String pStatement, Object... pSet) throws SQLException {
        stmt = con.prepareStatement(pStatement);
        if (pSet != null)
        {
            for (int i=0; i < pSet.length; i++)
            {
                if (Blob.class.equals(pSet[i].getClass())) stmt.setBlob(i + 1, (Blob) pSet[i]);
                else if (byte[].class.equals(pSet[i].getClass())) stmt.setBytes(i + 1, (byte[]) pSet[i]);
                else if (byte.class.equals(pSet[i].getClass())) stmt.setByte(i + 1, (byte) pSet[i]);
                else if (String.class.equals(pSet[i].getClass())) stmt.setString(i + 1, (String) pSet[i]);
                else if (Integer.class.equals(pSet[i].getClass())) stmt.setInt(i + 1, (Integer) pSet[i]);
                else if (Boolean.class.equals(pSet[i].getClass())) stmt.setBoolean(i + 1, (Boolean) pSet[i]);
                else if (Double.class.equals(pSet[i].getClass())) stmt.setDouble(i + 1, (Double) pSet[i]);
                else if (java.sql.Date.class.equals(pSet[i].getClass())) stmt.setDate(i + 1, (java.sql.Date) pSet[i]);
            }
        }
        stmt.execute();
        con.commit();
        stmt.clearParameters();
    }

    /**
     * @param pStatement SQL statement you want to execute
     * */
    public void onExecute(String pStatement) throws SQLException {
        con.createStatement().execute(pStatement);
        con.commit();
    }
/*
    public static void main(String[] args) throws SQLException {
        SQLUtils sut = new SQLUtils("src/main/java/SQL/kmes.db");
        sut.onExecute("INSERT INTO User VALUES(?,?,?)", new Object[] {
                "daddada", 1234, new byte[] {'2','2'}
        });
        ResultSet rs = sut.onQuery("SELECT * FROM User");
        while(rs.next())
        {
            System.out.println(rs.getString(1));
            System.out.println(rs.getString(2));
            System.out.println(Arrays.toString(rs.getBytes(3)));
        }
    }*/
}
