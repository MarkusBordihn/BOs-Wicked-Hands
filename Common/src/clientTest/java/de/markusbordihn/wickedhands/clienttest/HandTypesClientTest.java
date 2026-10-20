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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HandTypesClientTest extends ClientTestBase {

  private static final long SHOULDER_PASSIVE_TICKS = 2 * RENDER_TICKS;

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "rotten_hand",
        "husk_hand",
        "drowned_hand",
        "bone_hand",
        "stray_hand",
        "withered_hand"
      })
  @DisplayName("Hand renders in the world")
  void handRendersInTheWorld(String handName) {
    enterTestArea();
    lookAtCenterOf(spawnStaticHand(MOD_ID + ":" + handName));
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot(handName);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"husk_hand", "drowned_hand", "bone_hand", "stray_hand", "withered_hand"})
  @DisplayName("Hand sits on the player's shoulder")
  void handSitsOnShoulder(String handName) {
    enterTestArea();
    Entity hand = spawnStaticHand(MOD_ID + ":" + handName);
    client.runCommand("easy_npc shoulder sit " + hand.uuid() + " @s");
    client.renderSettings(Parameters.of().with("perspective", "front"));
    try {
      client.await(Until.entityAbsent(Parameters.of("uuid", hand.uuid())));
      client.await(Until.ticksElapsed(RENDER_TICKS));
      client.saveScreenshot(handName + "_on_shoulder");
    } finally {
      client.runCommand("easy_npc shoulder dismount @s");
    }
  }

  @ParameterizedTest(name = "{0} and {1}")
  @CsvSource({"rotten, drowned", "bone, husk", "stray, withered"})
  @DisplayName("Hands on both shoulders show their passive effects")
  void shoulderHandsShowPassiveEffects(String leftHandType, String rightHandType) {
    enterTestArea();
    Entity leftHand = spawnStaticHand(MOD_ID + ":" + leftHandType + "_hand");
    Entity rightHand = spawnStaticHand(MOD_ID + ":" + rightHandType + "_hand");
    client.runCommand("easy_npc shoulder sit " + leftHand.uuid() + " @s");
    client.runCommand("easy_npc shoulder sit " + rightHand.uuid() + " @s");
    client.runCommand("effect give @s minecraft:speed 60 0 true");
    client.renderSettings(Parameters.of().with("gui", true));
    String screenshotLabel = leftHandType + "_" + rightHandType;
    try {
      client.await(Until.entityAbsent(Parameters.of("uuid", leftHand.uuid())));
      client.await(Until.entityAbsent(Parameters.of("uuid", rightHand.uuid())));
      client.await(Until.ticksElapsed(SHOULDER_PASSIVE_TICKS));
      client.saveScreenshot(screenshotLabel + "_hud");
      client.pressBindingAndAwaitScreen("key.inventory");
      client.await(Until.ticksElapsed(RENDER_TICKS));
      client.saveScreenshot(screenshotLabel + "_inventory");
    } finally {
      client.runCommand("easy_npc shoulder dismount @s");
      client.runCommand("effect clear @s");
    }
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "lifeless_rotten_hand",
        "lifeless_husk_hand",
        "lifeless_drowned_hand",
        "lifeless_bone_hand",
        "lifeless_stray_hand",
        "lifeless_withered_hand"
      })
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
    client.setItemInSlot(1, MOD_ID + ":lifeless_husk_hand");
    client.setItemInSlot(2, MOD_ID + ":lifeless_drowned_hand");
    client.setItemInSlot(3, MOD_ID + ":lifeless_bone_hand");
    client.setItemInSlot(4, MOD_ID + ":lifeless_stray_hand");
    client.setItemInSlot(5, MOD_ID + ":lifeless_withered_hand");
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("lifeless_hand_icons");
  }

  @Test
  @DisplayName("Spawn eggs show their own icon")
  void spawnEggsShowTheirIcons() {
    enterTestArea();
    client.renderSettings(Parameters.of().with("gui", true));
    client.setItemInSlot(0, MOD_ID + ":rotten_hand_spawn_egg");
    client.setItemInSlot(1, MOD_ID + ":husk_hand_spawn_egg");
    client.setItemInSlot(2, MOD_ID + ":drowned_hand_spawn_egg");
    client.setItemInSlot(3, MOD_ID + ":bone_hand_spawn_egg");
    client.setItemInSlot(4, MOD_ID + ":stray_hand_spawn_egg");
    client.setItemInSlot(5, MOD_ID + ":withered_hand_spawn_egg");
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("spawn_egg_icons");
  }
}
