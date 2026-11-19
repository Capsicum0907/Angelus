package io.github.capsicum0907.angelus.autopilot;

//? if neoforge {
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
//?} else if forge {
/*import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
*///?}

//? if neoforge {
@Mod(value = "angelus_autopilot", dist = Dist.CLIENT)
//?} else if forge
/*@Mod("angelus_autopilot")*/
public final class AutopilotMod {
    public AutopilotMod() {
        if (System.getProperty("autopilot.world") == null) {
            return;
        }
        Pilot pilot = AngelusScenario.create();
        //? if neoforge {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> pilot.tick());
        //?} else if forge {
        /*MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                pilot.tick();
            }
        });
        *///?}
    }
}
