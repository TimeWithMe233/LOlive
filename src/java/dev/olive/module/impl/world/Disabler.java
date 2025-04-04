package dev.olive.module.impl.world;

import dev.olive.Client;
import dev.olive.event.annotations.EventPriority;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.Gapple;
import dev.olive.module.impl.misc.AutoPlay;
import dev.olive.module.impl.move.NoSlow;
import dev.olive.module.impl.move.Speed;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.ui.hud.notification.NotificationType;
import dev.olive.utils.*;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.util.EnumChatFormatting;

public class Disabler
        extends Module {
    private ModeValue mode = new ModeValue("Mode", new String[]{"GrimPost","Watchdog"}, "Watchdog");
    private BoolValue c0fFix = new BoolValue("C0FFix", false,()->mode.is("Grim"));
    private BoolValue lowhop = new BoolValue("Lowhop", false,()->mode.is("Watchdog"));
    public Disabler() {
        super("Disabler","禁用器",Category.World);
        INSTANCE = this;
    }

    @Override
    public String getSuffix() {
        return mode.getValue();
    }
    private int testTicks;
    private WorldClient lastWorld = null;
    private int lastSlot;
    boolean disabled;
    static Disabler INSTANCE;
    private boolean stuck = false;
    private boolean jump = false;
    private long lastLoadWorldTime = 0L;
    private boolean lastTickSentC0F = false;
    private boolean spoofed = false;
    private boolean lastGround = true;




    private double x, y, z, motionX, motionY, motionZ;
    private boolean flag1 = false;

    public static ArrayList<C0FPacketConfirmTransaction> c0fStorage = new ArrayList<>();

    @Override
    public void onEnable() {
        jump = false;
        testTicks = 0;
    }
    @EventTarget
    private void worldEventHandler(WorldEvent event) {
        c0fStorage.clear();
        lastLoadWorldTime = System.currentTimeMillis();
        stuck = false;
        spoofed = false;
        jump = true;
        testTicks = 0;
    };

    public static boolean getGrimPost() {
        return mc.thePlayer != null && mc.theWorld != null && Client.instance.moduleManager.getModule(Disabler.class).mode.is("GrimPost") && Client.instance.moduleManager.getModule(Disabler.class).getState() && mc.thePlayer.ticksExisted > 30;
    }

    public static boolean shouldProcess() {
        return true;
    }
    @EventTarget
    private void updateEventHandler(EventUpdate event) {

        lastTickSentC0F = false;
    };

    @EventTarget
    private void packetEventHandler(EventPacket event) {
        Packet<?> packet = event.getPacket();
        if (event.getEventType() == EventPacket.EventState.SEND) {
            if (packet instanceof C09PacketHeldItemChange) {
                if (lastSlot == ((C09PacketHeldItemChange) packet).getSlotId()) {
                    event.setCancelled(true);
                }
                lastSlot = ((C09PacketHeldItemChange) packet).getSlotId();
            }
            if (c0fFix.getValue() && System.currentTimeMillis() - lastLoadWorldTime >= 2000) {
                if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
                    if (!lastTickSentC0F) {
                        if (!c0fStorage.isEmpty()) {
                            c0fStorage.add((C0FPacketConfirmTransaction) event.getPacket());
                            event.setCancelled(true);
                            mc.getNetHandler().addToSendQueue(c0fStorage.get(0), true);
                            c0fStorage.remove(0);
                            lastTickSentC0F = true;
                        }
                    } else {
                        c0fStorage.add((C0FPacketConfirmTransaction) event.getPacket());
                        event.setCancelled(true);
                        DebugUtil.log("multi c0f in 1 client tick, blink.");
                    }
                }
            }
            if(mode.is("Watchdog")){
                if (event.getPacket() instanceof C03PacketPlayer && (!lowhop.getValue())) {
                    final C03PacketPlayer wrapper = ((C03PacketPlayer) event.getPacket());

                    if (!wrapper.isMoving() && !wrapper.rotating && wrapper.isOnGround() && lastGround) {
                        event.setCancelled(true);
                    }
                    lastGround = wrapper.isOnGround();
                }
            }
        } else {
            if (packet instanceof S09PacketHeldItemChange) {
                lastSlot = ((S09PacketHeldItemChange) packet).getHeldItemHotbarIndex();
            }
            if(mode.is("Watchdog") && lowhop.getValue() && disabled){
                if(mc.thePlayer.ticksExisted <= 200) {
                    if(mc.thePlayer.ticksExisted == 4) {
                        mc.thePlayer.motionY = mc.thePlayer.motionZ = mc.thePlayer.motionX = 0;
                    }
                }
                if(event.getPacket() instanceof S08PacketPlayerPosLook) {
                    testTicks++;
                    if(testTicks == 20) {

                        mc.thePlayer.jump();
                        disabled = false;
                        testTicks = 0;
                      NotificationManager.post(NotificationType.SUCCESS,"Disabler","Disabled Watchdog Motion Checks Successfully", 5);
                    }
                    mc.thePlayer.motionY = mc.thePlayer.motionZ = mc.thePlayer.motionX = 0;
                }
            }
        }
    };

    @EventTarget
    private void motionEventHandler(EventMotion event) {
        if (lowhop.getValue() && mode.is("Watchdog")) {
            if(mc.thePlayer.onGround && jump) {
                mc.thePlayer.jump();
                mc.thePlayer.jumpTicks = 0;
            } else if (jump) {
              NotificationManager.post(NotificationType.WARNING,"Disabler", "Disabler is working,do not move", 5);
                jump = false;
                disabled = true;
            } else if(disabled && mc.thePlayer.offGroundTicks >= 10) {
                if ((!ServerUtils.isHypixel() || AutoPlay.getCurrentServer() == Servers.NONE) && jump) {
                    NotificationManager.post(NotificationType.INFO,"Disabler", "Skip disabler.");
                    jump = false;
                    disabled = false;
                    testTicks = 0;
                    return;
                }
                if(mc.thePlayer.offGroundTicks % 2 == 0) {
                    event.setX(event.getX() + 0.095);
                    PlayerUtil.stop();
                }
                if(Client.instance.moduleManager.getModule(Speed.class).getState()) {
                    NotificationManager.post( NotificationType.INFO,"Disabler","Disabled module Speed due to disabler is working", 5);
                    Client.instance.moduleManager.getModule(Speed.class).setState(false);
                }
                mc.thePlayer.motionY = 0;
            }
        }
    };
}

