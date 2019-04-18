package iu.e510.message.board.dbserver.db;


import iu.e510.message.board.dbserver.DMBReply;
import iu.e510.message.board.dbserver.dbinterface.DMBDatabase;
import iu.e510.message.board.dbserver.model.DMBPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;

public class DMBDatabaseImpl implements DMBDatabase {
    private static Logger logger = LoggerFactory.getLogger(DMBDatabaseImpl.class);

//    private static final String DMB_DATABASE_FILE = "jdbc:sqlite:decentralizedMessageBoard.db";
//    private static final String DMB_DATABASE_FILE = "jdbc:sqlite:${catalina.home}/dbs/decentralizedMessageBoard.db";

    /*table for storing posts*/
    private static final String DMB_POSTS_TABLE = "dmb_posts";

    private static final String DMB_POST_ID_COLUMN = "post_id";
    //    private static final String DMB_POST_TOPIC_ID_COLUMN = "post_topic_id";
    private static final String DMB_POST_TOPIC_COLUMN = "post_topic";
    private static final String DMB_POST_TITLE_COLUMN = "post_title";
    private static final String DMB_POST_OWNER_COLUMN = "post_owner";
    private static final String DMB_POST_DESCRIPTION_COLUMN = "post_description";
    private static final String DMB_POST_CREATED_COLUMN = "post_created";
    private static final String DMB_POST_UPVOTES_COLUMN = "post_upvotes";
    private static final String DMB_POST_DOWNVOTES_COLUMN = "post_downvotes";

    /*table for storing replies (single level replies)*/
    public static final String DMB_REPLIES_TABLE = "dmb_replies";

    private static final String DMB_REPLY_ID_COLUMN = "reply_id";
    private static final String DMB_REPLY_POST_FK_COLUMN = "post_id_fk";
    private static final String DMB_REPLY_OWNER_COLUMN = "reply_owner";
    private static final String DMB_REPLY_DESCRIPTION_COLUMN = "reply_description";
    private static final String DMB_REPLY_CREATED_COLUMN = "reply_created";
    private static final String DMB_REPLY_UPVOTES_COLUMN = "reply_upvotes";
    private static final String DMB_REPLY_DOWNVOTES_COLUMN = "reply_downvotes";


    /*table for storing feedback*/
    public static final String DMB_FEEDBACK_TABLE = "dmb_feedback";

    private static final String DMB_FEEDBACK_FK_COLUMN = "reply_id_fk";
    private static final String DMB_FEEDBACK_POST_FK_COLUMN = "post_id_fk";
    private static final String DMB_FEEDBACK_OWNER_COLUMN = "reply_owner";
    private static final String DMB_FEEDBACK_DESCRIPTION_COLUMN = "reply_description";
    private static final String DMB_FEEDBACK_CREATED_COLUMN = "reply_created";
    private static final String DMB_FEEDBACK_UPVOTES_COLUMN = "reply_upvotes";
    private static final String DMB_FEEDBACK_DOWNVOTES_COLUMN = "reply_downvotes";



