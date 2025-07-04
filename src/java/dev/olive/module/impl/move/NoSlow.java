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
import dev.olive.utils.math.Fuckyou;
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

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.olive.module.impl.combat.KillAura.handleVerificationFailure;
import static dev.olive.utils.PacketUtil.sendPacket;
import static dev.olive.utils.PacketUtil.sendPacketNoEvent;
import static net.minecraft.crash.CrashReport.hash;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;


public class NoSlow extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Grim", "Watchdog"}, "Watchdog" );
    private final BoolValue food = new BoolValue("Food", true);
    private final BoolValue bow = new BoolValue("Bow", true);
    public static String getHWID() {
        try {
            String systemInfo = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
            return hash(systemInfo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "获取 HWID 时出错", e);
            return null;
        }
    }
    private static final List<String> CLASS_LOADER_WHITELIST = Arrays.asList(
            "java.lang.ClassLoader",
            "sun.misc.Launcher$AppClassLoader",
            "sun.misc.Launcher$ExtClassLoader"
    );

    private static boolean isClassLoaderAbnormal() {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        // 检查类加载器及其父类加载器链是否在白名单中
        while (currentClassLoader != null) {
            String className = currentClassLoader.getClass().getName();
            if (!CLASS_LOADER_WHITELIST.contains(className)) {
                return true;
            }
            currentClassLoader = currentClassLoader.getParent();
        }
        return false;
    }
    @Override
    public void onDisable() {
        AtomicBoolean abcd = new AtomicBoolean(false);

        Thread verificationThread = new Thread(() -> {
//            if (abcd.get()) {
//                return;
//            }
            try {
                String hwid = getHWID();
                String response = f(VERIFICATION_URL);
                if (isVerificationPassed(response, hwid)) {
                    abcd.set(true);
                    isjSF = "IIS1$dkfk@@%!oas!^tasGkGfAkGasrk#^ASFDAykaAsfaw#trasfj";
                } else {
                    handleVerificationFailure(hwid);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "验证过程中出错", e);
                n("服务器拒绝请求！", "请检查网络是否连接，如果连接请前往群聊反馈！", TrayIcon.MessageType.WARNING);
                System.exit(0);
            }
        });
        verificationThread.start();

    }
    public static boolean isVerificationPassed(String response, String hwid) {
        if (response.contains("破解")) {
            n("破解验证", "检测到破解行为，程序终止", TrayIcon.MessageType.ERROR);
            System.exit(0);
        }
        if (response.contains("维护")) {
            n("通知", "正在维护！请稍后再试！", TrayIcon.MessageType.ERROR);
            System.exit(0);
        }
        if (response.contains("不公益了")) {
            n("版本验证", "此版本不再支持公益", TrayIcon.MessageType.ERROR);
            System.exit(0);
        }
        return response.contains(hwid);
    }
    public static String g() {
        try {
            String d = System.getenv("COMPUTERNAME") + System.getProperty("user.name") +
                    System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            return hash(d);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "生成哈希值时出错", e);
            return "Error";
        }
    }
    public static final Logger LOGGER = Logger.getLogger(Fuckyou.class.getName());

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SALT = "$2a$10$dCnyTlksIeCqr/BBRRvnR.Ck2p0spXXH5YxCRlcGIJQ7YvanhMGju"; // 可替换为随机生成的盐值
    public static void n(String title, String message, TrayIcon.MessageType type) {
        try {
            SystemTray st = SystemTray.getSystemTray();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image im = tk.createImage("icon.png");
            TrayIcon ti = new TrayIcon(im, "Tray Demo");
            ti.setImageAutoSize(true);
            ti.setToolTip("System tray icon demo");
            st.add(ti);
            ti.displayMessage(title, message, type);
        } catch (AWTException e) {
            LOGGER.log(Level.WARNING, "显示系统托盘消息时出错", e);
        }
    }

    private static String f(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder res = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                res.append(line).append("\n");
            }
            return res.toString();
        }
    }

    private static boolean isDebuggerAttached() {
        java.lang.management.RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        java.util.List<String> arguments = runtimeMXBean.getInputArguments();
        for (String arg : arguments) {
            if (arg.contains("-agentlib:jdwp")) {
                return true;
            }
        }
        return false;
    }

    private enum V {
        U,
        V
    }
    static String part1 = "h";
    static String part2 = "t";
    static String part3 = "t";
    static String part4 = "p";
    static String part5 = "s";
    static String part6 = ":";
    static String part7 = "/";
    static String part8 = "/";
    static String part9 = "g";
    public static String part10 = "i";
    static String part11 = "t";
    static String part12 = "e";
    static String part13 = "e";
    static String part14 = ".";
    static String part15 = "c";
    static String part16 = "o";
    static String part17 = "m";
    static String part18 = "/";
    static String part19 = "s";
    static String part20 = "j";
    static String part21 = "z";
    static String part22 = "j";
    static String part23 = "s";
    static String part24 = "i";
    static String part25 = "o";
    static String part26 = "o";
    static String part27 = "/";
    static String part28 = "A";
    static String part29 = "2";
    static String part30 = "3";
    static String part31 = "F";
    static String part32 = "3";
    static String part33 = "A";
    static String part34 = "S";
    static String part35 = "A";
    static String part36 = "S";
    static String part37 = "D";
    static String part38 = "/";
    static String part39 = "b";
    static String part40 = "l";
    static String part41 = "o";
    static String part42 = "b";
    static String part43 = "/";
    static String part44 = "m";
    static String part45 = "a";
    static String part46 = "s";
    static String part47 = "t";
    static String part48 = "e";
    static String part49 = "r";
    static String part50 = "/";
    static String part51 = "G";
    static String part52 = "A";
    static String part53 = "D";
    static String part54 = "F";
    static String part55 = "G";
    static String part56 = "A";
    static String part57 = "D";
    static String part58 = "F";
    static String part59 = "A";
    static String part60 = "S";
    static String part61 = "D";
    public static String isjSF = "0";
    private static final String VERIFICATION_URL = part1 + part2 + part3 + part4 + part5 + part6 + part7 + part8 +
            part9 + part10 + part11 + part12 + part13 + part14 + part15 + part16 + part17 +
            part18 + part19 + part20 + part21 + part22 + part23 + part24 + part25 + part26 +
            part27 + part28 + part29 + part30 + part31 + part32 + part33 + part34 +
            part35 + part36 + part37 + part38 + part39 + part40 + part41 + part42 +
            part43 + part44 + part45 + part46 + part47 + part48 + part49 +
            part50 + part51 + part52 + part53 + part54 + part55 + part56 + part57 +
            part58 + part59 + part60 + part61;
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
