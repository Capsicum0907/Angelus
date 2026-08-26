package io.github.capsicum0907.angelus.data;

import java.util.concurrent.CompletableFuture;

import io.github.capsicum0907.angelus.Angelus;
import io.github.capsicum0907.angelus.AngelusRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Everything under {@code src/generated/resources} comes from here, so nothing in
 * that directory is written by hand.
 *
 * <p>No loot table, and that is deliberate rather than missing: the block declares
 * {@code noLootTable()} and hands itself back through
 * {@link io.github.capsicum0907.angelus.AngelBlock#onDestroyedByPlayer} instead. A
 * file here would give the block back twice.
 *
 * <p>No block tags either. Mining tags say which tool is right and how good it has
 * to be, and this block breaks instantly with a bare hand, so there is no question
 * for a tag to answer.
 */
@EventBusSubscriber(modid = Angelus.MODID, value = { Dist.CLIENT, Dist.DEDICATED_SERVER })
public final class AngelusDataGen {
    private AngelusDataGen() {
    }

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new Models(output, helper));
        generator.addProvider(event.includeClient(), new Language(output));
        generator.addProvider(event.includeServer(), new Recipes(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new TestStructures(output));
    }

    private static class Models extends BlockStateProvider {
        Models(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Angelus.MODID, existingFileHelper);
        }

        /**
         * An ordinary six-sided cube, with one thing said about it: cutout.
         *
         * <p>⚠ Without the render type the sprite's transparent pixels are drawn as
         * opaque black, because the solid layer has no alpha test to skip them. This is
         * one of three things that have to agree for the block to be seen through — the
         * others are the alpha actually being in the PNG, and {@code noOcclusion()} on
         * the block so the faces behind it are not culled away. Any one of them missing
         * looks like a different bug.
         */
        @Override
        protected void registerStatesAndModels() {
            String name = AngelusRegistry.ANGEL_BLOCK_ID;
            simpleBlockWithItem(
                    AngelusRegistry.block(),
                    models().cubeAll(name, modLoc("block/" + name)).renderType("cutout"));
        }
    }

    private static class Language extends LanguageProvider {
        Language(PackOutput output) {
            super(output, Angelus.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(AngelusRegistry.block(), "Angel Block");
        }
    }

    /**
     * Four feathers at the corners, four sticks between them, nothing in the middle.
     *
     * <p>The shape is the one this block has been made with since Extra Utilities, and
     * that is the reason to keep it: a player who has seen an angel block before knows
     * the recipe without looking it up, and there is nothing to gain by making them
     * look it up.
     *
     * <p>One block out, and that is not stingy. The block comes back to your hand every
     * time you break it, so a single one carries a walkway as far as you care to walk.
     * Costing more would only be a toll on the first one.
     */
    private static class Recipes extends RecipeProvider {
        Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput output) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AngelusRegistry.block())
                    .pattern("FSF")
                    .pattern("S S")
                    .pattern("FSF")
                    .define('F', Items.FEATHER)
                    .define('S', Items.STICK)
                    .unlockedBy("has_feather", has(Items.FEATHER))
                    .save(output);
        }
    }
}
