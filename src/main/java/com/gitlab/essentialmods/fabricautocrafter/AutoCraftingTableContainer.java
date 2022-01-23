package com.gitlab.essentialmods.fabricautocrafter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class AutoCraftingTableContainer extends CraftingScreenHandler {
    private final CraftingTableBlockEntity blockEntity;
    private final PlayerEntity player;
    private CraftingInventory crafting_inv;

    AutoCraftingTableContainer(int id, PlayerInventory playerInventory, CraftingTableBlockEntity blockEntity) {
        super(id, playerInventory);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;

        this.crafting_inv = blockEntity.boundCraftingInventory(this);

        slots.clear();
        this.addSlot(new OutputSlot(this.blockEntity, this.player));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                this.addSlot(new Slot(this.blockEntity, x + y * 3 + 1, 30 + x * 18, 17 + y * 18));
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void onContentChanged(Inventory inv) {
        if (this.player instanceof ServerPlayerEntity) {
            ServerPlayNetworkHandler netHandler = ((ServerPlayerEntity) this.player).networkHandler;
            netHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, 0, this.blockEntity.getStack(1)));
            netHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, 0, this.blockEntity.getStack(0)));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slot) {
        if (slot == 0) {
            ItemStack before = this.blockEntity.getStack(0).copy();
            ItemStack current = before.copy();
            if (!this.insertItem(current, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            this.blockEntity.removeStack(0, before.getCount() - current.getCount());
            slots.get(0).onQuickTransfer(current, before); // calls onCrafted if different
            return this.blockEntity.getStack(0);
        }
        return super.transferSlot(player, slot);
    }

    public void close(PlayerEntity player) {
        this.crafting_inv = blockEntity.unsetHandler();
        ItemStack cursorStack = this.player.currentScreenHandler.getCursorStack();
        if (!cursorStack.isEmpty()) {
            player.dropItem(cursorStack, false);
            this.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
        this.blockEntity.onContainerClose(this);
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.crafting_inv.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.crafting_inv.clear();
    }

    @Override
    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return recipe.matches(this.crafting_inv, this.player.world);
    }

    @Override
    public int getCraftingWidth() {
        return this.crafting_inv.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.crafting_inv.getHeight();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.blockEntity.canPlayerUse(player);
    }

    private class OutputSlot extends Slot {
        private final PlayerEntity player;

        OutputSlot(Inventory inv, PlayerEntity player) {
            super(inv, 0, 124, 35);
            this.player = player;
        }

        @Override
        public boolean canInsert(ItemStack itemStack_1) {
            return false;
        }

        @Override
        protected void onTake(int amount) {
            AutoCraftingTableContainer.this.blockEntity.removeStack(0, amount);
        }

        @Override
        protected void onCrafted(ItemStack stack, int amount) {
            super.onCrafted(stack);
            // from CraftingResultsSlot onCrafted
            if (amount > 0) {
                stack.onCraft(this.player.world, this.player, amount);
            }

            if (this.inventory instanceof RecipeUnlocker) {
                ((RecipeUnlocker) this.inventory).unlockLastRecipe(this.player);
            }
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            onCrafted(stack, stack.getCount());
            super.onTakeItem(player, stack);
        }
    }
}
