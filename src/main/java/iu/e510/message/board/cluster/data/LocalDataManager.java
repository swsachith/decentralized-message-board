package iu.e510.message.board.cluster.data;

/**
 * Data manager used to access local database data
 */
public interface LocalDataManager {

    byte[] getDataDump(String topic);

    void putDataDump(String topic, byte[] dataDump);

}
