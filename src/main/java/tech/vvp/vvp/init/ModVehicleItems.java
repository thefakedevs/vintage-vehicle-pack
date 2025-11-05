package tech.vvp.vvp.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.VehicleSpawnItem;

@SuppressWarnings("unused")
public class ModVehicleItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VVP.MOD_ID);

    public static final RegistryObject<Item> BIKEGREEN_SPAWN_ITEM = ITEMS.register("bikegreen_spawn_item",
            () -> new VehicleSpawnItem(() -> ModEntities.BIKEGREEN.get(), new Item.Properties()));

    public static final RegistryObject<Item> BIKERED_SPAWN_ITEM = ITEMS.register("bikered_spawn_item",
            () -> new VehicleSpawnItem(() -> ModEntities.BIKERED.get(), new Item.Properties()));

    public static final RegistryObject<Item> VAZIK_SPAWN_ITEM = ITEMS.register("vazik_spawn_item",
            () -> new VehicleSpawnItem(() -> ModEntities.VAZIK.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
} 