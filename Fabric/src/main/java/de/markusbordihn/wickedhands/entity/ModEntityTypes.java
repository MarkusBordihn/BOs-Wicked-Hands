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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;

public final class ModEntityTypes {

  public static final EntityType<RottenHandEntity> ROTTEN_HAND =
      RottenHandEntity.createEntityType();

  private ModEntityTypes() {}

  public static void register() {
    Registry.register(BuiltInRegistries.ENTITY_TYPE, RottenHandEntity.ENTITY_ID, ROTTEN_HAND);
    FabricDefaultAttributeRegistry.register(ROTTEN_HAND, RottenHandEntity.createAttributes());
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
}
