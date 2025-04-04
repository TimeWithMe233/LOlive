package dev.olive.module.impl.move;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.module.impl.misc.AntiBot;
import dev.olive.module.impl.misc.Teams;
import dev.olive.module.impl.player.Blink;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.ui.hud.notification.NotificationType;
import dev.olive.utils.player.AttackUtil;
import dev.olive.utils.player.MoveUtil;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.utils.player.RotationUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class TargetStrafe
        extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Grim"},  "Grim");
    private final BoolValue lagBackCheck = new BoolValue("LagBack Check", true);
    private final BoolValue scaffoldCheck = new BoolValue("Scaffold/Fly Check", true);
    private final BoolValue blinkCheck = new BoolValue("Blink Check", true);

    public TargetStrafe() {
        super("TargetStrafe","转圈圈" ,Category.Combat);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
        super.onDisable();
    }

    @EventTarget
    private void onPacketReceive(EventPacket event) {
        if (event.getEventType() == EventPacket.EventState.RECEIVE) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof S08PacketPlayerPosLook) {
                if (this.lagBackCheck.get()) {
                    NotificationManager.post(NotificationType.WARNING, "Speed", "LagBack detected");
                    this.setState(false);
                }
            }
        }
    }
    @EventTarget
    private void onUpdate(PreUpdateEvent event) {
        this.setSuffix(mode.get());
        if ((!blinkCheck.get() || !Client.instance.moduleManager.getModule(Blink.class).getState())) {
            if (scaffoldCheck.get()) {
                Client.instance.moduleManager.getModule(Scaffold.class);
            }
        }
    }

    @EventTarget
    private void onPreMotion(EventMotion event) {
        if (event.isPre()) {
            if ((blinkCheck.get() && Client.instance.moduleManager.getModule(Blink.class).getState()) || (scaffoldCheck.get() && Client.instance.moduleManager.getModule(Scaffold.class).getState())) {
                return;
            }
            if (isGapple()) return;
            if (mode.is("Grim")) {
                AxisAlignedBB playerBox = mc.thePlayer.boundingBox.expand(1.0D, 1.0D, 1.0D);
                int c = 0;
                Iterator<Entity> entitys = mc.theWorld.loadedEntityList.iterator();
                while (true) {
                    Entity entity;
                    do {
                        if (!entitys.hasNext()) {
                            if (c > 0 && MoveUtil.isMoving()) {
                                double strafeOffset = (double) Math.min(c, 3) * 0.08D;
                                float yaw = this.getMoveYaw();
                                double mx = -Math.sin(Math.toRadians(yaw));
                                double mz = Math.cos(Math.toRadians(yaw));
                                mc.thePlayer.addVelocity(mx * strafeOffset, 0.0D, mz * strafeOffset);
                                if (c < 4 && KillAura.target != null && this.shouldFollow()) {
                                    mc.gameSettings.keyBindLeft.pressed = true;
                                } else {
                                    mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
                                }
                                return;
                            } else {
                                mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
                                return;
                            }
                        }

                        entity = entitys.next();
                    } while (!(entity instanceof EntityLivingBase) && !(entity instanceof EntityBoat) && !(entity instanceof EntityMinecart) && !(entity instanceof EntityFishHook));

                    if (!(entity instanceof EntityArmorStand)
                            && entity.getEntityId() != mc.thePlayer.getEntityId()
                            && playerBox.intersectsWith(entity.boundingBox)
                            && entity.getEntityId() != -8 && entity.getEntityId() != -1337
                            && !(Client.instance.moduleManager.getModule(Blink.class)).getState()) {
                        ++c;
                    }
                }
            }
        }

    }


    public boolean shouldFollow() {
        return this.getState() && mc.gameSettings.keyBindJump.isKeyDown();
    }

    private float getMoveYaw() {
        EntityPlayerSP thePlayer = mc.thePlayer;
        float moveYaw = thePlayer.rotationYaw;
        if (thePlayer.moveForward != 0.0F && thePlayer.moveStrafing == 0.0F) {
            moveYaw += thePlayer.moveForward > 0.0F ? 0.0F : 180.0F;
        } else if (thePlayer.moveForward != 0.0F) {
            if (thePlayer.moveForward > 0.0F) {
                moveYaw += thePlayer.moveStrafing > 0.0F ? -45.0F : 45.0F;
            } else {
                moveYaw -= thePlayer.moveStrafing > 0.0F ? -45.0F : 45.0F;
            }

            moveYaw += thePlayer.moveForward > 0.0F ? 0.0F : 180.0F;
        } else if (thePlayer.moveStrafing != 0.0F) {
            moveYaw += thePlayer.moveStrafing > 0.0F ? -70.0F : 70.0F;
        }

        if (KillAura.target != null && mc.gameSettings.keyBindJump.isKeyDown()) {
            moveYaw = Client.instance.rotationManager.rotation.x;
        }

        return moveYaw;
    }
}

