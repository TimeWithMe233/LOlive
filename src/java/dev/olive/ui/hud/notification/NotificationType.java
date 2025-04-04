package dev.olive.ui.hud.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.optifine.util.FontUtils;

import java.awt.*;

@Getter
@AllArgsConstructor
public enum NotificationType {
    SUCCESS(new Color(25, 220, 84, 255), FontUtils.CHECKMARK),
    DISABLE(new Color(188, 49, 49, 255), FontUtils.XMARK),
    INFO(Color.DARK_GRAY, FontUtils.INFO),
    WARNING(Color.YELLOW, FontUtils.WARNING);
    private final Color color;
    private final String icon;
}