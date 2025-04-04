package dev.olive.ui.hud.impl;

import dev.olive.Client;
import dev.olive.ui.font.FontManager;
import dev.olive.ui.hud.HUD;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.StencilUtil;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static dev.olive.module.Module.isNull;
import static dev.olive.module.impl.render.HUD.markTextValue;
import static dev.olive.utils.render.RenderUtil.bg;


public class Watermark extends HUD {
    public static ModeValue mode = new ModeValue("Mode", new String[]{"Olive", "Naven"}, "Olive");
    public Watermark() {
        super(50, 20, "Watermark","水印");
    }

    @Override
    public void drawShader() {

    }


    @Override
    public void onTick() {
        String clientName = markTextValue.get();

        if (charIndex > clientName.length()) {
            if (clientName.isEmpty()) {
                charIndex = 0;
            } else {
                charIndex = clientName.length() - 1;
            }
        }

        if (clientName.isEmpty()) {
            return;
        }

        updateTick++;

        if (updateTick > 5) {
            if (charIndex > clientName.length() - 1) {
                backward = true;
            } else if (charIndex <= 0) {
                backward = false;
            }
            if (backward) {
                charIndex--;
            } else {
                charIndex++;
            }

            markStr = clientName.substring(0, charIndex);

            updateTick = 0;
        }
    }

    public double calculateBPS() {
        double bps = (Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }

    @Override
    public void predrawhud() {
    }

    int updateTick;
    int charIndex;
    boolean backward;
    String markStr;

    @Override
    public void drawHUD(int xPos, int yPos, float partialTicks) {
        switch (mode.getValue().toLowerCase()) {
            case "olive": {
                String clientName = Client.name;
                String mark = markStr;
                LocalDateTime now = LocalDateTime.now();
                int hour = now.getHour();
                int minute = now.getMinute();
                String text = "§7O§flive" + " | " + Client.instance.user + " | " + hour + " | " + Minecraft.getDebugFPS() + "FPS";
                String title = String.format(EnumChatFormatting.GRAY + "  |  " + EnumChatFormatting.WHITE + Client.instance.user + EnumChatFormatting.GRAY + "  |  " + EnumChatFormatting.WHITE + hour + ":" + minute);
                float width = FontManager.interSemiBold18.getStringWidth(title) + FontManager.interSemiBold18.getStringWidth(clientName) + 4;
//                ShaderElement.addBlurTask(() -> RoundedUtil.drawRound(4, 5, width, FontManager.interSemiBold18.getHeight() + 5, 4, Color.WHITE));
//                RoundedUtil.drawRound(4, 5, width, FontManager.interSemiBold18.getHeight() + 5, 4, new Color(0, 0, 0, 110));
                FontManager.interSemiBold18.drawStringWithShadow(text, 8, 9 + 1, -1);
                break;
            }
            case "naven":{
                // 获取当前时间
                LocalTime currentTime = LocalTime.now();

                // 获取小时、分钟和秒
                int hour = currentTime.getHour();
                int minute = currentTime.getMinute();
                int second = currentTime.getSecond();
                String title = Client.name+" Developer Build"+" | "+Client.instance.user+" | "+Minecraft.getDebugFPS()+"fps"+" | "+hour+":"+minute+":"+second;
                float width = FontManager.interSemiBold18.getStringWidth(title);
                float x = FontManager.interSemiBold16.getStringWidth(Client.name+" Developer Build");
                float x2 = FontManager.font16.getStringWidth(" | ");
                float x3 = FontManager.interSemiBold16.getStringWidth(Client.instance.user);
                float x4 = FontManager.interSemiBold16.getStringWidth(Minecraft.getDebugFPS()+"fps");
                ShaderElement.addBlurTask(()-> RenderUtil.roundedRectangle(4,5,width-15,17,5,Color.WHITE));
                ShaderElement.addBloomTask(()-> RenderUtil.roundedRectangle(4,5,width-15,17,5,Color.BLACK));
                RenderUtil.roundedRectangle(4,5,width-15,17,5,new Color(0xE7252323, true));
                bg(4,5.3f,width-15,17,2,5,new Color(158, 19, 19, 255));
                FontManager.interSemiBold16.drawStringWithShadow(Client.name+" Developer Build",8,12,Color.WHITE.getRGB());
                FontManager.font16.drawString("  |  ",8+x,10,Color.WHITE.brighter().getRGB());
                FontManager.interSemiBold16.drawStringWithShadow(Client.instance.user,8+x+x2+2,12,Color.WHITE.getRGB());
                FontManager.font16.drawString("  |  ",8+x+x2+x3+2,10,Color.WHITE.brighter().getRGB());
                FontManager.interSemiBold16.drawStringWithShadow(Minecraft.getDebugFPS()+"fps",8+x+x2+x3+x2+4,12,Color.WHITE.getRGB());
                FontManager.font16.drawString("  |  ",8+x+x2+x3+x2+x4+4,10,Color.WHITE.brighter().getRGB());
                FontManager.interSemiBold16.drawStringWithShadow(hour+":"+minute+":"+second,8+x+x2+x3+x2+x4+x2+6,12,Color.WHITE.getRGB());

            }

        }

    }


}