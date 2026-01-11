package tech.vvp.vvp.client.model;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.VazikEntity;

public class VazikModel extends VehicleModel<VazikEntity> {
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

    @Override
    public @Nullable TransformContext<VazikEntity> collectTransform(String boneName) {
        switch (boneName) {
            // Front right wheel (positive X, negative Z)
            case "wheel1":
                return (bone, vehicle, state) -> {
                    float wheelRot = vehicle.getRightWheelRot();
                    bone.setRotX((float) Math.toRadians(-wheelRot));
                    bone.setRotY(vehicle.getRudderRot());
                };
            // Rear right wheel (positive X, positive Z)
            case "wheel4":
                return (bone, vehicle, state) -> {
                    float wheelRot = vehicle.getRightWheelRot();
                    bone.setRotX((float) Math.toRadians(-wheelRot));
                };
            // Rear left wheel (negative X, positive Z)
            case "wheel3":
                return (bone, vehicle, state) -> {
                    float wheelRot = vehicle.getLeftWheelRot();
                    bone.setRotX((float) Math.toRadians(-wheelRot));
                };
            // Front left wheel (negative X, negative Z)
            case "wheel2":
                return (bone, vehicle, state) -> {
                    float wheelRot = vehicle.getLeftWheelRot();
                    bone.setRotX((float) Math.toRadians(-wheelRot));
                    bone.setRotY(vehicle.getRudderRot());
                };
            default:
                return super.collectTransform(boneName);
        }
    }
}
