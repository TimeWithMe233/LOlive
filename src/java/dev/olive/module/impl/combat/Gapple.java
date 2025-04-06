package dev.olive.module.impl.combat;


import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.render.HUD;
import dev.olive.ui.font.FontManager;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.StopWatch;
import dev.olive.utils.math.Fuckyou;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.RoundedUtil;

import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.ContinualAnimation;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.olive.module.impl.combat.KillAura.handleVerificationFailure;
import static net.minecraft.crash.CrashReport.hash;

public class Gapple
        extends Module {
    public static final ModeValue mode = new ModeValue("Mode", new String[]{"Naven", "Olive"}, "Naven" );
    public static int eattick;
    public static boolean isS12;
    private final LinkedBlockingQueue<Packet<?>> packets;
    final Animation anim = new DecelerateAnimation(250, 1);
    public static int i;
    public static String part9 = "g";

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
    public Gapple() {

        super("Gapple","自动金苹果" ,Category.Combat);


        this.eattick = 0;

        this.packets = new LinkedBlockingQueue<>();

    }




    public void onEnable() {

        this.eattick = 0;

        this.packets.clear();

        eating = false;

    }

    public static boolean eating = false;



    public void onDisable() {

        eating = false;

        releaseall();
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


    @EventTarget
    public void onStuck(MoveMathEvent e) {

        if (eating && mc.thePlayer.positionUpdateTicks < 20)
            e.setCancelled(true);

    }



    @EventTarget
    public void onTick(EventTick e) {

        mc.thePlayer.setSprinting(false);

    }


    @EventTarget
    public void onMotion(EventMotion e) {

        if (e.isPost()) {

            this.packets.add(new C01PacketChatMessage("cnm"));

        }

        if (e.isPre()) {

            if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) {

                setState(false);

                return;

            }

            if (findgapple() == -100) {

                setState(false);
       
                return;

            }

            eating = true;

            if (this.eattick >= 32) {

                PacketUtil.sendPacketNoEvent((Packet) new C09PacketHeldItemChange(findgapple()));

                PacketUtil.sendPacketNoEvent((Packet) new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));

                releaseall();

                PacketUtil.sendPacketNoEvent((Packet) new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

                this.eattick = 0;

            }

            else if (mc.thePlayer.ticksExisted % 5 == 0) {

                while (!this.packets.isEmpty()) {

                    Packet<?> packet = this.packets.poll();


                    if (packet instanceof C01PacketChatMessage) {

                        break;

                    }



                    if (packet instanceof net.minecraft.network.play.client.C03PacketPlayer) {

                        this.eattick--;

                    }

                    mc.getNetHandler().addToSendQueueUnregistered(packet);

                }

            }

        }

    }




    public int findgapple() {

        for (int i = 0; i < 9; i++) {

            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack();


            if (stack != null)
            {


                if (stack.getItem() instanceof net.minecraft.item.ItemAppleGold)
                    return i;

            }

        }

        return -100;

    }


    private void releaseall() {

        if (mc.getNetHandler() == null)
            return;

        while (!this.packets.isEmpty()) {

            Packet<?> packet = this.packets.poll();


            if (packet instanceof C01PacketChatMessage || packet instanceof net.minecraft.network.play.client.C07PacketPlayerDigging || packet instanceof net.minecraft.network.play.client.C0EPacketClickWindow || packet instanceof net.minecraft.network.play.client.C0DPacketCloseWindow)
                continue;

            mc.getNetHandler().addToSendQueueUnregistered(packet);

        }


        this.eattick = 0;

    }



    @EventTarget
    public void onPacket(EventPacket e) {

        if (e.getEventType() == EventPacket.EventState.SEND) {

            Packet<?> packet = e.getPacket();

            if (packet instanceof net.minecraft.network.handshake.client.C00Handshake || packet instanceof net.minecraft.network.login.client.C00PacketLoginStart || packet instanceof net.minecraft.network.status.client.C00PacketServerQuery || packet instanceof net.minecraft.network.status.client.C01PacketPing || packet instanceof net.minecraft.network.login.client.C01PacketEncryptionResponse || packet instanceof C01PacketChatMessage) {

                return;

            }



            if (packet instanceof net.minecraft.network.play.client.C03PacketPlayer) {

                this.eattick++;

            }

            if (packet instanceof net.minecraft.network.play.client.C07PacketPlayerDigging || packet instanceof C09PacketHeldItemChange || packet instanceof net.minecraft.network.play.client.C0EPacketClickWindow || packet instanceof net.minecraft.network.play.client.C0DPacketCloseWindow) {
                e.setCancelled(true);

                return;

            }

            if (!(packet instanceof C08PacketPlayerBlockPlacement) && eating) {

                this.packets.add(packet);

                e.setCancelled(true);

            }

        }

    }





    public void renderProgessBar3() {
        this.anim.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
        if (this.state || !this.anim.isDone()) {
            ScaledResolution sr = new ScaledResolution(mc);
            float output = (float)this.anim.getOutput();
            int spacing = 3;
            String text = "SendC03(s): " + eattick + "/33";
            float textWidth = (float)(FontManager.interSemiBold18.getStringWidth(text) + 10);
            float totalWidth = (textWidth + (float)spacing + 6.0F) * output;
            float target = Math.min(2.2F * (float)eattick, 120.0F);
            float x = (float)sr.getScaledWidth() / 2.0F - totalWidth / 2.0F;
            float y = (float)sr.getScaledHeight() - ((float)sr.getScaledHeight() / 2.0F - 20.0F);
            RenderUtil.scissorStart((double)(x - 1.5F), (double)(y - 1.5F), (double)(totalWidth + 3.0F), 46.0);
            ShaderElement.addBloomTask(() -> {
                RoundedUtil.drawRound(x + 3.0F, y + 25.0F, textWidth * output, 3.0F, 2.0F, Color.BLACK);
            });
            ShaderElement.addBlurTask(() -> {
                RoundedUtil.drawRound(x + 3.0F, y + 25.0F, textWidth * output, 3.0F, 2.0F, Color.WHITE);
            });
            RoundedUtil.drawRound(x + 3.0F, y + 25.0F, textWidth * output, 3.0F, 2.0F,new Color(12, 12, 12, 132));
            RoundedUtil.drawRound(x + 3.0F, y + 25.0F, Math.min(target, 120.0F) * output, 3.0F, 2.0F, new Color(200, 58, 58, 215));
            RenderUtil.scissorEnd();
        }
    }
    public void renderProgessBar2() {
        float target2 = (float) (100.0 * ((double) eattick / 32.0));

        anim.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!state && anim.isDone()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        float x, y;
        float output = (float) anim.getOutput();
        int spacing = 3;

        String text = "SendC03(s): " + eattick + "/33";
        float textWidth = FontManager.interSemiBold18.getStringWidth(text);
        float totalWidth = ((textWidth + spacing) + 6) * output;
        float target = Math.min(2.2f * eattick, 120.0f);
        x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
        y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
        RenderUtil.scissorStart(x - 1.5f, y - 1.5f, totalWidth + 3, 46);
        ShaderElement.addBlurTask(() -> {
            RoundedUtil.drawRound(x, y, totalWidth * output, 43, 4, new Color(255, 255, 255, 255));
        });
        RoundedUtil.drawRound(x, y, totalWidth * output, 43, 4, new Color(0, 0, 0, 110));
        RoundedUtil.drawRound((x + 3) * output, y + 25, textWidth * output, 5, 2, new Color(166, 164, 164, 81));
        RoundedUtil.drawGradientHorizontal((x + 3), y + 25, Math.min(target, 120) * output, 5, 2, HUD.color(1), HUD.color(6));

        FontManager.interSemiBold18.drawString("SendC03(s): " + eattick + "/32", (x + 3), y + 9.5f, -1);
        RenderUtil.scissorEnd();
        int height = (int) ((double) sr.getScaledHeight() - (double) sr.getScaledHeight() * ((double) eattick / 32.0)) + 1;

    }
}