package dev.olive.ui.gui.main;

import java.awt.Color;


import dev.olive.ui.font.FontManager;
import dev.olive.utils.render.animation.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

public final class SimpleButton
        extends GuiButton {
    private int color = 170;
    private double animation = 0.0;

    public SimpleButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x - (int)((double) FontManager.font15.getStringWidth(buttonText) / 2.0), y, FontManager.font15.getStringWidth(buttonText), 10, buttonText);
    }
    @Override
    public void playPressSound(SoundHandler soundHandlerIn){

    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        this.mouseDragged(mc, mouseX, mouseY);
        if (this.hovered) {
            if (this.color < 255) {
                this.color += 5;
            }
            if (this.animation < (double)this.width / 2.0) {
                this.animation = AnimationUtils.animate((double)this.width / 2.0, this.animation, 8f/Minecraft.getDebugFPS());
            }
        } else {
            if (this.color > 170) {
                this.color -= 5;
            }
            if (this.animation > 0.0) {
                this.animation = AnimationUtils.animate(0.0, this.animation, 0.1f);
            }
        }
        SimpleButton.drawRect((double)this.xPosition + (double)this.width / 2.0 - this.animation, this.yPosition + this.height - 2, (double)this.xPosition + (double)this.width / 2.0 + this.animation, this.yPosition + this.height - 1, new Color(this.color, this.color, this.color).getRGB());
        FontManager.font15.drawCenteredString(this.displayString, this.xPosition + this.width / 2f, this.yPosition + (this.height - 8) / 2f, -1);
    }
}

