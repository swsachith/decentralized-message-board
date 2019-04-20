package iu.e510.message.board.db;

import iu.e510.message.board.db.model.DMBReply;
import iu.e510.message.board.db.model.DMBPost;

import java.util.ArrayList;

public interface DMBDatabase {
    byte[] getAllPostsDataByteArray();
    void addAllPostsDataByteArray(byte[] postByteArray);
    byte[] getAllPostsDataByTopicByteArray(String pTopic);

    byte [] getAllRepliesByteArray();
    void addAllRepliesDataByteArray(byte[] repliesByteArray);
    ArrayList<DMBReply> getAllRepliesArrayList();
    void deleteTables();

    ArrayList<DMBPost> getAllPostsDataArrayList();
    ArrayList<DMBPost> getAllPostsDataByTopicArrayList(String pTopic);
    void addPostData(String pTitle, String pTopic, String pOwner, String pDescription);
    void removePostData(int pId, String pOwner);
    void upVotePost(int pId, String pOwner);
    void downVotePost(int pId, String pOwner);
    void addReplyData(int pId, String rOwner, String rDescription);
    ArrayList<DMBReply> getAllRepliesToPostArrayList(int pId);
    void upVoteReply(int rId, String rOwner);
    void downVoteReply(int rId, String rOwner);
}
