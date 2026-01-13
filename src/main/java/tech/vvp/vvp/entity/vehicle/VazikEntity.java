package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.config.server.ExplosionConfig;

import tech.vvp.vvp.VVP;
import com.atsuishio.superbwarfare.config.server.VehicleConfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.init.*;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.atsuishio.superbwarfare.tools.VectorTool;

// import net.minecraft.client.Minecraft;
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

public class VazikEntity extends GeoVehicleEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // OBBs are now configured via JSON like Toyota

    public VazikEntity(EntityType<VazikEntity> type, Level world) {
        super(type, world);
        this.setMaxUpStep(1.5f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "wheel_controller", 0, this::wheelPredicate));
    }

    private PlayState wheelPredicate(AnimationState<VazikEntity> event) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public ResourceLocation getVehicleIcon() {
        return VVP.loc("textures/vehicle_icon/vazik_icon.png");
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
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .multiply(1.5f)  // Базовый урон увеличен в 2.5 раза
                .multiply(3f, DamageTypes.ARROW)
                .multiply(3f, DamageTypes.TRIDENT)
                .multiply(4f, DamageTypes.MOB_ATTACK)
                .multiply(3f, DamageTypes.MOB_ATTACK_NO_AGGRO)
                .multiply(3f, DamageTypes.MOB_PROJECTILE)
                .multiply(15f, DamageTypes.LAVA)
                .multiply(8f, DamageTypes.EXPLOSION)
                .multiply(8f, DamageTypes.PLAYER_EXPLOSION)
                .multiply(5f, ModDamageTypes.CUSTOM_EXPLOSION)
                .multiply(4f, ModDamageTypes.MINE)
                .multiply(2f, ModTags.DamageTypes.PROJECTILE)
                .multiply(2f, ModTags.DamageTypes.PROJECTILE_ABSOLUTE)
                .multiply(15f, ModDamageTypes.VEHICLE_STRIKE);
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
            float f0 = 0.56f + 0.28f * Mth.abs(90 - (float) VectorTool.calculateAngle(this.getDeltaMovement(), this.getViewVector(1))) / 90;
            this.setDeltaMovement(this.getDeltaMovement().add(this.getViewVector(1).normalize().scale(0.06 * this.getDeltaMovement().horizontalDistance())));
            this.setDeltaMovement(this.getDeltaMovement().multiply(f0, 0.85, f0));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.95, 0.98));
        }

        // Ensure wheel spin visuals while moving
        double speed = this.getDeltaMovement().horizontalDistance();
        if (speed > 0.001) {
            float spin = (float) (speed * 20.0);
            this.setLeftWheelRot(this.getLeftWheelRot() + spin);
            this.setRightWheelRot(this.getRightWheelRot() + spin);
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
            this.entityData.set(POWER, Math.min(this.entityData.get(POWER) + (this.entityData.get(POWER) < 0 ? 0.014f : 0.0036f), 0.26f));
        }

        if (backInputDown()) {
            this.entityData.set(POWER, Math.max(this.entityData.get(POWER) - (this.entityData.get(POWER) > 0 ? 0.014f : 0.0036f), -0.15f));
        }

        if (rightInputDown()) {
            this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) + 0.15f);
        } else if (this.leftInputDown()) {
            this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) - 0.15f);
        }

        if (this.forwardInputDown() || this.backInputDown()) {
            this.consumeEnergy(1);
        }

        this.entityData.set(POWER, this.entityData.get(POWER) * (upInputDown() ? 0.5f : (rightInputDown() || leftInputDown()) ? 0.965f : 0.988f));
        this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) * (float) Math.max(0.80f - 0.08f * this.getDeltaMovement().horizontalDistance(), 0.45));

        float angle = (float) VectorTool.calculateAngle(this.getDeltaMovement(), this.getViewVector(1));
        double s0;

        if (Mth.abs(angle) < 90) {
            s0 = this.getDeltaMovement().horizontalDistance();
        } else {
            s0 = -this.getDeltaMovement().horizontalDistance();
        }

        this.setLeftWheelRot((float) ((this.getLeftWheelRot() - 1.25 * s0) - this.getDeltaMovement().horizontalDistance() * Mth.clamp(1.5f * this.entityData.get(DELTA_ROT), -5f, 5f)));
        this.setRightWheelRot((float) ((this.getRightWheelRot() - 1.25 * s0) + this.getDeltaMovement().horizontalDistance() * Mth.clamp(1.5f * this.entityData.get(DELTA_ROT), -5f, 5f)));

        this.setRudderRot(Mth.clamp(this.getRudderRot() - this.entityData.get(DELTA_ROT), -0.8f, 0.8f) * 0.75f);

        this.setYRot((float) (this.getYRot() - Math.max(10 * this.getDeltaMovement().horizontalDistance(), 0) * this.getRudderRot() * (this.entityData.get(POWER) > 0 ? 1 : -1)));
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
                worldPosition = com.atsuishio.superbwarfare.tools.CameraTool.transformPosition(transform, 0.4f, 0.30f, 0.2f);
                break;
            case 1: // Пассажир рядом с водителем
                worldPosition = com.atsuishio.superbwarfare.tools.CameraTool.transformPosition(transform, -0.4f, 0.30f, 0.3f);
                break;
            case 2: // Пассажир сзади слева
                worldPosition = com.atsuishio.superbwarfare.tools.CameraTool.transformPosition(transform, 0.4f, 0.30f, -0.7f);
                break;
            case 3: // Пассажир сзади справа
                worldPosition = com.atsuishio.superbwarfare.tools.CameraTool.transformPosition(transform, -0.6f, 0.30f, -0.9f);
                break;
            default:
                worldPosition = com.atsuishio.superbwarfare.tools.CameraTool.transformPosition(transform, 0, 1, 0);
                break;
        }

        passenger.setPos(worldPosition.x, worldPosition.y, worldPosition.z);
        callback.accept(passenger, worldPosition.x, worldPosition.y, worldPosition.z);
    }

    @Override
    public int getMaxPassengers() {
        return 4; // Водитель + 3 пассажира (типичная компоновка седана)
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

    // OBB list provided by base from JSON

    // OBB updates handled by base using JSON config
}