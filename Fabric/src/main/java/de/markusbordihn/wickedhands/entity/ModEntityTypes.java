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

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;

public final class ModEntityTypes {

  public static final EntityType<RottenHandEntity> ROTTEN_HAND =
      RottenHandEntity.createEntityType();
  public static final EntityType<LifelessRottenHandEntity> LIFELESS_ROTTEN_HAND =
      LifelessRottenHandEntity.createEntityType();
  public static final EntityType<DrownedHandEntity> DROWNED_HAND =
      DrownedHandEntity.createEntityType();
  public static final EntityType<LifelessDrownedHandEntity> LIFELESS_DROWNED_HAND =
      LifelessDrownedHandEntity.createEntityType();
  public static final EntityType<BoneHandEntity> BONE_HAND = BoneHandEntity.createEntityType();
  public static final EntityType<LifelessBoneHandEntity> LIFELESS_BONE_HAND =
      LifelessBoneHandEntity.createEntityType();
  public static final EntityType<HuskHandEntity> HUSK_HAND = HuskHandEntity.createEntityType();
  public static final EntityType<LifelessHuskHandEntity> LIFELESS_HUSK_HAND =
      LifelessHuskHandEntity.createEntityType();
  public static final EntityType<StrayHandEntity> STRAY_HAND = StrayHandEntity.createEntityType();
  public static final EntityType<LifelessStrayHandEntity> LIFELESS_STRAY_HAND =
      LifelessStrayHandEntity.createEntityType();
  public static final EntityType<WitheredHandEntity> WITHERED_HAND =
      WitheredHandEntity.createEntityType();
  public static final EntityType<LifelessWitheredHandEntity> LIFELESS_WITHERED_HAND =
      LifelessWitheredHandEntity.createEntityType();
  public static final EntityType<BoggedHandEntity> BOGGED_HAND =
      BoggedHandEntity.createEntityType();
  public static final EntityType<LifelessBoggedHandEntity> LIFELESS_BOGGED_HAND =
      LifelessBoggedHandEntity.createEntityType();
  public static final EntityType<WitchHandEntity> WITCH_HAND = WitchHandEntity.createEntityType();
  public static final EntityType<LifelessWitchHandEntity> LIFELESS_WITCH_HAND =
      LifelessWitchHandEntity.createEntityType();

  private ModEntityTypes() {}

  public static void register() {
    Registry.register(BuiltInRegistries.ENTITY_TYPE, RottenHandEntity.ENTITY_ID, ROTTEN_HAND);
    FabricDefaultAttributeRegistry.register(ROTTEN_HAND, RottenHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, DrownedHandEntity.ENTITY_ID, DROWNED_HAND);
    FabricDefaultAttributeRegistry.register(DROWNED_HAND, DrownedHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, BoneHandEntity.ENTITY_ID, BONE_HAND);
    FabricDefaultAttributeRegistry.register(BONE_HAND, BoneHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, HuskHandEntity.ENTITY_ID, HUSK_HAND);
    FabricDefaultAttributeRegistry.register(HUSK_HAND, HuskHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, StrayHandEntity.ENTITY_ID, STRAY_HAND);
    FabricDefaultAttributeRegistry.register(STRAY_HAND, StrayHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, WitheredHandEntity.ENTITY_ID, WITHERED_HAND);
    FabricDefaultAttributeRegistry.register(WITHERED_HAND, WitheredHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, BoggedHandEntity.ENTITY_ID, BOGGED_HAND);
    FabricDefaultAttributeRegistry.register(BOGGED_HAND, BoggedHandEntity.createAttributes());
    Registry.register(BuiltInRegistries.ENTITY_TYPE, WitchHandEntity.ENTITY_ID, WITCH_HAND);
    FabricDefaultAttributeRegistry.register(WITCH_HAND, WitchHandEntity.createAttributes());
    registerLifelessHand(LifelessRottenHandEntity.ENTITY_ID, LIFELESS_ROTTEN_HAND);
    registerLifelessHand(LifelessDrownedHandEntity.ENTITY_ID, LIFELESS_DROWNED_HAND);
    registerLifelessHand(LifelessBoneHandEntity.ENTITY_ID, LIFELESS_BONE_HAND);
    registerLifelessHand(LifelessHuskHandEntity.ENTITY_ID, LIFELESS_HUSK_HAND);
    registerLifelessHand(LifelessStrayHandEntity.ENTITY_ID, LIFELESS_STRAY_HAND);
    registerLifelessHand(LifelessWitheredHandEntity.ENTITY_ID, LIFELESS_WITHERED_HAND);
    registerLifelessHand(LifelessBoggedHandEntity.ENTITY_ID, LIFELESS_BOGGED_HAND);
    registerLifelessHand(LifelessWitchHandEntity.ENTITY_ID, LIFELESS_WITCH_HAND);
    SpawnPlacements.register(
        ROTTEN_HAND,
        RottenHandEntity.SPAWN_PLACEMENT_TYPE,
        RottenHandEntity.SPAWN_HEIGHTMAP,
        RottenHandEntity::checkRottenHandSpawnRules);
    BiomeModifications.addSpawn(
        BiomeSelectors.foundInOverworld(),
        MobCategory.MONSTER,
        ROTTEN_HAND,
        RottenHandEntity.SPAWN_WEIGHT,
        RottenHandEntity.SPAWN_GROUP_SIZE,
        RottenHandEntity.SPAWN_GROUP_SIZE);
  }

  private static void registerLifelessHand(
      Identifier lifelessHandId,
      EntityType<? extends AbstractLifelessHandEntity> lifelessHandType) {
    Registry.register(BuiltInRegistries.ENTITY_TYPE, lifelessHandId, lifelessHandType);
    FabricDefaultAttributeRegistry.register(
        lifelessHandType, AbstractLifelessHandEntity.createAttributes());
  }
}
