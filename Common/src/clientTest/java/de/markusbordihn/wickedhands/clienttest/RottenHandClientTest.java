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

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.GameClient.TemporaryChange;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.GameMode;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RottenHandClientTest extends ClientTestBase {

  private static final long HINT_TICKS = 10;

  private static Entity summonRottenHandWithFinalizeSpawn() {
    client.runCommand("summon " + ROTTEN_HAND + " " + HAND_X + " " + FLOOR_Y + " " + HAND_Z);
    return removeAfterTest(awaitEntityOfType(ROTTEN_HAND));
  }

  @Test
  @DisplayName("Rotten hand attacks a survival player")
  void rottenHandAttacksSurvivalPlayer() {
    enterTestArea();
    try (TemporaryChange survival = client.setGameMode(GameMode.SURVIVAL)) {
      double startHealth = client.playerHealth();
      lookAtCenterOf(summonRottenHandWithFinalizeSpawn());
      client.awaitValue(
          "player health below " + startHealth,
          client::playerHealth,
          health -> health < startHealth);
      client.saveScreenshot("rotten_hand_attack");
    }
  }

  @Test
  @DisplayName("Using the lifeless rotten hand shows a hint and keeps the item")
  void usingLifelessRottenHandShowsHint() {
    enterTestArea();
    client.renderSettings(Parameters.of().with("gui", true).with("chat", true));
    client.equip(LIFELESS_ROTTEN_HAND);
    client.lookAtPoint(HAND_X, FLOOR_Y + 1.6D, HAND_Z);
    client.useHeldItem();
    client.await(Until.chatMessage("and flesh"));
    client.await(Until.ticksElapsed(HINT_TICKS));
    client.saveScreenshot("revival_hint");
    client.await(Until.itemCount(LIFELESS_ROTTEN_HAND, 1));
  }

  @Test
  @DisplayName("Rotten hand sits on the player's shoulder seen from behind")
  void rottenHandSitsOnShoulderFromBehind() {
    enterTestArea();
    Entity rottenHand = spawnStaticHand(ROTTEN_HAND);
    client.runCommand("easy_npc shoulder sit " + rottenHand.uuid() + " @s");
    client.renderSettings(Parameters.of().with("perspective", "back"));
    try {
      client.await(Until.entityAbsent(Parameters.of("uuid", rottenHand.uuid())));
      client.await(Until.ticksElapsed(RENDER_TICKS));
      client.saveScreenshot("rotten_hand_on_shoulder_back");
    } finally {
      client.runCommand("easy_npc shoulder dismount @s");
    }
  }
}
