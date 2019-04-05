package iu.e510.message.board.cluster.data;

import java.util.HashSet;

public interface DataManager {
    void addData(String path, String data) throws Exception;

    boolean getData(String path);

    void deleteData(String path) throws Exception;

    void createTopic(String topic);

    boolean existsTopic(String topic);


    HashSet<String> getAllTopics();
}
