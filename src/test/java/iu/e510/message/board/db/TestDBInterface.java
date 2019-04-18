package iu.e510.message.board.db;

import iu.e510.message.board.db.model.DMBPost;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class TestDBInterface {
    private static DMBDatabase db;

    @BeforeSuite
    public void setup() {
        db = new DMBDatabaseImpl("Temp");
    }

    @Test
    public void testAddPost() {
        db.addPostData("Hello Bloomington", "Bloomington", "tempowner", "hello world!");
        ArrayList<DMBPost> posts = db.getAllPostsDataArrayList();
        Assert.assertEquals(posts.get(0).getPostTopic(), "Bloomington");
    }

    @Test (dependsOnMethods = { "testAddPost" })
    public void testGetAllPosts() {
        db.addPostData("Hello IU", "IU", "tempowner", "hello IU!");
        ArrayList<DMBPost> posts = db.getAllPostsDataArrayList();
        Assert.assertEquals(posts.size(), 2);
    }
}