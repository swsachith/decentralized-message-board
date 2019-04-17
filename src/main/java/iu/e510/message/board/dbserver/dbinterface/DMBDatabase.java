package iu.e510.message.board.dbserver.dbinterface;

import iu.e510.message.board.dbserver.model.DMBPost;

import java.util.ArrayList;

public interface DMBDatabase {
    byte[] getAllPostsDataByteArray();
    byte[] getAllPostsDataByTopicByteArray(String pTopic);
    void addAllPostsDataByteArray(byte [] postByteArray);
    ArrayList<DMBPost> getAllPostsDataArrayList();
    ArrayList<DMBPost> getAllPostsDataByTopicArrayList(String pTopic);
    void addPostData(String pTitle, String pCategory, String pOwner, String pDescription);
    void removePostData(int pId, String pOwner);
    void deleteTables();
}