package tech.vvp.vvp.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.VazikEntity;

public class VazikModel extends GeoModel<VazikEntity> {
    @Override
    public ResourceLocation getModelResource(VazikEntity animatable) {
        return VVP.loc("geo/vazik.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VazikEntity animatable) {
        return VVP.loc("textures/entity/vazik.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VazikEntity animatable) {
        return VVP.loc("animations/vaz.animation.json");
    }
}
