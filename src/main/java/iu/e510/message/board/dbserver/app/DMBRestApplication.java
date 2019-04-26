package iu.e510.message.board.dbserver.app;


import iu.e510.message.board.dbserver.rest.DMBPostsService;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class DMBRestApplication extends Application {

    private Set<Object> singletons = new HashSet<Object>();

    public DMBRestApplication(){
        singletons.add(new DMBPostsService());
    }

    @Override
    public Set<Object> getSingletons(){
        return singletons;
    }
}
