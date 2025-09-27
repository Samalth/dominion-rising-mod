package com.example.dominionrising.neoforge.client.renderer;

import com.example.dominionrising.neoforge.entity.UnitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for UnitEntity - uses Villager model and texture as placeholder
 */
public class UnitEntityRenderer extends MobRenderer<UnitEntity, VillagerModel<UnitEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public UnitEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UnitEntity entity) {
        return TEXTURE;
    }
}