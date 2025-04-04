package dev.olive.module.impl.player;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.vialoadingbase.ViaLoadingBase;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "无掉落伤害",Category.Player);
    }

    private boolean pre = false;

    @Override
    public void onDisable() {
        pre = false;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() < 755) return;

        setSuffix("Grim1.17+");

        if (pre) {
            if (!mc.thePlayer.onGround && mc.thePlayer.fallDistance > 1F) {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.000000001,
                        mc.thePlayer.posZ,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        false));
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.isPre()) pre = true;
    }
}
