package dev.olive.module.impl.combat;


import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.event.impl.events.EventWorldLoad;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.NumberValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.LinkedHashMap;



@SuppressWarnings("unused")
public class BackTrack extends Module {
    public BackTrack() {
        super("BackTrack","回溯", Category.Combat);
    }
    public static String part6 = ":";
    private NumberValue length = new NumberValue("BackTrackLength", 5, 1, 20,1);

    public static LinkedHashMap<EntityPlayer, ArrayList<AxisAlignedBB>> playerBBox = new LinkedHashMap<>();

    public static boolean hand = true;

    @Override
    public void onEnable() {
        playerBBox.clear();
    }
    @EventTarget
    private void updateEventHandler(EventUpdate event) {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            playerBBox.computeIfAbsent(player, k -> new ArrayList<>());
            playerBBox.get(player).add(player.boundingBox);

            while (true) {
                if (playerBBox.get(player).size() > length.getValue()) {
                    playerBBox.get(player).remove(0);
                } else {
                    break;
                }
            }
        }
    };

    @EventTarget
    private void worldEventHandler(EventWorldLoad event) {
        playerBBox.clear();
    };

    public static AxisAlignedBB getClosedBBox(EntityPlayer player) {
        if (mc.thePlayer.getDistanceToEntity(player) <= KillAura.range.getValue()) return predictPlayerBBox(player);
        if (!Client.instance.moduleManager.getModule(BackTrack.class).getState()) return predictPlayerBBox(player);
        if (playerBBox.get(player) == null) return predictPlayerBBox(player);
        AxisAlignedBB nearestBBox = predictPlayerBBox(player);
        double nearestDistance = mc.thePlayer.getClosestDistanceToBBox(nearestBBox);

        for (AxisAlignedBB bbox : playerBBox.get(player)) {
            float closestDistanceToBBox = mc.thePlayer.getClosestDistanceToBBox(bbox);
            if (closestDistanceToBBox < nearestDistance) {
                nearestDistance = closestDistanceToBBox;
                nearestBBox = bbox;
            }
        }

        return nearestBBox;
    }

    public static AxisAlignedBB predictPlayerBBox(EntityPlayer player) {
        double x = player.posX - player.lastTickPosX;
        double y = player.posY - player.lastTickPosY;
        double z = player.posZ - player.lastTickPosZ;
        return player.boundingBox.offset(x, y, z);
    }
}
