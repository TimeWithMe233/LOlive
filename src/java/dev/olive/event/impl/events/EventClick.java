package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class EventClick extends CancellableEvent {
    private int key;

    public EventClick(int key) {
        this.key = key;
    }

}
