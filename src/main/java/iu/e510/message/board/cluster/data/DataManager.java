package iu.e510.message.board.cluster.data;

import java.util.Set;

public interface DataManager {
    void addData(String path, byte[] data) throws Exception;

    byte[] getData(String path);

    boolean hasData(String path);

    void deleteData(String path) throws Exception;

    Set<String> getAllTopics();

    void setConsistency(boolean consistency);

    boolean getConsistency();
}