    /**
     * constructor for the database class, create database and tables
     */
    public DMBDatabaseImpl() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        createNewDatabase();
        createTables();
    }

    /**
     * create a new database if not exists
     */
    public void createNewDatabase() {

        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("The driver name is " + meta.getDriverName());
                logger.info("A new database has been created.");
            }

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }


    /**
     * create the tables if they don't exist
     */
    public void createTables() {

        String createPostsTableQuery = "CREATE TABLE IF NOT EXISTS " +
                DMB_POSTS_TABLE + " (" +
                DMB_POST_ID_COLUMN + " integer not null constraint posts_pk primary key," +
//                DMB_POST_TOPIC_ID_COLUMN + " integer not null, " +
                DMB_POST_TOPIC_COLUMN + " text not null ," +
                DMB_POST_TITLE_COLUMN + " text not null ," +
                DMB_POST_OWNER_COLUMN + " text not null ," +
                DMB_POST_DESCRIPTION_COLUMN + " text not null ," +
                DMB_POST_CREATED_COLUMN + " real not null," +
                DMB_POST_UPVOTES_COLUMN + " integer default 0," +
                DMB_POST_DOWNVOTES_COLUMN + " integer default 0" +
                ");" +
                "create unique index post_id_uidx" +
                " on " +
                DMB_POSTS_TABLE + " (" + DMB_POST_ID_COLUMN + ");" +
                "create index post_topic_id_idx" +
                " on " +
                DMB_POSTS_TABLE + " (" + DMB_POST_TOPIC_COLUMN + ");";

        String createRepliesTableQuery = "CREATE TABLE IF NOT EXISTS " +
                DMB_REPLIES_TABLE + " (" +
                DMB_REPLY_ID_COLUMN          + " integer not null constraint posts_pk primary key," +
                DMB_REPLY_OWNER_COLUMN       + " text not null ," +
                DMB_REPLY_DESCRIPTION_COLUMN + " text not null ," +
                DMB_REPLY_CREATED_COLUMN     + " real not null," +
                DMB_REPLY_UPVOTES_COLUMN    + " integer default 0," +
                DMB_REPLY_DOWNVOTES_COLUMN   + " integer default 0," +
                DMB_REPLY_POST_FK_COLUMN     + " integer not null," +
                " foreign key (" + DMB_REPLY_POST_FK_COLUMN + ") references "
                + DMB_POSTS_TABLE + "(" + DMB_POST_ID_COLUMN + ")" +
                ");" +
                "create unique index posts_post_id_uindex" +
                " on " +
                DMB_REPLIES_TABLE + " (" + DMB_REPLY_ID_COLUMN + ");";


        try (Connection conn = getConnection();

        Statement stmt = conn.createStatement()){
            // create a new table
            stmt.execute(createPostsTableQuery);
            logger.info("posts table created");
            stmt.execute(createRepliesTableQuery);
            logger.info("replies table created");
        } catch(SQLException e){
            logger.info(e.getMessage());
        }
    }

    /**
     * create an connection to the database
     *
     * @return Connection object
     */
    private Connection getConnection() {
        Context ctx = null;
        DataSource ds = null;
        try {
            ctx = new InitialContext();
            // looks in the context.xml file and gets the properties based on key
            ds = (DataSource)ctx.lookup("java:comp/env/jdbc/decentralizedMessageBoard");
        } catch (NamingException e) {
            e.printStackTrace();
        }


        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
//            connection = DriverManager.getConnection(DMB_DATABASE_FILE);
            //will use the url present in the context.xml file to generate connection
            connection = ds.getConnection();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("Connection obj:" + connection);
        return connection;
    }

    /**
     * get all posts as an array list from the database
     */
    @Override
    public ArrayList<DMBPost> getAllPostsDataArrayList() {
        Connection connection = null;
        try {

            connection = getConnection();

            String selectAllPostsQuery = "SELECT * FROM " + DMB_POSTS_TABLE;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectAllPostsQuery);
            ArrayList<DMBPost> dmbPostsArrayList = new ArrayList<>();
            while (resultSet.next()) {
                DMBPost postObject = new DMBPost();
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
     * get all posts as an array list from the database
     */
    @Override
    public ArrayList<DMBPost> getAllPostsDataByTopicArrayList(String pTopic) {
        Connection connection = null;
        try {

            connection = getConnection();

            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_POSTS_TABLE +
                    " WHERE " + DMB_POST_TOPIC_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            statement.setString(1, pTopic);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<DMBPost> dmbPostsArrayList = new ArrayList<>();
            while (resultSet.next()) {
                DMBPost postObject = new DMBPost();
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
    @Override
    public byte[] getAllPostsDataByteArray() {
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
                postObject.put(DMB_POST_TOPIC_COLUMN, resultSet.getString(DMB_POST_TOPIC_COLUMN));
                postObject.put(DMB_POST_OWNER_COLUMN, resultSet.getString(DMB_POST_OWNER_COLUMN));
                postObject.put(DMB_POST_TITLE_COLUMN, resultSet.getString(DMB_POST_TITLE_COLUMN));
                postObject.put(DMB_POST_DESCRIPTION_COLUMN, resultSet.getString(DMB_POST_DESCRIPTION_COLUMN));
                postObject.put(DMB_POST_CREATED_COLUMN, resultSet.getTimestamp(DMB_POST_CREATED_COLUMN).getTime());
                postObject.put(DMB_POST_UPVOTES_COLUMN, resultSet.getInt(DMB_POST_UPVOTES_COLUMN));
                postObject.put(DMB_POST_DOWNVOTES_COLUMN, resultSet.getInt(DMB_POST_DOWNVOTES_COLUMN));

                dmbPostsJsonArray.put(postObject);
            }
            return dmbPostsJsonArray.toString().getBytes();
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getAllPostsDataByTopicByteArray(String pTopic) {
        Connection connection = null;
        try {

            connection = getConnection();

            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_POSTS_TABLE +
                    " WHERE " + DMB_POST_TOPIC_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            ResultSet resultSet = statement.executeQuery();
            JSONArray dmbPostsJsonArray = new JSONArray();
            while (resultSet.next()) {

                JSONObject postObject = new JSONObject();
                postObject.put(DMB_POST_ID_COLUMN, resultSet.getInt(DMB_POST_ID_COLUMN));
                postObject.put(DMB_POST_TOPIC_COLUMN, resultSet.getString(DMB_POST_TOPIC_COLUMN));
                postObject.put(DMB_POST_OWNER_COLUMN, resultSet.getString(DMB_POST_OWNER_COLUMN));
                postObject.put(DMB_POST_TITLE_COLUMN, resultSet.getString(DMB_POST_TITLE_COLUMN));
                postObject.put(DMB_POST_DESCRIPTION_COLUMN, resultSet.getString(DMB_POST_DESCRIPTION_COLUMN));
                postObject.put(DMB_POST_CREATED_COLUMN, resultSet.getTimestamp(DMB_POST_CREATED_COLUMN).getTime());
                postObject.put(DMB_POST_UPVOTES_COLUMN, resultSet.getInt(DMB_POST_UPVOTES_COLUMN));
                postObject.put(DMB_POST_DOWNVOTES_COLUMN, resultSet.getInt(DMB_POST_DOWNVOTES_COLUMN));

                dmbPostsJsonArray.put(postObject);
            }
            return dmbPostsJsonArray.toString().getBytes();
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * put all posts from a byte array into the database
     */
    @Override
    public void addAllPostsDataByteArray(byte[] postByteArray) {
        Connection connection = null;
        try {

            connection = getConnection();
            String recordsJsonString = new String(postByteArray, StandardCharsets.UTF_8);
            JSONArray jsonPostsArray = new JSONArray(recordsJsonString);
            String sql = "INSERT INTO " + DMB_POSTS_TABLE + " (" +
                    DMB_POST_ID_COLUMN + ", " +
                    DMB_POST_TITLE_COLUMN + ", " +
                    DMB_POST_TOPIC_COLUMN + ", " +
                    DMB_POST_OWNER_COLUMN + ", " +
                    DMB_POST_DESCRIPTION_COLUMN + ", " +
                    DMB_POST_UPVOTES_COLUMN + ", " +
                    DMB_POST_DOWNVOTES_COLUMN + ", " +
                    DMB_POST_CREATED_COLUMN + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

            for (int i = 0; i < jsonPostsArray.length(); i++) {

                JSONObject jsonPostObject = jsonPostsArray.getJSONObject(i);

                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, jsonPostObject.getInt(DMB_POST_ID_COLUMN));
                pstmt.setString(2, jsonPostObject.getString(DMB_POST_TITLE_COLUMN));
                pstmt.setString(3, jsonPostObject.getString(DMB_POST_TOPIC_COLUMN));
                pstmt.setString(4, jsonPostObject.getString(DMB_POST_OWNER_COLUMN));
                pstmt.setString(5, jsonPostObject.getString(DMB_POST_DESCRIPTION_COLUMN));
                pstmt.setTimestamp(6, new Timestamp(jsonPostObject.getLong(DMB_POST_CREATED_COLUMN)));
                pstmt.setInt(7, jsonPostObject.getInt(DMB_POST_UPVOTES_COLUMN));
                pstmt.setInt(8, jsonPostObject.getInt(DMB_POST_DOWNVOTES_COLUMN));
                pstmt.executeUpdate();
                logger.info("post data added");
            }

        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * put a single post record into the database
     */
    @Override
    public void addPostData(String pTitle, String pTopic, String pOwner, String pDescription) {
        Connection connection = null;
        try {
            connection = getConnection();

            String sql = "INSERT INTO " + DMB_POSTS_TABLE + " (" +
                    DMB_POST_TITLE_COLUMN + ", " +
                    DMB_POST_TOPIC_COLUMN + ", " +
                    DMB_POST_OWNER_COLUMN + ", " +
                    DMB_POST_DESCRIPTION_COLUMN + ", " +
                    DMB_POST_CREATED_COLUMN + ") VALUES(?, ?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, pTitle);
            pstmt.setString(2, pTopic);
            pstmt.setString(3, pOwner);
            pstmt.setString(4, pDescription);
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            logger.info("post data added");
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * remove a single post record from the table
     *
     * @param pId
     */
    @Override
    public void removePostData(int pId, String pOwner) {
        Connection connection = null;


        String postsSql = "DELETE FROM " + DMB_POSTS_TABLE +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?" +
                " AND " + DMB_POST_OWNER_COLUMN + " = ?";

        String repliesSql = "DELETE FROM " + DMB_REPLIES_TABLE +
                " WHERE " + DMB_REPLY_POST_FK_COLUMN + " = ?";

        try {

            connection = getConnection();
            PreparedStatement postsStmt = connection.prepareStatement(postsSql);

            // set the corresponding param
            postsStmt.setInt(1, pId);
            postsStmt.setString(2, pOwner);
            // execute the delete statement
            postsStmt.executeUpdate();
            logger.info("posts table deleted");

            PreparedStatement repliesStmt = connection.prepareStatement(repliesSql);

            // set the corresponding param
            repliesStmt.setInt(1, pId);
            // execute the delete statement
            repliesStmt.executeUpdate();
            logger.info("replies table deleted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * drop the tables if exist
     */
    @Override
    public void deleteTables() {
        String deletePostsTableQuery = "DROP TABLE IF EXISTS " +
                DMB_POSTS_TABLE + ";";

        String deleteRepliesTableQuery = "DROP TABLE IF EXISTS " +
                DMB_REPLIES_TABLE + ";";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(deletePostsTableQuery);
            logger.info("posts table deleted");
            stmt.execute(deleteRepliesTableQuery);
            logger.info("replies table deleted");
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void upVotePost(int pId, String pOwner) {
        Connection connection = null;
        String postsSql = "UPDATE " + DMB_POSTS_TABLE +
                " SET " + DMB_POST_UPVOTES_COLUMN + " = " + DMB_POST_UPVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?";
        try {

            connection = getConnection();
            PreparedStatement postsStmt = connection.prepareStatement(postsSql);

            // set the corresponding param
            postsStmt.setInt(1, pId);
            // execute the delete statement
            postsStmt.executeUpdate();
            logger.info("post up voted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void downVotePost(int pId, String pOwner) {
        Connection connection = null;
        String postsSql = "UPDATE " + DMB_POSTS_TABLE +
                " SET " + DMB_POST_DOWNVOTES_COLUMN + " = " + DMB_POST_DOWNVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?";
        try {

            connection = getConnection();
            PreparedStatement postsStmt = connection.prepareStatement(postsSql);

            // set the corresponding param
            postsStmt.setInt(1, pId);
            // execute the delete statement
            postsStmt.executeUpdate();
            logger.info("post down voted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void addReplyData(int pId, String rOwner, String rDescription) {
        Connection connection = null;
        try {
            connection = getConnection();

            String sql = "INSERT INTO " + DMB_REPLIES_TABLE + " (" +
                    DMB_REPLY_POST_FK_COLUMN + ", " +
                    DMB_REPLY_OWNER_COLUMN + ", " +
                    DMB_REPLY_DESCRIPTION_COLUMN + ", " +
                    DMB_REPLY_CREATED_COLUMN + ") VALUES( ?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, pId);
            pstmt.setString(2, rOwner);
            pstmt.setString(3, rDescription);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            logger.info("reply data added");
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public ArrayList<DMBReply> getAllRepliesToPost(int pId) {
        Connection connection = null;
        try {

            connection = getConnection();

            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_REPLIES_TABLE +
                    " WHERE " + DMB_REPLY_POST_FK_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            statement.setInt(1, pId);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<DMBReply> dmbPostsArrayList = new ArrayList<>();
            while (resultSet.next()) {
                DMBReply postObject = new DMBReply();
                postObject.setReplyId(resultSet.getInt(DMB_REPLY_ID_COLUMN));
                postObject.setReplyOwner(resultSet.getString(DMB_REPLY_OWNER_COLUMN));
                postObject.setPostId(resultSet.getInt(DMB_REPLY_POST_FK_COLUMN));
                postObject.setReplyDescription(resultSet.getString(DMB_POST_DESCRIPTION_COLUMN));
                postObject.setReplyTimeStamp(resultSet.getTimestamp(DMB_POST_CREATED_COLUMN).getTime());
                postObject.setReplyUpVotes(resultSet.getInt(DMB_POST_UPVOTES_COLUMN));
                postObject.setReplyDownVotes(resultSet.getInt(DMB_POST_DOWNVOTES_COLUMN));

                dmbPostsArrayList.add(postObject);
            }

            return dmbPostsArrayList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void upVoteReply(int rId, String rOwner) {
        Connection connection = null;
        String postsSql = "UPDATE " + DMB_REPLIES_TABLE +
                " SET " + DMB_REPLY_UPVOTES_COLUMN + " = " + DMB_REPLY_UPVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_REPLY_ID_COLUMN + " = ?";
        try {

            connection = getConnection();
            PreparedStatement postsStmt = connection.prepareStatement(postsSql);

            // set the corresponding param
            postsStmt.setInt(1, rId);
            // execute the delete statement
            postsStmt.executeUpdate();
            logger.info("reply up voted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void downVoteReply(int rId, String rOwner) {
        Connection connection = null;
        String postsSql = "UPDATE " + DMB_REPLIES_TABLE +
                " SET " + DMB_REPLY_DOWNVOTES_COLUMN + " = " + DMB_REPLY_DOWNVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_REPLY_ID_COLUMN + " = ?";
        try {

            connection = getConnection();
            PreparedStatement postsStmt = connection.prepareStatement(postsSql);

            // set the corresponding param
            postsStmt.setInt(1, rId);
            // execute the delete statement
            postsStmt.executeUpdate();
            logger.info("reply down voted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }
}
