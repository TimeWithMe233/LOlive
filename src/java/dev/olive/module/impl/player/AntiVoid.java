/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package dev.olive.module.impl.player;


import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.PacketSendEvent;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.move.Speed;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.TimerUtil;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

public class AntiVoid
        extends Module {

    private final ModeValue mode = new ModeValue("Mode", new String[]{"Watchdog"}, "Watchdog");
    private final NumberValue fallDist = new NumberValue("Fall Distance", 3, 1, 20, 0.5);
    private final TimerUtil timer = new TimerUtil();
    private boolean reset;
    private double lastGroundY;

    private final List<Packet> packets = new ArrayList<>();

    public AntiVoid() {
        super("AntiVoid","反虚空", Category.Player);

    }

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent event) {
        setSuffix(mode.get());
        if (mode.is("Watchdog") && !Client.instance.getModuleManager().getModule(Speed.class).getState()) {
            if (event.getPacket() instanceof C03PacketPlayer) {
                if (!isBlockUnder()) {
                    if (mc.thePlayer.fallDistance < fallDist.getValue()) {
                        event.setCancelled();
                        packets.add(event.getPacket());
                    } else {
                        if (!packets.isEmpty()) {
                            for (Packet packet : packets) {
                                final C03PacketPlayer c03 = (C03PacketPlayer) packet;
                                c03.setY(lastGroundY);
                                PacketUtil.sendPacketNoEvent(packet);
                            }
                            packets.clear();
                        }
                    }
                } else {
                    lastGroundY = mc.thePlayer.posY;
                    if (!packets.isEmpty()) {
                        packets.forEach(PacketUtil::sendPacketNoEvent);
                        packets.clear();
                    }
                }
            }
        }
    }

    private boolean isBlockUnder() {
        if (mc.thePlayer.posY < 0) return false;
        for (int offset = 0; offset < (int) mc.thePlayer.posY + 2; offset += 2) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

}

