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
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

abstract class ClientTestBase {

  static final String ROTTEN_HAND = "wicked_hands:rotten_hand";
  static final String LIFELESS_ROTTEN_HAND = "wicked_hands:lifeless_rotten_hand";
  static final int PLATFORM_Y = 100;
  static final int FLOOR_Y = PLATFORM_Y + 1;

  @RegisterExtension
  static final GameClientExtension client =
      GameClientExtension.shared(ClientTestBase::configureLaunch);

  private static final String SUITE_NAME = "Wicked Hands";
  private static final int MAXIMUM_SESSION_LABEL_LENGTH = 64;
  private static final Duration WORLD_TIMEOUT = Duration.ofMinutes(3);
  private static final long NOON = 6000L;
  private static final String TEST_AREA =
      "-8 " + (PLATFORM_Y - 1) + " -8 8 " + (PLATFORM_Y + 6) + " 8";

  private static GameClientBuilder configureLaunch(GameClientBuilder builder) {
    return builder
        .withSuiteName(SUITE_NAME)
        .withWindowSize(1280, 720)
        .withGuiScale(2)
        .withConfiguration(
            "command_allowlist",
            "fill,tp,gamerule,item,kill,summon,clear,gamemode,difficulty,effect,advancement");
  }

  @BeforeAll
  static void loadWorld() {
    if (!client.checkState(Until.worldLoaded(true)).passed()) {
      client.await(WORLD_TIMEOUT, Until.resourcesLoaded());
      client.api().world().create(Parameters.of("name", "Wicked Hands Client Test"));
    }
    client.await(
        WORLD_TIMEOUT, Until.worldLoaded(true), Until.playerAvailable(true), Until.noScreen());
    command("gamerule send_command_feedback false");
    command("difficulty normal");
  }

  static void command(String command) {
    client.api().command().run(Parameters.of("command", command));
  }

  static void enterTestArea() {
    command("fill -6 " + PLATFORM_Y + " -6 6 " + PLATFORM_Y + " 6 minecraft:stone");
    command("tp @s 0.5 " + FLOOR_Y + " 0.5 0 0");
    client.await(Until.ticksElapsed(5));
  }

  static void summonRottenHand(double x, double z, boolean withoutAi) {
    command(
        "summon "
            + ROTTEN_HAND
            + " "
            + x
            + " "
            + FLOOR_Y
            + " "
            + z
            + (withoutAi ? " {NoAI:1b}" : ""));
  }

  static void lookAtPoint(double x, double y, double z) {
    client
        .api()
        .camera()
        .look(
            Parameters.of()
                .with("lookAtPoint", Parameters.of().with("x", x).with("y", y).with("z", z)));
    client.await(Until.ticksElapsed(2));
  }

  static void renderProfile(String hud) {
    client
        .api()
        .render()
        .profile(
            Parameters.of("preset", "deterministic")
                .with("dayTime", NOON)
                .with("weather", "clear")
                .with("hud", hud));
  }

  @BeforeEach
  void labelSessionWithTest() {
    String title = client.currentTest().title();
    client
        .api()
        .session()
        .label(
            Parameters.of(
                "name",
                title.substring(0, Math.min(title.length(), MAXIMUM_SESSION_LABEL_LENGTH))));
  }

  @BeforeEach
  void renderWithoutHudAtNoon() {
    renderProfile("hidden");
  }

  @AfterEach
  void clearTestArea() {
    if (!Until.NO_SCREEN_ID.equals(client.screenId())) {
      client.closeScreen();
    }
    command("gamemode creative");
    command("kill @e[type=" + ROTTEN_HAND + "]");
    command("kill @e[type=item]");
    command("fill " + TEST_AREA + " minecraft:air");
    command("clear @s");
  }
}
