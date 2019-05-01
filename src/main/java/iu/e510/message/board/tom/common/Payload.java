package iu.e510.message.board.tom.common;

import java.io.Serializable;

public class Payload<T> implements Serializable {

    private static final long serialVersionUID = -7878077485755636200L;
    private String origin;
    private T content;

    public Payload(T content) {
        this(null, content);
    }

    public Payload(String origin, T content) {
        this.origin = origin;
        this.content = content;
    }

    public String getOrigin() {
        return origin;
    }

    public T getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "content=" + content +
                '}';
    }

    public boolean isEmpty() {
        return content == null;
    }
}
