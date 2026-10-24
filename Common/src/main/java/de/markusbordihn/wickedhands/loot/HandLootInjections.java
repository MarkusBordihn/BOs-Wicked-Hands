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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;

public final class HandLootInjections {

  private static final Map<ResourceKey<LootTable>, ResourceKey<LootTable>> INJECTIONS =
      Map.of(
          vanillaEntityLootTable("zombie"),
          injection("zombie"),
          vanillaEntityLootTable("drowned"),
          injection("drowned"),
          vanillaEntityLootTable("skeleton"),
          injection("skeleton"),
          vanillaEntityLootTable("husk"),
          injection("husk"),
          vanillaEntityLootTable("stray"),
          injection("stray"),
          vanillaEntityLootTable("wither_skeleton"),
          injection("wither_skeleton"),
          vanillaEntityLootTable("bogged"),
          injection("bogged"),
          vanillaEntityLootTable("witch"),
          injection("witch"));

  private HandLootInjections() {}

  private static ResourceKey<LootTable> vanillaEntityLootTable(String entityName) {
    return ResourceKey.create(
        Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/" + entityName));
  }

  private static ResourceKey<LootTable> injection(String entityName) {
    return ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "injections/" + entityName));
  }

  public static Optional<LootPool.Builder> createDropPool(ResourceKey<LootTable> lootTable) {
    return Optional.ofNullable(INJECTIONS.get(lootTable))
        .map(injection -> LootPool.lootPool().add(NestedLootTable.lootTableReference(injection)));
  }
}
