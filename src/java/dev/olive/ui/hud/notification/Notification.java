package dev.olive.ui.hud.notification;

import dev.olive.ui.font.FontManager;
import dev.olive.utils.TimerUtil;
import dev.olive.utils.render.ColorUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.utils.render.shader.ShaderElement;
import lombok.Getter;

import java.awt.*;

@Getter
public class Notification {
    private final NotificationType notificationType;
    private final String title, description;
    private final float time;
    private final TimerUtil timerUtil;
    private final Animation animation;
    public String icon;

    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, NotificationManager.getToggleTime());
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000);
        timerUtil = new TimerUtil();
        this.notificationType = type;
        animation = new DecelerateAnimation(300, 1);

        switch (type) {
            case DISABLE:
                this.icon = "B";
                break;
            case SUCCESS:
                this.icon = "A";
                break;
            case INFO:
                this.icon = "C";
                break;
            case WARNING:
                this.icon = "D";
                break;
        }

    }

    public void drawOlive(float x, float y, float width, float height) {
        ShaderElement.addBlurTask(() -> RoundedUtil.drawRound(x, y, x + width, 18, 4, Color.WHITE));
        RoundedUtil.drawRound(x, y, x + width, 18, 4, new Color(0, 0, 0, 110));
        Color textColor = ColorUtil.applyOpacity(Color.WHITE, 80);
        //Icon
        FontManager.icontestFont35.drawStringDynamic(getNotificationType().getIcon(), x + 3, (y + FontManager.icontestFont35.getMiddleOfBox(height) - 1.5f), 1, 6);
        FontManager.interSemiBold20.drawString(getDescription(), x + 2.8f + FontManager.icontestFont35.getStringWidth(getNotificationType().getIcon()) + 2f, y + 6f, textColor.getRGB());
    }
    public void drawNaven(float x, float y, float width, float height) {
        ShaderElement.addBlurTask(() -> {
            RoundedUtil.drawRound(x + 10, y, (float)(FontManager.font20.getStringWidth(this.getDescription()) + 12), 19.0F, 4.0F, Color.WHITE);
        });
        ShaderElement.addBloomTask(() -> {
            RoundedUtil.drawRound(x+ 10, y, (float)(FontManager.font20.getStringWidth(this.getDescription()) + 12), 19.0F, 4.0F, Color.BLACK);
        });
        Color color = ColorUtil.applyOpacity(ColorUtil.interpolateColorC(Color.BLACK, this.getNotificationType().getColor(), 0.65F), 70.0F);
        RoundedUtil.drawRound(x + 8, y, 10f, 19.0F, 4.0F, color);

        RoundedUtil.drawRound(x + 12, y, (float)(FontManager.font20.getStringWidth(this.getDescription()) + 12), 19.0F, 0.0F, new Color(35,35,35,255));
        FontManager.font18.drawString(this.getDescription(), x + 2.8F + 1.0F+ 11, y + 5.0F, Color.WHITE.getRGB());
    }


}