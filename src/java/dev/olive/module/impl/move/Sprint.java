package dev.olive.module.impl.move;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventStrafe;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.Gapple;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint","疾跑", Category.Movement);
    }

    @EventTarget
    public void onStrafe(EventStrafe e) {
        if (Client.instance.moduleManager.getModule(Gapple.class).getState()) {
            mc.gameSettings.keyBindSprint.pressed = false;
            mc.thePlayer.setSprinting(false);
            return;
        }
        mc.gameSettings.keyBindSprint.pressed = true;
    }
}
