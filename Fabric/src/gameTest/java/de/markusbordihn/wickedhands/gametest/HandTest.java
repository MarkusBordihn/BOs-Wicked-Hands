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

  private final WildHandGameTests wildHandGameTests = new WildHandGameTests();
  private final RevivalRitualGameTests revivalRitualGameTests = new RevivalRitualGameTests();
  private final CompanionAbilityGameTests companionAbilityGameTests =
      new CompanionAbilityGameTests();
  private final CompanionBehaviourGameTests companionBehaviourGameTests =
      new CompanionBehaviourGameTests();
  private final ShoulderGameTests shoulderGameTests = new ShoulderGameTests();

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandSpawnsNaturallyAtNight(GameTestHelper helper) {
    this.wildHandGameTests.testRottenHandSpawnsNaturallyAtNight(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testSummonedRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    this.wildHandGameTests.testSummonedRottenHandDropsLifelessRottenHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testRottenHandAttacksAnimals(GameTestHelper helper) {
    this.wildHandGameTests.testRottenHandAttacksAnimals(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testRottenHandSwimsToSurface(GameTestHelper helper) {
    this.wildHandGameTests.testRottenHandSwimsToSurface(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandUsesActiveModelProfile(GameTestHelper helper) {
    this.wildHandGameTests.testRottenHandUsesActiveModelProfile(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRottenHandDespawnsWhenFarAway(GameTestHelper helper) {
    this.wildHandGameTests.testRottenHandDespawnsWhenFarAway(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testMonstersDropTheirLifelessHand(GameTestHelper helper) {
    this.wildHandGameTests.testMonstersDropTheirLifelessHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testLifelessRottenHandIsPlacedOnSoulSand(GameTestHelper helper) {
    this.revivalRitualGameTests.testLifelessRottenHandIsPlacedOnSoulSand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRevivalRitualNeedsCandles(GameTestHelper helper) {
    this.revivalRitualGameTests.testRevivalRitualNeedsCandles(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testRevivalRitualCreatesCompanion(GameTestHelper helper) {
    this.revivalRitualGameTests.testRevivalRitualCreatesCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testRevivalRitualGrantsAdvancements(GameTestHelper helper) {
    this.revivalRitualGameTests.testRevivalRitualGrantsAdvancements(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testOnlyCompanionGrabsTarget(GameTestHelper helper) {
    this.companionAbilityGameTests.testOnlyCompanionGrabsTarget(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testFallenCompanionWithoutOwnerLeavesProtectedRemains(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenCompanionWithoutOwnerLeavesProtectedRemains(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testFallenCompanionWithFullInventoryLeavesProtectedRemains(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenCompanionWithFullInventoryLeavesProtectedRemains(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testBoundRemainsResistAllButTheirOwner(GameTestHelper helper) {
    this.revivalRitualGameTests.testBoundRemainsResistAllButTheirOwner(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testRunningRitualProtectsLifelessHand(GameTestHelper helper) {
    this.revivalRitualGameTests.testRunningRitualProtectsLifelessHand(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testStrangerCannotReviveForeignRemains(GameTestHelper helper) {
    this.revivalRitualGameTests.testStrangerCannotReviveForeignRemains(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenCompanionReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenCompanionReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenDrownedHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenDrownedHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenBoneHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenBoneHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenHuskHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenHuskHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenStrayHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenStrayHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenWitheredHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenWitheredHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenBoggedHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenBoggedHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFallenWitchHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    this.revivalRitualGameTests.testFallenWitchHandReturnsAndRevivesWithSameIdentity(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testHuskHandRitualCreatesWeakeningCompanion(GameTestHelper helper) {
    this.companionAbilityGameTests.testHuskHandRitualCreatesWeakeningCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testStrayHandRitualCreatesChillingCompanion(GameTestHelper helper) {
    this.companionAbilityGameTests.testStrayHandRitualCreatesChillingCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testWitheredHandRitualCreatesWitheringCompanion(GameTestHelper helper) {
    this.companionAbilityGameTests.testWitheredHandRitualCreatesWitheringCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testWitheredHandIsImmuneToFire(GameTestHelper helper) {
    this.wildHandGameTests.testWitheredHandIsImmuneToFire(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testBoggedHandRitualCreatesPoisoningCompanion(GameTestHelper helper) {
    this.companionAbilityGameTests.testBoggedHandRitualCreatesPoisoningCompanion(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testBoggedHandIsImmuneToPoison(GameTestHelper helper) {
    this.wildHandGameTests.testBoggedHandIsImmuneToPoison(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testWitchHandRitualCreatesHexingCompanion(GameTestHelper helper) {
    this.companionAbilityGameTests.testWitchHandRitualCreatesHexingCompanion(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testWitchHandMendsWoundedOwnerAndCompanions(GameTestHelper helper) {
    this.companionAbilityGameTests.testWitchHandMendsWoundedOwnerAndCompanions(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderBoneAndWitheredHandsGrantPassives(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderBoneAndWitheredHandsGrantPassives(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderRottenAndDrownedHandsGrantPassives(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderRottenAndDrownedHandsGrantPassives(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testShoulderPassiveAppliesOnPlayerTick(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderPassiveAppliesOnPlayerTick(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderHandsProtectFromHungerAndFreezing(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderHandsProtectFromHungerAndFreezing(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1")
  public void testShoulderHandsCurePoisonAndGrantNightVision(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderHandsCurePoisonAndGrantNightVision(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testShoulderHandsHoldOnThroughTheirDamage(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderHandsHoldOnThroughTheirDamage(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.BEHAVIOUR_MAX_TICKS)
  public void testShoulderHandsStayInTheirEnvironment(GameTestHelper helper) {
    this.shoulderGameTests.testShoulderHandsStayInTheirEnvironment(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testDrownedHandRitualNeedsKelp(GameTestHelper helper) {
    this.revivalRitualGameTests.testDrownedHandRitualNeedsKelp(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testBoneHandRitualCreatesCompanion(GameTestHelper helper) {
    this.revivalRitualGameTests.testBoneHandRitualCreatesCompanion(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.UNDERWATER_MAX_TICKS)
  public void testDrownedHandBreathesUnderwater(GameTestHelper helper) {
    this.wildHandGameTests.testDrownedHandBreathesUnderwater(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testDrownedHandExtinguishesBurningOwner(GameTestHelper helper) {
    this.companionAbilityGameTests.testDrownedHandExtinguishesBurningOwner(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testBoneHandWarnsAboutMonsterOnce(GameTestHelper helper) {
    this.companionAbilityGameTests.testBoneHandWarnsAboutMonsterOnce(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionWavesAtStrangerOnce(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionWavesAtStrangerOnce(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionKeepsBondAfterReload(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionKeepsBondAfterReload(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionDefendsAndJoinsOwner(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionDefendsAndJoinsOwner(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testWoundedCompanionRetreatsFromCombat(GameTestHelper helper) {
    this.companionBehaviourGameTests.testWoundedCompanionRetreatsFromCombat(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testCompanionIgnoresOwnerHittingSiblingCompanion(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionIgnoresOwnerHittingSiblingCompanion(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionIgnoresCreepersAndForeignPets(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionIgnoresCreepersAndForeignPets(helper);
  }

  @GameTest(structure = "wicked_hands:gametest.1x1x1", maxTicks = HandGameTests.RITUAL_MAX_TICKS)
  public void testCompanionHealsFromOffering(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionHealsFromOffering(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.AFTER_AWAKENING_MAX_TICKS)
  public void testCompanionRegeneratesOnlyOutOfCombat(GameTestHelper helper) {
    this.companionBehaviourGameTests.testCompanionRegeneratesOnlyOutOfCombat(helper);
  }

  @GameTest(
      structure = "wicked_hands:gametest.1x1x1",
      maxTicks = HandGameTests.TWO_RITUALS_MAX_TICKS)
  public void testFollowingCompanionJoinsOwnerInOtherDimension(GameTestHelper helper) {
    this.companionBehaviourGameTests.testFollowingCompanionJoinsOwnerInOtherDimension(helper);
  }
}
