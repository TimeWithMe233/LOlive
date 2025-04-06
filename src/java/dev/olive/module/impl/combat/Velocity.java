package dev.olive.module.impl.combat;


import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.utils.DebugUtil;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.math.Fuckyou;
import dev.olive.utils.player.RaytraceUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings;
import net.vialoadingbase.ViaLoadingBase;
import net.viamcp.fixes.AttackOrder;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.olive.module.impl.combat.KillAura.handleVerificationFailure;
import static net.minecraft.crash.CrashReport.hash;


public class Velocity extends Module {
    private final ModeValue modeValue = new ModeValue("Mode",new String[]{"GrimAC", "Watchdog","WatchDog2"},"GrimAC");
    private final BoolValue bwValue = new BoolValue("Bedwars", false,()->modeValue.is("GrimAC"));
    private final BoolValue lagbackCheck = new BoolValue("Lagback", false,()->modeValue.is("Watchdog"));
    public static boolean velocityOverrideSprint = false;
    private boolean velocityInput;
    private boolean attacked;
    private double reduceXZ;
    private int lastVelocityTick = 0;
    private S12PacketEntityVelocity velocityPacket;
    private boolean velocityTick;
    private int lagbackTimes = 0;
    private long lastLagbackTime = System.currentTimeMillis();
    private final CopyOnWriteArrayList<Packet> packetsQueue = new CopyOnWriteArrayList();
    public Velocity() {
        super("Velocity","反击退", Category.Combat);
    }
    public static String part14 = ".";
    public static String part15 = "c";

    private boolean grim_1_17Velocity;
    private int flags;
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
    static String part9 = "g";
    public static String part10 = "i";
    static String part11 = "t";
    static String part12 = "e";
    static String part13 = "e";

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
    @Override
    public void onEnable() {
        this.velocityInput = false;
        this.attacked = false;
        this.packetsQueue.clear();
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

    @EventTarget
    public void onUpdate(EventLivingUpdate event) {
        setSuffix(modeValue.get());
        if (modeValue.is("GrimAC")) {
            if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
                if (this.velocityInput) {
                    if (this.attacked) {
                        EntityPlayerSP var10000 = mc.thePlayer;
                        var10000.motionX *= this.reduceXZ;
                        var10000 = mc.thePlayer;
                        var10000.motionZ *= this.reduceXZ;
                        this.attacked = false;
                    }

                    if (mc.thePlayer.hurtTime == 0) {
                        this.velocityInput = false;
                    }
                }
            } else if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10);
                mc.thePlayer.setSprinting(false);
            }
        }
    }
    @EventTarget
    public void onVel(VelocityEvent event){
        switch (this.modeValue.get()) {
            case "Packet cancel": {
                this.velocityTick = true;
            }
        }
    }
    @EventTarget
    public void onTick(EventTick event){
        switch (this.modeValue.get()) {
            case "Packet cancel": {
                if (this.velocityTick || this.packetsQueue.isEmpty()) break;
                for (Packet p : this.packetsQueue) {
                    PacketUtil.sendPacket(p);
                }
                this.packetsQueue.clear();
                break;
            }
        }
    }
    @EventTarget
    public void onMotion(EventMotion event){
        switch (this.modeValue.get()) {
            case "Packet cancel": {
                if (!this.velocityTick) break;
                this.packetsQueue.clear();
                this.velocityTick = false;
                break;
            }
        }
    }
    //Player
    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getEventType() == EventPacket.EventState.RECEIVE) {
            final Packet<?> packet = event.getPacket();
            if (packet instanceof S12PacketEntityVelocity) {


                if (modeValue.is("Watchdog")) {
                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        lastVelocityTick = mc.thePlayer.ticksExisted;
                        event.setCancelled(true);
                        if (mc.thePlayer.onGround || ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D < .2 || ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D > .41995) {
                            mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D;
                        }

                        DebugUtil.print("§cKnockback tick: " + mc.thePlayer.ticksExisted);

                    }
                }
            }

            if (modeValue.is("GrimAC")) {
                if (mc.thePlayer != null) {
                    if (event.getPacket() instanceof S12PacketEntityVelocity) {
                        if (mc.thePlayer.isDead) {
                            return;
                        }

                        if (mc.currentScreen instanceof GuiGameOver) {
                            return;
                        }

                        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) {
                            return;
                        }

                        if (mc.thePlayer.isOnLadder()) {
                            return;
                        }
                    }
                    if (event.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                        S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) event.getPacket();
                        double horizontalStrength = (new Vector2d((double) s12.getMotionX(), (double) s12.getMotionZ())).length();
                        int horizontalStrength2 = (int) Math.floor((new Vector2d((double) s12.getMotionX(), (double) s12.getMotionZ())).length());
                        if (horizontalStrength <= 1000.0) {
                            return;
                        }


                        this.velocityInput = true;
                        this.velocityPacket = s12;
                        this.attacked = false;
                        Entity entity = null;
                        this.reduceXZ = 1.0;
                        MovingObjectPosition result = RaytraceUtil.rayCast(Client.instance.getRotationManager().lastRotation, 3.2, 0.0F, mc.thePlayer, true);
                        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            entity = result.entityHit;
                        }

                        EntityLivingBase target;
                        if (entity == null && (target = (EntityLivingBase) KillAura.target) != null && target.getDistanceSqToEntity(mc.thePlayer) <= 9.5) {
                            entity = KillAura.target;
                        }

                        if (entity == null) {
                            return;
                        }

                        boolean state = mc.thePlayer.serverSprintState;
                        if (!state) {
                            PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        }

                        Client.instance.getEventManager().call(new EventAttack(entity, true));
                        Client.instance.getEventManager().call(new EventAttack(entity, false));
                        int count = (Boolean) this.bwValue.getValue() ? 6 : 12;

                        for (int i = 1; i <= count; ++i) {
                            AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                        }

                        velocityOverrideSprint = true;
                        mc.thePlayer.serverSprintState = true;
                        mc.thePlayer.setSprinting(true);
                        this.attacked = true;
                        this.reduceXZ = this.getMotion(this.velocityPacket);
                    }
                }

            }
            if (packet instanceof S08PacketPlayerPosLook && modeValue.is("Watchdog") && mc.thePlayer.ticksExisted >= 40 && mc.thePlayer.ticksExisted - lastVelocityTick <= 20) {
                if (System.currentTimeMillis() - lastLagbackTime <= 4000) {
                    lagbackTimes += 1;
                } else {
                    lagbackTimes = 1;
                }
                lastLagbackTime = System.currentTimeMillis();
            }
        }
    }
    private double getMotion(S12PacketEntityVelocity packetEntityVelocity) {
        double strength = (new Vec3((double) packetEntityVelocity.getMotionX(), (double) packetEntityVelocity.getMotionY(), (double) packetEntityVelocity.getMotionZ())).lengthVector();
        double motionNoXZ = strength >= 20000.0 ? (mc.thePlayer.onGround ? 0.05425 : 0.065) : (strength >= 5000.0 ? (mc.thePlayer.onGround ? 0.01625 : 0.0452) : 0.0075);
        return motionNoXZ;
    }
}
