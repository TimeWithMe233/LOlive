package dev.olive.module.impl.combat;


import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.utils.DebugUtil;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.player.RaytraceUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings;
import net.vialoadingbase.ViaLoadingBase;
import net.viamcp.fixes.AttackOrder;

import javax.vecmath.Vector2d;
import java.util.concurrent.CopyOnWriteArrayList;


public class Velocity extends Module {
    private final ModeValue modeValue = new ModeValue("Mode",new String[]{"GrimAC", "Watchdog","WatchDog2"},"GrimAC");
    private final BoolValue bwValue = new BoolValue("Bedwars", false,()->modeValue.is("GrimAC"));
    private final BoolValue lagbackCheck = new BoolValue("Lagback", false,()->modeValue.is("Watchdog"));
    public static boolean velocityOverrideSprint = false;
    private boolean velocityInput;
    private boolean attacked;
    private double reduceXZ;
    private int lastVelocityTick = 0;
    private S12PacketEntityVelocity velocityPacket;
    private boolean velocityTick;
    private int lagbackTimes = 0;
    private long lastLagbackTime = System.currentTimeMillis();
    private final CopyOnWriteArrayList<Packet> packetsQueue = new CopyOnWriteArrayList();
    public Velocity() {
        super("Velocity","反击退", Category.Combat);
    }


    private boolean grim_1_17Velocity;
    private int flags;

    @Override
    public void onEnable() {
        this.velocityInput = false;
        this.attacked = false;
        this.packetsQueue.clear();
    }


    @EventTarget
    public void onUpdate(EventLivingUpdate event) {
        setSuffix(modeValue.get());
        if (modeValue.is("GrimAC")) {
            if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
                if (this.velocityInput) {
                    if (this.attacked) {
                        EntityPlayerSP var10000 = mc.thePlayer;
                        var10000.motionX *= this.reduceXZ;
                        var10000 = mc.thePlayer;
                        var10000.motionZ *= this.reduceXZ;
                        this.attacked = false;
                    }

                    if (mc.thePlayer.hurtTime == 0) {
                        this.velocityInput = false;
                    }
                }
            } else if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10);
                mc.thePlayer.setSprinting(false);
            }
        }
    }
    @EventTarget
    public void onVel(VelocityEvent event){
        switch (this.modeValue.get()) {
            case "Packet cancel": {
                this.velocityTick = true;
            }
        }
    }
    @EventTarget
    public void onTick(EventTick event){
        switch (this.modeValue.get()) {
            case "Packet cancel": {
                if (this.velocityTick || this.packetsQueue.isEmpty()) break;
                for (Packet p : this.packetsQueue) {
                    PacketUtil.sendPacket(p);
                }
                this.packetsQueue.clear();
                break;
            }
        }
    }
    @EventTarget
    public void onMotion(EventMotion event){
        switch (this.modeValue.get()) {
            case "Packet cancel": {
                if (!this.velocityTick) break;
                this.packetsQueue.clear();
                this.velocityTick = false;
                break;
            }
        }
    }
    //Player
    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getEventType() == EventPacket.EventState.RECEIVE) {
            final Packet<?> packet = event.getPacket();
            if (packet instanceof S12PacketEntityVelocity) {


                if (modeValue.is("Watchdog")) {
                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        lastVelocityTick = mc.thePlayer.ticksExisted;
                        event.setCancelled(true);
                        if (mc.thePlayer.onGround || ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D < .2 || ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D > .41995) {
                            mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D;
                        }

                        DebugUtil.print("§cKnockback tick: " + mc.thePlayer.ticksExisted);

                    }
                }
            }

            if (modeValue.is("GrimAC")) {
                if (mc.thePlayer != null) {
                    if (event.getPacket() instanceof S12PacketEntityVelocity) {
                        if (mc.thePlayer.isDead) {
                            return;
                        }

                        if (mc.currentScreen instanceof GuiGameOver) {
                            return;
                        }

                        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) {
                            return;
                        }

                        if (mc.thePlayer.isOnLadder()) {
                            return;
                        }
                    }
                    if (event.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                        S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) event.getPacket();
                        double horizontalStrength = (new Vector2d((double) s12.getMotionX(), (double) s12.getMotionZ())).length();
                        int horizontalStrength2 = (int) Math.floor((new Vector2d((double) s12.getMotionX(), (double) s12.getMotionZ())).length());
                        if (horizontalStrength <= 1000.0) {
                            return;
                        }


                        this.velocityInput = true;
                        this.velocityPacket = s12;
                        this.attacked = false;
                        Entity entity = null;
                        this.reduceXZ = 1.0;
                        MovingObjectPosition result = RaytraceUtil.rayCast(Client.instance.getRotationManager().lastRotation, 3.2, 0.0F, mc.thePlayer, true);
                        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            entity = result.entityHit;
                        }

                        EntityLivingBase target;
                        if (entity == null && (target = (EntityLivingBase) KillAura.target) != null && target.getDistanceSqToEntity(mc.thePlayer) <= 9.5) {
                            entity = KillAura.target;
                        }

                        if (entity == null) {
                            return;
                        }

                        boolean state = mc.thePlayer.serverSprintState;
                        if (!state) {
                            PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        }

                        Client.instance.getEventManager().call(new EventAttack(entity, true));
                        Client.instance.getEventManager().call(new EventAttack(entity, false));
                        int count = (Boolean) this.bwValue.getValue() ? 6 : 12;

                        for (int i = 1; i <= count; ++i) {
                            AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                        }

                        velocityOverrideSprint = true;
                        mc.thePlayer.serverSprintState = true;
                        mc.thePlayer.setSprinting(true);
                        this.attacked = true;
                        this.reduceXZ = this.getMotion(this.velocityPacket);
                    }
                }

            }
            if (packet instanceof S08PacketPlayerPosLook && modeValue.is("Watchdog") && mc.thePlayer.ticksExisted >= 40 && mc.thePlayer.ticksExisted - lastVelocityTick <= 20) {
                if (System.currentTimeMillis() - lastLagbackTime <= 4000) {
                    lagbackTimes += 1;
                } else {
                    lagbackTimes = 1;
                }
                lastLagbackTime = System.currentTimeMillis();
            }
        }
    }
    private double getMotion(S12PacketEntityVelocity packetEntityVelocity) {
        double strength = (new Vec3((double) packetEntityVelocity.getMotionX(), (double) packetEntityVelocity.getMotionY(), (double) packetEntityVelocity.getMotionZ())).lengthVector();
        double motionNoXZ = strength >= 20000.0 ? (mc.thePlayer.onGround ? 0.05425 : 0.065) : (strength >= 5000.0 ? (mc.thePlayer.onGround ? 0.01625 : 0.0452) : 0.0075);
        return motionNoXZ;
    }
}
