package iu.e510.message.board.dbserver.rest;

import iu.e510.message.board.db.DMBDatabaseImpl;
import iu.e510.message.board.db.DMBDatabase;
import iu.e510.message.board.db.model.DMBPost;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/messageboard")
public class DMBPostsService {

    private DMBDatabase database;

    public DMBPostsService(){
        database = new DMBDatabaseImpl("temp");
    }

    @GET
    @Path("/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<DMBPost> getPosts(){
        ArrayList<DMBPost> res = database.getAllPostsDataArrayList();
        System.out.println(res);
        System.out.println(res.size());
        return res;
    }

    @GET
    @Path("/hello")
    public Response helloworld(){
        return Response.status(200).entity("helloworld").build();
    }
}
