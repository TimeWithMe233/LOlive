package dev.olive.event.impl.events;


import dev.olive.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class EventSendChatMessage extends CancellableEvent {
    String msg;

    public EventSendChatMessage(String msg) {
        this.msg = msg;
    }

}
