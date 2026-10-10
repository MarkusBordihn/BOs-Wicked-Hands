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
  private static final RottenHandGameTests ROTTEN_HAND_TESTS = new RottenHandGameTests();

  static {
    register("smoke_mod_registered", SMOKE_TESTS::testModRegistered);
    register(
        "rotten_hand_spawns_naturally_at_night",
        ROTTEN_HAND_TESTS::testRottenHandSpawnsNaturallyAtNight);
    register(
        "rotten_hand_drops_lifeless_rotten_hand",
        ROTTEN_HAND_TESTS::testRottenHandDropsLifelessRottenHand);
    register(
        "summoned_rotten_hand_drops_lifeless_rotten_hand",
        ROTTEN_HAND_TESTS::testSummonedRottenHandDropsLifelessRottenHand);
    register("rotten_hand_attacks_animals", ROTTEN_HAND_TESTS::testRottenHandAttacksAnimals);
    register("rotten_hand_swims_to_surface", ROTTEN_HAND_TESTS::testRottenHandSwimsToSurface);
    register(
        "rotten_hand_uses_active_model_profile",
        ROTTEN_HAND_TESTS::testRottenHandUsesActiveModelProfile);
    register(
        "rotten_hand_despawns_when_far_away", ROTTEN_HAND_TESTS::testRottenHandDespawnsWhenFarAway);
    register(
        "zombie_drops_lifeless_rotten_hand", ROTTEN_HAND_TESTS::testZombieDropsLifelessRottenHand);
    register(
        "lifeless_rotten_hand_is_placed_on_soul_sand",
        ROTTEN_HAND_TESTS::testLifelessRottenHandIsPlacedOnSoulSand);
    register("revival_ritual_needs_candles", ROTTEN_HAND_TESTS::testRevivalRitualNeedsCandles);
    register(
        "revival_ritual_creates_companion", ROTTEN_HAND_TESTS::testRevivalRitualCreatesCompanion);
    register(
        "revival_ritual_grants_advancements",
        ROTTEN_HAND_TESTS::testRevivalRitualGrantsAdvancements);
    register("only_companion_grabs_target", ROTTEN_HAND_TESTS::testOnlyCompanionGrabsTarget);
    register(
        "fallen_companion_without_owner_leaves_protected_remains",
        ROTTEN_HAND_TESTS::testFallenCompanionWithoutOwnerLeavesProtectedRemains);
    register(
        "fallen_companion_returns_and_revives_with_same_identity",
        ROTTEN_HAND_TESTS::testFallenCompanionReturnsAndRevivesWithSameIdentity);
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
