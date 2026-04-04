package com.github.tatercertified.fabricautocrafter.mixin;

import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes a fatal assumption of the hopper being able to always remove a stack when it cannot.
 * This is by doing an additional check <em>before</em> removing the item from the inventory.
 * <p>
 * This fixes the crafting table output slot being set by the hopper.
 *
 * @author Ampflower
 * @since ${version}
 **/
@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity {
    /**
     * Stub
     */
    @Shadow
    private static boolean canTakeItemFromContainer(Container into, Container from, ItemStack itemStack, int slot, Direction direction) {
        return Math.random() > .5d;
    }

    /**
     * Redirects the canExtract check to check if the stack can be inserted after checking if it can be extracted.
     * <p>
     * This should be side effect free for vanilla stuff; the only things that would be effected would be stuff
     * that does special logic on removal of a certain slot, like in the case of the automatic crafting table.
     * <p>
     * There's probably a better way, but it doesn't seem immediately obvious for an if condition.
     *
     * @author Ampflower
     * @reason Fix the hopper logic for the automatic crafting table
     */
    @Redirect(method = "tryTakeInItemFromSlot(Lnet/minecraft/world/level/block/entity/Hopper;Lnet/minecraft/world/Container;ILnet/minecraft/core/Direction;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;canTakeItemFromContainer(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Z"))
    private static boolean fabricAutoCrafter$canExtract$redirect(Container into, Container from, ItemStack itemStack, int slot, Direction direction, Hopper hopper) {
        return canTakeItemFromContainer(into, from, itemStack, slot, direction) && canInsertStack(hopper, itemStack);
    }

    /**
     * @param hopper The hopper attempting to transfer the item.
     * @param test   The ItemStack in process of being transferred.
     * @return true if the hopper can fit any amount of the stack, false otherwise.
     */
    private static boolean canInsertStack(Hopper hopper, ItemStack test) {
        for (int i = 0, l = hopper.getContainerSize(); i < l; i++) {
            if (hopper.canPlaceItem(i, test)) {
                var stack = hopper.getItem(i);
                if (stack.isEmpty() || (stack.getCount() < Math.min(stack.getMaxStackSize(), hopper.getMaxStackSize())
                        && ItemStack.isSameItemSameComponents(stack, test))) return true;
            }
        }
        return false;
    }
}
