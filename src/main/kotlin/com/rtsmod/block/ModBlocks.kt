package com.rtsmod.block

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.block.BlockState

/**
 * Custom Obelisk block for RTS interaction
 * Provides access to city management GUI
 */
class ObeliskBlock : BlockEntityProvider {
    
    companion object {
        val FACING: DirectionProperty = Properties.FACING
        
        // Create block with stone-like properties
        val SETTINGS = FabricBlockSettings.of(Material.STONE)
            .strength(3.0f, 6.0f)
            .requiresTool()
            .nonOpaque()
    }

    constructor() : super(SETTINGS) {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(FACING, ctx.playerFacing.opposite)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(FACING, rotation.rotate(state.get(FACING)))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.with(FACING, mirror.apply(state.get(FACING)))
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        // Register block entity type if not already registered
        if (ObeliskBlockEntity.TYPE == null) {
            ObeliskBlockEntity.TYPE = net.minecraft.registry.Registry.register(
                net.minecraft.registry.Registries.BLOCK_ENTITY_TYPE,
                net.minecraft.util.Identifier("rtsmod", "obelisk"),
                net.minecraft.block.entity.BlockEntityType.Builder
                    .create(::ObeliskBlockEntity, this)
                    .build()
            )
        }
        
        return ObeliskBlockEntity(pos, state)
    }

    override fun onUse(
        state: BlockState, 
        world: World, 
        pos: BlockPos, 
        player: PlayerEntity, 
        hand: Hand, 
        hit: BlockHitResult
    ): ActionResult {
        if (world.isClient) {
            return ActionResult.SUCCESS
        }

        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is ObeliskBlockEntity) {
            // Send packet to server to open GUI or handle interaction
            player.openHandledScreen(blockEntity)
        }

        return ActionResult.CONSUME
    }

    override fun onBreak(
        world: World, 
        pos: BlockPos, 
        state: BlockState, 
        player: PlayerEntity
    ) {
        super.onBreak(world, pos, state, player)
        
        // Clean up any RTS data associated with this obelisk
        if (world is net.minecraft.server.MinecraftServer) {
            // TODO: Handle cleanup when obelisk is broken
        }
    }
}

/**
 * Block entity for the Obelisk
 * Stores city reference and provides GUI factory
 */
class ObeliskBlockEntity(
    pos: BlockPos, 
    state: BlockState
) : BlockEntityImpl(TYPE, pos, state), Inventory, NamedScreenHandlerFactory {

    companion object {
        var TYPE: BlockEntityType<ObeliskBlockEntity>? = null
    }

    // Simple inventory for potential future use (storing scrolls, maps, etc.)
    private val inventory = arrayOfNulls<net.minecraft.item.ItemStack>(9)

    override fun getInvSize(): Int = 9

    override fun isInvEmpty(): Boolean {
        return inventory.all { it.isEmpty }
    }

    override fun getInvStack(slot: Int): net.minecraft.item.ItemStack {
        return inventory[slot] ?: net.minecraft.item.ItemStack.EMPTY
    }

    override fun takeInvStack(slot: Int, amount: Int): net.minecraft.item.ItemStack {
        val stack = getInvStack(slot)
        if (!stack.isEmpty) {
            val removed = stack.split(amount)
            setInvStack(slot, stack)
            return removed
        }
        return net.minecraft.item.ItemStack.EMPTY
    }

    override fun removeInvStack(slot: Int): net.minecraft.item.ItemStack {
        val stack = invStacks.getOrNull(slot) ?: return net.minecraft.item.ItemStack.EMPTY
        invStacks[slot] = net.minecraft.item.ItemStack.EMPTY
        markDirty()
        return stack
    }

    override fun setInvStack(slot: Int, stack: net.minecraft.item.ItemStack) {
        inventory[slot] = stack
        if (!stack.isEmpty && stack.count > getMaxCountPerStack()) {
            stack.count = getMaxCountPerStack()
        }
        markDirty()
    }

    override fun getMaxCountPerStack(): Int = 64

    override fun canInsertInvStack(slot: Int, stack: net.minecraft.item.ItemStack?): Boolean {
        return true // For now, allow any item
    }

    override fun canExtractInvStack(slot: Int, stack: net.minecraft.item.ItemStack?): Boolean {
        return true // For now, allow extraction
    }

    override fun clear() {
        inventory.fill(null)
    }

    // Track if GUI is currently open
    var isGuiOpen: Boolean = false

    override fun createMenu(syncId: Int, playerInv: net.minecraft.entity.player.PlayerInventory, player: PlayerEntity): net.minecraft.screen.ScreenHandler? {
        isGuiOpen = true
        
        // TODO: Create actual GUI for RTS city management
        // For now, just return null to prevent crashes
        return null
    }

    override fun getDisplayName() = net.minecraft.text.Text.of("RTS City Control")

    override fun onClose(player: PlayerEntity) {
        super.onClose(player)
        isGuiOpen = false
    }

    override fun markDirty() {
        super<BlockEntity>.markDirty()
    }
}

/**
 * Registry class for mod blocks
 */
object ModBlocks {
    // Register the Obelisk block
    val OBELISK = register("obelisk", ObeliskBlock())

    /**
     * Register all blocks and block entities
     */
    fun register() {
        // The block entity type will be registered when createBlockEntity is first called
        // This is a workaround for the circular dependency issue
    }

    /**
     * Helper to register a block
     */
    private fun register(name: String, block: Block): Block {
        return net.minecraft.registry.Registry.register(
            net.minecraft.registry.Registries.BLOCK,
            net.minecraft.util.Identifier("rtsmod", name),
            block
        )
    }
}