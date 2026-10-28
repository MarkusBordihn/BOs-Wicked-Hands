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
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.TestResources;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LootTableResourceTest {

  private static final double ORIGIN_DROP_CHANCE = 0.025D;

  private static JsonObject singlePool(String lootTable) {
    List<JsonElement> pools = TestResources.readJson(lootTable).getAsJsonArray("pools").asList();
    assertEquals(1, pools.size(), lootTable + " should have exactly one pool");
    return pools.getFirst().getAsJsonObject();
  }

  private static void assertDropsLifelessHand(JsonObject pool, String handType) {
    List<JsonElement> entries = pool.getAsJsonArray("entries").asList();
    assertEquals(1, entries.size());
    assertEquals(
        Constants.MOD_ID + ":lifeless_" + handType + "_hand",
        entries.getFirst().getAsJsonObject().get("name").getAsString());
  }

  private static JsonObject condition(JsonObject pool, String conditionType) {
    return pool.getAsJsonObject("condition").getAsJsonArray("terms").asList().stream()
        .map(JsonElement::getAsJsonObject)
        .filter(condition -> condition.get("type").getAsString().equals(conditionType))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing condition " + conditionType));
  }

  @ParameterizedTest
  @ValueSource(strings = {"rotten", "drowned", "bone", "husk", "stray", "withered"})
  @DisplayName("Every hand always drops its lifeless hand")
  void handAlwaysDropsItem(String handType) {
    JsonObject pool =
        singlePool("data/wicked_hands/loot_table/entities/" + handType + "_hand.json");
    assertFalse(pool.has("condition"), "Hand drop must not depend on conditions");
    assertEquals(1.0D, pool.get("rolls").getAsDouble());
    assertDropsLifelessHand(pool, handType);
  }

  @ParameterizedTest
  @CsvSource({
    "zombie, rotten",
    "drowned, drowned",
    "skeleton, bone",
    "husk, husk",
    "stray, stray",
    "wither_skeleton, withered"
  })
  @DisplayName("Origin mobs rarely drop their lifeless hand when killed by a player")
  void originDropIsRareAndPlayerOnly(String originMob, String handType) {
    JsonObject pool = singlePool("data/wicked_hands/loot_table/injections/" + originMob + ".json");
    assertDropsLifelessHand(pool, handType);
    condition(pool, "minecraft:killed_by_player");
    JsonObject randomChance = condition(pool, "minecraft:random_chance_with_enchanted_bonus");
    assertEquals(ORIGIN_DROP_CHANCE, randomChance.get("unenchanted_chance").getAsDouble());
    assertTrue(
        randomChance.getAsJsonObject("enchanted_chance").get("base").getAsDouble()
            > ORIGIN_DROP_CHANCE,
        "Looting should raise the drop chance");
  }
}
