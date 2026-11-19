package io.github.capsicum0907.angelus;

//? if >=1.18
import com.mojang.logging.LogUtils;

//? if >=1.20
import net.minecraft.world.item.CreativeModeTabs;
//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
//?} else if forge {
/*//? if >=1.20
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?} else if fabric {
/*//? if >=1.20
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
*///?}

//? if >=1.18 {
import org.slf4j.Logger;
//?} else {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?}

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
//? if fabric {
/*public class Angelus implements ModInitializer {
*///?} else {
@Mod(Angelus.MODID)
public class Angelus {
//?}
    public static final String MODID = "angelus";

    //? if >=1.18 {
    private static final Logger LOGGER = LogUtils.getLogger();
    //?} else
    /*private static final Logger LOGGER = LogManager.getLogger();*/

    //? if neoforge {
    public Angelus(IEventBus modEventBus, ModContainer modContainer) {
        AngelusRegistry.BLOCKS.register(modEventBus);
        AngelusRegistry.ITEMS.register(modEventBus);
        modEventBus.register(this);

        LOGGER.info("Angelus {} loaded.", modContainer.getModInfo().getVersion());
    }
    //?} else if forge {
    /*public Angelus() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AngelusRegistry.BLOCKS.register(modEventBus);
        AngelusRegistry.ITEMS.register(modEventBus);
        //? if <1.19 {
        /^if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            RenderLayers.register(modEventBus);
        }
        ^///?}
        //? if >=1.20
        modEventBus.register(this);

        LOGGER.info("Angelus {} loaded.", ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }
    *///?} else if fabric {
    /*@Override
    public void onInitialize() {
        AngelusRegistry.BLOCKS.register();
        AngelusRegistry.ITEMS.register();
        //? if >=1.20
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> entries.accept(AngelusRegistry.ANGEL_BLOCK_ITEM.get()));

        LOGGER.info("Angelus {} loaded.", FabricLoader.getInstance().getModContainer(MODID)
                .orElseThrow(IllegalStateException::new).getMetadata().getVersion().getFriendlyString());
    }
    *///?}

    /**
     * Filed with scaffolding rather than with the building materials. Both are put
     * down to stand on and taken back afterwards, and neither is what anybody wants
     * the finished thing to be made of.
     */
    //? if >=1.20 && !fabric {
    @SubscribeEvent
    public void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(AngelusRegistry.ANGEL_BLOCK_ITEM.get());
        }
    }
    //?}
}
