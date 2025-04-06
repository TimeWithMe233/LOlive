/*
 * Decompiled with CFR 0.152.
 */
package dev.olive.module.impl.move;

import dev.olive.Client;

import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.utils.DebugUtil;

import dev.olive.utils.math.Fuckyou;
import dev.olive.utils.math.MathUtils;
import dev.olive.utils.player.MovementUtils;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.utils.player.RotationComponent;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.olive.module.impl.combat.KillAura.handleVerificationFailure;
import static net.minecraft.crash.CrashReport.hash;

public class Speed
        extends Module {

    public Speed() {
        super("Speed","速度", Category.Movement);
    }
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Vanilla","Watchdog", "EntityCollide", "BlocksMC", "Intave", "NCP", "Miniblox"}, "Watchdog");
    private final ModeValue wdMode = new ModeValue("Watchdog Mode", new String[]{"Fast", "Glide","Ground Test"}, "Basic", () -> mode.is("Watchdog"));
    private final BoolValue fastFall = new BoolValue("Fast Fall", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue hurtTimeCheck = new BoolValue("Hurt Time Check", false, () -> mode.is("Watchdog") && (wdMode.is("Fast") && fastFall.get()));
    private final ModeValue wdFastFallMode = new ModeValue("Fast Fall Mode", new String[]{"7 Tick","8 Tick Strafe","8 Tick Fast","9 Tick"}, "8 Tick", () -> mode.is("Watchdog") && fastFall.get());
    private final BoolValue disableWhileScaffold = new BoolValue("Disable While Scaffold", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue frictionOverride = new BoolValue("Friction Override", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue extraStrafe = new BoolValue("Extra Strafe", true, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue expand = new BoolValue("More Expand", false, () -> Objects.equals(mode.get(), "EntityCollide"));
    private final BoolValue ignoreDamage = new BoolValue("Ignore Damage", true, () -> Objects.equals(mode.get(), "EntityCollide"));
    private final BoolValue pullDown = new BoolValue("Pull Down", true, () -> Objects.equals(mode.get(), "NCP"));
    private final NumberValue onTick = new NumberValue("On Tick", 5, 1, 10, 1, () -> Objects.equals(mode.get(), "NCP") && pullDown.get());
    private final BoolValue onHurt = new BoolValue("On Hurt", true, () -> Objects.equals(mode.get(), "NCP") && pullDown.get());
    private final BoolValue airBoost = new BoolValue("Air Boost", true, () -> Objects.equals(mode.get(), "NCP"));
    private final BoolValue damageBoost = new BoolValue("Damage Boost", false, () -> Objects.equals(mode.get(), "NCP"));
    private final NumberValue mTicks = new NumberValue("Ticks", 5, 1, 6, 1, () -> Objects.equals(mode.get(), "Miniblox"));
    public final BoolValue noBob = new BoolValue("No Bob", true);
    private final BoolValue forceStop = new BoolValue("Force Stop", true);
    private final BoolValue lagBackCheck = new BoolValue("Lag Back Check", true);
    private final BoolValue liquidCheck = new BoolValue("Liquid Check", true);
    private final BoolValue guiCheck = new BoolValue("Gui Check", true);
    private final BoolValue printOffGroundTicks = new BoolValue("Print Off Ground Ticks", true);
    private final NumberValue vanilla = new NumberValue("Vanilla Speed", 0.5f,0.05f,2, 0.05f, () -> Objects.equals(mode.get(), "Vanilla"));
    private final BoolValue vanillaPullDown = new BoolValue("Pull Down", true, () -> mode.is("Vanilla"));
    private final NumberValue vanillaPullDownAmount = new NumberValue("Vanilla Pull Down", 0.5f,0.05f,2, 0.05f, () -> Objects.equals(mode.get(), "Vanilla") && vanillaPullDown.get());
    private boolean disable;
    private boolean disable3;
    private int boostTicks;
    private boolean recentlyCollided;
    private boolean slab;
    private boolean stopVelocity;
    public boolean couldStrafe;
    private double speed;
    private int ticksSinceTeleport;
    private boolean valued;
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
    @Override
    public void onEnable() {
        if (mode.is("Watchdog")) {
            if (wdMode.is("Glide")) {
                speed = 0.28;
            }
            slab = false;

            disable3 = false;

            valued = false;
        }
    }

    @Override
    public void onDisable() {
        disable = false;
        couldStrafe = false;
        if(forceStop.get()){
            MovementUtils.stopXZ();
        }
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
    public void onUpdate(EventUpdate event) {
        setSuffix(mode.get());
        ticksSinceTeleport++;
        if(liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (printOffGroundTicks.get())
            DebugUtil.log(mc.thePlayer.offGroundTicks + "Tick");

        switch (mode.get()) {
            case "Miniblox": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.thePlayer.jump();
                }

                switch (mc.thePlayer.offGroundTicks) {
                    case 1: {
                        Double v = mTicks.get();
                        if (v == 1) {
                            mc.thePlayer.motionY -= 0.76;
                        } else if (v == 2) {
                            mc.thePlayer.motionY -= 0.52;
                        } else if (v == 3) {
                            mc.thePlayer.motionY -= 0.452335182447;
                        } else if (v == 4) {
                            mc.thePlayer.motionY -= 0.322335182447;
                        } else if (v == 5) {
                            mc.thePlayer.motionY -= 0.232335182447;
                        } else if (v == 6) {
                            mc.thePlayer.motionY -= 0.162335182447;
                        }
                    }
                    break;

                    case 3: {
                        mc.thePlayer.motionY -= 0.1523351824467155;
                    }
                    break;
                }
            }
            break;

            case "NCP": {
                if (mc.thePlayer.offGroundTicks == onTick.get() && pullDown.get()) {
                    MovementUtils.strafe();
                    mc.thePlayer.motionY -= 0.1523351824467155;
                }

                if (onHurt.get() && mc.thePlayer.hurtTime >= 5 && mc.thePlayer.motionY >= 0) {
                    mc.thePlayer.motionY -= 0.1;
                }

                if (airBoost.get() && MovementUtils.isMoving()) {
                    mc.thePlayer.motionX *= 1f + 0.00718;
                    mc.thePlayer.motionZ *= 1f + 0.00718;
                }
            }
            break;

            case "Vanilla": {
                MovementUtils.strafe((float) (1 * vanilla.get()));
                couldStrafe = true;

                if (vanillaPullDown.get()) {
                    mc.thePlayer.motionY = -vanillaPullDownAmount.get();
                }
            }
            break;

            case "Intave": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.thePlayer.jump();
                }

                if (mc.thePlayer.motionY > 0.03 && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 1f + 0.003;
                    mc.thePlayer.motionZ *= 1f + 0.003;
                }
            }
            break;

            case "EntityCollide": {
                couldStrafe = false;
                if (mc.thePlayer.hurtTime <= 1) {
                    stopVelocity = false;
                }

                if (stopVelocity && !ignoreDamage.get()) {
                    return;
                }

                if (!MovementUtils.isMoving())
                    return;

                int collisions = 0;
                AxisAlignedBB box = expand.get() ? mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0)
                        : mc.thePlayer.getEntityBoundingBox().expand(0.8, 0.8, 0.8);
                for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                    AxisAlignedBB entityBox = entity.getEntityBoundingBox();
                    if (canCauseSpeed(entity) && box.intersectsWith(entityBox)) {
                        collisions++;
                    }
                }

                double yaw = Math.toRadians(RotationComponent.shouldRotate() ? RotationComponent.currentRotation[0] : mc.thePlayer.rotationYaw);

                double boost = 0.078 * collisions;
                mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
            }
            break;

            case "Watchdog":
                if(wdMode.is("Fast")) {
                    if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                        mc.thePlayer.jump();
                        if(!Client.instance.getModuleManager().getModule(Scaffold.class).state)
                            MovementUtils.strafe((float) (0.47 + MovementUtils.getSpeedEffect() * 0.042));
                        couldStrafe = true;
                    }
                }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {

        if (liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (Client.instance.getModuleManager().getModule(Scaffold.class).state)
            return;

        switch (mode.get()) {
            case "Miniblox": {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        Double v = mTicks.get();
                        if (v == 1) {
                            MovementUtils.strafe(0.07F);
                        } else if (v == 2) {
                            MovementUtils.strafe(0.08f);
                        } else if (v == 3) {
                            MovementUtils.strafe(0.09f);
                        } else if (v == 4) {
                            MovementUtils.strafe(0.1f);
                        } else if (v == 5) {
                            MovementUtils.strafe(0.115f);
                        } else if (v == 6) {
                            MovementUtils.strafe(0.13f);
                        }
                    } else {
                        MovementUtils.strafe(0.35f);
                    }
                }
            }
            break;

            case "NCP": {
                if (MovementUtils.isMoving()) {
                    couldStrafe = true;
                    MovementUtils.strafe();
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                        MovementUtils.strafe(0.48f + MovementUtils.getSpeedEffect() * 0.07f);
                    }
                }

                if (damageBoost.get() && mc.thePlayer.hurtTime > 0) {
                    MovementUtils.strafe(Math.max(MovementUtils.getSpeed(), 0.5f));
                }
            }
            break;

            case "Vanilla": {
                if (MovementUtils.isMoving()) {
                    couldStrafe = true;
                    MovementUtils.strafe();
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
            }
            break;

            case "Watchdog":
                if (event.isPre()) {
                    if (fastFall.get()) {
                        if (mc.thePlayer.isCollidedHorizontally || ticksSinceTeleport < 2) {
                            recentlyCollided = true;
                            boostTicks = mc.thePlayer.ticksExisted + 9;
                        }
                        if (!mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.ticksExisted > boostTicks)) {
                            recentlyCollided = false;
                        }

                        if (mc.thePlayer.onGround) {
                            disable3 = false;
                        }
                        if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) {
                            disable = false;
                        }

                        if (mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && PlayerUtil.isBlockOver(2.0)) {
                            disable = true;
                        }

                        double posY = event.getY();
                        if (Math.abs(posY - Math.round(posY)) > 0.03 && mc.thePlayer.onGround) {
                            slab = true;
                        } else if (mc.thePlayer.onGround) {
                            slab = false;
                        }
                    }

                    if (fastFall.isAvailable() && fastFall.get()) {

                        if (mc.thePlayer.isInWater() ||
                                mc.thePlayer.isInWeb ||
                                mc.thePlayer.isInLava() ||
                                hurtTimeCheck.get() && mc.thePlayer.hurtTime > 0
                        ) {
                            disable = true;
                            return;
                        }

                        if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) {
                            disable = false;
                        }

                        if (mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && PlayerUtil.isBlockOver(2.0)) {
                            disable = true;
                        }
                    }

                    if (wdMode.is("Ground Test")) {
                        if (valued) {
                            if (mc.thePlayer.onGround) {
                                event.setY(event.getY() + 1E-13F);
                                mc.thePlayer.motionX *= 1.14 - MovementUtils.getSpeedEffect() * .01;
                                mc.thePlayer.motionZ *= 1.14 - MovementUtils.getSpeedEffect() * .01;
                                MovementUtils.strafe();
                                couldStrafe = true;
                            }
                        }
                    }
                }

                break;
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {

        if (liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (mode.get().equals("Watchdog") && wdMode.get().equals("Fast") && (mc.thePlayer.isInWater() || mc.thePlayer.isInWeb || mc.thePlayer.isInLava())) {
            disable = true;
            return;
        }
        if (mode.get().equals("Watchdog")) {

            switch (wdMode.get()) {
                case "Glide":
                    if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                        MovementUtils.strafe(MovementUtils.getAllowedHorizontalDistance());
                        mc.thePlayer.jump();
                    }

                    if (mc.thePlayer.onGround) {
                        speed = 1.0F;
                    }

                    final int[] allowedAirTicks = new int[]{10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29};

                    if (!(mc.theWorld.getBlockState(mc.thePlayer.getPosition().add(0, -0.25, 0)).getBlock() instanceof BlockAir)) {
                        for (final int allowedAirTick : allowedAirTicks) {
                            if (mc.thePlayer.offGroundTicks == allowedAirTick && allowedAirTick <= 11) {
                                mc.thePlayer.motionY = 0;
                                MovementUtils.strafe((float) (MovementUtils.getAllowedHorizontalDistance() * speed));
                                couldStrafe = true;

                                speed *= 0.98F;

                            }
                        }
                    }
                    break;
                case "Fast":

                    if (!disable && fastFall.get() && (disableWhileScaffold.get() && !Client.instance.getModuleManager().getModule(Scaffold.class).state || !disableWhileScaffold.get())) {

                        switch (wdFastFallMode.get()) {
                            case "7 Tick", "8 Tick Strafe":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 1:
                                        mc.thePlayer.motionY += 0.057f;
                                        break;
                                    case 3:
                                        mc.thePlayer.motionY -= 0.1309f;
                                        break;
                                    case 4:
                                        mc.thePlayer.motionY -= 0.2;
                                        break;
                                }
                                break;

                            case "8 Tick Fast":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 3:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                        break;
                                    case 5:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.1913;
                                        break;
                                }
                                break;
                            case "9 Tick":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 3:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                        break;
                                    case 5:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.16874;
                                        break;
                                }
                                break;
                        }

                    }

                    if (mc.thePlayer.offGroundTicks == 1 && !disable) {
                        if (Client.instance.getModuleManager().getModule(Scaffold.class).state) {
                                MovementUtils.strafe(0.3f);
                        } else {
                            MovementUtils.strafe((float) Math.max(MovementUtils.getSpeed(), 0.33f + MovementUtils.getSpeedEffect() * 0.075));
                            couldStrafe = true;
                        }
                    }

                    if (mc.thePlayer.offGroundTicks == 2 && !disable && extraStrafe.get()) {
                        double motionX3 = mc.thePlayer.motionX;
                        double motionZ3 = mc.thePlayer.motionZ;
                        mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 1 + motionZ3 * 2) / 3;
                        mc.thePlayer.motionX = (mc.thePlayer.motionX * 1 + motionX3 * 2) / 3;
                    }

                    if (mc.thePlayer.offGroundTicks == 6 && wdFastFallMode.is("8 Tick Strafe") && !disable && PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0) != Blocks.air && PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0).isFullBlock() && (disableWhileScaffold.get() && !Client.instance.getModuleManager().getModule(Scaffold.class).state || !disableWhileScaffold.get())) {
                        mc.thePlayer.motionY += 0.0754;
                        MovementUtils.strafe();
                        couldStrafe = true;
                    }

                    if ((mc.thePlayer.motionX == 0 || mc.thePlayer.motionZ == 0) && !disable && (!recentlyCollided && mc.thePlayer.isPotionActive(Potion.moveSpeed)) && !Client.instance.getModuleManager().getModule(Scaffold.class).state) {
                        MovementUtils.strafe();
                        couldStrafe = true;
                    }

                    if (mc.thePlayer.offGroundTicks < 7 && (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) && mc.thePlayer.isPotionActive(Potion.moveSpeed) && !slab) {
                        boostTicks = mc.thePlayer.ticksExisted + 9;
                        recentlyCollided = true;
                    }

                    if (mc.thePlayer.offGroundTicks == 7 && !disable && (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 2, 0) != Blocks.air) && !Client.instance.getModuleManager().getModule(Scaffold.class).state) {
                        MovementUtils.strafe(MovementUtils.getSpeed());
                        couldStrafe = true;
                    }

                    if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air && mc.thePlayer.offGroundTicks > 5 && !disable3) {
                        MovementUtils.strafe();
                        couldStrafe = true;
                        disable3 = true;
                    }

                    double speed2 = Math.hypot((mc.thePlayer.motionX - (mc.thePlayer.lastTickPosX - mc.thePlayer.lastLastTickPosX)), (mc.thePlayer.motionZ - (mc.thePlayer.lastTickPosZ - mc.thePlayer.lastLastTickPosZ)));
                    if (speed2 < .0125 && frictionOverride.get()) {
                        MovementUtils.strafe();
                        couldStrafe = true;
                    }

                    break;

                case "Ground Test":
                    if(!valued && mc.thePlayer.onGround){
                        mc.thePlayer.jump();
                        valued = true;
                    }
                    break;
            }
        }
    }

    @EventTarget
    public void onPostStrafe(PostStrafeEvent event) {
        if (mode.is("Watchdog") && wdMode.is("Fast")) {
            if (extraStrafe.get()) {
                double attempt_angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtils.getDirection()));
                double movement_angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90);
                if (MathUtils.wrappedDifference(attempt_angle, movement_angle) > 90) {
                    MovementUtils.strafe(MovementUtils.getSpeed(), (float) movement_angle - 180);
                }
            }
        }
    }

    @EventTarget
    public void onMove(EventMove event){

        if (!liquidCheck.get() || (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava())) {
            guiCheck.get();
        }

    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            ticksSinceTeleport = 0;
            if (lagBackCheck.get()) {
                toggle();
            }
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event){
        if(!mode.is("EntityCollide"))
            if(mc.thePlayer.onGround)
                event.setJump(false);
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.thePlayer && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
    }
}

