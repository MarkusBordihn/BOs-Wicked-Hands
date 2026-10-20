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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public final class ModGameTests {

  private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, Constants.MOD_ID);
  private static final List<TestEntry> TEST_ENTRIES = new ArrayList<>();
  private static final int DEFAULT_MAX_TICKS = 100;
  private static final Identifier STRUCTURE_1X1X1 =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "gametest.1x1x1");
  private static final Identifier DEFAULT_ENVIRONMENT =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "default");

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
    register(
        "revival_ritual_creates_companion",
        HAND_TESTS::testRevivalRitualCreatesCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "revival_ritual_grants_advancements",
        HAND_TESTS::testRevivalRitualGrantsAdvancements,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "only_companion_grabs_target",
        HAND_TESTS::testOnlyCompanionGrabsTarget,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "fallen_companion_without_owner_leaves_protected_remains",
        HAND_TESTS::testFallenCompanionWithoutOwnerLeavesProtectedRemains,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "stranger_cannot_revive_foreign_remains",
        HAND_TESTS::testStrangerCannotReviveForeignRemains,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "fallen_companion_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenCompanionReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_drowned_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenDrownedHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_bone_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenBoneHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_husk_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenHuskHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_stray_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenStrayHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "fallen_withered_hand_returns_and_revives_with_same_identity",
        HAND_TESTS::testFallenWitheredHandReturnsAndRevivesWithSameIdentity,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "husk_hand_ritual_creates_weakening_companion",
        HAND_TESTS::testHuskHandRitualCreatesWeakeningCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "stray_hand_ritual_creates_chilling_companion",
        HAND_TESTS::testStrayHandRitualCreatesChillingCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "withered_hand_ritual_creates_withering_companion",
        HAND_TESTS::testWitheredHandRitualCreatesWitheringCompanion,
        HandGameTests.RITUAL_MAX_TICKS);
    register("withered_hand_is_immune_to_fire", HAND_TESTS::testWitheredHandIsImmuneToFire);
    register(
        "shoulder_bone_and_withered_hands_grant_passives",
        HAND_TESTS::testShoulderBoneAndWitheredHandsGrantPassives);
    register(
        "shoulder_rotten_and_drowned_hands_grant_passives",
        HAND_TESTS::testShoulderRottenAndDrownedHandsGrantPassives);
    register(
        "shoulder_passive_applies_on_player_tick",
        HAND_TESTS::testShoulderPassiveAppliesOnPlayerTick,
        HandGameTests.BEHAVIOUR_MAX_TICKS);
    register(
        "shoulder_hands_protect_from_hunger_and_freezing",
        HAND_TESTS::testShoulderHandsProtectFromHungerAndFreezing);
    register(
        "drowned_hand_ritual_needs_kelp",
        HAND_TESTS::testDrownedHandRitualNeedsKelp,
        HandGameTests.RITUAL_MAX_TICKS);
    register(
        "bone_hand_ritual_creates_companion",
        HAND_TESTS::testBoneHandRitualCreatesCompanion,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "drowned_hand_breathes_underwater",
        HAND_TESTS::testDrownedHandBreathesUnderwater,
        HandGameTests.UNDERWATER_MAX_TICKS);
    register(
        "drowned_hand_extinguishes_burning_owner",
        HAND_TESTS::testDrownedHandExtinguishesBurningOwner,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "bone_hand_warns_about_monster_once",
        HAND_TESTS::testBoneHandWarnsAboutMonsterOnce,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_waves_at_stranger_once",
        HAND_TESTS::testCompanionWavesAtStrangerOnce,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_keeps_bond_after_reload",
        HAND_TESTS::testCompanionKeepsBondAfterReload,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_defends_and_joins_owner",
        HAND_TESTS::testCompanionDefendsAndJoinsOwner,
        HandGameTests.AFTER_AWAKENING_MAX_TICKS);
    register(
        "companion_ignores_owner_hitting_sibling_companion",
        HAND_TESTS::testCompanionIgnoresOwnerHittingSiblingCompanion,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
    register(
        "following_companion_joins_owner_in_other_dimension",
        HAND_TESTS::testFollowingCompanionJoinsOwnerInOtherDimension,
        HandGameTests.TWO_RITUALS_MAX_TICKS);
  }

  private ModGameTests() {}

  public static void register(IEventBus modEventBus) {
    if (FMLEnvironment.isProduction()) {
      return;
    }

    TEST_FUNCTIONS.register(modEventBus);
  }

  @SubscribeEvent
  public static void registerGameTests(RegisterGameTestsEvent event) {
    Holder<TestEnvironmentDefinition<?>> environment =
        event.registerEnvironment(
            DEFAULT_ENVIRONMENT, new TestEnvironmentDefinition.AllOf(List.of()));
    for (TestEntry testEntry : TEST_ENTRIES) {
      event.registerTest(
          testEntry.testFunction().getId(),
          new FunctionGameTestInstance(
              testEntry.testFunction().getKey(),
              new TestData<>(environment, STRUCTURE_1X1X1, testEntry.maxTicks(), 0, true)));
    }
  }

  private static void register(String name, Consumer<GameTestHelper> testFunction) {
    register(name, testFunction, DEFAULT_MAX_TICKS);
  }

  private static void register(String name, Consumer<GameTestHelper> testFunction, int maxTicks) {
    TEST_ENTRIES.add(new TestEntry(TEST_FUNCTIONS.register(name, () -> testFunction), maxTicks));
  }

  private record TestEntry(
      DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> testFunction,
      int maxTicks) {}
}
