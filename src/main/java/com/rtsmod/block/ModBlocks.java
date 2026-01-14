package com.rtsmod.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Block registry for the RTS mod.
 */
public class ModBlocks {
    public static final Block OBELISK = new ObeliskBlock(AbstractBlock.Settings.copy(Blocks.STONE).strength(4.0f));
    
    public static BlockEntityType<ObeliskBlockEntity> OBELISK_ENTITY_TYPE;

    public static void registerBlocks() {
        Registry.register(Registries.BLOCK, Identifier.of("rtsmod", "obelisk"), OBELISK);
        Registry.register(Registries.ITEM, Identifier.of("rtsmod", "obelisk"), new BlockItem(OBELISK, new Item.Settings()));
        
        OBELISK_ENTITY_TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of("rtsmod", "obelisk_entity"),
            BlockEntityType.Builder.create(ObeliskBlockEntity::new, OBELISK).build(null)
        );
    }
}
