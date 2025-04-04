package dev.olive.event.impl.events;


import dev.olive.event.impl.Event;

public class EventRenderPlayer
        implements Event {

    public REventState eventState;

    public EventRenderPlayer(REventState eventState) {
        this.eventState = eventState;
    }

    public enum REventState {
        PRE,
        POST
    }
}

