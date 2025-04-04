package dev.olive.module.impl.misc;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.ModuleManager;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.module.impl.player.BedBreaker;
import dev.olive.module.impl.world.PlayerTracker;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.ui.hud.notification.NotificationType;
import dev.olive.utils.HYTUtils;
import dev.olive.utils.ServerUtils;
import dev.olive.utils.Servers;
import dev.olive.utils.TimerUtil;
import dev.olive.utils.math.MathUtils;
import dev.olive.utils.tasks.FutureTask;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ScreenShotHelper;
import net.netease.GsonUtil;
import net.netease.PacketProcessor;
import net.netease.gui.GermGameGui;
import net.netease.packet.impl.Packet04;
import net.netease.packet.impl.Packet26;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;



public class AutoPlay
        extends Module {
    private static Servers currentServer = Servers.NONE;
    private boolean setLanguage, wasPre, mwPort, hubbed, notify;
    private long gled = System.currentTimeMillis();
    public static ModeValue mode = new ModeValue("Mode",new String[]{"Hypixel","Hyt"},"Hyt");
    public BoolValue bantracker = new BoolValue("BanTracker", true,()->mode.is("Hypixel"));
    private BoolValue reconnect = new BoolValue("Reconnect", true,()->mode.is("Hypixel"));
    private BoolValue auto_play = new BoolValue("AutoPlay", true,()->mode.is("Hypixel"));
    private NumberValue delay = new NumberValue("Delay", 3, 1, 10,1,()->auto_play.get());
    private BoolValue auto_gg = new BoolValue("AutoGG", true,()->mode.is("Hypixel"));
    private BoolValue auto_gl = new BoolValue("AutoGL", false,()->mode.is("Hypixel"));
    private BoolValue auto_who = new BoolValue("AutoWho", true,()->mode.is("Hypixel"));
    private BoolValue auto_screenshot = new BoolValue("AutoScreenShot", true,()->mode.is("Hypixel"));
    private NumberValue auto_screenshot_delay = new NumberValue("ScreenShotDelay", 700, 50, 3000,100,()->auto_screenshot.get()&&mode.is("Hypixel"));

    public static final BoolValue swValue = new BoolValue("SkyWars", true,()->mode.is("Hyt"));
    public static final BoolValue autoKit = new BoolValue("AutoKit", true,()->mode.is("Hyt"));
    public static final BoolValue bwValue = new BoolValue("BedWars", true,()->mode.is("Hyt"));
    public static final BoolValue toggleModule = new BoolValue("Toggle Module", true,()->mode.is("Hyt"));
    public static final NumberValue delayValue = new NumberValue("Delay", 3.0, 1.0, 10.0, 0.1,()->mode.is("Hyt"));
    public boolean display = false;
    private final TimerUtil timer = new TimerUtil();
    private boolean waiting = false;
    private boolean waiting2 = false;
    public static String name;
    public static boolean regen = false;
    public static boolean strength = false;
    public static boolean gapple = false;
    public static boolean godaxe = false;
    public static boolean kbball = false;
    private static final Pattern PATTERN_BEHAVIOR_EXCEPTION = Pattern.compile("\u73a9\u5bb6(.*?)\u5728\u672c\u5c40\u6e38\u620f\u4e2d\u884c\u4e3a\u5f02\u5e38");
    private static final Pattern PATTERN_WIN_MESSAGE = Pattern.compile("\u4f60\u5728\u5730\u56fe(.*?)\u4e2d\u8d62\u5f97\u4e86(.*?)");
    private static final String TEXT_LIKE_OPTIONS = "      \u559c\u6b22      \u4e00\u822c      \u4e0d\u559c\u6b22";
    private static final String TEXT_BEDWARS_GAME_END = "[\u8d77\u5e8a\u6218\u4e89] Game \u7ed3\u675f\uff01\u611f\u8c22\u60a8\u7684\u53c2\u4e0e\uff01";
    private static final String TEXT_COUNTDOWN = "\u5f00\u59cb\u5012\u8ba1\u65f6: 1 \u79d2";
    public int ban = 0;
    public int win = 0;

    public AutoPlay() {
        super("AutoPlay","自动游玩", Category.Misc);
    }

    @EventTarget
    public void onEventWorldLoad(EventWorldLoad event) {

        strength = false;
        regen = false;
        godaxe = false;
        gapple = false;
        kbball = false;
    }

    @Override
    public void onEnable() {
        ban = 0;
        win = 0;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        ban = 0;
        win = 0;
        super.onDisable();
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (mode.is("Hyt")) {
            ItemStack itemStack;
            if (event.isPost()) {
                return;
            }
            if (this.waiting && this.waiting2) {
                AutoPlay.mc.thePlayer.swingItem();
                HashMap<String, Integer> data = new HashMap<String, Integer>();
                data.put("click", 1);
                String json = GsonUtil.toJson(data);
                String message = new StringBuilder().insert(0, "GUI$").append("mainmenu").append("@").append("subject/skywar").toString();
                PacketProcessor.INSTANCE.sendPacket(new Packet04("mainmenu"));
                PacketProcessor.INSTANCE.sendPacket(new Packet26(message, json));
                HashMap<String, Object> data2 = new HashMap<String, Object>();
                data2.put("entry", GermGameGui.INSTANCE.getCurrentElement().getSubElements().get(0).getIndex());
                data2.put("sid", GermGameGui.INSTANCE.getCurrentElement().getSubElements().get(0).getSid());
                String json2 = GsonUtil.toJson(data2);
                String message2 = new StringBuilder().insert(0, "GUI$").append("mainmenu").append("@").append("entry/").append(0).toString();
                PacketProcessor.INSTANCE.sendPacket(new Packet04("mainmenu"));
                PacketProcessor.INSTANCE.sendPacket(new Packet26(message2, json2));
                this.waiting = false;
                this.waiting2 = false;
            }
            if ((itemStack = AutoPlay.mc.thePlayer.inventoryContainer.getSlot(44).getStack()) == null || itemStack.getDisplayName() == null) {
                return;
            }
            if (itemStack.getDisplayName().contains("\u6e38\u620f\u6307\u5357")) {
                this.waiting2 = true;
            }
            if (!itemStack.getDisplayName().contains("\u9000\u51fa\u89c2\u6218")) {
                return;
            }
            if (itemStack.getItem().equals(Items.iron_door) && this.swValue.getValue().booleanValue() || itemStack.getItem().equals(Items.chest_minecart) && this.bwValue.getValue().booleanValue()) {
                this.timer.reset();
                this.waiting = true;
            }
        }
    }
    @EventTarget
    public void onEventTick(EventTick event) {
        if (mode.is("Hyt")) {
            if (isNull()) return;
            if (mc.theWorld == null || mc.theWorld.loadedEntityList.isEmpty()) {
                strength = false;
                regen = false;
                godaxe = false;
                gapple = false;
                kbball = false;
                return;
            }
            if (HYTUtils.isInLobby()) {
                strength = false;
                regen = false;
                godaxe = false;
                gapple = false;
                kbball = false;
                return;
            }
            if (autoKit.get()) {
                if (mc.currentScreen != null) {
                    if (mc.currentScreen instanceof GuiChest chest) {
                        if (chest.lowerChestInventory.getDisplayName().toString().contains("职业"))
                            mc.playerController.windowClick(chest.inventorySlots.windowId, 6, 0, 0, mc.thePlayer);
                    }
                }
            }
        }
    }
    public Map<String, String> playerTag = new HashMap<>();
    @EventTarget
    private void updateEventHandler(HypixelServerSwitchEvent event) {
        if (mode.is("Hypixel")) {
            if (event.lastServer == Servers.PRE) {
                if (event.server == Servers.BW) {
                    BedBreaker.setWhiteListed(null);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            for (int x = -20; x < 21; x++) {
                                for (int z = -20; z < 21; z++) {
                                    for (int y = -10; y < 12; y++) {
                                        BlockPos pos = new BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z);
                                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                                        if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                                            BedBreaker.setWhiteListed(pos);
                                            NotificationManager.post(NotificationType.INFO, "Whitelisted your own bed!", "Whitelisted bed at " + pos, 3000);
                                        }
                                    }
                                }
                            }
                        }
                    }, 1000);
                }

                if (event.server != Servers.NONE) {
                    if (auto_gl.getValue()) mc.thePlayer.sendChatMessage("/ac glhf");
                    if (auto_who.getValue()) mc.thePlayer.sendChatMessage("/who");
                    NotificationManager.post(NotificationType.INFO, "HypixelUtils", "Game Started! Mode: " + event.server.name());
                }
            }
            if (event.server == Servers.PRE) {
                if (auto_who.getValue()) mc.thePlayer.sendChatMessage("/who");
            }
        }
        ;
    }
    @EventTarget
    public void onPacketReceiveEvent(EventPacket event) {
        if (mode.is("Hyt")) {
            if (AutoPlay.mc.thePlayer == null || AutoPlay.mc.theWorld == null) {
                return;
            }
            Packet packet = event.getPacket();
            String text = ((S02PacketChat) packet).getChatComponent().getUnformattedText();

            if (packet instanceof S02PacketChat) {
                if (PATTERN_BEHAVIOR_EXCEPTION.matcher(text).find()) {
                    NotificationManager.post(NotificationType.WARNING, "BanChecker", "A player was banned.", 5.0f);
                    ++ban;
                } else if (PATTERN_WIN_MESSAGE.matcher(text).find() || AutoPlay.mc.thePlayer.isSpectator() && this.toggleModule.getValue().booleanValue()) {
                    this.toggleOffensiveModules(false);
                    NotificationManager.post(NotificationType.SUCCESS, "Game Ending", "Sending you to next game in " + this.delayValue.getValue() + "s", 5.0f);
                } else if (text.contains(TEXT_LIKE_OPTIONS) || text.contains(TEXT_BEDWARS_GAME_END)) {
                    NotificationManager.post(NotificationType.SUCCESS, "Game Ending", "Your Health: " + MathUtils.DF_1.format(AutoPlay.mc.thePlayer.getHealth()), 5.0f);
                } else if (text.contains(TEXT_COUNTDOWN)) {
                    this.checkAndTogglePlayerTracker();
                }
            }

            if (text.contains("你在地图") && text.contains("赢得了")) {
                ++win;
            } else if (text.contains("[起床战争] Game 结束！感谢您的参与！") || text.contains("喜欢 一般 不喜欢")) {
                ++win;
            }
            if (text.contains("开始倒计时: 5 秒") && autoKit.get()) {
                int slot = 0;

                int nslot = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
                mc.rightClickMouse();
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(nslot));
                System.out.println(slot);
                System.out.println(nslot);
            }
        }
    }
    private void autoRegl(EventPacket event) {
        if (event.getPacket() instanceof S02PacketChat && Math.abs(System.currentTimeMillis() - gled) <= 3000) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();
            if (message.contains("You can't shout if you're not in a team game!")) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mc.thePlayer.sendChatMessage("/ac glhf");
                    }
                }, 4000);
            }
        }
    }

    private void reconnect(EventPacket event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();

            if (message.contains("Flying or related")) {
                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("/back"));
            }
        }
    }

    private void autoplay(EventPacket packetEventReceive) {
        if (packetEventReceive.getPacket() instanceof S02PacketChat) {
            try {
                final S02PacketChat packet = (S02PacketChat) packetEventReceive.getPacket();
                final String command = packet.getChatComponent().toString().split("action=RUN_COMMAND, value='")[1];

                if (command.startsWith("/play ")) {
                    final String split = command.split("'}")[0];
                   NotificationManager.post( NotificationType.INFO,"AutoPlay","Sending you to the next game in ", (float) (delay.getValue() * 1));

                    Client.instance.getTaskManager().queue(new dev.olive.utils.tasks.FutureTask((int) (this.delay.getValue() * 1_000)) {

                        @Override
                        public void execute() {
                            mc.getNetHandler().addToSendQueue(new C01PacketChatMessage(split));
                        }

                        @Override
                        public void run() {
                        }
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    private void autogg(EventPacket e) {
        if (e.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) e.getPacket();

            if (packet.getMessage().getUnformattedText().contains("VICTORY!")) {

                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("gg"));

            }
        }
    }

    private void autoscreenshot(EventPacket e) {
        if (e.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) e.getPacket();

            if (packet.getMessage().getUnformattedText().contains("VICTORY!")) {
                Client.instance.getTaskManager().queue(new FutureTask(auto_screenshot_delay.getValue().intValue()) {
                    @Override
                    public void execute() {
                        ScreenShotHelper.safeSaveScreenshot();
                    }

                    @Override
                    public void run() {
                    }
                });
            }
        }
    }

    private void quickmath(EventPacket event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) event.getPacket();
            String text = packetChat.getChatComponent().getUnformattedText();

            if (text.contains("QUICK MATHS! Solve:")) {
                String[] eArray = text.split("Solve: ");
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");

                try {
                    mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage(engine.eval(eArray[1].replace("x", "*")).toString()));
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @EventTarget
    private void packetEvent(EventPacket event) {
        if (mode.is("Hypixel")) {
            if (event.getEventType() == EventPacket.EventState.RECEIVE) {
                if (reconnect.getValue()) {
                    reconnect(event);
                }

                if (auto_play.getValue()) {
                    autoplay(event);
                }

                if (auto_gg.getValue()) {
                    autogg(event);
                }

                if (auto_screenshot.getValue()) {
                    autoscreenshot(event);
                }

                if (ServerUtils.isHypixel() && event.getPacket() instanceof S3DPacketDisplayScoreboard) {
                    S3DPacketDisplayScoreboard packet = (S3DPacketDisplayScoreboard) event.getPacket();
                    String serverName = packet.func_149370_d();
                    Servers _currentServer = Servers.NONE;

                    if (serverName.equalsIgnoreCase("Mw")) {
                        _currentServer = Servers.MW;
                    } else if (serverName.equalsIgnoreCase("§e§lHYPIXEL")) {
                        _currentServer = Servers.UHC;
                    } else if (serverName.equalsIgnoreCase("SForeboard")) {
                        _currentServer = Servers.SW;
                    } else if (serverName.equalsIgnoreCase("BForeboard")) {
                        _currentServer = Servers.BW;
                    } else if (serverName.equalsIgnoreCase("PreScoreboard")) {
                        _currentServer = Servers.PRE;
                    } else if (serverName.equalsIgnoreCase("Duels")) {
                        _currentServer = Servers.DUELS;
                    } else if (serverName.equalsIgnoreCase("Pit")) {
                        _currentServer = Servers.PIT;
                    } else if (serverName.equalsIgnoreCase("Blitz SG")) {
                        _currentServer = Servers.SG;
                    } else if (serverName.equalsIgnoreCase("MurderMystery")) {
                        _currentServer = Servers.MM;
                    } else if (!serverName.contains("health") && !serverName.contains("\u272B")) {
                        _currentServer = Servers.NONE;
                    }

                    if (_currentServer != currentServer) {
                        Client.instance.getEventManager().call(new HypixelServerSwitchEvent(currentServer, _currentServer));
                        currentServer = _currentServer;
                    }
                }
            }
        }
        ;
    }
    private void onPaste(String str){
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            StringSelection strse1 = new StringSelection(str);
            c.setContents(strse1, strse1);
        } catch (Exception e){
            e.printStackTrace();
        }//try
    }//onPaste


    public static Servers getCurrentServer() {
        return currentServer;
    }

    public static void setCurrentServer(Servers server) {
        currentServer = server;
    }

    private void toggleOffensiveModules(boolean state) {
        ModuleManager moduleManager = Client.instance.moduleManager;
        moduleManager.getModule(KillAura.class).setState(state);
    }

    private void checkAndTogglePlayerTracker() {
        ModuleManager moduleManager = Client.instance.moduleManager;
        if (!moduleManager.getModule(PlayerTracker.class).getState()) {
            NotificationManager.post(NotificationType.WARNING, "Skywars Warning (Wait 15s)", "Please enable PlayerTracker.", 15.0f);
        } else if (this.toggleModule.getValue().booleanValue()) {
            this.toggleOffensiveModules(true);
        }
    }

    public void drop(int slot) {
        AutoPlay.mc.playerController.windowClick(AutoPlay.mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, AutoPlay.mc.thePlayer);
    }
}

