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

import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.ModEntityTypes;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
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

  private ModItems() {}

  public static void register() {
    register(LifelessRottenHandEntity.ENTITY_ID, LIFELESS_ROTTEN_HAND);
    register(LifelessDrownedHandEntity.ENTITY_ID, LIFELESS_DROWNED_HAND);
    register(LifelessBoneHandEntity.ENTITY_ID, LIFELESS_BONE_HAND);
    register(HandSpawnEgg.itemId(RottenHandEntity.ENTITY_ID), ROTTEN_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(DrownedHandEntity.ENTITY_ID), DROWNED_HAND_SPAWN_EGG);
    register(HandSpawnEgg.itemId(BoneHandEntity.ENTITY_ID), BONE_HAND_SPAWN_EGG);
  }

  private static void register(Identifier itemId, Item item) {
    Registry.register(BuiltInRegistries.ITEM, itemId, item);
  }
}
