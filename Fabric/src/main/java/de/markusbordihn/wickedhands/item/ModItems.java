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

package de.markusbordihn.wickedhands.item;

import de.markusbordihn.wickedhands.entity.BoggedHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.HuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoggedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessHuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessStrayHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitchHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitheredHandEntity;
import de.markusbordihn.wickedhands.entity.ModEntityTypes;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.entity.StrayHandEntity;
import de.markusbordihn.wickedhands.entity.WitchHandEntity;
import de.markusbordihn.wickedhands.entity.WitheredHandEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class ModItems {

  public static final Item LIFELESS_ROTTEN_HAND =
      LifelessHandItem.create(LifelessRottenHandEntity.ENTITY_ID);
  public static final Item LIFELESS_DROWNED_HAND =
      LifelessHandItem.create(LifelessDrownedHandEntity.ENTITY_ID);
  public static final Item LIFELESS_BONE_HAND =
      LifelessHandItem.create(LifelessBoneHandEntity.ENTITY_ID);
  public static final Item ROTTEN_HAND_SPAWN_EGG =
      HandSpawnEgg.create(RottenHandEntity.ENTITY_ID, ModEntityTypes.ROTTEN_HAND);
  public static final Item DROWNED_HAND_SPAWN_EGG =
      HandSpawnEgg.create(DrownedHandEntity.ENTITY_ID, ModEntityTypes.DROWNED_HAND);
  public static final Item BONE_HAND_SPAWN_EGG =
      HandSpawnEgg.create(BoneHandEntity.ENTITY_ID, ModEntityTypes.BONE_HAND);
  public static final Item LIFELESS_HUSK_HAND =
      LifelessHandItem.create(LifelessHuskHandEntity.ENTITY_ID);
  public static final Item LIFELESS_STRAY_HAND =
      LifelessHandItem.create(LifelessStrayHandEntity.ENTITY_ID);
  public static final Item LIFELESS_WITHERED_HAND =
      LifelessHandItem.create(LifelessWitheredHandEntity.ENTITY_ID);
  public static final Item HUSK_HAND_SPAWN_EGG =
      HandSpawnEgg.create(HuskHandEntity.ENTITY_ID, ModEntityTypes.HUSK_HAND);
  public static final Item STRAY_HAND_SPAWN_EGG =
      HandSpawnEgg.create(StrayHandEntity.ENTITY_ID, ModEntityTypes.STRAY_HAND);
  public static final Item WITHERED_HAND_SPAWN_EGG =
      HandSpawnEgg.create(WitheredHandEntity.ENTITY_ID, ModEntityTypes.WITHERED_HAND);
  public static final Item LIFELESS_BOGGED_HAND =
      LifelessHandItem.create(LifelessBoggedHandEntity.ENTITY_ID);
  public static final Item LIFELESS_WITCH_HAND =
      LifelessHandItem.create(LifelessWitchHandEntity.ENTITY_ID);
  public static final Item BOGGED_HAND_SPAWN_EGG =
      HandSpawnEgg.create(BoggedHandEntity.ENTITY_ID, ModEntityTypes.BOGGED_HAND);
  public static final Item WITCH_HAND_SPAWN_EGG =
      HandSpawnEgg.create(WitchHandEntity.ENTITY_ID, ModEntityTypes.WITCH_HAND);

  private ModItems() {}

  public static void register() {
    register(LifelessRottenHandEntity.ENTITY_ID, LIFELESS_ROTTEN_HAND);
    register(LifelessDrownedHandEntity.ENTITY_ID, LIFELESS_DROWNED_HAND);
    register(LifelessBoneHandEntity.ENTITY_ID, LIFELESS_BONE_HAND);
    register(LifelessHuskHandEntity.ENTITY_ID, LIFELESS_HUSK_HAND);
    register(LifelessStrayHandEntity.ENTITY_ID, LIFELESS_STRAY_HAND);
    register(LifelessWitheredHandEntity.ENTITY_ID, LIFELESS_WITHERED_HAND);
    register(LifelessBoggedHandEntity.ENTITY_ID, LIFELESS_BOGGED_HAND);
    register(LifelessWitchHandEntity.ENTITY_ID, LIFELESS_WITCH_HAND);
    register(HandSpawnEgg.itemId(RottenHandEntity.ENTITY_ID), ROTTEN_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(DrownedHandEntity.ENTITY_ID), DROWNED_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(BoneHandEntity.ENTITY_ID), BONE_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(HuskHandEntity.ENTITY_ID), HUSK_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(StrayHandEntity.ENTITY_ID), STRAY_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(WitheredHandEntity.ENTITY_ID), WITHERED_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(BoggedHandEntity.ENTITY_ID), BOGGED_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(WitchHandEntity.ENTITY_ID), WITCH_HAND_SPAWN_EGG);
  }

  private static void register(Identifier itemId, Item item) {
    Registry.register(BuiltInRegistries.ITEM, itemId, item);
  }
}
