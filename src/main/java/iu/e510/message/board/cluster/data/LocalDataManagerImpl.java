package iu.e510.message.board.cluster.data;

public class LocalDataManagerImpl implements LocalDataManager {
    @Override
    public byte[] getDataDump(String topic) {
        return new byte[0];
    }

    @Override
    public void putDataDump(String topic, byte[] dataDump) {

    }
}
