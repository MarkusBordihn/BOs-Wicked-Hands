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

import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.wickedhands.data.CompanionMode;
import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.entity.AbstractCompanionHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.HandGesture;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class CompanionBehaviourGameTests extends HandGameTests {

  private static final int STRANGER_WAVE_CHECK_TICKS = 100;
  private static final int SIBLING_TARGET_CHECK_TICKS = 40;
  private static final int STAYING_COMPANION_CHECK_TICKS = 60;
  private static final float WOUNDED_COMPANION_HEALTH = 1.0F;
  private static final float COMBAT_DAMAGE = 2.0F;
  // Forge and NeoForge run tests millions of blocks out, beyond the Nether world border.
  private static final Vec3 NETHER_OWNER_POSITION = new Vec3(0.5D, 64.0D, 0.5D);

  public void testCompanionWavesAtStrangerOnce(GameTestHelper helper) {
    startRottenHandRitual(helper, helper.makeMockPlayer(GameType.SURVIVAL));

    AtomicReference<RottenHandEntity> companion = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> companion.set(findEntityNearTestPosition(helper, RottenHandEntity.class)))
        .thenWaitUntil(
            () ->
                assertActiveGesture(helper, companion.get(), null, "Companion is still awakening"))
        .thenExecute(() -> placeBesideRitual(helper, helper.makeMockServerPlayerInLevel()))
        .thenWaitUntil(
            () ->
                assertActiveGesture(
                    helper,
                    companion.get(),
                    HandGesture.WAVE,
                    "Companion did not wave at the stranger"))
        .thenWaitUntil(
            () -> assertActiveGesture(helper, companion.get(), null, "Companion kept waving"))
        .thenExecuteFor(
            STRANGER_WAVE_CHECK_TICKS,
            () ->
                helper.assertFalse(
                    companion.get().getActiveGesture() == HandGesture.WAVE,
                    Component.literal("Companion waved again during its cooldown")))
        .thenSucceed();
  }

  public void testCompanionKeepsBondAfterReload(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
    startRottenHandRitual(helper, owner);

    Component companionName = Component.literal("Grabby");
    AtomicReference<RottenHandEntity> reloadedCompanion = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              RottenHandEntity companion =
                  findEntityNearTestPosition(helper, RottenHandEntity.class);
              companion.setCustomName(companionName);
              owner.setShiftKeyDown(true);
              companion.mobInteract(owner, InteractionHand.MAIN_HAND);
              companion.mobInteract(owner, InteractionHand.MAIN_HAND);
              owner.setShiftKeyDown(false);

              TagValueOutput savedCompanion =
                  TagValueOutput.createWithContext(
                      ProblemReporter.DISCARDING, level.registryAccess());
              companion.saveWithoutId(savedCompanion);
              companion.discard();

              RottenHandEntity companionCopy =
                  (RottenHandEntity) rottenHandType().create(level, EntitySpawnReason.LOAD);
              companionCopy.load(
                  TagValueInput.create(
                      ProblemReporter.DISCARDING,
                      level.registryAccess(),
                      savedCompanion.buildResult()));
              level.addFreshEntity(companionCopy);
              reloadedCompanion.set(companionCopy);
            })
        .thenIdle(1)
        .thenExecute(
            () -> {
              RottenHandEntity companion = reloadedCompanion.get();
              helper.assertTrue(
                  companion.getHandState().lifecycle() == HandLifecycle.COMPANION
                      && companion.isNPCOwnedBy(owner)
                      && companionName.equals(companion.getCustomName()),
                  Component.literal("Reloaded hand is no longer the named companion of its owner"));
              helper.assertTrue(
                  companion.getHandState().companionMode() == CompanionMode.STAY
                      && companion.hasObjective(ObjectiveType.MOVE_BACK_TO_HOME)
                      && companion.hasObjective(ObjectiveType.OWNER_HURT_BY_TARGET)
                      && companion.hasObjective(ObjectiveType.OWNER_HURT_TARGET),
                  Component.literal("Reloaded companion lost its mode or its objectives"));
              helper.assertFalse(
                  companion.hasObjective(ObjectiveType.ATTACK_PLAYER)
                      || companion.hasObjective(ObjectiveType.ATTACK_ANIMAL),
                  Component.literal("Reloaded companion hunts players or animals"));
              helper.assertTrue(
                  companion.requiresCustomPersistence(),
                  Component.literal("Reloaded companion can despawn"));
            })
        .thenSucceed();
  }

  public void testCompanionDefendsAndJoinsOwner(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRottenHandRitual(helper, owner);

    AtomicReference<RottenHandEntity> companion = new AtomicReference<>();
    AtomicReference<Mob> attacker = new AtomicReference<>();
    AtomicReference<Pig> victim = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              companion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              attacker.set(spawnMobWithoutAi(helper, EntityType.ZOMBIE, MONSTER_POSITION));
              owner.setLastHurtByMob(attacker.get());
            })
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    companion.get().getTarget() == attacker.get(),
                    Component.literal("Companion did not defend its owner")))
        .thenExecute(
            () -> {
              attacker.get().discard();
              victim.set(spawnMobWithoutAi(helper, EntityType.PIG, MONSTER_POSITION));
              owner.setLastHurtMob(victim.get());
            })
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    companion.get().getTarget() == victim.get(),
                    Component.literal("Companion did not join its owner's attack")))
        .thenSucceed();
  }

  public void testCompanionIgnoresOwnerHittingSiblingCompanion(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRottenHandRitual(helper, owner);

    AtomicReference<RottenHandEntity> hitCompanion = new AtomicReference<>();
    AtomicReference<RottenHandEntity> siblingCompanion = new AtomicReference<>();
    AtomicReference<Pig> victim = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              hitCompanion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              helper.setBlock(MONSTER_POSITION.below(), Blocks.STONE);
              hitCompanion.get().setNoAi(true);
              hitCompanion
                  .get()
                  .setPos(helper.absoluteVec(Vec3.atBottomCenterOf(MONSTER_POSITION)));
              startRottenHandRitual(helper, owner);
            })
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              siblingCompanion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              owner.setLastHurtMob(hitCompanion.get());
            })
        .thenIdle(SIBLING_TARGET_CHECK_TICKS)
        .thenExecute(
            () -> {
              helper.assertTrue(
                  siblingCompanion.get().getTarget() != hitCompanion.get(),
                  Component.literal("Companion attacked a companion of the same owner"));
              victim.set(helper.spawn(EntityType.PIG, BESIDE_RITUAL_POSITION.east()));
              victim.get().setNoAi(true);
              owner.setLastHurtMob(victim.get());
            })
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    siblingCompanion.get().getTarget() == victim.get(),
                    Component.literal("Companion did not join its owner's attack")))
        .thenSucceed();
  }

  public void testCompanionIgnoresCreepersAndForeignPets(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRottenHandRitual(helper, owner);

    AtomicReference<RottenHandEntity> companion = new AtomicReference<>();
    AtomicReference<Mob> creeper = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              companion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              creeper.set(spawnMobWithoutAi(helper, EntityType.CREEPER, MONSTER_POSITION));
              owner.setLastHurtMob(creeper.get());
            })
        .thenExecuteFor(
            SIBLING_TARGET_CHECK_TICKS,
            () ->
                helper.assertFalse(
                    companion.get().getTarget() == creeper.get(),
                    Component.literal("Companion joined its owner's attack on a creeper")))
        .thenExecute(
            () -> {
              TamableAnimal foreignWolf = helper.spawn(EntityType.WOLF, MONSTER_POSITION.east());
              foreignWolf.tame(helper.makeMockPlayer(GameType.SURVIVAL));
              helper.assertFalse(
                  companion.get().canAttack(foreignWolf),
                  Component.literal("Companion may attack another player's pet"));
              AbstractHandEntity wildHand =
                  spawnWildHand(helper, RottenHandEntity.ENTITY_ID, MONSTER_POSITION.west());
              helper.assertTrue(
                  wildHand.canAttack(creeper.get()) && wildHand.canAttack(foreignWolf),
                  Component.literal("Wild hand lost its targets to the companion exclusions"));
            })
        .thenSucceed();
  }

  public void testCompanionHealsFromOffering(GameTestHelper helper) {
    Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
    Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
    startRottenHandRitual(helper, owner);

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          RottenHandEntity companion = findEntityNearTestPosition(helper, RottenHandEntity.class);
          companion.setHealth(WOUNDED_COMPANION_HEALTH);
          stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH));
          companion.mobInteract(stranger, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              companion.getHealth() == WOUNDED_COMPANION_HEALTH
                  && stranger.getMainHandItem().getCount() == 1,
              Component.literal("A stranger fed the companion"));

          owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH, 2));
          companion.mobInteract(owner, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              companion.getHealth()
                      == WOUNDED_COMPANION_HEALTH + AbstractCompanionHandEntity.FEEDING_HEAL_AMOUNT
                  && owner.getMainHandItem().getCount() == 1,
              Component.literal("Feeding the offering did not heal the companion"));

          companion.setHealth(companion.getMaxHealth());
          companion.mobInteract(owner, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              owner.getMainHandItem().getCount() == 1,
              Component.literal("Feeding a healthy companion took the offering"));
          helper.succeed();
        });
  }

  public void testCompanionRegeneratesOnlyOutOfCombat(GameTestHelper helper) {
    startRottenHandRitual(helper, helper.makeMockPlayer(GameType.SURVIVAL));

    AtomicReference<RottenHandEntity> companion = new AtomicReference<>();
    AtomicReference<Float> woundedHealth = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              companion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              companion
                  .get()
                  .hurtServer(
                      helper.getLevel(),
                      helper.getLevel().damageSources().generic(),
                      COMBAT_DAMAGE);
              woundedHealth.set(companion.get().getHealth());
              helper.assertTrue(
                  woundedHealth.get() < companion.get().getMaxHealth(),
                  Component.literal("Companion was not hurt"));
            })
        .thenExecuteFor(
            AbstractCompanionHandEntity.REGENERATION_COMBAT_COOLDOWN_TICKS
                - COOLDOWN_END_MARGIN_TICKS,
            () ->
                helper.assertTrue(
                    companion.get().getHealth() == woundedHealth.get(),
                    Component.literal("Companion regenerated right after being hurt")))
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    companion.get().getHealth() > woundedHealth.get(),
                    Component.literal("Companion did not regenerate out of combat")))
        .thenSucceed();
  }

  public void testFollowingCompanionJoinsOwnerInOtherDimension(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    startRottenHandRitual(helper, owner);

    ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
    AtomicReference<RottenHandEntity> companion = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              companion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
              owner.setShiftKeyDown(true);
              companion.get().mobInteract(owner, InteractionHand.MAIN_HAND);
              companion.get().mobInteract(owner, InteractionHand.MAIN_HAND);
              owner.teleport(
                  new TeleportTransition(
                      nether,
                      NETHER_OWNER_POSITION,
                      Vec3.ZERO,
                      0.0F,
                      0.0F,
                      TeleportTransition.DO_NOTHING));
            })
        .thenIdle(STAYING_COMPANION_CHECK_TICKS)
        .thenExecute(
            () -> {
              helper.assertTrue(
                  owner.level() == nether, Component.literal("Owner did not enter the Nether"));
              helper.assertTrue(
                  companion.get().getHandState().companionMode() == CompanionMode.STAY
                      && companion.get().isAlive()
                      && companion.get().level() == helper.getLevel(),
                  Component.literal("Staying companion followed its owner into the Nether"));
              companion.get().mobInteract(owner, InteractionHand.MAIN_HAND);
              owner.setShiftKeyDown(false);
            })
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    companion.get().getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION,
                    Component.literal("Following companion did not join its owner in the Nether")))
        .thenSucceed();
  }
}
