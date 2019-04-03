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
    private int nodeReplicas;
    private boolean uniformRing;

    public HashRing(int nodeReplicas) {
        hash = new Hash();
        ring = new TreeMap<>();
        this.nodeReplicas = nodeReplicas;
        this.uniformRing = nodeReplicas != 1;
    }

    /**
     * Add all the given nodes to the ring
     * @param nodes
     */
    public void addAll(List<String> nodes) {
        for (String node : nodes) {
            add(node);
        }
    }

    /**
     * Add a given nodes to the ring along with the nodeReplicas
     * @param node
     */
    public void add(String node) {
        logger.debug("Adding node to the hash ring: " + node);
        if (uniformRing) {
            for (int i = 0; i < nodeReplicas; i++) {
                ring.put(hash.getHash(node + "_" + i), node);
            }
        } else {
            ring.put(hash.getHash(node), node);
        }
    }

    /**
     * Remove a node from the ring. Removes all the nodeReplicas as well
     * @param node
     */
    public void remove(String node) {
        logger.debug("Removing node from the hash ring: " + node);
        if (uniformRing) {
            for (int i = 0; i < nodeReplicas; i++) {
                ring.remove(hash.getHash(node + "_" + i));
            }
        } else {
            ring.remove(hash.getHash(node));
        }
    }

    /**
     * Get the closest node to which a key hashes to
     * @param key
     * @return
     */
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

    /**
     * Get the ip of a node in the ring
     * @param key
     * @return
     */
    public String getValue(int key) {
        return ring.get(key);
    }
}
