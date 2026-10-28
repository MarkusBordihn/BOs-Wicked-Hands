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

import de.markusbordihn.wickedhands.Constants;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;

public final class HandLootInjections {

  private static final Map<ResourceLocation, ResourceLocation> INJECTIONS =
      Stream.of("zombie", "drowned", "skeleton", "husk", "stray", "wither_skeleton", "witch")
          .collect(
              Collectors.toUnmodifiableMap(
                  HandLootInjections::vanillaEntityLootTable, HandLootInjections::injection));

  private HandLootInjections() {}

  private static ResourceLocation vanillaEntityLootTable(String entityName) {
    return new ResourceLocation("entities/" + entityName);
  }

  private static ResourceLocation injection(String entityName) {
    return new ResourceLocation(Constants.MOD_ID, "injections/" + entityName);
  }

  public static Optional<LootPool.Builder> createDropPool(ResourceLocation lootTable) {
    return Optional.ofNullable(INJECTIONS.get(lootTable))
        .map(
            injection -> LootPool.lootPool().add(LootTableReference.lootTableReference(injection)));
  }
}
