package dev.olive.utils;

import dev.olive.utils.player.MovementUtil;
import dev.olive.utils.player.RotationUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

import java.util.HashMap;
import java.util.Map;

import static dev.olive.utils.HelperUtil.mc;

public class BlockUtil {


    public static boolean isAirBlock(final BlockPos blockPos) {
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockAir;
    }


    public static double getScaffoldPriority(BlockPos blockPos) {
        return getCenterDistance(blockPos) + Math.abs(MathHelper.wrapAngleTo180_double(getCenterRotation(blockPos)[0] - (mc.thePlayer.rotationYaw + 180)))/130;
    }
    public static double getCenterDistance(BlockPos blockPos) {
        return mc.thePlayer.getDistance(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
    }

    public static boolean isValidBock(final BlockPos blockPos) {
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace) && !(block instanceof BlockLadder) && !(block instanceof BlockTNT);
    }
    public static boolean isReplaceable(BlockPos blockPos) {
        Material material = getMaterial(blockPos);
        return material != null && material.isReplaceable();
    }
    public static float[] getFaceRotation(EnumFacing face, BlockPos blockPos) {
        Vec3i faceVec = face.getDirectionVec();
        Vec3 blockFaceVec = new Vec3(faceVec.getX() * 0.5, faceVec.getY() * 0.5, faceVec.getZ() * 0.5);
        blockFaceVec = blockFaceVec.add(blockPos.toVec3());
        blockFaceVec = blockFaceVec.addVector(0.5, 0.5, 0.5);
        return RotationUtil.getRotations(blockFaceVec);
    }
    public static float[] getCloseFaceRotation(EnumFacing face, BlockPos blockPos) {
        Vec3i faceVec = face.getDirectionVec();

        // Set the min and max ranges for each coordinate based on faceVec
        float minX, maxX, minY, maxY, minZ, maxZ;

        if (faceVec.getX() == 0) {
            minX = 0.1f;
            maxX = 0.9f;
        } else if (faceVec.getX() == 1) {
            minX = maxX = 1.0f;
        } else if (faceVec.getX() == -1) {
            minX = maxX = 0.0f;
        } else {
            // Fallback if necessary (should not happen with standard EnumFacing)
            minX = 0.1f;
            maxX = 0.9f;
        }

        if (faceVec.getY() == 0) {
            minY = 0.1f;
            maxY = 0.9f;
        } else if (faceVec.getY() == 1) {
            minY = maxY = 1.0f;
        } else if (faceVec.getY() == -1) {
            minY = maxY = 0.0f;
        } else {
            minY = 0.1f;
            maxY = 0.9f;
        }

        if (faceVec.getZ() == 0) {
            minZ = 0.1f;
            maxZ = 0.9f;
        } else if (faceVec.getZ() == 1) {
            minZ = maxZ = 1.0f;
        } else if (faceVec.getZ() == -1) {
            minZ = maxZ = 0.0f;
        } else {
            minZ = 0.1f;
            maxZ = 0.9f;
        }

        // Get a default rotation based on the face
        float[] bestRot = getFaceRotation(face, blockPos);
        double bestDist = RotationUtil.getRotationDifference(bestRot);

        // Iterate over a grid of candidate positions on the block face.
        // If min == max, the loop will only run once for that coordinate.
        for (float x = minX; x <= maxX; x += 0.1f) {
            for (float y = minY; y <= maxY; y += 0.1f) {
                for (float z = minZ; z <= maxZ; z += 0.1f) {
                    // Create a candidate hit position in local block coordinates
                    Vec3 candidateLocal = new Vec3(x, y, z);
                    // Convert to world coordinates by adding the block position
                    Vec3 candidateWorld = candidateLocal.add(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

                    double diff = RotationUtil.getRotationDifference(candidateWorld);
                    if (diff < bestDist) {
                        bestDist = diff;
                        bestRot = RotationUtil.getRotations(candidateWorld);
                    }
                }
            }
        }

        return bestRot;
    }
    public static float[] getCenterRotation(BlockPos blockPos) {
        return RotationUtil.getRotations(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
    }

    public static EnumFacing getHorizontalFacingEnum(BlockPos blockPos, double x, double z) {
        double dx = x - (blockPos.getX() + 0.5);
        double dz = z - (blockPos.getZ() + 0.5);

        if (dx > 0) {
            if (dz > dx) {
                return EnumFacing.SOUTH;
            } else if (-dz > dx) {
                return EnumFacing.NORTH;
            } else {
                return EnumFacing.EAST;
            }
        } else {
            if (dz > -dx) {
                return EnumFacing.SOUTH;
            } else if (dz < dx) {
                return EnumFacing.NORTH;
            } else {
                return EnumFacing.WEST;
            }
        }
    }

    public static Material getMaterial(BlockPos blockPos) {
        Block block = getBlock(blockPos);
        if (block != null) {
            return block.getMaterial();
        }
        return null;
    }


    public static BlockPos getBlockCorner(BlockPos start, BlockPos end) {
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                for (int z = 0; z <= 1; ++z) {
                    BlockPos pos = new BlockPos(end.getX() + x, end.getY() + y, end.getZ() + z);
                    if (!isBlockBetween(start, pos)) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isAir(BlockPos blockPos) {
        Material material = getMaterial(blockPos);
        return  material == Material.air;
    }
    public static boolean isBlockBetween(BlockPos start, BlockPos end) {
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();
        int endX = end.getX();
        int endY = end.getY();
        int endZ = end.getZ();
        double diffX = (double) (endX - startX);
        double diffY = (double) (endY - startY);
        double diffZ = (double) (endZ - startZ);
        double x = (double) startX;
        double y = (double) startY;
        double z = (double) startZ;
        double STEP = 0.1D;
        int STEPS = (int) Math.max(Math.abs(diffX), Math.max(Math.abs(diffY), Math.abs(diffZ))) * 4;

        for (int i = 0; i < STEPS - 1; ++i) {
            x += diffX / (double) STEPS;
            y += diffY / (double) STEPS;
            z += diffZ / (double) STEPS;
            if (x != (double) endX || y != (double) endY || z != (double) endZ) {
                BlockPos pos = new BlockPos(x, y, z);
                Block block = RotationUtil.mc.theWorld.getBlockState(pos).getBlock();
                if (block.getMaterial() != Material.air && block.getMaterial() != Material.water && !(block instanceof BlockVine) && !(block instanceof BlockLadder)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Block getBlock(final BlockPos blockPos) {
        return RotationUtil.mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static Map<BlockPos, Block> searchBlocks(final int radius) {
        final Map<BlockPos, Block> blocks = new HashMap<BlockPos, Block>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    final BlockPos blockPos = new BlockPos(RotationUtil.mc.thePlayer.lastTickPosX + x, RotationUtil.mc.thePlayer.lastTickPosY + y, RotationUtil.mc.thePlayer.lastTickPosZ + z);
                    final Block block = getBlock(blockPos);
                    blocks.put(blockPos, block);
                }
            }
        }
        return blocks;
    }
}
