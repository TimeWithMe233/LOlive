package dev.olive.module.impl.render;

import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.ui.gui.clickgui.NewClickgui.NewClickGui;
import org.lwjglx.input.Keyboard;

public class ClickGUI extends Module {

    public ClickGUI() {
        super("ClickGUI","点击面板", Category.Render);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {

        mc.displayGuiScreen(NewClickGui.INSTANCE);
        this.toggle();

    }
}
