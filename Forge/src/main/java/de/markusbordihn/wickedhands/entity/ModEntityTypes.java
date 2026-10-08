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

  private ModEntityTypes() {}

  public static void registerEvents() {
    EntityAttributeCreationEvent.BUS.addListener(ModEntityTypes::registerEntityAttributes);
    SpawnPlacementRegisterEvent.BUS.addListener(ModEntityTypes::registerSpawnPlacements);
  }

  private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
    event.put(ROTTEN_HAND.get(), RottenHandEntity.createAttributes().build());
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
