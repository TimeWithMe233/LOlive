package dev.olive.event.impl.events;


import dev.olive.event.impl.Event;
import lombok.Getter;
import net.minecraft.network.Packet;

@Getter
public class EventPacketCustom implements Event {
    Packet packet;

    public EventPacketCustom(Packet packet) {
        this.packet = packet;
    }

}
