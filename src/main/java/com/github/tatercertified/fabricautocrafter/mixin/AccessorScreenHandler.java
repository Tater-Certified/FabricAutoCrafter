package com.github.tatercertified.fabricautocrafter.mixin;// Created 2022-26-01T17:38:48

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 * @since ${version}
 **/
@Mixin(ScreenHandler.class)
public interface AccessorScreenHandler {
    @Accessor
    DefaultedList<ItemStack> getPreviousTrackedStacks();

    @Accessor
    DefaultedList<ItemStack> getTrackedStacks();
}
