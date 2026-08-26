package io.github.capsicum0907.angelus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

/**
 * The whole mod, in one method: it decides <em>where</em>, and hands the rest back
 * to the game.
 *
 * <p>Placing a block normally needs something to place it against, because the
 * position comes from the face the crosshair is on. With nothing under the
 * crosshair there is no face, so there is no position, so there is no placement.
 * That is the only thing standing between a player and a walkway out over open
 * air — not a rule about support, just a missing coordinate.
 *
 * <p>So this supplies the coordinate and nothing else. Everything after it —
 * whether the space is free, whether a claim mod objects, the sound, the shrunk
 * stack, the fact that you cannot put a block inside your own head — is vanilla's
 * placement path, reached through {@link ItemStack#useOn}. Reimplementing any of
 * that would only be a second copy to keep in step, and the second copy is always
 * the one that forgets to fire the event a protection mod is listening for.
 */
public class AngelBlockItem extends BlockItem {
    public AngelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * Right-click on nothing: put the block where the crosshair would have hit.
     *
     * <p>The distance is {@link Player#blockInteractionRange()}, which is the
     * attribute that decides how far away an ordinary block can be placed — 4.5 in
     * a vanilla game. Reading it rather than writing 4.5 here means the two can
     * never disagree, and that anything which extends a player's reach extends this
     * with it, without this mod knowing such a thing exists.
     *
     * <p>⚠ This only runs when the crosshair has <em>no</em> block in range. Aim at
     * ground two blocks away and the game routes to {@code useOn} instead and places
     * against it in the ordinary way. That is the right split — a mod that hijacked
     * aimed placement would be unusable for building — but it means a test that
     * calls this method directly has not exercised the path a player takes to get
     * here.
     *
     * <p>The face handed to the hit result is the one facing back along the look
     * vector, i.e. the side the block is approached from. Nothing here reads it —
     * the block has one state and no facing — but it is what a protection mod is
     * given as the placed-against side, and the honest answer is better than
     * {@code Direction.UP}.
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
            @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        Vec3 look = player.getLookAngle();
        Vec3 aimed = player.getEyePosition().add(look.scale(player.blockInteractionRange()));
        BlockPos target = BlockPos.containing(aimed);
        if (!level.isInWorldBounds(target)) {
            return InteractionResultHolder.pass(held);
        }

        BlockHitResult hit = new BlockHitResult(aimed, Direction.getNearest(look.reverse()), target, false);

        // ItemStack.useOn, not BlockItem.place: on a server it goes through the hook
        // that captures the placement and offers it to BlockEvent.EntityPlaceEvent,
        // and on a client it places the same block locally so the walkway appears
        // under the player's feet at once instead of a round trip later.
        //
        // No recursion, though it reads like it: BlockItem.useOn calls place(), and the
        // only route it has back to a use() is super.use() — bound to Item, not to this
        // override — and it takes it only for an item carrying a FOOD component.
        InteractionResult placed = held.useOn(new UseOnContext(player, hand, hit));
        return new InteractionResultHolder<>(placed, held);
    }
}
