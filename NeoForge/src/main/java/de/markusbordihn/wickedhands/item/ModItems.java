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
import de.markusbordihn.wickedhands.entity.ModEntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);

  public static final DeferredHolder<Item, Item> LIFELESS_ROTTEN_HAND =
      ITEMS.register(LifelessRottenHandItem.ID, LifelessRottenHandItem::create);
  public static final DeferredHolder<Item, Item> ROTTEN_HAND_SPAWN_EGG =
      ITEMS.register(
          RottenHandSpawnEgg.ID, () -> RottenHandSpawnEgg.create(ModEntityTypes.ROTTEN_HAND.get()));

  private ModItems() {}
}
