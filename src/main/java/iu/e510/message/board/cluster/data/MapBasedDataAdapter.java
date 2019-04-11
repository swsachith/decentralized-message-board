package iu.e510.message.board.cluster.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapBasedDataAdapter implements DataAdapter {

    private Map<String, byte[]> dataMap = new ConcurrentHashMap<>();

    @Override
    public byte[] getDataDump(String topic) {
        return dataMap.get(topic);
    }

    @Override
    public void putDataDump(String topic, byte[] dataDump) {
        dataMap.put(topic, dataDump);
    }
}
