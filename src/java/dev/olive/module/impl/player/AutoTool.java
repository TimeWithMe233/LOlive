package dev.olive.module.impl.player;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventTick;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.value.impl.BoolValue;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

public class AutoTool extends Module {
    private final BoolValue spoof = new BoolValue("Watchdog", true);
    private int oldSlot;
    private boolean wasDigging;

    public AutoTool() {
        super("AutoTool", "自动工具",Category.Player);
    }

    @EventTarget
    private void onUpdate(EventUpdate event) {
        if (spoof.get()) {
            this.setSuffix("Watchdog");
        } else {
            this.setSuffix("Basic");
        }
    }

    @Override
    public void onDisable() {
        if (this.wasDigging) {
            AutoTool.mc.thePlayer.inventory.currentItem = this.oldSlot;
            this.wasDigging = false;
        }
        Client.instance.getSlotSpoofManager().stopSpoofing();
    }

    @EventTarget()
    public void onTick(EventTick event) {
        boolean inContainer = mc.currentScreen instanceof GuiContainer;
        if (inContainer) return;
        if ((Mouse.isButtonDown((int) 0) || AutoTool.mc.gameSettings.keyBindAttack.isKeyDown()) && AutoTool.mc.objectMouseOver != null && AutoTool.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = AutoTool.mc.theWorld.getBlockState(AutoTool.mc.objectMouseOver.getBlockPos()).getBlock();
            float strength = 0.0f;
            if (!this.wasDigging) {
                this.oldSlot = AutoTool.mc.thePlayer.inventory.currentItem;
                if (this.spoof.get()) {
                    Client.instance.getSlotSpoofManager().startSpoofing(this.oldSlot);
                }
            }
            for (int i = 0; i <= 8; ++i) {
                float slotStrength;
                ItemStack stack = AutoTool.mc.thePlayer.inventory.getStackInSlot(i);
                if (stack == null || !((slotStrength = stack.getStrVsBlock(block)) > strength)) continue;
                AutoTool.mc.thePlayer.inventory.currentItem = i;
                strength = slotStrength;
            }
            this.wasDigging = true;
        } else if (this.wasDigging) {
            AutoTool.mc.thePlayer.inventory.currentItem = this.oldSlot;
            Client.instance.getSlotSpoofManager().stopSpoofing();
            this.wasDigging = false;
        } else {
            this.oldSlot = AutoTool.mc.thePlayer.inventory.currentItem;
        }
    }
}