package dev.olive.module.impl.world;


import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.Event;
import dev.olive.event.impl.events.EventRender3D;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.event.impl.events.EventWorldLoad;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.module.impl.render.HUD;
import dev.olive.utils.MSTimer;
import dev.olive.utils.player.Rotation;
import dev.olive.utils.player.RotationUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.value.impl.BoolValue;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import static dev.olive.module.impl.world.Scaffold.getVec3;


@SuppressWarnings("unused")
public class ChestAura extends Module {
    public ChestAura() {
        super("ChestAura","箱子小偷", Category.World);
    }

    public final BoolValue thoughtWall = new BoolValue("ThoughtWall", false);
    public final BoolValue chestOnly = new BoolValue("ChestOnly", false);

    private Rotation needRot;
    private EnumFacing needFacing;
    private MSTimer msTimer = new MSTimer();

    public static ArrayList<BlockPos> openedContainer = new ArrayList<>();

    @Override
    public void onEnable() {
        openedContainer.clear();
    }
    @EventTarget
    private void worldEventHandler(EventWorldLoad event) {
        openedContainer.clear();
    };
    @EventTarget
    private void scannerHandler(EventUpdate event ) {
        try {
            if (Client.instance.moduleManager.getModule(KillAura.class).getState() && KillAura.target != null) return;
            if (Client.instance.moduleManager.getModule(Scaffold.class).getState()) return;
            BlockPos nearestContainer = null;
            double nearestDistance = Double.MAX_VALUE;
            if (mc.currentScreen instanceof GuiContainer) return;
            for (int x = -5; x < 6; x++) {
                for (int y = -5; y < 6; y++) {
                    for (int z = -5; z < 6; z++) {
                        BlockPos fixedBP = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                        if (checkContainerOpenable(fixedBP)) {
                            MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(
                                    new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
                                    new Vec3(fixedBP).add(new Vec3(0.5, 0.5, 0.5)),
                                    false, true, true);
                            if (mc.thePlayer.getDistance(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ()) < 4.5 &&
                                    !openedContainer.contains(fixedBP) && mc.thePlayer.getDistance(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ()) <= nearestDistance &&
                                    isContainer(mc.theWorld.getBlockState(fixedBP).getBlock()) && mop != null &&
                                    mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && (mop.getBlockPos().equals(fixedBP) || thoughtWall.getValue())) {
                                nearestDistance = mc.thePlayer.getDistance(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ());
                                nearestContainer = fixedBP;
                                float[] r = RotationUtil.getRotationBlock(fixedBP);
                                needRot = new Rotation(r[0], r[1]);
                                needFacing = mop.sideHit;
                            }
                        }
                    }
                }
            }

            if (nearestContainer == null) return;
            if (!msTimer.check(500L)) return;
            msTimer.reset();
            Client.instance.getRotationManager().setRotation(needRot, 180f, true);
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), nearestContainer, needFacing, getVec3(nearestContainer, needFacing));
            openedContainer.add(nearestContainer);
        } catch (Throwable e) {}
    };
    @EventTarget
    public void onRender(EventRender3D event) {
        Iterator var2 = mc.theWorld.loadedTileEntityList.iterator();

        while(true) {
            TileEntity tileEntity;
            do {
                if (!var2.hasNext()) {
                    return;
                }

                tileEntity = (TileEntity)var2.next();
            } while(!(tileEntity instanceof TileEntityChest) && !(tileEntity instanceof TileEntityBrewingStand) && !(tileEntity instanceof TileEntityFurnace));

            Color color = openedContainer.contains(tileEntity.getPos()) ? new Color(185, 26, 26, 0) : HUD.color(1);
            if (mc.thePlayer.getDistance(tileEntity.getPos()) < 20.0) {
                RenderUtil.drawBlockBox(tileEntity.getPos(), color, false);
            }

            RenderUtil.renderOne();
            RenderUtil.drawBlockBox(tileEntity.getPos(), color, false);
            RenderUtil.renderTwo();
            RenderUtil.drawBlockBox(tileEntity.getPos(), color, false);
            RenderUtil.renderThree();
            RenderUtil.renderFour(color.getRGB());
            RenderUtil.drawBlockBox(tileEntity.getPos(), color, true);
            RenderUtil.renderFive();
        }
    }
    private boolean isContainer(Block block) {
        return block instanceof BlockChest || ((block instanceof BlockFurnace || block instanceof BlockBrewingStand) && !chestOnly.getValue());
    }

    private boolean checkContainerOpenable(BlockPos blockPos) {
        IBlockState blockState = mc.theWorld.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof BlockChest)) return true;
        IBlockState upBlockState = mc.theWorld.getBlockState(blockPos.add(0, 1, 0));
        if (upBlockState.getBlock().isFullBlock() && !(upBlockState.getBlock() instanceof BlockGlass)) return false;
        return true;
    }
}
