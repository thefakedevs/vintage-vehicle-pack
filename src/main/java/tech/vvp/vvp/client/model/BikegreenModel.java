package tech.vvp.vvp.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.BikegreenEntity;

public class BikegreenModel extends GeoModel<BikegreenEntity> {
    @Override
    public ResourceLocation getModelResource(BikegreenEntity animatable) {
        return VVP.loc("geo/bike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BikegreenEntity animatable) {
        return VVP.loc("textures/entity/bikegreen.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BikegreenEntity animatable) {
        return VVP.loc("animations/bike.animation.json");
    }
}
