package iu.e510.message.board.serverDatabase.db;



import iu.e510.message.board.serverDatabase.ds.DMBPosts;

import java.sql.*;
import java.util.ArrayList;

public class DMBDatabase {

    private static final String DMB_DATABASE_FILE = "jdbc:sqlite:decentralizedMessageBoard.db";
    /*table for storing posts*/
    private static final String DMB_POSTS_TABLE = "dmb_posts";
    private static final String DMB_POST_ID_COLUMN = "post_id";
    private static final String DMB_POST_TITLE_COLUMN = "post_title";
    private static final String DMB_POST_OWNER_COLUMN = "post_owner";
    private static final String DMB_POST_DESCRIPTION_COLUMN = "post_description";
    private static final String DMB_POST_CREATED_COLUMN = "post_created";
    private static final String DMB_POST_UPVOTES_COLUMN = "post_upvotes";
    private static final String DMB_POST_DOWNVOTES_COLUMN = "post_downvotes";
    /*table for storing replies*/
//    public static final String DMB_REPLIES_TABLE = "dmb_replies";
    
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

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(createPostsTableQuery);
            System.out.println("posts table created");
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
     * get all posts from the database
     */
    public static ArrayList<DMBPosts> selectPostsData(){
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

    public static void addPostData(String pTitle, String pOwner, String pDescription){
        Connection connection = null;
        try{
            connection = getConnection();

            String sql = "INSERT INTO " + DMB_POSTS_TABLE + " (" +
                    DMB_POST_TITLE_COLUMN + ", " +
                    DMB_POST_OWNER_COLUMN + ", " +
                    DMB_POST_DESCRIPTION_COLUMN +", " +
                    DMB_POST_CREATED_COLUMN + ") VALUES(?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, pTitle);
            pstmt.setString(2, pOwner);
            pstmt.setString(3, pDescription);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            System.out.println("post data added");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

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
