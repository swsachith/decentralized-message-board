package iu.e510.message.board.db;

import iu.e510.message.board.db.model.DMBReply;
import iu.e510.message.board.db.model.DMBPost;

import java.util.ArrayList;

public interface DMBDatabase {
    byte[] getAllPostsDataByteArray();
    byte[] getAllPostsDataByTopicByteArray(String pTopic);
    void addAllPostsDataByteArray(byte[] postByteArray);
    ArrayList<DMBPost> getAllPostsDataArrayList();
    ArrayList<DMBPost> getAllPostsDataByTopicArrayList(String pTopic);

    void addPostData(String pTitle, String pTopic, String pOwner, String pDescription);
    void removePostData(int pId, String pOwner);
    void deleteTables();
    void upVotePost(int pId, String pOwner);
    void downVotePost(int pId, String pOwner);


    void addReplyData(int pId, String rOwner, String rDescription);
    ArrayList<DMBReply> getAllRepliesToPostArrayList(int pId);

    ArrayList<DMBReply> getAllRepliesArrayList();

    byte [] getAllRepliesByteArray();

    void upVoteReply(int rId, String rOwner);
    void downVoteReply(int rId, String rOwner);
}
