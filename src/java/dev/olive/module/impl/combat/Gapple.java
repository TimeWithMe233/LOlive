package dev.olive.module.impl.combat;


import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.render.HUD;
import dev.olive.ui.font.FontManager;
import dev.olive.utils.PacketUtil;
import dev.olive.utils.StopWatch;
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
import java.util.concurrent.LinkedBlockingQueue;

public class Gapple
        extends Module {
    public static final ModeValue mode = new ModeValue("Mode", new String[]{"Naven", "Olive"}, "Naven" );
    public static int eattick;
    public static boolean isS12;
    private final LinkedBlockingQueue<Packet<?>> packets;
    final Animation anim = new DecelerateAnimation(250, 1);
    public static int i;



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