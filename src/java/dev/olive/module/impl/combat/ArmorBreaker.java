package dev.olive.module.impl.combat;


import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventPacket;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.utils.player.InventoryUtil;
import dev.olive.value.impl.ModeValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.util.Comparator;
import java.util.List;

/**
 * @author DSJ
 * @since 2025-02-25
 */
public class ArmorBreaker extends Module {
    private static final ModeValue mode = new ModeValue("Mode", new String[]{"Switch Sword", "Vanilla"},"Switch Sword");
    private EntityLivingBase target;
    private int currentItem = 0;

    public ArmorBreaker() {
        super("ArmorBreaker","破甲",Category.Combat );
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (target != null && !isGapple() && !Scaffold.canTellyPlace && mode.is("Switch Sword")) {
            Entity entity = KillAura.target;
            if (entity == target) {
                List<Slot> slots = mc.thePlayer.inventoryContainer.inventorySlots.stream()
                        .filter(slot -> slot.getHasStack() && slot.getStack().getItem() instanceof ItemSword)
                        .sorted(Comparator.comparingDouble(value -> InventoryUtil.getSwordStrength(((Slot) value).getStack())).reversed()).toList();

                if (currentItem >= slots.size())
                    currentItem = 0;

                mc.playerController.windowClick(0, slots.get(currentItem).slotNumber, mc.thePlayer.inventory.currentItem, 2, mc.thePlayer);
                mc.thePlayer.swingItem();

                currentItem++;
            } else {
                target = null;
            }
        }

    }

    @EventTarget
    public void onPacketSend(EventPacket event) {
        if (event.getEventType() == EventPacket.EventState.SEND) {
            if (!isGapple() && !Scaffold.canTellyPlace && event.getPacket() instanceof C02PacketUseEntity packetUseEntity && (packetUseEntity.getAction() == C02PacketUseEntity.Action.ATTACK) && mode.is("Switch Sword")) {
                if (packetUseEntity.getEntityFromWorld(mc.theWorld) instanceof EntityLivingBase) {
                    if (target == null)
                        currentItem = 0;
                    target = (EntityLivingBase) packetUseEntity.getEntityFromWorld(mc.theWorld);
                }
            }
        }
    }
}
