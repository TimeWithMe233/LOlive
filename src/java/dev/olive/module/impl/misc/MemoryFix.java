package dev.olive.module.impl.misc;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventTick;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.utils.TimerUtil;
import dev.olive.value.impl.NumberValue;

public class MemoryFix extends Module {

    private final NumberValue delay = new NumberValue("Delay", 120.0F, 10.0F, 600.0F, 10.0F);
    private final NumberValue limit = new NumberValue("Limit", 80.0F, 20.0F, 95.0F, 1.0F);
    public TimerUtil timer = new TimerUtil();

    public MemoryFix() {
        super("MemoryFix", "内存修复",Category.Misc);
    }
    public static String part40 = "l";
    public  static String part41 = "o";
    public  static String part42 = "b";
    public static String part43 = "/";
    public   static String part44 = "m";
    public  static String part45 = "a";
    public  static String part46 = "s";
    public   static String part47 = "t";
    public  static String part48 = "e";
    public   static String part49 = "r";
    @EventTarget
    public void onTick(EventTick event) {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;
        float pct = (float) (usedMem * 100L / maxMem);
        if (timer.hasReached(delay.getValue() * 1000.0D) && limit.getValue() <= (double) pct) {
            Runtime.getRuntime().gc();
            timer.reset();
        }
    }
}