
package dev.olive.module.impl.move;

import dev.olive.event.annotations.EventPriority;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.*;
import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.module.impl.combat.KillAura;
import dev.olive.module.impl.world.Scaffold;
import dev.olive.utils.player.MovementUtils;
import dev.olive.utils.player.PlayerUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;


public class TargetStrafe extends Module {

    public final NumberValue range = new NumberValue("Range", 1, 0.1f, 6, 0.1f);
    public final BoolValue holdJump = new BoolValue("Hold Jump", false);
    public final BoolValue render = new BoolValue("Render", true);
    public final BoolValue behind = new BoolValue("Behind", true);
    public float yaw;
    private boolean left, colliding;
    public boolean active;
    public EntityLivingBase target;

    public TargetStrafe() {
        super("TargetStrafe","转圈圈", Category.Movement);
    }


    @EventTarget
    @EventPriority(3)
    public void onJump(EventJump event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }

    @EventTarget
    @EventPriority(3)
    public void onStrafe(EventStrafe event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }

    @EventTarget
    @EventPriority(3)
    public void onUpdate(EventUpdate event) {

        Speed speed = getModule(Speed.class);

        if (holdJump.get() && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) || !(mc.gameSettings.keyBindForward.isKeyDown() && (speed != null && speed.state)) || !isEnabled(KillAura.class) || isEnabled(Scaffold.class)) {
            active = false;
            target = null;
            return;
        }

        getModule(KillAura.class);
        target = KillAura.target;

        if (target == null) {
            active = false;
            return;
        }

        if(!speed.couldStrafe)
            return;

        if (mc.thePlayer.isCollidedHorizontally || !PlayerUtil.isBlockUnder(5, false)) {
            if (!colliding) {
                MovementUtils.strafe();
                left = !left;
            }
            colliding = true;
        } else {
            colliding = false;
        }

        active = true;


        float yaw;

        if (behind.get()) {
            yaw = target.rotationYaw + 180;
        } else {
            yaw = getYaw(mc.thePlayer, new Vec3(target.posX, target.posY, target.posZ)) + (90 + 45) * (left ? -1 : 1);
        }

        final double range = this.range.get() + Math.random() / 100f;
        final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
        final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

        yaw = getYaw(mc.thePlayer, new Vec3(posX, target.posY, posZ));

        this.yaw = yaw;
    }

    public static float getYaw(EntityPlayer from, Vec3 pos) {
        return from.rotationYaw + MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(pos.zCoord - from.posZ, pos.xCoord - from.posX)) - 90f - from.rotationYaw);
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event){
        if(Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && holdJump.get() && active)
            event.setJump(false);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (render.get()) {
            if (target == null) {
                return;
            }
            GL11.glPushMatrix();
            GL11.glTranslated(
                    target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX,
                    target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY,
                    target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ
            );
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glRotatef(90F, 1F, 0F, 0F);

            GL11.glLineWidth(3 + 7.25F);

            GL11.glColor3f(0F, 0F, 0F);
            GL11.glBegin(GL11.GL_LINE_LOOP);

            for (int i = 0; i <= 360; i += 30) {
                GL11.glVertex2f((float) (Math.cos(i * Math.PI / 180.0) * range.get()), (float) (Math.sin(i * Math.PI / 180.0) * range.get()));
            }

            GL11.glEnd();

            GL11.glLineWidth(3);
            GL11.glBegin(GL11.GL_LINE_LOOP);

            for (int i = 0; i <= 360; i += 30) {
                if (active)
                    GL11.glColor4f(0.5f, 1, 0.5f, 1f);
                else
                    GL11.glColor4f(1f, 1f, 1f, 1f);

                GL11.glVertex2f((float) (Math.cos(i * Math.PI / 180.0) * range.get()), (float) (Math.sin(i * Math.PI / 180.0) * range.get()));
            }

            GL11.glEnd();

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            GL11.glPopMatrix();

            GlStateManager.resetColor();
            GL11.glColor4f(1F, 1F, 1F, 1F);
        }
    }
}
