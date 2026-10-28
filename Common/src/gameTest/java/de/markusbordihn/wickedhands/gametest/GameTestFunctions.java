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

import de.markusbordihn.wickedhands.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

public final class GameTestFunctions {

  private static final int DEFAULT_MAX_TICKS = 100;
  private static final String STRUCTURE_1X1X1 = Constants.MOD_ID + ":gametest.1x1x1";
  private static final List<TestFunction> TEST_FUNCTIONS = new ArrayList<>();

  private static final WildHandGameTests WILD_HAND_TESTS = new WildHandGameTests();
  private static final RevivalRitualGameTests REVIVAL_RITUAL_TESTS = new RevivalRitualGameTests();
  private static final CompanionAbilityGameTests COMPANION_ABILITY_TESTS =
      new CompanionAbilityGameTests();
  private static final CompanionBehaviourGameTests COMPANION_BEHAVIOUR_TESTS =
      new CompanionBehaviourGameTests();
  private static final ShoulderGameTests SHOULDER_TESTS = new ShoulderGameTests();

  static {
    register(
        "rotten_hand_spawns_naturally_at_night",
        WILD_HAND_TESTS::testRottenHandSpawnsNaturallyAtNight);
    register(
        "summoned_rotten_hand_drops_lifeless_rotten_hand",
        WILD_HAND_TESTS::testSummonedRottenHandDropsLifelessRottenHand);
    register("rotten_hand_attacks_animals", WILD_HAND_TESTS::testRottenHandAttacksAnimals);
    register("rotten_hand_swims_to_surface", WILD_HAND_TESTS::testRottenHandSwimsToSurface);
    register(
        "rotten_hand_uses_active_model_profile",
        WILD_HAND_TESTS::testRottenHandUsesActiveModelProfile);
    register(
        "rotten_hand_despawns_when_far_away", WILD_HAND_TESTS::testRottenHandDespawnsWhenFarAway);
    register(
        "monsters_drop_their_lifeless_hand", WILD_HAND_TESTS::testMonstersDropTheirLifelessHand);
    register(
        "lifeless_rotten_hand_is_placed_on_soul_sand",
        REVIVAL_RITUAL_TESTS::testLifelessRottenHandIsPlacedOnSoulSand);
    register("revival_ritual_needs_candles", REVIVAL_RITUAL_TESTS::testRevivalRitualNeedsCandles);
    register(
        "revival_ritual_creates_companion",
        REVIVAL_RITUAL_TESTS::testRevivalRitualCreatesCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "revival_ritual_grants_advancements",
        REVIVAL_RITUAL_TESTS::testRevivalRitualGrantsAdvancements,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "only_companion_grabs_target",
        COMPANION_ABILITY_TESTS::testOnlyCompanionGrabsTarget,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "fallen_companion_without_owner_leaves_protected_remains",
        REVIVAL_RITUAL_TESTS::testFallenCompanionWithoutOwnerLeavesProtectedRemains,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "fallen_companion_with_full_inventory_leaves_protected_remains",
        REVIVAL_RITUAL_TESTS::testFallenCompanionWithFullInventoryLeavesProtectedRemains,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "bound_remains_resist_all_but_their_owner",
        REVIVAL_RITUAL_TESTS::testBoundRemainsResistAllButTheirOwner,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "running_ritual_protects_lifeless_hand",
        REVIVAL_RITUAL_TESTS::testRunningRitualProtectsLifelessHand,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "stranger_cannot_revive_foreign_remains",
        REVIVAL_RITUAL_TESTS::testStrangerCannotReviveForeignRemains,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "fallen_companion_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenCompanionReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_drowned_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenDrownedHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_bone_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenBoneHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_husk_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenHuskHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_stray_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenStrayHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_withered_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenWitheredHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_bogged_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenBoggedHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_witch_hand_returns_and_revives_with_same_identity",
        REVIVAL_RITUAL_TESTS::testFallenWitchHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "husk_hand_ritual_creates_weakening_companion",
        COMPANION_ABILITY_TESTS::testHuskHandRitualCreatesWeakeningCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "stray_hand_ritual_creates_chilling_companion",
        COMPANION_ABILITY_TESTS::testStrayHandRitualCreatesChillingCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "withered_hand_ritual_creates_withering_companion",
        COMPANION_ABILITY_TESTS::testWitheredHandRitualCreatesWitheringCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register("withered_hand_is_immune_to_fire", WILD_HAND_TESTS::testWitheredHandIsImmuneToFire);
    register(
        "bogged_hand_ritual_creates_poisoning_companion",
        COMPANION_ABILITY_TESTS::testBoggedHandRitualCreatesPoisoningCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register("bogged_hand_is_immune_to_poison", WILD_HAND_TESTS::testBoggedHandIsImmuneToPoison);
    register(
        "witch_hand_ritual_creates_hexing_companion",
        COMPANION_ABILITY_TESTS::testWitchHandRitualCreatesHexingCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "witch_hand_mends_wounded_owner_and_companions",
        COMPANION_ABILITY_TESTS::testWitchHandMendsWoundedOwnerAndCompanions,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "shoulder_bone_and_withered_hands_grant_passives",
        SHOULDER_TESTS::testShoulderBoneAndWitheredHandsGrantPassives);
    register(
        "shoulder_rotten_and_drowned_hands_grant_passives",
        SHOULDER_TESTS::testShoulderRottenAndDrownedHandsGrantPassives);
    register(
        "shoulder_passive_applies_on_player_tick",
        SHOULDER_TESTS::testShoulderPassiveAppliesOnPlayerTick,
        HandGameTests.BEHAVIOUR_MAX_TICKS);
    register(
        "shoulder_hands_protect_from_hunger_and_freezing",
        SHOULDER_TESTS::testShoulderHandsProtectFromHungerAndFreezing);
    register(
        "shoulder_hands_cure_poison_and_grant_night_vision",
        SHOULDER_TESTS::testShoulderHandsCurePoisonAndGrantNightVision);
    register(
        "shoulder_hands_hold_on_through_their_damage",
        SHOULDER_TESTS::testShoulderHandsHoldOnThroughTheirDamage,
        HandGameTests.BEHAVIOUR_MAX_TICKS);
    register(
        "shoulder_hands_stay_in_their_environment",
        SHOULDER_TESTS::testShoulderHandsStayInTheirEnvironment,
        HandGameTests.BEHAVIOUR_MAX_TICKS);
    register(
        "drowned_hand_ritual_needs_kelp",
        REVIVAL_RITUAL_TESTS::testDrownedHandRitualNeedsKelp,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "bone_hand_ritual_creates_companion",
        REVIVAL_RITUAL_TESTS::testBoneHandRitualCreatesCompanion,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "drowned_hand_breathes_underwater",
        WILD_HAND_TESTS::testDrownedHandBreathesUnderwater,
        HandGameTests.UNDERWATER_MAX_TICKS);
    register(
        "drowned_hand_extinguishes_burning_owner",
        COMPANION_ABILITY_TESTS::testDrownedHandExtinguishesBurningOwner,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "bone_hand_warns_about_monster_once",
        COMPANION_ABILITY_TESTS::testBoneHandWarnsAboutMonsterOnce,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_waves_at_stranger_once",
        COMPANION_BEHAVIOUR_TESTS::testCompanionWavesAtStrangerOnce,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_keeps_bond_after_reload",
        COMPANION_BEHAVIOUR_TESTS::testCompanionKeepsBondAfterReload,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_defends_and_joins_owner",
        COMPANION_BEHAVIOUR_TESTS::testCompanionDefendsAndJoinsOwner,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "wounded_companion_retreats_from_combat",
        COMPANION_BEHAVIOUR_TESTS::testWoundedCompanionRetreatsFromCombat,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_ignores_owner_hitting_sibling_companion",
        COMPANION_BEHAVIOUR_TESTS::testCompanionIgnoresOwnerHittingSiblingCompanion,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "companion_ignores_creepers_and_foreign_pets",
        COMPANION_BEHAVIOUR_TESTS::testCompanionIgnoresCreepersAndForeignPets,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_heals_from_offering",
        COMPANION_BEHAVIOUR_TESTS::testCompanionHealsFromOffering,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "companion_regenerates_only_out_of_combat",
        COMPANION_BEHAVIOUR_TESTS::testCompanionRegeneratesOnlyOutOfCombat,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "following_companion_joins_owner_in_other_dimension",
        COMPANION_BEHAVIOUR_TESTS::testFollowingCompanionJoinsOwnerInOtherDimension,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
  }

  private GameTestFunctions() {}

  public static List<TestFunction> create() {
    return List.copyOf(TEST_FUNCTIONS);
  }

  private static void register(String name, Consumer<GameTestHelper> testFunction) {
    register(name, testFunction, DEFAULT_MAX_TICKS);
  }

  // Forge 52 only runs tests whose batch starts with an enabled game test namespace.
  private static void register(String name, Consumer<GameTestHelper> testFunction, int maxTicks) {
    TEST_FUNCTIONS.add(
        new TestFunction(
            Constants.MOD_ID, name, STRUCTURE_1X1X1, maxTicks, 0, true, testFunction));
  }
}
