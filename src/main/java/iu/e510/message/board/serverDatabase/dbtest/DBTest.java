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
        DMBDatabase.addPostData("first post", "/r/iu", "ninaad", "this is the first post");
        DMBDatabase.addPostData("second post", "/r/iu", "ninaad", "this is the second post");
        ArrayList<DMBPosts> postsArrayList;

        postsArrayList = DMBDatabase.getAllPostsDataArrayList();
        System.out.println(Collections.singletonList(postsArrayList));


        if (postsArrayList != null) {
            DMBDatabase.removePostData(postsArrayList.get(0).getPostId());
        }

        DMBDatabase.addPostData("third post", "/r/iu", "ninaad", "this is the third post");
        DMBDatabase.addPostData("fourth post", "/r/iu", "ninaad", "this is the fourth post");

        byte [] databytes = DMBDatabase.getAllPostsDataByteArray();

        postsArrayList = DMBDatabase.getAllPostsDataArrayList();

        if (postsArrayList != null) {
            for (int i = 0; i < postsArrayList.size(); i++){
                DMBDatabase.removePostData(postsArrayList.get(i).getPostId());
            }
        }
        postsArrayList = DMBDatabase.getAllPostsDataArrayList();
        System.out.println("on removing all records");
        System.out.println(Collections.singletonList(postsArrayList));

        DMBDatabase.addAllPostsDataByteArray(databytes);

        postsArrayList = DMBDatabase.getAllPostsDataArrayList();
        System.out.println("after adding back all records");
        System.out.println(Collections.singletonList(postsArrayList));

        DMBDatabase.deleteTables();

    }

}
