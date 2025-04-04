package dev.olive.module.impl.render;


import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.Gapple;
import dev.olive.ui.font.FontManager;
import dev.olive.utils.IMinecraft;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.utils.render.shader.ShaderElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemFood;

import java.awt.*;

public class ProgessBar extends Module {
    public ProgessBar() {
        super("ProgessBar","进度条", Category.Render);
    }

    private long eatStartTime = 0;
    private boolean isEating = false;

    private boolean hasEaten = false;

    public static final Animation anim = new DecelerateAnimation(250, 1);

    public void renderProgessBar3() {
        if (isNull()) return;
        ScaledResolution sr = new ScaledResolution(mc);

        float width = 75;
        anim.setDirection(IMinecraft.mc.thePlayer.isEating() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!IMinecraft.mc.thePlayer.isEating() && anim.isDone()) return;
        float output = (float) anim.getOutput();

        String text = "SendC03(s): " + Gapple.eattick + "/32";
        float textWidth = FontManager.interSemiBold18.getStringWidth(text);
        float totalWidth = ((textWidth + 3) + 6) * output;
        float x, y;
        x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
        y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
        if (IMinecraft.mc.thePlayer.getHeldItem() != null) {
            if (IMinecraft.mc.thePlayer.isEating() && IMinecraft.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                if (!isEating) {
                    isEating = true;
                    eatStartTime = System.currentTimeMillis();
                    hasEaten = false;
                }
                float timerSpeed = IMinecraft.mc.timer.timerSpeed;
                long duration = (long) (1500 / timerSpeed);
                long elapsedTime = System.currentTimeMillis() - eatStartTime;
                float progress = Math.min(1.0f, elapsedTime / (float) duration);
                float fillWidth = width * progress;
                String progressText = String.format("%.0f", progress * 100);
                RenderUtil.scissorStart(x - 1.5f, y - 1.5f, totalWidth + 3, 46);
                ShaderElement.addBlurTask(() -> {
                    RoundedUtil.drawRound(x, y, totalWidth * output, 43, 4, new Color(255, 255, 255, 255));
                });
                RoundedUtil.drawRound(x, y, totalWidth * output, 43, 4, new Color(0, 0, 0, 110));
                RoundedUtil.drawRound((x + 3) * output, y + 25, textWidth * output, 5, 2, new Color(166, 164, 164, 81));
                RoundedUtil.drawGradientHorizontal((x + 3), y + 25, fillWidth * output, 5, 2, HUD.color(1), HUD.color(6));
                FontManager.interSemiBold18.drawString("Eating: " + progressText + "/100", (x + 3), y + 9.5f, -1);
                RenderUtil.scissorEnd();

                if (elapsedTime >= duration && !hasEaten) {
                    hasEaten = true;
                }
            } else {
                isEating = false;
                hasEaten = false;
            }

        }
    }
};


