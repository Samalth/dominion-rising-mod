package com.example.dominionrising.forge.client.events;

import com.example.dominionrising.forge.client.renderer.UnitEntityRenderer;
import com.example.dominionrising.forge.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles client-side entity renderer registration for Forge
 */
@Mod.EventBusSubscriber(modid = "dominionrising", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEntityRendererEvents {

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NATION_UNIT.get(), UnitEntityRenderer::new);
    }
}