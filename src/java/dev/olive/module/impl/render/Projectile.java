package dev.olive.module.impl.render;

import org.lwjgl.opengl.GL11;
import org.lwjglx.util.glu.Cylinder;
import org.lwjglx.util.glu.Disk;
import org.lwjglx.util.glu.GLU;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.event.impl.events.EventRender3D;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.utils.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Projectile extends Module {

    float yaw, pitch;

    public Projectile() {
        super("Projectile","抛物线", Category.Render);
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.isPost()) return;
        yaw = e.getYaw();
        pitch = e.getPitch();
    }

    @EventTarget
    public void onR3D(EventRender3D e) {
        boolean finished = false;
        boolean isBow = false;
        float pitchDifference = 0.0F;
        float motionFactor = 1.5F;
        float motionSlowdown = 0.99F;
        if (mc.thePlayer.getCurrentEquippedItem() != null) {
            Item heldItem = mc.thePlayer.getCurrentEquippedItem().getItem();
            float gravity = 0;
            float size = 0;
            if (heldItem instanceof ItemBow) {
                isBow = true;
                gravity = 0.05F;
                size = 0.3F;
                float power = (float) mc.thePlayer.getItemInUseDuration() / 20.0F;
                power = (power * power + power * 2.0F) / 3.0F;

                if ((double) power < 0.1D) {
                    finished = true;
                } else {
                    if (power > 1.0F) {
                        power = 1.0F;
                    }
                    motionFactor = power * 3.0F;
                }

            } else if (heldItem instanceof ItemFishingRod) {
                gravity = 0.04F;
                size = 0.25F;
                motionSlowdown = 0.92F;
            } else if (ItemPotion.isSplash(mc.thePlayer.getCurrentEquippedItem().getMetadata())) {
                gravity = 0.05F;
                size = 0.25F;
                pitchDifference = -20.0F;
                motionFactor = 0.5F;
            } else if (!(heldItem instanceof ItemSnowball) && !(heldItem instanceof ItemEnderPearl)
                    && !(heldItem instanceof ItemEgg) && !(heldItem.equals(Item.getItemById(46)))) {
                finished = true;
            } else {
                gravity = 0.03F;
                size = 0.25F;
            }
            if (!finished) {
                double posX = mc.getRenderManager().renderPosX
                        - (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F);
                double posY = mc.getRenderManager().getRenderPosY() + (double) mc.thePlayer.getEyeHeight()
                        - 0.10000000149011612D;
                double posZ = mc.getRenderManager().getRenderPosZ()
                        - (double) (MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F);
                double motionX = (double) (-MathHelper.sin(yaw / 180.0F * 3.1415927F)
                        * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
                double motionY = (double) (-MathHelper
                        .sin((pitch + pitchDifference) / 180.0F * 3.1415927F))
                        * (isBow ? 1.0D : 0.4D);
                double motionZ = (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F)
                        * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
                float distance = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                motionX /= (double) distance;
                motionY /= (double) distance;
                motionZ /= (double) distance;
                motionX *= (double) motionFactor;
                motionY *= (double) motionFactor;
                motionZ *= (double) motionFactor;
                MovingObjectPosition landingPosition = null;
                boolean hasLanded = false;
                boolean hitEntity = false;
                RenderUtil.enableRender3D(true);
                RenderUtil.color(new Color(255, 255, 255, 255).getRGB());
                GL11.glLineWidth(2.0F);
                GL11.glBegin(1);

                while (!hasLanded && posY > 0.0D) {
                    Vec3 posBefore = new Vec3(posX, posY, posZ);
                    Vec3 posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                    landingPosition = mc.theWorld.rayTraceBlocks(posBefore, posAfter, false, true, false);
                    posBefore = new Vec3(posX, posY, posZ);
                    posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                    if (landingPosition != null) {
                        hasLanded = true;
                        posAfter = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord,
                                landingPosition.hitVec.zCoord);
                    }

                    AxisAlignedBB arrowBox = new AxisAlignedBB(posX - (double) size, posY - (double) size,
                            posZ - (double) size, posX + (double) size, posY + (double) size, posZ + (double) size);
                    List entityList = this
                            .getEntitiesWithinAABB(arrowBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));

                    for (int i = 0; i < entityList.size(); ++i) {
                        Entity var18 = (Entity) entityList.get(i);
                        if (var18.canBeCollidedWith() && var18 != mc.thePlayer) {
                            AxisAlignedBB var2 = var18.getEntityBoundingBox().expand((double) size, (double) size,
                                    (double) size);
                            MovingObjectPosition possibleEntityLanding = var2.calculateIntercept(posBefore, posAfter);
                            if (possibleEntityLanding != null) {
                                hitEntity = true;
                                hasLanded = true;
                                landingPosition = possibleEntityLanding;
                            }
                        }
                    }

                    posX += motionX;
                    posY += motionY;
                    posZ += motionZ;
                    BlockPos var35 = new BlockPos(posX, posY, posZ);
                    Block var36 = mc.theWorld.getBlockState(var35).getBlock();
                    if (var36.getMaterial() == Material.water) {
                        motionX *= 0.6D;
                        motionY *= 0.6D;
                        motionZ *= 0.6D;
                    } else {
                        motionX *= (double) motionSlowdown;
                        motionY *= (double) motionSlowdown;
                        motionZ *= (double) motionSlowdown;
                    }

                    motionY -= (double) gravity;
                    GL11.glVertex3d(posX - mc.getRenderManager().getRenderPosX(),
                            posY - mc.getRenderManager().getRenderPosY(),
                            posZ - mc.getRenderManager().getRenderPosZ());
                }

                GL11.glEnd();
                GL11.glPushMatrix();
                GL11.glTranslated(posX - mc.getRenderManager().getRenderPosX(),
                        posY - mc.getRenderManager().getRenderPosY(),
                        posZ - mc.getRenderManager().getRenderPosZ());

                if (landingPosition != null) {

                    int side = landingPosition.sideHit.getIndex();

                    if (side == 1 && heldItem instanceof ItemEnderPearl) {
                        RenderUtil.color((new Color(44, 189, 162, 192)).getRGB());
                    } else if (side == 2) {
                        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    } else if (side == 3) {
                        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    } else if (side == 4) {
                        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                    } else if (side == 5) {
                        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                    }

                    if (hitEntity) {
                        RenderUtil.color((new Color(44, 189, 162, 192)).getRGB());
                    }

                }

                this.renderPoint();
                GL11.glPopMatrix();
                RenderUtil.disableRender3D(true);
            }

        }
    }

    private void renderPoint() {
        GL11.glBegin(1);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glEnd();
        Disk d1 = new Disk();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);

        d1.setDrawStyle(100011);
        d1.draw(0.4F, 0.4F, 6, 256);
        Disk d2 = new Disk();

        d2.setDrawStyle(100011);
        d2.draw(0.13F, 0.13F, 6, 256);
    }
    private List getEntitiesWithinAABB(AxisAlignedBB axisalignedBB) {
        ArrayList list = new ArrayList();
        int chunkMinX = MathHelper.floor_double((axisalignedBB.minX - 2.0D) / 16.0D);
        int chunkMaxX = MathHelper.floor_double((axisalignedBB.maxX + 2.0D) / 16.0D);
        int chunkMinZ = MathHelper.floor_double((axisalignedBB.minZ - 2.0D) / 16.0D);
        int chunkMaxZ = MathHelper.floor_double((axisalignedBB.maxZ + 2.0D) / 16.0D);

        for (int x = chunkMinX; x <= chunkMaxX; ++x) {
            for (int z = chunkMinZ; z <= chunkMaxZ; ++z) {
                if (mc.theWorld.getChunkProvider().chunkExists(x, z)) {
                    mc.theWorld.getChunkFromChunkCoords(x, z).getEntitiesWithinAABBForEntity(mc.thePlayer,
                            axisalignedBB, list, EntitySelectors.selectAnything);
                }
            }
        }

        return list;
    }
}
