package dev.olive.module.impl.world;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventPacket;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public class Ambience extends Module {

    private final ModeValue mode = new ModeValue("Time-Mode", new String[]{"Static", "Cycle"}, "Static");
    private final ModeValue weathermode = new ModeValue("Weather-Mode", new String[]{"Clear", "Rain"}, "Clear");
    private final NumberValue cycleSpeed = new NumberValue("Cycle-Speed", 24.0, 1.0, 24.0, 1.0);
    private final BoolValue reverseCycle = new BoolValue("Reverse-Cycle", false);
    private final NumberValue time = new NumberValue("Static-Time", 24000.0, 0.0, 24000.0, 100.0);
    private final NumberValue rainstrength = new NumberValue("Rain-Strength", 0.1, 0.1, 0.5, 0.05);

    private final BoolValue displayTag = new BoolValue("Display-Tag", false);

    private int timeCycle = 0;

    public Ambience() {
        super("Ambience","时间", Category.World);
    }

    public void onEnable() {
        timeCycle = 0; //reset
    }

    @EventTarget
    public void onUpdate(final EventUpdate event) {

        if (mode.get().equalsIgnoreCase("static")) {
            mc.theWorld.setWorldTime(time.get().intValue());
        } else {
            mc.theWorld.setWorldTime(timeCycle);
            timeCycle += (reverseCycle.get() ? -cycleSpeed.get() : cycleSpeed.get()) * 10;

            if (timeCycle > 24000) {
                timeCycle = 0;
            } else if (timeCycle < 0) {
                timeCycle = 24000;
            }
        }
        if (weathermode.get().equalsIgnoreCase("clear")) {
            mc.theWorld.setRainStrength(0F);
        } else {
            mc.theWorld.setRainStrength(rainstrength.get().longValue());
        }
    }

    @EventTarget
    public void onPacket(final EventPacket event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S03PacketTimeUpdate) {
            event.setCancelled(true);
        }
    }

}
