package dev.olive.ui.sidegui.panels.infopanel;


import dev.olive.ui.Screen;
import dev.olive.utils.math.MathUtils;
import dev.olive.utils.objects.Scroll;
import dev.olive.utils.render.ColorUtil;
import dev.olive.utils.render.HoveringUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.StencilUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InfoRect implements Screen {

    public float x, y, width, height, alpha;


    public final List<InfoButton> faqButtons;

    public InfoRect() {
        faqButtons = new ArrayList<>();
    }


    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    private dev.olive.utils.objects.Scroll infoScroll = new Scroll();

    @Override
    public void drawScreen(int mouseX, int mouseY) {

        RoundedUtil.drawRound(x, y, width, height, 5, ColorUtil.tripleColor(27, alpha));


        if (HoveringUtil.isHovering(x, y, width, height, mouseX, mouseY)) {
            infoScroll.onScroll(35);
        }

        StencilUtil.initStencilToWrite();
        RoundedUtil.drawRound(x, y, width, height, 5, Color.WHITE);
        StencilUtil.bindReadStencilBuffer(1);

        float count = 0;
        for (InfoButton button : faqButtons) {
            button.setX(x + 5);
            button.setWidth(width - 10);
            button.setHeight(20);
            button.setY((float) (y + 5 + (count * button.getHeight()) + MathUtils.roundToHalf(infoScroll.getScroll())));
            button.setAlpha(alpha);

            button.drawScreen(mouseX, mouseY);
            count += button.getCount() + .25f;
        }

        float hiddenHeight = (count * 20) - (height - 5);

        infoScroll.setMaxScroll(Math.max(0, hiddenHeight));


        StencilUtil.uninitStencilBuffer();

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        faqButtons.forEach(faqButton -> faqButton.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}
