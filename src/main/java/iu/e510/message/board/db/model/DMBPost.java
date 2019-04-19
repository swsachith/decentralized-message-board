package iu.e510.message.board.db.model;

import java.io.Serializable;
import java.util.ArrayList;

public class DMBPost implements Serializable {
    private int mPostId;
    private String mPostOwnerId;
    private String mPostTopic;
    private String mPostTitle;
    private String mPostDescription;
    private ArrayList<DMBReply> mPostReplies;
    private long mPostTimeStamp;
    private int mPostUpvotes;
    private int mPostDownvotes;

    public DMBPost(){}

    public DMBPost(String mPostOwnerId, String mPostTopic, String mPostTitle, String mPostDescription) {
        this.mPostOwnerId = mPostOwnerId;
        this.mPostTopic = mPostTopic;
        this.mPostTitle = mPostTitle;
        this.mPostDescription = mPostDescription;
    }

    public String getPostOwnerId() {
        return mPostOwnerId;
    }

    public void setPostOwnerId(String mPostOwnerId) {
        this.mPostOwnerId = mPostOwnerId;
    }

    public int getPostId() {
        return mPostId;
    }

    public void setPostId(int mPostId) {
        this.mPostId = mPostId;
    }

    public String getPostTitle() {
        return mPostTitle;
    }

    public void setPostTitle(String mPostTitle) {
        this.mPostTitle = mPostTitle;
    }

    public String getPostTopic() {
        return mPostTopic;
    }

    public void setPostTopic(String mPostCategory) {
        this.mPostTopic = mPostCategory;
    }

    public String getPostDescription() {
        return mPostDescription;
    }

    public void setPostDescription(String mPostDescription) {
        this.mPostDescription = mPostDescription;
    }

    public ArrayList<DMBReply> getPostReplies() {
        return mPostReplies;
    }

    public void setPostReplies(ArrayList<DMBReply> mPostReplies) {
        this.mPostReplies = mPostReplies;
    }

    public long getPostTimeStamp() {
        return mPostTimeStamp;
    }

    public void setPostTimeStamp(long mPostTimeStamp) {
        this.mPostTimeStamp = mPostTimeStamp;
    }

    public int getPostUpvotes() {
        return mPostUpvotes;
    }

    public void setPostUpvotes(int mPostUpvotes) {
        this.mPostUpvotes = mPostUpvotes;
    }

    public int getPostDownvotes() {
        return mPostDownvotes;
    }

    public void setPostDownvotes(int mPostDownvotes) {
        this.mPostDownvotes = mPostDownvotes;
    }


    @Override
    public String toString() {
        return "DMBPost{" +
                "mPostId=" + mPostId +
                ", mPostOwnerId='" + mPostOwnerId + '\'' +
                ", mPostTopic='" + mPostTopic + '\'' +
                ", mPostTitle='" + mPostTitle + '\'' +
                ", mPostDescription='" + mPostDescription + '\'' +
                ", mPostReplies=" + mPostReplies +
                ", mPostTimeStamp=" + mPostTimeStamp +
                ", mPostUpvotes=" + mPostUpvotes +
                ", mPostDownvotes=" + mPostDownvotes +
                '}';
    }
}
