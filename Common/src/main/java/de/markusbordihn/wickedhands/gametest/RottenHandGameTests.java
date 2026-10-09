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
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.advancement.HandAdvancements;
import de.markusbordihn.wickedhands.data.CompanionMode;
import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.entity.AbstractRottenHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.item.LifelessRottenHandItem;
import de.markusbordihn.wickedhands.loot.ZombieLoot;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RottenHandGameTests {

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
      GameTestHelper helper, AbstractRottenHandEntity hand, Identifier expectedProfileId) {
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
    helper.setBlock(TEST_POSITION.below(), Blocks.SOUL_SAND);
    helper.setBlock(TEST_POSITION, Blocks.AIR);
    Item lifelessRottenHand = lifelessRottenHandItem();
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(lifelessRottenHand));
    BlockPos ritualBasePosition = helper.absolutePos(TEST_POSITION.below());
    helper.assertTrue(
        lifelessRottenHand
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
        Component.literal("Lifeless rotten hand could not be placed on soul sand"));

    return findEntityNearTestPosition(helper, LifelessRottenHandEntity.class);
  }

  private static <T extends Entity> T findEntityNearTestPosition(
      GameTestHelper helper, Class<T> entityClass) {
    List<T> entities =
        helper
            .getLevel()
            .getEntitiesOfClass(
                entityClass, new AABB(helper.absolutePos(TEST_POSITION)).inflate(1.0D));
    helper.assertFalse(
        entities.isEmpty(), Component.literal("No " + entityClass.getSimpleName() + " was found"));
    return entities.getFirst();
  }

  private static boolean passesSpawnRules(GameTestHelper helper, EntityType<?> rottenHandType) {
    ServerLevel level = helper.getLevel();
    return SpawnPlacements.checkSpawnRules(
        rottenHandType,
        level,
        EntitySpawnReason.NATURAL,
        helper.absolutePos(TEST_POSITION),
        level.getRandom());
  }

  private static EntityType<?> rottenHandType() {
    return BuiltInRegistries.ENTITY_TYPE.getValue(RottenHandEntity.ENTITY_ID);
  }

  private static Item lifelessRottenHandItem() {
    return BuiltInRegistries.ITEM.getValue(LifelessRottenHandItem.ITEM_ID);
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
        level.getEntitiesOfClass(
            RottenHandEntity.class, new AABB(helper.absolutePos(TEST_POSITION)).inflate(2.0D));
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
    helper.setBlock(TEST_POSITION.below(), Blocks.GLASS);
    for (int depth = 0; depth < WATER_DEPTH; depth++) {
      BlockPos waterPosition = TEST_POSITION.above(depth);
      helper.setBlock(waterPosition, Blocks.WATER);
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        helper.setBlock(waterPosition.relative(direction), Blocks.GLASS);
      }
    }
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
        level.getServer().reloadableRegistries().getLootTable(ZombieLoot.ZOMBIE_LOOT_TABLE);
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
    placeLitCandles(helper);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LifelessRottenHandEntity lifelessRottenHand = placeLifelessRottenHand(helper, player);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH));
    lifelessRottenHand.mobInteract(player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        lifelessRottenHand.getRevivalState().ownerUUID().equals(Optional.of(player.getUUID())),
        Component.literal("Revival ritual did not start for the ritual player"));
    helper.assertTrue(
        player.getMainHandItem().isEmpty(), Component.literal("Catalyst was not consumed"));

    helper.runAfterDelay(
        RevivalRitual.DURATION_TICKS + 5,
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

          player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
          player.setShiftKeyDown(true);
          rottenHand.mobInteract(player, InteractionHand.MAIN_HAND);
          helper.assertTrue(
              rottenHand.getHandState().companionMode() == CompanionMode.STAY
                  && rottenHand.hasObjective(ObjectiveType.MOVE_BACK_TO_HOME),
              Component.literal("Sneak interaction did not make the companion stay"));
          helper.succeed();
        });
  }

  public void testRevivalRitualGrantsAdvancements(GameTestHelper helper) {
    ServerAdvancementManager advancementManager = helper.getLevel().getServer().getAdvancements();
    helper.assertTrue(
        advancementManager.get(HandAdvancements.ROOT_ID) != null,
        Component.literal("Root advancement is not loaded"));
    AdvancementHolder summonAdvancement =
        advancementManager.get(HandAdvancements.SUMMON_ROTTEN_HAND_ID);
    AdvancementHolder companionAdvancement =
        advancementManager.get(HandAdvancements.ROTTEN_HAND_COMPANION_ID);
    helper.assertTrue(
        summonAdvancement != null && companionAdvancement != null,
        Component.literal("Ritual advancements are not loaded"));

    placeLitCandles(helper);
    ServerPlayer serverPlayer = helper.makeMockServerPlayerInLevel();
    LifelessRottenHandEntity lifelessRottenHand = placeLifelessRottenHand(helper, serverPlayer);
    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH));
    lifelessRottenHand.mobInteract(serverPlayer, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        serverPlayer.getAdvancements().getOrStartProgress(summonAdvancement).isDone(),
        Component.literal("Starting the ritual did not grant the summon advancement"));

    helper.runAfterDelay(
        RevivalRitual.DURATION_TICKS + 5,
        () -> {
          helper.assertTrue(
              serverPlayer.getAdvancements().getOrStartProgress(companionAdvancement).isDone(),
              Component.literal("Reviving the hand did not grant the companion advancement"));
          helper.succeed();
        });
  }
}
