package iu.e510.message.board.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

public class HashRing implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(HashRing.class);

    private TreeMap<Integer, String> ring;
    private Hash hash;

    public HashRing() {
        hash = new Hash();
        ring = new TreeMap<>();
    }

    public synchronized void addAll(List<String> nodes) {
        for (String node : nodes) {
            add(node);
        }
    }

    //todo: extend this to support replicas (adding an index)
    public synchronized void add(String node) {
        logger.debug("Adding node to the hash ring: " + node);
        ring.put(hash.getHash(node), node);
    }

    public synchronized void remove(String node) {
        logger.debug("Removing node from the hash ring: " + node);
        ring.remove(hash.getHash(node));
    }

    public synchronized Integer getHashNode(String key) {
        int hashValue = hash.getHash(key);
        Integer floorKey = ring.floorKey(hashValue);
        Integer ceilingKey = ring.ceilingKey(hashValue);

        // Ring is empty
        if (floorKey == null && ceilingKey == null) {
            return null;
        }

        // return the next node
        if (ceilingKey != null) {
            return ceilingKey;
        } else { // return the first key (since it's a ring)
            return ring.firstKey();
        }
    }
}
