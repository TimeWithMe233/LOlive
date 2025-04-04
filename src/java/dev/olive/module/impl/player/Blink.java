package dev.olive.module.impl.player;

import com.mojang.authlib.GameProfile;
import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventRender2D;
import dev.olive.event.impl.events.EventRender3D;
import dev.olive.event.impl.events.EventTick;
import dev.olive.event.impl.events.EventUpdate;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.render.HUD;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.ui.font.FontManager;
import dev.olive.utils.BlinkUtils;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.TimerUtil;
import dev.olive.utils.render.RenderUtil;
import dev.olive.utils.render.RoundedUtil;
import dev.olive.utils.render.animation.Animation;
import dev.olive.utils.render.animation.Direction;
import dev.olive.utils.render.animation.impl.DecelerateAnimation;
import dev.olive.utils.render.shader.ShaderElement;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import lombok.Getter;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.LinkedList;
import java.util.UUID;

public class Blink
        extends Module {
    @Getter
    private static EntityOtherPlayerMP fakePlayer;
    public static final ModeValue mode = new ModeValue("Mode",new String[]{"Olive","Naven"},"Olive");
    private final BoolValue slowRelease = new BoolValue("SlowRelease", false);
    private final NumberValue releaseDelay = new NumberValue("ReleaseDelay", 1000, 10000, 300, 10);
    private final TimerUtil timer = new TimerUtil();
    private final LinkedList<double[]> positions = new LinkedList();
    public static final int color = new Color(255, 255, 255, 200).getRGB();
    final Animation anim = new DecelerateAnimation(250, 1);
    private Vec3 pos;

    public Blink() {
        super("Blink", "闪现",Category.Player);
    }

    @Override
    public void onEnable() {
        if (Blink.mc.thePlayer == null) {
            return;
        }
        LinkedList<double[]> linkedList = this.positions;
        synchronized (linkedList) {
            this.positions.add(new double[]{Blink.mc.thePlayer.posX, Blink.mc.thePlayer.getEntityBoundingBox().minY + (double) (Blink.mc.thePlayer.getEyeHeight() / 2.0f), Blink.mc.thePlayer.posZ});
            this.positions.add(new double[]{Blink.mc.thePlayer.posX, Blink.mc.thePlayer.getEntityBoundingBox().minY, Blink.mc.thePlayer.posZ});
        }
        timer.reset();
        BlinkUtils.startBlink();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        fakePlayer = new EntityOtherPlayerMP(mc.theWorld, new GameProfile(new UUID(69L, 96L), "[Blink]" + mc.thePlayer.getName()));
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
        mc.theWorld.addEntityToWorld(-1337, fakePlayer);
    }

    private void handleFakePlayerPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition position = (C03PacketPlayer.C04PacketPlayerPosition) packet;
            this.fakePlayer.setPositionAndRotation2(position.x, position.y, position.z, this.fakePlayer.rotationYaw, this.fakePlayer.rotationPitch, 3, true);
            this.fakePlayer.onGround = position.isOnGround();
        } else if (packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
            C03PacketPlayer.C05PacketPlayerLook rotation = (C03PacketPlayer.C05PacketPlayerLook) packet;
            this.fakePlayer.setPositionAndRotation2(this.fakePlayer.posX, this.fakePlayer.posY, this.fakePlayer.posZ, rotation.getYaw(), rotation.getPitch(), 3, true);
            this.fakePlayer.onGround = rotation.isOnGround();
            this.fakePlayer.rotationYawHead = rotation.getYaw();
            this.fakePlayer.rotationYaw = rotation.getYaw();
            this.fakePlayer.rotationPitch = rotation.getPitch();
        } else if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook positionRotation = (C03PacketPlayer.C06PacketPlayerPosLook) packet;
            this.fakePlayer.setPositionAndRotation2(positionRotation.x, positionRotation.y, positionRotation.z, positionRotation.getYaw(), positionRotation.getPitch(), 3, true);
            this.fakePlayer.onGround = positionRotation.isOnGround();
            this.fakePlayer.rotationYawHead = positionRotation.getYaw();
            this.fakePlayer.rotationYaw = positionRotation.getYaw();
            this.fakePlayer.rotationPitch = positionRotation.getPitch();
        } else if (packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction action = (C0BPacketEntityAction) packet;
            if (action.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                this.fakePlayer.setSprinting(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                this.fakePlayer.setSprinting(false);
            } else if (action.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
                this.fakePlayer.setSneaking(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                this.fakePlayer.setSneaking(false);
            }
        } else if (packet instanceof C0APacketAnimation) {
            C0APacketAnimation animation = (C0APacketAnimation) packet;
            this.fakePlayer.swingItem();
        }
    }

    @Override
    public void onDisable() {
        BlinkUtils.stopBlink();
        timer.reset();
        LinkedList<double[]> linkedList = this.positions;
        synchronized (linkedList) {
            this.positions.clear();
        }
        if (fakePlayer != null) {
            mc.theWorld.removeEntityFromWorld(fakePlayer.getEntityId());
            fakePlayer = null;
        }
    }

    @EventTarget
    private void onTick(EventTick event) {

        if (slowRelease.get() && BlinkUtils.isBlinking() && timer.hasReached(releaseDelay.getValue().longValue())) {
            BlinkUtils.releaseC03render(1);
        }
    }
    @EventTarget
    public void onUpdateEvent(EventUpdate event) {
        LinkedList<double[]> linkedList = this.positions;
        synchronized (linkedList) {
            this.positions.add(new double[]{Blink.mc.thePlayer.posX, Blink.mc.thePlayer.getEntityBoundingBox().minY, Blink.mc.thePlayer.posZ});
        }
    }
    @EventTarget
    private void onRender(EventRender3D event) {
        drawBox(pos);
        LinkedList<double[]> linkedList = this.positions;
        synchronized (linkedList) {
            GL11.glPushMatrix();
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GL11.glDisable(2929);
            Blink.mc.entityRenderer.disableLightmap();
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            RenderUtil.glColor(new Color(68, 131, 123, 255).getRGB());
            double renderPosX = Blink.mc.getRenderManager().viewerPosX;
            double renderPosY = Blink.mc.getRenderManager().viewerPosY;
            double renderPosZ = Blink.mc.getRenderManager().viewerPosZ;
            for (double[] pos : this.positions) {
                GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);
            }
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
            GL11.glEnd();
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glPopMatrix();
        }
    }


    public void renderProgessBar() {
        int maxPacketNumber = 500;
        int packetNumber = BlinkUtils.packets.size();
        if (packetNumber >= maxPacketNumber) {
            for (Packet<?> packet : BlinkUtils.packets) {
                PacketUtil.sendPacketNoEvent(packet);
            }
            setState(false);
        }
        Scaffold scaffold = Client.instance.moduleManager.getModule(Scaffold.class);

        if (scaffold.state) {

        } else {
            anim.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!state && anim.isDone()) return;
            int spacing = 3;
            String text = "SendC03(s): " + packetNumber + "/33";
            float textWidth = FontManager.interSemiBold18.getStringWidth(text);
            float x, y;
            ScaledResolution sr = new ScaledResolution(mc);
            float output = (float) anim.getOutput();
            float totalWidth = ((textWidth + spacing) + 6) * output;
            float target = (textWidth) * Math.min(Math.max((packetNumber / 100f), 0F), 1f);
            x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
            y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
            RenderUtil.scissorStart(x - 1.5f, y - 1.5f, totalWidth + 3, 46);
            ShaderElement.addBlurTask(() -> {
                RoundedUtil.drawRound(x, y, totalWidth * output, 43, 4, new Color(255, 255, 255, 255));
            });

            RoundedUtil.drawRound(x, y, totalWidth * output, 43, 4, new Color(0, 0, 0, 110));
            RoundedUtil.drawRound((x + 3) * output, y + 25, textWidth * output, 5, 2, new Color(166, 164, 164, 81));
            RoundedUtil.drawGradientHorizontal((x + 3), y + 25, Math.min(target, 120) * output, 5, 2, HUD.color(1), HUD.color(6));
            FontManager.interSemiBold18.drawString("SendPacket(s): " + packetNumber, (x + 3), y + 9.5f, -1);
            RenderUtil.scissorEnd();
        }
}
    public static void drawBox(@NotNull Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderUtil.drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }

  public void renderProgessBar2() {
        int maxPacketNumber = 500;
        int packetNumber = BlinkUtils.packets.size();
        if (packetNumber >= maxPacketNumber) {
            for (Packet<?> packet : BlinkUtils.packets) {
                PacketUtil.sendPacketNoEvent(packet);
            }
            setState(false);
        }
        Scaffold scaffold = Client.instance.moduleManager.getModule(Scaffold.class);

        if (scaffold.state) {

        } else {
            anim.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!state && anim.isDone()) return;
            int spacing = 3;
            String text = "SendC03(s): " + packetNumber + "/33";
            float textWidth = FontManager.interSemiBold18.getStringWidth(text)+10;
            float x, y;
            ScaledResolution sr = new ScaledResolution(mc);
            float output = (float) anim.getOutput();
            float totalWidth = ((textWidth + spacing) + 6) * output;
            float target = (textWidth) * Math.min(Math.max((packetNumber / 150f), 0F), 1f);
            x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
            y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
            RenderUtil.scissorStart(x - 1.5f, y - 1.5f, totalWidth + 3, 46);
            RoundedUtil.drawRound((x + 3) , y + 10, textWidth * output, 3, 2, new Color(12, 12, 12, 132));
            RoundedUtil.drawRound((x + 3), y + 10, Math.min(target, 120) * output, 3, 2, new Color(200, 58, 58, 215));
            ShaderElement.addBlurTask(()->RoundedUtil.drawRound((x + 3) , y + 10, textWidth * output, 3, 2, Color.WHITE));
            ShaderElement.addBloomTask(()->RoundedUtil.drawRound((x + 3) , y + 10, textWidth * output, 3, 2, Color.BLACK));
            RenderUtil.scissorEnd();
        }
}
    @Override
    public String getSuffix() {
        return "Grim";
    }
}

