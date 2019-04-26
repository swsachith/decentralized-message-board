package iu.e510.message.board.db;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDBService {
    @Test
    public void testInit() throws SQLException {
        DBService dbService = new DBService("temp023");
        String tempTable = "TEMP_TABLE";
        Connection connection = dbService.getConnection();
        String createTempTable = "CREATE TABLE IF NOT EXISTS " +
                tempTable + " (" +
                "id" + " INT default 0" +
                ");";
        String insertquery = "INSERT INTO " + tempTable + " (id) VALUES(1);";
        String selectQuery = "SELECT * FROM " + tempTable + ";";

        Statement stmt = connection.createStatement();
        stmt.execute(createTempTable);
        stmt.execute(insertquery);
        ResultSet rs = stmt.executeQuery(selectQuery);
        rs.next();
        Assert.assertEquals(rs.getString("id"), "1");
    }
}
