package it.polimi.progettotiw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.UnavailableException;

/**
 * This class is used to handle the setup with the database
 */
public class ConnectionHandler {

    public static Connection getConnection(ServletContext context) throws UnavailableException {
        Connection connection;
        try {
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Couldn't get db connection");
        }
        return connection;
    }

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

}
