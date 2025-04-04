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
import dev.olive.utils.*;
import dev.olive.utils.math.MathUtils;
import dev.olive.utils.player.*;
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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static dev.olive.utils.player.MoveUtil.isMoving;


public class BWScaffold extends Module {
    public static final List<Block> invalidBlocks;
    private static final BoolValue keepYValue;
    public static BWScaffold INSTANCE;
    public static double keepYCoord;
    private final ArrayList<PlacedBlock> blockPlaceList = new ArrayList();
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprintValue = new BoolValue("Sprint", false);
    public final BoolValue watchdogValue = new BoolValue("Watchdog", false);
    public final BoolValue adStrafe = new BoolValue("ADStrafe", false);
    public final BoolValue tower = new BoolValue("Tower", false);
    public final BoolValue safeValue = new BoolValue("Safe walk", false);
    private final BoolValue bwValue = new BoolValue("BedWars",false);
    public final BoolValue telly = new BoolValue("Telly", true);
    public final BoolValue upValue = new BoolValue("Up", false, () -> {
        return (Boolean)this.telly.getValue() && !(Boolean)keepYValue.getValue();
    });
    public final BoolValue blockPlaceESP = new BoolValue("Block Place ESP", true);
    private final Animation anim = new DecelerateAnimation(250, 1.0);
    private final NumberValue tellyTicks = new NumberValue("TellyTicks", 2.9, 0.5, 8.0, 0.01);
    public boolean tip = false;
    public int vl = 0;
    protected Random rand = new Random();
    boolean idk = false;
    int idkTick = 0;
    int towerTick = 0;
    private int direction;
    private int slot;
    private BlockPos data;
    private final List<BlockPos> placedBlocks = new ArrayList();
    private boolean canTellyPlace;
    private int prevItem = 0;
    private EnumFacing enumFacing;

    public BWScaffold() {
        super("BWScaffold","起床搭路", Category.World);
    }

