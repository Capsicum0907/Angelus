package io.github.capsicum0907.angelus.autopilot;

import java.nio.file.Path;

import io.github.capsicum0907.angelus.AngelBlockItem;
import io.github.capsicum0907.angelus.AngelusRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
//? if >=1.20.2
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class AngelusScenario {
    private static final String WORLD = System.getProperty("autopilot.world", "Test World");
    private static final long TIMEOUT_SECONDS = Long.getLong("autopilot.timeout", 300L);
    private static final String ITEM = "angelus:angel_block";
    private static final int STANDING_Y = 200;
    private static final int SETTLE = 40;
    private static final int SHORT = 10;
    private static final int ROW = 3;

    private AngelusScenario() {
    }

    public static Pilot create() {
        Minecraft minecraft = Minecraft.getInstance();
        Pilot pilot = new Pilot(minecraft.gameDirectory.toPath().resolve("autopilot").resolve("result.txt"),
                TIMEOUT_SECONDS);
        BlockPos[] placed = new BlockPos[ROW];
        int[] held = new int[1];

        pilot.until("title screen", () -> minecraft.screen instanceof TitleScreen)
                .then("title shot", SHORT, () -> pilot.screenshot("title"))
                .then("open " + WORLD, 0, () -> pilot.openWorld(WORLD))
                .until("in the world", () -> minecraft.player != null && minecraft.level != null
                        && minecraft.getSingleplayerServer() != null && minecraft.player.tickCount > SETTLE)
                .then("check the recipe", 0, () -> recipe(pilot))
                .then("fly to the spot", SETTLE, () -> {
                    pilot.command("gamerule doDaylightCycle false");
                    pilot.command("gamerule doWeatherCycle false");
                    pilot.command("time set noon");
                    pilot.command("weather clear");
                    pilot.command("gamemode creative");
                    pilot.command("tp @s 0.5 " + STANDING_Y + " 0.5 0 0");
                    pilot.onServer(() -> {
                        pilot.serverPlayer().getAbilities().flying = true;
                        pilot.serverPlayer().onUpdateAbilities();
                        return null;
                    });
                })
                .then("floor and items", SETTLE, () -> {
                    pilot.command("fill 0 " + (STANDING_Y - 1) + " 0 " + (ROW - 1) + " " + (STANDING_Y - 1)
                            + " 0 minecraft:barrier");
                    pilot.command("tp @s 0.5 " + STANDING_Y + " 0.5 0 0");
                    pilot.command("gamemode survival");
                    pilot.command("clear");
                    pilot.command("item replace entity @s weapon.mainhand with " + ITEM + " 16");
                });

        for (int i = 0; i < ROW; i++) {
            int step = i;
            pilot.then("stand for block " + step, SHORT,
                    () -> pilot.command("tp @s " + (step + 0.5) + " " + STANDING_Y + " 0.5 0 0"))
                    .then("aim block " + step, SHORT, () -> placed[step] = aimed(minecraft.player))
                    .then("use on air " + step, SHORT, () -> pilot.press(minecraft.options.keyUse))
                    .then("check block " + step, 0, () -> {
                        BlockPos pos = placed[step];
                        boolean client = minecraft.level.getBlockState(pos).is(angelBlock());
                        boolean server = pilot.onServer(() -> level(pilot.serverPlayer()).getBlockState(pos).is(angelBlock()));
                        pilot.check("placed in air at " + pos.toShortString() + " (client)", client, "no angel block there");
                        pilot.check("placed in air at " + pos.toShortString() + " (server)", server, "no angel block there");
                    });
        }

        pilot.then("view the row", SETTLE, () -> {
                    pilot.command("gamemode spectator");
                    pilot.command("tp @s -1.5 " + STANDING_Y + " 1.5 -35 10");
                    minecraft.options.hideGui = true;
                })
                .then("row shot", SHORT, () -> pilot.screenshot("row"))
                .then("back to the first", SHORT, () -> {
                    minecraft.options.hideGui = false;
                    pilot.command("fill 0 " + (STANDING_Y - 1) + " 1 0 " + (STANDING_Y - 1) + " 1 minecraft:barrier");
                    pilot.command("tp @s 0.5 " + STANDING_Y + " 1.5 0 0");
                    pilot.command("gamemode survival");
                })
                .then("count before breaking", 0, () -> held[0] = pilot.onServer(() -> count(pilot)))
                .then("break", SHORT, () -> pilot.press(minecraft.options.keyAttack))
                .then("check break", 0, () -> {
                    BlockPos pos = placed[0];
                    boolean gone = pilot.onServer(() -> level(pilot.serverPlayer()).getBlockState(pos).isAir());
                    int after = pilot.onServer(() -> count(pilot));
                    pilot.check("broken in one hit", gone, "the block is still at " + pos.toShortString());
                    pilot.check("back in the inventory", after == held[0] + 1,
                            "held " + held[0] + " before and " + after + " after");
                })
                .then("hand shot", SHORT, () -> pilot.screenshot("hand"));
        return pilot;
    }

    private static void recipe(Pilot pilot) {
        int[] found = pilot.onServer(() -> {
            MinecraftServer server = pilot.minecraft().getSingleplayerServer();
            //? if >=1.21 {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("angelus", "angel_block");
            //?} else
            /*ResourceLocation id = new ResourceLocation("angelus", "angel_block");*/
            //? if >=1.20.2 {
            Recipe<?> recipe = server.getRecipeManager().byKey(id).map(RecipeHolder::value).orElse(null);
            //?} else
            /*Recipe<?> recipe = server.getRecipeManager().byKey(id).orElse(null);*/
            if (recipe == null) {
                return null;
            }
            //? if >=1.19.4 {
            ItemStack result = recipe.getResultItem(server.registryAccess());
            //?} else
            /*ItemStack result = recipe.getResultItem();*/
            int feathers = 0;
            int sticks = 0;
            for (Ingredient ingredient : recipe.getIngredients()) {
                feathers += ingredient.test(new ItemStack(Items.FEATHER)) ? 1 : 0;
                sticks += ingredient.test(new ItemStack(Items.STICK)) ? 1 : 0;
            }
            return new int[] {feathers, sticks, result.is(AngelusRegistry.ANGEL_BLOCK_ITEM.get()) ? result.getCount() : 0};
        });
        pilot.check("recipe loaded", found != null, "no recipe angelus:angel_block on the server");
        if (found != null) {
            pilot.check("recipe takes 4 feathers and 4 sticks", found[0] == 4 && found[1] == 4,
                    found[0] + " feathers and " + found[1] + " sticks");
            pilot.check("recipe makes 1 angel block", found[2] == 1, "makes " + found[2]);
        }
    }

    private static Level level(Player player) {
        //? if >=1.20 {
        return player.level();
        //?} else
        /*return player.level;*/
    }

    private static Block angelBlock() {
        return AngelusRegistry.ANGEL_BLOCK.get();
    }

    private static int count(Pilot pilot) {
        return pilot.serverPlayer().getInventory().countItem(AngelusRegistry.ANGEL_BLOCK_ITEM.get());
    }

    private static BlockPos aimed(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 aimed = eye.add(player.getLookAngle().scale(AngelBlockItem.reach(player)));
        //? if >=1.19.4 {
        return BlockPos.containing(aimed);
        //?} else
        /*return new BlockPos(aimed);*/
    }
}
