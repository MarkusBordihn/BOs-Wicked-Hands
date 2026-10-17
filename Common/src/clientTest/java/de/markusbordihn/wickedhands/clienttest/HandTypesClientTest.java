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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HandTypesClientTest extends ClientTestBase {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"rotten_hand", "drowned_hand", "bone_hand"})
  @DisplayName("Hand renders in the world")
  void handRendersInTheWorld(String handName) {
    enterTestArea();
    lookAtCenterOf(spawnStaticHand(MOD_ID + ":" + handName));
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot(handName);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"lifeless_rotten_hand", "lifeless_drowned_hand", "lifeless_bone_hand"})
  @DisplayName("Placed lifeless hand lies on its back on soul sand")
  void placedLifelessHandLiesOnItsBack(String lifelessHandName) {
    enterTestArea();
    placeLifelessHand(MOD_ID + ":" + lifelessHandName);
    client.teleport(HAND_X + 2D, FLOOR_Y, HAND_Z);
    lookAtCenterOf(awaitEntityOfType(MOD_ID + ":" + lifelessHandName));
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot(lifelessHandName + "_on_back");
  }

  @Test
  @DisplayName("Lifeless hands show their model as item icon")
  void lifelessHandsShowModelIcons() {
    enterTestArea();
    client.renderSettings(Parameters.of().with("gui", true));
    client.setItemInSlot(0, LIFELESS_ROTTEN_HAND);
    client.setItemInSlot(1, MOD_ID + ":lifeless_drowned_hand");
    client.setItemInSlot(2, MOD_ID + ":lifeless_bone_hand");
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("lifeless_hand_icons");
  }
}
