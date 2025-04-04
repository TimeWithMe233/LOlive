 package dev.olive.module.impl.player;

 import dev.olive.Client;
 import dev.olive.event.annotations.EventTarget;
 import dev.olive.event.impl.events.EventUpdate;
 import dev.olive.module.Category;
 import dev.olive.module.Module;
 import dev.olive.module.impl.combat.KillAura;
 import dev.olive.module.impl.combat.Velocity;
 import dev.olive.value.impl.BoolValue;
 import dev.olive.value.impl.NumberValue;
 import net.minecraft.client.gui.inventory.GuiInventory;
 import net.minecraft.network.play.client.C0DPacketCloseWindow;
 import net.minecraft.network.play.client.C16PacketClientStatus;

 public class AutoLobby extends Module {
     private final NumberValue health = new NumberValue("Health", 5.0, 0.0, 20.0, 1.0);
     private final BoolValue randomhub = new BoolValue("RandomHub", false);
     private final BoolValue disabler = new BoolValue("AutoDisable-KillAura-Velocity", true);
     private final BoolValue keepArmor = new BoolValue("KeepArmor", true);
     private final BoolValue noHub = new BoolValue("NoHub", false);

     public AutoLobby() {
         super("AutoLobby", "自动逃逸",Category.Misc);
     }

     @EventTarget
     public void onUpdate(EventUpdate event) {
         KillAura killAura = Client.instance.moduleManager.getModule(KillAura.class);
         Velocity velocity = Client.instance.moduleManager.getModule(Velocity.class);
         if (!((Boolean)this.noHub.getValue()).booleanValue()) {
             if (AutoLobby.mc.thePlayer.getHealth() <= ((Double)this.health.getValue()).floatValue()) {
                 if (((Boolean)this.keepArmor.getValue()).booleanValue()) {
                     for (int i = 0; i <= 3; ++i) {
                         int armorSlot = 3 - i;
                         this.move(8 - armorSlot);
                     }
                 }
                 if (((Boolean)this.randomhub.getValue()).booleanValue()) {
                     AutoLobby.mc.thePlayer.sendChatMessage("/hub " + (int)(Math.random() * 100.0 + 1.0));
                 } else {
                     AutoLobby.mc.thePlayer.sendChatMessage("/hub");
                 }
                 if (((Boolean)this.disabler.getValue()).booleanValue()) {
                     killAura.setState(false);

                 }
             }
         } else if ((AutoLobby.mc.thePlayer.isDead || AutoLobby.mc.thePlayer.getHealth() == 0.0f || AutoLobby.mc.thePlayer.getHealth() <= 0.0f) && ((Boolean)this.disabler.getValue()).booleanValue()) {
             killAura.setState(false);
         }
     }

     private void move(int item) {
         if (item != -1) {
             boolean openInventory;
             boolean bl = openInventory = !(AutoLobby.mc.currentScreen instanceof GuiInventory);
             if (openInventory) {
                 mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
             }
             AutoLobby.mc.playerController.windowClick(AutoLobby.mc.thePlayer.inventoryContainer.windowId, item, 0, 1, AutoLobby.mc.thePlayer);
             if (openInventory) {
                 mc.getNetHandler().addToSendQueue(new C0DPacketCloseWindow());
             }
         }
     }
 }

