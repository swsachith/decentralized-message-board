package iu.e510.message.board.db;

import iu.e510.message.board.db.model.DMBReply;
import iu.e510.message.board.db.model.DMBPost;

import java.util.ArrayList;

public interface DMBDatabase {
    byte[] getPostsDataByteArray();
    void addPostsDataFromByteArray(byte[] postByteArray);
    byte[] getPostsDataByTopicByteArray(String pTopic);

    byte [] getRepliesByteArray();
    void addRepliesFromByteArray(byte[] repliesByteArray);
    ArrayList<DMBReply> getRepliesArrayList();
    void truncateTables();
    void deleteTables();

    ArrayList<DMBPost> getPostsDataArrayList();
    ArrayList<DMBPost> getAllPostsDataByTopicArrayList(String pTopic);
    void addPostData(String pTitle, String pTopic, String pOwner, String pDescription);
    void removePostData(int pId, String pOwner);
    void upVotePost(int pId, String pOwner);
    void downVotePost(int pId, String pOwner);
    void addReplyData(int pId, String rOwner, String rDescription);
    ArrayList<DMBReply> getRepliesByPostIdArrayList(int pId);
    void upVoteReply(int rId, String rOwner);
    void downVoteReply(int rId, String rOwner);
}
