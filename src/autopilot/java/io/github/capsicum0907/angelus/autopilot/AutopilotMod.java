package io.github.capsicum0907.angelus.autopilot;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "angelus_autopilot", dist = Dist.CLIENT)
public final class AutopilotMod {
    public AutopilotMod() {
        Pilot pilot = AngelusScenario.create();
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> pilot.tick());
    }
}
