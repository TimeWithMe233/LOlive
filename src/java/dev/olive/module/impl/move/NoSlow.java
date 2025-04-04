package dev.olive.module.impl.move;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.Gapple;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.utils.HYTUtils;
import dev.olive.utils.player.MoveUtil;
import dev.olive.utils.player.MovementUtil;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import static dev.olive.utils.PacketUtil.sendPacket;
import static dev.olive.utils.PacketUtil.sendPacketNoEvent;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;


public class NoSlow extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Grim", "Watchdog"}, "Watchdog" );
    private final BoolValue food = new BoolValue("Food", true);
    private final BoolValue bow = new BoolValue("Bow", true);

    public boolean hasDroppedFood = false;
    public static boolean fix = false;

    public NoSlow() {
        super("NoSlow","无减速" ,Category.Player);
    }

    public static boolean hasSwordwithout() {
        return  Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }
    @EventTarget()
    public void onSlowDownEvent(EventSlowDown event) {
        if (this.mode.is("Grim")) {
            if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.getHeldItem() == null) return;
            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && food.getValue()) return;
            if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
                    || (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && bow.getValue())) && mc.thePlayer.isUsingItem())
                event.setCancelled(true);
            if (!mc.thePlayer.isSprinting() && !mc.thePlayer.isSneaking() && MoveUtil.isMoving()) {
                mc.thePlayer.setSprinting(true);
            }
        }
        if (this.mode.is("Watchdog")) {
            if (!(mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemPotion))
                event.setCancelled(true);
            if (!mc.thePlayer.isSprinting() && !mc.thePlayer.isSneaking() && MoveUtil.isMoving()) {
                mc.thePlayer.setSprinting(true);
            }
        }
    }
    @EventTarget()
    public void onMotionEvent(EventMotion e) {
        setSuffix(mode.get());
        switch (this.mode.get()) {
            case "Watchdog":
                if (e.isPre()) {
                    if (mc.thePlayer.getHeldItem() == null) {
                        return;
                    } else if (mc.thePlayer.isUsingItem()) {
                        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                            dev.olive.utils.PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), EnumFacing.UP.getIndex(), (ItemStack)null, 0.0F, 0.0F, 0.0F));

                        } else if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                            dev.olive.utils.PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            dev.olive.utils.PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

                        }
                    }
                }
                break;
            case "Grim":
                if  (!mc.isSingleplayer()) {
                    if (e.isPre()) {
                        if (mc.thePlayer.onGround && mc.thePlayer.isInWeb) {
                            MoveUtil.strafe(0.29);
                        }
                        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.getHeldItem() == null) return;
                        ItemStack itemInHand = mc.thePlayer.getCurrentEquippedItem();
                        ItemStack itemStack = mc.thePlayer.getHeldItem();
                        int itemID = Item.getIdFromItem(itemInHand.getItem());
                        int itemMeta = itemInHand.getMetadata();
                        String itemId = itemInHand.getItem().getUnlocalizedName();
                        if(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && food.getValue()) {
                            if (mc.thePlayer.getHeldItem() != null && (!((itemID == 322 && itemMeta == 1) || itemId.equals("item.appleGoldEnchanted")))) {
                                if (Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem() != null) {

                                    if (Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock) {
                                        Minecraft.getMinecraft().rightClickDelayTimer = 4;
                                    } else {
                                        Minecraft.getMinecraft().rightClickDelayTimer = 4;
                                    }
                                }
                                if (mc.thePlayer.isUsingItem() && !hasDroppedFood  && itemStack.stackSize > 1) {
                                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, new BlockPos(0, 0, 0), EnumFacing.DOWN));
                                    hasDroppedFood = true;
                                    fix =true;
                                } else if (!mc.thePlayer.isUsingItem()) {
                                    hasDroppedFood = false;
                                    new Thread(() -> {try {Thread.sleep(500); fix =false;} catch (InterruptedException ex) {ex.printStackTrace();}}).start();

                                }
                            }
                        }else {
                            fix =false;
                        }
                        if (Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem() != null) {
                            if ((mc.thePlayer.isBlocking()) ||mc.thePlayer.isUsingItem() && hasSwordwithout()) {
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9));
                                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MadeByFire", new PacketBuffer(Unpooled.buffer())));
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            }
                            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem() && bow.getValue() && !mc.thePlayer.isSneaking()) {
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9));
                                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MadeByFire", new PacketBuffer(Unpooled.buffer())));
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            }
                        }
                    }
                    if (e.isPost()) {
                        if (mc.thePlayer.getHeldItem() == null) return;
                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()) {
                            dev.olive.utils.PacketUtil.sendC0F();
                            PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                            PacketWrapper useItem2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem2.write(Type.VAR_INT, 0);
                            PacketUtil.sendToServer(useItem2, Protocol1_8To1_9.class, true, true);
                        }
                        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem() && bow.getValue()) {
                            dev.olive.utils.PacketUtil.sendC0F();
                            PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                            PacketWrapper useItem2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem2.write(Type.VAR_INT, 0);
                            PacketUtil.sendToServer(useItem2, Protocol1_8To1_9.class, true, true);
                        }
                    }
                }
                break;
        }
    }
}
