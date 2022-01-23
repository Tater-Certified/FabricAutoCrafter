package com.gitlab.essentialmods.fabricautocrafter.mixin;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingInventory.class)
public interface CraftingInventoryMixin {
    @Mutable
    @Accessor("stacks")
    void setInventory(DefaultedList<ItemStack> inventory);

    @Mutable
    @Accessor("handler")
    void setHandler(@Nullable ScreenHandler handler);
}
