package dev.olive.event.impl.events;


import dev.olive.event.impl.CancellableEvent;
import net.minecraft.entity.EntityLivingBase;

public class RenderNameTagEvent extends CancellableEvent {

    private final EntityLivingBase entityLivingBase;

    public RenderNameTagEvent(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    public EntityLivingBase getEntityLivingBase() {
        return entityLivingBase;
    }

}
