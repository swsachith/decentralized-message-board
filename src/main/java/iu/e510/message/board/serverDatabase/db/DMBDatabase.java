package iu.e510.message.board.serverDatabase.db;



import iu.e510.message.board.serverDatabase.ds.DMBPosts;
import org.apache.logging.log4j.core.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DMBDatabase {

    private static final String DMB_DATABASE_FILE = "jdbc:sqlite:decentralizedMessageBoard.db";
    /*table for storing posts*/
    private static final String DMB_POSTS_TABLE = "dmb_posts";

    private static final String DMB_POST_ID_COLUMN = "post_id";
    private static final String DMB_POST_CATEGORY_COLUMN = "post_category";
    private static final String DMB_POST_TITLE_COLUMN = "post_title";
    private static final String DMB_POST_OWNER_COLUMN = "post_owner";
    private static final String DMB_POST_DESCRIPTION_COLUMN = "post_description";
    private static final String DMB_POST_CREATED_COLUMN = "post_created";
    private static final String DMB_POST_UPVOTES_COLUMN = "post_upvotes";
    private static final String DMB_POST_DOWNVOTES_COLUMN = "post_downvotes";

    /*table for storing replies (single level replies)*/
    public static final String DMB_REPLIES_TABLE = "dmb_replies";

    private static final String DMB_REPLY_ID_COLUMN = "reply_id";
    private static final String DMB_REPLY_POST_ID_COLUMN = "post_id";
    private static final String DMB_REPLY_OWNER_COLUMN = "reply_owner";
    private static final String DMB_REPLY_DESCRIPTION_COLUMN = "reply_description";
    private static final String DMB_REPLY_CREATED_COLUMN = "reply_created";
    private static final String DMB_REPLY_UPVOTES_COLUMN = "reply_upvotes";
    private static final String DMB_REPLY_DOWNVOTES_COLUMN = "reply_downvotes";


    /*table for storing replies*/

    
    /**
     * create a new database if not exists
     */
    public static void createNewDatabase() {

        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * create the tables if they don't exist
     */
    public static void createTables() {
        String createPostsTableQuery = "CREATE TABLE IF NOT EXISTS " +
                DMB_POSTS_TABLE + " (" +
                DMB_POST_ID_COLUMN          + " integer not null constraint posts_pk primary key," +
                DMB_POST_CATEGORY_COLUMN    + " text not null ," +
                DMB_POST_TITLE_COLUMN       + " text not null ," +
                DMB_POST_OWNER_COLUMN       + " text not null ," +
                DMB_POST_DESCRIPTION_COLUMN + " text not null ," +
                DMB_POST_CREATED_COLUMN     + " real not null," +
                DMB_POST_UPVOTES_COLUMN     + " integer default 0," +
                DMB_POST_DOWNVOTES_COLUMN   + " integer default 0" +
                ");" +
                "create unique index posts_post_id_uindex" +
                " on " +
                DMB_POSTS_TABLE + " (" + DMB_POST_ID_COLUMN + ");";

        String createRepliesTableQuery = "CREATE TABLE IF NOT EXISTS " +
                DMB_REPLIES_TABLE + " (" +
                DMB_REPLY_ID_COLUMN          + " integer not null constraint posts_pk primary key," +
                DMB_POST_ID_COLUMN          + " integer not null," +
                DMB_REPLY_OWNER_COLUMN       + " text not null ," +
                DMB_POST_DESCRIPTION_COLUMN + " text not null ," +
                DMB_POST_CREATED_COLUMN     + " real not null," +
                DMB_REPLY_UPVOTES_COLUMN     + " integer default 0," +
                DMB_POST_DOWNVOTES_COLUMN   + " integer default 0" +
                ");" +
                "create unique index posts_post_id_uindex" +
                " on " +
                DMB_REPLIES_TABLE + " (" + DMB_REPLY_ID_COLUMN + ");";


        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(createPostsTableQuery);
            System.out.println("posts table created");
            stmt.execute(createRepliesTableQuery);
            System.out.println("replies table created");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * create an connection to the database
     * @return
     */
    private static Connection getConnection(){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DMB_DATABASE_FILE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * get all posts as an array list from the database
     */
    public static ArrayList<DMBPosts> getAllPostsDataArrayList(){
        Connection connection = null;
        try {

            connection = getConnection();

            String selectAllPostsQuery = "SELECT * FROM " + DMB_POSTS_TABLE;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectAllPostsQuery);
            ArrayList<DMBPosts> dmbPostsArrayList = new ArrayList<>();
            while (resultSet.next()){

                DMBPosts postObject = new DMBPosts();
                postObject.setPostId(resultSet.getInt(DMB_POST_ID_COLUMN));
                postObject.setPostOwnerId(resultSet.getString(DMB_POST_OWNER_COLUMN));
                postObject.setPostTitle(resultSet.getString(DMB_POST_TITLE_COLUMN));
                postObject.setPostDescription(resultSet.getString(DMB_POST_DESCRIPTION_COLUMN));
                postObject.setPostTimeStamp(resultSet.getTimestamp(DMB_POST_CREATED_COLUMN).getTime());
                postObject.setPostUpvotes(resultSet.getInt(DMB_POST_UPVOTES_COLUMN));
                postObject.setPostDownvotes(resultSet.getInt(DMB_POST_DOWNVOTES_COLUMN));

                dmbPostsArrayList.add(postObject);
            }

            return dmbPostsArrayList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get all posts as a byte array from the database
     */
    public static byte[] getAllPostsDataByteArray(){
        Connection connection = null;
        try {

            connection = getConnection();

            String selectAllPostsQuery = "SELECT * FROM " + DMB_POSTS_TABLE;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectAllPostsQuery);
            JSONArray dmbPostsJsonArray = new JSONArray();
            while (resultSet.next()) {

                JSONObject postObject = new JSONObject();
                postObject.put(DMB_POST_ID_COLUMN, resultSet.getInt(DMB_POST_ID_COLUMN));
                postObject.put(DMB_POST_CATEGORY_COLUMN, resultSet.getString(DMB_POST_CATEGORY_COLUMN));
                postObject.put(DMB_POST_OWNER_COLUMN, resultSet.getString(DMB_POST_OWNER_COLUMN));
                postObject.put(DMB_POST_TITLE_COLUMN, resultSet.getString(DMB_POST_TITLE_COLUMN));
                postObject.put(DMB_POST_DESCRIPTION_COLUMN, resultSet.getString(DMB_POST_DESCRIPTION_COLUMN));
                postObject.put(DMB_POST_CREATED_COLUMN, resultSet.getTimestamp(DMB_POST_CREATED_COLUMN).getTime());
                postObject.put(DMB_POST_UPVOTES_COLUMN, resultSet.getInt(DMB_POST_UPVOTES_COLUMN));
                postObject.put(DMB_POST_DOWNVOTES_COLUMN, resultSet.getInt(DMB_POST_DOWNVOTES_COLUMN));

                dmbPostsJsonArray.put(postObject);
            }
//            jsonArray.toString().getBytes();
            return dmbPostsJsonArray.toString().getBytes();
        } catch (JSONException | SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * put all posts from a byte array into the database
     */
    public static void addAllPostsDataByteArray(byte [] postByteArray){
        Connection connection = null;
        try {

            connection = getConnection();
            String recordsJsonString = new String(postByteArray, StandardCharsets.UTF_8);
            JSONArray jsonPostsArray = new JSONArray(recordsJsonString);
            String sql = "INSERT INTO " + DMB_POSTS_TABLE + " (" +
                    DMB_POST_ID_COLUMN       + ", " +
                    DMB_POST_TITLE_COLUMN       + ", " +
                    DMB_POST_CATEGORY_COLUMN    + ", " +
                    DMB_POST_OWNER_COLUMN       + ", " +
                    DMB_POST_DESCRIPTION_COLUMN + ", " +
                    DMB_POST_UPVOTES_COLUMN + ", " +
                    DMB_POST_DOWNVOTES_COLUMN + ", " +
                    DMB_POST_CREATED_COLUMN     + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

            for (int i = 0; i < jsonPostsArray.length(); i++){

                JSONObject jsonPostObject = jsonPostsArray.getJSONObject(i);

                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, jsonPostObject.getInt(DMB_POST_ID_COLUMN));
                pstmt.setString(2, jsonPostObject.getString(DMB_POST_TITLE_COLUMN));
                pstmt.setString(3, jsonPostObject.getString(DMB_POST_CATEGORY_COLUMN));
                pstmt.setString(4, jsonPostObject.getString(DMB_POST_OWNER_COLUMN));
                pstmt.setString(5, jsonPostObject.getString(DMB_POST_DESCRIPTION_COLUMN));
                pstmt.setTimestamp(6, new Timestamp(jsonPostObject.getLong(DMB_POST_CREATED_COLUMN)));
                pstmt.setInt(7, jsonPostObject.getInt(DMB_POST_UPVOTES_COLUMN));
                pstmt.setInt(8, jsonPostObject.getInt(DMB_POST_DOWNVOTES_COLUMN));
                pstmt.executeUpdate();
                System.out.println("post data added");
            }

        } catch (JSONException | SQLException e){
            e.printStackTrace();
        }
    }


    /**
     * put a single posts into the database
     */
    public static void addPostData(String pTitle, String pCategory, String pOwner, String pDescription){
        Connection connection = null;
        try{
            connection = getConnection();

            String sql = "INSERT INTO " + DMB_POSTS_TABLE + " (" +
                    DMB_POST_TITLE_COLUMN       + ", " +
                    DMB_POST_CATEGORY_COLUMN    + ", " +
                    DMB_POST_OWNER_COLUMN       + ", " +
                    DMB_POST_DESCRIPTION_COLUMN + ", " +
                    DMB_POST_CREATED_COLUMN     + ") VALUES(?, ?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, pTitle);
            pstmt.setString(2, pCategory);
            pstmt.setString(3, pOwner);
            pstmt.setString(4, pDescription);
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            System.out.println("post data added");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * drop the tables if exist
     */
    public static void deleteTables(){
        String deletePostsTableQuery = "DROP TABLE IF EXISTS " +
                DMB_POSTS_TABLE + ";";

        String createRepliesTableQuery = "DROP TABLE IF EXISTS " +
                DMB_REPLIES_TABLE + ";";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(deletePostsTableQuery);
            System.out.println("posts table deleted");
            stmt.execute(createRepliesTableQuery);
            System.out.println("replies table deleted");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * remove a single post record from the table
     * @param pId
     */
    public static void removePostData(int pId){
        Connection connection = null;


        String sql = "DELETE FROM " + DMB_POSTS_TABLE + " WHERE " + DMB_POST_ID_COLUMN + " = ?";

        try {

            connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);

            // set the corresponding param
            pstmt.setInt(1, pId);
            // execute the delete statement
            pstmt.executeUpdate();
            System.out.println("post data deleted");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

}
