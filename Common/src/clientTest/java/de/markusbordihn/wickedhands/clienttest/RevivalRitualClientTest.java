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
import org.junit.jupiter.api.Test;

class RevivalRitualClientTest extends ClientTestBase {

  private static final String LIT_CANDLE = "minecraft:candle[lit=true]";
  private static final long VORTEX_TICKS = 60;

  @Test
  @DisplayName("Revival ritual wakes a lifeless rotten hand as companion")
  void revivalRitualWakesCompanion() {
    enterTestArea();
    Entity lifelessHand = placeLifelessHand(LIFELESS_ROTTEN_HAND);
    for (int offsetX : new int[] {-1, 1}) {
      for (int offsetZ : new int[] {-1, 1}) {
        client.setBlock(HAND_BLOCK_X + offsetX, FLOOR_Y, HAND_BLOCK_Z + offsetZ, LIT_CANDLE);
      }
    }
    client.equip("minecraft:rotten_flesh");
    lookAtCenterOf(lifelessHand);
    client.useHeldItem();
    client.await(Until.ticksElapsed(VORTEX_TICKS));
    client.saveScreenshot("revival_ritual");

    Entity companion = removeAfterTest(awaitEntityOfType(ROTTEN_HAND));
    client.await(Until.entityAbsent(Parameters.of("uuid", lifelessHand.uuid())));
    lookAtCenterOf(companion);
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("revived_companion");
    client.runCommand("effect clear @s minecraft:darkness");
    client.runCommand("advancement revoke @s from " + MOD_ID + ":root");
  }
}
