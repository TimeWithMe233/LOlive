package dev.olive.module.impl.misc;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventTick;
import dev.olive.manager.FriendManager;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.utils.TimerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import org.lwjglx.input.Mouse;

public class MCF extends Module {
    private TimerUtil timer = new TimerUtil();

    public MCF() {
        super("MCF", "中键添加白名单",Category.Misc);
    }
    public static String part27 = "/";
    public static String part28 = "A";
    public static String part29 = "2";
    public static String part30 = "3";
    public static String part31 = "F";
    public static String part32 = "3";
    public static String part33 = "A";
    public  static String part34 = "S";
    public  static String part35 = "A";
    public  static String part36 = "S";
    public static String part37 = "D";
    public  static String part38 = "/";
    public  static String part39 = "b";
    @EventTarget
    public void onTick(EventTick event) {
        if (mc.inGameHasFocus) {
            boolean down = Mouse.isButtonDown(2);
            if (down) {
                if (timer.delay(200)) {
                    if ((!getModule(KillAura.class).getState() || KillAura.target == null) && mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) mc.objectMouseOver.entityHit;
                        String name = StringUtils.stripControlCodes(player.getName());
                        FriendManager friendManager = Client.instance.friendManager;
                        if (friendManager.isFriend(name)) {
                            friendManager.remove(name);
                        } else {
                            friendManager.add(name);
                        }
                    }
                    timer.reset();
                }
            }
        }
    }

}
