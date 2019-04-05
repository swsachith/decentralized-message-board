package iu.e510.message.board.serverDatabase.ds;

import java.io.Serializable;
import java.util.ArrayList;

public class DMBPosts implements Serializable {
    private int mPostId;
    private String mPostOwnerId;
    private String mPostTitle;
    private String mPostDescription;
    private ArrayList<String> mPostReplies;
    private long mPostTimeStamp;
    private int mPostUpvotes;
    private int mPostDownvotes;

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

    public String getPostDescription() {
        return mPostDescription;
    }

    public void setPostDescription(String mPostDescription) {
        this.mPostDescription = mPostDescription;
    }

    public ArrayList<String> getPostReplies() {
        return mPostReplies;
    }

    public void setPostReplies(ArrayList<String> mPostReplies) {
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
        return "DMBPosts{" +
                "mPostId=" + mPostId +
                ", mPostOwnerId='" + mPostOwnerId + '\'' +
                ", mPostTitle='" + mPostTitle + '\'' +
                ", mPostDescription='" + mPostDescription + '\'' +
                ", mPostReplies=" + mPostReplies +
                ", mPostTimeStamp=" + mPostTimeStamp +
                ", mPostUpvotes=" + mPostUpvotes +
                ", mPostDownvotes=" + mPostDownvotes +
                '}';
    }
}
