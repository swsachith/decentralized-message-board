package iu.e510.message.board.tom.common;

import java.io.Serializable;

public interface Payload<T> extends Serializable {
    String getOrigin();

    T getContent();
}
