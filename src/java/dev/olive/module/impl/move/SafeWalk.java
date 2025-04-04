// Slack Client (discord.gg/paGUcq2UTb)

package dev.olive.module.impl.move;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.MoveEvent;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.BoolValue;
import net.minecraft.util.Vec3;



public class SafeWalk extends Module {

    private final BoolValue offGround = new BoolValue("In Air", false);
    private final BoolValue overEdge = new BoolValue("Only Over Edge", true);
    private final BoolValue avoidJump = new BoolValue("Avoid During Jump", true);

    public SafeWalk() {
       super("SafeWalk","边缘行走", Category.Movement);
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (!isOverEdge() && overEdge.getValue()) return;
        if (mc.thePlayer.motionY > 0 && mc.thePlayer.offGroundTicks < 6 && avoidJump.getValue()) return;
        event.safewalk = mc.thePlayer.onGround || offGround.getValue();
    }

    private boolean isOverEdge() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ),
                true, true, false) == null;
    }


}
