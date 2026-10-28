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

package de.markusbordihn.wickedhands.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

// Forge 66 LootTableLoadEvent offers no registry lookup, which a nested loot table reference needs.
public class HandDropsLootModifier extends LootModifier {

  public static final MapCodec<HandDropsLootModifier> CODEC =
      RecordCodecBuilder.mapCodec(
          instance -> codecStart(instance).apply(instance, HandDropsLootModifier::new));

  public HandDropsLootModifier(LootItemCondition[] conditions) {
    super(conditions);
  }

  @Override
  protected ObjectArrayList<ItemStack> doApply(
      LootTable lootTable, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    HandLootInjections.injectionFor(
            ResourceKey.create(Registries.LOOT_TABLE, context.getQueriedLootTableId()))
        .flatMap(injection -> context.getResolver().get(injection))
        .ifPresent(
            injectionTable ->
                injectionTable
                    .value()
                    .getRandomItemsRaw(
                        context,
                        LootTable.createStackSplitter(context.getLevel(), generatedLoot::add)));
    return generatedLoot;
  }

  @Override
  public MapCodec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }
}
