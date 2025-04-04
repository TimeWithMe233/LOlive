package dev.olive.module.impl.world;


import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.module.impl.player.BalanceTimer;
import dev.olive.module.impl.render.HUD;
import dev.olive.ui.font.FontManager;
import dev.olive.ui.font.RapeMasterFontManager;
import dev.olive.utils.*;
import dev.olive.utils.ScaffoldUtils;
import dev.olive.utils.math.MathUtils;
import dev.olive.utils.math.TimerUtils;
import dev.olive.utils.player.*;
import dev.olive.utils.render.ColorUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ColorValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static dev.olive.utils.player.MoveUtil.isMoving;


public class Scaffold extends Module {
    private static final ModeValue modeValue = new ModeValue("Mode", new String[]{"Normal","Watchdog"}, "Normal");

    private final ModeValue counter = new ModeValue("Counter", new String[]{"Naven", "Easy"}, "Naven",() -> (modeValue.get().equals("Normal")));
    private final BoolValue swing = new BoolValue("Swing", true,() -> (modeValue.get().equals("Normal")));
    private final BoolValue towerMove = new BoolValue("Tower", false,() -> (modeValue.get().equals("Normal")))/*.visible(() -> false)*/;
    private final BoolValue eagle = new BoolValue("Eagle", false,() -> (modeValue.get().equals("Normal")));
    private final BoolValue telly = new BoolValue("Telly", true,() -> (modeValue.get().equals("Normal")));
    private final BoolValue moveFix = new BoolValue("MoveFix", false,() -> (modeValue.get().equals("Normal")));
    private final BoolValue bugFlyValue = new BoolValue("BugFly", false, Client::getIsBeta);
    private final BoolValue swap = new BoolValue("Swap", true,() -> (modeValue.get().equals("Normal")));
    private final BoolValue keepYValue = new BoolValue("Keep Y", false,() -> (modeValue.get().equals("Normal")));
    private final BoolValue upValue = new BoolValue("Up", false, () -> (telly.getValue() && !keepYValue.getValue()));
    private final BoolValue smoothCamera = new BoolValue("SmoothCamera", false,() -> (modeValue.get().equals("Normal")));
    private final ModeValue tellymode = new ModeValue("TellyMode", new String[]{"HYTBW", "Ming"}, "Grim",() -> (modeValue.get().equals("Normal")));
    public final BoolValue blockPlaceESP = new BoolValue("Block Place ESP", true,() -> (modeValue.get().equals("Normal")));

    private final BoolValue rotations = new BoolValue("Rotations", true,() -> (modeValue.get().equals("Watchdog")));
    private final ModeValue rotationMode = new ModeValue("Rotation Mode",new String[]{"Watchdog", "NCP", "Backwards"} ,"Watchdog" ,() -> (modeValue.get().equals("Watchdog")));
    public static ModeValue sprintMode = new ModeValue("Sprint Mode", new String[]{"Vanilla", "Watchdog", "Legit", "None"} ,"Vanilla",() -> (modeValue.get().equals("Watchdog")));
    public static ModeValue watchdogSprint = new ModeValue("Watchdog Mode", new String[]{"Jump SameY"}, "Jump SameY",() -> sprintMode.is("Watchdog") && modeValue.get().equals("Watchdog"));
    public static ModeValue towerMode = new ModeValue("Tower Mode",new String[]{"Watchdog", "Vanilla", "NCP", "Legit"} , "Watchdog",() -> (modeValue.get().equals("Watchdog")));
    private final Animation anim = new DecelerateAnimation(250, 1.0);
    private final ArrayList<PlacedBlock> blockPlaceList = new ArrayList();
    private float y;
    private int idkTick = 0, towerTick = 0, slot = 0;
    private boolean onGround = false;
    private BlockPos data;
    private EnumFacing enumFacing;
    public static boolean up, keepY, canTellyPlace;
    private static final List<Block> invalidBlocks = Arrays.asList(Blocks.wall_banner, Blocks.waterlily, Blocks.ender_chest, Blocks.standing_banner, Blocks.dropper, Blocks.enchanting_table, Blocks.furnace, Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.sand, Blocks.snow_layer, Blocks.torch, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower, Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder, Blocks.web, Blocks.tnt);
    public static double keepYCoord;
    public double lastOnGroundPosY;
    public int lastSlot;
    public boolean flyFlag = false;
    public LinkedList<List<Packet<?>>> packets = new LinkedList<>();
    public int c08PacketSize = 0;
    public boolean packetHandlerFlag = true;
    public float[] lastRotation = null;
    public boolean placeFlag = false;
    public boolean lastKeepYMode = false;
    public boolean lastJumpMode = false;
    public int placedAfterTower = 0;
    public boolean wasTowering;
    public int slowTicks;
    public int ticks;
    public int tickCounter;
    public float angle;
    public double targetZ;
    public boolean targetCalculated;
    public int ticks2;
    public int lastY;
    public float keepYaw;
    public dev.olive.utils.player.ScaffoldUtils.BlockCache blockCache, lastBlockCache;
    public final TimerUtils delayTimer = new TimerUtils();
    public float[] cachedRots = new float[2];
    public static double lastGroundY = 0;
    public static double moveTicks = 0;
    public static double startY = 0;

    private boolean pre;

    float yaw = 0;
    public Scaffold() {
        super("Scaffold", "自动搭路",Category.World);
        /*towerMove.setValue(false);*/
    }

    public void sendPacketHook(Packet packet) {
        if (packet instanceof C09PacketHeldItemChange) {
            if (((C09PacketHeldItemChange) packet).getSlotId() == lastSlot) {
                return;
            }
            mc.getNetHandler().addToSendQueue(packet);
            lastSlot = ((C09PacketHeldItemChange) packet).getSlotId();
        }
    }

