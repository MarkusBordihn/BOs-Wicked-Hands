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

import de.markusbordihn.easynpc.api.handler.EasyNPCEntityHandler;
import de.markusbordihn.easynpc.data.npc.NPCRemovalReason;
import de.markusbordihn.easynpc.data.npc.SavedNPCEntityEntry;
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.advancement.HandAdvancements;
import de.markusbordihn.wickedhands.data.CompanionMode;
import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.entity.AbstractCompanionHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractLifelessHandEntity;
import de.markusbordihn.wickedhands.entity.BoggedHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.HandGesture;
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
import de.markusbordihn.wickedhands.item.HandDataComponents;
import de.markusbordihn.wickedhands.item.LifelessHandItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.CandleBlock;

public class RevivalRitualGameTests extends HandGameTests {

  private static final Identifier LIFELESS_ROTTEN_HAND_PROFILE_ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/lifeless_rotten_hand");
  private static final int DEATH_ANIMATION_TICKS = 20;

  private static LifelessRottenHandEntity placeLifelessRottenHand(
      GameTestHelper helper, Player player) {
    return placeLifelessHand(
        helper, player, new ItemStack(lifelessRottenHandItem()), LifelessRottenHandEntity.class);
  }

  private static List<ItemEntity> findItemEntities(GameTestHelper helper, Item item) {
    return helper
        .getLevel()
        .getEntitiesOfClass(
            ItemEntity.class,
            aroundTestPosition(helper, 3.0D),
            itemEntity -> itemEntity.getItem().is(item));
  }

