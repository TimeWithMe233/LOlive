package dev.olive.event.impl.events;


import dev.olive.event.impl.Event;
import dev.olive.utils.Servers;

public class HypixelServerSwitchEvent implements Event {
    public final Servers lastServer;
    public final Servers server;

    public HypixelServerSwitchEvent(Servers lastServer, Servers server) {
        this.lastServer = lastServer;
        this.server = server;
    }
}
