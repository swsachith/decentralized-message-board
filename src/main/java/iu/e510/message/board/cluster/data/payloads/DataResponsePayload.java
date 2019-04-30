package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.tom.common.Payload;

public class DataResponsePayload extends Payload<byte[]> {
    public DataResponsePayload(byte[] content) {
        super(content);
    }

    @Override
    public String toString() {
        return "DataResPayload{" +
                "content=" + getContent() +
                '}';
    }
}
