package com.github.tatercertified.fabricautocrafter;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import static net.minecraft.block.Blocks.CRAFTING_TABLE;

public class AutoCrafter extends Block implements PolymerBlock, BlockEntityProvider {

    protected AutoCrafter(AbstractBlock.Settings blockSettings) {
        super(blockSettings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        } else if (world.getBlockEntity(pos) instanceof AutoCraftingTableBlockEntity entity) {
            player.openHandledScreen(entity);
            player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return CRAFTING_TABLE.getDefaultState();
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return state.hasBlockEntity();
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (!state.hasBlockEntity()) {
            return 0;
        }
        if (world.getBlockEntity(pos) instanceof AutoCraftingTableBlockEntity craftingTableBlockEntity) {
            int filled = 0;
            for (ItemStack stack : craftingTableBlockEntity.getHeldStacks()) {
                if (!stack.isEmpty()) filled++;
            }
            return (filled * 15) / 9;
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.isOf(AutoCrafterMod.BLOCK) ? new AutoCraftingTableBlockEntity(pos, state) : null;
    }
}
