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

public class HandTest {

  private final HandGameTests handGameTests = new HandGameTests();

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandSpawnsNaturallyAtNight(GameTestHelper helper) {
    this.handGameTests.testRottenHandSpawnsNaturallyAtNight(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    this.handGameTests.testRottenHandDropsLifelessRottenHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testSummonedRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    this.handGameTests.testSummonedRottenHandDropsLifelessRottenHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testRottenHandAttacksAnimals(GameTestHelper helper) {
    this.handGameTests.testRottenHandAttacksAnimals(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testRottenHandSwimsToSurface(GameTestHelper helper) {
    this.handGameTests.testRottenHandSwimsToSurface(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandUsesActiveModelProfile(GameTestHelper helper) {
    this.handGameTests.testRottenHandUsesActiveModelProfile(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandDespawnsWhenFarAway(GameTestHelper helper) {
    this.handGameTests.testRottenHandDespawnsWhenFarAway(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testMonstersDropTheirLifelessHand(GameTestHelper helper) {
    this.handGameTests.testMonstersDropTheirLifelessHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testLifelessRottenHandIsPlacedOnSoulSand(GameTestHelper helper) {
    this.handGameTests.testLifelessRottenHandIsPlacedOnSoulSand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRevivalRitualNeedsCandles(GameTestHelper helper) {
    this.handGameTests.testRevivalRitualNeedsCandles(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testRevivalRitualCreatesCompanion(GameTestHelper helper) {
    this.handGameTests.testRevivalRitualCreatesCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testRevivalRitualGrantsAdvancements(GameTestHelper helper) {
    this.handGameTests.testRevivalRitualGrantsAdvancements(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testOnlyCompanionGrabsTarget(GameTestHelper helper) {
    this.handGameTests.testOnlyCompanionGrabsTarget(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testFallenCompanionWithoutOwnerLeavesProtectedRemains(GameTestHelper helper) {
    this.handGameTests.testFallenCompanionWithoutOwnerLeavesProtectedRemains(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testStrangerCannotReviveForeignRemains(GameTestHelper helper) {
    this.handGameTests.testStrangerCannotReviveForeignRemains(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenCompanionReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.handGameTests.testFallenCompanionReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenDrownedHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.handGameTests.testFallenDrownedHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenBoneHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.handGameTests.testFallenBoneHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenHuskHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.handGameTests.testFallenHuskHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenStrayHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.handGameTests.testFallenStrayHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenWitheredHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.handGameTests.testFallenWitheredHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testHuskHandRitualCreatesWeakeningCompanion(GameTestHelper helper) {
    this.handGameTests.testHuskHandRitualCreatesWeakeningCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testStrayHandRitualCreatesChillingCompanion(GameTestHelper helper) {
    this.handGameTests.testStrayHandRitualCreatesChillingCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testWitheredHandRitualCreatesWitheringCompanion(GameTestHelper helper) {
    this.handGameTests.testWitheredHandRitualCreatesWitheringCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testWitheredHandIsImmuneToFire(GameTestHelper helper) {
    this.handGameTests.testWitheredHandIsImmuneToFire(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderBoneAndWitheredHandsGrantPassives(GameTestHelper helper) {
    this.handGameTests.testShoulderBoneAndWitheredHandsGrantPassives(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderRottenAndDrownedHandsGrantPassives(GameTestHelper helper) {
    this.handGameTests.testShoulderRottenAndDrownedHandsGrantPassives(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testShoulderPassiveAppliesOnPlayerTick(GameTestHelper helper) {
    this.handGameTests.testShoulderPassiveAppliesOnPlayerTick(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderHandsProtectFromHungerAndFreezing(GameTestHelper helper) {
    this.handGameTests.testShoulderHandsProtectFromHungerAndFreezing(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testDrownedHandRitualNeedsKelp(GameTestHelper helper) {
    this.handGameTests.testDrownedHandRitualNeedsKelp(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testBoneHandRitualCreatesCompanion(GameTestHelper helper) {
    this.handGameTests.testBoneHandRitualCreatesCompanion(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.UNDERWATER_MAX_TICKS)
  public void testDrownedHandBreathesUnderwater(GameTestHelper helper) {
    this.handGameTests.testDrownedHandBreathesUnderwater(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testDrownedHandExtinguishesBurningOwner(GameTestHelper helper) {
    this.handGameTests.testDrownedHandExtinguishesBurningOwner(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testBoneHandWarnsAboutMonsterOnce(GameTestHelper helper) {
    this.handGameTests.testBoneHandWarnsAboutMonsterOnce(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionWavesAtStrangerOnce(GameTestHelper helper) {
    this.handGameTests.testCompanionWavesAtStrangerOnce(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionKeepsBondAfterReload(GameTestHelper helper) {
    this.handGameTests.testCompanionKeepsBondAfterReload(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionDefendsAndJoinsOwner(GameTestHelper helper) {
    this.handGameTests.testCompanionDefendsAndJoinsOwner(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testCompanionIgnoresOwnerHittingSiblingCompanion(GameTestHelper helper) {
    this.handGameTests.testCompanionIgnoresOwnerHittingSiblingCompanion(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFollowingCompanionJoinsOwnerInOtherDimension(GameTestHelper helper) {
    this.handGameTests.testFollowingCompanionJoinsOwnerInOtherDimension(helper);
  }
}
