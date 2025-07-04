package net.minecraft.item;

import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ItemSword extends Item {
    private float attackDamage;
    @Getter
    private final Item.ToolMaterial material;

    public ItemSword(Item.ToolMaterial material) {
        this.material = material;
        this.maxStackSize = 1;
        this.setMaxDamage(material.getMaxUses());
        this.setCreativeTab(CreativeTabs.tabCombat);
        this.attackDamage = 4.0F + material.getDamageVsEntity();
    }
    private float getAttackDamage(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseDamage = 0.0f;
        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword)item;
            baseDamage += sword.getAttackDamage();
        } else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            baseDamage += tool.getAttackDamage();
        }
        float enchantsDamage = (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseDamage + enchantsDamage;
    }
    public float getAttackDamage() {
        return this.attackDamage;
    }

    /**
     * Returns the amount of damage this item will deal. One heart of damage is equal to 2 damage points.
     */
    public float getDamageVsEntity() {
        return this.material.getDamageVsEntity();
    }

    public float getStrVsBlock(ItemStack stack, Block state) {
        if (state == Blocks.web) {
            return 15.0F;
        } else {
            Material material = state.getMaterial();
            return material != Material.plants && material != Material.vine && material != Material.coral && material != Material.leaves && material != Material.gourd ? 1.0F : 1.5F;
        }
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    /**
     * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     */
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, Block blockIn, BlockPos pos, EntityLivingBase playerIn) {
        if ((double) blockIn.getBlockHardness(worldIn, pos) != 0.0D) {
            stack.damageItem(2, playerIn);
        }

        return true;
    }

    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    public boolean isFull3D() {
        return true;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
        playerIn.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
        return itemStackIn;
    }

    /**
     * Check whether this Item can harvest the given Block
     */
    public boolean canHarvestBlock(Block blockIn) {
        return blockIn == Blocks.web;
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability() {
        return this.material.getEnchantability();
    }

    /**
     * Return the name for this tool's material.
     */
    public String getToolMaterialName() {
        return this.material.toString();
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.material.getRepairItem() == repair.getItem() ? true : super.getIsRepairable(toRepair, repair);
    }

    public Multimap<String, AttributeModifier> getItemAttributeModifiers() {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Weapon modifier", (double) this.attackDamage, 0));
        return multimap;
    }
}
