package dev.olive.utils.player;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.event.impl.events.EventTick;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.utils.math.Location;
import dev.olive.utils.math.MathUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.lwjglx.util.vector.Vector2f;

import javax.vecmath.Vector3d;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static org.apache.commons.lang3.RandomUtils.nextFloat;

public class RotationUtil {
    public static Minecraft mc = Minecraft.getMinecraft();

    private static final double RAD_TO_DEG = 180.0 / Math.PI;
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static int keepLength;
    public static boolean isEnabled = false;
    private float yaw;
    private float pitch;
    public static boolean strafeFix = true;
    public static boolean strictStrafeFix = false;
    public static float keepRotationTicks = 0;
    public static float[] clientRotation = new float[]{0.0F, 0.0F};
    private static List<Double> xzPercents = Arrays.asList(0.5, 0.4, 0.3, 0.2, 0.1, 0.0, -0.1, -0.2, -0.3, -0.4, -0.5);
    public static Rotation targetRotation;

    public RotationUtil(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
    public static void disable() {
        isEnabled = false;
        keepRotationTicks = 0;
        strafeFix = false;
        strictStrafeFix = true;
        MovementUtil.updateBinds(false);
    }
    public static void setClientRotation(final float[] targetRotation) {
        setClientRotation(targetRotation, 0);
    }

    public static void setClientRotation(final float[] targetRotation, int keepRotation) {
        if (!isEnabled || keepRotationTicks <= 0) {
            isEnabled = true;
            keepRotationTicks = keepRotation;
            clientRotation = targetRotation;
        }

    }

    public static void overrideRotation(final float[] targetRotation) {
        isEnabled = true;
        keepRotationTicks = 1;
        clientRotation = targetRotation;
    }
    public static double getRotationDifference(Entity entity) {
        Vector2f rotation = toRotation(getCenter(entity.getEntityBoundingBox()), true);
        return getRotationDifference(rotation, new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));
    }
    public static double getRotationDifference(float[] e) {
        return getRotationDifference(clientRotation, e);
    }
    public static double getRotationDifference(Vec3 e) {
        float[] entityRotation = getRotations(e.xCoord, e.yCoord, e.zCoord);
        return getRotationDifference(entityRotation);
    }
    public static double getRotationDifference(float[] a, float[] b) {
        final double yawDif = MathHelper.wrapAngleTo180_double(a[0] - b[0]);
        final double pitchDif = MathHelper.wrapAngleTo180_double(a[1] - b[1]);
        return Math.sqrt(yawDif * yawDif + pitchDif * pitchDif);
    }
    public static void setStrafeFix(boolean enabled, boolean strict) {
        strafeFix = enabled;
        strictStrafeFix = strict;
    }
    public static void updateStrafeFixBinds() {
        if (isEnabled) {
            if (strafeFix) {
                if (!strictStrafeFix) {
                    if (MovementUtil.isBindsMoving()) {
                        int strafeYaw = round((clientRotation[0] - MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw)) / 45);
                        if (strafeYaw > 4) {
                            strafeYaw -= 8;
                        }
                        if (strafeYaw < -4) {
                            strafeYaw += 8;
                        }
                        mc.gameSettings.keyBindForward.pressed = abs(strafeYaw) <= 1;
                        mc.gameSettings.keyBindLeft.pressed = strafeYaw >= 1 && strafeYaw <= 3;
                        mc.gameSettings.keyBindBack.pressed = abs(strafeYaw) >= 3;
                        mc.gameSettings.keyBindRight.pressed = strafeYaw >= -3 && strafeYaw <= -1;
                    } else {
                        mc.gameSettings.keyBindForward.pressed = false;
                        mc.gameSettings.keyBindRight.pressed = false;
                        mc.gameSettings.keyBindBack.pressed = false;
                        mc.gameSettings.keyBindLeft.pressed = false;
                    }
                }
            }
        }
    }

    public static void strafeFixBinds(float offsetYaw) {
        if (MovementUtil.isBindsMoving()) {
            int strafeYaw = round(offsetYaw / 45);
            if (strafeYaw > 4) {
                strafeYaw -= 8;
            }
            if (strafeYaw < -4) {
                strafeYaw += 8;
            }
            Minecraft.getGameSettings().keyBindForward.pressed = abs(strafeYaw) <= 1;
            Minecraft.getGameSettings().keyBindLeft.pressed = strafeYaw >= 1 && strafeYaw <= 3;
            Minecraft.getGameSettings().keyBindBack.pressed = abs(strafeYaw) >= 3;
            Minecraft.getGameSettings().keyBindRight.pressed = strafeYaw >= -3 && strafeYaw <= -1;
        } else {
            Minecraft.getGameSettings().keyBindForward.pressed = false;
            Minecraft.getGameSettings().keyBindRight.pressed = false;
            Minecraft.getGameSettings().keyBindBack.pressed = false;
            Minecraft.getGameSettings().keyBindLeft.pressed = false;
        }
    }
    public static float getAngleDifference(float a, float b2) {
        return ((a - b2) % 360.0f + 540.0f) % 360.0f - 180.0f;
    }

    public static double getRotationDifference(Vector2f a, Vector2f b2) {
        return Math.hypot(getAngleDifference(a.getX(), b2.getX()), a.getY() - b2.getY());
    }
    public static float[] getThrowRotation(final Entity entity, final double maxRange) {
        if (entity == null) {
            return null;
        }
        final double diffX = entity.posX - RotationUtil.mc.thePlayer.posX + entity.motionX;
        final double diffZ = entity.posZ - RotationUtil.mc.thePlayer.posZ + entity.motionZ;
        final Vec3 BestPos = getNearestPointBB(RotationUtil.mc.thePlayer.getPositionEyes(1.0f), entity.getEntityBoundingBox());
        final Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + RotationUtil.mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);
        final double diffY = BestPos.yCoord - myEyePos.getY();
        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793) - 90.0f;
        final float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793));
        return new float[] { yaw, pitch };
    }
    public static float getEnumRotations(EnumFacing facing) {
        float yaw = 0;
        if (facing == EnumFacing.NORTH) {
            yaw = 0;
        }
        if (facing == EnumFacing.EAST) {
            yaw = 90;
        }
        if (facing == EnumFacing.WEST) {
            yaw = -90;
        }
        if (facing == EnumFacing.SOUTH) {
            yaw = 180;
        }
        return yaw;
    }


    public static void setVisualRotations(EventMotion e) {
        setVisualRotations(e.getYaw(), e.getPitch());
    }
    public static void setVisualRotations(float yaw, float pitch) {
        mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = yaw;
        mc.thePlayer.rotationPitchHead = pitch;
    }
    public static float changeRotation(float p_706631, float p_706632, float p_706633) {
        float var4 = MathHelper.wrapAngleTo180_float(p_706632 - p_706631);
        if (var4 > p_706633) {
            var4 = p_706633;
        }
        if (var4 < -p_706633) {
            var4 = -p_706633;
        }
        return p_706631 + var4;
    }

    public static float[] getBlockRotations(double x, double y, double z) {
        double var4 = x - RotationUtil.mc.thePlayer.posX + 0.5;
        double var6 = z - RotationUtil.mc.thePlayer.posZ + 0.5;
        double var8 = y - (RotationUtil.mc.thePlayer.posY + (double) RotationUtil.mc.thePlayer.getEyeHeight() - 1.0);
        double var14 = MathHelper.sqrt_double(var4 * var4 + var6 * var6);
        float var12 = (float) (Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0f;
        return new float[]{var12, (float) (-Math.atan2(var8, var14) * 180.0 / Math.PI)};
    }
    public static float[] getRotationsToEntity(EntityLivingBase entity, boolean usePartialTicks) {
        float partialTicks = RotationUtil.mc.timer.renderPartialTicks;
        double entityX = usePartialTicks ? entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks : entity.posX;
        double entityY = usePartialTicks ? entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks : entity.posY;
        double entityZ = usePartialTicks ? entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks : entity.posZ;
        double yDiff = RotationUtil.mc.thePlayer.posY - entityY;
        double finalEntityY = yDiff >= 0.0 ? entityY + (double)entity.getEyeHeight() : (-yDiff < (double)RotationUtil.mc.thePlayer.getEyeHeight() ? RotationUtil.mc.thePlayer.posY + (double)RotationUtil.mc.thePlayer.getEyeHeight() : entityY);
        return RotationUtil.getRotationsToPosition(entityX, finalEntityY, entityZ);
    }

    public static float[] getRotationsToEntity(double x, double y, double z, EntityLivingBase entity, boolean usePartialTicks) {
        float partialTicks = RotationUtil.mc.timer.renderPartialTicks;
        double entityX = usePartialTicks ? entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks : entity.posX;
        double entityY = usePartialTicks ? entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks : entity.posY;
        double entityZ = usePartialTicks ? entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks : entity.posZ;
        double yDiff = RotationUtil.mc.thePlayer.posY - entityY;
        double finalEntityY = yDiff >= 0.0 ? entityY + (double)entity.getEyeHeight() : (-yDiff < (double)RotationUtil.mc.thePlayer.getEyeHeight() ? y + (double)RotationUtil.mc.thePlayer.getEyeHeight() : entityY);
        return RotationUtil.getRotationsToPosition(x, y + (double)RotationUtil.mc.thePlayer.getEyeHeight(), z, entityX, finalEntityY, entityZ);
    }

    public static float[] getRotationsToEntityRandomised(EntityLivingBase entity, boolean usePartialTicks, double randomAmount) {
        float partialTicks = RotationUtil.mc.timer.renderPartialTicks;
        double entityX = usePartialTicks ? entity.lastTickPosX + (entity.posX + (Math.random() * randomAmount - randomAmount * 0.5) - entity.lastTickPosX) * (double)partialTicks : entity.posX + (Math.random() * randomAmount - randomAmount * 0.5);
        double entityY = usePartialTicks ? entity.lastTickPosY + (entity.posY + (Math.random() * randomAmount - randomAmount * 0.5) - entity.lastTickPosY) * (double)partialTicks : entity.posY + (Math.random() * randomAmount - randomAmount * 0.9);
        double entityZ = usePartialTicks ? entity.lastTickPosZ + (entity.posZ + (Math.random() * randomAmount - randomAmount * 0.5) - entity.lastTickPosZ) * (double)partialTicks : entity.posZ + (Math.random() * randomAmount - randomAmount * 0.5);
        double yDiff = RotationUtil.mc.thePlayer.posY - entityY;
        double finalEntityY = yDiff >= 0.0 ? entityY + (double)entity.getEyeHeight() : (-yDiff < (double)RotationUtil.mc.thePlayer.getEyeHeight() ? RotationUtil.mc.thePlayer.posY + (double)RotationUtil.mc.thePlayer.getEyeHeight() : entityY);
        return RotationUtil.getRotationsToPosition(entityX, finalEntityY, entityZ);
    }

    public static AxisAlignedBB getHittableBoundingBox(final Entity entity,
                                                       final double boundingBoxScale) {
        return entity.getEntityBoundingBox().expand(boundingBoxScale, boundingBoxScale, boundingBoxScale);
    }

    public static AxisAlignedBB getHittableBoundingBox(final Entity entity) {
        return getHittableBoundingBox(entity, entity.getCollisionBorderSize());
    }


    /**
     * Get optimal attack hit vec
     *
     * @param mc           Minecraft instance
     * @param src          Ray trace origin {@link RotationUtil#getHitOrigin}
     * @param boundingBox  boundingBox of entity being attacked
     * @param ignoreBlocks If can attack through blocks
     * @param maxRayTraces Max number of ray traces that can be performed (use -1 for max)
     * @return Hit vec
     */
    public static Vec3 getAttackHitVec(final Minecraft mc,
                                       final Vec3 src,
                                       final AxisAlignedBB boundingBox,
                                       final Vec3 desiredHitVec,
                                       final boolean ignoreBlocks,
                                       final int maxRayTraces) {
        // Validate that closest hit vec is legit
        if (validateHitVec(mc, src, desiredHitVec, ignoreBlocks))
            return desiredHitVec;

        // If not find a better hit vec
        double closestDist = Double.MAX_VALUE;
        Vec3 bone = null;

        final double xWidth = boundingBox.maxX - boundingBox.minX;
        final double zWidth = boundingBox.maxZ - boundingBox.minZ;
        final double height = boundingBox.maxY - boundingBox.minY;

        int passes = 0;

        for (double x = 0.0; x < 1.0; x += 0.2) {
            for (double y = 0.0; y < 1.0; y += 0.2) {
                for (double z = 0.0; z < 1.0; z += 0.2) {
                    if (maxRayTraces != -1 && passes > maxRayTraces) return null;

                    final Vec3 hitVec = new Vec3(boundingBox.minX + xWidth * x,
                            boundingBox.minY + height * y,
                            boundingBox.minZ + zWidth * z);

                    final double dist;
                    if (validateHitVec(mc, src, hitVec, ignoreBlocks) &&
                            (dist = src.distanceTo(hitVec)) < closestDist) {

                        closestDist = dist;
                        bone = hitVec;
                    }

                    passes++;
                }
            }
        }

        return bone;
    }
    /**
     * Get the closest point on a boundingBox from start
     *
     * @param start       Src
     * @param boundingBox boundingBox to calculate closest point from start
     * @return The closest point on boundingBox as a hit vec
     */
    public static Vec3 getClosestPoint(final Vec3 start,
                                       final AxisAlignedBB boundingBox) {
        final double closestX = start.xCoord >= boundingBox.maxX ? boundingBox.maxX :
                start.xCoord <= boundingBox.minX ? boundingBox.minX :
                        boundingBox.minX + (start.xCoord - boundingBox.minX);

        final double closestY = start.yCoord >= boundingBox.maxY ? boundingBox.maxY :
                start.yCoord <= boundingBox.minY ? boundingBox.minY :
                        boundingBox.minY + (start.yCoord - boundingBox.minY);

        final double closestZ = start.zCoord >= boundingBox.maxZ ? boundingBox.maxZ :
                start.zCoord <= boundingBox.minZ ? boundingBox.minZ :
                        boundingBox.minZ + (start.zCoord - boundingBox.minZ);

        return new Vec3(closestX, closestY, closestZ);
    }

    public static Vec3 getCenterPointOnBB(final AxisAlignedBB hitBox,
                                          final double progressToTop) {
        final double xWidth = hitBox.maxX - hitBox.minX;
        final double zWidth = hitBox.maxZ - hitBox.minZ;
        final double height = hitBox.maxY - hitBox.minY;
        return new Vec3(hitBox.minX + xWidth / 2.0, hitBox.minY + height * progressToTop, hitBox.minZ + zWidth / 2.0);
    }

    /**
     * @param entity Entity to get hit origin from
     * @return hit origin of entity (eye pos)
     */
    public static Vec3 getHitOrigin(final Entity entity) {
        return new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
    }

    public static boolean validateHitVec(final Minecraft mc,
                                         final Vec3 src,
                                         final Vec3 dst,
                                         final boolean ignoreBlocks,
                                         final double penetrationDist) {
        final Vec3 blockHitVec = rayTraceHitVec(mc, src, dst);
        // If not return closest
        if (blockHitVec == null) return true;
        // Get the distance of the hit (a.k.a the reach)
        final double distance = src.distanceTo(dst);
        // If ignoreBlocks & distance passed < penetrationDist use the closest
        return ignoreBlocks && distance < penetrationDist;
    }

    public static boolean validateHitVec(final Minecraft mc,
                                         final Vec3 src,
                                         final Vec3 dst,
                                         final boolean ignoreBlocks) {
        // Max vanilla penetration range
        return validateHitVec(mc, src, dst, ignoreBlocks, 2.8);
    }

    public static Vec3 rayTraceHitVec(final Minecraft mc,
                                      final Vec3 src,
                                      final Vec3 dst) {
        final MovingObjectPosition rayTraceResult = mc.theWorld.rayTraceBlocks(src, dst,
                false,
                false,
                false);

        return rayTraceResult != null ? rayTraceResult.hitVec : null;
    }


    public static float[] getAngles(AxisAlignedBB axisAlignedBB) {
        final EntityPlayerSP thePlayer = mc.thePlayer;
        Vec3 BestPos = getNearestPointBB(mc.thePlayer.getPositionEyes(1f), axisAlignedBB);
        Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY +
                mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);

        double diffY;
        double diffZ;
        double diffX;

        diffY = BestPos.yCoord - myEyePos.getY();
        diffZ = BestPos.zCoord - myEyePos.getZ();
        diffX = BestPos.xCoord - myEyePos.getX();

        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ); // @on

        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F,
                pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
        return new float[]{thePlayer.rotationYaw + MathHelper.wrapDegrees(yaw - thePlayer.rotationYaw), thePlayer.rotationPitch + MathHelper.wrapDegrees(pitch - thePlayer.rotationPitch)};
    }
    public static float[] getAngles(Entity entity) {
        if (entity == null)
            return null;
        final EntityPlayerSP thePlayer = mc.thePlayer;

        final double diffX = entity.posX - thePlayer.posX,
                diffY = entity.posY + entity.getEyeHeight() * 0.9 - (thePlayer.posY + thePlayer.getEyeHeight()),
                diffZ = entity.posZ - thePlayer.posZ, dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ); // @on

        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F,
                pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
        return new float[]{thePlayer.rotationYaw + MathHelper.wrapDegrees(yaw - thePlayer.rotationYaw), thePlayer.rotationPitch + MathHelper.wrapDegrees(pitch - thePlayer.rotationPitch)};
    }
    public static float[] getRotationsNeeded(final Entity entity) {
        if (entity == null) {
            return null;
        }
        Minecraft mc = Minecraft.getMinecraft();
        final double xSize = entity.posX - mc.thePlayer.posX;
        final double ySize = entity.posY + entity.getEyeHeight() / 2 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double zSize = entity.posZ - mc.thePlayer.posZ;
        final double theta = MathHelper.sqrt_double(xSize * xSize + zSize * zSize);
        final float yaw = (float) (Math.atan2(zSize, xSize) * 180 / Math.PI) - 90;
        final float pitch = (float) (-(Math.atan2(ySize, theta) * 180 / Math.PI));
        return new float[]{(mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw)) % 360, (mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)) % 360.0f};
    }
    public static Vector2f calculateSimple(final Entity entity, double range, double wallRange) {
        AxisAlignedBB aabb = entity.getEntityBoundingBox().contract(-0.05, -0.05, -0.05).contract(0.05, 0.05, 0.05);
        range += 0.05;
        wallRange += 0.05;
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1F);
        Vec3 nearest = new Vec3(
                MathUtils.clamp(eyePos.xCoord, aabb.minX, aabb.maxX),
                MathUtils.clamp(eyePos.yCoord, aabb.minY, aabb.maxY),
                MathUtils.clamp(eyePos.zCoord, aabb.minZ, aabb.maxZ)
        );
        Vector2f rotation = toRotation(nearest, false);
        if (nearest.subtract(eyePos).lengthSquared() <= wallRange * wallRange) {
            return rotation;
        }

        MovingObjectPosition result = RaytraceUtil.rayCast(rotation, range, 0F, false);
        final double maxRange = Math.max(wallRange, range);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && result.entityHit == entity && result.hitVec.subtract(eyePos).lengthSquared() <= maxRange * maxRange) {
            return rotation;
        }

        return null;
    }

    public static Vector2f calculate(final Entity entity, final boolean adaptive, final double range, final double wallRange, boolean predict, boolean randomCenter) {
        if (mc.thePlayer == null) return null;

        final double rangeSq = range * range;
        final double wallRangeSq = wallRange * wallRange;

        Vector2f simpleRotation = calculateSimple(entity, range, wallRange);
        if (simpleRotation != null) return simpleRotation;

        Vector2f normalRotations = toRotation(getVec(entity), predict);

        if (!randomCenter) {
            MovingObjectPosition normalResult = RaytraceUtil.rayCast(normalRotations, range, 0F, false);
            if (normalResult != null && normalResult.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                return normalRotations;
            }
        }

        double yStart = 1, yEnd = 0, yStep = -0.5;
        if (randomCenter && MathUtils.secureRandom.nextBoolean()) {
            yStart = 0;
            yEnd = 1;
            yStep = 0.5;
        }
        for (double yPercent = yStart; Math.abs(yEnd - yPercent) > 1e-3; yPercent += yStep) {
            double xzStart = 0.5, xzEnd = -0.5, xzStep = -0.1;
            if (randomCenter) {
                Collections.shuffle(xzPercents);
            }
            for (double xzPercent : xzPercents) {
                for (int side = 0; side <= 3; side++) {
                    double xPercent = 0F, zPercent = 0F;
                    switch (side) {
                        case 0: {
                            xPercent = xzPercent;
                            zPercent = 0.5F;
                            break;
                        }
                        case 1: {
                            xPercent = xzPercent;
                            zPercent = -0.5F;
                            break;

                        }
                        case 2: {
                            xPercent = 0.5F;
                            zPercent = xzPercent;
                            break;

                        }
                        case 3: {
                            xPercent = -0.5F;
                            zPercent = xzPercent;
                            break;
                        }
                    }
                    Vec3 Vec3 = getVec(entity).add(
                            new Vec3((entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) * xPercent,
                                    (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * yPercent,
                                    (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) * zPercent));
                    double distanceSq = Vec3.squareDistanceTo(mc.thePlayer.getPositionEyes(1F));

                    Rotation rotation = toRotationRot(Vec3, predict);
                    rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity);
                    rotation.distanceSq = distanceSq;

                    if (distanceSq <= wallRangeSq) {
                        MovingObjectPosition result = RaytraceUtil.rayCast(rotation.toVec2f(), wallRange, 0F, true);
                        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            return rotation.toVec2f();
                        }
                    }

                    if (distanceSq <= rangeSq) {
                        MovingObjectPosition result = RaytraceUtil.rayCast(rotation.toVec2f(), range, 0F, false);
                        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            return rotation.toVec2f();
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Vec3 getVec(Entity entity) {
        return new Vec3(entity.posX, entity.posY, entity.posZ);
    }



    public static float[] getBlockPosRotation(BlockPos pos) {
        return getRotationFromPosition(pos.getX(), pos.getZ(), pos.getY());
    }

    public static float[] getRotationFromPosition(double x, double z, double y) {
        double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        double yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 1.2;

        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
        return new float[]{yaw, pitch};
    }

    public static Vector2f getRotationFromEyeToPoint(javax.vecmath.Vector3d point3d) {
        return calculate(new Vector3d(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), point3d);
    }

    public static Vector2f calculate(final Entity entity) {
        return calculate(entity.getCustomPositionVector().add(0, Math.max(0, Math.min(mc.thePlayer.posY - entity.posY +
                mc.thePlayer.getEyeHeight(), (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 0.9)), 0));
    }

    public static Vector2f calculate(Vector3d from, Vector3d to) {

        final double x = to.getX() - from.getX();
        final double y = to.getY() - from.getY();
        final double z = to.getZ() - from.getZ();

        final double sqrt = Math.sqrt(x * x + z * z);

        final float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90F;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(y, sqrt)));

        return new Vector2f(yaw, Math.min(Math.max(pitch, -90), 90));
    }

    public static float getMoveYaw(float yaw) {
        Vector2f from = new Vector2f((float) mc.thePlayer.lastTickPosX, (float) mc.thePlayer.lastTickPosZ),
                to = new Vector2f((float) mc.thePlayer.posX, (float) mc.thePlayer.posZ),
                diff = new Vector2f(to.x - from.x, to.y - from.y);

        double x = diff.x, z = diff.y;
        if (x != 0 && z != 0) {
            yaw = (float) Math.toDegrees((Math.atan2(-x, z) + MathHelper.PI2) % MathHelper.PI2);
        }
        return yaw;
    }

    public static float[] getRotationNormal(EntityLivingBase target) {
        double xDiff = target.posX - mc.thePlayer.posX;
        double yDiff = (target.posY + (target.getEyeHeight() / 5 * 4) - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight()));
        return getRotationFloat(target, xDiff, yDiff);
    }

    private static float[] getRotationFloat(EntityLivingBase target, double xDiff, double yDiff) {
        double zDiff = target.posZ - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);

        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180 / Math.PI) - 90f;
        float pitch = (float) ((-Math.atan2(yDiff, dist)) * 180 / Math.PI);
        float[] array = new float[2];
        int n = 0;
        float rotationYaw = mc.thePlayer.rotationYaw;
        array[n] = rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);
        int n3 = 1;
        float rotationPitch = mc.thePlayer.rotationPitch;
        array[n3] = rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch);
        return array;
    }


    public static Vector2f calculate(final dev.olive.utils.vector.Vector3d from, final dev.olive.utils.vector.Vector3d to) {

        final dev.olive.utils.vector.Vector3d diff = to.subtract(from);
        final double distance = Math.hypot(diff.getX(), diff.getZ());
        final float yaw = (float) (MathHelper.atan2(diff.getZ(), diff.getX()) * MathHelper.TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.getY(), distance) * MathHelper.TO_DEGREES));
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f calculate(final Vec3 to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), new dev.olive.utils.vector.Vector3d(to.xCoord, to.yCoord, to.zCoord));
    }

    public static Vector2f calculate(final dev.olive.utils.vector.Vector3d to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), to);
    }

    public static Vector2f calculate(final dev.olive.utils.vector.Vector3d position, final EnumFacing enumFacing) {
        double x = position.getX() + 0.5D;
        double y = position.getY() + 0.5D;
        double z = position.getZ() + 0.5D;

        x += (double) enumFacing.getDirectionVec().getX() * 0.5D;
        y += (double) enumFacing.getDirectionVec().getY() * 0.5D;
        z += (double) enumFacing.getDirectionVec().getZ() * 0.5D;
        return calculate(new dev.olive.utils.vector.Vector3d(x, y, z));
    }

    //get the rotation by vec3
    private static float[] getRotationsByVec(final Vec3 origin, final Vec3 position) {
        final Vec3 difference = position.subtract(origin);
        final double distance = difference.flat().lengthVector();
        final float yaw = (float) Math.toDegrees(Math.atan2(difference.zCoord, difference.xCoord)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(difference.yCoord, distance)));
        return new float[]{yaw, pitch};
    }

    //get the rotation to block pos

    public static float[] getRotationBlock(final BlockPos pos) {
        IBlockState bs = mc.theWorld.getBlockState(pos);
        AxisAlignedBB blockBB = bs.getBlock().getCollisionBoundingBox(mc.theWorld, pos, bs);
        double x = Math.abs(blockBB.maxX - blockBB.minX) / 2.0;
        double y = Math.abs(blockBB.maxY - blockBB.minY) / 2.0;
        double z = Math.abs(blockBB.maxZ - blockBB.minZ) / 2.0;
        return getRotationsByVec(mc.thePlayer.getPositionVector().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0), new Vec3(pos.getX() + x, pos.getY() + y, pos.getZ() + z));
    }

    public static float[] positionRotation(final double posX, final double posY, final double posZ, final float[] lastRots, final float yawSpeed, final float pitchSpeed, final boolean random) {
        final double x = posX - mc.thePlayer.posX;
        final double y = posY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double z = posZ - mc.thePlayer.posZ;
        final float calcYaw = (float) (MathHelper.atan2(z, x) * 180.0 / 3.141592653589793 - 90.0);
        final float calcPitch = (float) (-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / 3.141592653589793));
        float yaw = updateRotation(lastRots[0], calcYaw, yawSpeed);
        float pitch = updateRotation(lastRots[1], calcPitch, pitchSpeed);
        if (random) {
            yaw += (float) ThreadLocalRandom.current().nextGaussian();
            pitch += (float) ThreadLocalRandom.current().nextGaussian();
        }
        return new float[]{yaw, pitch};
    }

    public static int wrapAngleToDirection(float yaw, int zones) {
        int angle = (int) ((double) (yaw + (float) (360 / (2 * zones))) + 0.5) % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle / (360 / zones);
    }

    public static float getGCD() {
        return (float) (Math.pow(mc.gameSettings.mouseSensitivity * 0.6 + 0.2, 3) * 1.2);
    }

    public static Vector2f getRotationFromEyeToPointOffset(Vec3 position, EnumFacing enumFacing) {
        double x = position.xCoord + 0.5D;
        double y = position.yCoord + 0.5D;
        double z = position.zCoord + 0.5D;

        x += (double) enumFacing.getDirectionVec().getX() * 0.5D;
        y += (double) enumFacing.getDirectionVec().getY() * 0.5D;
        z += (double) enumFacing.getDirectionVec().getZ() * 0.5D;
        return getRot(new Vec3(x, y, z));
    }

    public static Vector2f getRot(Vec3 pos) {

        Vec3 vec = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        final double x = pos.xCoord - vec.xCoord;
        final double y = pos.yCoord - vec.yCoord;
        final double z = pos.zCoord - vec.zCoord;

        final double sqrt = Math.sqrt(x * x + z * z);

        final float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90F;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(y, sqrt)));

        return new Vector2f(yaw, Math.min(Math.max(pitch, -90), 90));
    }

    public static float[] getRotationsToPosition(double x, double y, double z) {
        double deltaX = x - mc.thePlayer.posX;
        double deltaY = y - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
        double deltaZ = z - mc.thePlayer.posZ;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(-Math.atan2(deltaX, deltaZ));
        float pitch = (float) Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));

        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsToPosition(double x, double y, double z, double targetX, double targetY, double targetZ) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(-Math.atan2(dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, horizontalDistance));

        return new float[]{yaw, pitch};
    }

    public static float[] scaffoldRots(final double bx, final double by, final double bz, final float lastYaw, final float lastPitch, final float yawSpeed, final float pitchSpeed, final boolean random) {
        final double x = bx - RotationUtil.mc.thePlayer.posX;
        final double y = by - (RotationUtil.mc.thePlayer.posY + RotationUtil.mc.thePlayer.getEyeHeight());
        final double z = bz - RotationUtil.mc.thePlayer.posZ;
        final float calcYaw = (float) (Math.toDegrees(MathHelper.atan2(z, x)) - 90.0);
        final float calcPitch = (float) (-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / 3.141592653589793));
        float pitch = updateRotation(lastPitch, calcPitch, pitchSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        float yaw = updateRotation(lastYaw, calcYaw, yawSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        if (random) {
            yaw += (float) ThreadLocalRandom.current().nextDouble(-2.0, 2.0);
            pitch += (float) ThreadLocalRandom.current().nextDouble(-0.2, 0.2);
        }
        return new float[]{yaw, pitch};
    }

    public static float[] mouseSens(float yaw, float pitch, final float lastYaw, final float lastPitch) {
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        if (yaw == lastYaw && pitch == lastPitch) {
            return new float[]{yaw, pitch};
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaX = (int) ((6.667 * yaw - 6.667 * lastYaw) / f2);
        final int deltaY = (int) ((6.667 * pitch - 6.667 * lastPitch) / f2) * -1;
        final float f3 = deltaX * f2;
        final float f4 = deltaY * f2;
        yaw = (float) (lastYaw + f3 * 0.15);
        final float f5 = (float) (lastPitch - f4 * 0.15);
        pitch = MathHelper.clamp_float(f5, -90.0f, 90.0f);
        return new float[]{yaw, pitch};
    }

    public static float rotateToYaw(final float yawSpeed, final float currentYaw, final float calcYaw) {
        float yaw = updateRotation(currentYaw, calcYaw, yawSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        final double diffYaw = MathHelper.wrapAngleTo180_float(calcYaw - currentYaw);
        if (-yawSpeed > diffYaw || diffYaw > yawSpeed) {
            yaw += (float) (RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin(mc.thePlayer.rotationPitch * 3.141592653589793));
        }
        if (yaw == currentYaw) {
            return currentYaw;
        }
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaX = (int) ((6.667 * yaw - 6.666666666666667 * currentYaw) / f2);
        final float f3 = deltaX * f2;
        yaw = (float) (currentYaw + f3 * 0.15);
        return yaw;
    }

    public static float updateRotation(final float current, final float calc, final float maxDelta) {
        float f = MathHelper.wrapAngleTo180_float(calc - current);
        if (f > maxDelta) {
            f = maxDelta;
        }
        if (f < -maxDelta) {
            f = -maxDelta;
        }
        return current + f;
    }

    public static float rotateToPitch(final float pitchSpeed, final float currentPitch, final float calcPitch) {
        float pitch = updateRotation(currentPitch, calcPitch, pitchSpeed + nextFloat(0.0f, 15.0f));
        if (pitch != calcPitch) {
            pitch += (float) (nextFloat(1.0f, 2.0f) * Math.sin(mc.thePlayer.rotationYaw * 3.141592653589793));
        }
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaY = (int) ((6.667 * pitch - 6.666667 * currentPitch) / f2) * -1;
        final float f3 = deltaY * f2;
        final float f4 = (float) (currentPitch - f3 * 0.15);
        pitch = MathHelper.clamp_float(f4, -90.0f, 90.0f);
        return pitch;
    }

    /**
     * Get the center of a box
     *
     * @param bb your box
     * @return center of box
     */
    public static Vec3 getCenter(final AxisAlignedBB bb) {
        return new Vec3(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5);
    }

    /**
     * Translate vec to rotation
     *
     * @param vec     target vec
     * @param predict predict new location of your body
     * @return rotation
     */
    public static Vector2f toRotation(final Vec3 vec, final boolean predict) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY +
                mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Vector2f(MathHelper.wrapAngleTo180_float(
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F
        ), MathHelper.wrapAngleTo180_float(
                (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }

    public static RotationNew toRotation(final Vec3 vec, float partialTicks) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY +
                mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ).addVector(mc.thePlayer.motionX * partialTicks, mc.thePlayer.motionY * partialTicks, mc.thePlayer.motionZ * partialTicks);
        return new RotationNew(eyesPos, vec);
    }

    public static Rotation toRotationRot(final Vec3 vec, final boolean predict) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY +
                mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Rotation(MathHelper.wrapAngleTo180_float(
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F
        ), MathHelper.wrapAngleTo180_float(
                (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }



    public static float[] getHVHRotation(Entity entity, double maxRange) {
        if (entity == null) {
            return null;
        } else {
            double diffX = entity.posX - mc.thePlayer.posX;
            double diffZ = entity.posZ - mc.thePlayer.posZ;
            Vec3 BestPos = getNearestPointBB(mc.thePlayer.getPositionEyes(1f), entity.getEntityBoundingBox());
            Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY +
                    mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);

            double diffY;

            diffY = BestPos.yCoord - myEyePos.getY();
            double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
            float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
            return new float[]{yaw, pitch};
        }
    }

    public static float[] getRotationsNeededBlock(double x, double y, double z) {
        double diffX = x + 0.5 - Minecraft.getMinecraft().thePlayer.posX;
        double diffZ = z + 0.5 - Minecraft.getMinecraft().thePlayer.posZ;
        double diffY = y + 0.5 - (Minecraft.getMinecraft().thePlayer.posY + (double) Minecraft.getMinecraft().thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) (-Math.atan2(diffY, dist) * 180.0 / 3.141592653589793);
        return new float[]{Minecraft.getMinecraft().thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - Minecraft.getMinecraft().thePlayer.rotationYaw), Minecraft.getMinecraft().thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - Minecraft.getMinecraft().thePlayer.rotationPitch)};
    }


    public static float[] getRotations(final Vec3 start,
                                       final Vec3 dst) {
        final double xDif = dst.xCoord - start.xCoord;
        final double yDif = dst.yCoord - start.yCoord;
        final double zDif = dst.zCoord - start.zCoord;

        final double distXZ = Math.sqrt(xDif * xDif + zDif * zDif);

        return new float[]{
                (float) (Math.atan2(zDif, xDif) * RAD_TO_DEG) - 90.0F,
                (float) (-(Math.atan2(yDif, distXZ) * RAD_TO_DEG))
        };
    }


    public static void applySmoothing(final Vector2f lastRotations,
                                      final float smoothing,
                                      final float[] dstRotation) {
        if (smoothing > 0.0F) {
            final float yawChange = MathHelper.wrapAngleTo180_float(dstRotation[0] - lastRotations.getX());
            final float pitchChange = MathHelper.wrapAngleTo180_float(dstRotation[1] - lastRotations.getY());

            final float smoothingFactor = Math.max(1.0F, smoothing / 10.0F);

            dstRotation[0] = lastRotations.getX() + yawChange / smoothingFactor;
            dstRotation[1] = Math.max(Math.min(90.0F, lastRotations.getY() + pitchChange / smoothingFactor), -90.0F);
        }
    }

    public static float[] getRotations(final Vector2f lastRotations,
                                       final float smoothing,
                                       final Vec3 start,
                                       final Vec3 dst) {
        // Get rotations from start - dst
        final float[] rotations = getRotations(start, dst);
        // Apply smoothing to them
        applySmoothing(lastRotations, smoothing, rotations);
        return rotations;
    }

    public static float[] getRotations(BlockPos blockPos, EnumFacing enumFacing) {
        double d = (double) blockPos.getX() + 0.5 - mc.thePlayer.posX + (double) enumFacing.getFrontOffsetX() * 0.25;
        double d2 = (double) blockPos.getZ() + 0.5 - mc.thePlayer.posZ + (double) enumFacing.getFrontOffsetZ() * 0.25;
        double d3 = mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight() - blockPos.getY() - (double) enumFacing.getFrontOffsetY() * 0.25;
        double d4 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float) (Math.atan2(d3, d4) * 180.0 / Math.PI);
        return new float[]{MathHelper.wrapAngleTo180_float(f), f2};
    }




    public static Vec3 getVectorForRotation(final Vector2f rotation) {
        float yawCos = MathHelper.cos(-rotation.getX() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getX() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getY() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getY() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static float[] getRotations(Vec3 vec) {
        return getRotations(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static float[] getRotations(double x, double y, double z) {
        final Vec3 lookVec = mc.thePlayer.getPositionEyes(1.0f);
        final double dx = lookVec.xCoord - x;
        final double dy = lookVec.yCoord - y;
        final double dz = lookVec.zCoord - z;

        final double dist = Math.hypot(dx, dz);
        final double yaw = Math.toDegrees(Math.atan2(dz, dx));
        final double pitch = Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{
                (float) yaw + 90,
                (float) pitch
        };
    }

    public static float[] getRotations(Entity entity) {
        double pX = Minecraft.getMinecraft().thePlayer.posX;
        double pY = Minecraft.getMinecraft().thePlayer.posY + (double) Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double pZ = Minecraft.getMinecraft().thePlayer.posZ;
        double eX = entity.posX;
        double eY = entity.posY + (double) (entity.height / 2.0f);
        double eZ = entity.posZ;
        double dX = pX - eX;
        double dY = pY - eY;
        double dZ = pZ - eZ;
        double dH = Math.sqrt(Math.pow(dX, 2.0) + Math.pow(dZ, 2.0));
        double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
        double pitch = Math.toDegrees(Math.atan2(dH, dY));
        return new float[]{(float) yaw, (float) (90.0 - pitch)};
    }

    public static Vec3 getNearestPointBB(Vec3 eye, AxisAlignedBB box) {
        double[] origin = {eye.xCoord, eye.yCoord, eye.zCoord};
        double[] destMins = {box.minX, box.minY, box.minZ};
        double[] destMaxs = {box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i < 3; i++) {
            if (origin[i] > destMaxs[i]) {
                origin[i] = destMaxs[i];
            } else if (origin[i] < destMins[i]) {
                origin[i] = destMins[i];
            }
        }

        return new Vec3(origin[0], origin[1], origin[2]);
    }


    public static Vector2f toRotationMisc(final Vec3 vec, final boolean predict) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict)
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Vector2f(MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F),
                MathHelper.wrapAngleTo180_float(
                        (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))));
    }

    public static float getTrajAngleSolutionLow(float d3, float d1, float velocity) {
        float g = 0.006F;
        float sqrt = velocity * velocity * velocity * velocity - g * (g * (d3 * d3) + 2.0F * d1 * (velocity * velocity));
        return (float) Math.toDegrees(Math.atan((velocity * velocity - Math.sqrt(sqrt)) / (g * d3)));
    }

    /**
     * Rotation from Flux , maxrange for killaura range get + 1
     */
    public static float getBowRot(Entity entity) {

        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        Location BestPos = new Location(entity.posX, entity.posY, entity.posZ);
        Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY +
                mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);

        double diffY;
        for (diffY = entity.boundingBox.minY + 0.7D; diffY < entity.boundingBox.maxY - 0.1D; diffY += 0.1D) {
            if (myEyePos.distanceTo(new Location(entity.posX, diffY, entity.posZ)) < myEyePos.distanceTo(BestPos)) {
                BestPos = new Location(entity.posX, diffY, entity.posZ);
            }
        }

        diffY = BestPos.getY() - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
        return yaw;
    }


    public static float getRotation(float currentRotation, float targetRotation, float maxIncrement) {
        float deltaAngle = MathHelper.wrapAngleTo180_float(targetRotation - currentRotation);
        if (deltaAngle > maxIncrement) {
            deltaAngle = maxIncrement;
        }
        if (deltaAngle < -maxIncrement) {
            deltaAngle = -maxIncrement;
        }
        return currentRotation + deltaAngle / 2.0f;
    }

    public static Vector2f resetRotation(final Vector2f rotation) {
        if (rotation == null) {
            return null;
        }

        final float yaw = mc.thePlayer.rotationYaw;
        final float pitch = mc.thePlayer.rotationPitch;
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f applySensitivityPatch(final Vector2f rotation, final Vector2f previousRotation) {
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }


    public static Vector2f smoothReal(Vector2f targetRotation) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        float randomYaw = (float) (Math.random() * 2.0 - 1.0) / 10.0f;
        float randomPitch = (float) (Math.random() * 2.0 - 1.0) / 10.0f;
        Vector2f rotations = new Vector2f(yaw += randomYaw, pitch += randomPitch);
        yaw = MathHelper.wrapDegrees(rotations.x);
        pitch = MathHelper.clamp_float(rotations.y, -90.0f, 90.0f);
        return new Vector2f(yaw, pitch);
    }


    public static Vector2f smooth(final Vector2f targetRotation) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f smooth(final Vector2f lastRotation, final Vector2f targetRotation, final double speed) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;

        return new Vector2f(yaw, pitch);
    }

    public static Vector2f applySensitivityPatch(final Vector2f rotation) {
        final Vector2f previousRotation = mc.thePlayer.getPreviousRotation();
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    //get moving direction
    public static float getYawDirection(float yaw, float strafe, float moveForward) {
        float rotationYaw = yaw;

        if (moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (moveForward < 0F)
            forward = -0.5F;
        else if (moveForward > 0F)
            forward = 0.5F;

        if (strafe > 0F)
            rotationYaw -= 90F * forward;

        if (strafe < 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }

    //get clamp rotation
    public static float getClampRotation() {
        float rotationYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
        float n = 1.0f;
        if (Minecraft.getMinecraft().thePlayer.movementInput.moveForward < 0.0f) {
            rotationYaw += 180.0f;
            n = -0.5f;
        } else if (Minecraft.getMinecraft().thePlayer.movementInput.moveForward > 0.0f) {
            n = 0.5f;
        }
        if (Minecraft.getMinecraft().thePlayer.movementInput.moveStrafe > 0.0f) {
            rotationYaw -= 90.0f * n;
        }
        if (Minecraft.getMinecraft().thePlayer.movementInput.moveStrafe < 0.0f) {
            rotationYaw += 90.0f * n;
        }
        return rotationYaw * 0.017453292f;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @EventTarget
    public void onTick(final EventTick event) {
        if (targetRotation != null) {
            keepLength--;

            if (keepLength <= 0)
                reset();
        }
    }

    public static void reset() {
        keepLength = 0;
        targetRotation = null;
    }

}