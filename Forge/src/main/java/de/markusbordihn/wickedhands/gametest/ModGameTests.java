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
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;

public final class ModGameTests {

  private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(Registries.TEST_FUNCTION, Constants.MOD_ID);

  private static final SmokeTest SMOKE_TESTS = new SmokeTest();
  private static final HandGameTests HAND_TESTS = new HandGameTests();

  static {
    register("smoke_mod_registered", SMOKE_TESTS::testModRegistered);
    register(
        "rotten_hand_spawns_naturally_at_night", HAND_TESTS::testRottenHandSpawnsNaturallyAtNight);
    register(
        "rotten_hand_drops_lifeless_rotten_hand",
        HAND_TESTS::testRottenHandDropsLifelessRottenHand);
    register(
        "summoned_rotten_hand_drops_lifeless_rotten_hand",
        HAND_TESTS::testSummonedRottenHandDropsLifelessRottenHand);
    register("rotten_hand_attacks_animals", HAND_TESTS::testRottenHandAttacksAnimals);
    register("rotten_hand_swims_to_surface", HAND_TESTS::testRottenHandSwimsToSurface);
    register(
        "rotten_hand_uses_active_model_profile", HAND_TESTS::testRottenHandUsesActiveModelProfile);
    register("rotten_hand_despawns_when_far_away", HAND_TESTS::testRottenHandDespawnsWhenFarAway);
    register("monsters_drop_their_lifeless_hand", HAND_TESTS::testMonstersDropTheirLifelessHand);
    register(
        "lifeless_rotten_hand_is_placed_on_soul_sand",
        HAND_TESTS::testLifelessRottenHandIsPlacedOnSoulSand);
    register("revival_ritual_needs_candles", HAND_TESTS::testRevivalRitualNeedsCandles);
    register("revival_ritual_creates_companion", HAND_TESTS::testRevivalRitualCreatesCompanion);
    register("revival_ritual_grants_advancements", HAND_TESTS::testRevivalRitualGrantsAdvancements);
    register("only_companion_grabs_target", HAND_TESTS::testOnlyCompanionGrabsTarget);
    register(
        "fallen_companion_without_owner_leaves_protected_remains",
        HAND_TESTS::testFallenCompanionWithoutOwnerLeavesProtectedRemains);
    register(
        "stranger_cannot_revive_foreign_remains",
        HAND_TESTS::testStrangerCannotReviveForeignRemains);
    register(
        "fallen_companion_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenCompanionReturnsAndRevivesWithSameIdentity);
    register(
        "fallen_drowned_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenDrownedHandReturnsAndRevivesWithSameIdentity);
    register(
        "fallen_bone_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenBoneHandReturnsAndRevivesWithSameIdentity);
    register(
        "fallen_husk_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenHuskHandReturnsAndRevivesWithSameIdentity);
    register(
        "fallen_stray_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenStrayHandReturnsAndRevivesWithSameIdentity);
    register(
        "fallen_withered_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenWitheredHandReturnsAndRevivesWithSameIdentity);
    register(
        "husk_hand_ritual_creates_weakening_companion",
        HAND_TESTS::testHuskHandRitualCreatesWeakeningCompanion);
    register(
        "stray_hand_ritual_creates_chilling_companion",
        HAND_TESTS::testStrayHandRitualCreatesChillingCompanion);
    register(
        "withered_hand_ritual_creates_withering_companion",
        HAND_TESTS::testWitheredHandRitualCreatesWitheringCompanion);
    register("withered_hand_is_immune_to_fire", HAND_TESTS::testWitheredHandIsImmuneToFire);
    register(
        "shoulder_bone_and_withered_hands_grant_passives",
        HAND_TESTS::testShoulderBoneAndWitheredHandsGrantPassives);
    register(
        "shoulder_rotten_and_drowned_hands_grant_passives",
        HAND_TESTS::testShoulderRottenAndDrownedHandsGrantPassives);
    register(
        "shoulder_passive_applies_on_player_tick",
        HAND_TESTS::testShoulderPassiveAppliesOnPlayerTick);
    register(
        "shoulder_hands_protect_from_hunger_and_freezing",
        HAND_TESTS::testShoulderHandsProtectFromHungerAndFreezing);
    register("drowned_hand_ritual_needs_kelp", HAND_TESTS::testDrownedHandRitualNeedsKelp);
    register("bone_hand_ritual_creates_companion", HAND_TESTS::testBoneHandRitualCreatesCompanion);
    register("drowned_hand_breathes_underwater", HAND_TESTS::testDrownedHandBreathesUnderwater);
    register(
        "drowned_hand_extinguishes_burning_owner",
        HAND_TESTS::testDrownedHandExtinguishesBurningOwner);
    register("bone_hand_warns_about_monster_once", HAND_TESTS::testBoneHandWarnsAboutMonsterOnce);
    register("companion_waves_at_stranger_once", HAND_TESTS::testCompanionWavesAtStrangerOnce);
    register("companion_keeps_bond_after_reload", HAND_TESTS::testCompanionKeepsBondAfterReload);
    register("companion_defends_and_joins_owner", HAND_TESTS::testCompanionDefendsAndJoinsOwner);
    register(
        "companion_ignores_owner_hitting_sibling_companion",
        HAND_TESTS::testCompanionIgnoresOwnerHittingSiblingCompanion);
    register(
        "following_companion_joins_owner_in_other_dimension",
        HAND_TESTS::testFollowingCompanionJoinsOwnerInOtherDimension);
  }

  private ModGameTests() {}

  public static void register(BusGroup modBusGroup) {
    if (FMLLoader.isProduction()) {
      return;
    }

    TEST_FUNCTIONS.register(modBusGroup);
  }

  private static void register(String name, Consumer<GameTestHelper> testFunction) {
    TEST_FUNCTIONS.register(name, () -> testFunction);
  }
}
