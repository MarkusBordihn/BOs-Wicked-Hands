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

package de.markusbordihn.wickedhands.tabs;

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
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.entity.StrayHandEntity;
import de.markusbordihn.wickedhands.entity.WitchHandEntity;
import de.markusbordihn.wickedhands.entity.WitheredHandEntity;
import de.markusbordihn.wickedhands.item.HandSpawnEgg;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class WickedHandsTab {

  public static final String ID = Constants.MOD_ID;
  public static final ResourceLocation TAB_ID =
      new ResourceLocation(Constants.MOD_ID, ID);
  public static final Component TITLE = Component.translatable("itemGroup." + Constants.MOD_ID);
  private static final List<ResourceLocation> SPAWN_EGG_IDS =
      Stream.of(
              RottenHandEntity.ENTITY_ID,
              HuskHandEntity.ENTITY_ID,
              DrownedHandEntity.ENTITY_ID,
              BoneHandEntity.ENTITY_ID,
              StrayHandEntity.ENTITY_ID,
              WitheredHandEntity.ENTITY_ID,
              BoggedHandEntity.ENTITY_ID,
              WitchHandEntity.ENTITY_ID)
          .map(HandSpawnEgg::itemId)
          .toList();
  private static final List<ResourceLocation> LIFELESS_HAND_IDS =
      List.of(
          LifelessRottenHandEntity.ENTITY_ID,
          LifelessHuskHandEntity.ENTITY_ID,
          LifelessDrownedHandEntity.ENTITY_ID,
          LifelessBoneHandEntity.ENTITY_ID,
          LifelessStrayHandEntity.ENTITY_ID,
          LifelessWitheredHandEntity.ENTITY_ID,
          LifelessBoggedHandEntity.ENTITY_ID,
          LifelessWitchHandEntity.ENTITY_ID);

  private WickedHandsTab() {}

  public static List<Item> spawnEggs() {
    return SPAWN_EGG_IDS.stream().map(BuiltInRegistries.ITEM::get).toList();
  }

  public static List<Item> displayItems() {
    return Stream.concat(SPAWN_EGG_IDS.stream(), LIFELESS_HAND_IDS.stream())
        .map(BuiltInRegistries.ITEM::get)
        .toList();
  }
}
