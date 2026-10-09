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

package de.markusbordihn.wickedhands.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import de.markusbordihn.wickedhands.TestBootstrap;
import de.markusbordihn.wickedhands.TestResources;
import java.nio.file.Files;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class LifelessRottenHandItemTest {

  private static final String REVIVAL_HINT_TRANSLATION_KEY =
      "item.wicked_hands.lifeless_rotten_hand.revival_hint";
  private static final String ITEM_ID = LifelessRottenHandItem.ITEM_ID.toString();

  @BeforeAll
  static void bootstrap() {
    TestBootstrap.bootstrap();
  }

  // Item's constructor needs an unfrozen item registry, so the real use() runs on a mock.
  private static LifelessRottenHandItem createItem() {
    return mock(
        LifelessRottenHandItem.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
  }

  private static boolean isRevivalHint(Component component) {
    return component.getContents() instanceof TranslatableContents translatableContents
        && translatableContents.getKey().equals(REVIVAL_HINT_TRANSLATION_KEY);
  }

  @Test
  @DisplayName("Using the item on the server shows the revival hint")
  void useOnServerShowsRevivalHint() {
    Level level = mock(Level.class);
    when(level.isClientSide()).thenReturn(false);
    Player player = mock(Player.class);

    assertEquals(
        InteractionResult.SUCCESS, createItem().use(level, player, InteractionHand.MAIN_HAND));
    verify(player).sendOverlayMessage(argThat(LifelessRottenHandItemTest::isRevivalHint));
  }

  @Test
  @DisplayName("Using the item on the client sends no message")
  void useOnClientSendsNoMessage() {
    Level level = mock(Level.class);
    when(level.isClientSide()).thenReturn(true);
    Player player = mock(Player.class);

    assertEquals(
        InteractionResult.SUCCESS, createItem().use(level, player, InteractionHand.OFF_HAND));
    verify(player, never()).sendOverlayMessage(any());
  }

  @Test
  @DisplayName("Item definition, model and texture exist")
  void itemModelResourcesExist() {
    String modelId =
        TestResources.readJson(TestResources.assetPath(ITEM_ID, "items/", ".json"))
            .getAsJsonObject("model")
            .get("model")
            .getAsString();
    TestResources.readJson(TestResources.assetPath(modelId, "models/", ".json"))
        .getAsJsonObject("textures")
        .asMap()
        .values()
        .forEach(
            texture ->
                assertTrue(
                    Files.exists(
                        TestResources.assetPath(texture.getAsString(), "textures/", ".png")),
                    "Missing item texture " + texture.getAsString()));
  }

  @Test
  @DisplayName("Lifeless hands item tag contains the item")
  void lifelessHandsTagContainsItem() {
    assertTrue(
        TestResources.readJson("data/wicked_hands/tags/item/lifeless_hands.json")
            .getAsJsonArray("values")
            .asList()
            .stream()
            .anyMatch(value -> value.getAsString().equals(ITEM_ID)));
  }
}
