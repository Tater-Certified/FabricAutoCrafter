package com.github.tatercertified.fabricautocrafter;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.Blocks.CRAFTING_TABLE;

public class AutoCrafter extends Block implements PolymerBlock, EntityBlock {

    protected AutoCrafter(BlockBehaviour.Properties blockSettings) {
        super(blockSettings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else if (world.getBlockEntity(pos) instanceof AutoCraftingTableBlockEntity entity) {
            player.openMenu(entity);
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return CRAFTING_TABLE.defaultBlockState();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return state.hasBlockEntity();
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        if (!state.hasBlockEntity()) {
            return 0;
        }
        if (world.getBlockEntity(pos) instanceof AutoCraftingTableBlockEntity craftingTableBlockEntity) {
            int filled = 0;
            for (ItemStack stack : craftingTableBlockEntity.getItems()) {
                if (!stack.isEmpty()) filled++;
            }
            return (filled * 15) / 9;
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.is(AutoCrafterMod.BLOCK) ? new AutoCraftingTableBlockEntity(pos, state) : null;
    }
}
