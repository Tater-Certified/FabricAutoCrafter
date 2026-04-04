package com.github.tatercertified.fabricautocrafter;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public class AutoCrafterMod implements ModInitializer {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("autocrafter", "autocrafter");
    private static final ResourceKey<Block> key_block = ResourceKey.create(Registries.BLOCK, IDENTIFIER);
    private static final ResourceKey<Item> key_item = ResourceKey.create(Registries.ITEM, IDENTIFIER);
    public static final Block BLOCK = new AutoCrafter(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).strength(2.5f, 2.5f).setId(key_block));
    public static final BlockItem ITEM = new PolymerBlockItem(BLOCK, new net.minecraft.world.item.Item.Properties().setId(key_item), Items.CRAFTING_TABLE);
    public static final BlockEntityType<AutoCraftingTableBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(AutoCraftingTableBlockEntity::new, BLOCK).build();

    @Override
    public void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register((content) -> content.accept(ITEM));

        Registry.register(BuiltInRegistries.BLOCK, IDENTIFIER, BLOCK);
        Registry.register(BuiltInRegistries.ITEM, IDENTIFIER, ITEM);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, IDENTIFIER, TYPE);
        PolymerBlockUtils.registerBlockEntity(TYPE);

    }
}