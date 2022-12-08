package com.github.tatercertified.fabricautocrafter;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent;

public class AutoCrafterMod implements ModInitializer {

    public static final Identifier IDENTIFIER = new Identifier("autocrafter", "autocrafter");
    public static final Block BLOCK = new AutoCrafter(FabricBlockSettings.of(Material.WOOD).strength(2.5f, 2.5f));
    public static final BlockItem ITEM = new PolymerBlockItem(BLOCK, new FabricItemSettings(), Items.CRAFTING_TABLE);
    public static final BlockEntityType<CraftingTableBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(CraftingTableBlockEntity::new, BLOCK).build(null);

    @Override
    public void onInitialize() {
        modifyEntriesEvent(ItemGroups.REDSTONE).register((content) -> content.add(ITEM));

        Registry.register(Registries.BLOCK, IDENTIFIER, BLOCK);
        Registry.register(Registries.ITEM, IDENTIFIER, ITEM);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, IDENTIFIER, TYPE);
        PolymerBlockUtils.registerBlockEntity(TYPE);
    }
}
