package dev.olive.utils;

// Mymylesaws's Shit code(2019)!

import dev.olive.utils.player.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class SuperLib {
    public static int id = 0;
    public static int id2 = 1;
    private static final Minecraft mc = Minecraft.getMinecraft();

    //render
    public static String removeColorCode(String text) {
        String finalText = text;
        if (text.contains("§")) {
            for (int i = 0; i < finalText.length(); ++i) {
                if (Character.toString(finalText.charAt(i)).equals("§")) {
                    try {
                        String part1 = finalText.substring(0, i);
                        String part2 = finalText.substring(Math.min(i + 2, finalText.length()));
                        finalText = part1 + part2;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return finalText;
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = 0.003921569F * (float) c.getRed();
        float g = 0.003921569F * (float) c.getGreen();
        float b = 0.003921569F * (float) c.getBlue();
        return (new Color(r, g, b, alpha)).getRGB();
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static double getAnimationState(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }

    public static float getAnimationState(float animation, float finalState, float speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }

    public static double getAnimationState2(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }

    //movement
    public static void setSpeed(double speed) {
        mc.thePlayer.motionX = -Math.sin(PlayerUtil.getDirection()) * speed;
        mc.thePlayer.motionZ = Math.cos(PlayerUtil.getDirection()) * speed;
    }

    public static double getSpeed() {
        return Math.sqrt(Minecraft.getMinecraft().thePlayer.motionX * Minecraft.getMinecraft().thePlayer.motionX + Minecraft.getMinecraft().thePlayer.motionZ * Minecraft.getMinecraft().thePlayer.motionZ);
    }
    //input


    //enum
    public enum Colors {
        BLACK(-16711423),
        BLUE(-12028161),
        DARKBLUE(-12621684),
        GREEN(-9830551),
        DARKGREEN(-9320847),
        WHITE(-65794),
        AQUA(-7820064),
        DARKAQUA(-12621684),
        GREY(-9868951),
        DARKGREY(-14342875),
        RED(-65536),
        DARKRED(-8388608),
        ORANGE(-29696),
        DARKORANGE(-2263808),
        YELLOW(-256),
        DARKYELLOW(-2702025),
        MAGENTA(-18751),
        DARKMAGENTA(-2252579);

        public int c;

        Colors(int co) {
            this.c = co;
        }
    }
}
