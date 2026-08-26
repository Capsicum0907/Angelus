package io.github.capsicum0907.angelus;

import io.github.capsicum0907.angelus.data.TestStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What the block does, checked without a person having to look.
 *
 * <p>These call {@link AngelBlockItem#use} directly, which is one step short of what
 * a player does. In play the game only reaches {@code use} when the crosshair has no
 * block in range; aim at something and it routes to {@code useOn} and places against
 * it in the ordinary way. That routing is vanilla's and is not tested here, so a
 * green run means "the block goes where it should once we are asked", not
 * "right-clicking works".
 *
 * <p>The one thing none of this can check is whether you can see through it. That
 * needs three separate things to line up - alpha in the sprite, {@code cutout} on the
 * model, {@code noOcclusion()} on the block - and only the third is visible from
 * here, in {@link #isNotSolid}. The other two are settled where they are made.
 *
 * <p>Run with {@code gradlew runGameTestServer}.
 */
@GameTestHolder(Angelus.MODID)
@PrefixGameTestTemplate(false)
public final class AngelusTests {
    /** On the last block of the ledge, with the drop off the end of it. */
    private static final BlockPos STAND = new BlockPos(TestStructures.LEDGE, 1, 3);

    /**
     * How far off {@link Vec3#atCenterOf} a hit can land and still be the same block.
     *
     * <p>Half the diagonal of a cube, which is the furthest a point inside one can be
     * from its middle. Any tolerance smaller than this would be rejecting placements
     * that are correct; any larger would start accepting the next block along.
     */
    private static final double HALF_DIAGONAL = 0.87;

    private AngelusTests() {
    }

    /**
     * The claim the whole mod is for: a block appears with nothing under, over or
     * beside it.
     *
     * <p>Stated as the thing that would disprove it. If any neighbour were solid this
     * would be an ordinary placement against that neighbour and would prove nothing,
     * which is why the stage has a short ledge and open air rather than a floor.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void placesWithNothingToPlaceAgainst(GameTestHelper helper) {
        Player player = aiming(helper, GameType.SURVIVAL, new ItemStack(AngelusRegistry.block()));
        BlockPos where = aimedAt(player);

        use(player, helper);

        landed(helper, where);
        for (Direction side : Direction.values()) {
            BlockPos neighbour = where.relative(side);
            check(helper.getLevel().getBlockState(neighbour).is(Blocks.AIR),
                    "nothing should be touching a block placed in mid-air, but the "
                            + side + " side of it holds "
                            + helper.getLevel().getBlockState(neighbour).getBlock());
        }
        check(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
                "placing the only angel block in hand should have used it up");
        helper.succeed();
    }

    /**
     * That it goes where it is aimed, rather than to a fixed spot beside the player.
     *
     * <p>Worth its own test because a mod can look right in play and still be doing
     * this: place from one stance, place from another, and if both blocks land in the
     * same place relative to the player then nothing was ever read off the crosshair.
     * The distance is checked at the same time, against
     * {@link Player#blockInteractionRange()} rather than against a number written
     * here - the point is that the two agree.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void goesWhereItIsAimed(GameTestHelper helper) {
        Player player = aiming(helper, GameType.SURVIVAL, new ItemStack(AngelusRegistry.block()));
        Vec3 eye = player.getEyePosition();
        double reach = player.blockInteractionRange();

        BlockPos first = aimedAt(player);
        use(player, helper);
        landed(helper, first);
        away(eye, first, reach);

        // The same player, looking up. Nothing else about the stance changes - the eye
        // has not moved, so a block that lands somewhere new can only have read the
        // look. Pitch rather than yaw because the stage is boxed in by barriers on
        // every side and turning would aim out through one of them.
        player.setXRot(-45.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AngelusRegistry.block()));
        BlockPos second = aimedAt(player);
        use(player, helper);
        landed(helper, second);
        away(eye, second, reach);

        check(!first.equals(second),
                "looking somewhere else should put the next block somewhere else, but "
                        + "both landed at " + helper.relativePos(first));
        helper.succeed();
    }

    /**
     * The other half of what the block is for: building down into water.
     *
     * <p>This is the case that separates delegating to vanilla's placement from
     * checking the target cell against air. Water is replaceable, so
     * {@code canBeReplaced} says yes and the block takes its place; a mod that asked
     * {@code == Blocks.AIR} instead would refuse here, and refuse in tall grass and
     * snow and everywhere else a block can ordinarily be put.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void goesInWaterAsWellAsAir(GameTestHelper helper) {
        Player player = aiming(helper, GameType.SURVIVAL, new ItemStack(AngelusRegistry.block()));
        BlockPos where = aimedAt(player);
        helper.getLevel().setBlockAndUpdate(where, Blocks.WATER.defaultBlockState());

        use(player, helper);

        landed(helper, where);
        helper.succeed();
    }

    /**
     * That a block already standing somewhere is never destroyed to make room.
     *
     * <p>What happens next is vanilla's and is not this mod's business: with something
     * solid at the far end of the reach, the placement resolves the way a click on that
     * block would, and the angel block ends up against its near face instead. That is
     * not reachable in play - a crosshair that could see the stone would have gone down
     * the {@code useOn} path long before {@code use} was called - so the only thing
     * worth pinning down here is the safe half: the stone survives.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void neverOverwritesWhatIsThere(GameTestHelper helper) {
        Player player = aiming(helper, GameType.SURVIVAL, new ItemStack(AngelusRegistry.block()));
        BlockPos where = aimedAt(player);
        helper.getLevel().setBlockAndUpdate(where, Blocks.STONE.defaultBlockState());

        use(player, helper);

        check(helper.getLevel().getBlockState(where).is(Blocks.STONE),
                "the stone in the way should still be there, but the space now holds "
                        + helper.getLevel().getBlockState(where).getBlock());
        helper.succeed();
    }

    /**
     * The half that makes one block enough for a whole walkway.
     *
     * <p>Broken through {@code BlockState.onDestroyedByPlayer}, which is the call the
     * server makes when a player finishes mining, rather than by reaching for the
     * override directly - so the dispatch is part of what is being checked.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void comesBackToTheHand(GameTestHelper helper) {
        Player player = aiming(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        BlockPos where = aimedAt(player);
        helper.getLevel().setBlockAndUpdate(where, AngelusRegistry.block().defaultBlockState());

        broken(helper, player, where);

        check(helper.getLevel().getBlockState(where).is(Blocks.AIR),
                "the block should be gone from the world once broken");
        check(player.getInventory().countItem(AngelusRegistry.block().asItem()) == 1,
                "breaking it should have put exactly one back in the inventory");
        helper.succeed();
    }

    /**
     * The mirror of the test above, and the one that would catch a duplication bug: a
     * creative player breaking a block must not be handed a copy of it.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void givesCreativeNothingBack(GameTestHelper helper) {
        Player player = aiming(helper, GameType.CREATIVE, ItemStack.EMPTY);
        BlockPos where = aimedAt(player);
        helper.getLevel().setBlockAndUpdate(where, AngelusRegistry.block().defaultBlockState());

        broken(helper, player, where);

        check(player.getInventory().countItem(AngelusRegistry.block().asItem()) == 0,
                "a creative player already has the block; breaking one should not hand "
                        + "over another");
        helper.succeed();
    }

    /**
     * One third of being see-through, and the only third a test can reach.
     *
     * <p>If this ever fails, the symptom in play is not a missing texture: the block
     * looks right and the world behind its holes is culled away, so the gaps show
     * whatever is drawn beyond the world instead. That is a strange enough thing to
     * look at that nobody guesses the cause from it, which is why it is worth a test
     * rather than a comment.
     */
    @GameTest(template = TestStructures.LEDGE_AND_AIR)
    public static void isNotSolid(GameTestHelper helper) {
        BlockState state = AngelusRegistry.block().defaultBlockState();
        check(!state.canOcclude(),
                "the block has holes in it, so it must not occlude the faces behind it");
        helper.succeed();
    }

    // --- the stance -------------------------------------------------------------

    /**
     * A mock player stood on {@link #STAND} facing out over the drop, holding the given
     * stack in the main hand.
     *
     * <p>The yaw is worked out from where the stage actually ended up rather than
     * written down, because a game test structure is placed with a rotation and the
     * ledge does not have to run east in the world. Two relative positions are turned
     * into world ones and the angle is read off the line between them, so "out over the
     * drop" stays true however the stage was laid down.
     */
    private static Player aiming(GameTestHelper helper, GameType mode, ItemStack held) {
        // Offset in the stage's own coordinates and then converted, not converted and
        // then offset: half a block along the stage's z is only half a block along the
        // world's z when the stage happens to have been laid down unrotated.
        Vec3 here = helper.absoluteVec(new Vec3(STAND.getX(), STAND.getY(), STAND.getZ() + 0.5));
        Vec3 outward = helper.absoluteVec(Vec3.atLowerCornerOf(STAND.east()))
                .subtract(helper.absoluteVec(Vec3.atLowerCornerOf(STAND)));

        Player player = helper.makeMockPlayer(mode);
        player.moveTo(here.x, here.y, here.z, yaw(outward), 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        return player;
    }

    /** The yaw that looks along a horizontal direction. Zero faces +Z, and it turns the other way. */
    private static float yaw(Vec3 direction) {
        return (float) (-Math.atan2(direction.x, direction.z) * 180.0 / Math.PI);
    }

    /** Where this player's crosshair falls: a whole reach out from the eye. */
    private static BlockPos aimedAt(Player player) {
        return BlockPos.containing(
                player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange())));
    }

    private static void use(Player player, GameTestHelper helper) {
        player.getItemInHand(InteractionHand.MAIN_HAND).getItem()
                .use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static void broken(GameTestHelper helper, Player player, BlockPos where) {
        BlockState state = helper.getLevel().getBlockState(where);
        state.onDestroyedByPlayer(helper.getLevel(), where, player, true,
                helper.getLevel().getFluidState(where));
    }

    // --- what went wrong --------------------------------------------------------

    /**
     * That the block is at the given place, and if it is not, where it went instead.
     *
     * <p>The sweep is only for the failure message. "Expected here, got air" leaves the
     * next person to go and find it by hand, and the interesting failures of a mod that
     * chooses a position are all of the form "somewhere else, consistently".
     */
    private static void landed(GameTestHelper helper, BlockPos where) {
        if (helper.getLevel().getBlockState(where).is(AngelusRegistry.block())) {
            return;
        }
        BlockPos found = sweep(helper);
        throw new GameTestAssertException("expected the block at " + helper.relativePos(where)
                + " (relative), but it is "
                + (found == null ? "nowhere on the stage" : "at " + helper.relativePos(found)));
    }

    /** How far a placed block's middle is from the eye, against the reach it should be. */
    private static void away(Vec3 eye, BlockPos where, double reach) {
        double reached = Vec3.atCenterOf(where).subtract(eye).length();
        check(Math.abs(reached - reach) <= HALF_DIAGONAL,
                "the block should sit a reach of " + reach + " from the eye, but its middle is "
                        + reached + " away");
    }

    private static BlockPos sweep(GameTestHelper helper) {
        for (int x = 0; x < TestStructures.WIDTH; x++) {
            for (int y = 0; y < TestStructures.HEIGHT; y++) {
                for (int z = 0; z < TestStructures.DEPTH; z++) {
                    BlockPos at = helper.absolutePos(new BlockPos(x, y, z));
                    if (helper.getLevel().getBlockState(at).is(AngelusRegistry.block())) {
                        return at;
                    }
                }
            }
        }
        return null;
    }

    private static void check(boolean condition, String expectation) {
        if (!condition) {
            throw new GameTestAssertException(expectation);
        }
    }
}
