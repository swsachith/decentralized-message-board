package iu.e510.message.board.serverDatabase.dbtest;


import iu.e510.message.board.serverDatabase.db.DMBDatabase;
import iu.e510.message.board.serverDatabase.ds.DMBPosts;

import java.util.ArrayList;
import java.util.Collections;

public class DBTest {

    public static void main(String[] args){

        /*check if jdbc is integrated*/
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /*create a database if not exists*/
        DMBDatabase.createNewDatabase();
        /*create the posts table if not exists*/
        DMBDatabase.createTables();
        DMBDatabase.addPostData("first post", "ninaad", "this is the first post");
        DMBDatabase.addPostData("second post", "ninaad", "this is the second post");
        ArrayList<DMBPosts> postsArrayList;

        postsArrayList = DMBDatabase.selectPostsData();
        System.out.println(Collections.singletonList(postsArrayList));


        if (postsArrayList != null) {
            DMBDatabase.removePostData(postsArrayList.get(0).getPostId());
        }

        postsArrayList = DMBDatabase.selectPostsData();
        System.out.print(Collections.singletonList(postsArrayList));

    }

}
