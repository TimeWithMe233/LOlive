//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.olive.ui.hud.impl;

import dev.olive.Client;

import dev.olive.ui.font.FontManager;
import dev.olive.ui.hud.HUD;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.shader.ShaderElement;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerList extends HUD {
    private final DecimalFormat DF_1 = new DecimalFormat("0");
    private float anmitY = 0.0F;
    private float maxTextWidth = 0.0F;

    public PlayerList() {
        super(200, 100, "PlayerList","玩家列表");
    }

    public void drawShader() {
    }

    public void predrawhud() {
    }

    public void onTick() {
    }

    public void drawHUD(int xPos, int yPos, float partialTicks) {
        this.render(true);
    }

    private void render(boolean font) {
        this.setWidth(150);
        this.setHeight((int)this.anmitY);
        double Y = (double)this.getPosY();
        double textX = (double)((float)this.getPosX() + 5.3F);
        double textY = Y + (double)((float)this.getHeight() / 2.0F) - (double)((float)FontManager.font20.getHeight() / 8.0F) - 12.0;
        double renderPlayerY = Y + 4.0;
        if (mc.thePlayer != null && mc.theWorld != null) {
            List<EntityPlayer> players = (List)mc.theWorld.playerEntities.stream().filter((p) -> {
                return p != null && !p.isDead;
            }).collect(Collectors.toList());
            this.anmitY = (float)RenderUtil.getAnimationState((double)this.anmitY, (double)(29 + (players.size() - 1) * FontManager.font18.getHeight()), 90.0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ShaderElement.addBlurTask(() -> {
                RenderUtil.roundedRectangle(textX, (double)((float)(Y - 10.0)), 130.0, (double)this.anmitY, 5.0, Color.WHITE);
            });
            ShaderElement.addBloomTask(() -> {
                RenderUtil.roundedRectangle(textX, (double)((float)(Y - 10.0)), 130.0, (double)this.anmitY, 5.0, Color.BLACK);
            });
            RenderUtil.roundedRectangle(textX, (double)((float)(Y - 10.0)), 130.0, (double)this.anmitY, 5.0, new Color(-416996573, true));
            RenderUtil.bg(textX, Y - 9.300000190734863, 130.0, (double)this.anmitY, 2.0, 5.0, new Color(165, 17, 17, 255));
            if (font) {
                for(int i = 0; i < players.size(); ++i) {
                    EntityPlayer player2 = (EntityPlayer)players.get(i);
                    this.renderPlayer(player2, i, (float)textX, (float)renderPlayerY);
                }

                FontManager.font16.drawStringWithShadow("Players", (float)(textX + 3.0), (float)(Y - 11.0) + 6.0F, -1);
            }
        }
    }

    private void renderPlayer(EntityPlayer player, int i, float x, float y) {
        float height = (float)FontManager.font18.getHeight();
        float offset = (float)i * height;
        int distance = (int)mc.thePlayer.getDistanceToEntity(player);

        String originalName = "[" + distance + "m][" + this.DF_1.format((double)(player.getHealth() + player.getAbsorptionAmount())) + "HP] " + player.getCommandSenderName();

        FontManager.font16.drawStringWithShadow(originalName, x + 3.0F, y + offset + FontManager.font18.getMiddleOfBox(height) + 1.0F, Color.WHITE.getRGB());
    }
}
