package iu.e510.message.board.db;

import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.db.model.DMBReply;
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
        db.addPostData("Hello Bloomington", "Bloomington", "postowner1", "hello world!");
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        Assert.assertEquals(posts.get(0).getPostTopic(), "Bloomington");
    }

    @Test (dependsOnMethods = { "testAddPost" })
    public void testGetAllPosts() {
        db.addPostData("Hello IU", "IU", "postowner2", "hello IU!");
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        Assert.assertEquals(posts.size(), 2);
    }

    @Test (dependsOnMethods = { "testGetAllPosts" })
    public void testAddReply() {
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        db.addReplyData(posts.get(0).getPostId(), "replyowner1", "Welcome to Bloomington!");
        db.addReplyData(posts.get(1).getPostId(), "replyowner2", "Welcome to IU!");

        ArrayList<DMBReply> replies1 = db.getRepliesByPostIdArrayList(posts.get(0).getPostId());
        ArrayList<DMBReply> replies2 = db.getRepliesByPostIdArrayList(posts.get(1).getPostId());

        Assert.assertEquals(replies1.get(0).getReplyOwner(), "replyowner1");
        Assert.assertEquals(replies2.get(0).getReplyOwner(), "replyowner2");
    }

    @Test (dependsOnMethods = { "testAddReply"})
    public void testAddUpVoteToPost(){
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        Assert.assertEquals(posts.get(0).getPostUpvotes(), 0);
        db.upVotePost(posts.get(0).getPostId(), "upvoteowner1");
        ArrayList<DMBPost> newposts = db.getPostsDataArrayList();
        Assert.assertEquals(newposts.get(0).getPostUpvotes(), 1);
    }

    @Test (dependsOnMethods = { "testAddUpVoteToPost"})
    public void testAddDownVoteToPost(){
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        Assert.assertEquals(posts.get(0).getPostDownvotes(), 0);
        db.downVotePost(posts.get(0).getPostId(), "downvoteowner1");
        ArrayList<DMBPost> newposts = db.getPostsDataArrayList();
        Assert.assertEquals(newposts.get(0).getPostDownvotes(), 1);
    }

    @Test (dependsOnMethods = { "testAddDownVoteToPost"})
    public void testAddUpVoteToReply(){
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        ArrayList<DMBReply> repliesToPostArrayList = db.getRepliesByPostIdArrayList(posts.get(0).getPostId());
        Assert.assertEquals(repliesToPostArrayList.get(0).getReplyUpVotes(), 0);
        db.upVoteReply(repliesToPostArrayList.get(0).getReplyId(), "upvotereplier1");
        ArrayList<DMBReply> newrepliesToPostArrayList = db.getRepliesByPostIdArrayList(posts.get(0).getPostId());
        Assert.assertEquals(newrepliesToPostArrayList.get(0).getReplyUpVotes(), 1);
    }

    @Test (dependsOnMethods = { "testAddUpVoteToPost"})
    public void testAddDownVoteToReply(){
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        ArrayList<DMBReply> repliesToPostArrayList = db.getRepliesByPostIdArrayList(posts.get(0).getPostId());

        Assert.assertEquals(repliesToPostArrayList.get(0).getReplyDownVotes(), 0);
        db.downVoteReply(repliesToPostArrayList.get(0).getReplyId(), "downvotereplier1");
        ArrayList<DMBReply> newrepliesToPostArrayList = db.getRepliesByPostIdArrayList(posts.get(0).getPostId());
        Assert.assertEquals(newrepliesToPostArrayList.get(0).getReplyDownVotes(), 1);
    }

    @Test (dependsOnMethods = {"testAddDownVoteToReply"})
    public void testRemoveAndAddByteArray(){
        byte[] postData = db.getPostsDataByteArray();
        db.truncateTables();
        db.addPostsDataFromByteArray(postData);
        ArrayList<DMBPost> posts = db.getPostsDataArrayList();
        Assert.assertEquals(posts.size(), 2);
    }

    @Test (dependsOnMethods = { "testRemoveAndAddByteArray"})
    public void testRemovePost(){
        ArrayList<DMBPost> posts1 = db.getPostsDataArrayList();
        db.removePostData(posts1.get(0).getPostId(), "downvoteowner1");
        ArrayList<DMBPost> posts2 = db.getPostsDataArrayList();
        Assert.assertEquals(posts2.size(), 2);
        db.removePostData(posts1.get(0).getPostId(), "postowner1");
        ArrayList<DMBPost> posts3 = db.getPostsDataArrayList();
        Assert.assertEquals(posts3.size(), 1);
        db.removePostData(posts1.get(0).getPostId(), "postowner1");
        ArrayList<DMBPost> posts4 = db.getPostsDataArrayList();
        Assert.assertEquals(posts4.size(), 1);
    }

}