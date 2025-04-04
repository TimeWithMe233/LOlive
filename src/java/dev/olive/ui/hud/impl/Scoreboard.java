package dev.olive.ui.hud.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.olive.ui.font.FontManager;
import dev.olive.ui.hud.HUD;
import dev.olive.utils.render.ColorUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.BoolValue;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * @author AquaVase
 * @since 3/17/2024 - 1:38 PM
 */
public class Scoreboard extends HUD {
    private final BoolValue leftLayout = new BoolValue("Left Layout", true);
    private final BoolValue redNumbers = new BoolValue("Red Numbers", false);

    public Scoreboard() {
        super(90, 160, "Scoreboard","积分板");
    }

    @Override
    public void drawShader() {

    }

    @Override
    public void drawHUD(int posX, int posY, float partialTicks) {
        FontRenderer font = FontManager.font16;

        ScoreObjective scoreObjective = getScoreObjective();

        if (scoreObjective == null) {
            return;
        }

        net.minecraft.scoreboard.Scoreboard scoreboard = scoreObjective.getScoreboard();
        Collection<Score> sortedScores = scoreboard.getSortedScores(scoreObjective);
        List<Score> list = Lists.newArrayList(Iterables.filter(sortedScores, score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")));

        if (list.size() > 15) {
            sortedScores = Lists.newArrayList(Iterables.skip(list, sortedScores.size() - 15));
        } else {
            sortedScores = list;
        }

        int maxWidth = mc.fontRendererObj.getStringWidth(scoreObjective.getDisplayName()) + 8;

        boolean domain = false;
        for (Score score : sortedScores) {
            ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
            String playerName = ScorePlayerTeam.formatPlayerName(playerTeam, score.getPlayerName()) + ":" + (redNumbers.get() ? " " + EnumChatFormatting.RED + score.getScorePoints() : "");

            if (!domain) {
                playerName = "Olive";
                domain = true;
            }

            maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(playerName));
        }

        if (!redNumbers.get()) {
            maxWidth += 4;
        }

        int lineHeight = font.getHeight();
        int totalHeight = (sortedScores.size()) * lineHeight;

        if (!leftLayout.get()) {
            posX -= maxWidth;
        }

        int xEnd = posX + maxWidth;
        int yEnd = posY + totalHeight;

        int x = posX;
        int index = -1;

        int finalMaxWidth = maxWidth;

        ShaderElement.addBlurTask(() -> {
            RenderUtil.roundedRectangle((double)x, (double)posY, (double)finalMaxWidth, (double)(totalHeight + lineHeight + 5), 5.0, Color.WHITE);
        });
        ShaderElement.addBloomTask(() -> {
            RenderUtil.roundedRectangle((double)x, (double)posY, (double)finalMaxWidth, (double)(totalHeight + lineHeight + 5), 5.0, Color.BLACK);
        });
        RenderUtil.roundedRectangle((double)x, (double)posY, (double)finalMaxWidth, (double)(totalHeight + lineHeight + 5), 5.0, new Color(-416996573, true));
        RenderUtil.bg((double)x, (double)((float)posY + 0.7F), (double)finalMaxWidth, (double)(totalHeight + lineHeight + 5), 2.0, 5.0, new Color(165, 17, 17, 255));


        this.setWidth(leftLayout.get() ? maxWidth : -maxWidth);
        this.setHeight(totalHeight);

        for (Score score : sortedScores) {
            ++index;
            ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
            String playerName = ScorePlayerTeam.formatPlayerName(playerTeam, score.getPlayerName());

            int y = yEnd - index * lineHeight;


            {
                font.drawString(playerName, x + 2, y + 2, ColorUtil.applyOpacity(553648127, 1F));
            }

            // Red numbers
            if (redNumbers.get()) {
                String scorePoint = EnumChatFormatting.RED + "" + score.getScorePoints();
                font.drawString(scorePoint, xEnd - font.getStringWidth(scorePoint), y, ColorUtil.applyOpacity(553648127, 1F));
            }

            if (index == sortedScores.size() - 1) {
                String replaced = StringUtils.replace(StringUtils.replace(scoreObjective.getDisplayName(), "花雨庭", "花雨庭"), "✿", "⌨");
                font.drawString(replaced, x + maxWidth / 2 - font.getStringWidth(replaced) / 2, y - lineHeight + 2, ColorUtil.applyOpacity(553648127, 1F));

            }
        }
    }

    private static ScoreObjective getScoreObjective() {
        net.minecraft.scoreboard.Scoreboard worldScoreboard = mc.theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = worldScoreboard.getPlayersTeam(mc.thePlayer.getName());

        if (scoreplayerteam != null) {
            int colorIndex = scoreplayerteam.getChatFormat().getColorIndex();

            if (colorIndex >= 0) {
                scoreobjective = worldScoreboard.getObjectiveInDisplaySlot(3 + colorIndex);
            }
        }

        return scoreobjective != null ? scoreobjective : worldScoreboard.getObjectiveInDisplaySlot(1);
    }

    @Override
    public void predrawhud() {

    }

    @Override
    public void onTick() {

    }
}
