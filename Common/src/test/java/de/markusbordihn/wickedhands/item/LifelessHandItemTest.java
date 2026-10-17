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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.TestBootstrap;
import de.markusbordihn.wickedhands.TestResources;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import java.nio.file.Files;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;

class LifelessHandItemTest {

  @BeforeAll
  static void bootstrap() {
    TestBootstrap.bootstrap();
  }

  private static Identifier itemId(String lifelessHandId) {
    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, lifelessHandId);
  }

  // Item's constructor needs an unfrozen item registry, so the real use() runs on a mock.
  private static LifelessHandItem createItem(String lifelessHandId) {
    LifelessHandItem item =
        mock(LifelessHandItem.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
    doReturn(itemId(lifelessHandId).toLanguageKey("item")).when(item).getDescriptionId();
    return item;
  }

  private static boolean isRevivalWhisper(Component component, String lifelessHandId) {
    return component.getContents() instanceof TranslatableContents whisper
        && whisper.getKey().equals("message.wicked_hands.hand.whisper")
        && whisper.getArgs()[1] instanceof Component hint
        && hint.getContents() instanceof TranslatableContents hintContents
        && hintContents
            .getKey()
            .equals(itemId(lifelessHandId).toLanguageKey("item", "revival_hint"));
  }

  private static Stream<Identifier> itemIds() {
    return Stream.of(
        LifelessRottenHandEntity.ENTITY_ID,
        LifelessDrownedHandEntity.ENTITY_ID,
        LifelessBoneHandEntity.ENTITY_ID,
        HandSpawnEgg.itemId(RottenHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(DrownedHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(BoneHandEntity.ENTITY_ID));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        LifelessRottenHandEntity.ID,
        LifelessDrownedHandEntity.ID,
        LifelessBoneHandEntity.ID
      })
  @DisplayName("Using the item on the server whispers its revival hint and starts a cooldown")
  void useOnServerWhispersRevivalHint(String lifelessHandId) {
    Level level = mock(Level.class);
    when(level.isClientSide()).thenReturn(false);
    ItemStack itemStack = mock(ItemStack.class);
    when(itemStack.getHoverName()).thenReturn(Component.literal(lifelessHandId));
    ItemCooldowns cooldowns = mock(ItemCooldowns.class);
    Player player = mock(Player.class);
    when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(itemStack);
    when(player.getCooldowns()).thenReturn(cooldowns);

    assertEquals(
        InteractionResult.SUCCESS,
        createItem(lifelessHandId).use(level, player, InteractionHand.MAIN_HAND));
    verify(player)
        .sendSystemMessage(argThat(message -> isRevivalWhisper(message, lifelessHandId)));
    verify(cooldowns).addCooldown(eq(itemStack), anyInt());
  }

  @Test
  @DisplayName("Using the item on the client sends no message")
  void useOnClientSendsNoMessage() {
    Level level = mock(Level.class);
    when(level.isClientSide()).thenReturn(true);
    Player player = mock(Player.class);

    assertEquals(
        InteractionResult.SUCCESS,
        createItem(LifelessRottenHandEntity.ID).use(level, player, InteractionHand.OFF_HAND));
    verify(player, never()).sendSystemMessage(any());
  }

  @ParameterizedTest
  @MethodSource("itemIds")
  @DisplayName("Item definition, model and texture exist")
  void itemModelResourcesExist(Identifier itemId) {
    String modelId =
        TestResources.readJson(TestResources.assetPath(itemId.toString(), "items/", ".json"))
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

  @ParameterizedTest
  @ValueSource(
      strings = {
        LifelessRottenHandEntity.ID,
        LifelessDrownedHandEntity.ID,
        LifelessBoneHandEntity.ID
      })
  @DisplayName("Lifeless hands item and entity tags contain the hand")
  void lifelessHandsTagsContainHand(String lifelessHandId) {
    for (String tagType : new String[] {"item", "entity_type"}) {
      assertTrue(
          TestResources.readJson("data/wicked_hands/tags/" + tagType + "/lifeless_hands.json")
              .getAsJsonArray("values")
              .asList()
              .stream()
              .anyMatch(value -> value.getAsString().equals(itemId(lifelessHandId).toString())),
          tagType + " tag misses " + lifelessHandId);
    }
  }
}
