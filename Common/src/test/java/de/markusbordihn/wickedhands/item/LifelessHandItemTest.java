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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import de.markusbordihn.wickedhands.entity.BoggedHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.HuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoggedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessHuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessStrayHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitchHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitheredHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.entity.StrayHandEntity;
import de.markusbordihn.wickedhands.entity.WitchHandEntity;
import de.markusbordihn.wickedhands.entity.WitheredHandEntity;
import java.nio.file.Files;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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

  private static ResourceLocation itemId(String lifelessHandId) {
    return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, lifelessHandId);
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

  private static Stream<ResourceLocation> itemIds() {
    return Stream.of(
        LifelessRottenHandEntity.ENTITY_ID,
        LifelessDrownedHandEntity.ENTITY_ID,
        LifelessBoneHandEntity.ENTITY_ID,
        LifelessHuskHandEntity.ENTITY_ID,
        LifelessStrayHandEntity.ENTITY_ID,
        LifelessWitheredHandEntity.ENTITY_ID,
        LifelessBoggedHandEntity.ENTITY_ID,
        LifelessWitchHandEntity.ENTITY_ID,
        HandSpawnEgg.itemId(RottenHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(DrownedHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(BoneHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(HuskHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(StrayHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(WitheredHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(BoggedHandEntity.ENTITY_ID),
        HandSpawnEgg.itemId(WitchHandEntity.ENTITY_ID));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        LifelessRottenHandEntity.ID,
        LifelessDrownedHandEntity.ID,
        LifelessBoneHandEntity.ID,
        LifelessHuskHandEntity.ID,
        LifelessStrayHandEntity.ID,
        LifelessWitheredHandEntity.ID,
        LifelessBoggedHandEntity.ID,
        LifelessWitchHandEntity.ID
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
    LifelessHandItem item = createItem(lifelessHandId);

    assertEquals(
        InteractionResult.SUCCESS,
        item.use(level, player, InteractionHand.MAIN_HAND).getResult());
    verify(player)
        .displayClientMessage(
            argThat(message -> isRevivalWhisper(message, lifelessHandId)), eq(false));
    verify(cooldowns).addCooldown(eq(item), anyInt());
  }

  @Test
  @DisplayName("Using the item on the client sends no message")
  void useOnClientSendsNoMessage() {
    Level level = mock(Level.class);
    when(level.isClientSide()).thenReturn(true);
    Player player = mock(Player.class);

    assertEquals(
        InteractionResult.SUCCESS,
        createItem(LifelessRottenHandEntity.ID)
            .use(level, player, InteractionHand.OFF_HAND)
            .getResult());
    verify(player, never()).displayClientMessage(any(), anyBoolean());
  }

  @Test
  @DisplayName("Protected remains never burn, despawn or go to anyone but their owner")
  void protectRemainsBindsDroppedItemToOwner() {
    ItemEntity remains = mock(ItemEntity.class);
    UUID ownerUUID = UUID.randomUUID();

    LifelessHandItem.protectRemains(remains, ownerUUID);
    verify(remains).setInvulnerable(true);
    verify(remains).setUnlimitedLifetime();
    verify(remains).setTarget(ownerUUID);
  }

  @Test
  @DisplayName("Protecting remains that could not be dropped does nothing")
  void protectRemainsIgnoresMissingItem() {
    assertDoesNotThrow(() -> LifelessHandItem.protectRemains(null, UUID.randomUUID()));
  }

  @ParameterizedTest
  @MethodSource("itemIds")
  @DisplayName("Item model and texture exist")
  void itemModelResourcesExist(ResourceLocation itemId) {
    TestResources.readJson(
            TestResources.assetPath(itemId.withPrefix("item/").toString(), "models/", ".json"))
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
        LifelessBoneHandEntity.ID,
        LifelessHuskHandEntity.ID,
        LifelessStrayHandEntity.ID,
        LifelessWitheredHandEntity.ID,
        LifelessBoggedHandEntity.ID,
        LifelessWitchHandEntity.ID
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
