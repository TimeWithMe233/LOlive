/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package dev.olive.utils;


import dev.olive.Client;
import dev.olive.module.ModuleManager;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.module.impl.misc.Teams;
import dev.olive.module.impl.move.Speed;
import dev.olive.module.impl.player.Blink;
import dev.olive.module.impl.render.BlockAnimation;
import dev.olive.module.impl.render.HUD;
import dev.olive.utils.render.animation.Animation;

public class ModuleUtil {
    private static ModuleManager moduleManager;

    private static ModuleManager getModuleManager() {
        if (moduleManager == null) {
            moduleManager = Client.instance.getModuleManager();
        }
        return moduleManager;
    }

    public static KillAura getKillaura() {
        return ModuleUtil.getModuleManager().getModule(KillAura.class);
    }


    public static Teams getTeams() {
        return ModuleUtil.getModuleManager().getModule(Teams.class);
    }


    public static Speed getSpeed() {
        return ModuleUtil.getModuleManager().getModule(Speed.class);
    }

    public static HUD getHUD() {
        return ModuleUtil.getModuleManager().getModule(HUD.class);
    }


    public static BlockAnimation getAnimations() {
        return ModuleUtil.getModuleManager().getModule(BlockAnimation.class);
    }

    public static Blink getBlink() {
        return ModuleUtil.getModuleManager().getModule(Blink.class);
    }
}

