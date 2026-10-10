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

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.Entity;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RottenHandClientTest extends ClientTestBase {

  private static final double HAND_X = 0.5D;
  private static final double HAND_Z = 3.5D;
  private static final double HAND_CENTER_HEIGHT = 0.4D;
  private static final long RENDER_TICKS = 20;
  private static final long HINT_TICKS = 10;
  private static final Duration ATTACK_TIMEOUT = Duration.ofSeconds(20);

  private static Entity awaitRottenHand() {
    return client.awaitEntity(Parameters.of("type", ROTTEN_HAND), entity -> true);
  }

  private static void lookAtRottenHand() {
    lookAtPoint(HAND_X, FLOOR_Y + HAND_CENTER_HEIGHT, HAND_Z);
  }

  private static double playerHealth() {
    return client.api().state().playerGet().player().orElseThrow().vitals().health();
  }

  @Test
  @DisplayName("Summoned rotten hand renders in the world")
  void summonedRottenHandRenders() {
    enterTestArea();
    summonRottenHand(HAND_X, HAND_Z, true);
    awaitRottenHand();
    lookAtRottenHand();
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("rotten_hand");
  }

  @Test
  @DisplayName("Rotten hand attacks a survival player")
  void rottenHandAttacksSurvivalPlayer() {
    enterTestArea();
    command("effect give @s minecraft:instant_health 1 10 true");
    command("gamemode survival");
    client.await(Until.ticksElapsed(5));
    double startHealth = playerHealth();
    summonRottenHand(HAND_X, HAND_Z, false);
    awaitRottenHand();
    lookAtRottenHand();

    long deadline = System.nanoTime() + ATTACK_TIMEOUT.toNanos();
    double lowestHealthPerTick = startHealth;
    while (lowestHealthPerTick >= startHealth && System.nanoTime() < deadline) {
      client.await(Until.ticksElapsed(1));
      lowestHealthPerTick = Math.min(lowestHealthPerTick, playerHealth());
    }
    client.saveScreenshot("rotten_hand_attack");
    assertTrue(
        lowestHealthPerTick < startHealth,
        "Rotten hand did not hurt the player, health stayed at " + startHealth);
  }

  @Test
  @DisplayName("Killed rotten hand drops a lifeless rotten hand")
  void killedRottenHandDropsLifelessRottenHand() {
    enterTestArea();
    summonRottenHand(HAND_X, HAND_Z, true);
    awaitRottenHand();
    command("kill @e[type=" + ROTTEN_HAND + "]");
    client.awaitEntity(Parameters.of("type", "minecraft:item"), entity -> true);
    command("tp @e[type=item] @s");
    client.await(Until.itemCount(LIFELESS_ROTTEN_HAND, 1));
  }

  @Test
  @DisplayName("Using the lifeless rotten hand shows a hint and keeps the item")
  void usingLifelessRottenHandShowsHint() {
    enterTestArea();
    renderProfile("shown");
    command("item replace entity @s weapon.mainhand with " + LIFELESS_ROTTEN_HAND);
    client.await(Until.itemCount(LIFELESS_ROTTEN_HAND, 1));
    lookAtPoint(HAND_X, FLOOR_Y + 1.6D, HAND_Z);
    client.useHeldItem();
    client.await(Until.ticksElapsed(HINT_TICKS));
    client.saveScreenshot("revival_hint");
    client.await(Until.itemCount(LIFELESS_ROTTEN_HAND, 1));
  }

  @Test
  @DisplayName("Lifeless rotten hand lies on its back on soul sand")
  void lifelessRottenHandLiesOnItsBack() {
    enterTestArea();
    command("fill 0 " + PLATFORM_Y + " 3 0 " + PLATFORM_Y + " 3 minecraft:soul_sand");
    command("item replace entity @s weapon.mainhand with " + LIFELESS_ROTTEN_HAND);
    client.await(Until.itemCount(LIFELESS_ROTTEN_HAND, 1));
    lookAtPoint(HAND_X, FLOOR_Y - 0.1D, HAND_Z);
    client.useHeldItem();
    client.awaitEntity(Parameters.of("type", LIFELESS_ROTTEN_HAND), entity -> true);
    command("tp @s 2.5 " + FLOOR_Y + " 3.5");
    lookAtRottenHand();
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("lifeless_rotten_hand_on_back");
  }
}
