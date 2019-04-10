package iu.e510.message.board.dbconnect.dbtest;


import iu.e510.message.board.dbconnect.db.DMBDatabase;
import iu.e510.message.board.dbconnect.ds.DMBPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

public class DBTest {
    private static Logger logger = LoggerFactory.getLogger(DBTest.class);
    public static void main(String[] args){

        /*check if jdbc is integrated*/
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /*create a database if not exists*/
        DMBDatabase dmbDatabase = new DMBDatabase();
        dmbDatabase.createNewDatabase();
        /*create the posts table if not exists*/
        dmbDatabase.createTables();
//        dmbDatabase.addPostData("first post", "/r/iu", "ninaad", "this is the first post");
//        dmbDatabase.addPostData("second post", "/r/iu", "ninaad", "this is the second post");
//        ArrayList<DMBPost> postsArrayList;
//
//        postsArrayList = dmbDatabase.getAllPostsDataArrayList();
//        System.out.println(Collections.singletonList(postsArrayList));
//
//
//        if (postsArrayList != null) {
//            dmbDatabase.removePostData(postsArrayList.get(0).getPostId(), postsArrayList.get(0).getPostOwnerId());
//        }
//
//        dmbDatabase.addPostData("third post", "/r/iu", "ninaad", "this is the third post");
//        dmbDatabase.addPostData("fourth post", "/r/iu", "ninaad", "this is the fourth post");
//
//        byte [] databytes = dmbDatabase.getAllPostsDataByteArray();
//
//        postsArrayList = dmbDatabase.getAllPostsDataArrayList();
//
//        if (postsArrayList != null) {
//            for (int i = 0; i < postsArrayList.size(); i++){
//                dmbDatabase.removePostData(postsArrayList.get(i).getPostId(), postsArrayList.get(0).getPostOwnerId());
//            }
//        }
//        postsArrayList = dmbDatabase.getAllPostsDataArrayList();
//        System.out.println("on removing all records");
//        System.out.println(Collections.singletonList(postsArrayList));
//
//        dmbDatabase.addAllPostsDataByteArray(databytes);
//
//        postsArrayList = dmbDatabase.getAllPostsDataArrayList();
//        System.out.println("after adding back all records");
//        System.out.println(Collections.singletonList(postsArrayList));

        System.out.println("adding records with same topic");
        for (int i = 5; i < 150; i++){
            dmbDatabase.addPostData(i + " post", "/r/" + (i % 10), "ninaad", "this is the " +
                    i +" post");
        }
        System.out.println("getting records with same topic");
        System.out.println(dmbDatabase.getAllPostsDataByTopicArrayList("/r/" + 5).size());

//        dmbDatabase.deleteTables();
    }

}
