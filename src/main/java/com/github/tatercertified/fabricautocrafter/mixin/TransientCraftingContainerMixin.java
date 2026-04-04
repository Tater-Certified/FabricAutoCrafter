package com.github.tatercertified.fabricautocrafter.mixin;

import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TransientCraftingContainer.class)
public interface TransientCraftingContainerMixin {
    @Mutable
    @Accessor("items")
    void setInventory(NonNullList<ItemStack> inventory);

    @Mutable
    @Accessor("menu")
    void setMenu(@Nullable AbstractContainerMenu handler);
}
