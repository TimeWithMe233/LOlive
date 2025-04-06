package dev.olive.module;

import dev.olive.utils.TimerUtil;
import lombok.Getter;
import lombok.Setter;
import dev.olive.Client;
import dev.olive.module.impl.combat.Gapple;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.ui.hud.notification.NotificationType;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    public TimerUtil enabledTime = new TimerUtil();
    public TimerUtil disabledTime = new TimerUtil();
    public static float allowedClickGuiHeight = 300;
    @Getter
    public float cGUIAnimation = 0f;
    @Getter
    private final Animation animation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);
    @Getter
    private final List<Value<?>> values = new ArrayList<>();
    /*
     * Information
     */
    @Getter
    public String name;
    @Getter
    public String cnname;
    @Getter
    public String suffix;
    @Getter
    public Category category;
    public boolean state = false;
    @Getter
    public boolean defaultOn = false;
    @Setter
    @Getter
    public int key = -1;
    @Setter
    @Getter
    public int mouseKey = -1;
    public boolean handleEvents() {
        return this.state;
    }

    /*
     * Values
     */
    @Setter
    @Getter
    public double progress;

    public Module(String name,String cnname, Category category) {
        this.name = name;
        this.cnname = cnname;

        this.category = category;
        this.suffix = "";
    }

    public void toggle() {
        this.setState(!this.state);
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public <M extends Module> boolean isEnabled(Class<M> module) {
        Module mod = Client.instance.getModuleManager().getModule(module);
        return mod != null && mod.state;
    }
    @Getter
    @Setter
    private boolean expanded;
    public static <T extends Module> T getModule(Class<T> clazz) {
        return Client.instance.moduleManager.getModule(clazz);
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        if (this.state == state) return;

        this.state = state;

        if (mc.theWorld != null)
            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.click", 0.5F, state ? 0.6F : 0.5F, false);


        if (state) {
            Client.instance.eventManager.register(this);
            NotificationManager.post(NotificationType.SUCCESS, "Module", this.name + " Enabled! ");
            onEnable();
        } else {
            Client.instance.eventManager.unregister(this);
            NotificationManager.post(NotificationType.DISABLE, "Module", this.name + " Disabled! ");


            onDisable();
        }
    }

    public void setStateSilent(boolean state) {
        if (this.state == state) return;

        this.state = state;

        if (mc.theWorld != null)
            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.click", 0.5F, state ? 0.6F : 0.5F, false);
        if (state) {
            Client.instance.eventManager.register(this);
        } else {
            Client.instance.eventManager.unregister(this);
        }
    }

    public boolean isGapple() {
        return Client.instance.getModuleManager().getModule(Gapple.class).state;
    }

    public boolean isFood() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood;
    }

    public boolean isBow() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow;
    }

    public static boolean isNull() {
        return mc.thePlayer == null || mc.theWorld == null;
    }

    public void setSuffix(Object obj) {
        String suffix = obj.toString();
        if (suffix.isEmpty()) {
            this.suffix = suffix;
        } else {
            this.suffix = String.format("§f%s§7", EnumChatFormatting.GRAY + suffix);
        }
    }

}
