package io.github.capsicum0907.angelus;

//? if forge && <1.19 {
/*import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class RenderLayers {
    private RenderLayers() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(
                () -> ItemBlockRenderTypes.setRenderLayer(AngelusRegistry.block(), RenderType.cutout())));
    }
}
*///?} else if fabric {
/*import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public final class RenderLayers implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(AngelusRegistry.block(), RenderType.cutout());
    }
}
*///?}
