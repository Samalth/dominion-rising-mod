package com.example.dominionrising.neoforge.client.events;

import com.example.dominionrising.neoforge.client.renderer.UnitEntityRenderer;
import com.example.dominionrising.neoforge.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Handles client-side entity renderer registration for NeoForge
 */
@EventBusSubscriber(modid = "dominionrising", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEntityRendererEvents {

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NATION_UNIT.get(), UnitEntityRenderer::new);
    }
}