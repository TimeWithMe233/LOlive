package dev.olive.module.impl.player;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.event.impl.events.EventTick;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.utils.HYTUtils;
import dev.olive.utils.TimerUtil;
import dev.olive.utils.math.TimerUtils;
import dev.olive.utils.player.ItemComponent;
import dev.olive.utils.player.ItemUtils;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;

import java.util.Iterator;
import java.util.Random;

public class ChestStealer extends Module {
    private final BoolValue postValue = new BoolValue("Post", false);

    private final BoolValue chest = new BoolValue("Chest", true);
    private final BoolValue furnace = new BoolValue("Furnace", true);
    private final BoolValue brewingStand = new BoolValue("BrewingStand", true);

    public static final TimerUtil timer = new TimerUtil();
    public static boolean isChest = false;
    public static TimerUtil openChestTimer = new TimerUtil();
    private final NumberValue delay = new NumberValue("StealDelay", 100, 0, 100, 1);
    private final BoolValue trash = new BoolValue("PickTrash", true);
    public final BoolValue silentValue = new BoolValue("Silent", true);

    private int nextDelay = 0;

    public ChestStealer() {
        super("ChestStealer", "自动拿箱子物品",Category.Player);
    }


    @EventTarget
    public void onMotion(EventMotion event) {

        if ((postValue.getValue() && event.isPost()) || (!postValue.getValue() && event.isPre())) {
            if (mc.thePlayer.openContainer == null)
                return;

            if (mc.thePlayer.openContainer instanceof ContainerFurnace && furnace.getValue()) {
                ContainerFurnace container = (ContainerFurnace) mc.thePlayer.openContainer;

                if (isFurnaceEmpty(container) && openChestTimer.delay(100) && timer.delay(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.tileFurnace.getSizeInventory(); ++i) {
                    if (container.tileFurnace.getStackInSlot(i) != null) {
                        if (timer.delay(nextDelay)) {

//                            for (int j = 0; j < 21; ++j) {
                            mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
//                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerBrewingStand && brewingStand.getValue()) {
                ContainerBrewingStand container = (ContainerBrewingStand) mc.thePlayer.openContainer;

                if (isBrewingStandEmpty(container) && openChestTimer.delay(100) && timer.delay(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.tileBrewingStand.getSizeInventory(); ++i) {
                    if (container.tileBrewingStand.getStackInSlot(i) != null) {
                        if (timer.delay(nextDelay)) {
//                            for (int j = 0; j < 21; ++j) {
                            mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
//                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerChest && chest.getValue() && isChest) {
                ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;


                if (isChestEmpty(container) && openChestTimer.delay(100) && timer.delay(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                    if (container.getLowerChestInventory().getStackInSlot(i) != null) {
                        if (timer.delay(nextDelay) && (isItemUseful(container, i) || trash.getValue())) {
//                            for (int j = 0; j < 21; ++j) {
                            mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
//                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }
        }
    }

    private boolean isChestEmpty(ContainerChest c) {
        for (int i = 0; i < c.getLowerChestInventory().getSizeInventory(); ++i) {
            if (c.getLowerChestInventory().getStackInSlot(i) != null) {
                if (isItemUseful(c, i) || trash.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFurnaceEmpty(ContainerFurnace c) {
        for (int i = 0; i < c.tileFurnace.getSizeInventory(); ++i) {
            if (c.tileFurnace.getStackInSlot(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isBrewingStandEmpty(ContainerBrewingStand c) {
        for (int i = 0; i < c.tileBrewingStand.getSizeInventory(); ++i) {
            if (c.tileBrewingStand.getStackInSlot(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isItemUseful(ContainerChest c, int i) {
        ItemStack itemStack = c.getLowerChestInventory().getStackInSlot(i);
        Item item = itemStack.getItem();

        if (item instanceof ItemAxe || item instanceof ItemPickaxe) {
            return true;
        }

        if (item instanceof ItemFood)
            return true;
        if (item instanceof ItemBow || item == Items.arrow)
            return true;

        if (item instanceof ItemPotion && !ItemUtils.isPotionNegative(itemStack))
            return true;
        if (item instanceof ItemSword && ItemUtils.isBestSword(c, itemStack))
            return true;
        if (item instanceof ItemArmor && ItemUtils.isBestArmor(c, itemStack))
            return true;
        if (item instanceof ItemBlock)
            return true;

        return item instanceof ItemEnderPearl;
    }
}
