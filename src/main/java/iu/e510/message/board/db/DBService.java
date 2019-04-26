package iu.e510.message.board.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBService {
    private static Logger logger = LoggerFactory.getLogger(DBService.class);

    private String nodeID;

    public DBService(String nodeID) {
        this.nodeID = nodeID;
    }

    public Connection getConnection() {
        try {
            return DBConnection.getDbConnection(nodeID);
        } catch (ClassNotFoundException e) {
            logger.error("Could not load the class for the database! ", e);
        } catch (SQLException e) {
            logger.error("Error connecting to the database!", e);
        }
        return null;
    }
}
