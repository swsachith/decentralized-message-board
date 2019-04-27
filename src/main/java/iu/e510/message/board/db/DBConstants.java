package iu.e510.message.board.db;

public class DBConstants {


    /*table for storing posts*/
    public static final String DMB_POSTS_TABLE = "dmb_posts";

    public static final String DMB_POST_ID_COLUMN = "post_id";
    //    private static final String DMB_POST_TOPIC_ID_COLUMN = "post_topic_id";
    public static final String DMB_POST_TOPIC_COLUMN = "post_topic";
    public static final String DMB_POST_TITLE_COLUMN = "post_title";
    public static final String DMB_POST_OWNER_COLUMN = "post_owner";
    public static final String DMB_POST_DESCRIPTION_COLUMN = "post_description";
    public static final String DMB_POST_CREATED_COLUMN = "post_created";
    public static final String DMB_POST_UPVOTES_COLUMN = "post_upvotes";
    public static final String DMB_POST_DOWNVOTES_COLUMN = "post_downvotes";

    /*table for storing replies (single level replies)*/
    public static final String DMB_REPLIES_TABLE = "dmb_replies";

    public static final String DMB_REPLY_ID_COLUMN = "reply_id";
    public static final String DMB_REPLY_POST_FK_COLUMN = "post_id_fk";
    public static final String DMB_REPLY_OWNER_COLUMN = "reply_owner";
    public static final String DMB_REPLY_DESCRIPTION_COLUMN = "reply_description";
    public static final String DMB_REPLY_CREATED_COLUMN = "reply_created";
    public static final String DMB_REPLY_UPVOTES_COLUMN = "reply_upvotes";
    public static final String DMB_REPLY_DOWNVOTES_COLUMN = "reply_downvotes";


    /*table for storing feedback*/
    public static final String DMB_FEEDBACK_TABLE = "dmb_feedback";

    public static final String DMB_FEEDBACK_FK_COLUMN = "reply_id_fk";
    public static final String DMB_FEEDBACK_POST_FK_COLUMN = "post_id_fk";
    public static final String DMB_FEEDBACK_OWNER_COLUMN = "reply_owner";
    public static final String DMB_FEEDBACK_DESCRIPTION_COLUMN = "reply_description";
    public static final String DMB_FEEDBACK_CREATED_COLUMN = "reply_created";
    public static final String DMB_FEEDBACK_UPVOTES_COLUMN = "reply_upvotes";
    public static final String DMB_FEEDBACK_DOWNVOTES_COLUMN = "reply_downvotes";


    /*key for json array containing replies to specific post*/
    public static final String DMB_REPLIES_LIST_KEY = "replies_list";
}
