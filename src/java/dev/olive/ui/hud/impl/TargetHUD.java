package dev.olive.ui.hud.impl;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventMotion;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.ui.font.FontManager;
import dev.olive.ui.hud.HUD;
import dev.olive.utils.StopWatch;
import dev.olive.utils.TimerUtil;

import dev.olive.utils.math.MathUtils;
import dev.olive.utils.render.*;

import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.ContinualAnimation;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.utils.render.shader.KawaseBloom;
import dev.olive.utils.render.shader.KawaseBlur;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.olive.module.impl.render.HUD.color;

public class TargetHUD extends HUD {
    public TargetHUD() {
        super(150, 100, "TargetHUD","攻击面板");
    }

    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
    private boolean sentParticles;

    public final List<Particle> particles = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();
    private final DecimalFormat DF_2 = new DecimalFormat("0");
    public static boolean inWorld;
    private BoolValue outline = new BoolValue("OutLine",true);
    private final ContinualAnimation animation2 = new ContinualAnimation();
    private final Animation openAnimation = new DecelerateAnimation(175, .5);
    private final DecimalFormat DF_1 = new DecimalFormat("0.0");
    public StopWatch stopwatch = new StopWatch();
    private EntityLivingBase target;
    private KillAura killAura;
    @Override
    public void drawShader() {

    }


