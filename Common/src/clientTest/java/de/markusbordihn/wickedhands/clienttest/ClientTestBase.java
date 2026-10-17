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

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.GameClientBuilder;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.GameClientExtension;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Difficulty;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.TestArea;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.WorldSetup;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.Entity;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;

abstract class ClientTestBase {

  static final String MOD_ID = "wicked_hands";
  static final String ROTTEN_HAND = MOD_ID + ":rotten_hand";
  static final String LIFELESS_ROTTEN_HAND = MOD_ID + ":lifeless_rotten_hand";
  static final int PLATFORM_Y = 100;
  static final int FLOOR_Y = PLATFORM_Y + 1;
  static final int HAND_BLOCK_X = 0;
  static final int HAND_BLOCK_Z = 3;
  static final double HAND_X = HAND_BLOCK_X + 0.5D;
  static final double HAND_Z = HAND_BLOCK_Z + 0.5D;
  static final long RENDER_TICKS = 20;

  @RegisterExtension
  static final GameClientExtension client =
      GameClientExtension.shared(ClientTestBase::configureLaunch);

  private static final long NOON = 6000L;
  private static final long LOOK_TICKS = 2;
  private static final long LANDING_TICKS = 5;
  private static final List<UUID> entitiesToRemoveAfterTest = new ArrayList<>();

  private static GameClientBuilder configureLaunch(GameClientBuilder builder) {
    return builder
        .withSuiteName("Wicked Hands")
        .withWindowSize(1280, 720)
        .withGuiScale(2)
        .withAllowedCommands("summon", "kill", "effect", "advancement", "easy_npc")
        .withWorld(
            WorldSetup.creative()
                .withDifficulty(Difficulty.NORMAL)
                .withGameRule("spawn_mobs", false)
                .withGameRule("send_command_feedback", false))
        .withRenderProfile(
            Parameters.of("preset", "deterministic").with("dayTime", NOON).with("weather", "clear"))
        .withRenderSettings(
            Parameters.of().with("gui", false).with("chat", false).with("toasts", false));
  }

  static void enterTestArea() {
    client.fill(TestArea.between(-6, PLATFORM_Y, -6, 6, PLATFORM_Y, 6), "minecraft:stone");
    client.teleport(0.5D, FLOOR_Y, 0.5D, 0F, 0F);
    client.await(Until.ticksElapsed(LANDING_TICKS));
  }

  static Entity spawnStaticHand(String handType) {
    UUID handUUID = client.spawnEntity(handType, HAND_X, FLOOR_Y, HAND_Z, true);
    return client.awaitEntity(Parameters.of("uuid", handUUID.toString()), hand -> true);
  }

  static Entity awaitEntityOfType(String entityType) {
    return client.awaitEntity(Parameters.of("type", entityType), entity -> true);
  }

  static Entity removeAfterTest(Entity entity) {
    entitiesToRemoveAfterTest.add(UUID.fromString(entity.uuid()));
    return entity;
  }

  static Entity placeLifelessHand(String lifelessHand) {
    client.setBlock(HAND_BLOCK_X, PLATFORM_Y, HAND_BLOCK_Z, "minecraft:soul_sand");
    client.equip(lifelessHand);
    client.lookAtPoint(HAND_X, FLOOR_Y - 0.1D, HAND_Z);
    client.await(Until.ticksElapsed(LOOK_TICKS));
    client.useHeldItem();
    return removeAfterTest(awaitEntityOfType(lifelessHand));
  }

  private static boolean isPresent(Parameters entityQuery) {
    return client.checkState(Until.entityPresent(entityQuery)).passed();
  }

  static void lookAtCenterOf(Entity entity) {
    Position min = entity.boundingBox().min();
    Position max = entity.boundingBox().max();
    client.lookAtPoint(
        (min.x() + max.x()) / 2D, (min.y() + max.y()) / 2D, (min.z() + max.z()) / 2D);
    client.await(Until.ticksElapsed(LOOK_TICKS));
  }

  @AfterEach
  void resetPlayerAndWorld() {
    for (UUID entityUUID : entitiesToRemoveAfterTest) {
      if (isPresent(Parameters.of("uuid", entityUUID.toString()))) {
        client.runCommand("easy_npc delete " + entityUUID);
      }
    }
    entitiesToRemoveAfterTest.clear();
    if (isPresent(Parameters.of("type", "minecraft:item"))) {
      client.runCommand("kill @e[type=minecraft:item]");
    }
    client.clearInventory();
  }
}
