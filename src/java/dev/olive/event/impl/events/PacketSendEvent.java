package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.Packet;

/**
 * @author TG_format
 * @since 2024/6/1 0:24
 */
@Getter
@AllArgsConstructor
public class PacketSendEvent extends CancellableEvent {
    private Packet<?> packet;
}
