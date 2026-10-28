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

import de.markusbordihn.wickedhands.Constants;
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
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

  public static final RegistryObject<Item> LIFELESS_ROTTEN_HAND =
      ITEMS.register(
          LifelessRottenHandEntity.ID,
          () -> LifelessHandItem.create(LifelessRottenHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> LIFELESS_DROWNED_HAND =
      ITEMS.register(
          LifelessDrownedHandEntity.ID,
          () -> LifelessHandItem.create(LifelessDrownedHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> LIFELESS_BONE_HAND =
      ITEMS.register(
          LifelessBoneHandEntity.ID,
          () -> LifelessHandItem.create(LifelessBoneHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> ROTTEN_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(RottenHandEntity.ENTITY_ID).getPath(),
          () -> HandSpawnEgg.create(ModEntityTypes.ROTTEN_HAND));
  public static final RegistryObject<Item> DROWNED_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(DrownedHandEntity.ENTITY_ID).getPath(),
          () ->
              HandSpawnEgg.create(ModEntityTypes.DROWNED_HAND));
  public static final RegistryObject<Item> BONE_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(BoneHandEntity.ENTITY_ID).getPath(),
          () -> HandSpawnEgg.create(ModEntityTypes.BONE_HAND));
  public static final RegistryObject<Item> LIFELESS_HUSK_HAND =
      ITEMS.register(
          LifelessHuskHandEntity.ID,
          () -> LifelessHandItem.create(LifelessHuskHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> LIFELESS_STRAY_HAND =
      ITEMS.register(
          LifelessStrayHandEntity.ID,
          () -> LifelessHandItem.create(LifelessStrayHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> LIFELESS_WITHERED_HAND =
      ITEMS.register(
          LifelessWitheredHandEntity.ID,
          () -> LifelessHandItem.create(LifelessWitheredHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> HUSK_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(HuskHandEntity.ENTITY_ID).getPath(),
          () -> HandSpawnEgg.create(ModEntityTypes.HUSK_HAND));
  public static final RegistryObject<Item> STRAY_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(StrayHandEntity.ENTITY_ID).getPath(),
          () -> HandSpawnEgg.create(ModEntityTypes.STRAY_HAND));
  public static final RegistryObject<Item> WITHERED_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(WitheredHandEntity.ENTITY_ID).getPath(),
          () ->
              HandSpawnEgg.create(ModEntityTypes.WITHERED_HAND));
  public static final RegistryObject<Item> LIFELESS_BOGGED_HAND =
      ITEMS.register(
          LifelessBoggedHandEntity.ID,
          () -> LifelessHandItem.create(LifelessBoggedHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> LIFELESS_WITCH_HAND =
      ITEMS.register(
          LifelessWitchHandEntity.ID,
          () -> LifelessHandItem.create(LifelessWitchHandEntity.ENTITY_ID));
  public static final RegistryObject<Item> BOGGED_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(BoggedHandEntity.ENTITY_ID).getPath(),
          () -> HandSpawnEgg.create(ModEntityTypes.BOGGED_HAND));
  public static final RegistryObject<Item> WITCH_HAND_SPAWN_EGG =
      ITEMS.register(
          HandSpawnEgg.itemId(WitchHandEntity.ENTITY_ID).getPath(),
          () -> HandSpawnEgg.create(ModEntityTypes.WITCH_HAND));

  private ModItems() {}
}
