package io.github.capsicum0907.angelus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * The block itself, which does nothing while it stands there.
 *
 * <p>All of its character is in {@link AngelusRegistry#ANGEL_BLOCK}'s properties and
 * in the one method below. Where it can be put is the item's business, not the
 * block's — see {@link AngelBlockItem}.
 */
public class AngelBlock extends Block {
    public AngelBlock(Properties properties) {
        super(properties);
    }

    /**
     * Straight back into the inventory rather than onto the floor.
     *
     * <p>This is the half of the mod that makes one block enough for a whole walkway:
     * break the one behind you, and it is already in your hand for the one in front.
     * An item on the ground would have to be walked back to, which on a bridge over
     * nothing is exactly the walk the bridge exists to avoid.
     *
     * <p>The stack is built here rather than dropped through the loot table because
     * the block has none — see the note on {@code noLootTable} in the registry. If it
     * had one, a player breaking a block would get it twice.
     *
     * <p>⚠ Server side only. This runs on both, and the client copy would be a ghost
     * item that the next inventory packet quietly takes away again.
     *
     * <p>If the inventory is full, {@code placeItemBackInInventory} drops the stack at
     * the player's feet, which is the one case where it does hit the floor — and the
     * right one, since the alternative is losing it.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide && !player.isCreative()) {
            player.getInventory().placeItemBackInInventory(new ItemStack(this));
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
