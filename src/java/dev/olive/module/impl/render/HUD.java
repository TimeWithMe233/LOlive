package dev.olive.module.impl.render;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventPreRender;
import dev.olive.event.impl.events.EventRender2D;
import dev.olive.event.impl.events.EventTick;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.Gapple;
import dev.olive.module.impl.player.BalanceTimer;
import dev.olive.module.impl.player.Blink;
import dev.olive.module.impl.world.BWScaffold;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.ui.font.RapeMasterFontManager;

import dev.olive.ui.hud.notification.Notification;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.utils.render.ColorUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.shader.KawaseBloom;
import dev.olive.utils.render.shader.KawaseBlur;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

import static dev.olive.ui.font.FontManager.font20;
import static dev.olive.ui.font.FontManager.interSemiBold18;
import static dev.olive.utils.render.shader.ShaderElement.createFrameBuffer;
import static net.minecraft.client.gui.GuiChat.openingAnimation;

public class HUD extends Module {
    public HUD() {
        super("HUD","视觉面板", Category.Render);
    }

    public static ModeValue colorMode = new ModeValue("Color Mode", new String[]{"Fade", "Static", "Double", "RainBow"}, "Fade");
    public static ModeValue hotbarMode = new ModeValue("Hotbar Mode", new String[]{"Normal", "New"}, "Normal");
    public static ModeValue statsMode = new ModeValue("Stats Mode", new String[]{"Normal", "New"}, "Normal");
    public static ColorValue mainColor = new ColorValue("Main Color", new Color(255, 175, 63).getRGB());
    public static ColorValue secondColor = new ColorValue("Second Color", new Color(255, 175, 63).getRGB(), () -> colorMode.is("Double"));
    public ModeValue notiMode = new ModeValue("Notification Mode", new String[]{"Olive","Naven"}, "Olive");
    public static TextValue markTextValue = new TextValue("Text", "new SilenceFix()");

