package iu.e510.message.board.tom.common;

public class StringPayload implements Payload<String> {
    private String origin;
    private String content;

    public StringPayload(String content) {
        this(null, content);
    }

    public StringPayload(String origin, String content) {
        this.origin = origin;
        this.content = content;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return origin + ":" + content;
    }
}