    public double getYLevel() {
        if (modeValue.is("WatchdogKeepY")) {
            if (mc.gameSettings.keyBindJump.pressed) return mc.thePlayer.posY - 1;
            if (mc.thePlayer.offGroundTicks == 4) {
                return mc.thePlayer.posY - 1;
            } else {
                return keepYCoord;
            }
        }
        if (!keepY) {
            return mc.thePlayer.posY - 1.0;
        }

        return !isMoving() ? mc.thePlayer.posY - 1.0 : keepYCoord;
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        } else {
            y += 0.08;
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }

    private void sendTick(List<Packet<?>> tick) {
        if (mc.getNetHandler() != null) {
            tick.forEach(packet -> {
                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    c08PacketSize -= 1;
                }
                mc.getNetHandler().addToSendQueue(packet, true);
            });
        }
    }

    @Override
    public void onEnable() {
        if (modeValue.get().equals("Watchdog")) {
            lastBlockCache = null;
            if (mc.thePlayer != null) {
                slot = mc.thePlayer.inventory.currentItem;
                if (mc.thePlayer.isSprinting() && sprintMode.isNotCurrentMode("None") && sprintMode.isNotCurrentMode("Vanilla") && sprintMode.isNotCurrentMode("Legit")) {
                    PacketUtil.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                }
            }

            assert mc.thePlayer != null;
            targetZ = mc.thePlayer.posZ;
            tickCounter = 0;
            angle = mc.thePlayer.rotationYaw;

            if (!mc.thePlayer.onGround) {
                ticks = 100;
            }
            y = 80;
        } else {


            idkTick = 5;
            placedAfterTower = 0;

            if (mc.thePlayer == null) return;

            mc.thePlayer.setSprinting(!canTellyPlace);
            mc.gameSettings.keyBindSprint.pressed = !canTellyPlace;
            canTellyPlace = false;
            this.data = null;
            this.slot = -1;
            keepY = keepYValue.getValue();
            up = upValue.getValue();
            lastSlot = mc.thePlayer.inventory.currentItem;
            flyFlag = false;
            c08PacketSize = 0;
            packetHandlerFlag = true;
            lastOnGroundPosY = mc.thePlayer.posY;
            lastJumpMode = modeValue.is("WatchdogJump");
            lastKeepYMode = modeValue.is("WatchdogKeepY");
            targetCalculated = false;
            keepYaw = PlayerUtil.getMoveYaw(mc.thePlayer.rotationYaw) - 180f;
        }
    }
    public int getBlocksAmount() {
        int amount = 0;
        for (int i = 36; i < 45; ++i) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack == null || !(itemStack.getItem() instanceof ItemBlock)) continue;
            Block block = ((ItemBlock)itemStack.getItem()).getBlock();
            if (mc.thePlayer.getHeldItem() != itemStack && InventoryUtil.invalidBlocks.contains(block)) continue;
            amount += itemStack.stackSize;
        }
        return amount;
    }
    @EventTarget
    public void onJump(EventJump event) {
        if (modeValue.get().equals("Watchdog")) {
            if (mc.gameSettings.keyBindJump.pressed && (towerMode.is("Watchdog") || towerMode.isCurrentMode("Test")) && MoveUtil.isMoving())
                event.setCancelled(true);
        }
    }
    @Override
    public void onDisable() {
        if(modeValue.get().equals("Watchdog")) {
            if (mc.thePlayer != null) {
                if (slot != mc.thePlayer.inventory.currentItem)
                    PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            }
            startY = 0;
            lastGroundY = 0;
            mc.timer.timerSpeed = 1;
            mc.gameSettings.keyBindSneak.pressed = false;
        } else {
            if (mc.thePlayer == null) return;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            if (slot != mc.thePlayer.inventory.currentItem)
                sendPacketHook(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            if (bugFlyValue.getValue()) {
                packets.forEach(this::sendTick);
                packets.clear();
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem + 1), true);
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem), true);
            }
        }

    }

    @EventTarget
    private void motionEventHandler(EventMotion event) {
        if (modeValue.get().equals("Watchdog")) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                keepYCoord = mc.thePlayer.posY - 1;
                startY = mc.thePlayer.posY;
            }

            if (mc.thePlayer.onGround)
                lastGroundY = mc.thePlayer.posY;
            if (startY == 0) {
                if (mc.thePlayer.onGround)
                    startY = mc.thePlayer.posY;
                else
                    startY = keepYCoord;
            }

            if (sprintMode.isCurrentMode("Legit")) {
                if (Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(yaw)) > 90) {
                    mc.gameSettings.keyBindSprint.pressed = false;
                    mc.thePlayer.setSprinting(false);
                } else {
                    mc.gameSettings.keyBindSprint.pressed = true;
                    mc.thePlayer.setSprinting(true);
                }
            } else if (sprintMode.isCurrentMode("None")) {
                mc.gameSettings.keyBindSprint.pressed = false;
                mc.thePlayer.setSprinting(false);
            } else {
                mc.gameSettings.keyBindSprint.pressed = true;
                mc.thePlayer.setSprinting(true);
            }
            if (sprintMode.isCurrentMode("Watchdog") && !mc.gameSettings.keyBindJump.pressed) {
                if (watchdogSprint.isCurrentMode("Jump SameY")) {
                    if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                        mc.thePlayer.jump();
                        MoveUtil.strafe(0.48);
                    }
                }
            }

            if (towerMode.isCurrentMode("Watchdog")) {
                if (mc.gameSettings.keyBindJump.pressed) {
                    if (mc.thePlayer.onGround) onGround = true;
                } else onGround = false;

            }
            // Rotations
            if (rotations.getValue()) {
                float[] rotations = new float[]{0, 0};
                switch (rotationMode.getValue()) {
                    case "Watchdog":
                        rotations = new float[]{MoveUtil.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, y};
                        if (mc.thePlayer.onGround && !MoveUtil.isMoving()) {
                            if ((blockCache = dev.olive.utils.player.ScaffoldUtils.getBlockInfo()) == null) {
                                blockCache = lastBlockCache;
                            }

                            if (this.blockCache != null && (mc.thePlayer.ticksExisted % 3 == 0 || mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, dev.olive.utils.player.ScaffoldUtils.getYLevel(), mc.thePlayer.posZ)).getBlock() == Blocks.air)) {
                                this.cachedRots = RotationUtil.getRotations(this.blockCache.getPosition(), this.blockCache.getFacing());
                            }
                            rotations = cachedRots;
                            yaw = rotations[0];
                            Client.instance.rotationManager.setRotation(new Vector2f(rotations[0], rotations[1]), 360f, false);
                            break;
                        }
                        Client.instance.rotationManager.setRotation(new Vector2f(rotations[0], rotations[1]), 360f, false);
                        break;
                    case "NCP":
                        if ((blockCache = dev.olive.utils.player.ScaffoldUtils.getBlockInfo()) == null) {
                            blockCache = lastBlockCache;
                        }
                        if (blockCache != null && (mc.thePlayer.ticksExisted % 3 == 0
                                || mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, dev.olive.utils.player.ScaffoldUtils.getYLevel(), mc.thePlayer.posZ)).getBlock() == Blocks.air)) {
                            cachedRots = RotationUtil.getRotations(blockCache.getPosition(), blockCache.getFacing());
                        }
                        rotations = cachedRots;
                        yaw = rotations[0];
                        Client.instance.rotationManager.setRotation(new Vector2f(rotations[0], rotations[1]), 360f, false);
                        break;
                    case "Backwards":
                        rotations = new float[]{MoveUtil.getMoveYaw(mc.thePlayer.rotationYaw) - 180, 77};
                        yaw = rotations[0];
                        Client.instance.rotationManager.setRotation(new Vector2f(rotations[0], rotations[1]), 360f, false);
                        break;
                }
                yaw = rotations[0];
            }

            // Save ground Y level for keep Y
            if (mc.thePlayer.onGround) {
                keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
            }

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                switch (towerMode.getValue()) {
                    case "Vanilla":
                        mc.thePlayer.motionY = 0.42f;
                        break;
                    case "Watchdog": {
                        if (event.isPre() && onGround) {
                            if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                                angle = mc.thePlayer.rotationYaw;
                                ticks = 100;
                                return;
                            }
                            tickCounter++;
                            ticks++;
                            if (tickCounter >= 35) {
                                tickCounter = 0; // Reset the counter
                            }
                            if (mc.thePlayer.onGround) {
                                ticks = 0;
                            }
                            if (!MoveUtil.isMoving()) {
                                if (!targetCalculated) {
                                    // Calculate the targetZ position only once
                                    targetZ = Math.floor(mc.thePlayer.posZ) + 0.99999999999998;
                                    targetCalculated = true;
                                }
                                ticks2++;

                                if (Math.abs(mc.thePlayer.posY) >= 1) {
                                    if (ticks2 == 1) {
                                        // Move to the middle position
                                        MoveUtil.stop();
                                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY,
                                                (mc.thePlayer.posZ + targetZ) / 2);
                                    } else if (ticks2 == 2) {
                                        // Move to the final target position after 2 ticks
                                        MoveUtil.stop();
                                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, targetZ);
//                                        doSidePlacement();
                                        ticks2 = 0; // Reset the tick counter after reaching the final position
                                        targetCalculated = false; // Reset the flag for the next cycle
                                    }
                                } else {
                                    // Reset ticks2 if the Y position condition is not met
                                    ticks2 = 0;
                                    targetCalculated = false; // Reset the flag if the condition is not met
                                }
                            }

                            float step = ticks == 1 ? 90 : 0;

                            if (MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - angle) < step) {
                                angle = mc.thePlayer.rotationYaw;
                            } else if (MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - angle) < 0) {
                                angle -= step;
                            } else if (MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - angle) > 0) {
                                angle += step;
                            }

                            mc.thePlayer.movementYaw = angle;

                            if (tickCounter <= 20) {
                                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                    MoveUtil.strafe();

                                    switch (ticks) {
                                        case 0:
                                            if (mc.thePlayer.posY % 1 == 0) {
                                                event.setGround(true);
                                                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                                    mc.thePlayer.motionX *= .998765;
                                                    mc.thePlayer.motionZ *= .998765;
                                                } else {

                                                }
                                            }
                                            mc.thePlayer.motionX *= .985765;
                                            mc.thePlayer.motionZ *= .985765;
                                            mc.thePlayer.motionY = 0.42f;
                                            break;

                                        case 1:
                                            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                                mc.thePlayer.motionX *= .985765;
                                                mc.thePlayer.motionZ *= .985765;
                                            } else {

                                            }
                                            mc.thePlayer.motionY = 0.33;
                                            break;

                                        case 2:
                                            mc.thePlayer.motionX *= .985765;
                                            mc.thePlayer.motionZ *= .985765;
                                            mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                                            break;
                                    }
                                }
                            } else {
                                mc.thePlayer.motionX *= .985765;
                                mc.thePlayer.motionZ *= .985765;
                            }

                            if (ticks == 2) ticks = -1;
                        }
                    }
                    break;
                    case "NCP":
                        if (!MoveUtil.isMoving() || MoveUtil.getSpeed() < 0.16) {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.motionY = 0.42;
                            } else if (mc.thePlayer.motionY < 0.23) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX, (int) mc.thePlayer.posY, mc.thePlayer.posZ);
                                mc.thePlayer.motionY = 0.42;
                            }
                        }
                        break;
                }
            }

            // Setting Block Cache
            blockCache = dev.olive.utils.player.ScaffoldUtils.getBlockInfo();
            if (blockCache != null) {
                lastBlockCache = dev.olive.utils.player.ScaffoldUtils.getBlockInfo();
            } else {
                return;
            }

            if (mc.thePlayer.ticksExisted % 4 == 0) {
                pre = true;
            }
        } else {
            if (this.idkTick > 0) {
                --this.idkTick;
            }

            if (event.isPre()) {
                if (mc.thePlayer.onGround) {
                    lastOnGroundPosY = mc.thePlayer.posY;
                }
                if (smoothCamera.getValue() && !mc.gameSettings.keyBindJump.pressed && !modeValue.getValue().contains("Jump")) {
                    SmoothCameraComponent.setY(lastOnGroundPosY + ((telly.getValue() || modeValue.getValue().contains("KeepY")) ? 1.0 : 0.0));
                }
                if (eagle.getValue()) {
                    if (getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
                        if (mc.thePlayer.onGround) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                        }
                    } else if (mc.thePlayer.onGround) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    }
                }

                if (modeValue.is("WatchdogGround")) {
                    mc.thePlayer.setSprinting(false);
                    PlayerUtil.strafe(PlayerUtil.getSpeed() / 1.3f);
                }

                if (modeValue.is("WatchdogKeepY")) {
                    if (mc.thePlayer.onGround && PlayerUtil.isMoving()) {
                        mc.thePlayer.setSprinting(false);
                        MoveUtil.setMotion(0.47F);
                        mc.thePlayer.jump();
                    }
                    if (mc.thePlayer.offGroundTicks == 1 && PlayerUtil.isMoving()) {
                        MoveUtil.setMotion(0.30F);
                    }
                }
                if (this.getBlockCount() <= 5 && getAllBlockCount() > 5) {
                    int spoofSlot = this.getBestSpoofSlot();
                    this.getBlock(spoofSlot);
                }
                final ItemStack itemStack = switchToBlock();
                if (itemStack == null) return;

                if (modeValue.is("WatchdogJump")) {
                    mc.thePlayer.setSprinting(false);
                    if (mc.thePlayer.onGround && isMoving()) {
                        PlayerUtil.strafe(0.44);
                        mc.thePlayer.jump();
                    }
                }

                if (mc.gameSettings.keyBindJump.pressed && isMoving()) {
                    if (mc.thePlayer.onGround) {
                        onGround = true;
                    }
                } else {
                    onGround = false;
                }

                if (towerMove.getValue()) {
                    if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                        angle = mc.thePlayer.rotationYaw;
                        ticks = 100;
                        return;
                    }

                    tickCounter++;
                    ticks++;

                    if (tickCounter >= 23) {
                        tickCounter = 1; // Reset the counter
                        angle = mc.thePlayer.rotationYaw;
                        ticks = 100;
                    }

                    if (mc.thePlayer.onGround) {
                        ticks = 0;
                    }

                    if (!PlayerUtil.isMoving()) {
                        if (!targetCalculated) {
                            // Calculate the targetZ position only once
                            targetZ = Math.floor(mc.thePlayer.posZ) + 0.99999999999998;
                            targetCalculated = true;
                        }

                        ticks2++;

                        if (Math.abs(lastY - mc.thePlayer.posY) >= 1) {
                            if (ticks2 == 1) {
                                // Move to the middle position
                                PlayerUtil.stop();
                                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, (mc.thePlayer.posZ + targetZ) / 2);
                            } else if (ticks2 == 2) {
                                // Move to the final target position after 2 ticks
                                PlayerUtil.stop();
                                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, targetZ);
                                ticks2 = 0; // Reset the tick counter after reaching the final position
                                targetCalculated = false; // Reset the flag for the next cycle
                            }
                        } else {
                            // Reset ticks2 if the Y position condition is not met
                            ticks2 = 0;
                            targetCalculated = false; // Reset the flag if the condition is not met
                        }
                    } else {
                        return;
                    }

                    if (modeValue.is("WatchdogJump") || modeValue.is("WatchdogKeepY")) {
                        lastJumpMode = modeValue.is("WatchdogJump");
                        lastKeepYMode = modeValue.is("WatchdogKeepY");
                        modeValue.setValue("WatchdogGround");
                        placedAfterTower = 0;
                    }


                    float step = ticks == 1 ? 90 : 0;

                    if (MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - angle) < step) {
                        angle = mc.thePlayer.rotationYaw;
                    } else if (MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - angle) < 0) {
                        angle -= step;
                    } else if (MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - angle) > 0) {
                        angle += step;
                    }

                    mc.thePlayer.movementYaw = angle;

                    if (tickCounter < 20) {
                        PlayerUtil.strafe(.26);
                        if (mc.gameSettings.keyBindJump.isKeyDown()) {

//            getParent().startY = Math.floor(mc.thePlayer.posY);

                            switch (ticks) {
                                case 0:
                                    if (mc.thePlayer.posY % 1 == 0) {
                                        event.setGround(true);
                                    }
                                    mc.thePlayer.motionY = 0.42f;
                                    break;
                                case 1:
                                    mc.thePlayer.motionY = 0.33;
                                    break;
                                case 2:
                                    mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                                    break;
                            }
                        }
                    } else {

                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.motionY = 0.4196F;
                        } else if (mc.thePlayer.offGroundTicks == 3) {
                            mc.thePlayer.motionY = 0F;
                        }
                    }

                    if (ticks == 2) ticks = -1;
                }
            }

        }
    }


    @EventTarget
    private void jumpEventHandler(EventJump event) {
        if (modeValue.get().equals("Normal")) {
            if (mc.gameSettings.keyBindJump.pressed && towerMove.getValue() && isMoving()) {
                event.setCancelled(true);
            }
        }
    }

    ;

    @EventTarget
    private void strafeEvent(EventStrafe event) {
        if (modeValue.get().equals("Normal")) {
            if (mc.thePlayer == null || mc.theWorld == null) return;
            if ((up || keepY) && mc.thePlayer.onGround && isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.jump();
            }
        }
    }

    ;

    @EventTarget
    private void tickEvent(EventTick event) {
        if (modeValue.get().equals("Normal")) {
            if (mc.thePlayer == null) return;
            if (getBlockSlot() < 0) return;
            if (!telly.getValue()) {
                canTellyPlace = true;
            }
            if (bugFlyValue.getValue()) {
                packets.add(new ArrayList<>());

                if (c08PacketSize >= 12 && !flyFlag) {
                    flyFlag = true;
                    while (c08PacketSize > 2) {
                        poll();
                    }
                }

                while (flyFlag && c08PacketSize > 2) {
                    poll();
                }
            }
        }
    }

    ;

    private void poll() {
        if (packets.isEmpty()) return;
        this.sendTick(packets.getFirst());
        packets.removeFirst();
    }

    private ItemStack switchToBlock() {
        int blockSlot;
        ItemStack itemStack;
        blockSlot = getBlockSlot();

        if (blockSlot == -1)
            return null;

        sendPacketHook(new C09PacketHeldItemChange(blockSlot - 36));
        itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).getStack();
        return itemStack;
    }


    @EventTarget
    private void placeEvent(EventPlace event) {
        if (modeValue.get().equals("Normal")) {
            if (!telly.getValue()) {
                mc.gameSettings.keyBindSprint.pressed = false;
            }
            if (mc.thePlayer == null) return;

            final ItemStack itemStack = switchToBlock();
            if (itemStack == null) return;

            place(itemStack);
            mc.sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown() && mc.inGameHasFocus);
        }
    }

    ;

    @EventTarget
    private void packetEvent(EventPacket event) {
        if (modeValue.get().equals("Normal")) {
            if (event.getEventType() == EventPacket.EventState.SEND) {
                final Packet<?> packet = event.getPacket();
                if (packet instanceof C09PacketHeldItemChange) {
                    final C09PacketHeldItemChange packetHeldItemChange = (C09PacketHeldItemChange) packet;
                    slot = packetHeldItemChange.getSlotId();
                }

                if (bugFlyValue.getValue()) {
                    if (packet instanceof C08PacketPlayerBlockPlacement) {
                        c08PacketSize += 1;
                    }
                    mc.addScheduledTask(() -> {
                        if (packets.isEmpty()) {
                            packets.add(new LinkedList<Packet<?>>());
                        }
                        packets.getLast().add(packet);
                    });
                    event.setCancelled(true);
                }
            }
        }
    }

    ;

    public static boolean canPlaceBlock(Block block) {
        return block.isFullCube() && !invalidBlocks.contains(block);
    }

    public static int findAutoBlockBlock() {
        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                final Block block = itemBlock.getBlock();

                if (block.isFullCube() && !invalidBlocks.contains(block))
                    return i;
            }
        }

        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                final Block block = itemBlock.getBlock();

                if (!invalidBlocks.contains(block))
                    return i;
            }
        }

        return -1;
    }

    public  void renderCounter() {
        if (!state && anim.isDone()) return;
        int slot = ScaffoldUtils.getBlockSlot();
        ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
        int count = slot == -1 ? 0 : ScaffoldUtils.getBlockCount();
        String countStr = String.valueOf("Blocks: "+count);
        FontRenderer fr = mc.fontRendererObj;
        RapeMasterFontManager fr2 = FontManager.font18;
        ScaledResolution sr = new ScaledResolution(mc);
        int color;
        float x, y;
        String str = countStr + " block" + (count != 1 ? "s" : "");
        float output = (float) anim.getOutput();
        if (counter.is("Easy")) {
            color = count < 24 ? 0xFFFF5555 : count < 128 ? 0xFFFFFF55 : 0xFF55FF55;
            x = sr.getScaledWidth() / 2F - fr.getStringWidth(countStr) / 2F + (heldItem != null ? 6 : 1);
            y = sr.getScaledHeight() / 2F + 10;

            GlStateManager.pushMatrix();
            RenderUtil.fixBlendIssues();
            GL11.glTranslatef(x + (heldItem == null ? 1 : 0), y, 1);
            GL11.glScaled(anim.getOutput(), anim.getOutput(), 1);
            GL11.glTranslatef(-x - (heldItem == null ? 1 : 0), -y, 1);

            fr.drawOutlinedString(countStr, x, y, ColorUtil.applyOpacity(color, output), true);

            if (heldItem != null) {
                double scale = 0.7;
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.scale(scale, scale, scale);
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(
                        heldItem,
                        (int) ((sr.getScaledWidth() / 2F - fr.getStringWidth(countStr) / 2F - 7) / scale),
                        (int) ((sr.getScaledHeight() / 2F + 8.5F) / scale)
                );
                RenderHelper.disableStandardItemLighting();
            }
            GlStateManager.popMatrix();
        }
        if (counter.is("Naven")) {
            x = sr.getScaledWidth() / 2F - fr.getStringWidth(countStr) / 2F + (heldItem != null ? 6 : 1);
            y = sr.getScaledHeight() / 2F + 10;
            float finalX = x;
            float finalY = y;
            ShaderElement.addBlurTask(() -> {
                RenderUtil.roundedRectangle(finalX - 4, finalY + 4, fr2.getStringWidth(countStr) + 10, 17*output, 5, Color.WHITE);
            });
            float finalY1 = y;
            float finalX1 = x;
            ShaderElement.addBloomTask(() -> {
                RenderUtil.roundedRectangle(finalX1 - 4, finalY1 + 4, fr2.getStringWidth(countStr) + 10, 17*output, 5, Color.BLACK);
            });

            RenderUtil.roundedRectangle(x - 4, y + 4, fr2.getStringWidth(countStr) + 10, 17*output, 5, new Color(-416996573, true));
            RenderUtil.bg(x - 4, y + 4.25, fr2.getStringWidth(countStr) + 10, 17, 2, 5, new Color(165, 17, 17, 255));
            fr2.drawStringWithShadow(countStr, x, y+9, Color.WHITE.getRGB());
            }
    }
    private ItemStack barrier = new ItemStack(Item.getItemById(166), 0, 0);
    @EventTarget
    public void onPacketSend(PacketSendEvent event) {
        C08PacketPlayerBlockPlacement packet;
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && (packet = (C08PacketPlayerBlockPlacement)event.getPacket()).getPlacedBlockDirection() != 255) {
            BlockPos pos = packet.getPosition().offset(EnumFacing.values()[packet.getPlacedBlockDirection()]);
            if ((Boolean)this.blockPlaceESP.get()) {
                int color = HUD.color(1).getRGB();
                this.blockPlaceList.add(new PlacedBlock( pos, color));
            }
        }

    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        Color color = new Color(Color.BLACK.getRGB());
        Color color2 = new Color(Color.WHITE.getRGB());
        double x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double)mc.timer.renderPartialTicks - RenderManager.renderPosX;
        double y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double)mc.timer.renderPartialTicks - RenderManager.renderPosY;
        double z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double)mc.timer.renderPartialTicks - RenderManager.renderPosZ;
        double x2 = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double)mc.timer.renderPartialTicks - RenderManager.renderPosX;
        double y2 = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double)mc.timer.renderPartialTicks - RenderManager.renderPosY;
        double z2 = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double)mc.timer.renderPartialTicks - RenderManager.renderPosZ;
        x -= 0.65;
        z -= 0.65;
        x2 -= 0.5;
        z2 -= 0.5;
        y += (double)mc.thePlayer.getEyeHeight() + 0.35 - (mc.thePlayer.isSneaking() ? 0.25 : 0.0);
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        double rotAdd = -0.25 * (double)(Math.abs(mc.thePlayer.rotationPitch) / 90.0F);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 2.0F);
        GL11.glLineWidth(2.0F);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x, y - 2.0, z, x + 1.3, y - 2.0, z + 1.3));
        GL11.glColor4f((float)color2.getRed() / 255.0F, (float)color2.getGreen() / 255.0F, (float)color2.getBlue() / 255.0F, 2.0F);
        GL11.glLineWidth(2.0F);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x2, y - 2.0, z2, x2 + 1.0, y - 2.0, z2 + 1.0));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        ArrayList<PlacedBlock> toRemove = new ArrayList();
        Iterator var3 = this.blockPlaceList.iterator();

        PlacedBlock block;
        while(var3.hasNext()) {
            block = (PlacedBlock)var3.next();
            float alpha = Math.max(1.0F - (float)block.timer.getTimeElapsed() / 500.0F, 0.01F);
            if (alpha <= 0.01F) {
                toRemove.add(block);
            }

            Color finalColor = new Color(block.color);
            float r = (float)finalColor.getRed() / 255.0F;
            float g = (float)finalColor.getGreen() / 255.0F;
            float b = (float)finalColor.getBlue() / 255.0F;
            RenderUtil.prepareBoxRender(5.5F, (double)r, (double)g, (double)b, (double)alpha);
            RenderUtil.renderBlockBox(mc.getRenderManager(), event.getPartialTicks(), (double)block.pos.getX(), (double)block.pos.getY(), (double)block.pos.getZ());
            RenderUtil.stopBoxRender();
        }

        var3 = toRemove.iterator();

        while(var3.hasNext()) {
            block = (PlacedBlock)var3.next();
            this.blockPlaceList.remove(block);
        }

        toRemove.clear();
    }

    @EventTarget
    private void updateEventHandler(EventUpdate event){
        if (modeValue.get().equals("Normal")) {
            final ItemStack itemStack = switchToBlock();
            if (itemStack == null) return;

            if (telly.getValue()) {
                up = mc.gameSettings.keyBindJump.pressed;
                keepY = !up;
            } else {
                up = upValue.getValue();
                keepY = modeValue.is("WatchdogKeepY") || keepYValue.getValue();
            }
            if (mc.thePlayer.onGround) {
                keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
            }

            if (getBlockSlot() < 0) {
                return;
            }


            if (telly.getValue()) {

                this.findBlock();
                mc.gameSettings.keyBindSprint.pressed = true;

                if (canTellyPlace && !mc.thePlayer.onGround && isMoving())
                    mc.thePlayer.setSprinting(false);
                /*
             canTellyPlace = mc.thePlayer.offGroundTicks >= (up ? (mc.thePlayer.ticksExisted % 16 == 0 ? 2 : 1) : 2.9);

             */

                float tellyTicks;
                switch (tellymode.getValue()) {
                    case "Ming": {
                        tellyTicks = 1F;
                        break;
                    }
                    case "HYTBW": {
                        tellyTicks = 3.8F;
                        break;
                    }
                    default: {
                        tellyTicks = 3.8F;
                    }
                }
                canTellyPlace = mc.thePlayer.offGroundTicks >= tellyTicks;
            }

            if (!this.canTellyPlace) {
                return;
            }
            if (!modeValue.is("Normal") && data != null && !modeValue.is("WatchdogKeepY")) {
                try {
                    float[] rotations = lastRotation = ScaffoldUtils.faceBlock(data);
                    Client.instance.getRotationManager().setRotation(new Rotation(rotations[0], rotations[1]), 180, moveFix.getValue());
                } catch (Exception e) {
                    if (lastRotation != null) {
                        Client.instance.getRotationManager().setRotation(new Rotation(lastRotation[0], lastRotation[1]), 180, moveFix.getValue());
                    } else {
                        e.printStackTrace();
                    }
                }
            } else if (modeValue.is("WatchdogKeepY")) {
                if (mc.thePlayer.onGround)
                    keepYaw = PlayerUtil.getMoveYaw(mc.thePlayer.rotationYaw) - 180f;
                float[] rotations = new float[]{keepYaw, y};
                Client.instance.getRotationManager().setRotation(new Rotation(rotations[0], rotations[1]), 180, moveFix.getValue());
            }
            if (!canTellyPlace) return;
            if (data != null && modeValue.is("Normal")) {
                float[] rot = RotationUtil.getRotationBlock(data);
                float yaw = rot[0];
                float pitch = rot[1];
                Client.instance.getRotationManager().setRotation(new Rotation(yaw, pitch), 180, moveFix.getValue());
            }
        }
    };

    private void place(final ItemStack block) {
        if (!canTellyPlace) return;
        if (data != null) {
            EnumFacing enumFacing = keepY ? this.enumFacing : this.getPlaceSide(this.data);
            if (enumFacing == null) return;
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, block, this.data, enumFacing, getVec3(data, enumFacing))) {
                if ((lastJumpMode || lastKeepYMode) && modeValue.is("WatchdogGround")) {
                    placedAfterTower++;
                    if (placedAfterTower >= 2) {
                        modeValue.setValue(lastJumpMode?"WatchdogJump":"WatchdogKeepY");
                    }
                }
                y = 80.8964f;
                if (swing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }
            }

        }

    }
    @EventTarget
    public void onTick(EventTick event) {
        if (modeValue.get().equals("Watchdog")) {


            if (mc.thePlayer == null) return;

            if (MoveUtil.isMoving()) moveTicks++;
            else moveTicks = 0;

        }
    }
    private boolean place() {
        int slot = dev.olive.utils.player.ScaffoldUtils.getBlockSlot();
        if (blockCache == null || lastBlockCache == null || slot == -1) return false;

        if (this.slot != slot) {
            this.slot = slot;

            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(this.slot));
        }

        boolean placed = false;
        if (delayTimer.hasTimeElapsed(1000)) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
                    mc.thePlayer.inventory.getStackInSlot(this.slot),
                    lastBlockCache.getPosition(), lastBlockCache.getFacing(),
                    dev.olive.utils.player.ScaffoldUtils.getHypixelVec3(lastBlockCache))) {
                placed = true;
                y = (float) MathUtils.getRandomInRange(79.5f, 83.5f);
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
            blockCache = null;
        }
        return placed;
    }
    @EventTarget
    public void onBlockPlace(EventPlace event) {
        if (modeValue.get().equals("Watchdog")) {
            place();
        }
    }

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent e) {
        if (modeValue.get().equals("Watchdog")) {
            if (e.getPacket() instanceof C0BPacketEntityAction
                    && ((C0BPacketEntityAction) e.getPacket()).getAction() == C0BPacketEntityAction.Action.START_SPRINTING
                    && sprintMode.isNotCurrentMode("None") && sprintMode.isNotCurrentMode("Vanilla") && sprintMode.isNotCurrentMode("Legit")) {
                e.setCancelled(true);
            }
            if (e.getPacket() instanceof C09PacketHeldItemChange) {
                e.setCancelled(true);
            }

            if (e.getPacket() instanceof C08PacketPlayerBlockPlacement c08PacketPlacement) {
                c08PacketPlacement.setStack(mc.thePlayer.inventory.getStackInSlot(slot));
            }
        }

    }
    private void findBlock() {
        if (isMoving() && keepY) {
            boolean shouldGoDown = false;
            final BlockPos blockPosition = new BlockPos(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ);

            if ((BlockUtil.isValidBock(blockPosition) || search(blockPosition, !shouldGoDown))) return;

            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown)) return;
        } else {
            this.data = getBlockPos();
        }

    }

    private double calcStepSize(double range) {
        double accuracy = 6;
        accuracy += accuracy % 2; // If it is set to uneven it changes it to even. Fixes a bug
        return Math.max(range / accuracy, 0.01);
    }

    private boolean search(final BlockPos blockPosition, final boolean checks) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        ScaffoldUtils.PlaceRotation placeRotation = null;

        double xzRV = 0.5;
        double yRV = 0.5;
        double xzSSV = calcStepSize(xzRV);
        double ySSV = calcStepSize(xzRV);

        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = blockPosition.offset(side);

            if (!BlockUtil.isValidBock(neighbor)) continue;

            final Vec3 dirVec = new Vec3(side.getDirectionVec());
            for (double xSearch = 0.5 - xzRV / 2; xSearch <= 0.5 + xzRV / 2; xSearch += xzSSV) {
                for (double ySearch = 0.5 - yRV / 2; ySearch <= 0.5 + yRV / 2; ySearch += ySSV) {
                    for (double zSearch = 0.5 - xzRV / 2; zSearch <= 0.5 + xzRV / 2; zSearch += xzSSV) {
                        final Vec3 posVec = new Vec3(blockPosition).addVector(xSearch, ySearch, zSearch);
                        final double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                        final Vec3 hitVec = posVec.add(new Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5));

                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec)) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null))
                            continue;

                        // face block
                        final Rotation rotation = getRotation(hitVec, eyesPos);

                        final Vec3 vecRot = RotationUtil.getVectorForRotation(rotation);
                        final Vec3 rotationVector = new Vec3(vecRot.xCoord, vecRot.yCoord, vecRot.zCoord);
                        final Vec3 vector = eyesPos.addVector(rotationVector.xCoord * 4, rotationVector.yCoord * 4, rotationVector.zCoord * 4);
                        final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true);

                        if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.getBlockPos().equals(neighbor)))
                            continue;

                        if (placeRotation == null || Client.instance.getRotationManager().getRotationDifference(rotation) < Client.instance.getRotationManager().getRotationDifference(placeRotation.getRotation()))
                            placeRotation = new ScaffoldUtils.PlaceRotation(new ScaffoldUtils.PlaceInfo(neighbor, side.getOpposite(), hitVec), rotation);
                    }
                }
            }
        }

        if (placeRotation == null) return false;

        data = placeRotation.getPlaceInfo().getBlockPos();
        enumFacing = placeRotation.getPlaceInfo().getEnumFacing();

        return true;
    }

    private Rotation getRotation(Vec3 hitVec, Vec3 eyesPos) {
        final double diffX = hitVec.xCoord - eyesPos.xCoord;
        final double diffY = hitVec.yCoord - eyesPos.yCoord;
        final double diffZ = hitVec.zCoord - eyesPos.zCoord;

        final double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

        return new Rotation(MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F), MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ))));
    }


    private EnumFacing getPlaceSide(BlockPos blockPos) {
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, EnumFacing> hashMap = new HashMap<>();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        BlockPos bp;
        Vec3 vec3;
        if (BlockUtil.isAirBlock(blockPos.add(0, 1, 0)) && !blockPos.add(0, 1, 0).equals(playerPos) && !mc.thePlayer.onGround) {
            bp = blockPos.add(0, 1, 0);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.UP);
        }

        if (BlockUtil.isAirBlock(blockPos.add(1, 0, 0)) && !blockPos.add(1, 0, 0).equals(playerPos)) {
            bp = blockPos.add(1, 0, 0);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.EAST);
        }

        if (BlockUtil.isAirBlock(blockPos.add(-1, 0, 0)) && !blockPos.add(-1, 0, 0).equals(playerPos)) {
            bp = blockPos.add(-1, 0, 0);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.WEST);
        }

        if (BlockUtil.isAirBlock(blockPos.add(0, 0, 1)) && !blockPos.add(0, 0, 1).equals(playerPos)) {
            bp = blockPos.add(0, 0, 1);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.SOUTH);
        }

        if (BlockUtil.isAirBlock(blockPos.add(0, 0, -1)) && !blockPos.add(0, 0, -1).equals(playerPos)) {
            bp = blockPos.add(0, 0, -1);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.NORTH);
        }

        positions.sort(Comparator.comparingDouble((vec3x) -> mc.thePlayer.getDistance(vec3x.xCoord, vec3x.yCoord, vec3x.zCoord)));
        if (!positions.isEmpty()) {
            vec3 = this.getBestHitFeet(this.data);
            if (mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) >= mc.thePlayer.getDistance(positions.get(0).xCoord, positions.get(0).yCoord, positions.get(0).zCoord)) {
                return hashMap.get(positions.get(0));
            }
        }

        return null;
    }

    private Vec3 getBestHitFeet(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        double ex = MathHelper.clamp_double(mc.thePlayer.posX, blockPos.getX(), (double) blockPos.getX() + block.getBlockBoundsMaxX());
        double ey = MathHelper.clamp_double(keepY ? getYLevel() : mc.thePlayer.posY, blockPos.getY(), (double) blockPos.getY() + block.getBlockBoundsMaxY());
        double ez = MathHelper.clamp_double(mc.thePlayer.posZ, blockPos.getZ(), (double) blockPos.getZ() + block.getBlockBoundsMaxZ());
        return new Vec3(ex, ey, ez);
    }

    private BlockPos getBlockPos() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, BlockPos> hashMap = new HashMap<>();

        for (int x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
            for (int y = playerPos.getY() - 1; y <= playerPos.getY(); ++y) {
                for (int z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    if (BlockUtil.isValidBock(new BlockPos(x, y, z))) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                        Vec3 vec3 = getVec3(blockPos, block);
                        positions.add(vec3);
                        hashMap.put(vec3, blockPos);
                    }
                }
            }
        }

        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(this::getBestBlock));
            return hashMap.get(positions.get(0));
        } else {
            return null;
        }
    }

    private Vec3 getVec3(BlockPos blockPos, Block block) {
        double ex = MathHelper.clamp_double(mc.thePlayer.posX, blockPos.getX(), (double) blockPos.getX() + block.getBlockBoundsMaxX());
        double ey = MathHelper.clamp_double(keepY ? getYLevel() : mc.thePlayer.posY, blockPos.getY(), (double) blockPos.getY() + block.getBlockBoundsMaxY());
        double ez = MathHelper.clamp_double(mc.thePlayer.posZ, blockPos.getZ(), (double) blockPos.getZ() + block.getBlockBoundsMaxZ());
        return new Vec3(ex, ey, ez);
    }

    private double getBestBlock(Vec3 vec3) {
        return mc.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public int getBlockSlot() {
        for (int i = 36; i < 45; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                if (itemStack.stackSize >= 3) {
                    final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                    final Block block = itemBlock.getBlock();

                    if (block.isFullCube() && !invalidBlocks.contains(block))
                        return i;
                }
            }
        }

        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                if (itemStack.stackSize >= 3) {
                    final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                    final Block block = itemBlock.getBlock();

                    if (!invalidBlocks.contains(block))
                        return i;
                }
            }
        }

        return -1;
    }
    public class PlacedBlock {
        public final BlockPos pos;
        public final int color;
        public final TimerUtil timer;

        public PlacedBlock(BlockPos pos, int color) {

            this.pos = pos;
            this.color = color;
            this.timer = new TimerUtil();
        }
    }
    public int getAllBlockCount() {
        int count = 0;
        int i = 0;
        while (i < 45) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                final Item item = stack.getItem();
                if (stack.getItem() instanceof ItemBlock && this.isValid(item) && stack.stackSize >= 3) {
                    count += stack.stackSize - 2;
                }
            }
            ++i;
        }
        return count;
    }

    public int getBlockCount() {
        int count = 0;
        int i = 36;
        while (i < 45) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                final Item item = stack.getItem();
                if (stack.getItem() instanceof ItemBlock && this.isValid(item)) {
                    count += stack.stackSize;
                }
            }
            ++i;
        }
        return count;
    }

    private boolean isValid(final Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock) (item)).getBlock());
    }

    private void getBlock(int switchSlot) {
        for (int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack() && (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory)) {
                ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (itemStack.getItem() instanceof ItemBlock) {
                    final ItemBlock block = (ItemBlock) itemStack.getItem();
                    if (isValid(block) && swap.getValue()) {
                        if (36 + switchSlot != i) {
                            mc.thePlayer.swap(i, switchSlot);
                        }
                        break;
                    }
                }
            }
        }

    }

    int getBestSpoofSlot() {
        int spoofSlot = 5;
        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                spoofSlot = i - 36;
                break;
            }
        }

        return spoofSlot;
    }

    public Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }

    public Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }
}