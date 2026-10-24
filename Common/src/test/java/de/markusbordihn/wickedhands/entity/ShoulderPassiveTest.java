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

package de.markusbordihn.wickedhands.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.wickedhands.TestBootstrap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShoulderPassiveTest {

  @BeforeAll
  static void bootstrap() {
    TestBootstrap.bootstrap();
  }

  private static List<Identifier> companionHandIds() {
    return List.of(
        RottenHandEntity.ENTITY_ID,
        DrownedHandEntity.ENTITY_ID,
        BoneHandEntity.ENTITY_ID,
        HuskHandEntity.ENTITY_ID,
        StrayHandEntity.ENTITY_ID,
        WitheredHandEntity.ENTITY_ID,
        BoggedHandEntity.ENTITY_ID,
        WitchHandEntity.ENTITY_ID);
  }

  private static List<Identifier> lifelessHandIds() {
    return List.of(
        LifelessRottenHandEntity.ENTITY_ID,
        LifelessDrownedHandEntity.ENTITY_ID,
        LifelessBoneHandEntity.ENTITY_ID,
        LifelessHuskHandEntity.ENTITY_ID,
        LifelessStrayHandEntity.ENTITY_ID,
        LifelessWitheredHandEntity.ENTITY_ID,
        LifelessBoggedHandEntity.ENTITY_ID,
        LifelessWitchHandEntity.ENTITY_ID);
  }

  @Test
  @DisplayName("Every companion hand has its own shoulder passive")
  void everyCompanionHandHasOwnPassive() {
    List<Identifier> companionHandIds = companionHandIds();
    List<ShoulderPassive> shoulderPassives =
        companionHandIds.stream()
            .map(ShoulderPassive::forHand)
            .flatMap(Optional::stream)
            .distinct()
            .toList();
    assertEquals(companionHandIds.size(), shoulderPassives.size());
    assertEquals(ShoulderPassive.values().length, shoulderPassives.size());
  }

  @Test
  @DisplayName("Lifeless hands have no shoulder passive")
  void lifelessHandsHaveNoPassive() {
    assertTrue(
        lifelessHandIds().stream().map(ShoulderPassive::forHand).allMatch(Optional::isEmpty));
  }

  @Test
  @DisplayName("Shoulder passives use distinct effect and modifier IDs")
  void effectAndModifierIdsAreDistinct() {
    assertEquals(
        ShoulderPassive.values().length * 2L,
        Arrays.stream(ShoulderPassive.values())
            .flatMap(
                shoulderPassive ->
                    Stream.of(shoulderPassive.getEffectId(), shoulderPassive.getModifierId()))
            .distinct()
            .count());
  }

  @Test
  @DisplayName("Shoulder effects are beneficial and only carry their own modifier")
  void effectsAreBeneficialWithOwnModifier() {
    for (ShoulderPassive shoulderPassive : ShoulderPassive.values()) {
      MobEffect mobEffect = shoulderPassive.createEffect();
      assertTrue(mobEffect.isBeneficial(), shoulderPassive + " is not beneficial");
      mobEffect.createModifiers(
          0,
          (attribute, attributeModifier) ->
              assertEquals(shoulderPassive.getModifierId(), attributeModifier.id()));
    }
  }
}
