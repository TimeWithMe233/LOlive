/*
 * Decompiled with CFR 0.152.
 */
package dev.olive.module.impl.move;

import dev.olive.Client;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.utils.DebugUtil;

import dev.olive.utils.math.MathUtils;
import dev.olive.utils.player.MovementUtils;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.utils.player.RotationComponent;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import java.util.Objects;

public class Speed
        extends Module {

    public Speed() {
        super("Speed","速度", Category.Movement);
    }
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Vanilla","Watchdog", "EntityCollide", "BlocksMC", "Intave", "NCP", "Miniblox"}, "Watchdog");
    private final ModeValue wdMode = new ModeValue("Watchdog Mode", new String[]{"Fast", "Glide","Ground Test"}, "Basic", () -> mode.is("Watchdog"));
    private final BoolValue fastFall = new BoolValue("Fast Fall", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue hurtTimeCheck = new BoolValue("Hurt Time Check", false, () -> mode.is("Watchdog") && (wdMode.is("Fast") && fastFall.get()));
    private final ModeValue wdFastFallMode = new ModeValue("Fast Fall Mode", new String[]{"7 Tick","8 Tick Strafe","8 Tick Fast","9 Tick"}, "8 Tick", () -> mode.is("Watchdog") && fastFall.get());
    private final BoolValue disableWhileScaffold = new BoolValue("Disable While Scaffold", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue frictionOverride = new BoolValue("Friction Override", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue extraStrafe = new BoolValue("Extra Strafe", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue expand = new BoolValue("More Expand", false, () -> Objects.equals(mode.get(), "EntityCollide"));
    private final BoolValue ignoreDamage = new BoolValue("Ignore Damage", true, () -> Objects.equals(mode.get(), "EntityCollide"));
    private final BoolValue pullDown = new BoolValue("Pull Down", true, () -> Objects.equals(mode.get(), "NCP"));
    private final NumberValue onTick = new NumberValue("On Tick", 5, 1, 10, 1, () -> Objects.equals(mode.get(), "NCP") && pullDown.get());
    private final BoolValue onHurt = new BoolValue("On Hurt", true, () -> Objects.equals(mode.get(), "NCP") && pullDown.get());
    private final BoolValue airBoost = new BoolValue("Air Boost", true, () -> Objects.equals(mode.get(), "NCP"));
    private final BoolValue damageBoost = new BoolValue("Damage Boost", false, () -> Objects.equals(mode.get(), "NCP"));
    private final NumberValue mTicks = new NumberValue("Ticks", 5, 1, 6, 1, () -> Objects.equals(mode.get(), "Miniblox"));
    public final BoolValue noBob = new BoolValue("No Bob", true);
    private final BoolValue forceStop = new BoolValue("Force Stop", true);
    private final BoolValue lagBackCheck = new BoolValue("Lag Back Check", true);
    private final BoolValue liquidCheck = new BoolValue("Liquid Check", true);
    private final BoolValue guiCheck = new BoolValue("Gui Check", true);
    private final BoolValue printOffGroundTicks = new BoolValue("Print Off Ground Ticks", true);
    private final NumberValue vanilla = new NumberValue("Vanilla Speed", 0.5f,0.05f,2, 0.05f, () -> Objects.equals(mode.get(), "Vanilla"));
    private final BoolValue vanillaPullDown = new BoolValue("Pull Down", true, () -> mode.is("Vanilla"));
    private final NumberValue vanillaPullDownAmount = new NumberValue("Vanilla Pull Down", 0.5f,0.05f,2, 0.05f, () -> Objects.equals(mode.get(), "Vanilla") && vanillaPullDown.get());
    private boolean disable;
    private boolean disable3;
    private int boostTicks;
    private boolean recentlyCollided;
    private boolean slab;
    private boolean stopVelocity;
    public boolean couldStrafe;
    private double speed;
    private int ticksSinceTeleport;
    private boolean valued;

    @Override
    public void onEnable() {
        if (mode.is("Watchdog")) {
            if (wdMode.is("Glide")) {
                speed = 0.28;
            }
            slab = false;

            disable3 = false;

            valued = false;
        }
    }

    @Override
    public void onDisable() {
        disable = false;
        couldStrafe = false;
        if(forceStop.get()){
            MovementUtils.stopXZ();
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        setSuffix(mode.get());
        ticksSinceTeleport++;
        if(liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (printOffGroundTicks.get())
            DebugUtil.log(mc.thePlayer.offGroundTicks + "Tick");

        switch (mode.get()) {
            case "Miniblox": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.thePlayer.jump();
                }

                switch (mc.thePlayer.offGroundTicks) {
                    case 1: {
                        Double v = mTicks.get();
                        if (v == 1) {
                            mc.thePlayer.motionY -= 0.76;
                        } else if (v == 2) {
                            mc.thePlayer.motionY -= 0.52;
                        } else if (v == 3) {
                            mc.thePlayer.motionY -= 0.452335182447;
                        } else if (v == 4) {
                            mc.thePlayer.motionY -= 0.322335182447;
                        } else if (v == 5) {
                            mc.thePlayer.motionY -= 0.232335182447;
                        } else if (v == 6) {
                            mc.thePlayer.motionY -= 0.162335182447;
                        }
                    }
                    break;

                    case 3: {
                        mc.thePlayer.motionY -= 0.1523351824467155;
                    }
                    break;
                }
            }
            break;

            case "NCP": {
                if (mc.thePlayer.offGroundTicks == onTick.get() && pullDown.get()) {
                    MovementUtils.strafe();
                    mc.thePlayer.motionY -= 0.1523351824467155;
                }

                if (onHurt.get() && mc.thePlayer.hurtTime >= 5 && mc.thePlayer.motionY >= 0) {
                    mc.thePlayer.motionY -= 0.1;
                }

                if (airBoost.get() && MovementUtils.isMoving()) {
                    mc.thePlayer.motionX *= 1f + 0.00718;
                    mc.thePlayer.motionZ *= 1f + 0.00718;
                }
            }
            break;

            case "Vanilla": {
                MovementUtils.strafe((float) (1 * vanilla.get()));
                couldStrafe = true;

                if (vanillaPullDown.get()) {
                    mc.thePlayer.motionY = -vanillaPullDownAmount.get();
                }
            }
            break;

            case "Intave": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.thePlayer.jump();
                }

                if (mc.thePlayer.motionY > 0.03 && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 1f + 0.003;
                    mc.thePlayer.motionZ *= 1f + 0.003;
                }
            }
            break;

            case "EntityCollide": {
                couldStrafe = false;
                if (mc.thePlayer.hurtTime <= 1) {
                    stopVelocity = false;
                }

                if (stopVelocity && !ignoreDamage.get()) {
                    return;
                }

                if (!MovementUtils.isMoving())
                    return;

                int collisions = 0;
                AxisAlignedBB box = expand.get() ? mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0)
                        : mc.thePlayer.getEntityBoundingBox().expand(0.8, 0.8, 0.8);
                for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                    AxisAlignedBB entityBox = entity.getEntityBoundingBox();
                    if (canCauseSpeed(entity) && box.intersectsWith(entityBox)) {
                        collisions++;
                    }
                }

                double yaw = Math.toRadians(RotationComponent.shouldRotate() ? RotationComponent.currentRotation[0] : mc.thePlayer.rotationYaw);

                double boost = 0.078 * collisions;
                mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
            }
            break;

            case "Watchdog":
                if(wdMode.is("Fast")) {
                    if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                        mc.thePlayer.jump();
                        if(!Client.instance.getModuleManager().getModule(Scaffold.class).state)
                            MovementUtils.strafe((float) (0.47 + MovementUtils.getSpeedEffect() * 0.042));
                        couldStrafe = true;
                    }
                }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {

        if (liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (Client.instance.getModuleManager().getModule(Scaffold.class).state)
            return;

        switch (mode.get()) {
            case "Miniblox": {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        Double v = mTicks.get();
                        if (v == 1) {
                            MovementUtils.strafe(0.07F);
                        } else if (v == 2) {
                            MovementUtils.strafe(0.08f);
                        } else if (v == 3) {
                            MovementUtils.strafe(0.09f);
                        } else if (v == 4) {
                            MovementUtils.strafe(0.1f);
                        } else if (v == 5) {
                            MovementUtils.strafe(0.115f);
                        } else if (v == 6) {
                            MovementUtils.strafe(0.13f);
                        }
                    } else {
                        MovementUtils.strafe(0.35f);
                    }
                }
            }
            break;

            case "NCP": {
                if (MovementUtils.isMoving()) {
                    couldStrafe = true;
                    MovementUtils.strafe();
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                        MovementUtils.strafe(0.48f + MovementUtils.getSpeedEffect() * 0.07f);
                    }
                }

                if (damageBoost.get() && mc.thePlayer.hurtTime > 0) {
                    MovementUtils.strafe(Math.max(MovementUtils.getSpeed(), 0.5f));
                }
            }
            break;

            case "Vanilla": {
                if (MovementUtils.isMoving()) {
                    couldStrafe = true;
                    MovementUtils.strafe();
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
            }
            break;

            case "Watchdog":
                if (event.isPre()) {
                    if (fastFall.get()) {
                        if (mc.thePlayer.isCollidedHorizontally || ticksSinceTeleport < 2) {
                            recentlyCollided = true;
                            boostTicks = mc.thePlayer.ticksExisted + 9;
                        }
                        if (!mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.ticksExisted > boostTicks)) {
                            recentlyCollided = false;
                        }

                        if (mc.thePlayer.onGround) {
                            disable3 = false;
                        }
                        if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) {
                            disable = false;
                        }

                        if (mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && PlayerUtil.isBlockOver(2.0)) {
                            disable = true;
                        }

                        double posY = event.getY();
                        if (Math.abs(posY - Math.round(posY)) > 0.03 && mc.thePlayer.onGround) {
                            slab = true;
                        } else if (mc.thePlayer.onGround) {
                            slab = false;
                        }
                    }

                    if (fastFall.isAvailable() && fastFall.get()) {

                        if (mc.thePlayer.isInWater() ||
                                mc.thePlayer.isInWeb ||
                                mc.thePlayer.isInLava() ||
                                hurtTimeCheck.get() && mc.thePlayer.hurtTime > 0
                        ) {
                            disable = true;
                            return;
                        }

                        if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) {
                            disable = false;
                        }

                        if (mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && PlayerUtil.isBlockOver(2.0)) {
                            disable = true;
                        }
                    }

                    if (wdMode.is("Ground Test")) {
                        if (valued) {
                            if (mc.thePlayer.onGround) {
                                event.setY(event.getY() + 1E-13F);
                                mc.thePlayer.motionX *= 1.14 - MovementUtils.getSpeedEffect() * .01;
                                mc.thePlayer.motionZ *= 1.14 - MovementUtils.getSpeedEffect() * .01;
                                MovementUtils.strafe();
                                couldStrafe = true;
                            }
                        }
                    }
                }

                break;
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {

        if (liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (mode.get().equals("Watchdog") && wdMode.get().equals("Fast") && (mc.thePlayer.isInWater() || mc.thePlayer.isInWeb || mc.thePlayer.isInLava())) {
            disable = true;
            return;
        }
        if (mode.get().equals("Watchdog")) {

            switch (wdMode.get()) {
                case "Glide":
                    if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                        MovementUtils.strafe(MovementUtils.getAllowedHorizontalDistance());
                        mc.thePlayer.jump();
                    }

                    if (mc.thePlayer.onGround) {
                        speed = 1.0F;
                    }

                    final int[] allowedAirTicks = new int[]{10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29};

                    if (!(mc.theWorld.getBlockState(mc.thePlayer.getPosition().add(0, -0.25, 0)).getBlock() instanceof BlockAir)) {
                        for (final int allowedAirTick : allowedAirTicks) {
                            if (mc.thePlayer.offGroundTicks == allowedAirTick && allowedAirTick <= 11) {
                                mc.thePlayer.motionY = 0;
                                MovementUtils.strafe((float) (MovementUtils.getAllowedHorizontalDistance() * speed));
                                couldStrafe = true;

                                speed *= 0.98F;

                            }
                        }
                    }
                    break;
                case "Fast":

                    if (!disable && fastFall.get() && (disableWhileScaffold.get() && !Client.instance.getModuleManager().getModule(Scaffold.class).state || !disableWhileScaffold.get())) {

                        switch (wdFastFallMode.get()) {
                            case "7 Tick", "8 Tick Strafe":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 1:
                                        mc.thePlayer.motionY += 0.057f;
                                        break;
                                    case 3:
                                        mc.thePlayer.motionY -= 0.1309f;
                                        break;
                                    case 4:
                                        mc.thePlayer.motionY -= 0.2;
                                        break;
                                }
                                break;

                            case "8 Tick Fast":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 3:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                        break;
                                    case 5:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.1913;
                                        break;
                                }
                                break;
                            case "9 Tick":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 3:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                        break;
                                    case 5:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.16874;
                                        break;
                                }
                                break;
                        }

                    }

                    if (mc.thePlayer.offGroundTicks == 1 && !disable) {
                        if (Client.instance.getModuleManager().getModule(Scaffold.class).state) {
                                MovementUtils.strafe(0.3f);
                        } else {
                            MovementUtils.strafe((float) Math.max(MovementUtils.getSpeed(), 0.33f + MovementUtils.getSpeedEffect() * 0.075));
                            couldStrafe = true;
                        }
                    }

                    if (mc.thePlayer.offGroundTicks == 2 && !disable && extraStrafe.get()) {
                        double motionX3 = mc.thePlayer.motionX;
                        double motionZ3 = mc.thePlayer.motionZ;
                        mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 1 + motionZ3 * 2) / 3;
                        mc.thePlayer.motionX = (mc.thePlayer.motionX * 1 + motionX3 * 2) / 3;
                    }

                    if (mc.thePlayer.offGroundTicks == 6 && wdFastFallMode.is("8 Tick Strafe") && !disable && PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0) != Blocks.air && PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0).isFullBlock() && (disableWhileScaffold.get() && !Client.instance.getModuleManager().getModule(Scaffold.class).state || !disableWhileScaffold.get())) {
                        mc.thePlayer.motionY += 0.0754;
                        MovementUtils.strafe();
                        couldStrafe = true;
                    }

                    if ((mc.thePlayer.motionX == 0 || mc.thePlayer.motionZ == 0) && !disable && (!recentlyCollided && mc.thePlayer.isPotionActive(Potion.moveSpeed)) && !Client.instance.getModuleManager().getModule(Scaffold.class).state) {
                        MovementUtils.strafe();
                        couldStrafe = true;
                    }

                    if (mc.thePlayer.offGroundTicks < 7 && (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) && mc.thePlayer.isPotionActive(Potion.moveSpeed) && !slab) {
                        boostTicks = mc.thePlayer.ticksExisted + 9;
                        recentlyCollided = true;
                    }

                    if (mc.thePlayer.offGroundTicks == 7 && !disable && (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 2, 0) != Blocks.air) && !Client.instance.getModuleManager().getModule(Scaffold.class).state) {
                        MovementUtils.strafe(MovementUtils.getSpeed());
                        couldStrafe = true;
                    }

                    if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air && mc.thePlayer.offGroundTicks > 5 && !disable3) {
                        MovementUtils.strafe();
                        couldStrafe = true;
                        disable3 = true;
                    }

                    double speed2 = Math.hypot((mc.thePlayer.motionX - (mc.thePlayer.lastTickPosX - mc.thePlayer.lastLastTickPosX)), (mc.thePlayer.motionZ - (mc.thePlayer.lastTickPosZ - mc.thePlayer.lastLastTickPosZ)));
                    if (speed2 < .0125 && frictionOverride.get()) {
                        MovementUtils.strafe();
                        couldStrafe = true;
                    }

                    break;

                case "Ground Test":
                    if(!valued && mc.thePlayer.onGround){
                        mc.thePlayer.jump();
                        valued = true;
                    }
                    break;
            }
        }
    }

    @EventTarget
    public void onPostStrafe(PostStrafeEvent event) {
        if (mode.is("Watchdog") && wdMode.is("Fast")) {
            if (extraStrafe.get()) {
                double attempt_angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtils.getDirection()));
                double movement_angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90);
                if (MathUtils.wrappedDifference(attempt_angle, movement_angle) > 90) {
                    MovementUtils.strafe(MovementUtils.getSpeed(), (float) movement_angle - 180);
                }
            }
        }
    }

    @EventTarget
    public void onMove(EventMove event){

        if (!liquidCheck.get() || (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava())) {
            guiCheck.get();
        }

    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            ticksSinceTeleport = 0;
            if (lagBackCheck.get()) {
                toggle();
            }
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event){
        if(!mode.is("EntityCollide"))
            if(mc.thePlayer.onGround)
                event.setJump(false);
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.thePlayer && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
    }
}

