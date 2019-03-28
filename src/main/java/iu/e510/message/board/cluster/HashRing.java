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
    private int replicas;

    public HashRing(int replicas) {
        hash = new Hash();
        ring = new TreeMap<>();
        this.replicas = replicas;
    }

    public void addAll(List<String> nodes) {
        for (String node : nodes) {
            add(node);
        }
    }

    public void add(String node) {
        logger.debug("Adding node to the hash ring: " + node);
        for (int i = 0; i < replicas; i++) {
            ring.put(hash.getHash(node + "_" + i), node);
        }
    }

    public void remove(String node) {
        logger.debug("Removing node from the hash ring: " + node);
        for (int i = 0; i < replicas; i++) {
            ring.remove(hash.getHash(node + "_" + i));
        }
    }

    public Integer getHashingNode(String key) {
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

    public String getValue(int key) {
        return ring.get(key);
    }
}
