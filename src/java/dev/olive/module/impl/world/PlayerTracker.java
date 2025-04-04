package dev.olive.module.impl.world;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventTick;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.event.impl.events.EventWorldLoad;
import dev.olive.event.impl.events.WorldEvent;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.misc.Teams;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.ui.hud.notification.NotificationType;
import dev.olive.utils.DebugUtil;
import dev.olive.utils.HYTUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerTracker extends Module {
    private static CopyOnWriteArrayList<EntityPlayer> godAxePlayer = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<EntityPlayer> kbBallPlayer = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<EntityPlayer> enchantedGApplePlayer = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<EntityPlayer> isxinxindogPlayer = new CopyOnWriteArrayList<>();
    public PlayerTracker() {
        super("PlayerTracker","玩家跟踪器", Category.Misc);
    }
    @EventTarget
    private void handler(EventUpdate event ) {
        mc.theWorld.playerEntities.forEach((entity) -> {
            if (!entity.equals(mc.thePlayer) && HYTUtils.isHoldingGodAxe(entity) && !godAxePlayer.contains(entity)) {
                godAxePlayer.add(entity);
                DebugUtil.log("Notify", "WARNING! " + entity.getName() + " is holding §cGodAxe§r");

                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cGodAxe§r!", 5);
                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cGodAxe§r!", 5);
                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cGodAxe§r!", 5);

                mc.thePlayer.playSound("random.orb", 1, 16);
            }
            if (!entity.equals(mc.thePlayer) && HYTUtils.isKBBall(entity.getEquipmentInSlot(0)) && !kbBallPlayer.contains(entity)) {
                kbBallPlayer.add(entity);
                DebugUtil.log("Notify", "WARNING! " + entity.getName() + " is holding §cKBBall§r");

                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cKBBall§r", 5);
                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cKBBall§r", 5);
                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cKBBall§r", 5);

                mc.thePlayer.playSound("random.orb", 1, 16);
            }
            if (!entity.equals(mc.thePlayer) && HYTUtils.isHoldingEnchantedGoldenApple(entity) && !enchantedGApplePlayer.contains(entity)) {
                enchantedGApplePlayer.add(entity);
                DebugUtil.log("Notify", "WARNING! " + entity.getName() + " is holding §cEnchanted GApple§r");

                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cEnchanted GApple§r", 5);
                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cEnchanted GApple§r", 5);
                NotificationManager.post(NotificationType.WARNING,"Notifl", "WARNING! " + entity.getName() + " is holding §cEnchanted GApple§r", 5);

                mc.thePlayer.playSound("random.orb", 1, 16);
            }
        });
    };
     @EventTarget
    private void worldEventHandler(EventWorldLoad event) {
        godAxePlayer.clear();
        kbBallPlayer.clear();
        enchantedGApplePlayer.clear();
    };



    @Override
    public void onEnable() {
        godAxePlayer.clear();
        kbBallPlayer.clear();
        enchantedGApplePlayer.clear();
    }

    @Override
    public void onDisable() {
        godAxePlayer.clear();
        kbBallPlayer.clear();
        enchantedGApplePlayer.clear();
    }

    public static boolean isHeldGodAxe(EntityPlayer player) {
        if (!Client.instance.moduleManager.getModule(PlayerTracker.class).getState()) return false;
        return godAxePlayer.contains(player);
    }

    public static boolean isHeldKBBall(EntityPlayer player) {
        if (!Client.instance.moduleManager.getModule(PlayerTracker.class).getState()) return false;
        return kbBallPlayer.contains(player);
    }

    public static boolean isHeldEnchantedGApple(EntityPlayer player) {
        if (!Client.instance.moduleManager.getModule(PlayerTracker.class).getState()) return false;
        return enchantedGApplePlayer.contains(player);
    }
    public static boolean isxinxindog(EntityPlayer player) {
        if (!Client.instance.moduleManager.getModule(PlayerTracker.class).getState()) return false;
        return isxinxindogPlayer.contains(player);
    }
}
