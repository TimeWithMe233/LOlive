package dev.olive.utils.render;

import dev.olive.utils.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

public class ColorUtil {
    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }
    public static int swapAlpha(int color, float alpha) {
        int f = color >> 16 & 0xFF;
        int f1 = color >> 8 & 0xFF;
        int f2 = color & 0xFF;
        return hexColor(f, f1, f2, (int) alpha);
    }
    public static int hexColor(int red, int green, int blue, int alpha) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int getColor2(Color color1, Color color2, long ms, int offset) {
        double scale = (double)((System.currentTimeMillis() + (long)offset) % ms) / (double)ms * 2.0;
        double finalScale = scale > 1.0 ? 2.0 - scale : scale;
        return ColorUtil.getGradient(color1, color2, finalScale).getRGB();
    }
    static Color getGradient(Color color1, Color color2, double scale) {
        scale = Math.max(0.0, Math.min(1.0, scale));
        return new Color((int)((double)color1.getRed() + (double)(color2.getRed() - color1.getRed()) * scale), (int)((double)color1.getGreen() + (double)(color2.getGreen() - color1.getGreen()) * scale), (int)((double)color1.getBlue() + (double)(color2.getBlue() - color1.getBlue()) * scale));
    }
    public static int[] getFractionIndicies(final float[] fractions, final float progress) {
        final int[] range = new int[2];
        int startPoint;
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    static void glColor(final int hex) {
        final float a = (hex >> 24 & 0xFF) / 255.0F;
        final float r = (hex >> 16 & 0xFF) / 255.0F;
        final float g = (hex >> 8 & 0xFF) / 255.0F;
        final float b = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(r, g, b, a);
    }

    public static int darkersb(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public static int swapAlphasb(int color, float alpha) {
        int f = color >> 16 & 0xFF;
        int f1 = color >> 8 & 0xFF;
        int f2 = color & 0xFF;
        return ColorUtil.getColor(f, f1, f2, (int) alpha);
    }

    public static int getOverallColorFrom(int color1, int color2, float percentTo2) {
        final int finalRed = (int) net.optifine.util.MathUtils.lerp(color1 >> 16 & 0xFF, color2 >> 16 & 0xFF, percentTo2),
                finalGreen = (int) net.optifine.util.MathUtils.lerp(color1 >> 8 & 0xFF, color2 >> 8 & 0xFF, percentTo2),
                finalBlue = (int) net.optifine.util.MathUtils.lerp(color1 & 0xFF, color2 & 0xFF, percentTo2),
                finalAlpha = (int) net.optifine.util.MathUtils.lerp(color1 >> 24 & 0xFF, color2 >> 24 & 0xFF, percentTo2);
        return new Color(finalRed, finalGreen, finalBlue, finalAlpha).getRGB();
    }

    public static int interpolateColorsb(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return interpolateColorC(cColor1, cColor2, amount).getRGB();
    }

    public static int applyOpacitysb(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    public static int getAlphaFromColor(int color) {
        return color >> 24 & 0xFF;
    }

    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float) ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        } else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        } else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        } else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color3 = null;
        try {
            color3 = new Color(red, green, blue);
        } catch (IllegalArgumentException exp) {
            final NumberFormat nf = NumberFormat.getNumberInstance();
            System.out.println(nf.format(red) + "; " + nf.format(green) + "; " + nf.format(blue));
            exp.printStackTrace();
        }
        return color3;
    }


    public static Color blendColors(final float[] fractions, final Color[] colors, final float progress) {
        if (fractions == null) {
            throw new IllegalArgumentException("Fractions can't be null");
        }
        if (colors == null) {
            throw new IllegalArgumentException("Colours can't be null");
        }
        if (fractions.length == colors.length) {
            final int[] indicies = getFractionIndicies(fractions, progress);
            final float[] range = {fractions[indicies[0]], fractions[indicies[1]]};
            final Color[] colorRange = {colors[indicies[0]], colors[indicies[1]]};
            final float max = range[1] - range[0];
            final float value = progress - range[0];
            final float weight = value / max;
            final Color color = blend(colorRange[0], colorRange[1], 1.0f - weight);
            return color;
        }
        throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
    }

    public static Color getBlendColor(double current, double max) {
        final long base = Math.round(max / 5);
        if (current >= base * 5) return new Color(15, 255, 15);
        else if (current >= base * 4) return new Color(165, 255, 0);
        else if (current >= base * 3) return new Color(255, 190, 0);
        else if (current >= base * 2) return new Color(255, 90, 0);
        else return new Color(255, 0, 0);
    }

    public static Color getRandomColor() {
        return new Color(Color.HSBtoRGB((float) Math.random(), (float) (.5 + Math.random() / 2), (float) (.5 + Math.random() / 2f)));
    }

    public static int overwriteAlphaComponent(final int colour, final int alphaComponent) {
        final int red = colour >> 16 & 0xFF;
        final int green = colour >> 8 & 0xFF;
        final int blue = colour & 0xFF;

        return ((alphaComponent & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                (blue & 0xFF);
    }

    public static int getColor(final int red, final int green, final int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = MathHelper.clamp_int(alpha, 0, 255) << 24;
        color |= MathHelper.clamp_int(red, 0, 255) << 16;
        color |= MathHelper.clamp_int(green, 0, 255) << 8;
        color |= MathHelper.clamp_int(blue, 0, 255);

        return color;
    }

    public static Color tripleColor(int rgbValue) {
        return tripleColor(rgbValue, 1);
    }

    public static Color tripleColor(int rgbValue, float alpha) {
        alpha = Math.min(1, Math.max(0, alpha));
        return new Color(rgbValue, rgbValue, rgbValue, (int) (255 * alpha));
    }

    public static int removeAlphaComponent(int colour) {
        int red = colour >> 16 & 255;
        int green = colour >> 8 & 255;
        int blue = colour & 255;
        return (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }

    public static Color getRainbow() {
        return new Color(Color.HSBtoRGB((float) ((double) Minecraft.getMinecraft().thePlayer.ticksExisted / 50.0 + Math.sin((double) 1 / 50.0 * 1.6)) % 1.0f, 0.5f, 1.0f));
    }

    public static Color[] getAnalogousColor(Color color) {
        Color[] colors = new Color[2];
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        float degree = 30 / 360f;

        float newHueAdded = hsb[0] + degree;
        colors[0] = new Color(Color.HSBtoRGB(newHueAdded, hsb[1], hsb[2]));

        float newHueSubtracted = hsb[0] - degree;

        colors[1] = new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));

        return colors;
    }


    //RGB TO HSL AND HSL TO RGB FOUND HERE: https://gist.github.com/mjackson/5311256
    public static Color hslToRGB(float[] hsl) {
        float red, green, blue;

        if (hsl[1] == 0) {
            red = green = blue = 1;
        } else {
            float q = hsl[2] < .5 ? hsl[2] * (1 + hsl[1]) : hsl[2] + hsl[1] - hsl[2] * hsl[1];
            float p = 2 * hsl[2] - q;

            red = hueToRGB(p, q, hsl[0] + 1 / 3f);
            green = hueToRGB(p, q, hsl[0]);
            blue = hueToRGB(p, q, hsl[0] - 1 / 3f);
        }

        red *= 255;
        green *= 255;
        blue *= 255;

        return new Color((int) red, (int) green, (int) blue);
    }


    public static float hueToRGB(float p, float q, float t) {
        float newT = t;
        if (newT < 0) newT += 1;
        if (newT > 1) newT -= 1;
        if (newT < 1 / 6f) return p + (q - p) * 6 * newT;
        if (newT < .5f) return q;
        if (newT < 2 / 3f) return p + (q - p) * (2 / 3f - newT) * 6;
        return p;
    }

    public static float[] rgbToHSL(Color rgb) {
        float red = rgb.getRed() / 255f;
        float green = rgb.getGreen() / 255f;
        float blue = rgb.getBlue() / 255f;

        float max = Math.max(Math.max(red, green), blue);
        float min = Math.min(Math.min(red, green), blue);
        float c = (max + min) / 2f;
        float[] hsl = new float[]{c, c, c};

        if (max == min) {
            hsl[0] = hsl[1] = 0;
        } else {
            float d = max - min;
            hsl[1] = hsl[2] > .5 ? d / (2 - max - min) : d / (max + min);

            if (max == red) {
                hsl[0] = (green - blue) / d + (green < blue ? 6 : 0);
            } else if (max == blue) {
                hsl[0] = (blue - red) / d + 2;
            } else if (max == green) {
                hsl[0] = (red - green) / d + 4;
            }
            hsl[0] /= 6;
        }
        return hsl;
    }


    public static Color imitateTransparency(Color backgroundColor, Color accentColor, float percentage) {
        return new Color(ColorUtil.interpolateColor(backgroundColor, accentColor, (255 * percentage) / 255));
    }

    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    //Opacity value ranges from 0-1
    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static Color darker(Color color, float FACTOR) {
        return new Color(Math.max((int) (color.getRed() * FACTOR), 0),
                Math.max((int) (color.getGreen() * FACTOR), 0),
                Math.max((int) (color.getBlue() * FACTOR), 0),
                color.getAlpha());
    }

    public static int darker(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public static Color brighter(Color color, float FACTOR) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255),
                alpha);
    }

    /**
     * This method gets the average color of an image
     * performance of this goes as O((width * height) / step)
     */
    public static Color averageColor(BufferedImage bi, int width, int height, int pixelStep) {
        int[] color = new int[3];
        for (int x = 0; x < width; x += pixelStep) {
            for (int y = 0; y < height; y += pixelStep) {
                Color pixel = new Color(bi.getRGB(x, y));
                color[0] += pixel.getRed();
                color[1] += pixel.getGreen();
                color[2] += pixel.getBlue();
            }
        }
        int num = (width * height) / (pixelStep * pixelStep);
        return new Color(color[0] / num, color[1] / num, color[2] / num);
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static Color rainbow() {
        final Color currentColor = new Color(Color.HSBtoRGB((System.nanoTime() + 400000L) / 1.0E10f % 1.0f, 1.0f, 1.0f));
        return new Color(currentColor.getRed() / 255.0f, currentColor.getGreen() / 255.0f, currentColor.getBlue() / 255.0f, currentColor.getAlpha() / 255.0f);
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? ColorUtil.interpolateColorHue(start, end, angle / 360f) : ColorUtil.interpolateColorC(start, end, angle / 360f);
    }

    //The next few methods are for interpolating colors
    public static int interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return interpolateColorC(color1, color2, amount).getRGB();
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return interpolateColorC(cColor1, cColor2, amount).getRGB();
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(MathUtils.interpolateInt(color1.getRed(), color2.getRed(), amount),
                MathUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                MathUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount),
                MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), MathUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(),
                MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }


    //Fade a color in and out with a specified alpha value ranging from 0-1
    public static Color fade(int speed, int index, Color color, float alpha) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;

        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f));

        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255))));
    }


    private static float getAnimationEquation(int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return ((angle > 180 ? 360 - angle : angle) + 180) / 360f;
    }

    public static int[] createColorArray(int color) {
        return new int[]{bitChangeColor(color, 16), bitChangeColor(color, 8), bitChangeColor(color, 0), bitChangeColor(color, 24)};
    }

    public static int getOppositeColor(int color) {
        int R = bitChangeColor(color, 0);
        int G = bitChangeColor(color, 8);
        int B = bitChangeColor(color, 16);
        int A = bitChangeColor(color, 24);
        R = 255 - R;
        G = 255 - G;
        B = 255 - B;
        return R + (G << 8) + (B << 16) + (A << 24);
    }

    private static int bitChangeColor(int color, int bitChange) {
        return (color >> bitChange) & 255;
    }

}
