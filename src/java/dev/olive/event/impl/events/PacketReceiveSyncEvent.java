/*
 * Decompiled with CFR 0.151.
 */
package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;
import net.minecraft.network.Packet;

public class PacketReceiveSyncEvent
        extends CancellableEvent
        implements SingleInstance {
    private Packet packet;

    public PacketReceiveSyncEvent(Packet packet) {
        this.packet = packet;
    }

    @Override
    public void cleanup() {
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }
}

