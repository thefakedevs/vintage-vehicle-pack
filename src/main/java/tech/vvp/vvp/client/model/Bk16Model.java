package tech.vvp.vvp.client.model;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.Bk16Entity;

public class Bk16Model extends VehicleModel<Bk16Entity> {

    @Override
    public ResourceLocation getModelResource(Bk16Entity object) {
        return new ResourceLocation(VVP.MOD_ID, "geo/bk_16.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Bk16Entity object) {
        return new ResourceLocation(VVP.MOD_ID, "textures/entity/bk_16.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Bk16Entity animatable) {
        return new ResourceLocation(VVP.MOD_ID, "animations/bk_16.animation.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<Bk16Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.propellerRotO, vehicle.getPropellerRot()));
            case "rudder" ->
                    (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.rudderRotO, vehicle.getRudderRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
