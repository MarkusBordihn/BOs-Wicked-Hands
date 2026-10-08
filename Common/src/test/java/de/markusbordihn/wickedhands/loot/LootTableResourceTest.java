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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.wickedhands.TestResources;
import de.markusbordihn.wickedhands.item.LifelessRottenHandItem;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LootTableResourceTest {

  private static final String ROTTEN_HAND_LOOT_TABLE =
      "data/wicked_hands/loot_table/entities/rotten_hand.json";
  private static final String ZOMBIE_INJECTION_LOOT_TABLE =
      "data/wicked_hands/loot_table/injections/zombie.json";
  private static final double ZOMBIE_DROP_CHANCE = 0.025D;

  private static JsonObject singlePool(String lootTable) {
    List<JsonElement> pools = TestResources.readJson(lootTable).getAsJsonArray("pools").asList();
    assertEquals(1, pools.size(), lootTable + " should have exactly one pool");
    return pools.getFirst().getAsJsonObject();
  }

  private static void assertDropsLifelessRottenHand(JsonObject pool) {
    List<JsonElement> entries = pool.getAsJsonArray("entries").asList();
    assertEquals(1, entries.size());
    assertEquals(
        LifelessRottenHandItem.ITEM_ID.toString(),
        entries.getFirst().getAsJsonObject().get("name").getAsString());
  }

  private static JsonObject condition(JsonObject pool, String conditionType) {
    return pool.getAsJsonArray("conditions").asList().stream()
        .map(JsonElement::getAsJsonObject)
        .filter(condition -> condition.get("condition").getAsString().equals(conditionType))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing condition " + conditionType));
  }

  @Test
  @DisplayName("Rotten hand always drops a lifeless rotten hand")
  void rottenHandAlwaysDropsItem() {
    JsonObject pool = singlePool(ROTTEN_HAND_LOOT_TABLE);
    assertFalse(pool.has("conditions"), "Rotten hand drop must not depend on conditions");
    assertEquals(1.0D, pool.get("rolls").getAsDouble());
    assertDropsLifelessRottenHand(pool);
  }

  @Test
  @DisplayName("Zombies rarely drop a lifeless rotten hand when killed by a player")
  void zombieDropIsRareAndPlayerOnly() {
    JsonObject pool = singlePool(ZOMBIE_INJECTION_LOOT_TABLE);
    assertDropsLifelessRottenHand(pool);
    condition(pool, "minecraft:killed_by_player");
    JsonObject randomChance = condition(pool, "minecraft:random_chance_with_enchanted_bonus");
    assertEquals(ZOMBIE_DROP_CHANCE, randomChance.get("unenchanted_chance").getAsDouble());
    assertTrue(
        randomChance.getAsJsonObject("enchanted_chance").get("base").getAsDouble()
            > ZOMBIE_DROP_CHANCE,
        "Looting should raise the zombie drop chance");
  }
}
