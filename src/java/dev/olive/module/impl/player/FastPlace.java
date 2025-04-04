package dev.olive.module.impl.player;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventTick;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.NumberValue;
import net.minecraft.item.ItemBlock;

public class FastPlace extends Module {
    private final NumberValue ticks = new NumberValue("Ticks", 0, 0, 4, 1);

    public FastPlace() {
        super("FastPlace","快速放置", Category.Player);
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
            // 取消防止方块延迟
            mc.rightClickDelayTimer = Math.min(0, ticks.getValue().intValue());
        }
    }
}
