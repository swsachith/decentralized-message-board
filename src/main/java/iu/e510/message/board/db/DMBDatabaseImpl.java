package iu.e510.message.board.db;


import iu.e510.message.board.db.model.DMBReply;
import iu.e510.message.board.db.model.DMBPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static iu.e510.message.board.db.DBConstants.*;

public class DMBDatabaseImpl implements DMBDatabase {
    private static Logger logger = LoggerFactory.getLogger(DMBDatabaseImpl.class);


    private Connection connection;

    /**
     * constructor for the database class, create database and tables
     */
    public DMBDatabaseImpl(String nodeID) {
        DBService dbService = new DBService(nodeID);
        connection = dbService.getConnection();
        createTables();
    }


    /**
     * create the tables if they don't exist
     */
    public void createTables() {

        String createPostsTableQuery = "CREATE TABLE IF NOT EXISTS " +
                DMB_POSTS_TABLE + " (" +
                DMB_POST_ID_COLUMN + " integer not null primary key," +
//                DMB_POST_TOPIC_ID_COLUMN + " integer not null, " +
                DMB_POST_TOPIC_COLUMN + " text not null ," +
                DMB_POST_TITLE_COLUMN + " text not null ," +
                DMB_POST_OWNER_COLUMN + " text not null ," +
                DMB_POST_DESCRIPTION_COLUMN + " text not null ," +
                DMB_POST_CREATED_COLUMN + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                DMB_POST_UPVOTES_COLUMN + " integer default 0," +
                DMB_POST_DOWNVOTES_COLUMN + " integer default 0" +
                ");";

        String createRepliesTableQuery = "CREATE TABLE IF NOT EXISTS " +
                DMB_REPLIES_TABLE + " (" +
                DMB_REPLY_ID_COLUMN          + " integer not null constraint posts_pk primary key," +
                DMB_REPLY_OWNER_COLUMN       + " text not null ," +
                DMB_REPLY_DESCRIPTION_COLUMN + " text not null ," +
                DMB_REPLY_CREATED_COLUMN     + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                DMB_REPLY_UPVOTES_COLUMN    + " integer default 0," +
                DMB_REPLY_DOWNVOTES_COLUMN   + " integer default 0," +
                DMB_REPLY_POST_FK_COLUMN     + " integer not null," +
                " foreign key (" + DMB_REPLY_POST_FK_COLUMN + ") references "
                + DMB_POSTS_TABLE + "(" + DMB_POST_ID_COLUMN + ")" +
                ");" +
                "create unique index posts_post_id_uindex" +
                " on " +
                DMB_REPLIES_TABLE + " (" + DMB_REPLY_ID_COLUMN + ");";


        try (Statement stmt = connection.createStatement()){
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
     * get all posts as an array list from the database
     */
    @Override
    public ArrayList<DMBPost> getPostsDataArrayList() {
        try {
            String selectAllPostsQuery = "SELECT * FROM " + DMB_POSTS_TABLE;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectAllPostsQuery);
            ArrayList<DMBPost> dmbPostsArrayList = new ArrayList<>();
            while (resultSet.next()) {
                DMBPost postObject = getPostFromRS(resultSet);
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
        try {

            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_POSTS_TABLE +
                    " WHERE " + DMB_POST_TOPIC_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            statement.setString(1, pTopic);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<DMBPost> dmbPostsArrayList = new ArrayList<>();
            while (resultSet.next()) {
                DMBPost postObject = getPostFromRS(resultSet);
                dmbPostsArrayList.add(postObject);
            }

            return dmbPostsArrayList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DMBPost getPostFromRS(ResultSet resultSet) throws SQLException {
        DMBPost postObject = new DMBPost();
        postObject.setPostId(resultSet.getInt(DMB_POST_ID_COLUMN));
        postObject.setPostOwnerId(resultSet.getString(DMB_POST_OWNER_COLUMN));
        postObject.setPostTitle(resultSet.getString(DMB_POST_TITLE_COLUMN));
        postObject.setPostDescription(resultSet.getString(DMB_POST_DESCRIPTION_COLUMN));
        postObject.setPostTimeStamp(resultSet.getTimestamp(DMB_POST_CREATED_COLUMN).getTime());
        postObject.setPostUpvotes(resultSet.getInt(DMB_POST_UPVOTES_COLUMN));
        postObject.setPostDownvotes(resultSet.getInt(DMB_POST_DOWNVOTES_COLUMN));
        postObject.setPostTopic(resultSet.getString(DMB_POST_TOPIC_COLUMN));
        postObject.setPostReplies(getRepliesByPostIdArrayList(resultSet.getInt(DMB_POST_ID_COLUMN)));
        return postObject;
    }


    /**
     * get all posts as a byte array from the database
     */
    @Override
    public byte[] getPostsDataByteArray() {
        try {
            String selectAllPostsQuery = "SELECT * FROM " + DMB_POSTS_TABLE;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectAllPostsQuery);
            return getPostBytesFromRS(resultSet);
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get all posts by topic as a byte array from the database
     * @param pTopic
     * @return
     */
    @Override
    public byte[] getPostsDataByTopicByteArray(String pTopic) {
        try {
            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_POSTS_TABLE +
                    " WHERE " + DMB_POST_TOPIC_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            statement.setString(1, pTopic);
            ResultSet resultSet = statement.executeQuery();
            return getPostBytesFromRS(resultSet);
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void removePostsDataByTopic(String topic) {
        // todo: implement this!
    }

    private byte[] getPostBytesFromRS(ResultSet resultSet) throws SQLException, JSONException {
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
            postObject.put(DMB_REPLIES_LIST_KEY, getRepliesJsonArrayByPostId(resultSet.getInt(DMB_POST_ID_COLUMN)));
            dmbPostsJsonArray.put(postObject);
        }
        return dmbPostsJsonArray.toString().getBytes();
    }

    private JSONArray getRepliesJsonArrayByPostId(int pId){
        try {
            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_REPLIES_TABLE +
                    " WHERE " + DMB_REPLY_POST_FK_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            statement.setInt(1, pId);
            ResultSet resultSet = statement.executeQuery();
            JSONArray repliesByPostIdJsonArray = new JSONArray();
            while (resultSet.next()) {
                JSONObject replyObject = new JSONObject();
                replyObject.put(DMB_REPLY_ID_COLUMN, resultSet.getInt(DMB_REPLY_ID_COLUMN));
                replyObject.put(DMB_REPLY_OWNER_COLUMN, resultSet.getString(DMB_REPLY_OWNER_COLUMN));
                replyObject.put(DMB_REPLY_POST_FK_COLUMN, resultSet.getInt(DMB_REPLY_POST_FK_COLUMN));
                replyObject.put(DMB_REPLY_DESCRIPTION_COLUMN, resultSet.getString(DMB_REPLY_DESCRIPTION_COLUMN));
                replyObject.put(DMB_REPLY_CREATED_COLUMN, resultSet.getTimestamp(DMB_REPLY_CREATED_COLUMN).getTime());
                replyObject.put(DMB_REPLY_UPVOTES_COLUMN, resultSet.getInt(DMB_REPLY_UPVOTES_COLUMN));
                replyObject.put(DMB_REPLY_DOWNVOTES_COLUMN, resultSet.getInt(DMB_REPLY_DOWNVOTES_COLUMN));

                repliesByPostIdJsonArray.put(replyObject);
            }
            return repliesByPostIdJsonArray;
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * put all posts from a byte array into the database
     */
    @Override
    public void addPostsDataFromByteArray(byte[] postByteArray) {
        try {
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
                pstmt.setInt(6, jsonPostObject.getInt(DMB_POST_UPVOTES_COLUMN));
                pstmt.setInt(7, jsonPostObject.getInt(DMB_POST_DOWNVOTES_COLUMN));
                pstmt.setTimestamp(8, new Timestamp(jsonPostObject.getLong(DMB_POST_CREATED_COLUMN)));
                pstmt.executeUpdate();

                JSONArray jsonRepliesArray = jsonPostObject.getJSONArray(DMB_REPLIES_LIST_KEY);
                String repliesSql = "INSERT INTO " + DMB_REPLIES_TABLE + " (" +
                        DMB_REPLY_ID_COLUMN + ", " +
                        DMB_REPLY_POST_FK_COLUMN + ", " +
                        DMB_REPLY_OWNER_COLUMN + ", " +
                        DMB_REPLY_DESCRIPTION_COLUMN + ", " +
                        DMB_REPLY_UPVOTES_COLUMN + ", " +
                        DMB_REPLY_DOWNVOTES_COLUMN + ", " +
                        DMB_REPLY_CREATED_COLUMN + ") VALUES(?, ?, ?, ?, ?, ?, ?)";

                for (int j = 0; j < jsonRepliesArray.length(); j++) {

                    JSONObject jsonReplyObject = jsonRepliesArray.getJSONObject(j);

                    PreparedStatement rstmt = connection.prepareStatement(repliesSql);
                    rstmt.setInt(1, jsonReplyObject.getInt(DMB_REPLY_ID_COLUMN));
                    rstmt.setInt(2, jsonReplyObject.getInt(DMB_REPLY_POST_FK_COLUMN));
                    rstmt.setString(3, jsonReplyObject.getString(DMB_REPLY_OWNER_COLUMN));
                    rstmt.setString(4, jsonReplyObject.getString(DMB_REPLY_DESCRIPTION_COLUMN));
                    rstmt.setInt(5, jsonReplyObject.getInt(DMB_REPLY_UPVOTES_COLUMN));
                    rstmt.setInt(6, jsonReplyObject.getInt(DMB_REPLY_DOWNVOTES_COLUMN));
                    rstmt.setTimestamp(7, new Timestamp(jsonReplyObject.getLong(DMB_REPLY_CREATED_COLUMN)));
                    rstmt.executeUpdate();

                }

                logger.info("posts data added");
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
        int randomID = ThreadLocalRandom.current().nextInt(10000, 1000000);
        try {
            String sql = "INSERT INTO " + DMB_POSTS_TABLE + " (" +
                    DMB_POST_ID_COLUMN + ", " +
                    DMB_POST_TITLE_COLUMN + ", " +
                    DMB_POST_TOPIC_COLUMN + ", " +
                    DMB_POST_OWNER_COLUMN + ", " +
                    DMB_POST_DESCRIPTION_COLUMN + ", " +
                    DMB_POST_CREATED_COLUMN + ") VALUES(?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, randomID);
            pstmt.setString(2, pTitle);
            pstmt.setString(3, pTopic);
            pstmt.setString(4, pOwner);
            pstmt.setString(5, pDescription);
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
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
        String checkIfPostExistsSql = "SELECT count(*) AS rowcount FROM " + DMB_POSTS_TABLE +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?" +
                " AND " + DMB_POST_OWNER_COLUMN + " = ?";

        String repliesSql = "DELETE FROM " + DMB_REPLIES_TABLE +
                " WHERE " + DMB_REPLY_POST_FK_COLUMN + " = ?";

        String postsSql = "DELETE FROM " + DMB_POSTS_TABLE +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?" +
                " AND " + DMB_POST_OWNER_COLUMN + " = ?";

        try {

            PreparedStatement postExistsStmt = connection.prepareStatement(checkIfPostExistsSql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            postExistsStmt.setInt(1, pId);
            postExistsStmt.setString(2, pOwner);

            ResultSet postExistsSet = postExistsStmt.executeQuery();
            postExistsSet.next();
            int rowcount = postExistsSet.getInt("rowcount");
            postExistsSet.close();

            logger.info("rowcount = " + rowcount);

            if (rowcount > 0) {
                PreparedStatement repliesStmt = connection.prepareStatement(repliesSql);

                // set the corresponding param
                repliesStmt.setInt(1, pId);
                // execute the delete statement
                repliesStmt.executeUpdate();
                logger.info("replies related to post deleted");

                PreparedStatement postsStmt = connection.prepareStatement(postsSql);
                postsStmt.setInt(1, pId);
                postsStmt.setString(2, pOwner);
                postsStmt.executeUpdate();
                logger.info("post deleted");
            } else {
                logger.info("post does not exist");
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * drop the tables if exist
     */
    @Override
    public void deleteTables() {
        String deleteRepliesTableQuery = "DROP TABLE IF EXISTS " +
                DMB_REPLIES_TABLE + ";";

        String deletePostsTableQuery = "DROP TABLE IF EXISTS " +
                DMB_POSTS_TABLE + ";";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(deleteRepliesTableQuery);
            logger.info("replies table deleted");
            stmt.execute(deletePostsTableQuery);
            logger.info("posts table deleted");
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }


    /**
     * drop the tables if exist
     */
    @Override
    public void truncateTables() {
        String deleteRepliesTableQuery = "DELETE FROM " +
                DMB_REPLIES_TABLE + ";";

        String deletePostsTableQuery = "DELETE FROM " +
                DMB_POSTS_TABLE + ";";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(deleteRepliesTableQuery);
            logger.info("replies table truncated");
            stmt.execute(deletePostsTableQuery);
            logger.info("posts table truncated");
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     *  Add a reply
     * @param pId
     * @param rOwner
     * @param rDescription
     */
    @Override
    public void addReplyData(int pId, String rOwner, String rDescription) {
        int randomID = ThreadLocalRandom.current().nextInt(10000, 1000000);
        try {

            String sql = "INSERT INTO " + DMB_REPLIES_TABLE + " (" +
                    DMB_REPLY_POST_FK_COLUMN + ", " +
                    DMB_REPLY_ID_COLUMN + ", " +
                    DMB_REPLY_OWNER_COLUMN + ", " +
                    DMB_REPLY_DESCRIPTION_COLUMN + ", " +
                    DMB_REPLY_CREATED_COLUMN + ") VALUES( ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, pId);
            pstmt.setInt(2, randomID);
            pstmt.setString(3, rOwner);
            pstmt.setString(4, rDescription);
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            logger.info("reply data added");
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * Get replies for a particular post as array list
     * @param pId
     * @return
     */
    @Override
    public ArrayList<DMBReply> getRepliesByPostIdArrayList(int pId) {
        try {
            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_REPLIES_TABLE +
                    " WHERE " + DMB_REPLY_POST_FK_COLUMN + " = ?";

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            statement.setInt(1, pId);
            ResultSet resultSet = statement.executeQuery();
            return getRepliesArrayListFromRS(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get all replies as array list
     * @return
     */
    @Override
    public ArrayList<DMBReply> getRepliesArrayList() {
        try {
            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_REPLIES_TABLE;

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            ResultSet resultSet = statement.executeQuery();
            return getRepliesArrayListFromRS(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<DMBReply> getRepliesArrayListFromRS(ResultSet resultSet) throws SQLException {
        ArrayList<DMBReply> dmbReplietsArrayList = new ArrayList<>();
        while (resultSet.next()) {
            DMBReply replyObject = new DMBReply();
            replyObject.setReplyId(resultSet.getInt(DMB_REPLY_ID_COLUMN));
            replyObject.setReplyOwner(resultSet.getString(DMB_REPLY_OWNER_COLUMN));
            replyObject.setPostId(resultSet.getInt(DMB_REPLY_POST_FK_COLUMN));
            replyObject.setReplyDescription(resultSet.getString(DMB_REPLY_DESCRIPTION_COLUMN));
            replyObject.setReplyTimeStamp(resultSet.getTimestamp(DMB_REPLY_CREATED_COLUMN).getTime());
            replyObject.setReplyUpVotes(resultSet.getInt(DMB_REPLY_UPVOTES_COLUMN));
            replyObject.setReplyDownVotes(resultSet.getInt(DMB_REPLY_DOWNVOTES_COLUMN));

            dmbReplietsArrayList.add(replyObject);
        }
        return dmbReplietsArrayList;
    }


    /**
     * Get replies for a particular post as byte array
     * @return
     */
    @Override
    public byte[] getRepliesByteArray() {
        try {
            String selectPostsByTopicQuery = "SELECT * FROM " + DMB_REPLIES_TABLE;

            PreparedStatement statement = connection.prepareStatement(selectPostsByTopicQuery);
            ResultSet resultSet = statement.executeQuery();
            JSONArray dmbRepliesJsonArray = new JSONArray();
            while (resultSet.next()) {
                JSONObject replyObject = new JSONObject();
                replyObject.put(DMB_REPLY_ID_COLUMN, resultSet.getInt(DMB_REPLY_ID_COLUMN));
                replyObject.put(DMB_REPLY_POST_FK_COLUMN, resultSet.getInt(DMB_REPLY_POST_FK_COLUMN));
                replyObject.put(DMB_REPLY_OWNER_COLUMN, resultSet.getString(DMB_REPLY_OWNER_COLUMN));
                replyObject.put(DMB_REPLY_DESCRIPTION_COLUMN, resultSet.getString(DMB_REPLY_DESCRIPTION_COLUMN));
                replyObject.put(DMB_REPLY_CREATED_COLUMN, resultSet.getTimestamp(DMB_REPLY_CREATED_COLUMN).getTime());
                replyObject.put(DMB_REPLY_UPVOTES_COLUMN, resultSet.getInt(DMB_REPLY_UPVOTES_COLUMN));
                replyObject.put(DMB_REPLY_DOWNVOTES_COLUMN, resultSet.getInt(DMB_REPLY_DOWNVOTES_COLUMN));

                dmbRepliesJsonArray.put(replyObject);
            }
            return dmbRepliesJsonArray.toString().getBytes();
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addRepliesFromByteArray(byte[] repliesByteArray) {
        try {
            String recordsJsonString = new String(repliesByteArray, StandardCharsets.UTF_8);
            JSONArray jsonRepliesArray = new JSONArray(recordsJsonString);
            String sql = "INSERT INTO " + DMB_REPLIES_TABLE + " (" +
                    DMB_REPLY_ID_COLUMN + ", " +
                    DMB_REPLY_POST_FK_COLUMN + ", " +
                    DMB_REPLY_OWNER_COLUMN + ", " +
                    DMB_REPLY_DESCRIPTION_COLUMN + ", " +
                    DMB_REPLY_UPVOTES_COLUMN + ", " +
                    DMB_REPLY_DOWNVOTES_COLUMN + ", " +
                    DMB_REPLY_CREATED_COLUMN + ") VALUES(?, ?, ?, ?, ?, ?, ?)";

            for (int i = 0; i < jsonRepliesArray.length(); i++) {

                JSONObject jsonReplyObject = jsonRepliesArray.getJSONObject(i);

                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, jsonReplyObject.getInt(DMB_REPLY_ID_COLUMN));
                pstmt.setInt(2, jsonReplyObject.getInt(DMB_REPLY_POST_FK_COLUMN));
                pstmt.setString(3, jsonReplyObject.getString(DMB_REPLY_OWNER_COLUMN));
                pstmt.setString(4, jsonReplyObject.getString(DMB_REPLY_DESCRIPTION_COLUMN));
                pstmt.setInt(5, jsonReplyObject.getInt(DMB_POST_UPVOTES_COLUMN));
                pstmt.setInt(6, jsonReplyObject.getInt(DMB_POST_DOWNVOTES_COLUMN));
                pstmt.setTimestamp(7, new Timestamp(jsonReplyObject.getLong(DMB_REPLY_CREATED_COLUMN)));
                pstmt.executeUpdate();
                logger.info("replies data added");
            }

        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Up vote a post
     * @param pId
     * @param pOwner
     */
    @Override
    public void upVotePost(int pId, String pOwner) {
        String postsSql = "UPDATE " + DMB_POSTS_TABLE +
                " SET " + DMB_POST_UPVOTES_COLUMN + " = " + DMB_POST_UPVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?";
        try {

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

    /**
     * Down vote a post
     * @param pId
     * @param pOwner
     */
    @Override
    public void downVotePost(int pId, String pOwner) {
        String postsSql = "UPDATE " + DMB_POSTS_TABLE +
                " SET " + DMB_POST_DOWNVOTES_COLUMN + " = " + DMB_POST_DOWNVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_POST_ID_COLUMN + " = ?";
        try {

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

    /**
     * Up vote a reply
     * @param rId
     * @param rOwner
     */
    @Override
    public void upVoteReply(int rId, String rOwner) {
        String repliesSql = "UPDATE " + DMB_REPLIES_TABLE +
                " SET " + DMB_REPLY_UPVOTES_COLUMN + " = " + DMB_REPLY_UPVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_REPLY_ID_COLUMN + " = ?";
        try {
            PreparedStatement replyStmt = connection.prepareStatement(repliesSql);
            replyStmt.setInt(1, rId);
            replyStmt.executeUpdate();
            logger.info("reply up voted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     *  Down vote a reply
     * @param rId
     * @param rOwner
     */
    @Override
    public void downVoteReply(int rId, String rOwner) {
        String repliesSql = "UPDATE " + DMB_REPLIES_TABLE +
                " SET " + DMB_REPLY_DOWNVOTES_COLUMN + " = " + DMB_REPLY_DOWNVOTES_COLUMN + " + 1 " +
                " WHERE " + DMB_REPLY_ID_COLUMN + " = ?";
        try {
            PreparedStatement replyStmt = connection.prepareStatement(repliesSql);
            replyStmt.setInt(1, rId);
            replyStmt.executeUpdate();
            logger.info("reply down voted");

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }
}
