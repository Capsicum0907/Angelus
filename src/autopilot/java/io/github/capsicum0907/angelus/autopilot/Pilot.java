package io.github.capsicum0907.angelus.autopilot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Pilot {
    private static final Logger LOGGER = LogManager.getLogger("autopilot");
    private static final int CONDITION_TIMEOUT_TICKS = 20 * 120;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final Path out;
    private final List<Step> steps = new ArrayList<>();
    private final List<String> results = new ArrayList<>();
    private int index;
    private int wait;
    private int waited;
    private boolean finished;

    private static final class Step {
        private final String name;
        private final BooleanSupplier until;
        private final Runnable action;
        private final int ticksAfter;

        private Step(String name, BooleanSupplier until, Runnable action, int ticksAfter) {
            this.name = name;
            this.until = until;
            this.action = action;
            this.ticksAfter = ticksAfter;
        }
    }

    public Pilot(Path out, long timeoutSeconds) {
        this.out = out;
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(timeoutSeconds * 1000L);
            } catch (InterruptedException e) {
                return;
            }
            synchronized (this) {
                if (!this.finished) {
                    this.results.add("FAIL watchdog: no result after " + timeoutSeconds + " s, at step " + stepName());
                    write();
                    Runtime.getRuntime().halt(3);
                }
            }
        }, "autopilot-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    public Minecraft minecraft() {
        return this.minecraft;
    }

    public Pilot until(String name, BooleanSupplier condition) {
        this.steps.add(new Step(name, condition, () -> {
        }, 0));
        return this;
    }

    public Pilot then(String name, int ticksAfter, Runnable action) {
        this.steps.add(new Step(name, () -> true, action, ticksAfter));
        return this;
    }

    public synchronized void tick() {
        if (this.finished) {
            return;
        }
        if (this.wait > 0) {
            this.wait--;
            return;
        }
        if (this.index >= this.steps.size()) {
            finish();
            return;
        }
        Step step = this.steps.get(this.index);
        try {
            if (!step.until.getAsBoolean()) {
                if (++this.waited > CONDITION_TIMEOUT_TICKS) {
                    this.results.add("FAIL " + step.name + ": condition never held, the screen was "
                            + (this.minecraft.screen == null ? "none" : this.minecraft.screen.getClass().getName()));
                    finish();
                }
                return;
            }
            this.waited = 0;
            LOGGER.info("autopilot step: {}", step.name);
            step.action.run();
        } catch (Throwable thrown) {
            LOGGER.error("autopilot step {} threw", step.name, thrown);
            this.results.add("FAIL " + step.name + ": " + thrown);
            finish();
            return;
        }
        this.index++;
        this.wait = step.ticksAfter;
    }

    public void check(String name, boolean passed, String detail) {
        this.results.add((passed ? "PASS " : "FAIL ") + name + (passed ? "" : ": " + detail));
    }

    public void screenshot(String name) {
        //? if >=1.17 {
        Screenshot.grab(this.minecraft.gameDirectory, "autopilot-" + name + ".png",
                this.minecraft.getMainRenderTarget(), message -> {
                });
        //?} else {
        /*Screenshot.grab(this.minecraft.gameDirectory, "autopilot-" + name + ".png",
                this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(),
                this.minecraft.getMainRenderTarget(), message -> {
                });
        *///?}
        this.results.add("SHOT autopilot-" + name + ".png");
    }

    public void openWorld(String name) {
        //? if >=1.20.5 {
        this.minecraft.createWorldOpenFlows().openWorld(name,
                () -> check("open " + name, false, "the world could not be opened"));
        //?} else if >=1.19 {
        /*this.minecraft.createWorldOpenFlows().loadLevel(this.minecraft.screen, name);
        *///?} else
        /*this.minecraft.loadLevel(name);*/
    }

    public void press(KeyMapping key) {
        KeyMapping.click(InputConstants.getKey(key.saveString()));
    }

    public void command(String command) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        onServer(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            //? if >=1.19 {
            server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), command);
            //?} else {
            /*server.getCommands().performCommand(
                    player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), command);
            *///?}
            return null;
        });
    }

    public <T> T onServer(Supplier<T> work) {
        return this.minecraft.getSingleplayerServer().submit(work).join();
    }

    public ServerPlayer serverPlayer() {
        return this.minecraft.getSingleplayerServer().getPlayerList().getPlayers().get(0);
    }

    private String stepName() {
        return this.index < this.steps.size() ? this.steps.get(this.index).name : "end";
    }

    private synchronized void finish() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        write();
        this.minecraft.stop();
    }

    private void write() {
        boolean passed = this.results.stream().noneMatch(line -> line.startsWith("FAIL"))
                && this.index >= this.steps.size();
        List<String> lines = new ArrayList<>(this.results);
        lines.add(passed ? "RESULT PASS" : "RESULT FAIL");
        try {
            Files.createDirectories(this.out.getParent());
            Files.write(this.out, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Could not write {}", this.out, e);
        }
        lines.forEach(line -> LOGGER.info("autopilot {}", line));
    }
}
