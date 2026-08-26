package io.github.capsicum0907.angelus;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import org.slf4j.Logger;

/**
 * Entry point. {@link #MODID} must match {@code mod_id} in gradle.properties,
 * which is what the generated neoforge.mods.toml is filled from.
 *
 * <p>There is no config. The one number this mod could have exposed is how far
 * in front of you the block appears, and that is not a setting here — it is read
 * off {@link net.minecraft.world.entity.ai.attributes.Attributes#BLOCK_INTERACTION_RANGE},
 * the same attribute that decides how far away you can place an ordinary block.
 * See {@link AngelBlockItem#use}. A slider would only let the two disagree.
 */
@Mod(Angelus.MODID)
public class Angelus {
    public static final String MODID = "angelus";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Angelus(IEventBus modEventBus, ModContainer modContainer) {
        AngelusRegistry.BLOCKS.register(modEventBus);
        AngelusRegistry.ITEMS.register(modEventBus);
        modEventBus.register(this);

        LOGGER.info("Angelus {} loaded.", modContainer.getModInfo().getVersion());
    }

    /**
     * Filed with scaffolding rather than with the building materials. Both are put
     * down to stand on and taken back afterwards, and neither is what anybody wants
     * the finished thing to be made of.
     */
    @SubscribeEvent
    public void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(AngelusRegistry.ANGEL_BLOCK_ITEM.get());
        }
    }
}
