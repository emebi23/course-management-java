package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single place to manage the database connection.
 */
public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/CMS";
    private static final String USER = "root";
    private static final String PASS = "pass123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
