/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.wickedhands.entity;

import de.markusbordihn.wickedhands.Constants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {

  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);

  public static final RegistryObject<EntityType<RottenHandEntity>> ROTTEN_HAND =
      ENTITY_TYPES.register(RottenHandEntity.ID, RottenHandEntity::createEntityType);
  public static final RegistryObject<EntityType<LifelessRottenHandEntity>> LIFELESS_ROTTEN_HAND =
      ENTITY_TYPES.register(
          LifelessRottenHandEntity.ID, LifelessRottenHandEntity::createEntityType);
  public static final RegistryObject<EntityType<DrownedHandEntity>> DROWNED_HAND =
      ENTITY_TYPES.register(DrownedHandEntity.ID, DrownedHandEntity::createEntityType);
  public static final RegistryObject<EntityType<LifelessDrownedHandEntity>> LIFELESS_DROWNED_HAND =
      ENTITY_TYPES.register(
          LifelessDrownedHandEntity.ID, LifelessDrownedHandEntity::createEntityType);
  public static final RegistryObject<EntityType<BoneHandEntity>> BONE_HAND =
      ENTITY_TYPES.register(BoneHandEntity.ID, BoneHandEntity::createEntityType);
  public static final RegistryObject<EntityType<LifelessBoneHandEntity>> LIFELESS_BONE_HAND =
      ENTITY_TYPES.register(LifelessBoneHandEntity.ID, LifelessBoneHandEntity::createEntityType);
  public static final RegistryObject<EntityType<HuskHandEntity>> HUSK_HAND =
      ENTITY_TYPES.register(HuskHandEntity.ID, HuskHandEntity::createEntityType);
  public static final RegistryObject<EntityType<LifelessHuskHandEntity>> LIFELESS_HUSK_HAND =
      ENTITY_TYPES.register(LifelessHuskHandEntity.ID, LifelessHuskHandEntity::createEntityType);
  public static final RegistryObject<EntityType<StrayHandEntity>> STRAY_HAND =
      ENTITY_TYPES.register(StrayHandEntity.ID, StrayHandEntity::createEntityType);
  public static final RegistryObject<EntityType<LifelessStrayHandEntity>> LIFELESS_STRAY_HAND =
      ENTITY_TYPES.register(LifelessStrayHandEntity.ID, LifelessStrayHandEntity::createEntityType);
  public static final RegistryObject<EntityType<WitheredHandEntity>> WITHERED_HAND =
      ENTITY_TYPES.register(WitheredHandEntity.ID, WitheredHandEntity::createEntityType);
  public static final RegistryObject<EntityType<LifelessWitheredHandEntity>>
      LIFELESS_WITHERED_HAND =
          ENTITY_TYPES.register(
              LifelessWitheredHandEntity.ID, LifelessWitheredHandEntity::createEntityType);

  private ModEntityTypes() {}

  public static void registerEvents() {
    EntityAttributeCreationEvent.BUS.addListener(ModEntityTypes::registerEntityAttributes);
    SpawnPlacementRegisterEvent.BUS.addListener(ModEntityTypes::registerSpawnPlacements);
  }

  private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
    event.put(ROTTEN_HAND.get(), RottenHandEntity.createAttributes().build());
    event.put(DROWNED_HAND.get(), DrownedHandEntity.createAttributes().build());
    event.put(BONE_HAND.get(), BoneHandEntity.createAttributes().build());
    event.put(HUSK_HAND.get(), HuskHandEntity.createAttributes().build());
    event.put(STRAY_HAND.get(), StrayHandEntity.createAttributes().build());
    event.put(WITHERED_HAND.get(), WitheredHandEntity.createAttributes().build());
    AttributeSupplier lifelessHandAttributes =
        AbstractLifelessHandEntity.createAttributes().build();
    event.put(LIFELESS_ROTTEN_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_DROWNED_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_BONE_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_HUSK_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_STRAY_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_WITHERED_HAND.get(), lifelessHandAttributes);
  }

  private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
    event.register(
        ROTTEN_HAND.get(),
        RottenHandEntity.SPAWN_PLACEMENT_TYPE,
        RottenHandEntity.SPAWN_HEIGHTMAP,
        RottenHandEntity::checkRottenHandSpawnRules,
        SpawnPlacementRegisterEvent.Operation.REPLACE);
  }
}
