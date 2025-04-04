 package dev.olive.module.impl.combat;

 import java.util.Collection;

 import dev.olive.event.annotations.EventTarget;
 import dev.olive.event.impl.events.EventMotion;
 import dev.olive.module.Category;
 import dev.olive.module.Module;
 import dev.olive.value.impl.NumberValue;
 import net.minecraft.init.Items;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.Packet;
 import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
 import net.minecraft.network.play.client.C09PacketHeldItemChange;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;

 public class AutoHead
           extends Module {
       public final NumberValue delayValue = new NumberValue("Delay", 500.0D, 0.0D, 15000.0D, 1.0D);
       public final NumberValue healValue = new NumberValue("Health", 12.0D, 0.0D, 20.0D, 1.0D);
       private long time = -1L;
    
       public AutoHead() {
             super("AutoHead","自动金头", Category.Misc);
           }
    
    
       @EventTarget
       public void onMotion(EventMotion event) {
           if (!hasTimePassed(((Double)this.delayValue.getValue()).longValue())) {
                   return;
                 }
             if (mc.thePlayer.getHealth() <= ((Double)this.healValue.getValue()).doubleValue() && !hasRegeneration() && findItem(36, 45, Items.skull) != -1) {
                   mc.getNetHandler().addToSendQueue((Packet)new C09PacketHeldItemChange(findItem(36, 45, Items.skull) - 36));
                   mc.getNetHandler().addToSendQueue((Packet)new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                   mc.getNetHandler().addToSendQueue((Packet)new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                   reset();
                 }
           }
    
       private boolean hasRegeneration() {
             Collection<PotionEffect> activeEffects = mc.thePlayer.getActivePotionEffects();
             for (PotionEffect effect : activeEffects) {
                   if (effect.getPotionID() != Potion.regeneration.id)
                         continue;  return true;
                 }
             return false;
           }
    
       public boolean hasTimePassed(long MS) {
             return (System.currentTimeMillis() >= this.time + MS);
           }
    
       public void reset() {
             this.time = System.currentTimeMillis();
           }
    
       public static int findItem(int startSlot, int endSlot, Item item) {
             for (int i = startSlot; i < endSlot; ) {
                   ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                   if (stack == null || stack.getItem() != item) { i++; continue; }
                    return i;
                 }
             return -1;
           }
     }


