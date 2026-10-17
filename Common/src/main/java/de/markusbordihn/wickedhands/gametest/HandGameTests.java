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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileManager;
import de.markusbordihn.easynpc.api.handler.EasyNPCEntityHandler;
import de.markusbordihn.easynpc.data.npc.NPCRemovalReason;
import de.markusbordihn.easynpc.data.npc.SavedNPCEntityEntry;
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.advancement.HandAdvancements;
import de.markusbordihn.wickedhands.data.CompanionMode;
import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.entity.AbstractCompanionHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractLifelessHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.HandGesture;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.item.HandDataComponents;
import de.markusbordihn.wickedhands.item.LifelessHandItem;
import de.markusbordihn.wickedhands.loot.HandLootInjections;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HandGameTests {

  public static final int BEHAVIOUR_MAX_TICKS = 200;
  public static final int RITUAL_MAX_TICKS = 200;
  public static final int TWO_RITUALS_MAX_TICKS = 400;
  public static final int UNDERWATER_MAX_TICKS = 400;
  public static final int AFTER_AWAKENING_MAX_TICKS = 500;
  private static final BlockPos TEST_POSITION = new BlockPos(0, 1, 0);
  private static final Identifier ROTTEN_HAND_PROFILE_ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/rotten_hand");
  private static final Identifier LIFELESS_ROTTEN_HAND_PROFILE_ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/lifeless_rotten_hand");
  private static final long NOON_DAY_TIME = 6000L;
  private static final long MIDNIGHT_DAY_TIME = 18000L;
  private static final int SPAWN_RULE_ATTEMPTS = 200;
  private static final double DESPAWN_DISTANCE = 128.0D;
  private static final int ZOMBIE_LOOT_ROLLS = 2000;
  private static final int MINIMUM_EXPECTED_HAND_DROPS = 20;
  private static final int MAXIMUM_EXPECTED_HAND_DROPS = 100;
  private static final int WATER_DEPTH = 3;
  private static final int DEATH_ANIMATION_TICKS = 20;
  private static final int PAST_AIR_SUPPLY_TICKS = 340;
  private static final double APPROACH_SPEED = 0.1D;
  private static final int OWNER_BURN_SECONDS = 20;
  private static final int STRANGER_WAVE_CHECK_TICKS = 100;
  private static final int SIBLING_TARGET_CHECK_TICKS = 40;
  private static final int STAYING_COMPANION_CHECK_TICKS = 60;
  // Forge and NeoForge run tests millions of blocks out, beyond the Nether world border.
  private static final Vec3 NETHER_OWNER_POSITION = new Vec3(0.5D, 64.0D, 0.5D);
  private static final int COOLDOWN_END_MARGIN_TICKS = 20;
  private static final int AFTER_RITUAL_TICKS = RevivalRitual.DURATION_TICKS + 5;
  private static final BlockPos BESIDE_RITUAL_POSITION = TEST_POSITION.south(3);
  private static final BlockPos MONSTER_POSITION = TEST_POSITION.north(4);
  private static final List<BlockPos> CANDLE_POSITIONS =
      List.of(
          TEST_POSITION.east(), TEST_POSITION.west(), TEST_POSITION.north(), TEST_POSITION.south());

  private static void placeLitCandles(GameTestHelper helper) {
    for (BlockPos candlePosition : CANDLE_POSITIONS) {
      helper.setBlock(candlePosition.below(), Blocks.STONE);
      helper.setBlock(
          candlePosition, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true));
    }
  }

  private static void assertActiveModelProfile(
      GameTestHelper helper, AbstractHandEntity hand, Identifier expectedProfileId) {
    Identifier profileId = hand.getEasyModelProfileId();
    helper.assertTrue(
        expectedProfileId.equals(profileId),
        Component.literal(hand.getType() + " uses the model profile " + profileId));
    Optional<EasyModelEntityProfile> profile =
        EasyModelProfileManager.load(helper.getLevel().getServer().getResourceManager())
            .getProfile(profileId);
    helper.assertTrue(
        profile.isPresent(), Component.literal("Model profile " + profileId + " is missing"));
    helper.assertTrue(
        profile.get().isActive(),
        Component.literal(
            "Easy Model Entities rejected the model profile "
                + profileId
                + ": "
                + profile.get().validationIssues()));
  }

  private static LifelessRottenHandEntity placeLifelessRottenHand(
      GameTestHelper helper, Player player) {
    return placeLifelessHand(
        helper, player, new ItemStack(lifelessRottenHandItem()), LifelessRottenHandEntity.class);
  }

  private static <T extends AbstractLifelessHandEntity> T placeLifelessHand(
      GameTestHelper helper,
      Player player,
      ItemStack lifelessHandStack,
      Class<T> lifelessHandClass) {
    helper.setBlock(TEST_POSITION.below(), Blocks.SOUL_SAND);
    helper.setBlock(TEST_POSITION, Blocks.AIR);
    player.setItemInHand(InteractionHand.MAIN_HAND, lifelessHandStack);
    BlockPos ritualBasePosition = helper.absolutePos(TEST_POSITION.below());
    helper.assertTrue(
        lifelessHandStack
            .getItem()
            .useOn(
                new UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(
                        Vec3.atCenterOf(ritualBasePosition),
                        Direction.UP,
                        ritualBasePosition,
                        false)))
            .consumesAction(),
        Component.literal(lifelessHandStack.getItem() + " could not be placed on soul sand"));

    return findEntityNearTestPosition(helper, lifelessHandClass);
  }

  private static AbstractLifelessHandEntity startRevivalRitual(
      GameTestHelper helper, Player player, ItemStack lifelessHandStack, Item catalyst) {
    placeLitCandles(helper);
    AbstractLifelessHandEntity lifelessHand =
        placeLifelessHand(helper, player, lifelessHandStack, AbstractLifelessHandEntity.class);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(catalyst));
    lifelessHand.mobInteract(player, InteractionHand.MAIN_HAND);
    return lifelessHand;
  }

  private static void placeBesideRitual(GameTestHelper helper, Player player) {
    helper.setBlock(BESIDE_RITUAL_POSITION.north().below(), Blocks.STONE);
    helper.setBlock(BESIDE_RITUAL_POSITION.below(), Blocks.STONE);
    player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(BESIDE_RITUAL_POSITION)));
  }

  private static void fillWaterColumn(GameTestHelper helper) {
    helper.setBlock(TEST_POSITION.below(), Blocks.GLASS);
    for (int depth = 0; depth < WATER_DEPTH; depth++) {
      BlockPos waterPosition = TEST_POSITION.above(depth);
      helper.setBlock(waterPosition, Blocks.WATER);
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        helper.setBlock(waterPosition.relative(direction), Blocks.GLASS);
      }
    }
  }

  private static void assertActiveGesture(
      GameTestHelper helper,
      AbstractCompanionHandEntity hand,
      HandGesture expectedGesture,
      String failureMessage) {
    helper.assertTrue(
        hand.getActiveGesture() == expectedGesture, Component.literal(failureMessage));
  }

  private static AABB aroundTestPosition(GameTestHelper helper, double radius) {
    return new AABB(helper.absolutePos(TEST_POSITION)).inflate(radius);
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

  private static <T extends Entity> T findEntityNearTestPosition(
      GameTestHelper helper, Class<T> entityClass) {
    List<T> entities =
        helper.getLevel().getEntitiesOfClass(entityClass, aroundTestPosition(helper, 1.0D));
    helper.assertFalse(
        entities.isEmpty(), Component.literal("No " + entityClass.getSimpleName() + " was found"));
    return entities.getFirst();
  }

  private static boolean passesSpawnRules(GameTestHelper helper, EntityType<?> entityType) {
    ServerLevel level = helper.getLevel();
    return SpawnPlacements.checkSpawnRules(
        entityType,
        level,
        EntitySpawnReason.NATURAL,
        helper.absolutePos(TEST_POSITION),
        level.getRandom());
  }

  private static EntityType<?> rottenHandType() {
    return entityType(RottenHandEntity.ENTITY_ID);
  }

  private static EntityType<?> entityType(Identifier entityId) {
    return BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
  }

  private static Item lifelessRottenHandItem() {
    return item(LifelessRottenHandEntity.ENTITY_ID);
  }

  private static Item item(Identifier itemId) {
    return BuiltInRegistries.ITEM.getValue(itemId);
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
    List<? extends AbstractCompanionHandEntity> revivedHands =
        helper.getLevel().getEntitiesOfClass(companionClass, aroundTestPosition(helper, 3.0D));
    helper.assertTrue(
        revivedHands.size() == 1,
        Component.literal("Expected one revived hand, found " + revivedHands.size()));
    AbstractCompanionHandEntity revivedCompanion = revivedHands.getFirst();
    helper.assertTrue(
        companionUUID.equals(revivedCompanion.getUUID())
            && companionName.equals(revivedCompanion.getCustomName()),
        Component.literal("Revival did not bring back the same companion"));
    helper.assertTrue(
        revivedCompanion.getHandState().lifecycle() == HandLifecycle.COMPANION
            && revivedCompanion.isNPCOwnedBy(owner)
            && revivedCompanion.getHealth() == revivedCompanion.getMaxHealth(),
        Component.literal("Revived companion is not a healthy companion of the owner"));
    helper.assertTrue(
        findSavedNPCEntry(lifelessHandUUID).isEmpty(),
        Component.literal("Easy NPC still lists the consumed lifeless hand"));
    helper.succeed();
  }

  public void testRottenHandSpawnsNaturallyAtNight(GameTestHelper helper) {
    EntityType<?> rottenHandType = rottenHandType();
    helper.assertTrue(
        helper
            .getLevel()
            .registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getValueOrThrow(Biomes.PLAINS)
            .getMobSettings()
            .getMobs(MobCategory.MONSTER)
            .unwrap()
            .stream()
            .anyMatch(spawner -> spawner.value().type() == rottenHandType),
        Component.literal("Rotten hand is missing from the plains monster spawns"));
    helper.assertTrue(
        SpawnPlacements.getPlacementType(rottenHandType) == RottenHandEntity.SPAWN_PLACEMENT_TYPE,
        Component.literal("Rotten hand has no registered spawn placement"));

    ServerLevel level = helper.getLevel();
    helper.setBlock(TEST_POSITION.below(), Blocks.STONE);
    helper.setTime(MIDNIGHT_DAY_TIME);
    level.updateSkyBrightness();
    boolean spawnsAtMidnight = false;
    for (int i = 0; i < SPAWN_RULE_ATTEMPTS && !spawnsAtMidnight; i++) {
      spawnsAtMidnight = passesSpawnRules(helper, rottenHandType);
    }
    helper.assertTrue(
        spawnsAtMidnight,
        Component.literal("Rotten hand never passed its spawn rules at midnight"));

    helper.setTime(NOON_DAY_TIME);
    level.updateSkyBrightness();
    for (int i = 0; i < SPAWN_RULE_ATTEMPTS; i++) {
      helper.assertFalse(
          passesSpawnRules(helper, rottenHandType),
          Component.literal("Rotten hand passed its spawn rules at noon"));
    }
    helper.succeed();
  }

  public void testRottenHandUsesActiveModelProfile(GameTestHelper helper) {
    assertActiveModelProfile(
        helper,
        (RottenHandEntity)
            helper.spawn(
                rottenHandType(),
                Vec3.atBottomCenterOf(TEST_POSITION),
                EntitySpawnReason.SPAWN_ITEM_USE),
        ROTTEN_HAND_PROFILE_ID);
    helper.succeed();
  }

  public void testRottenHandDespawnsWhenFarAway(GameTestHelper helper) {
    helper.assertTrue(
        ((Mob)
                helper.spawn(
                    rottenHandType(),
                    Vec3.atBottomCenterOf(TEST_POSITION),
                    EntitySpawnReason.NATURAL))
            .removeWhenFarAway(DESPAWN_DISTANCE),
        Component.literal("Rotten hand would never despawn"));
    helper.succeed();
  }

  public void testRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    helper
        .spawn(rottenHandType(), TEST_POSITION)
        .hurtServer(
            helper.getLevel(),
            helper
                .getLevel()
                .damageSources()
                .playerAttack(helper.makeMockPlayer(GameType.SURVIVAL)),
            100.0F);
    helper.assertItemEntityPresent(lifelessRottenHandItem(), TEST_POSITION, 2.0D);
    helper.succeed();
  }

  public void testSummonedRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    try {
      level
          .getServer()
          .getCommands()
          .getDispatcher()
          .execute(
              "summon " + RottenHandEntity.ENTITY_ID,
              level
                  .getServer()
                  .createCommandSourceStack()
                  .withLevel(level)
                  .withPosition(helper.absoluteVec(Vec3.atBottomCenterOf(TEST_POSITION))));
    } catch (CommandSyntaxException e) {
      helper.fail(Component.literal("Summon command failed: " + e.getMessage()));
    }
    List<RottenHandEntity> summonedRottenHands =
        level.getEntitiesOfClass(RottenHandEntity.class, aroundTestPosition(helper, 2.0D));
    helper.assertFalse(
        summonedRottenHands.isEmpty(), Component.literal("Summon command spawned no rotten hand"));

    Entity rottenHand = summonedRottenHands.getFirst();
    helper.assertFalse(
        rottenHand.isInvulnerable(), Component.literal("Summoned rotten hand is invulnerable"));

    rottenHand.hurtServer(
        level,
        level.damageSources().playerAttack(helper.makeMockPlayer(GameType.SURVIVAL)),
        100.0F);
    helper.assertItemEntityPresent(lifelessRottenHandItem(), TEST_POSITION, 2.0D);
    helper.succeed();
  }

  public void testRottenHandAttacksAnimals(GameTestHelper helper) {
    Pig pig = helper.spawn(EntityType.PIG, TEST_POSITION);
    helper.spawn(rottenHandType(), Vec3.atBottomCenterOf(TEST_POSITION), EntitySpawnReason.NATURAL);
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                pig.getHealth() < pig.getMaxHealth(),
                Component.literal("Rotten hand did not attack the pig")));
  }

  public void testRottenHandSwimsToSurface(GameTestHelper helper) {
    fillWaterColumn(helper);
    RottenHandEntity rottenHand =
        (RottenHandEntity)
            helper.spawn(
                rottenHandType(), Vec3.atBottomCenterOf(TEST_POSITION), EntitySpawnReason.NATURAL);
    helper.assertTrue(
        rottenHand.hasObjective(ObjectiveType.FLOAT),
        Component.literal("Rotten hand has no float objective"));
    double waterSurfaceY = helper.absolutePos(TEST_POSITION.above(WATER_DEPTH - 1)).getY();
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                rottenHand.getY() > waterSurfaceY,
                Component.literal("Rotten hand did not swim to the water surface")));
  }

  public void testZombieDropsLifelessRottenHand(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    LootTable zombieLootTable =
        level.getServer().reloadableRegistries().getLootTable(HandLootInjections.ZOMBIE_LOOT_TABLE);
    Entity zombie = helper.spawn(EntityType.ZOMBIE, TEST_POSITION);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LootParams lootParams =
        new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, zombie)
            .withParameter(LootContextParams.ORIGIN, zombie.position())
            .withParameter(
                LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
            .withParameter(LootContextParams.ATTACKING_ENTITY, player)
            .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
            .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
            .create(LootContextParamSets.ENTITY);

    Item lifelessRottenHand = lifelessRottenHandItem();
    int handDrops = 0;
    for (int i = 0; i < ZOMBIE_LOOT_ROLLS; i++) {
      for (ItemStack itemStack : zombieLootTable.getRandomItems(lootParams)) {
        if (itemStack.is(lifelessRottenHand)) {
          handDrops++;
        }
      }
    }

    helper.assertTrue(
        handDrops >= MINIMUM_EXPECTED_HAND_DROPS && handDrops <= MAXIMUM_EXPECTED_HAND_DROPS,
        Component.literal(
            "Expected 1-5% lifeless rotten hands from "
                + ZOMBIE_LOOT_ROLLS
                + " zombie kills, got "
                + handDrops));
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
    AbstractLifelessHandEntity lifelessRottenHand =
        startRevivalRitual(
            helper, player, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);
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
    startRevivalRitual(
        helper, serverPlayer, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);
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

  public void testOnlyCompanionGrabsTarget(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    startRevivalRitual(
        helper,
        helper.makeMockPlayer(GameType.SURVIVAL),
        new ItemStack(lifelessRottenHandItem()),
        Items.ROTTEN_FLESH);

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

          RottenHandEntity wildRottenHand =
              (RottenHandEntity)
                  helper.spawn(
                      rottenHandType(),
                      Vec3.atBottomCenterOf(TEST_POSITION.west(2)),
                      EntitySpawnReason.NATURAL);
          pig.invulnerableTime = 0;
          helper.assertTrue(
              wildRottenHand.doHurtTarget(level, pig) && !pig.hasEffect(MobEffects.SLOWNESS),
              Component.literal("Wild rotten hand grabbed its target"));
          helper.succeed();
        });
  }

  public void testFallenCompanionWithoutOwnerLeavesProtectedRemains(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
    startRevivalRitual(helper, owner, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);

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
        helper
            .getLevel()
            .getServer()
            .getAdvancements()
            .get(HandAdvancements.companionAdvancementId(BoneHandEntity.ENTITY_ID));
    helper.assertTrue(
        companionAdvancement != null,
        Component.literal("Bone hand companion advancement is not loaded"));

    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(
        helper, owner, new ItemStack(item(LifelessBoneHandEntity.ENTITY_ID)), Items.BONE_MEAL);

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

  public void testDrownedHandBreathesUnderwater(GameTestHelper helper) {
    fillWaterColumn(helper);
    helper.setBlock(TEST_POSITION.above(WATER_DEPTH), Blocks.GLASS);
    DrownedHandEntity drownedHand =
        (DrownedHandEntity)
            helper.spawn(
                entityType(DrownedHandEntity.ENTITY_ID),
                Vec3.atBottomCenterOf(TEST_POSITION),
                EntitySpawnReason.NATURAL);
    helper.assertFalse(
        drownedHand.hasObjective(ObjectiveType.FLOAT),
        Component.literal("Drowned hand floats instead of diving"));

    helper.runAfterDelay(
        PAST_AIR_SUPPLY_TICKS,
        () -> {
          helper.assertTrue(
              drownedHand.isUnderWater(), Component.literal("Drowned hand left the water"));
          helper.assertTrue(
              drownedHand.getAirSupply() > 0
                  && drownedHand.getHealth() == drownedHand.getMaxHealth(),
              Component.literal("Drowned hand drowns underwater"));
          helper.succeed();
        });
  }

  public void testDrownedHandExtinguishesBurningOwner(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    placeBesideRitual(helper, owner);
    startRevivalRitual(
        helper, owner, new ItemStack(item(LifelessDrownedHandEntity.ENTITY_ID)), Items.KELP);

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
    startRevivalRitual(
        helper, owner, new ItemStack(item(LifelessBoneHandEntity.ENTITY_ID)), Items.BONE_MEAL);

    AtomicReference<BoneHandEntity> boneHand = new AtomicReference<>();
    AtomicReference<Mob> zombie = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(() -> boneHand.set(findEntityNearTestPosition(helper, BoneHandEntity.class)))
        .thenWaitUntil(
            () -> assertActiveGesture(helper, boneHand.get(), null, "Bone hand is still awakening"))
        .thenExecute(
            () -> {
              helper.setBlock(MONSTER_POSITION.below(), Blocks.STONE);
              zombie.set(helper.spawn(EntityType.ZOMBIE, MONSTER_POSITION));
              zombie.get().setNoAi(true);
            })
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

  public void testCompanionWavesAtStrangerOnce(GameTestHelper helper) {
    startRevivalRitual(
        helper,
        helper.makeMockPlayer(GameType.SURVIVAL),
        new ItemStack(lifelessRottenHandItem()),
        Items.ROTTEN_FLESH);

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
    startRevivalRitual(helper, owner, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);

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
    startRevivalRitual(helper, owner, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);

    AtomicReference<RottenHandEntity> companion = new AtomicReference<>();
    AtomicReference<Mob> attacker = new AtomicReference<>();
    AtomicReference<Pig> victim = new AtomicReference<>();
    helper
        .startSequence()
        .thenIdle(AFTER_RITUAL_TICKS)
        .thenExecute(
            () -> {
              companion.set(findEntityNearTestPosition(helper, RottenHandEntity.class));
              helper.setBlock(MONSTER_POSITION.below(), Blocks.STONE);
              attacker.set(helper.spawn(EntityType.ZOMBIE, MONSTER_POSITION));
              attacker.get().setNoAi(true);
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
              victim.set(helper.spawn(EntityType.PIG, MONSTER_POSITION));
              victim.get().setNoAi(true);
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
    startRevivalRitual(helper, owner, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);

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
              startRevivalRitual(
                  helper, owner, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);
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

  public void testFollowingCompanionJoinsOwnerInOtherDimension(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    startRevivalRitual(helper, owner, new ItemStack(lifelessRottenHandItem()), Items.ROTTEN_FLESH);

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
