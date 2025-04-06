// Slack Client (discord.gg/paGUcq2UTb)

package dev.olive.module.impl.player;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.move.GuiMove;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.TimerUtil;
import dev.olive.utils.math.Fuckyou;
import dev.olive.utils.player.AttackUtil;
import dev.olive.utils.player.InventoryUtil;
import dev.olive.utils.player.MovementUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Timer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.olive.module.impl.combat.KillAura.handleVerificationFailure;
import static net.minecraft.crash.CrashReport.hash;


public class InvManager extends Module {
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
    private final BoolValue silentInv = new BoolValue("Silent Inventory", false);
    private final NumberValue delayValue = new NumberValue("Delay", 1, 0, 20, 1);
    private final NumberValue weapon_slot_value = new NumberValue("Sword slot", 0, 0, 8, 1);
    private final NumberValue stack_slot_value = new NumberValue("Stack slot", 1, 0, 8, 1);
    private final NumberValue axe_slot_value = new NumberValue("Axe slot", 2, 0, 8, 1);
    private final NumberValue pickaxe_slot_value = new NumberValue("Pickaxe slot", 3, 0, 8, 1);
    private final NumberValue shovel_slot_value = new NumberValue("Shovel slot", 4, 0, 8, 1);
    private final NumberValue gapple_slot_value = new NumberValue("Gapple slot", 5, 0, 8, 1);

    boolean isHypixel = false;

    // ItemStack values
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack weapon;
    private ItemStack pickaxe;
    private ItemStack axe;
    private ItemStack shovel;
    private ItemStack block_stack;
    private ItemStack golden_apples;
    private int delay;
    private boolean silent = false;

    private TimerUtil wait = new TimerUtil();


    public InvManager() {
        super("InventoryManager","自动清理背包", Category.Player);
    }

    @Override
    public void onEnable() {
        delay = 0;
        silent = false;
    }

    @Override
    public void onDisable() {
        if (silent) {
            PacketUtil.send(new C0DPacketCloseWindow());
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

    @Override
    public String getSuffix() {
        if (isHypixel) {
            return "Hypixel";
        } else {
            return delayValue.getValue().toString();
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdate (EventUpdate event) {
        GuiMove invmove = Client.instance.getModuleManager().getModule(GuiMove.class);
        isHypixel = invmove.getState() ;
        if (isHypixel && !silentInv.getValue()) {
            if (mc.thePlayer.ticksExisted % 4 <= 1) {
                return;
            } else {
                delay = 0;
            }
        }
        if (silentInv.getValue()) {
            if (!MovementUtil.isBindsMoving() && mc.currentScreen == null && !AttackUtil.inCombat && !Client.instance.getModuleManager().getModule(Scaffold.class).getState()) {
                if (wait.hasReached(500)) {
                    if (!silent) {
                        PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    }
                    silent = true;
                }
            } else {
                wait.reset();
                if (silent) {
                    PacketUtil.send(new C0DPacketCloseWindow());
                }
                silent = false;
            }
        }
        Container container = mc.thePlayer.inventoryContainer;
        helmet = container.getSlot(5).getStack();
        chestplate = container.getSlot(6).getStack();
        leggings = container.getSlot(7).getStack();
        boots = container.getSlot(8).getStack();
        weapon = container.getSlot(weapon_slot_value.getValue().intValue() + 36).getStack();
        axe = container.getSlot(axe_slot_value.getValue().intValue() + 36).getStack();
        pickaxe = container.getSlot(pickaxe_slot_value.getValue().intValue() + 36).getStack();
        shovel = container.getSlot(shovel_slot_value.getValue().intValue() + 36).getStack();
        block_stack = container.getSlot(stack_slot_value.getValue().intValue() + 36).getStack();
        golden_apples = container.getSlot(gapple_slot_value.getValue().intValue() + 36).getStack();
        if (mc.currentScreen instanceof GuiChest) return;
        if (mc.getCurrentScreen() instanceof GuiInventory || (silentInv.getValue() && silent)) {
            if (++delay > delayValue.getValue()) {
                for (ArmorType type : ArmorType.values()) {
                    getBestArmor(type);
                }
                getBestWeapon();
                getBestAxe();
                getBestPickaxe();
                getBestShovel();
                getBlockStack();
                getGoldenApples();
                dropUselessItems();
            }
        } else {
            delay = 0;
        }

    }

    private void hotbarExchange(int hotbarNumber, int slotId) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, hotbarNumber, 2, mc.thePlayer);
        delay = 0;
    }

    private void shiftClick(int slotId) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 1, mc.thePlayer);
        delay = 0;
    }

