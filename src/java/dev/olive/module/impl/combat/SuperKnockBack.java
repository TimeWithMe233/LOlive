package dev.olive.module.impl.combat;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventAttack;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.module.Category;
import dev.olive.module.Module;

public class SuperKnockBack extends Module {
    public static boolean sprint = true, wTap;
    public static String part11 = "t";
    public SuperKnockBack() {
        super("SuperKnockBack", "超级击退",Category.Combat);
    }

    @Override
    public void onDisable() {
        sprint = true;
    }


    @Override
    public void onEnable() {
        sprint = true;
    }

    @EventTarget
    public void onAttack(EventAttack event) {

        wTap = Math.random() * 100 < 100;

        //if (!wTap) return;

    }

    @EventTarget
    public void onPre(EventMotion event) {
        if (event.isPre()) {
            // if (!wTap) return;

            if (mc.thePlayer.moveForward > 0 && mc.thePlayer.serverSprintState == mc.thePlayer.isSprinting()) {
                sprint = !mc.thePlayer.serverSprintState;
            }
        }

    }

}
