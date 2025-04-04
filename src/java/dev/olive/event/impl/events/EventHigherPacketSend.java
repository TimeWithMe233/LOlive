package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;
import net.minecraft.network.Packet;

;

public class EventHigherPacketSend
        extends CancellableEvent {
    public Packet packet;

    public EventHigherPacketSend(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}

