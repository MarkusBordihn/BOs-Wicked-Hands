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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public final class ModEntityTypes {

  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
      DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

  public static final DeferredHolder<EntityType<?>, EntityType<RottenHandEntity>> ROTTEN_HAND =
      ENTITY_TYPES.register(RottenHandEntity.ID, RottenHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessRottenHandEntity>>
      LIFELESS_ROTTEN_HAND =
          ENTITY_TYPES.register(
              LifelessRottenHandEntity.ID, LifelessRottenHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<DrownedHandEntity>> DROWNED_HAND =
      ENTITY_TYPES.register(DrownedHandEntity.ID, DrownedHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessDrownedHandEntity>>
      LIFELESS_DROWNED_HAND =
          ENTITY_TYPES.register(
              LifelessDrownedHandEntity.ID, LifelessDrownedHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<BoneHandEntity>> BONE_HAND =
      ENTITY_TYPES.register(BoneHandEntity.ID, BoneHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessBoneHandEntity>>
      LIFELESS_BONE_HAND =
          ENTITY_TYPES.register(
              LifelessBoneHandEntity.ID, LifelessBoneHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<HuskHandEntity>> HUSK_HAND =
      ENTITY_TYPES.register(HuskHandEntity.ID, HuskHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessHuskHandEntity>>
      LIFELESS_HUSK_HAND =
          ENTITY_TYPES.register(
              LifelessHuskHandEntity.ID, LifelessHuskHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<StrayHandEntity>> STRAY_HAND =
      ENTITY_TYPES.register(StrayHandEntity.ID, StrayHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessStrayHandEntity>>
      LIFELESS_STRAY_HAND =
          ENTITY_TYPES.register(
              LifelessStrayHandEntity.ID, LifelessStrayHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<WitheredHandEntity>> WITHERED_HAND =
      ENTITY_TYPES.register(WitheredHandEntity.ID, WitheredHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessWitheredHandEntity>>
      LIFELESS_WITHERED_HAND =
          ENTITY_TYPES.register(
              LifelessWitheredHandEntity.ID, LifelessWitheredHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<BoggedHandEntity>> BOGGED_HAND =
      ENTITY_TYPES.register(BoggedHandEntity.ID, BoggedHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessBoggedHandEntity>>
      LIFELESS_BOGGED_HAND =
          ENTITY_TYPES.register(
              LifelessBoggedHandEntity.ID, LifelessBoggedHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<WitchHandEntity>> WITCH_HAND =
      ENTITY_TYPES.register(WitchHandEntity.ID, WitchHandEntity::createEntityType);
  public static final DeferredHolder<EntityType<?>, EntityType<LifelessWitchHandEntity>>
      LIFELESS_WITCH_HAND =
          ENTITY_TYPES.register(
              LifelessWitchHandEntity.ID, LifelessWitchHandEntity::createEntityType);

  private ModEntityTypes() {}

  @SubscribeEvent
  public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
    event.put(ROTTEN_HAND.get(), RottenHandEntity.createAttributes().build());
    event.put(DROWNED_HAND.get(), DrownedHandEntity.createAttributes().build());
    event.put(BONE_HAND.get(), BoneHandEntity.createAttributes().build());
    event.put(HUSK_HAND.get(), HuskHandEntity.createAttributes().build());
    event.put(STRAY_HAND.get(), StrayHandEntity.createAttributes().build());
    event.put(WITHERED_HAND.get(), WitheredHandEntity.createAttributes().build());
    event.put(BOGGED_HAND.get(), BoggedHandEntity.createAttributes().build());
    event.put(WITCH_HAND.get(), WitchHandEntity.createAttributes().build());
    AttributeSupplier lifelessHandAttributes =
        AbstractLifelessHandEntity.createAttributes().build();
    event.put(LIFELESS_ROTTEN_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_DROWNED_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_BONE_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_HUSK_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_STRAY_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_WITHERED_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_BOGGED_HAND.get(), lifelessHandAttributes);
    event.put(LIFELESS_WITCH_HAND.get(), lifelessHandAttributes);
  }

  @SubscribeEvent
  public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
    event.register(
        ROTTEN_HAND.get(),
        RottenHandEntity.SPAWN_PLACEMENT_TYPE,
        RottenHandEntity.SPAWN_HEIGHTMAP,
        RottenHandEntity::checkRottenHandSpawnRules,
        RegisterSpawnPlacementsEvent.Operation.REPLACE);
  }
}
