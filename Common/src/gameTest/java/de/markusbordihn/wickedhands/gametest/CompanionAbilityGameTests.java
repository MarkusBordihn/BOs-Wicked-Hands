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

import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.entity.AbstractCompanionHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.BoggedHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.HandGesture;
import de.markusbordihn.wickedhands.entity.HuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoggedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessHuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessStrayHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitchHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitheredHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.entity.StrayHandEntity;
import de.markusbordihn.wickedhands.entity.WitchHandEntity;
import de.markusbordihn.wickedhands.entity.WitheredHandEntity;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class CompanionAbilityGameTests extends HandGameTests {

  private static final double APPROACH_SPEED = 0.1D;
  private static final int OWNER_BURN_SECONDS = 20;
  private static final int HEALTHY_OWNER_CHECK_TICKS = 40;

  private static <T extends AbstractCompanionHandEntity>
      void assertRitualCreatesCompanionWithHitAbility(
          GameTestHelper helper,
          Identifier lifelessHandId,
          Item catalyst,
          Class<T> companionClass,
          Predicate<Pig> hitAbilityApplied) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(helper, owner, lifelessHandId, catalyst);

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          T companion = findEntityNearTestPosition(helper, companionClass);
          helper.assertTrue(
              companion.isNPCOwnedBy(owner)
                  && companion.getHandState().lifecycle() == HandLifecycle.COMPANION,
              Component.literal("Ritual did not create a companion"));
          AdvancementHolder companionAdvancement =
              companionAdvancement(helper, EntityType.getKey(companion.getType()));
          helper.assertTrue(
              companionAdvancement != null
                  && owner.getAdvancements().getOrStartProgress(companionAdvancement).isDone(),
              Component.literal("Reviving the companion did not grant its advancement"));

          Pig pig = helper.spawn(EntityType.PIG, TEST_POSITION.east(2));
          helper.assertTrue(
              companion.doHurtTarget(helper.getLevel(), pig) && hitAbilityApplied.test(pig),
              Component.literal("Companion hit did not apply its ability"));
          helper.succeed();
        });
  }

  private static void keepApproaching(GameTestHelper helper, Mob monster, Entity target) {
    // Mob.asValidTarget rejects the always-creative mock player, so approaching is the only threat.
    Vec3 startPosition = helper.absoluteVec(Vec3.atBottomCenterOf(MONSTER_POSITION));
    monster.setPos(startPosition);
    monster.setDeltaMovement(
        target
            .position()
            .subtract(startPosition)
            .multiply(1.0D, 0.0D, 1.0D)
            .normalize()
            .scale(APPROACH_SPEED));
  }

  public void testOnlyCompanionGrabsTarget(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    startRottenHandRitual(helper, helper.makeMockPlayer(GameType.SURVIVAL));

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          RottenHandEntity companion = findEntityNearTestPosition(helper, RottenHandEntity.class);
          Pig pig = helper.spawn(EntityType.PIG, TEST_POSITION.east(2));
          helper.assertTrue(
              companion.doHurtTarget(level, pig) && pig.hasEffect(MobEffects.SLOWNESS),
              Component.literal("Companion did not grab its target"));

          pig.removeEffect(MobEffects.SLOWNESS);
          pig.invulnerableTime = 0;
          helper.assertTrue(
              companion.doHurtTarget(level, pig) && !pig.hasEffect(MobEffects.SLOWNESS),
              Component.literal("Companion grabbed again during the cooldown"));

          AbstractHandEntity wildRottenHand =
              spawnWildHand(helper, RottenHandEntity.ENTITY_ID, TEST_POSITION.west(2));
          pig.invulnerableTime = 0;
          helper.assertTrue(
              wildRottenHand.doHurtTarget(level, pig) && !pig.hasEffect(MobEffects.SLOWNESS),
              Component.literal("Wild rotten hand grabbed its target"));
          helper.succeed();
        });
  }

  public void testHuskHandRitualCreatesWeakeningCompanion(GameTestHelper helper) {
    assertRitualCreatesCompanionWithHitAbility(
        helper,
        LifelessHuskHandEntity.ENTITY_ID,
        Items.SAND,
        HuskHandEntity.class,
        pig -> pig.hasEffect(MobEffects.WEAKNESS));
  }

  public void testStrayHandRitualCreatesChillingCompanion(GameTestHelper helper) {
    assertRitualCreatesCompanionWithHitAbility(
        helper,
        LifelessStrayHandEntity.ENTITY_ID,
        Items.SNOWBALL,
        StrayHandEntity.class,
        pig -> pig.hasEffect(MobEffects.SLOWNESS) && pig.getTicksFrozen() > 0);
  }

  public void testWitheredHandRitualCreatesWitheringCompanion(GameTestHelper helper) {
    assertRitualCreatesCompanionWithHitAbility(
        helper,
        LifelessWitheredHandEntity.ENTITY_ID,
        Items.COAL,
        WitheredHandEntity.class,
        pig -> pig.hasEffect(MobEffects.WITHER));
  }

  public void testBoggedHandRitualCreatesPoisoningCompanion(GameTestHelper helper) {
    assertRitualCreatesCompanionWithHitAbility(
        helper,
        LifelessBoggedHandEntity.ENTITY_ID,
        Items.RED_MUSHROOM,
        BoggedHandEntity.class,
        pig -> pig.hasEffect(MobEffects.POISON));
  }

  public void testWitchHandRitualCreatesHexingCompanion(GameTestHelper helper) {
    assertRitualCreatesCompanionWithHitAbility(
        helper,
        LifelessWitchHandEntity.ENTITY_ID,
        Items.SPIDER_EYE,
        WitchHandEntity.class,
        pig ->
            pig.hasEffect(MobEffects.POISON)
                || pig.hasEffect(MobEffects.SLOWNESS)
                || pig.hasEffect(MobEffects.WEAKNESS));
  }

  public void testWitchHandMendsWoundedOwnerAndCompanions(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(helper, owner, LifelessWitchHandEntity.ENTITY_ID, Items.SPIDER_EYE);

    AtomicReference<WitchHandEntity> companion = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(() -> companion.set(findEntityNearTestPosition(helper, WitchHandEntity.class)))
        .thenExecuteFor(
            HEALTHY_OWNER_CHECK_TICKS,
            () ->
                helper.assertFalse(
                    owner.hasEffect(MobEffects.REGENERATION),
                    Component.literal("Witch hand mended a healthy owner")))
        .thenExecute(
            () -> owner.setHealth(owner.getMaxHealth() * WitchHandEntity.MENDING_HEALTH_RATIO / 2))
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    owner.hasEffect(MobEffects.REGENERATION)
                        && companion.get().hasEffect(MobEffects.REGENERATION),
                    Component.literal("Witch hand did not mend its wounded owner and itself")))
        .thenSucceed();
  }

  public void testDrownedHandExtinguishesBurningOwner(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(helper, owner, LifelessDrownedHandEntity.ENTITY_ID, Items.KELP);

    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              // Player.setRemainingFireTicks caps fire to one tick for the creative mock player.
              owner.getAbilities().invulnerable = false;
              owner.igniteForSeconds(OWNER_BURN_SECONDS);
              helper.assertTrue(
                  owner.getRemainingFireTicks() > 1, Component.literal("Owner does not burn"));
            })
        .thenWaitUntil(
            () ->
                helper.assertFalse(
                    owner.isOnFire(),
                    Component.literal("Drowned hand did not extinguish its burning owner")))
        .thenSucceed();
  }

  public void testBoneHandWarnsAboutMonsterOnce(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(helper, owner, LifelessBoneHandEntity.ENTITY_ID, Items.BONE_MEAL);

    AtomicReference<BoneHandEntity> boneHand = new AtomicReference<>();
    AtomicReference<Mob> zombie = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(() -> boneHand.set(findEntityNearTestPosition(helper, BoneHandEntity.class)))
        .thenWaitUntil(
            () -> assertActiveGesture(helper, boneHand.get(), null, "Bone hand is still awakening"))
        .thenExecute(
            () -> zombie.set(spawnMobWithoutAi(helper, EntityType.ZOMBIE, MONSTER_POSITION)))
        .thenWaitUntil(
            () -> {
              keepApproaching(helper, zombie.get(), owner);
              assertActiveGesture(
                  helper,
                  boneHand.get(),
                  HandGesture.POINT,
                  "Bone hand did not point at the monster approaching its owner");
            })
        .thenWaitUntil(
            () -> {
              keepApproaching(helper, zombie.get(), owner);
              assertActiveGesture(helper, boneHand.get(), null, "Bone hand kept pointing");
            })
        .thenExecuteFor(
            BoneHandEntity.WARNING_COOLDOWN_TICKS
                - HandGesture.POINT.getDurationTicks()
                - COOLDOWN_END_MARGIN_TICKS,
            () -> {
              keepApproaching(helper, zombie.get(), owner);
              helper.assertFalse(
                  boneHand.get().getActiveGesture() == HandGesture.POINT,
                  Component.literal("Bone hand pointed again during its cooldown"));
            })
        .thenSucceed();
  }
}
