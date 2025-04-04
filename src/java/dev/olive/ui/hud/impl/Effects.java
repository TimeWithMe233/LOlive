package dev.olive.ui.hud.impl;

import dev.olive.module.Module;
import dev.olive.ui.font.FontManager;
import dev.olive.ui.font.RapeMasterFontManager;
import dev.olive.ui.hud.HUD;
import dev.olive.utils.render.*;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.ContinualAnimation;
import dev.olive.utils.render.animation.impl.EaseBackIn;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.ModeValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static dev.olive.utils.render.RenderUtil.*;
//import static dev.olive.module.impl.render.HUD.styleValue;

public class Effects extends HUD {

    public Effects() {
        super(25, 120, "Effects","药水显示");
    }

    public static int offsetValue = 0;
    private final Map<Potion, PotionData> potionMap = new HashMap<>();
    private final Map<Integer, Integer> potionMaxDurations = new HashMap<>();
    private final ContinualAnimation widthanimation = new ContinualAnimation();
    private final ContinualAnimation heightanimation = new ContinualAnimation();
    private final EaseBackIn animation = new EaseBackIn(200, 1F, 1.3F);
    List<PotionEffect> effects = new ArrayList<>();
    public static Color color1;
    public static Color color2;
    @Override
    public void drawShader() {

    }


    @Override
    public void onTick() {

    }

    private int maxString = 0;

    @Override
    public void drawHUD(int x, int y, float partialTicks) {

        setHeight((int) this.heightanimation.getOutput() - 1);
        setWidth(25);
        this.effects = Effects.mc.thePlayer.getActivePotionEffects().stream().sorted(Comparator.comparingInt(it -> FontManager.font16.getStringWidth(this.get(String.valueOf(it))))).collect(Collectors.toList());
        int offsetX = 21;
        int offsetY = 14;
        int i2 = 14;
        ArrayList<Integer> needRemove = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> entry : this.potionMaxDurations.entrySet()) {
            if (Effects.mc.thePlayer.getActivePotionEffect(Potion.potionTypes[entry.getKey()]) != null)
                continue;
            needRemove.add(entry.getKey());
        }
        Iterator iterator = needRemove.iterator();
        while (iterator.hasNext()) {
            int id = (Integer) iterator.next();
            this.potionMaxDurations.remove(id);
        }

        float height = this.effects.size() * 17 + 20;
        this.heightanimation.animate(height, 20);
        if (!(Effects.mc.currentScreen instanceof GuiChat)) {
            this.animation.setDirection(Direction.BACKWARDS);
        }
        RenderUtil.scaleStart(x + 50, y + 15, (float) this.animation.getOutput());
        FontManager.font16.drawStringWithShadow("Potion Example", (float) x + 50.0f - (float) (FontManager.font16.getStringWidth("Potion Example") / 2), y + 15 - FontManager.font16.getHeight() / 2, new Color(255, 255, 255, 60).getRGB());
        RenderUtil.scaleEnd();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableLighting();
        int l = 16;


        RoundedUtil.drawRound(x, (float) (y ), 100f, (float) ((int) this.heightanimation.getOutput()) - 20,
                4, new Color(0, 0, 0, 110));

        java.util.List<PotionEffect> potions2 = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        for (PotionEffect effect : potions2) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = I18n.format(potion.getName());
            Color c = new Color(potion.getLiquidColor());
            String time = get(" " + Potion.getDurationString(effect) + "");

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            String s1 = name;
            String s2 = time;
            FontManager.font16.drawString(s1.replaceAll("(?<=[a-z])(?=[A-Z])", " "), x + 6.0f, y + i2 - offsetY + 5, Color.WHITE.getRGB());
            FontManager.font16.drawString(s2, x + 79.0f, y + i2 - offsetY + 5, Color.GRAY.getRGB());

            int finalI = i2;

            i2 += l;
            if (this.maxString >= Effects.mc.fontRendererObj.getStringWidth(s1)) continue;
            this.maxString = Effects.mc.fontRendererObj.getStringWidth(s1);
        }


    }


    final ContinualAnimation heightAnimation = new ContinualAnimation();

    @Override
    public void predrawhud() {

    }

    public static String get(String text) {
        return text;
    }
    private String get(PotionEffect potioneffect) {
        Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
        String s1 = I18n.format(potion.getName(), new Object[0]);
        s1 = s1 + " " + intToRomanByGreedy(potioneffect.getAmplifier() + 1);
        return s1;
    }
    private String intToRomanByGreedy(int num) {
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.length && num >= 0; i++)
            while (values[i] <= num) {
                num -= values[i];
                stringBuilder.append(symbols[i]);
            }

        return stringBuilder.toString();
    }
}
