package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.config.server.ExplosionConfig;

import tech.vvp.vvp.VVP;
import com.atsuishio.superbwarfare.config.server.VehicleConfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.init.*;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import com.atsuishio.superbwarfare.tools.OBB;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.atsuishio.superbwarfare.tools.VectorTool;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector4d;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// Импортируем необходимые классы для атрибутов
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;

public class BikeredEntity extends GeoVehicleEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BikeredEntity(EntityType<BikeredEntity> type, Level world) {
        super(type, world);
        this.setMaxUpStep(1.5f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "wheel_controller", 0, this::wheelPredicate));
    }

    private PlayState wheelPredicate(AnimationState<BikeredEntity> event) {
        if (Mth.abs((float)this.getDeltaMovement().horizontalDistanceSqr()) > 0.001 || Mth.abs(this.entityData.get(POWER)) > 0.05) {
            float power = this.entityData.get(POWER);
            double speed = Math.abs(power) * 10.0;
            if (speed < 0.1) speed = 1.0;
            event.getController().setAnimationSpeed(speed);
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.bike.new"));
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public ResourceLocation getVehicleIcon() {
        return VVP.loc("textures/vehicle_icon/bikered_icon.png");
    }

    @Override
    public Vec3 getThirdPersonCameraPosition() {
        return new Vec3(2.75, 1, 0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        // Добавьте здесь любые дополнительные данные для сохранения, если они есть
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // Восстановите здесь любые дополнительные данные, если они есть
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pState) {
        // Sound removed - WHEEL_STEP not available
    }

    @Override
    public void baseTick() {
        turretYRotO = this.getTurretYRot();
        turretXRotO = this.getTurretXRot();
        rudderRotO = this.getRudderRot();
        leftWheelRotO = this.getLeftWheelRot();
        rightWheelRotO = this.getRightWheelRot();

        super.baseTick();
        this.updateOBB();

        if (this.onGround()) {
            float f0 = 0.50f + 0.28f * Mth.abs(90 - (float) VectorTool.calculateAngle(this.getDeltaMovement(), this.getViewVector(1))) / 90;
            this.setDeltaMovement(this.getDeltaMovement().add(this.getViewVector(1).normalize().scale(0.06 * this.getDeltaMovement().horizontalDistance())));
            this.setDeltaMovement(this.getDeltaMovement().multiply(f0, 0.85, f0));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.985, 0.95, 0.985));
        }


        this.refreshDimensions();
    }

    @Override
    public void travel() {
        Entity passenger0 = this.getFirstPassenger();

        if (this.getEnergy() <= 0) return;

        if (passenger0 == null) {
            setLeftInputDown(false);
            setRightInputDown(false);
            setForwardInputDown(false);
            setBackInputDown(false);
            this.entityData.set(POWER, 0f);
        }

        if (forwardInputDown()) {
            this.entityData.set(POWER, Math.min(this.entityData.get(POWER) + (this.entityData.get(POWER) < 0 ? 0.016f : 0.0055f), 0.28f));
        }

        if (backInputDown()) {
            this.entityData.set(POWER, Math.max(this.entityData.get(POWER) - (this.entityData.get(POWER) > 0 ? 0.016f : 0.0055f), -0.18f));
        }

        if (rightInputDown()) {
            this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) + 0.16f);
        } else if (this.leftInputDown()) {
            this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) - 0.16f);
        }

        if (this.forwardInputDown() || this.backInputDown()) {
            this.consumeEnergy(1);
        }

        this.entityData.set(POWER, this.entityData.get(POWER) * (upInputDown() ? 0.5f : (rightInputDown() || leftInputDown()) ? 0.96f : 0.989f));
        this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) * (float) Math.max(0.78f - 0.09f * this.getDeltaMovement().horizontalDistance(), 0.4f));

        float angle = (float) VectorTool.calculateAngle(this.getDeltaMovement(), this.getViewVector(1));
        double s0;

        if (Mth.abs(angle) < 90) {
            s0 = this.getDeltaMovement().horizontalDistance();
        } else {
            s0 = -this.getDeltaMovement().horizontalDistance();
        }

        this.setLeftWheelRot((float) ((this.getLeftWheelRot() - 1.25 * s0) - this.getDeltaMovement().horizontalDistance() * Mth.clamp(1.5f * this.entityData.get(DELTA_ROT), -5f, 5f)));
        this.setRightWheelRot((float) ((this.getRightWheelRot() - 1.25 * s0) + this.getDeltaMovement().horizontalDistance() * Mth.clamp(1.5f * this.entityData.get(DELTA_ROT), -5f, 5f)));

        this.setRudderRot(Mth.clamp(this.getRudderRot() - this.entityData.get(DELTA_ROT), -0.8f, 0.8f) * 0.70f);

        this.setYRot((float) (this.getYRot() - Math.max(11 * this.getDeltaMovement().horizontalDistance(), 0) * this.getRudderRot() * (this.entityData.get(POWER) > 0 ? 1 : -1)));
        if (onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(getViewVector(1).scale(this.entityData.get(POWER))));
        }
    }


    @Override
    public SoundEvent getEngineSound() {
        return tech.vvp.vvp.init.ModSounds.BTR_80A_ENGINE.get();
    }

    @Override
    public float getEngineSoundVolume() {
        return Mth.abs(entityData.get(POWER)) * 2f;
    }

    /**
     * Server-safe position transformation (avoids client-only CameraTool)
     */
    private Vector4d transformPositionSafe(Matrix4d transform, double x, double y, double z) {
        Vector4d pos = new Vector4d(x, y, z, 1.0);
        transform.transform(pos);
        return pos;
    }

    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) {
            return;
        }

        Matrix4d transform = getVehicleTransform(1);

        int i = this.getSeatIndex(passenger);
        Vector4d worldPosition;

        switch(i) {
            case 0: // Водитель (слева спереди)
                worldPosition = transformPositionSafe(transform, -0.f, 0.30f, -0.3f);
                break;
            case 1: // Пассажир (взади)
                worldPosition = transformPositionSafe(transform, 0.f, 0.30f, -0.9f);
                break;
            default:
                worldPosition = transformPositionSafe(transform, 0, 1, 0);
                break;
        }

        passenger.setPos(worldPosition.x, worldPosition.y, worldPosition.z);
        callback.accept(passenger, worldPosition.x, worldPosition.y, worldPosition.z);
    }

    @Override
    public int getMaxPassengers() {
        return 2; // Водитель + 3 пассажира (типичная компоновка седана)
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        // Ничего не делаем здесь, чтобы предотвратить вращение турели при повороте головы пассажира
    }



    // Все методы вооружения унаследованы от VehicleEntity



    @OnlyIn(Dist.CLIENT)
    @Override
    public @Nullable Vec2 getCameraRotation(float partialTicks, Player player, boolean zoom, boolean isFirstPerson) {
        if (isFirstPerson) {
            return new Vec2(Mth.lerp(partialTicks, player.yHeadRotO, player.getYHeadRot()),
                    Mth.lerp(partialTicks, player.xRotO, player.getXRot()));
        }
        return super.getCameraRotation(partialTicks, player, false, false);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getCameraPosition(float partialTicks, Player player, boolean zoom, boolean isFirstPerson) {
        if (isFirstPerson) {
            // В режиме от первого лица камера находится примерно на уровне глаз
            return new Vec3(Mth.lerp(partialTicks, player.xo, player.getX()),
                    Mth.lerp(partialTicks, player.yo + player.getEyeHeight(), player.getEyeY()),
                    Mth.lerp(partialTicks, player.zo, player.getZ()));
        }
        return super.getCameraPosition(partialTicks, player, false, false);
    }
}