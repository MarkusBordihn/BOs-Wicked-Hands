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
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LootTableResourceTest {

  private static final double ORIGIN_DROP_CHANCE = 0.025D;

  private static JsonObject singlePool(String lootTable) {
    List<JsonElement> pools = TestResources.readJson(lootTable).getAsJsonArray("pools").asList();
    assertEquals(1, pools.size(), lootTable + " should have exactly one pool");
    return pools.get(0).getAsJsonObject();
  }

  private static JsonObject poolDropping(String lootTable, String handType) {
    String lifelessHandId = lifelessHandId(handType);
    return TestResources.readJson(lootTable).getAsJsonArray("pools").asList().stream()
        .map(JsonElement::getAsJsonObject)
        .filter(
            pool ->
                pool.getAsJsonArray("entries").asList().stream()
                    .anyMatch(
                        entry ->
                            entry.getAsJsonObject().get("name").getAsString().equals(lifelessHandId)))
        .findFirst()
        .orElseThrow(() -> new AssertionError(lootTable + " has no pool for " + lifelessHandId));
  }

  private static String lifelessHandId(String handType) {
    return Constants.MOD_ID + ":lifeless_" + handType + "_hand";
  }

  private static void assertDropsLifelessHand(JsonObject pool, String handType) {
    List<JsonElement> entries = pool.getAsJsonArray("entries").asList();
    assertEquals(1, entries.size());
    assertEquals(
        lifelessHandId(handType), entries.get(0).getAsJsonObject().get("name").getAsString());
  }

  private static JsonObject condition(JsonObject pool, String conditionType) {
    return pool.getAsJsonArray("conditions").asList().stream()
        .map(JsonElement::getAsJsonObject)
        .filter(condition -> condition.get("condition").getAsString().equals(conditionType))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing condition " + conditionType));
  }

  @ParameterizedTest
  @ValueSource(strings = {"rotten", "drowned", "bone", "husk", "stray", "withered"})
  @DisplayName("Every hand always drops its lifeless hand")
  void handAlwaysDropsItem(String handType) {
    JsonObject pool =
        singlePool("data/wicked_hands/loot_tables/entities/" + handType + "_hand.json");
    assertFalse(pool.has("conditions"), "Hand drop must not depend on conditions");
    assertEquals(1.0D, pool.get("rolls").getAsDouble());
    assertDropsLifelessHand(pool, handType);
  }

  @ParameterizedTest
  @CsvSource({
    "zombie, rotten",
    "drowned, drowned",
    "skeleton, bone",
    "skeleton, bogged",
    "husk, husk",
    "stray, stray",
    "wither_skeleton, withered"
  })
  @DisplayName("Origin mobs rarely drop their lifeless hand when killed by a player")
  void originDropIsRareAndPlayerOnly(String originMob, String handType) {
    JsonObject pool =
        poolDropping("data/wicked_hands/loot_tables/injections/" + originMob + ".json", handType);
    assertDropsLifelessHand(pool, handType);
    condition(pool, "minecraft:killed_by_player");
    JsonObject randomChance = condition(pool, "minecraft:random_chance_with_looting");
    assertEquals(ORIGIN_DROP_CHANCE, randomChance.get("chance").getAsDouble());
    assertTrue(
        randomChance.get("looting_multiplier").getAsDouble() > 0.0D,
        "Looting should raise the drop chance");
  }

  @Test
  @DisplayName("Skeletons drop the bogged hand only in swamps")
  void boggedHandDropsFromSwampSkeletons() {
    JsonObject pool =
        poolDropping("data/wicked_hands/loot_tables/injections/skeleton.json", "bogged");
    List<JsonObject> terms =
        condition(pool, "minecraft:any_of").getAsJsonArray("terms").asList().stream()
            .map(JsonElement::getAsJsonObject)
            .toList();
    assertTrue(
        terms.stream()
            .allMatch(term -> term.get("condition").getAsString().equals("minecraft:location_check")));
    assertEquals(
        Set.of("minecraft:swamp", "minecraft:mangrove_swamp"),
        terms.stream()
            .map(term -> term.getAsJsonObject("predicate").get("biome").getAsString())
            .collect(Collectors.toSet()));
  }
}
