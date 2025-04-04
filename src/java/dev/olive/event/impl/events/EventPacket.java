package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

@Getter
@Setter
public class EventPacket extends CancellableEvent {
    private final EventState eventType;
    public Packet packet;
    private final INetHandler netHandler;

    public EventPacket(EventState eventType, Packet packet, INetHandler netHandler) {
        this.eventType = eventType;
        this.packet = packet;
        this.netHandler = netHandler;
    }

    public enum EventState {
        SEND,
        RECEIVE
    }
}


