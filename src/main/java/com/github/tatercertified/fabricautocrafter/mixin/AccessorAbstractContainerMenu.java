package com.github.tatercertified.fabricautocrafter.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 * @since ${version}
 **/
@Mixin(AbstractContainerMenu.class)
public interface AccessorAbstractContainerMenu {
    @Accessor
    NonNullList<ItemStack> getLastSlots();
}
