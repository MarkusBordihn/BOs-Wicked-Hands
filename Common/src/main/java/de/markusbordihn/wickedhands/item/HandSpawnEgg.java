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

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public final class HandSpawnEgg extends SpawnEggItem {

  private static final int UNTINTED_COLOR = 0xFFFFFF;

  private final Supplier<? extends EntityType<? extends Mob>> handType;

  // Forge and NeoForge 1.21.1 register items before entity types, so the type is resolved lazily.
  private HandSpawnEgg(Supplier<? extends EntityType<? extends Mob>> handType) {
    super(null, UNTINTED_COLOR, UNTINTED_COLOR, new Item.Properties());
    this.handType = handType;
  }

  public static ResourceLocation itemId(ResourceLocation handId) {
    return handId.withSuffix("_spawn_egg");
  }

  public static Item create(Supplier<? extends EntityType<? extends Mob>> handType) {
    return new HandSpawnEgg(handType);
  }

  @Override
  public EntityType<?> getType(ItemStack itemStack) {
    EntityType<?> storedType = super.getType(itemStack);
    return storedType != null ? storedType : this.handType.get();
  }

  @Override
  public FeatureFlagSet requiredFeatures() {
    return this.handType.get().requiredFeatures();
  }
}
