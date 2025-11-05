package tech.vvp.vvp.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.BikeredEntity;

public class BikeredModel extends GeoModel<BikeredEntity> {
    @Override
    public ResourceLocation getModelResource(BikeredEntity animatable) {
        return VVP.loc("geo/bike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BikeredEntity animatable) {
        return VVP.loc("textures/entity/bikered.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BikeredEntity animatable) {
        // Возвращаем null если анимации нет
        return null;
    }
}
