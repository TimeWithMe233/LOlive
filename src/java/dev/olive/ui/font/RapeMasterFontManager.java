package dev.olive.ui.font;

import dev.olive.Client;
import dev.olive.event.impl.events.EventText;
import dev.olive.module.impl.render.HUD;
import dev.olive.utils.render.GradientUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.tuples.mutable.MutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class RapeMasterFontManager extends FontRenderer {
    private static final int[] colorCode = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            int base = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + base;
            int g = (i >> 1 & 1) * 170 + base;
            int b = (i & 1) * 170 + base;
            if (i == 6) {
                r += 85;
            }

            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }

            colorCode[i] = (r & 255) << 16 | (g & 255) << 8 | b & 255;
        }
    }

    public final float drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, (float) (x - getStringWidth(text) / 2), y, color);
    }

    public final float drawCenteredStringNoFormat(String text, float x, float y, int color) {
        return drawStringNoFormat(text, (x - getStringWidth(text) / 2), y, color, false);
    }

    public final void drawCenteredStringWithShadow(String text, float x, float y, int color) {
        drawStringWithShadow(text, (x - getStringWidth(text) / 2), y, color);
    }

    private final byte[][] charwidth = new byte[256][];
    private final int[] textures = new int[256];
    private final FontRenderContext context = new FontRenderContext(new AffineTransform(), true, true);
    private Font font = null;

    private float size = 0;
    private int fontWidth = 0;
    private int fontHeight = 0;
    private int textureWidth = 0;
    private int textureHeight = 0;

    public RapeMasterFontManager(Font font) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
                Minecraft.getMinecraft().getTextureManager(), false);
        this.font = font;
        size = font.getSize2D();
        Arrays.fill(textures, -1);
        Rectangle2D maxBounds = font.getMaxCharBounds(context);
        this.fontWidth = (int) Math.ceil(maxBounds.getWidth());
        this.fontHeight = (int) Math.ceil(maxBounds.getHeight());
        if (fontWidth > 127 || fontHeight > 127) throw new IllegalArgumentException("Font size too large!");
        this.textureWidth = resizeToOpenGLSupportResolution(fontWidth * 16);
        this.textureHeight = resizeToOpenGLSupportResolution(fontHeight * 16);
    }

    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String p_trimStringToWidth_1_, int p_trimStringToWidth_2_, boolean p_trimStringToWidth_3_) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = p_trimStringToWidth_3_ ? p_trimStringToWidth_1_.length() - 1 : 0;
        int k = p_trimStringToWidth_3_ ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < p_trimStringToWidth_1_.length() && i < p_trimStringToWidth_2_; l += k) {
            char c0 = p_trimStringToWidth_1_.charAt(l);
            int i1 = this.getStringWidth(String.valueOf(c0));
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (i1 < 0) {
                flag = true;
            } else {
                i += i1;
                if (flag1) {
                    ++i;
                }
            }

            if (i > p_trimStringToWidth_2_) {
                break;
            }

            if (p_trimStringToWidth_3_) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    public final int getHeight() {
        return fontHeight / 2;
    }

    public final int getFontHeight() {
        return fontHeight / 2;
    }

    protected final int drawChar(char chr, float x, float y) {
        int region = chr >> 8;
        int id = chr & 0xFF;
        int xTexCoord = (id & 0xF) * fontWidth,
                yTexCoord = (id >> 4) * (fontHeight + 5);
        int width = getOrGenerateCharWidthMap(region)[id];
        GlStateManager.bindTexture(getOrGenerateCharTexture(region)); // 怒我直说GlStateManager是世界上最傻逼的东西
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBegin(GL_QUADS);
        glTexCoord2d(wrapTextureCoord(xTexCoord, textureWidth), wrapTextureCoord(yTexCoord, textureHeight));
        glVertex2f(x, y);
        glTexCoord2d(wrapTextureCoord(xTexCoord, textureWidth), wrapTextureCoord(yTexCoord + fontHeight, textureHeight));
        glVertex2f(x, y + fontHeight);
        glTexCoord2d(wrapTextureCoord(xTexCoord + width, textureWidth), wrapTextureCoord(yTexCoord + fontHeight, textureHeight));
        glVertex2f(x + width, y + fontHeight);
        glTexCoord2d(wrapTextureCoord(xTexCoord + width, textureWidth), wrapTextureCoord(yTexCoord, textureHeight));
        glVertex2f(x + width, y);
        glEnd();
        return width;
    }


    public int drawString(String str, float x, float y, int color) {
        return drawString(str, x, y, color, false);
    }

    public final int drawStringNoFormat(String str, float x, float y, int color, boolean darken) {
        EventText event = new EventText(str);
        Client.instance.eventManager.call(event);
        str = event.getText();
        y = y - 2;
        x *= 2;
        y *= 2;
        y -= 2;
        int offset = 0;
        if (darken) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000; // 相当于给RGB每个通道除以4
        }
        float r, g, b, a;
        r = (color >> 16 & 0xFF) / 255f;
        g = (color >> 8 & 0xFF) / 255f;
        b = (color & 0xFF) / 255f;
        a = (color >> 24 & 0xFF) / 255f;
        if (a == 0)
            a = 1;
        GlStateManager.color(r, g, b, a);
        glPushMatrix();
        glScaled(0.5, 0.5, 0.5);
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char chr = chars[i];
            if (chr == '\u00A7' && i != chars.length - 1) {
                i++;
                color = "0123456789abcdef".indexOf(chars[i]);
                if (color != -1) {
                    if (darken) color |= 0x10;
                }
                continue;
            }
            offset += drawChar(chr, x + offset, y);
        }
        glPopMatrix();
        return offset;
    }

    private final List<String> lines = new ArrayList<>();

    //Lines list and height
    public List<String> getWrappedLines(String text, float x, float width, float heightIncrement) {
        wrapTextToLines(text, x, width);
        return lines;
    }

    public float drawWrappedText(String text, float x, float y, int color, float width, float heightIncrement) {
        wrapTextToLines(text, x, width);

        float newY = y;
        for (String s : lines) {
            RenderUtil.resetColor();
            drawString(s, x, newY, color);
            newY += getHeight() + heightIncrement;
        }
        return newY - y;
    }

    public MutablePair<Float, Float> drawNewLineText(String text, float x, float y, int color, float heightIncrement) {
        wrapTextToNewLine(text, x);

        String longest = "";
        float newY = y;
        for (String s : lines) {
            if (getStringWidth(s) > getStringWidth(longest)) {
                longest = s;
            }
            RenderUtil.resetColor();
            drawString(s, x, newY, color);
            newY += getHeight() + heightIncrement;
        }

        return MutablePair.of(getStringWidth(longest), newY - y).pairOfSecond();
    }

    private void wrapTextToNewLine(String text, float x) {
        lines.clear();
        lines.addAll(Arrays.asList(text.trim().split("\n")));
    }

    private void wrapTextToLines(String text, float x, float width) {
        lines.clear();
        String[] words = text.trim().split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            float totalWidth = getStringWidth(line + " " + word);

            if (x + totalWidth >= x + width) {
                lines.add(line.toString());
                line = new StringBuilder(word).append(" ");
                continue;
            }

            line.append(word).append(" ");
        }
        lines.add(line.toString());
    }

    private void drawLine(double x2, double y2, double x1, double y1) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth((float) 1);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public final int drawString(String str, float x, float y, int color, boolean darken) {
        EventText event = new EventText(str);
        Client.instance.eventManager.call(event);
        str = event.getText();
        y = y - 2;
        x *= 2;
        y *= 2;
        y -= 2;
        int offset = 0;
        if (darken) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000; // 相当于给RGB每个通道除以4
        }
        float r, g, b, a;
        r = (color >> 16 & 0xFF) / 255f;
        g = (color >> 8 & 0xFF) / 255f;
        b = (color & 0xFF) / 255f;
        a = (color >> 24 & 0xFF) / 255f;
        if (a == 0)
            a = 1;
        GlStateManager.color(r, g, b, a);
        glPushMatrix();
        glScaled(0.5, 0.5, 0.5);
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char chr = chars[i];
            if (chr == '\u00A7' && i != chars.length - 1) {
                i++;
                color = "0123456789abcdef".indexOf(chars[i]);
                if (color != -1) {
                    if (darken) color |= 0x10;
                    color = colorCode[color];
                    r = (color >> 16 & 0xFF) / 255f;
                    g = (color >> 8 & 0xFF) / 255f;
                    b = (color & 0xFF) / 255f;
                    GlStateManager.color(r, g, b, a);
                }
                continue;
            }
            offset += drawChar(chr, x + offset, y);
        }
        glPopMatrix();
        return offset;
    }

    public float getMiddleOfBox(float height) {
        return height / 2f - getHeight() / 2f;
    }

    public final int getStringWidth(String text) {
        EventText event = new EventText(text);
        Client.instance.eventManager.call(event);
        text = event.getText();
        if (text == null) {
            return 0;
        }
        int width = 0;
        char[] currentData = text.toCharArray();

        int size = text.length();
        int i = 0;
        while (i < size) {
            char chr = currentData[i];

            char character = text.charAt(i);
            if (character == '\u00a7') {
                ++i;
            } else {
                width += getOrGenerateCharWidthMap(chr >> 8)[chr & 0xFF];
            }
            ++i;
        }
        return width / 2;
    }

    public final float getSize() {
        return size;
    }

    private final int generateCharTexture(int id) {
        int textureId = glGenTextures();
        int offset = id << 8;
        BufferedImage img = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(new Color(255, 255, 255, 1));
        g.fillRect(0, 0, textureWidth, textureHeight);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics fontMetrics = g.getFontMetrics();
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++) {
                String chr = String.valueOf((char) ((y << 4 | x) | offset));
                g.drawString(chr, x * fontWidth, y * (fontHeight + 5) + fontMetrics.getAscent());
            }
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageToBuffer(img));
        return textureId;
    }

    private final int getOrGenerateCharTexture(int id) {
        if (textures[id] == -1)
            return textures[id] = generateCharTexture(id);
        return textures[id];
    }

    /**
     * 由于某些显卡不能很好的支持分辨率不是2的n次方的纹理，此处用于缩放到支持的范围内
     */
    private final int resizeToOpenGLSupportResolution(int size) {
        int power = 0;
        while (size > 1 << power) power++;
        return 1 << power;
    }

    private final byte[] generateCharWidthMap(int id) {
        int offset = id << 8;
        byte[] widthmap = new byte[256];
        for (int i = 0; i < widthmap.length; i++) {
            widthmap[i] = (byte) font.getStringBounds(String.valueOf((char) (i | offset)), context).getWidth();

        }
        return widthmap;
    }

    private final byte[] getOrGenerateCharWidthMap(int id) {
        if (charwidth[id] == null)
            return charwidth[id] = generateCharWidthMap(id);
        return charwidth[id];
    }

    public void drawStringDynamic(String text, double x, double y, int tick1, int tick2) {
        drawStringDynamic(text, x, y, tick1, tick2, 1.0F);
    }

    public void drawStringDynamic(String text, double x, double y, int tick1, int tick2, float opacity) {
        GradientUtil.applyGradientHorizontal((float) x, (float) y, (float) getStringWidth(text), getFontHeight(), opacity, HUD.color(tick1), HUD.color(tick2), () -> {
            RenderUtil.setAlphaLimit(0);
            drawString(text, (float) x, (float) y, -1);
        });
    }

    private final double wrapTextureCoord(int coord, int size) {
        return coord / (double) size;
    }

    private static final ByteBuffer imageToBuffer(BufferedImage img) {
        int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * arr.length);

        for (int i : arr) {
            buf.putInt(i << 8 | i >> 24 & 0xFF);
        }

        buf.flip();
        return buf;
    }

    protected final void finalize() throws Throwable {
        for (int textureId : textures) {
            if (textureId != -1)
                glDeleteTextures(textureId);
        }
    }

    public final int drawStringWithShadow(String newstr, float x, float y, int rgb) {
        drawString(newstr, x + 0.5f, y + 0.5f, rgb, true);
        return drawString(newstr, x, y, rgb);
    }


    public final void drawLimitedString(String text, float x, float y, int color, float maxWidth) {
        drawLimitedStringWithAlpha(text, x, y, color, (((color >> 24) & 0xFF) / 255f), maxWidth);
    }

    public final void drawLimitedStringWithAlpha(String text, float x, float y, int color, float alpha, float maxWidth) {
        //   text = processString(text);
        x *= 2.0F;
        y *= 2.0F;
        float originalX = x;
        float curWidth = 0;

        GL11.glPushMatrix();
        GL11.glScaled(0.5F, 0.5F, 0.5F);

        final boolean wasBlend = glGetBoolean(GL_BLEND);
        GlStateManager.enableAlpha();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);

        int currentColor = color;
        char[] characters = text.toCharArray();

        int index = 0;
        for (char c : characters) {
            if (c == '\r') {
                x = originalX;
            }
            if (c == '\n') {
                y += getFontHeight() * 2.0F;
            }
            if (c != '\247' && (index == 0 || index == characters.length - 1 || characters[index - 1] != '\247')) {
                if (index >= 1 && characters[index - 1] == '\247') continue;
                glPushMatrix();
                drawString(Character.toString(c), x, y, RenderUtil.reAlpha(new Color(currentColor), (int) alpha).getRGB(), false);
                glPopMatrix();

                curWidth += (getStringWidth(Character.toString(c)) * 2.0F);
                x += (getStringWidth(Character.toString(c)) * 2.0F);

                if (curWidth > maxWidth) {
                    break;
                }

            } else if (c == ' ') {
                x += getStringWidth(" ");
            } else if (c == '\247' && index != characters.length - 1) {
                int codeIndex = "0123456789abcdefklmnor".indexOf(text.charAt(index + 1));
                if (codeIndex < 0) continue;

                if (codeIndex < 16) {
                    currentColor = colorCode[codeIndex];
                } else if (codeIndex == 21) {
                    currentColor = Color.WHITE.getRGB();
                }
            }

            index++;
        }

        if (!wasBlend)
            glDisable(GL_BLEND);
        glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    public final void drawOutlinedString(String str, float x, float y, int internalCol, int externalCol) {
        this.drawString(str, x - 0.5f, y, externalCol);
        this.drawString(str, x + 0.5f, y, externalCol);
        this.drawString(str, x, y - 0.5f, externalCol);
        this.drawString(str, x, y + 0.5f, externalCol);
        this.drawString(str, x, y, internalCol);
    }

    public void drawStringWithShadow(String z, double x, double positionY, int mainTextColor) {
        drawStringWithShadow(z, (float) x, (float) positionY, mainTextColor);
    }

    public double getStringHeight() {
        return getHeight();
    }


    public float drawStringWithShadow(String text, double x, double y, double sWidth, int color) {
        float shadowWidth = this.drawString(text, (float) (x + sWidth), (float) (y + sWidth), color, true);
        return Math.max(shadowWidth, this.drawString(text, (float) x, (float) y, color, false));
    }
}
