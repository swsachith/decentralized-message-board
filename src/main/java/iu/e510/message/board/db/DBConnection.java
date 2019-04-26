package iu.e510.message.board.db;

import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static Connection dbConnection;

    private DBConnection() {
    }

    public static synchronized Connection getDbConnection(String nodeID) throws ClassNotFoundException, SQLException {
        if (dbConnection == null) {
            Config config = new Config();
            String DB_DRIVER = config.getConfig(Constants.DB_DRIVER);
            String DB_USER = config.getConfig(Constants.DB_USER);
            String DB_PASSWORD = config.getConfig(Constants.DB_PASSWORD);
            String DB_CONN_URL = config.getConfig(Constants.DB_CONNECTION_PREFIX) + "/" + nodeID;

            Class.forName(DB_DRIVER);
            dbConnection = DriverManager.getConnection(DB_CONN_URL, DB_USER, DB_PASSWORD);
            // clean the database at startup
            Statement statement = dbConnection.createStatement();
            statement.execute(Constants.CLEAN_DB_COMMAND);
        }
        return dbConnection;
    }
}