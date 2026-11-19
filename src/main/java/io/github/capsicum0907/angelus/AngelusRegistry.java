package io.github.capsicum0907.angelus;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
//? if >=1.20 {
import net.minecraft.world.level.material.MapColor;
//?} else {
/*import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
*///?}
//? if neoforge {
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
//?} else if forge {
/*import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
//? if >=1.17 {
import net.minecraftforge.registries.RegistryObject;
//?} else
/^import net.minecraftforge.fml.RegistryObject;^/
*///?} else if fabric {
/*import java.util.function.Supplier;
*///?}

/**
 * The whole of the mod's contents: one block and the item that places it.
 *
 * <p>The id is {@code angel_block} rather than {@code angelus}, because the name
 * a player types into a recipe search is the one the block has had in every mod
 * that ever shipped it. The Latin is the mod's name, not the block's.
 */
public final class AngelusRegistry {
    //? if neoforge {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Angelus.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Angelus.MODID);
    //?} else if forge {
    /*public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Angelus.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Angelus.MODID);
    *///?} else if fabric && >=1.19.3 {
    /*public static final Registrar<Block> BLOCKS = new Registrar<>(net.minecraft.core.registries.BuiltInRegistries.BLOCK);
    public static final Registrar<Item> ITEMS = new Registrar<>(net.minecraft.core.registries.BuiltInRegistries.ITEM);
    *///?} else if fabric {
    /*public static final Registrar<Block> BLOCKS = new Registrar<>(net.minecraft.core.Registry.BLOCK);
    public static final Registrar<Item> ITEMS = new Registrar<>(net.minecraft.core.Registry.ITEM);
    *///?}

    public static final String ANGEL_BLOCK_ID = "angel_block";

    /**
     * Wool's sounds, at a quarter of their loudness.
     *
     * <p>Wool because the block is put down and taken back once per step, and a sharp
     * sound is only tolerable when it is occasional. Its samples are the softest
     * vanilla has for a solid block — a muffled thud with no attack on it.
     *
     * <p>Quarter volume because wool alone does not make it quieter: every vanilla
     * sound type carries volume 1.0, so swapping the type changes the sample and
     * nothing else. Only this number moves the level.
     *
     * <p>⚠ It does not move it linearly. Placing and breaking are played at
     * {@code (volume + 1) / 2}, so 0.25 lands at 0.63 against every other block's
     * 1.0, and even 0.0 would only reach 0.5 — half is the floor without replacing
     * the sound events themselves. Footsteps are the other way, played at
     * {@code volume * 0.15} with no floor, which is why this is not 0.0: silent to
     * stand on reads as a bug rather than as quiet.
     *
     * <p>The events are read off {@link SoundType#WOOL} rather than named again, so
     * the one thing that is ours here is the number.
     */
    private static final SoundType QUIET_WOOL = new SoundType(0.25F, SoundType.WOOL.getPitch(),
            SoundType.WOOL.getBreakSound(), SoundType.WOOL.getStepSound(),
            SoundType.WOOL.getPlaceSound(), SoundType.WOOL.getHitSound(),
            SoundType.WOOL.getFallSound());

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
     * <li>{@link #QUIET_WOOL} — a block placed and broken this often is
     *     heard more than any other in the game, so it is the one block whose sound
     *     should be the least like an event.
     * <li>{@code ICE} — the map colour, and not picked by eye either. {@code
     *     tools/make_textures.py} holds the one tone the sprite is built from and
     *     prints the vanilla map colour nearest it; this is what it printed. Move the
     *     tone far enough and the script names a different winner, which is the only
     *     way the map and the block stay in agreement without anybody checking.
     * </ul>
     */
    //? if neoforge {
    public static final DeferredBlock<AngelBlock> ANGEL_BLOCK = BLOCKS.registerBlock(
            ANGEL_BLOCK_ID,
            AngelBlock::new,
    //?} else if forge {
    /*public static final RegistryObject<AngelBlock> ANGEL_BLOCK = BLOCKS.register(
            ANGEL_BLOCK_ID,
            () -> new AngelBlock(
    *///?} else if fabric {
    /*public static final Supplier<AngelBlock> ANGEL_BLOCK = BLOCKS.register(
            ANGEL_BLOCK_ID,
            () -> new AngelBlock(
    *///?}
            //? if >=1.20 {
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
            //?} else
            /*BlockBehaviour.Properties.of(Material.BUILDABLE_GLASS, MaterialColor.QUARTZ)*/
                    .instabreak()
                    .noOcclusion()
                    //? if >=1.19 {
                    .noLootTable()
                    //?} else
                    /*.noDrops()*/
                    .sound(QUIET_WOOL)
    //? if !neoforge
    /*)*/
            );

    //? if neoforge {
    public static final DeferredItem<AngelBlockItem> ANGEL_BLOCK_ITEM = ITEMS.register(
    //?} else if forge {
    /*public static final RegistryObject<AngelBlockItem> ANGEL_BLOCK_ITEM = ITEMS.register(
    *///?} else
    /*public static final Supplier<AngelBlockItem> ANGEL_BLOCK_ITEM = ITEMS.register(*/
            ANGEL_BLOCK_ID,
            // Plain properties: BlockItem already answers getDescriptionId() with the
            // block's, so there is no name to set here and nothing else to say.
            () -> new AngelBlockItem(ANGEL_BLOCK.get(), new Item.Properties()
                    //? if <1.19.3
                    /*.tab(CreativeModeTab.TAB_DECORATIONS)*/
            ));

    private AngelusRegistry() {
    }

    /** The block, as a plain {@link Block}, for the places that do not care which one it is. */
    public static Block block() {
        return ANGEL_BLOCK.get();
    }
}
