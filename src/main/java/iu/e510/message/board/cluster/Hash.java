package iu.e510.message.board.cluster;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.Serializable;

public class Hash implements Serializable {

    private HashFunction hashFunction;

    public Hash() {
        hashFunction = Hashing.murmur3_32();
    }

    public int getHash(String value) {
        return hashFunction.hashString(value).asInt() >>> 1;
    }
}