    public int offsetValue = 0;
    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);
    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
    private final RapeMasterFontManager productSansRegular = interSemiBold18;
    private Scaffold scaffold;
    private BWScaffold bwScaffold;
    private Gapple gapple;
    private Blink blink;
    private ProgessBar progessBar;
    private BalanceTimer balanceTimer;
    private String username, fps;
    private float userWidth, fpsWidth, bpsWidth;

    @Override
    public void onEnable() {
        scaffold = getModule(Scaffold.class);
        bwScaffold = getModule(BWScaffold.class);
        progessBar = getModule(ProgessBar.class);
        balanceTimer = getModule(BalanceTimer.class);
        blink = getModule(Blink.class);
        gapple = getModule(Gapple.class);
    }

    public static Color color(int tick) {
        Color textColor = new Color(-1);
        switch (colorMode.get()) {
            case "Fade":
                textColor = ColorUtil.fade(5, tick * 20, new Color(mainColor.getColor()), 1);
                break;
            case "Static":
                textColor = mainColor.getColorC();
                break;
            case "Double":
                tick *= 200;
                textColor = new Color(RenderUtil.colorSwitch(mainColor.getColorC(), secondColor.getColorC(), 5000, -tick / 40, 75, 2));
                break;
            case "RainBow":
                tick *= 4;
                textColor = new Color(Color.HSBtoRGB((float) ((double) mc.thePlayer.ticksExisted / 50.0 + Math.sin((double) tick / 50.0 * 1.6)) % 1.0F, 0.6F, 1.0F));
                break;
        }

        return textColor;
    }


    public void drawBlur() {
        stencilFramebuffer = createFrameBuffer(stencilFramebuffer);

        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);

        for (Runnable runnable : ShaderElement.getTasks()) {
            runnable.run();
        }
        ShaderElement.getTasks().clear();

        stencilFramebuffer.unbindFramebuffer();


    }

    public void drawBloom() {

        bloomFramebuffer = createFrameBuffer(bloomFramebuffer);
        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(false);

        for (Runnable runnable : ShaderElement.getBloomTasks()) {
            runnable.run();
        }
        ShaderElement.getBloomTasks().clear();

        bloomFramebuffer.unbindFramebuffer();


    }


    public void drawNotifications() {
        ScaledResolution sr = new ScaledResolution(mc);
        float yOffset = 0;
        int notificationHeight = 0, notificationWidth = 0, actualOffset;

        NotificationManager.setToggleTime(2f);

        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }

            float x, y;
            animation.setDuration(200);
            actualOffset = 3;
            switch (notiMode.getValue()) {
                case "Olive":
                    notificationHeight = 23;
                    notificationWidth = font20.getStringWidth(notification.getDescription()) + 25;
                    break;
                case "Naven":
                    notificationHeight = 21;
                    notificationWidth = font20.getStringWidth(notification.getDescription()) + 8;
                    break;

            }

            x = (float) (sr.getScaledWidth() - (notificationWidth) * animation.getOutput());
            y = sr.getScaledHeight() - (yOffset + 18 + offsetValue + notificationHeight + 15);
            switch (notiMode.getValue()) {
                case "Olive":
                    notification.drawOlive(x, y, notificationWidth, notificationHeight);
                    break;

                case "Naven":
                    notification.drawNaven(x, y+30, notificationWidth, notificationHeight);
                    break;

            }
            yOffset += (float) ((notificationHeight + actualOffset) * animation.getOutput());
        }
    }

    @EventTarget
    public void onPreRender(EventPreRender e) {
        for (dev.olive.ui.hud.HUD hud : Client.instance.hudManager.hudObjects.values()) {
            if (hud.m.getState())
                hud.predraw();
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        for (dev.olive.ui.hud.HUD hud : Client.instance.hudManager.hudObjects.values()) {
            if (hud.m.getState())
                hud.onTick();
        }
        username = mc.getSession() == null || mc.getSession().getUsername() == null ? "null" : mc.getSession().getUsername();
        userWidth = this.productSansRegular.getStringWidth("Ehereal - " + Client.instance.user + " - " + "Dev") + 2;
        fps = String.valueOf(Minecraft.getDebugFPS());
        fpsWidth = this.productSansRegular.getStringWidth("FPS:") + 2;
        bpsWidth = this.productSansRegular.getStringWidth("BPS:") + 2;

    }

    @EventTarget
    public void onRender2D(EventRender2D e) {

        for (dev.olive.ui.hud.HUD hud : Client.instance.hudManager.hudObjects.values()) {
            if (hud.m.getState())
                hud.draw(e.getPartialTicks());
        }
        drawNotifications();
        if (mc.thePlayer != null && mc.theWorld != null) {
            scaffold.renderCounter();
        }
        if (mc.thePlayer != null && mc.theWorld != null) {
            bwScaffold.renderCounter();
        }
        if (mc.thePlayer != null && mc.theWorld != null) {
            if (blink.mode.is("Olive")) {
                blink.renderProgessBar();
            } else if (blink.mode.is("Naven")) {
                blink.renderProgessBar2();
            }
        }
        if (mc.thePlayer != null && mc.theWorld != null) {
            Gapple var8 = this.gapple;
            if (Gapple.mode.is("Olive")) {
                this.gapple.renderProgessBar2();
            } else {
                var8 = this.gapple;
                if (Gapple.mode.is("Naven")) {
                    this.gapple.renderProgessBar3();
                }
            }
        }
        if (mc.thePlayer != null && mc.theWorld != null) {
            progessBar.renderProgessBar3();
        }
        if (mc.thePlayer != null && mc.theWorld != null) {
            balanceTimer.renderProgessBar4();
        }
        // information of user in the bottom right corner of the screen
        float x = e.getScaledResolution().getScaledWidth();
        float y = e.getScaledResolution().getScaledHeight() - this.productSansRegular.getHeight() - 1;

        final float coordX = 5;
        this.productSansRegular.drawStringWithShadow("FPS:", coordX, (float) (y - (mc.currentScreen instanceof GuiChat ? openingAnimation.getOutput() * 13 : 0)), 0xFFCCCCCC);
        this.productSansRegular.drawStringWithShadow("BPS:", coordX, (float) (y - (mc.currentScreen instanceof GuiChat ? openingAnimation.getOutput() * 13 : 0) - 13), 0xFFCCCCCC);
        this.productSansRegular.drawStringDynamic(fps, coordX + fpsWidth, (float) (y - (mc.currentScreen instanceof GuiChat ? openingAnimation.getOutput() * 13 : 0)), 1, 6);
        this.productSansRegular.drawStringWithShadow(String.valueOf(calculateBPS()), coordX + bpsWidth, (float) (y - (mc.currentScreen instanceof GuiChat ? openingAnimation.getOutput() * 13 : 0) - 13), 0xFFCCCCCC);
    }

    public static class CounterBar {


    }

    private double calculateBPS() {
        double bps = (Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }

}