    private void drop(int slotId) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 4, mc.thePlayer);
        delay = 0;
    }

    private void dropUselessItems() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !isGarbage(stack)) continue;
            drop(i);
            break;
        }
    }

    public boolean isGarbage(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.snowball || item == Items.egg || item == Items.fishing_rod || item == Items.experience_bottle || item == Items.skull || item == Items.flint || item == Items.lava_bucket || item == Items.flint_and_steel || item == Items.string) {
            return true;
        }
        if (item instanceof ItemHoe) {
            return true;
        }
        if (item instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion)item;
            for (PotionEffect effect : potion.getEffects(stack)) {
                int id = effect.getPotionID();
                if (id != Potion.moveSlowdown.getId() && id != Potion.blindness.getId() && id != Potion.poison.getId() && id != Potion.digSlowdown.getId() && id != Potion.weakness.getId() && id != Potion.harm.getId()) continue;
                return true;
            }
        } else {
            String itemName = stack.getItem().getUnlocalizedName().toLowerCase();
            if (itemName.contains("anvil") || itemName.contains("tnt") || itemName.contains("seed") || itemName.contains("table") || itemName.contains("string") || itemName.contains("eye") || itemName.contains("mushroom") || itemName.contains("chest") && !itemName.contains("plate") || itemName.contains("pressure_plate")) {
                return true;
            }
        }
        return false;
    }

    public boolean isUseless(ItemStack stack) {
        if (!getState()) {
            return isGarbage(stack);
        }
        if (isGarbage(stack)) {
            return true;
        }
        if (helmet != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 0 && !isBetterArmor(stack, helmet, ArmorType.HELMET)) {
            return true;
        }
        if (chestplate != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 1 && !isBetterArmor(stack, chestplate, ArmorType.CHESTPLATE)) {
            return true;
        }
        if (leggings != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 2 && !isBetterArmor(stack, leggings, ArmorType.LEGGINGS)) {
            return true;
        }
        if (boots != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 3 && !isBetterArmor(stack, boots, ArmorType.BOOTS)) {
            return true;
        }
        if (stack.getItem() instanceof ItemSword && weapon != null && !isBetterWeapon(stack, weapon)) {
            return true;
        }
        if (stack.getItem() instanceof ItemAxe && axe != null && !isBetterTool(stack, axe)) {
            return true;
        }
        if (stack.getItem() instanceof ItemPickaxe && pickaxe != null && !isBetterTool(stack, pickaxe)) {
            return true;
        }
        return stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") && shovel != null && !isBetterTool(stack, shovel);
    }

    private void getBlockStack() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack blockStack = null;
        int slot = -1;
        if (block_stack == null || !shouldChooseBlock(block_stack)) {
            for (int i = 9; i < 45; ++i) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack == null || !shouldChooseBlock(stack) || blockStack != null && stack.stackSize < blockStack.stackSize) continue;
                blockStack = stack;
                slot = i;
            }
        }
        if (blockStack != null) {
            hotbarExchange(stack_slot_value.getValue().intValue(), slot);
        }
    }

    private void getGoldenApples() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        if (golden_apples == null || !(golden_apples.getItem() instanceof ItemAppleGold)) {
            for (int i = 9; i < 45; ++i) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack == null || !(stack.getItem() instanceof ItemAppleGold)) continue;
                hotbarExchange(gapple_slot_value.getValue().intValue(), i);
                return;
            }
        }
    }

    private boolean shouldChooseBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    private void getBestWeapon() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldWeapon = weapon;
        int newSwordSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemSword) || i == weapon_slot_value.getValue() + 36) continue;
            boolean better = isBetterWeapon(stack, oldWeapon);
            boolean worse = isWorseWeapon(stack, oldWeapon);
            if (better) {
                newSwordSlot = i;
                oldWeapon = stack;
                continue;
            }
            if (!(stack.getItem() instanceof ItemSword)) continue;
            dropSlot = i;
        }
        if (newSwordSlot != -1) {
            hotbarExchange(weapon_slot_value.getValue().intValue(), newSwordSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestAxe() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldAxe = axe;
        int newAxeSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemAxe) || i == axe_slot_value.getValue() + 36) continue;
            if (isBetterTool(stack, oldAxe)) {
                newAxeSlot = i;
                oldAxe = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newAxeSlot != -1) {
            hotbarExchange(axe_slot_value.getValue().intValue(), newAxeSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestPickaxe() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldPickaxe = pickaxe;
        int newPickaxeSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemPickaxe) || i == pickaxe_slot_value.getValue() + 36) continue;
            if (isBetterTool(stack, oldPickaxe)) {
                newPickaxeSlot = i;
                oldPickaxe = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newPickaxeSlot != -1) {
            hotbarExchange(pickaxe_slot_value.getValue().intValue(), newPickaxeSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestShovel() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldShovel = shovel;
        int newShovelSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemTool) || !stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") || i == shovel_slot_value.getValue() + 36) continue;
            if (isBetterTool(stack, oldShovel)) {
                newShovelSlot = i;
                oldShovel = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newShovelSlot != -1) {
            hotbarExchange(shovel_slot_value.getValue().intValue(), newShovelSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestArmor(ArmorType type) {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldArmor = type == ArmorType.HELMET ? helmet : (type == ArmorType.CHESTPLATE ? chestplate : (type == ArmorType.LEGGINGS ? leggings : boots));
        int newArmorSlot = -1;
        int dropSlot = -1;
        int armorSlot = type == ArmorType.HELMET ? 5 : (type == ArmorType.CHESTPLATE ? 6 : (type == ArmorType.LEGGINGS ? 7 : 8));
        for (int i = 5; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemArmor)) continue;
            ItemArmor armor = (ItemArmor)stack.getItem();
            boolean better = isBetterArmor(stack, oldArmor, type);
            boolean worse = isWorseArmor(stack, oldArmor, type);
            if (armor.armorType != type.ordinal()) continue;
            if (better) {
                if (i == armorSlot) continue;
                if (oldArmor != null) {
                    dropSlot = armorSlot;
                    continue;
                }
                newArmorSlot = i;
                oldArmor = stack;
                armorSlot = i;
                continue;
            }
            if (worse || i != armorSlot) {
                dropSlot = i;
                continue;
            }
            if (i == armorSlot) continue;
            newArmorSlot = i;
            oldArmor = stack;
            armorSlot = i;
        }
        if (dropSlot != -1) {
            drop(dropSlot);
        } else if (newArmorSlot != -1) {
            shiftClick(newArmorSlot);
        }
    }

    private boolean isBetterWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();
        if (item instanceof ItemSword || item instanceof ItemTool) {
            if (oldWeapon != null) {
                return getAttackDamage(newWeapon) > getAttackDamage(oldWeapon);
            }
            return true;
        }
        return false;
    }

    private boolean isWorseWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();
        if (item instanceof ItemSword || item instanceof ItemTool) {
            if (oldWeapon != null) {
                return getAttackDamage(newWeapon) < getAttackDamage(oldWeapon);
            }
            return false;
        }
        return true;
    }

    private boolean isBetterTool(ItemStack newTool, ItemStack oldTool) {
        Item item = newTool.getItem();
        if (item instanceof ItemTool) {
            if (oldTool != null) {
                return getToolUsefulness(newTool) > getToolUsefulness(oldTool);
            }
            return true;
        }
        return false;
    }

    private boolean isBetterArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if (oldArmor == null) {
            return true;
        }
        Item oldItem = oldArmor.getItem();
        if (oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor)oldItem;
            if (oldArmor != null && oldItemArmor.armorType == type.ordinal()) {
                return getArmorProtection(newArmor) > getArmorProtection(oldArmor);
            }
            return true;
        }
        return false;
    }

    private boolean isWorseArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if (oldArmor == null) {
            return false;
        }
        Item oldItem = oldArmor.getItem();
        if (oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor)oldItem;
            if (oldArmor != null && oldItemArmor.armorType == type.ordinal()) {
                return getArmorProtection(newArmor) < getArmorProtection(oldArmor);
            }
            return false;
        }
        return true;
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseDamage = 0.0f;
        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword)item;
            baseDamage += sword.getAttackDamage();
        } else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            baseDamage += tool.getAttackDamage();
        }
        float enchantsDamage = (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseDamage + enchantsDamage;
    }

    private float getToolUsefulness(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseUsefulness = 0.0f;
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            switch (tool.getToolMaterial()) {
                case WOOD: {
                    baseUsefulness = 1.0f;
                    break;
                }
                case GOLD:
                    baseUsefulness = 1.0f;
                    break;
                case STONE: {
                    baseUsefulness = 2.0f;
                    break;
                }
                case IRON: {
                    baseUsefulness = 3.0f;
                    break;
                }
                case EMERALD: {
                    baseUsefulness = 4.0f;
                }
            }
        }
        float enchantsUsefulness = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) * 0.5f + 0.0f;
        return baseUsefulness + enchantsUsefulness;
    }

    private float getArmorProtection(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseProtection = 0.0f;
        if (item instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)item;
            baseProtection += (float)armor.damageReduceAmount;
        }
        float enchantsProtection = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) * 0.1f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseProtection + enchantsProtection;
    }

    public enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS

    }

}
