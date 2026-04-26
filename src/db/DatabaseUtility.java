package db;

import java.sql.*;

public class DatabaseUtility {

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException ex) {
            System.out.println("Error: unable to load driver class!");
            System.exit(1);
        }
        return connection;
    }
}