  private static ItemStack findInInventory(Player player, Item item) {
    Inventory inventory = player.getInventory();
    for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
      if (inventory.getItem(slot).is(item)) {
        return inventory.getItem(slot);
      }
    }
    return ItemStack.EMPTY;
  }

  private static Optional<SavedNPCEntityEntry> findSavedNPCEntry(UUID entityUUID) {
    return EasyNPCEntityHandler.getAll().stream()
        .filter(savedEntry -> savedEntry.entityUUID().equals(entityUUID))
        .findFirst();
  }

  private static void assertFallenCompanionReturnsAndRevives(
      GameTestHelper helper,
      Item lifelessHandItem,
      Item catalyst,
      Class<? extends AbstractCompanionHandEntity> companionClass) {
    ServerLevel level = helper.getLevel();
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    startRevivalRitual(helper, owner, new ItemStack(lifelessHandItem), catalyst);

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          AbstractCompanionHandEntity companion =
              findEntityNearTestPosition(helper, companionClass);
          UUID companionUUID = companion.getUUID();
          Component companionName = Component.literal("Grabby");
          companion.setCustomName(companionName);
          companion.hurtServer(level, level.damageSources().magic(), 100.0F);

          ItemStack returnedRemains = findInInventory(owner, lifelessHandItem);
          helper.assertTrue(
              companionUUID.equals(returnedRemains.get(HandDataComponents.COMPANION_UUID))
                  && owner.getUUID().equals(returnedRemains.get(HandDataComponents.OWNER_UUID))
                  && companionName.equals(returnedRemains.getCustomName()),
              Component.literal("Remains did not return to the owner with name and identity"));
          helper.assertTrue(
              findItemEntities(helper, lifelessHandItem).isEmpty(),
              Component.literal("Fallen companion also dropped its loot table"));

          helper.runAfterDelay(
              DEATH_ANIMATION_TICKS + 5,
              () -> {
                helper.assertTrue(
                    findSavedNPCEntry(companionUUID)
                        .filter(
                            savedEntry ->
                                savedEntry.metadata().removalReason() == NPCRemovalReason.KILLED)
                        .isPresent(),
                    Component.literal("Easy NPC did not keep the fallen companion as killed"));
                ItemStack remainsToPlace = returnedRemains.copy();
                returnedRemains.setCount(0);
                UUID lifelessHandUUID =
                    startRevivalRitual(helper, owner, remainsToPlace, catalyst).getUUID();
                helper.runAfterDelay(
                    AFTER_RITUAL_TICKS,
                    () ->
                        assertSameCompanionRevived(
                            helper,
                            owner,
                            companionClass,
                            companionUUID,
                            companionName,
                            lifelessHandUUID));
              });
        });
  }

  private static void assertSameCompanionRevived(
      GameTestHelper helper,
      Player owner,
      Class<? extends AbstractCompanionHandEntity> companionClass,
      UUID companionUUID,
      Component companionName,
      UUID lifelessHandUUID) {
    List<? extends AbstractCompanionHandEntity> revivedHandsOfOwner =
        helper
            .getLevel()
            .getEntitiesOfClass(
                companionClass,
                aroundTestPosition(helper, 3.0D),
                companion -> companion.isNPCOwnedBy(owner));
    helper.assertTrue(
        revivedHandsOfOwner.size() == 1,
        Component.literal(
            "Expected one revived hand of the owner, found " + revivedHandsOfOwner.size()));
    AbstractCompanionHandEntity revivedCompanion = revivedHandsOfOwner.getFirst();
    helper.assertTrue(
        companionUUID.equals(revivedCompanion.getUUID())
            && companionName.equals(revivedCompanion.getCustomName()),
        Component.literal("Revival did not bring back the same companion"));
    helper.assertTrue(
        revivedCompanion.getHandState().lifecycle() == HandLifecycle.COMPANION
            && revivedCompanion.getHealth() == revivedCompanion.getMaxHealth(),
        Component.literal("Revived companion is not a healthy companion"));
    helper.assertTrue(
        findSavedNPCEntry(lifelessHandUUID).isEmpty(),
        Component.literal("Easy NPC still lists the consumed lifeless hand"));
    helper.succeed();
  }

  public void testLifelessRottenHandIsPlacedOnSoulSand(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LifelessRottenHandEntity lifelessRottenHand = placeLifelessRottenHand(helper, player);
    helper.assertTrue(
        lifelessRottenHand.isNoAi() && lifelessRottenHand.getRevivalState().ownerUUID().isEmpty(),
        Component.literal("Placed rotten hand is not lifeless"));
    helper.assertTrue(
        player.getMainHandItem().isEmpty(),
        Component.literal("Placing did not consume the lifeless rotten hand"));
    helper.assertTrue(
        lifelessRottenHand.requiresCustomPersistence()
            && !lifelessRottenHand.removeWhenFarAway(DESPAWN_DISTANCE),
        Component.literal("Placed rotten hand would despawn"));
    helper.assertTrue(
        lifelessRottenHand.getModelRootData().rotation().x() == (float) Math.PI,
        Component.literal("Placed rotten hand does not lie on its back"));
    assertActiveModelProfile(helper, lifelessRottenHand, LIFELESS_ROTTEN_HAND_PROFILE_ID);
    helper.succeed();
  }

  public void testRevivalRitualNeedsCandles(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LifelessRottenHandEntity lifelessRottenHand = placeLifelessRottenHand(helper, player);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH));
    lifelessRottenHand.mobInteract(player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        lifelessRottenHand.getRevivalState().ownerUUID().isEmpty(),
        Component.literal("Rotten hand was revived without candles"));
    helper.assertTrue(
        player.getMainHandItem().is(Items.ROTTEN_FLESH),
        Component.literal("Catalyst was consumed without candles"));
    helper.succeed();
  }

  public void testRevivalRitualCreatesCompanion(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    AbstractLifelessHandEntity lifelessRottenHand = startRottenHandRitual(helper, player);
    helper.assertTrue(
        lifelessRottenHand.getRevivalState().ownerUUID().equals(Optional.of(player.getUUID())),
        Component.literal("Revival ritual did not start for the ritual player"));
    helper.assertTrue(
        player.getMainHandItem().isEmpty(), Component.literal("Catalyst was not consumed"));

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          helper.assertTrue(
              lifelessRottenHand.isRemoved(),
              Component.literal("Lifeless rotten hand was not replaced"));
          RottenHandEntity rottenHand = findEntityNearTestPosition(helper, RottenHandEntity.class);
          helper.assertTrue(
              rottenHand.isNPCOwnedBy(player), Component.literal("Ritual player is not the owner"));
          helper.assertTrue(
              rottenHand.getHandState().lifecycle() == HandLifecycle.COMPANION
                  && !rottenHand.isNoAi(),
              Component.literal("Rotten hand did not become a companion"));
          helper.assertTrue(
              rottenHand.hasObjective(ObjectiveType.FLOAT),
              Component.literal("Companion rotten hand cannot swim"));
          for (BlockPos candlePosition : CANDLE_POSITIONS) {
            helper.assertFalse(
                helper.getBlockState(candlePosition).getValue(CandleBlock.LIT),
                Component.literal("Ritual candle at " + candlePosition + " is still burning"));
          }

          player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
          player.setShiftKeyDown(true);
          rottenHand.mobInteract(player, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              rottenHand.getHandState().companionMode() == CompanionMode.FOLLOW,
              Component.literal("Sneak interaction with an item switched the companion mode"));

          player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
          rottenHand.mobInteract(player, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              rottenHand.getHandState().companionMode() == CompanionMode.SHOULDER
                  && rottenHand.hasObjective(ObjectiveType.SIT_ON_OWNER_SHOULDER),
              Component.literal("Sneak interaction did not send the companion to the shoulder"));

          rottenHand.mobInteract(player, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              rottenHand.getHandState().companionMode() == CompanionMode.STAY
                  && rottenHand.hasObjective(ObjectiveType.MOVE_BACK_TO_HOME)
                  && !rottenHand.hasObjective(ObjectiveType.SIT_ON_OWNER_SHOULDER),
              Component.literal("Sneak interaction did not make the companion stay"));
          helper.succeed();
        });
  }

  public void testRevivalRitualGrantsAdvancements(GameTestHelper helper) {
    ServerAdvancementManager advancementManager = helper.getLevel().getServer().getAdvancements();
    helper.assertTrue(
        advancementManager.get(HandAdvancements.ROOT_ID) != null,
        Component.literal("Root advancement is not loaded"));
    AdvancementHolder ritualAdvancement = advancementManager.get(HandAdvancements.START_RITUAL_ID);
    AdvancementHolder companionAdvancement =
        advancementManager.get(HandAdvancements.companionAdvancementId(RottenHandEntity.ENTITY_ID));
    helper.assertTrue(
        ritualAdvancement != null && companionAdvancement != null,
        Component.literal("Ritual advancements are not loaded"));

    ServerPlayer serverPlayer = helper.makeMockServerPlayerInLevel();
    startRottenHandRitual(helper, serverPlayer);
    helper.assertTrue(
        serverPlayer.getAdvancements().getOrStartProgress(ritualAdvancement).isDone(),
        Component.literal("Starting the ritual did not grant the ritual advancement"));

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          helper.assertTrue(
              serverPlayer.getAdvancements().getOrStartProgress(companionAdvancement).isDone(),
              Component.literal("Reviving the hand did not grant the companion advancement"));
          helper.succeed();
        });
  }

  public void testDrownedHandRitualNeedsKelp(GameTestHelper helper) {
    placeLitCandles(helper);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LifelessDrownedHandEntity lifelessDrownedHand =
        placeLifelessHand(
            helper,
            player,
            new ItemStack(item(LifelessDrownedHandEntity.ENTITY_ID)),
            LifelessDrownedHandEntity.class);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH));
    lifelessDrownedHand.mobInteract(player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        lifelessDrownedHand.getRevivalState().ownerUUID().isEmpty()
            && player.getMainHandItem().is(Items.ROTTEN_FLESH),
        Component.literal("Rotten flesh started the drowned hand ritual"));

    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.KELP));
    lifelessDrownedHand.mobInteract(player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        lifelessDrownedHand.getRevivalState().ownerUUID().equals(Optional.of(player.getUUID())),
        Component.literal("Kelp did not start the drowned hand ritual"));

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          DrownedHandEntity drownedHand =
              findEntityNearTestPosition(helper, DrownedHandEntity.class);
          helper.assertTrue(
              drownedHand.isNPCOwnedBy(player)
                  && drownedHand.getHandState().lifecycle() == HandLifecycle.COMPANION,
              Component.literal("Drowned hand did not become a companion"));
          helper.assertFalse(
              drownedHand.hasObjective(ObjectiveType.FLOAT),
              Component.literal("Companion drowned hand floats instead of diving"));
          helper.succeed();
        });
  }

  public void testBoneHandRitualCreatesCompanion(GameTestHelper helper) {
    AdvancementHolder companionAdvancement =
        companionAdvancement(helper, BoneHandEntity.ENTITY_ID);
    helper.assertTrue(
        companionAdvancement != null,
        Component.literal("Bone hand companion advancement is not loaded"));

    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(helper, owner, LifelessBoneHandEntity.ENTITY_ID, Items.BONE_MEAL);

    AtomicReference<BoneHandEntity> boneHand = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              boneHand.set(findEntityNearTestPosition(helper, BoneHandEntity.class));
              helper.assertTrue(
                  boneHand.get().isNPCOwnedBy(owner)
                      && boneHand.get().getHandState().lifecycle() == HandLifecycle.COMPANION,
                  Component.literal("Bone hand did not become a companion"));
              helper.assertTrue(
                  boneHand.get().hasObjective(ObjectiveType.FLOAT),
                  Component.literal("Companion bone hand cannot swim"));
              helper.assertTrue(
                  owner.getAdvancements().getOrStartProgress(companionAdvancement).isDone(),
                  Component.literal("Reviving the bone hand did not grant its advancement"));
              assertActiveGesture(
                  helper, boneHand.get(), HandGesture.REVIVE, "Awakened hand did not play revive");
            })
        .thenWaitUntil(
            () ->
                assertActiveGesture(
                    helper,
                    boneHand.get(),
                    HandGesture.WAVE,
                    "Awakened hand did not wave at its owner"))
        .thenSucceed();
  }

  public void testStrangerCannotReviveForeignRemains(GameTestHelper helper) {
    Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
    Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack foreignRemains =
        LifelessHandItem.createCompanionRemains(
            LifelessRottenHandEntity.ENTITY_ID,
            UUID.randomUUID(),
            Optional.of(owner.getUUID()),
            null);
    AbstractLifelessHandEntity lifelessHand =
        startRevivalRitual(helper, stranger, foreignRemains, Items.ROTTEN_FLESH);
    helper.assertTrue(
        lifelessHand.getRevivalState().ownerUUID().isEmpty()
            && stranger.getMainHandItem().is(Items.ROTTEN_FLESH),
        Component.literal("A stranger started the ritual for foreign remains"));

    owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH));
    lifelessHand.mobInteract(owner, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        lifelessHand.getRevivalState().ownerUUID().equals(Optional.of(owner.getUUID())),
        Component.literal("The owner could not start the ritual for their remains"));
    helper.succeed();
  }

  public void testFallenCompanionWithoutOwnerLeavesProtectedRemains(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
    startRottenHandRitual(helper, owner);

    helper.runAfterDelay(
        AFTER_RITUAL_TICKS,
        () -> {
          RottenHandEntity companion = findEntityNearTestPosition(helper, RottenHandEntity.class);
          companion.hurtServer(level, level.damageSources().lava(), 100.0F);
          List<ItemEntity> remains = findItemEntities(helper, lifelessRottenHandItem());
          helper.assertTrue(
              remains.size() == 1,
              Component.literal("Expected only the companion remains, found " + remains.size()));
          ItemEntity companionRemains = remains.getFirst();
          helper.assertTrue(
              companion
                  .getUUID()
                  .equals(companionRemains.getItem().get(HandDataComponents.COMPANION_UUID)),
              Component.literal("Remains are not linked to the fallen companion"));
          helper.assertTrue(
              companionRemains.isInvulnerable() && companionRemains.getAge() < 0,
              Component.literal("Remains of an absent owner could burn or despawn"));

          companionRemains.setNoPickUpDelay();
          Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
          companionRemains.playerTouch(stranger);
          helper.assertTrue(
              !companionRemains.isRemoved()
                  && findInInventory(stranger, lifelessRottenHandItem()).isEmpty(),
              Component.literal("A stranger picked up the protected remains"));
          companionRemains.playerTouch(owner);
          helper.assertTrue(
              companionRemains.isRemoved()
                  && !findInInventory(owner, lifelessRottenHandItem()).isEmpty(),
              Component.literal("The owner could not pick up the protected remains"));
          helper.succeed();
        });
  }

  public void testFallenCompanionReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, lifelessRottenHandItem(), Items.ROTTEN_FLESH, RottenHandEntity.class);
  }

  public void testFallenDrownedHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, item(LifelessDrownedHandEntity.ENTITY_ID), Items.KELP, DrownedHandEntity.class);
  }

  public void testFallenBoneHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, item(LifelessBoneHandEntity.ENTITY_ID), Items.BONE_MEAL, BoneHandEntity.class);
  }

  public void testFallenHuskHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, item(LifelessHuskHandEntity.ENTITY_ID), Items.SAND, HuskHandEntity.class);
  }

  public void testFallenStrayHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, item(LifelessStrayHandEntity.ENTITY_ID), Items.SNOWBALL, StrayHandEntity.class);
  }

  public void testFallenWitheredHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, item(LifelessWitheredHandEntity.ENTITY_ID), Items.COAL, WitheredHandEntity.class);
  }

  public void testFallenBoggedHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper,
        item(LifelessBoggedHandEntity.ENTITY_ID),
        Items.RED_MUSHROOM,
        BoggedHandEntity.class);
  }

  public void testFallenWitchHandReturnsAndRevivesWithSameIdentity(GameTestHelper helper) {
    assertFallenCompanionReturnsAndRevives(
        helper, item(LifelessWitchHandEntity.ENTITY_ID), Items.SPIDER_EYE, WitchHandEntity.class);
  }
}
