package dev.olive.module.impl.player;


import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.event.impl.events.EventRender2D;
import dev.olive.event.impl.events.EventRender3D;
import dev.olive.event.impl.events.EventTick;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.ui.font.FontManager;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.utils.player.RotationUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjglx.util.vector.Vector2f;


import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@SuppressWarnings("unused")
public class BedBreaker extends Module {
    public static double currentDamage;
    private BlockPos currentPos;
    private int bestToolSlot = -1;
    public static boolean skipAb = false;

    static BlockPos whiteListed = new BlockPos(0, 0, 0);
    private final List<BlockPos> targets = new ArrayList<>();
    private final NumberValue radius = new NumberValue("Radius", 3, 3, 5,0.5);

    public BedBreaker() {
        super("BedBreaker","破床者", Category.World);
    }

    protected final Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos((float) (Math.toRadians(-yaw) - (float) Math.PI));
        float f1 = MathHelper.sin((float) (Math.toRadians(-yaw) - (float) Math.PI));
        float f2 = -MathHelper.cos((float) Math.toRadians(-pitch));
        float f3 = MathHelper.sin((float) Math.toRadians(-pitch));
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public Vec3 getPositionEyes(float partialTicks) {
        return new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    private boolean bedAround() {
        for (int x = -radius.getValue().intValue(); x < radius.getValue() + 1; x++) {
            for (int z = -radius.getValue().intValue(); z < radius.getValue() + 1; z++) {
                for (int y = -3; y < 5; y++) {
                    BlockPos pos = new BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (!isWhitelisted(pos) && mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @EventTarget
    private void tickUpdateEventHandler(EventTick event) {
        if (currentPos != null) {
            if (currentDamage == 0) {
                bestToolSlot = mc.thePlayer.getBestToolSlot(currentPos);
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(bestToolSlot));
                }
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, EnumFacing.UP));
                skipAb = true;
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }

            Block block = mc.theWorld.getBlockState(currentPos).getBlock();
            currentDamage += PlayerUtil.getPlayerRelativeBlockHardness(block, currentPos, (bestToolSlot==-1?mc.thePlayer.inventory.currentItem:bestToolSlot));

            if (this.currentDamage >= 1.1F) {
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(bestToolSlot));
                }
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, EnumFacing.UP));
                mc.playerController.onPlayerDestroyBlock(currentPos, EnumFacing.UP);
                this.currentDamage = 0.0F;
                currentPos = null;
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }

            mc.thePlayer.swingItem();
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentPos, (int) (this.currentDamage * 10.0F) - 1);
        }
    };

    @EventTarget
    private void motionEventHandler(EventMotion event) {
 {
            for (int x = -radius.getValue().intValue(); x < radius.getValue() + 1; x++) {
                for (int z = -radius.getValue().intValue(); z < radius.getValue() + 1; z++) {
                    for (int y = -3; y < 5; y++) {
                        BlockPos pos = new BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z);
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (!isWhitelisted(pos) && mc.theWorld.getBlockState(pos).getBlock() ==
                                Blocks.bed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) ==
                                BlockBed.EnumPartType.HEAD) {
                            List<Block> breakQueue = Arrays.asList(Blocks.stained_glass, Blocks.wool, Blocks.stained_hardened_clay, Blocks.planks, Blocks.log, Blocks.log2, Blocks.end_stone, Blocks.obsidian, Blocks.bed);
                            targets.clear();
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.EAST){
                                targets.add(pos.east());
                                targets.add(pos.west().west());
                                targets.add(pos.north().west());
                                targets.add(pos.north());
                                targets.add(pos.south());
                                targets.add(pos.south().west());
                                targets.add(pos.up().west());
                                targets.add(pos.up());
                            }
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.WEST){
                                targets.add(pos.west());
                                targets.add(pos.east().east());
                                targets.add(pos.north().east());
                                targets.add(pos.north());
                                targets.add(pos.south());
                                targets.add(pos.south().east());
                                targets.add(pos.up().east());
                                targets.add(pos.up());
                            }
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.NORTH){
                                targets.add(pos.north());
                                targets.add(pos.south().south());
                                targets.add(pos.east());
                                targets.add(pos.west());
                                targets.add(pos.west().south());
                                targets.add(pos.south().east());
                                targets.add(pos.up().south());
                                targets.add(pos.up());
                            }
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.SOUTH){
                                targets.add(pos.south());
                                targets.add(pos.north().north());
                                targets.add(pos.east());
                                targets.add(pos.west());
                                targets.add(pos.west().north());
                                targets.add(pos.north().east());
                                targets.add(pos.up().north());
                                targets.add(pos.up());
                            }
                            for(BlockPos blockPos : targets){
                                if(mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.air)
                                {
                                    currentPos = pos;
                                    currentDamage = 0;
                                    return;
                                }
                            }
                            targets.sort(Comparator.comparingInt(p -> {
                                Block blockAtPos = mc.theWorld.getBlockState(p).getBlock();
                                int index = breakQueue.indexOf(blockAtPos);
                                return index == -1 ? Integer.MAX_VALUE : index;
                            }));
                            currentPos = targets.get(0);
                            currentDamage = 0;

                        }
                    }
                }
            }
        }
    };
    @EventTarget
    private void render2DEventHandler(EventRender2D event) {
        if (currentDamage != 0) {
            ScaledResolution sc = new ScaledResolution(mc);
            final double percentage = currentDamage;
            FontManager.font20.drawString("Breaking...", sc.getScaledWidth() / 2F - 15, sc.getScaledHeight() / 2F + 30,-1);
            RenderUtil.drawRectWH(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94,5, Color.GRAY.getRGB());
            RenderUtil.drawRectWH(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94 * percentage,5,-1);
        }
    };


    public BlockPos getWhiteListed() {
        return whiteListed;
    }

    public static void setWhiteListed(BlockPos _whiteListed) {
        whiteListed = _whiteListed;
    }

    private boolean isWhitelisted(BlockPos pos) {
        if (pos == null || whiteListed == null) return false;
        return pos.getX() == whiteListed.getX() && pos.getY() == whiteListed.getY() && pos.getZ() == whiteListed.getZ();
    }
}
