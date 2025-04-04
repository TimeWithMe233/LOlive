package dev.olive.module.impl.render;


import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.NumberValue;

public class MotionBlur extends Module {

    public final NumberValue blurAmount = new NumberValue("Amount", 7, 0.0, 10.0, 0.1);


    public MotionBlur() {
        super("MotionBlur", "动态模糊",Category.Render);
    }


}
