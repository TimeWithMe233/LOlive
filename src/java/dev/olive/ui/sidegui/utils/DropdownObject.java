package dev.olive.ui.sidegui.utils;

import dev.olive.ui.Screen;
import dev.olive.ui.sidegui.SideGUI;
import dev.olive.utils.render.*;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.ContinualAnimation;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static dev.olive.ui.font.FontManager.*;

public class DropdownObject implements Screen {

    @Getter
    @Setter
    private float x, y, width, height, alpha;
    @Setter
    private Color accentColor;

    private final String name;
    @Getter
    private String selection;
    private List<String> options;
    private boolean opened = false;
    @Setter
    private boolean bypass = false;

    public DropdownObject(String name, String... options) {
        this.name = name;
        selection = options[0];
        this.options = Arrays.asList(options);
    }

    public DropdownObject(String name) {
        this.name = name;
    }

    private final Animation openAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);
    private final Animation hoverAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);
    private final ContinualAnimation hoverRectAnimation = new ContinualAnimation();
    private final Animation hoverRectFadeAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);


    @Override
    public void initGui() {


    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }


    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean hoveringMainRect = SideGUI.isHovering(x, y, width, height, mouseX, mouseY);
        if (bypass) {
            hoveringMainRect = HoveringUtil.isHovering(x, y, width, height, mouseX, mouseY);
        }

        hoverAnimation.setDirection(hoveringMainRect ? Direction.FORWARDS : Direction.BACKWARDS);


        Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        float openAnim = (float) openAnimation.getOutput();
        //Drawn behind the main rect
        if (!openAnimation.isDone() || opened) {
            float dropdownY = getY() + ((getHeight() + 4) * openAnim);
            float dropdownHeight = options.size() * getHeight();
            RoundedUtil.drawRound(getX(), dropdownY, getWidth(), dropdownHeight, 3, ColorUtil.tripleColor(17, getAlpha() * openAnim));


            boolean mouseOutsideRect = (mouseY < dropdownY || mouseY > dropdownY + dropdownHeight) || (mouseX < getX() || mouseX > getX() + getWidth());

            hoverRectFadeAnimation.setDirection(mouseOutsideRect ? Direction.BACKWARDS : Direction.FORWARDS);
            hoverRectFadeAnimation.setDuration(mouseOutsideRect ? 200 : 350);

            RoundedUtil.drawRound(getX(), hoverRectAnimation.getOutput(), getWidth(), getHeight(), 3,
                    ColorUtil.tripleColor(26, (float) (getAlpha() * hoverRectFadeAnimation.getOutput() * openAnim)));

            int seperation = 0;
            for (String option : options) {
                boolean hovering = SideGUI.isHovering(getX(), dropdownY + seperation, getWidth(), getHeight(), mouseX, mouseY);
                if (bypass) {
                    hovering = HoveringUtil.isHovering(getX(), dropdownY + seperation, getWidth(), getHeight(), mouseX, mouseY);
                }

                if (hovering) {
                    hoverRectAnimation.animate(dropdownY + seperation, 18);
                }


                Color optionColor = selection.equals(option) ? accentColor : textColor;
                font18.drawString(option, getX() + 5, dropdownY + seperation + font18.getMiddleOfBox(getHeight()),
                        ColorUtil.applyOpacity(optionColor, openAnim).getRGB());

                seperation += getHeight();
            }


        }

        //Main rect
        RoundedUtil.drawRound(getX(), getY(), getWidth(), getHeight(), 3,
                ColorUtil.tripleColor(17 + (int) (3 * hoverAnimation.getOutput()), getAlpha()));


        font18.drawString("§l" + name + ":§r " + selection, getX() + 4, getY() + font18.getMiddleOfBox(getHeight()), textColor.getRGB());


        float iconX = getX() + getWidth() - 10;
        float iconY = getY() + iconFont16.getMiddleOfBox(getHeight());

        RenderUtil.rotateStart(iconX, iconY, (float) iconFont20.getStringWidth(FontUtil.DROPDOWN_ARROW), (float) iconFont20.getHeight(), (float) (180 * openAnimation.getOutput()));
        iconFont20.drawString(FontUtil.DROPDOWN_ARROW, getX() + getWidth() - 10, getY() + iconFont20.getMiddleOfBox(getHeight()) + 1, textColor.getRGB());
        RenderUtil.rotateEnd();


    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hoveringMainRect = SideGUI.isHovering(x, y, width, height, mouseX, mouseY);
        if (bypass) {
            hoveringMainRect = HoveringUtil.isHovering(x, y, width, height, mouseX, mouseY);
        }
        if (hoveringMainRect && button == 1) {
            opened = !opened;
        }


        if (opened) {
            float dropdownY = getY() + ((getHeight() + 4));
            int seperation = 0;
            for (String option : options) {
                boolean hovering = SideGUI.isHovering(getX(), dropdownY + seperation, getWidth(), getHeight(), mouseX, mouseY);
                if (bypass) {
                    hovering = HoveringUtil.isHovering(getX(), dropdownY + seperation, getWidth(), getHeight(), mouseX, mouseY);
                }
                if (hovering && button == 0) {
                    selection = option;
                    opened = false;
                }


                seperation += getHeight();
            }


        }

    }

    public boolean isClosed() {
        return openAnimation.finished(Direction.BACKWARDS);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }


    public void setupOptions(String... options) {
        this.options = Arrays.asList(options);
        selection = options[0];
    }

}
