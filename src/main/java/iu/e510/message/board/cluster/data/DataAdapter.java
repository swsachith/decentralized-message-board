package iu.e510.message.board.cluster.data;

/**
 * Adapter for data operations. Can be implemented for in-memory/ database operations
 */
public interface DataAdapter {

    byte[] getDataDump(String topic);

    void putDataDump(String topic, byte[] dataDump);

}
