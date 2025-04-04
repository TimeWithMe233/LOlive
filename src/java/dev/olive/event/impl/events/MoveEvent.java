package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

/**
 * @author TG_format
 * @since 2024/8/9 下午9:08
 */
@AllArgsConstructor
@Setter
@Getter
public class MoveEvent extends CancellableEvent {

    private double x, y, z;
    public boolean safewalk = false;

    public void setZero() {
        setX(0);
        setZ(0);
        setY(0);
    }

    public void setZeroXZ() {
        setX(0);
        setZ(0);
    }

    public void setX(double x) {
        Minecraft.getMinecraft().thePlayer.motionX = x;
        this.x = x;
    }

    public void setY(double y) {
        Minecraft.getMinecraft().thePlayer.motionY = y;
        this.y = y;
    }

    public void setZ(double z) {
        Minecraft.getMinecraft().thePlayer.motionZ = z;
        this.z = z;
    }

}
