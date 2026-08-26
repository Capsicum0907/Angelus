package io.github.capsicum0907.angelus.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;

import io.github.capsicum0907.angelus.Angelus;

import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

/**
 * The stage the game tests run on: a short ledge, and open air off the end of it.
 *
 * <p>A game test needs a structure to be placed in, and there is no empty one to
 * borrow. Writing the NBT here rather than checking a binary into the repository
 * keeps the rule that generated files are generated — and the data version comes
 * from the game itself, so it cannot drift out of date silently.
 *
 * <p>The ledge is short on purpose. A full floor would put a block under every
 * place the tests aim at, and then "it was placed with nothing to place it
 * against" could not be told apart from "it was placed against the floor" — which
 * is the entire claim being tested. Everything past {@link #LEDGE} is air, so a
 * block that appears out there appeared unsupported.
 */
public class TestStructures implements DataProvider {
    /** Referenced by {@code @GameTest(template = ...)}. */
    public static final String LEDGE_AND_AIR = "ledge_and_air";

    /**
     * Wide enough to hold a stood-back player and everything within their reach.
     *
     * <p>Public because the tests sweep the whole stage when something is not where
     * they expected it, and a "not found here" that cannot say where it did find it is
     * half a failure message.
     */
    public static final int WIDTH = 12;
    public static final int HEIGHT = 6;
    public static final int DEPTH = 7;

    /** The last x that has floor under it. Beyond this there is nothing. */
    public static final int LEDGE = 3;

    private static final String FLOOR = "minecraft:polished_andesite";
    private static final String AIR = "minecraft:air";

    private final PackOutput.PathProvider path;

    public TestStructures(PackOutput output) {
        this.path = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
    }

    @Override
    public String getName() {
        return "Test Structures: " + Angelus.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path target = path.file(
                ResourceLocation.fromNamespaceAndPath(Angelus.MODID, LEDGE_AND_AIR), "nbt");
        return CompletableFuture.runAsync(() -> write(output, stage(), target), Util.backgroundExecutor());
    }

    private static CompoundTag stage() {
        CompoundTag tag = new CompoundTag();
        NbtUtils.addCurrentDataVersion(tag);
        tag.put("size", vector(WIDTH, HEIGHT, DEPTH));

        ListTag palette = new ListTag();
        palette.add(named(AIR));
        palette.add(named(FLOOR));
        tag.put("palette", palette);

        // Every cell is listed, air included: an omitted cell is left as whatever was
        // already there, which would let one test leave a block behind for the next.
        ListTag blocks = new ListTag();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    CompoundTag block = new CompoundTag();
                    block.put("pos", vector(x, y, z));
                    block.putInt("state", y == 0 && x <= LEDGE ? 1 : 0);
                    blocks.add(block);
                }
            }
        }
        tag.put("blocks", blocks);
        tag.put("entities", new ListTag());
        return tag;
    }

    private static ListTag vector(int x, int y, int z) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(x));
        list.add(IntTag.valueOf(y));
        list.add(IntTag.valueOf(z));
        return list;
    }

    private static CompoundTag named(String block) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", block);
        return tag;
    }

    @SuppressWarnings("deprecation") // Hashing.sha1 is what CachedOutput expects
    private static void write(CachedOutput output, CompoundTag tag, Path target) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, buffer);
            byte[] bytes = buffer.toByteArray();
            output.writeIfNeeded(target, bytes, Hashing.sha1().hashBytes(bytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