    @Override
    public void onTick() {

    }
    @EventTarget()
    public void onPreMotionEvent(EventMotion event ) {
        if (event.isPre()) {
//        target = mc.thePlayer;

            if (mc.currentScreen instanceof GuiChat) {
                stopwatch.reset();
                target = mc.thePlayer;
            }

            if (target == null) {
                inWorld = false;
                return;
            }
            inWorld = mc.theWorld.loadedEntityList.contains(target);
        }
        ;
    }
    @Override
    public void drawHUD(int xPos, int yPos, float partialTicks) {
        GlStateManager.pushMatrix();

        float alpha = (float) Math.min(1, openAnimation.getOutput() * 2);
        if (target != null) {
            render(xPos, yPos, alpha, target, false);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void predrawhud() {

        if (killAura == null) {
            killAura = Client.instance.moduleManager.getModule(KillAura.class);
        }
        if (!(mc.currentScreen instanceof GuiChat)) {
            if (!killAura.getState()) {
                openAnimation.setDirection(Direction.BACKWARDS);
            }

            if (target == null && KillAura.target != null) {
                target = KillAura.target;
                openAnimation.setDirection(Direction.FORWARDS);


            } else if (KillAura.target == null || target != KillAura.target) {
                openAnimation.setDirection(Direction.BACKWARDS);
            }

            if (openAnimation.finished(Direction.BACKWARDS)) {
                target = null;
            }
        } else {
            openAnimation.setDirection(Direction.FORWARDS);
            target = mc.thePlayer;
        }

    }


    public void render(float x, float y, float alpha, EntityLivingBase target, boolean blur) {

        GlStateManager.pushMatrix();

        int textColor = ColorUtil.applyOpacity(-1, alpha);
        float hurtPercent = target.hurtTime / 10F;
        float scale;
        if (hurtPercent == 0f) {
            scale = 1f;
        } else if (hurtPercent < 0.5f) {
            scale = 1 - (0.1f * hurtPercent * 2);
        } else {
            scale = 0.9f + (0.1f * (hurtPercent - 0.5f) * 2);
        }

        DecimalFormat DF1 = new DecimalFormat("0.0");
        this.setWidth((int) Math.max(90.0f, 150.0f));
        this.setHeight((int) 35.0F);
        RoundedUtil.drawRound(x, y, (float) this.getWidth(), (float) this.getHeight() + 2, 8.0f, new Color(0, 0, 0, 85));

        float hurt_time1 = Math.max(7, target.hurtTime);
        target.animatedHealthBar = (float) AnimationUtils.animate(target.animatedHealthBar, target.getHealth(), 0.2f);
        GradientUtil.applyGradientHorizontal((float) x + 37.0f + FontManager.font20.getStringWidth("Name: "), (float) y + 11.0f, (float) FontManager.font20.getStringWidth(target.getDisplayName().getFormattedText()), FontManager.font18.getHeight(), 1.0F, dev.olive.module.impl.render.HUD.color(1), dev.olive.module.impl.render.HUD.color(6), () -> {
            RenderUtil.setAlphaLimit(0);
            FontManager.font18.drawString(target.getDisplayName().getFormattedText(), x + 37.0f + FontManager.font20.getStringWidth("Name: "), y + 11.0f, -1);
        });
        FontManager.font20.drawString("Name: ", x + 37.0f, y + 9.0f, Color.WHITE.getRGB());
        GradientUtil.applyGradientHorizontal((float) ((double) x + this.getWidth() - 19.0), y + 22.0f, (float) FontManager.font20.getStringWidth(DF1.format(target.animatedHealthBar)), FontManager.font18.getHeight(), 1.0F, dev.olive.module.impl.render.HUD.color(1), dev.olive.module.impl.render.HUD.color(6), () -> {
            RenderUtil.setAlphaLimit(0);
            FontManager.interSemiBold18.drawString(DF1.format(target.animatedHealthBar), (float) ((double) x + this.getWidth() - 19.0), y + 22.0f, -1);
        });

        RoundedUtil.drawRound(x + 38.0f, y + 24.0f, (float) (this.getWidth() - 64), 5.0f, 2.0f, new Color(0, 0, 0, 79));
        RoundedUtil.drawGradientHorizontal(x + 38.0f, y + 24.0f, (float) ((double) (target.animatedHealthBar / target.getMaxHealth()) * (this.getWidth() - 64)), 5.0f, 2.0f, dev.olive.module.impl.render.HUD.color(1), dev.olive.module.impl.render.HUD.color(6));

        //background glow
        stencilFramebuffer = ShaderElement.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        RoundedUtil.drawGradientHorizontal(x + 38.0f, y + 24.0f, (float) ((double) (target.animatedHealthBar / target.getMaxHealth()) * (this.getWidth() - 64)), 5.0f, 3.0f, dev.olive.module.impl.render.HUD.color(1), dev.olive.module.impl.render.HUD.color(6));
        stencilFramebuffer.bindFramebuffer(false);
        for (Particle p : particles) {
            //If tracking then the x value changes so we want to make it track the target aswell
            p.x = x + 20;
            p.y = y + 20;
            GlStateManager.color(1, 1, 1, 1);
            if (p.opacity > 4) p.render2D();
        }
        if (target instanceof AbstractClientPlayer) {
            textColor = ((EntityPlayer) target).isBlocking() ? Color.RED.getRGB() : ColorUtil.applyOpacity(-1, alpha);
            RenderUtil.color(-1, alpha);
            RenderUtil.scaleStart(x + 3, y + 3, scale);
            RenderUtil.renderPlayer2D((AbstractClientPlayer) target, x + 3, y + 3, 31, 31, textColor);
            RenderUtil.scaleEnd();
            StencilUtil.dispose();
        } else {
            if (!(openAnimation.getDirection() == Direction.BACKWARDS)) {
                FontManager.bold32.drawCenteredStringWithShadow("?", x + 19, y + 20 - FontManager.bold32.getHeight() / 2f, textColor);
            }
        }
        if (timer.hasTimeElapsed(1000 / 60, true)) {
            for (Particle p : particles) {
                p.updatePosition();
                if (p.opacity < 1) particles.remove(p);
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 15; i++) {
                Particle particle = new Particle();
                particle.init(x + 3, y + 3, (float) (((Math.random() - 0.5) * 2) * 1.4), (float) (((Math.random() - 0.5) * 2) * 1.4),
                        (float) (Math.random() * 4), i % 2 == 0 ? dev.olive.module.impl.render.HUD.color(1) : dev.olive.module.impl.render.HUD.color(6));
                particles.add(particle);
            }
            sentParticles = true;
        }
        if (target.hurtTime == 8) sentParticles = false;


        if (hurt_time1 != 7.0f)
            hurt_time1 = 0.0f;

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static class Particle {
        public float x, y, adjustedX, adjustedY, deltaX, deltaY, size, opacity;
        public Color color;

        public void render2D() {
            RoundedUtil.drawRound(x + adjustedX, y + adjustedY, size, size, (size / 2f) - .5f, ColorUtil.applyOpacity(color, opacity / 255f));
        }

        public void updatePosition() {
            for (int i = 1; i <= 2; i++) {
                adjustedX += deltaX;
                adjustedY += deltaY;
                deltaY *= 0.97;
                deltaX *= 0.97;
                opacity -= 1f;
                if (opacity < 1) opacity = 1;
            }
        }

        public void init(float x, float y, float deltaX, float deltaY, float size, Color color) {
            this.x = x;
            this.y = y;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.size = size;
            this.opacity = 254;
            this.color = color;
        }
    }

    public static void drawEquippedShit(final int x, final int y, final EntityLivingBase target) {
        if (!(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final ArrayList<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        for (int geraltOfNigeria = 3; geraltOfNigeria >= 0; --geraltOfNigeria) {
            final ItemStack armor = target.getCurrentArmor(geraltOfNigeria);
            if (armor != null) {
                stuff.add(armor);
            }
        }
        if (target.getHeldItem() != null) {
            stuff.add(target.getHeldItem());
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, yes, cock + x, y);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            yes.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
    }

    protected void renderPlayer2D(float x, float y, float width, float height, AbstractClientPlayer player) {
        GLUtil.startBlend();
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect(x, y, (float) 8.0, (float) 8.0, 8, 8, width, height, 64.0F, 64.0F);
        GLUtil.endBlend();
    }

    public void drawModel(final float yaw, final float pitch, final EntityLivingBase entityLivingBase) {
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = entityLivingBase.renderYawOffset;
        final float rotationYaw = entityLivingBase.rotationYaw;
        final float rotationPitch = entityLivingBase.rotationPitch;
        final float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        final float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - 0.4f;
        entityLivingBase.rotationYaw = yaw - 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }

    // 构建颜色代码到颜色名称的映射
    private static final Map<Character, String> COLOR_CODE_MAP = new HashMap<>();

    public static String detectColorInitials(String text) {
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '§') {
                char code = text.charAt(i + 1);
                String colorName = COLOR_CODE_MAP.get(code);
                if (colorName != null) {
                    // 提取颜色名称首字母并转为大写添加到结果中
                    initials.append(Character.toUpperCase(colorName.charAt(0)));
                }
            }
        }
        return initials.toString();

    }


    // 颜色代码映射表，将颜色代码字符映射到对应的颜色名称

    static {
        COLOR_CODE_MAP.put('0', "黑色");
        COLOR_CODE_MAP.put('1', "蓝色");
        COLOR_CODE_MAP.put('2', "深绿色");
        COLOR_CODE_MAP.put('3', "湖蓝色");
        COLOR_CODE_MAP.put('4', "深红色");
        COLOR_CODE_MAP.put('5', "紫色");
        COLOR_CODE_MAP.put('6', "金色");
        COLOR_CODE_MAP.put('7', "灰色");
        COLOR_CODE_MAP.put('8', "深灰色");
        COLOR_CODE_MAP.put('9', "蓝色");
        COLOR_CODE_MAP.put('a', "绿色");
        COLOR_CODE_MAP.put('b', "天蓝色");
        COLOR_CODE_MAP.put('c', "红色");
        COLOR_CODE_MAP.put('d', "粉红色");
        COLOR_CODE_MAP.put('e', "黄色");
        COLOR_CODE_MAP.put('f', "白色");
    }

    private double calculateBPS() {
        double bps = (Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20.0;
        return Math.round(bps * 100.0) / 10.0;
    }
}
