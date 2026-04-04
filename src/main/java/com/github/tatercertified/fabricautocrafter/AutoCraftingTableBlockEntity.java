package com.github.tatercertified.fabricautocrafter;

import com.github.tatercertified.fabricautocrafter.mixin.TransientCraftingContainerMixin;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AutoCraftingTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, CraftingContainer {

    private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int GRID_WIDTH = 3;
    private static final int GRID_HEIGHT = 3;

    private final List<AutoCraftingTableContainer> openContainers = new ArrayList<>();
    private final TransientCraftingContainer craftingInventory = new TransientCraftingContainer(null, 3, 3);
    public NonNullList<ItemStack> inventory;
    private ItemStack output = ItemStack.EMPTY;
    private RecipeHolder<?> lastRecipe;
    private static final RecipeCache recipeCache = new RecipeCache(10);

    public AutoCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(AutoCrafterMod.TYPE, pos, state);
        this.inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        ((TransientCraftingContainerMixin) craftingInventory).setInventory(this.inventory);
    }

    public TransientCraftingContainer bindInventory(AbstractContainerMenu handler) {
        ((TransientCraftingContainerMixin) craftingInventory).setMenu(handler);
        return craftingInventory;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, inventory);
        if (!output.isEmpty()) {
            view.store("Output", ItemStack.CODEC, output);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ContainerHelper.loadAllItems(view, inventory);
        view.read("Output", ItemStack.CODEC).ifPresentOrElse(
                stack -> this.output = stack, () -> this.output = ItemStack.EMPTY
        );
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.autocrafter");
    }

    @Override
    public int getWidth() {
        return GRID_WIDTH;
    }

    @Override
    public int getHeight() {
        return GRID_HEIGHT;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        final AutoCraftingTableContainer container = new AutoCraftingTableContainer(id, playerInventory, this);
        this.openContainers.add(container);
        return container;
    }

    @Override
    public int[] getSlotsForFace(Direction dir) {
        return (dir == Direction.DOWN && (!output.isEmpty() || (!quickEscape() && !getCurrentRecipe().isEmpty()))) ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot > 0 && getItem(slot).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot != 0 || !output.isEmpty() || (!quickEscape() && !getCurrentRecipe().isEmpty());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != 0 && slot <= getContainerSize();
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return output.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot > 0) return this.inventory.get(slot - 1);
        if (!output.isEmpty()) return output;
        return quickEscape()? ItemStack.EMPTY : getCurrentRecipe();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot == 0) {
            if (output.isEmpty()) output = craft();
            return output.split(amount);
        }
        return ContainerHelper.removeItem(this.inventory, slot - 1, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            ItemStack output = this.output;
            this.output = ItemStack.EMPTY;
            return output;
        }
        return ContainerHelper.takeItem(this.inventory, slot - 1);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            output = stack;
            return;
        }
        inventory.set(slot - 1, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (AutoCraftingTableContainer c : openContainers) c.slotsChanged(this);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.blockPosition().distSqr(this.worldPosition) <= 64.0D;
    }

    @Override
    public void fillStackedContents(StackedItemContents finder) {
        for (ItemStack stack : this.inventory) finder.accountStack(stack);
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        lastRecipe = recipe;
    }

    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return lastRecipe;
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
    }

    private ItemStack getCurrentRecipe() {
        BlockEntity craftingRecipeInput = this.level.getBlockEntity(worldPosition);
        if (craftingRecipeInput instanceof AutoCraftingTableBlockEntity autoCraftingTableBlockEntity) {
            CraftingInput var11 = autoCraftingTableBlockEntity.asCraftInput();
            Optional<RecipeHolder<CraftingRecipe>> optional = getCraftingRecipe((ServerLevel) this.level, var11);
            if (optional.isPresent()) {
                RecipeHolder<CraftingRecipe> recipeEntry = optional.get();
                return recipeEntry.value().assemble(var11);
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean quickEscape() {
        return this.level == null || this.isEmpty();
    }

    private ItemStack craft() {
        if (quickEscape()) return ItemStack.EMPTY;
        BlockEntity craftingRecipeInput = this.level.getBlockEntity(worldPosition);
        if (craftingRecipeInput instanceof AutoCraftingTableBlockEntity autoCraftingTableBlockEntity) {
            ItemStack itemStack = getCurrentRecipe();
            if (!itemStack.isEmpty()) {
                itemStack.onCraftedBySystem(this.level);
                autoCraftingTableBlockEntity.getItems().forEach((stack) -> {
                    if (!stack.isEmpty()) {
                        stack.shrink(1);
                    }
                });
                autoCraftingTableBlockEntity.setChanged();
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Optional<RecipeHolder<CraftingRecipe>> getCraftingRecipe(ServerLevel world, CraftingInput input) {
        return recipeCache.get(world, input);
    }

    public TransientCraftingContainer unsetHandler() {
        ((TransientCraftingContainerMixin) craftingInventory).setMenu(null);
        return craftingInventory;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.getItems());
        }

        ListIterator<AutoCraftingTableContainer> iterator = this.openContainers.listIterator();
        while (iterator.hasNext()) {
            iterator.next().close();
            iterator.remove();
        }
    }
}
