package com.ru.mag.db.jdbc.util;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;

public class DBUtil {

    private Connection cachedConnection = null;
    private static final DBUtil instance = new DBUtil();

    private DBUtil() { }

    public static DBUtil getInstance() {
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (cachedConnection == null || cachedConnection.isClosed() || !cachedConnection.isValid(5)) {

                Class.forName("oracle.jdbc.OracleDriver");

                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "system";
                String pass = "oracle";

                cachedConnection = DriverManager.getConnection(url, user, pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedConnection;
    }


    public ResultSet getAllFromTable(String tableName) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName.toUpperCase());

                RowSetFactory factory = RowSetProvider.newFactory();
                CachedRowSet crs = factory.createCachedRowSet();
                crs.populate(rs);

                stmt.close();
                return crs;
            }
        } catch (SQLException e) {
            System.err.println("Грешка при SELECT: " + e.getMessage());
        }
        return null;
    }
}