    public static double getYLevel() {
        if (!(Boolean)keepYValue.getValue()) {
            return mc.thePlayer.posY - 1.0;
        } else {
            return !MoveUtil.isMoving() ? mc.thePlayer.posY - 1.0 : keepYCoord;
        }
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            y += 0.08;
        } else {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }

        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }

        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }

        return new Vec3(x, y, z);
    }

    private boolean doHighVLTips() {
        if (!this.tip) {
            DebugUtil.log("You are in high vl,sprint disabled.");
        }

        this.tip = true;
        return false;
    }

    public void onEnable() {
        this.idkTick = 5;
        if (mc.thePlayer != null) {
            this.prevItem = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.setSprinting((Boolean)this.sprintValue.getValue() || !this.canTellyPlace);
            mc.gameSettings.keyBindSprint.pressed = (Boolean)this.sprintValue.getValue() || !this.canTellyPlace;
            this.canTellyPlace = false;
            this.tip = false;
            this.data = null;
            this.slot = -1;
        }
    }

    public void onDisable() {
        if (mc.thePlayer != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            if ((Boolean)this.adStrafe.getValue()) {
                if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                    mc.gameSettings.keyBindLeft.setPressed(false);
                } else if (mc.gameSettings.keyBindRight.isKeyDown()) {
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
            }

            mc.thePlayer.inventory.currentItem = this.prevItem;
            Client.instance.slotSpoofManager.stopSpoofing();
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof C0BPacketEntityAction && (Boolean)this.watchdogValue.get() && (((C0BPacketEntityAction)e.getPacket()).getAction() == C0BPacketEntityAction.Action.START_SPRINTING || ((C0BPacketEntityAction)e.getPacket()).getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING)) {
            e.setCancelled(true);
        }

    }

    @EventTarget
    public void onPacketSend(PacketSendEvent event) {
        C08PacketPlayerBlockPlacement packet;
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && (packet = (C08PacketPlayerBlockPlacement)event.getPacket()).getPlacedBlockDirection() != 255) {
            BlockPos pos = packet.getPosition().offset(EnumFacing.values()[packet.getPlacedBlockDirection()]);
            if (this.blockPlaceESP.get()) {
                int color = HUD.color(1).getRGB();
                this.blockPlaceList.add(new PlacedBlock(pos, color));
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
    public void onUpdate(EventMotion event) {
        if (this.idkTick > 0) {
            --this.idkTick;
        }

    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (((Boolean)this.upValue.getValue() || (Boolean)keepYValue.getValue()) && mc.thePlayer.onGround && MoveUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.jump();
        }

    }

    @EventTarget
    private void onTick(EventTick event) {
        if (mc.thePlayer != null) {
            if (this.slot >= 0) {
                if (!(Boolean)this.telly.getValue()) {
                    this.canTellyPlace = true;
                }

            }
        }
    }

    @EventTarget
    private void onMove(EventMove event) {
        if (mc.thePlayer.onGround && (Boolean)this.safeValue.getValue()) {
            mc.thePlayer.safeWalk = true;
        }

        if ((Boolean)this.watchdogValue.getValue() && (Boolean)this.sprintValue.getValue()) {
        }

    }

    @EventTarget
    private void onPlace(EventPlace event) {
        this.slot = this.getBlockSlot();
        if (this.slot >= 0) {
            if (!(Boolean)this.telly.getValue()) {
                mc.thePlayer.setSprinting((Boolean)this.sprintValue.getValue());
                mc.gameSettings.keyBindSprint.pressed = false;
            }

            event.setCancelled(true);
            if (mc.thePlayer != null) {
                this.place();
                mc.sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown() && mc.inGameHasFocus);
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate event) {
        if ((Boolean)this.telly.getValue()) {
            if (mc.gameSettings.keyBindJump.pressed) {
                this.upValue.set(true);
                keepYValue.set(false);
            } else {
                this.upValue.set(false);
                keepYValue.set(true);
            }
        }

        if (mc.thePlayer.onGround) {
            keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
        }

        this.slot = this.getBlockSlot();
        if (this.slot >= 0) {
            mc.thePlayer.inventory.currentItem = this.slot;
            Client.instance.slotSpoofManager.startSpoofing(this.prevItem);
            this.findBlock();
            if ((Boolean)this.telly.getValue()) {
                if (this.canTellyPlace && !mc.thePlayer.onGround && MoveUtil.isMoving()) {
                    mc.thePlayer.setSprinting(false);
                }

                if (bwValue.get()){
                    canTellyPlace = (mc.thePlayer.offGroundTicks >=
                            ((this.upValue.getValue()) ? (3.0D) : (this.tellyTicks.getValue()).doubleValue()));
                }else {
                    this.canTellyPlace = (double)mc.thePlayer.offGroundTicks >= ((Boolean)this.upValue.getValue() ?
                            (double)(mc.thePlayer.ticksExisted % 16 == 0 ? 2 : 1) : (Double)this.tellyTicks.getValue());
                }

            }

            if (this.canTellyPlace) {
                if (this.data != null) {
                    float yaw = RotationUtil.getRotationBlock(this.data)[0];
                    float pitch = RotationUtil.getRotationBlock(this.data)[1];
                    if (!(Boolean)this.watchdogValue.getValue()) {
                        Client.instance.rotationManager.setRotation(new Vector2f(yaw, pitch), 180.0F, !(Boolean)this.watchdogValue.getValue());
                        mc.thePlayer.setSprinting((Boolean)this.sprintValue.getValue());
                    }
                }

                if (this.idkTick != 0) {
                    this.towerTick = 0;
                } else {
                    if (this.towerTick > 0) {
                        ++this.towerTick;
                        if (this.towerTick > 6) {
                            this.idk1(MoveUtil.speed() * 0.05);
                        }

                        if (this.towerTick > 16) {
                            this.towerTick = 0;
                        }
                    }

                    if (this.isTowering()) {
                        this.towerMove();
                    } else {
                        this.towerTick = 0;
                    }

                }
            }
        }
    }

    public void idk1(double d) {
        float f = MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90.0F);
        MoveUtil.setMotion2(d, f);
    }

    private boolean isTowering() {
        return (Boolean)this.tower.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    private void pickBlock() {
        if (this.getBlockSlot() > 0) {
            mc.thePlayer.inventory.currentItem = this.getBlockSlot();
        }
    }

    public  void renderCounter() {
        BalanceTimer balanceTimer = (BalanceTimer)Client.instance.moduleManager.getModule(BalanceTimer.class);
        if (!balanceTimer.state) {
            this.anim.setDirection(this.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
            int count = this.slot == -1 ? 0 : this.getBlockCount();
            if (!this.getState() && this.anim.isDone()) {
                return;
            }

            int var10000;
            if (count < 24) {
                var10000 = -43691;
            } else {
                var10000 = count < 45 ? -171 : -11141291;
            }

            float output = (float)this.anim.getOutput();
            int slot = this.getBlockSlot();
            ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
            String countStr = String.valueOf(count);
            float blockWH = heldItem != null ? 15.0F : -2.0F;
            int spacing = 3;
            String text2 = countStr;
            ScaledResolution sr = new ScaledResolution(mc);
            float totalWidth = (blockWH + (float)spacing + (float)FontManager.interSemiBold18.getStringWidth(text2) + 6.0F + 2.0F) * output;
            float x = (float)sr.getScaledWidth() / 2.0F - totalWidth / 2.0F;
            float y = (float)sr.getScaledHeight() - (float)sr.getScaledHeight() / 2.0F;
            float height = 20.0F;
            RenderUtil.scissorStart((double)x - 1.5, (double)y - 1.5 + 15.0, (double)(totalWidth + 3.0F), (double)(height + 3.0F));
            ShaderElement.addBlurTask(() -> {
                RoundedUtil.drawRound(x, y * output + 15.0F, totalWidth, height, 4.0F, Color.WHITE);
            });
            RoundedUtil.drawRound(x + 2.0F, y * output + 15.0F, totalWidth - 2.0F, height, 4.0F, new Color(0, 0, 0, 110));
            FontManager.interSemiBold20.drawStringDynamic(text2, (double)(x + 3.0F + blockWH + (float)spacing), (double)(y + FontManager.font20.getMiddleOfBox(height) + 0.5F + 4.0F + 15.0F), 1, 6);
            if (heldItem != null) {
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int)x + 4, (int)(y + 10.0F - blockWH / 2.0F + 15.0F));
                RenderHelper.disableStandardItemLighting();
            }

            if (heldItem != null) {
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int)x + 3 + 1, (int)(y + 10.0F - blockWH / 2.0F + 15.0F));
                RenderHelper.disableStandardItemLighting();
            }

            RenderUtil.scissorEnd();
        }

    }

    private double calculateBPS() {
        double bps = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)mc.timer.timerSpeed * 20.0;
        return (double)Math.round(bps * 100.0) / 100.0;
    }

    @EventTarget
    private void onMotion(EventMotion event) {
        if (event.isPre()) {
            if (this.slot < 0) {
                return;
            }

            if (this.getBlockCount() <= 0) {
                int spoofSlot = this.getBestSpoofSlot();
                this.getBlock(spoofSlot);
            }

            if (this.slot < 0) {
                return;
            }

            mc.thePlayer.inventoryContainer.getSlot(this.slot + 36).getStack();
            if ((Boolean)this.watchdogValue.getValue()) {
                event.setYaw(MoveUtil.getDirection(mc.thePlayer.rotationYaw) - 180.0F);
                event.setPitch(mc.gameSettings.keyBindJump.isKeyDown() ? 85.0F : 80.0F);
                RotationUtil.setVisualRotations(MoveUtil.getDirection(mc.thePlayer.rotationYaw) - 180.0F, mc.gameSettings.keyBindJump.isKeyDown() ? 85.0F : 80.0F);
                if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        MoveUtil.setMotion(0.25);
                        mc.thePlayer.jump();
                    } else {
                        MoveUtil.setMotion(0.003);
                    }
                }
            }
        }

    }

    private void towerMove() {
        mc.thePlayer.setSprinting(false);
        mc.gameSettings.keyBindSprint.pressed = false;
        if (!MoveUtil.isMoving()) {
            EntityPlayerSP var10000 = mc.thePlayer;
            var10000.motionX += 0.008;
        }

        ++this.towerTick;
        if (mc.thePlayer.onGround) {
            this.towerTick = 0;
        }

        mc.thePlayer.motionY = 0.41965;
        mc.thePlayer.motionX = Math.min(mc.thePlayer.motionX, 0.265);
        mc.thePlayer.motionZ = Math.min(mc.thePlayer.motionZ, 0.265);
        if (this.towerTick == 1) {
            mc.thePlayer.motionY = 0.33;
        } else if (this.towerTick == 2) {
            mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
        } else if (this.towerTick >= 3) {
            this.towerTick = 0;
        }

    }

    private void place() {
        if (this.canTellyPlace) {
            this.slot = this.getBlockSlot();
            if (this.slot >= 0) {
                if (this.slot >= 0) {
                    if (this.data != null) {
                        EnumFacing enumFacing = (Boolean)keepYValue.getValue() ? this.enumFacing : this.getPlaceSide(this.data);
                        if (enumFacing == null) {
                            return;
                        }

                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), this.data, enumFacing, getVec3(this.data, enumFacing))) {
                            if ((Boolean)this.swing.getValue()) {
                                mc.thePlayer.swingItem();
                            } else {
                                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                            }
                        }
                    }

                }
            }
        }
    }

    private void findBlock() {
        if (MoveUtil.isMoving() && (Boolean)keepYValue.getValue()) {
            boolean shouldGoDown = false;
            BlockPos blockPosition = new BlockPos(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ);
            if (BlockUtil.isValidBock(blockPosition) || this.search(blockPosition, !shouldGoDown)) {
                return;
            }

            for(int x = -1; x <= 1; ++x) {
                for(int z = -1; z <= 1; ++z) {
                    if (this.search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                        return;
                    }
                }
            }
        } else {
            this.data = this.getBlockPos();
        }

    }

    private double calcStepSize(double range) {
        double accuracy = 6.0;
        accuracy += accuracy % 2.0;
        return Math.max(range / accuracy, 0.01);
    }

    private boolean search(BlockPos blockPosition, boolean checks) {
        Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        PlaceRotation placeRotation = null;
        double xzRV = 0.5;
        double yRV = 0.5;
        double xzSSV = this.calcStepSize(xzRV);
        double ySSV = this.calcStepSize(xzRV);
        EnumFacing[] var13 = EnumFacing.values();
        int var14 = var13.length;

        for(int var15 = 0; var15 < var14; ++var15) {
            EnumFacing side = var13[var15];
            BlockPos neighbor = blockPosition.offset(side);
            if (BlockUtil.isValidBock(neighbor)) {
                Vec3 dirVec = new Vec3(side.getDirectionVec());

                for(double xSearch = 0.5 - xzRV / 2.0; xSearch <= 0.5 + xzRV / 2.0; xSearch += xzSSV) {
                    for(double ySearch = 0.5 - yRV / 2.0; ySearch <= 0.5 + yRV / 2.0; ySearch += ySSV) {
                        for(double zSearch = 0.5 - xzRV / 2.0; zSearch <= 0.5 + xzRV / 2.0; zSearch += xzSSV) {
                            Vec3 posVec = (new Vec3(blockPosition)).addVector(xSearch, ySearch, zSearch);
                            double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                            Vec3 hitVec = posVec.add(new Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5));
                            if (!checks || !(eyesPos.squareDistanceTo(hitVec) > 18.0) && !(distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec))) && mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) == null) {
                                double diffX = hitVec.xCoord - eyesPos.xCoord;
                                double diffY = hitVec.yCoord - eyesPos.yCoord;
                                double diffZ = hitVec.zCoord - eyesPos.zCoord;
                                double diffXZ = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
                                Rotation rotation = new Rotation(MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapAngleTo180_float((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)))));
                                Vec3 rotationVector = new Vec3(RotationUtil.getVectorForRotation(rotation).xCoord, RotationUtil.getVectorForRotation(rotation).yCoord, RotationUtil.getVectorForRotation(rotation).zCoord);
                                Vec3 vector = eyesPos.addVector(rotationVector.xCoord * 4.0, rotationVector.yCoord * 4.0, rotationVector.zCoord * 4.0);
                                MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true);
                                if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.getBlockPos().equals(neighbor) && (placeRotation == null || Client.instance.rotationManager.getRotationDifference(rotation) < Client.instance.rotationManager.getRotationDifference(placeRotation.getRotation()))) {
                                    placeRotation = new PlaceRotation(new PlaceInfo(neighbor, side.getOpposite(), hitVec), rotation);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (placeRotation == null) {
            return false;
        } else {
            this.data = placeRotation.getPlaceInfo().getBlockPos();
            this.enumFacing = placeRotation.getPlaceInfo().getEnumFacing();
            return true;
        }
    }

    private EnumFacing getPlaceSide(BlockPos blockPos) {
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, EnumFacing> hashMap = new HashMap<>();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);


        if ((this.bwValue.getValue()).booleanValue()) {
            if (mc.thePlayer.fallDistance > 0.2D && BlockUtil.isAirBlock(blockPos.add(0, 1, 0)) && !blockPos.add(0, 1, 0).equals(playerPos) && !mc.thePlayer.onGround) {
                BlockPos bp = blockPos.add(0, 1, 0);
                Vec3 vec3 = getBestHitFeet(bp);
                positions.add(vec3);
                hashMap.put(vec3, EnumFacing.UP);
            }

        } else if (BlockUtil.isAirBlock(blockPos.add(0, 1, 0)) && !blockPos.add(0, 1, 0).equals(playerPos) && !mc.thePlayer.onGround) {
            BlockPos bp = blockPos.add(0, 1, 0);
            Vec3 vec3 = getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.UP);
        }

        if (BlockUtil.isAirBlock(blockPos.add(1, 0, 0)) && !blockPos.add(1, 0, 0).equals(playerPos)) {
            BlockPos bp = blockPos.add(1, 0, 0);
            Vec3 vec3 = getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.EAST);
        }

        if (BlockUtil.isAirBlock(blockPos.add(-1, 0, 0)) && !blockPos.add(-1, 0, 0).equals(playerPos)) {
            BlockPos bp = blockPos.add(-1, 0, 0);
            Vec3 vec3 = getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.WEST);
        }

        if (BlockUtil.isAirBlock(blockPos.add(0, 0, 1)) && !blockPos.add(0, 0, 1).equals(playerPos)) {
            BlockPos bp = blockPos.add(0, 0, 1);
            Vec3 vec3 = getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.SOUTH);
        }

        if (BlockUtil.isAirBlock(blockPos.add(0, 0, -1)) && !blockPos.add(0, 0, -1).equals(playerPos)) {
            BlockPos bp = blockPos.add(0, 0, -1);
            Vec3 vec3 = getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.NORTH);
        }

        positions.sort(Comparator.comparingDouble(vec3x -> mc.thePlayer.getDistance(vec3x.xCoord, vec3x.yCoord, vec3x.zCoord)));
        if (!positions.isEmpty()) {
            Vec3 vec3 = getBestHitFeet(this.data);
            if (mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) >= mc.thePlayer.getDistance(((Vec3)positions.get(0)).xCoord, ((Vec3)positions.get(0)).yCoord, ((Vec3)positions.get(0)).zCoord)) {
                return hashMap.get(positions.get(0));
            }
        }

        return null;
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
    private Vec3 getBestHitFeet(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        double ex = MathHelper.clamp_double(mc.thePlayer.posX, (double)blockPos.getX(), (double)blockPos.getX() + block.getBlockBoundsMaxX());
        double ey = MathHelper.clamp_double((Boolean)keepYValue.getValue() ? getYLevel() : mc.thePlayer.posY, (double)blockPos.getY(), (double)blockPos.getY() + block.getBlockBoundsMaxY());
        double ez = MathHelper.clamp_double(mc.thePlayer.posZ, (double)blockPos.getZ(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
        return new Vec3(ex, ey, ez);
    }

    private BlockPos getBlockPos() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList();
        HashMap<Vec3, BlockPos> hashMap = new HashMap();

        for(int x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
            for(int y = playerPos.getY() - 1; y <= playerPos.getY(); ++y) {
                for(int z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    if (BlockUtil.isValidBock(new BlockPos(x, y, z))) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                        double ex = MathHelper.clamp_double(mc.thePlayer.posX, (double)blockPos.getX(), (double)blockPos.getX() + block.getBlockBoundsMaxX());
                        double ey = MathHelper.clamp_double((Boolean)keepYValue.getValue() ? getYLevel() : mc.thePlayer.posY, (double)blockPos.getY(), (double)blockPos.getY() + block.getBlockBoundsMaxY());
                        double ez = MathHelper.clamp_double(mc.thePlayer.posZ, (double)blockPos.getZ(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
                        Vec3 vec3 = new Vec3(ex, ey, ez);
                        positions.add(vec3);
                        hashMap.put(vec3, blockPos);
                    }
                }
            }
        }

        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(this::getBestBlock));
            return (BlockPos)hashMap.get(positions.get(0));
        } else {
            return null;
        }
    }

    private double getBestBlock(Vec3 vec3) {
        return mc.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public int getBlockSlot() {
        for(int i = 0; i < 9; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() && mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock) {
                return i;
            }
        }

        return -1;
    }

    public int getBlockCount() {
        int n = 0;

        for(int i = 36; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = stack.getItem();
                if (stack.getItem() instanceof ItemBlock && this.isValid(item)) {
                    n += stack.stackSize;
                }
            }
        }

        return n;
    }

    private boolean isValid(Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock)item).getBlock());
    }

    private float getYaw() {
        if (mc.gameSettings.keyBindBack.isKeyDown()) {
            return mc.thePlayer.rotationYaw;
        } else if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            return mc.thePlayer.rotationYaw + 90.0F;
        } else {
            return mc.gameSettings.keyBindRight.isKeyDown() ? mc.thePlayer.rotationYaw - 90.0F : mc.thePlayer.rotationYaw - 180.0F;
        }
    }

    private void getBlock(int switchSlot) {
        for(int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack() && (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory)) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemBlock) {
                    ItemBlock block = (ItemBlock)is.getItem();
                    if (this.isValid(block)) {
                        if (36 + switchSlot != i) {
                            InventoryUtil.swap(i, switchSlot);
                        }
                        break;
                    }
                }
            }
        }

    }

    int getBestSpoofSlot() {
        int spoofSlot = 5;

        for(int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                spoofSlot = i - 36;
                break;
            }
        }

        return spoofSlot;
    }

    public int getSlot() {
        return this.slot;
    }

    static {
        invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.furnace, Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.snow_layer, Blocks.torch, Blocks.anvil, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower, Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder, Blocks.web, Blocks.tnt);
        keepYValue = new BoolValue("Keep Y", false);
    }
}