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

package de.markusbordihn.wickedhands.clienttest;

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RevivalRitualClientTest extends ClientTestBase {

  private static final String CANDLE = "minecraft:candle";
  private static final int[] CANDLE_OFFSETS = {-1, 1};
  private static final long VORTEX_TICKS = 60;

  @ParameterizedTest(name = "{0}_hand")
  @CsvSource({
    "rotten, rotten_flesh",
    "husk, sand",
    "drowned, kelp",
    "bone, bone_meal",
    "stray, snowball",
    "withered, coal",
    "bogged, red_mushroom",
    "witch, spider_eye"
  })
  @DisplayName("Revival ritual wakes a lifeless hand as companion despite damage")
  void revivalRitualWakesCompanion(String handType, String ritualOffering) {
    enterTestArea();
    Entity lifelessHand = placeLifelessHand(MOD_ID + ":lifeless_" + handType + "_hand");
    for (int offsetX : CANDLE_OFFSETS) {
      for (int offsetZ : CANDLE_OFFSETS) {
        client.setBlock(
            HAND_BLOCK_X + offsetX, FLOOR_Y, HAND_BLOCK_Z + offsetZ, CANDLE + "[lit=true]");
      }
    }
    client.renderSettings(Parameters.of().with("particles", "all"));
    client.equip("minecraft:" + ritualOffering);
    lookAtCenterOf(lifelessHand);
    client.useHeldItem();
    client.await(Until.ticksElapsed(RENDER_TICKS + VORTEX_TICKS));
    client.saveScreenshot(handType + "_revival_ritual");

    Entity companion = removeAfterTest(awaitEntityOfType(MOD_ID + ":" + handType + "_hand"));
    client.await(Until.entityAbsent(Parameters.of("uuid", lifelessHand.uuid())));
    lookAtCenterOf(companion);
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot(handType + "_revived_companion");
    client.runCommand("effect clear @s minecraft:darkness");
    client.runCommand("advancement revoke @s from " + MOD_ID + ":root");
  }
}
