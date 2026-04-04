package com.github.tatercertified.fabricautocrafter;

import com.github.tatercertified.fabricautocrafter.mixin.AccessorAbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class AutoCraftingTableContainer extends CraftingMenu {
    private final AutoCraftingTableBlockEntity blockEntity;
    private final Player player;
    private TransientCraftingContainer crafting_inv;

    AutoCraftingTableContainer(int id, Inventory playerInventory, AutoCraftingTableBlockEntity blockEntity) {
        super(id, playerInventory);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;

        this.crafting_inv = blockEntity.bindInventory(this);

        var self = (AccessorAbstractContainerMenu) this;
        slots.clear();
        self.getLastSlots().clear();

        this.addSlot(new OutputSlot(this.blockEntity, this.player));

        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 3; ++x) {
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
    public void slotsChanged(Container inv) {
        if (this.player instanceof ServerPlayer) {
            ServerGamePacketListenerImpl netHandler = ((ServerPlayer) this.player).connection;
            netHandler.send(new ClientboundContainerSetSlotPacket(this.containerId, 0, 0, this.blockEntity.getItem(0)));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        if (slot == 0) {
            ItemStack before = this.blockEntity.getItem(0).copy();
            ItemStack current = before.copy();
            if (!this.moveItemStackTo(current, 10, 46, true)) return ItemStack.EMPTY;
            this.blockEntity.removeItem(0, before.getCount() - current.getCount());
            slots.getFirst().onQuickCraft(current, before); // calls onCrafted if different
            return this.blockEntity.getItem(0);
        }
        return super.quickMoveStack(player, slot);
    }

    public void close() {
        this.crafting_inv = blockEntity.unsetHandler();
        ItemStack cursorStack = this.player.containerMenu.getCarried();
        if (!cursorStack.isEmpty()) {
            this.player.drop(cursorStack, false);
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        if (this.player instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents finder) {
        this.crafting_inv.fillStackedContents(finder);
    }

    @Override
    public int getGridWidth() {
        return this.crafting_inv.getWidth();
    }

    @Override
    public int getGridHeight() {
        return this.crafting_inv.getHeight();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    private class OutputSlot extends Slot {
        private final Player player;
        OutputSlot(Container inv, Player player) {
            super(inv, 0, 124, 35);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack_1) {
            return false;
        }

        @Override
        protected void onSwapCraft(int amount) {
            AutoCraftingTableContainer.this.blockEntity.removeItem(0, amount);
        }

        @Override
        protected void onQuickCraft(ItemStack stack, int amount) {
            super.checkTakeAchievements(stack); // from CraftingResultsSlot onCrafted
            if (amount > 0) stack.onCraftedBy(this.player, amount);
            if (this.container instanceof RecipeCraftingHolder) ((RecipeCraftingHolder)this.container).awardUsedRecipes(this.player, List.of(stack));
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            onQuickCraft(stack, stack.getCount());
            super.onTake(player, stack);
        }
    }
}
