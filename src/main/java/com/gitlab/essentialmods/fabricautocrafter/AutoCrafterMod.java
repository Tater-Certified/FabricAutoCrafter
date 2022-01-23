package com.gitlab.essentialmods.fabricautocrafter;

import eu.pb4.polymer.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AutoCrafterMod implements ModInitializer {

    public static final String MODID = "autocrafter";
    public static final Identifier IDENTIFIER = new Identifier(MODID, "autocrafter");
    public static final Block BLOCK = new AutoCrafter(FabricBlockSettings.of(Material.WOOD).strength(2.5f,2.5f));
    public static final BlockItem ITEM = new PolymerBlockItem(BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE.setNoScrollbar()), Items.CRAFTING_TABLE);
    public static final BlockEntityType<CraftingTableBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(CraftingTableBlockEntity::new, BLOCK).build(null);

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, IDENTIFIER, BLOCK);
        Registry.register(Registry.ITEM, IDENTIFIER, ITEM);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, IDENTIFIER, TYPE);
    }
}
