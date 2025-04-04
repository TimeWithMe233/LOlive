package dev.olive.ui.sidegui.panels;


import dev.olive.module.impl.render.HUD;
import dev.olive.ui.Screen;
import dev.olive.utils.render.ColorUtil;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public abstract class Panel implements Screen {
    private float x, y, width, height, alpha;

    public Color getTextColor() {
        return ColorUtil.applyOpacity(Color.WHITE, alpha);
    }

    public Color getAccentColor() {
        return ColorUtil.applyOpacity(HUD.color(1), alpha);
    }

}
