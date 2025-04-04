package dev.olive.module.impl.render;

import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;

public class BlockAnimation extends Module {
    public ModeValue type = new ModeValue("Mode", new String[]{"Minecraft", "SlideDown2", "Swank", "Swing", "Swang", "Swong", "Swaing", "Punch", "Stella", "Styles", "Slide", "Interia", "Olive", "1.7", "Sigma", "Exhibition", "Smooth", "Spinning"}, "1.7");

    public BlockAnimation() {
        super("BlockAnimation","防砍动画", Category.Render);
    }

    public static final NumberValue SpeedSwing = new NumberValue("Swing-Speed", 4, 0, 20, 1);
    public final BoolValue prog = new BoolValue("Equip Prog", true);
    public final NumberValue progm = new NumberValue("E-Prog Multiplier", 2.0, 0.5, 3.0, 0.1);
    public static final NumberValue itemPosX = new NumberValue("ItemX", 0.0, -1.0, 1.0, 0.01);
    public static final NumberValue itemPosY = new NumberValue("ItemY", 0.0, -1.0, 1.0, 0.01);
    public static final NumberValue itemPosZ = new NumberValue("ItemZ", 0.0, -1.0, 1.0, 0.01);
    public static final NumberValue itemDistance = new NumberValue("ItemDistance", 1.0, 1.0, 5.0, 0.01);
    // change Position Blocking Sword
    public static final NumberValue blockPosX = new NumberValue("BlockingX", 0.0, -1.0, 1.0, 0.01);
    public static final NumberValue blockPosY = new NumberValue("BlockingY", 0.0, -1.0, 1.0, 0.01);
    public static final NumberValue blockPosZ = new NumberValue("BlockingZ", 0.0, -1.0, 1.0, 0.01);
}
