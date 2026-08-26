package io.github.capsicum0907.angelus;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The whole of the mod's contents: one block and the item that places it.
 *
 * <p>The id is {@code angel_block} rather than {@code angelus}, because the name
 * a player types into a recipe search is the one the block has had in every mod
 * that ever shipped it. The Latin is the mod's name, not the block's.
 */
public final class AngelusRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Angelus.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Angelus.MODID);

    public static final String ANGEL_BLOCK_ID = "angel_block";

    /**
     * <ul>
     * <li>{@code instabreak} — one hit, no tool. This is scaffolding you are meant to
     *     pick up again as you go, and anything slower turns a walkway into a chore.
     *     It sets blast resistance to zero at the same time, which is the intent:
     *     a temporary block should not survive as accidental blast cover.
     * <li>{@code noOcclusion} — the texture has holes in it, so the faces behind it
     *     have to be drawn. Without this the neighbouring block's face is culled and
     *     the gaps show a hole in the world instead of what is behind them.
     * <li>{@code noLootTable} — ⚠ nothing drops on the floor, ever. What a player
     *     breaks is handed back by {@link AngelBlock#onDestroyedByPlayer} instead. The
     *     two go together: a self-drop table here would give one block back twice.
     *     The cost is that anything which destroys it some other way — an explosion,
     *     another mod calling {@code destroyBlock} — loses it outright. That is the
     *     same bargain the block makes by breaking instantly, and it is cheap.
     * <li>{@code SCAFFOLDING} — borrowed rather than chosen. It is the sound vanilla
     *     already uses for the block you put down to stand on and take back, which is
     *     what this is; picking anything else would only make it sound like something
     *     it does not behave like.
     * <li>{@code ICE} — the map colour, and not picked by eye either. {@code
     *     tools/make_textures.py} holds the one tone the sprite is built from and
     *     prints the vanilla map colour nearest it; this is what it printed. Move the
     *     tone far enough and the script names a different winner, which is the only
     *     way the map and the block stay in agreement without anybody checking.
     * </ul>
     */
    public static final DeferredBlock<AngelBlock> ANGEL_BLOCK = BLOCKS.registerBlock(
            ANGEL_BLOCK_ID,
            AngelBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .instabreak()
                    .noOcclusion()
                    .noLootTable()
                    .sound(SoundType.SCAFFOLDING));

    public static final DeferredItem<AngelBlockItem> ANGEL_BLOCK_ITEM = ITEMS.register(
            ANGEL_BLOCK_ID,
            // Plain properties: BlockItem already answers getDescriptionId() with the
            // block's, so there is no name to set here and nothing else to say.
            () -> new AngelBlockItem(ANGEL_BLOCK.get(), new Item.Properties()));

    private AngelusRegistry() {
    }

    /** The block, as a plain {@link Block}, for the places that do not care which one it is. */
    public static Block block() {
        return ANGEL_BLOCK.get();
    }
}
