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

package de.markusbordihn.wickedhands.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class RottenHandTest {

  private final RottenHandGameTests rottenHandGameTests = new RottenHandGameTests();

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandSpawnsNaturallyAtNight(GameTestHelper helper) {
    this.rottenHandGameTests.testRottenHandSpawnsNaturallyAtNight(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    this.rottenHandGameTests.testRottenHandDropsLifelessRottenHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testSummonedRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    this.rottenHandGameTests.testSummonedRottenHandDropsLifelessRottenHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = 100)
  public void testRottenHandAttacksAnimals(GameTestHelper helper) {
    this.rottenHandGameTests.testRottenHandAttacksAnimals(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandDespawnsWhenFarAway(GameTestHelper helper) {
    this.rottenHandGameTests.testRottenHandDespawnsWhenFarAway(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testZombieDropsLifelessRottenHand(GameTestHelper helper) {
    this.rottenHandGameTests.testZombieDropsLifelessRottenHand(helper);
  }
}
