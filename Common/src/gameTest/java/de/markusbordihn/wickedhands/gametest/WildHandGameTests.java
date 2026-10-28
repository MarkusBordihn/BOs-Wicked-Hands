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
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.BoggedHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoggedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessBoneHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessDrownedHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessHuskHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessStrayHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitchHandEntity;
import de.markusbordihn.wickedhands.entity.LifelessWitheredHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.entity.WitheredHandEntity;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class WildHandGameTests extends HandGameTests {

  private static final Identifier ROTTEN_HAND_PROFILE_ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/rotten_hand");
  private static final long MIDNIGHT_DAY_TIME = 18000L;
  private static final int SPAWN_RULE_ATTEMPTS = 200;
  private static final Map<EntityType<?>, Identifier> LIFELESS_HAND_BY_MONSTER =
      Map.of(
          EntityTypes.ZOMBIE, LifelessRottenHandEntity.ENTITY_ID,
          EntityTypes.DROWNED, LifelessDrownedHandEntity.ENTITY_ID,
          EntityTypes.SKELETON, LifelessBoneHandEntity.ENTITY_ID,
          EntityTypes.HUSK, LifelessHuskHandEntity.ENTITY_ID,
          EntityTypes.STRAY, LifelessStrayHandEntity.ENTITY_ID,
          EntityTypes.WITHER_SKELETON, LifelessWitheredHandEntity.ENTITY_ID,
          EntityTypes.BOGGED, LifelessBoggedHandEntity.ENTITY_ID,
          EntityTypes.WITCH, LifelessWitchHandEntity.ENTITY_ID);
  private static final int LOOT_ROLLS_PER_MONSTER = 2000;
  private static final float FIRE_DAMAGE = 4.0F;
  private static final int MINIMUM_EXPECTED_HAND_DROPS = 20;
  private static final int MAXIMUM_EXPECTED_HAND_DROPS = 100;
  private static final int WATER_DEPTH = 3;
  private static final int PAST_AIR_SUPPLY_TICKS = 340;

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

  private static boolean passesSpawnRules(GameTestHelper helper, EntityType<?> entityType) {
    ServerLevel level = helper.getLevel();
    return SpawnPlacements.checkSpawnRules(
        entityType,
        level,
        EntitySpawnReason.NATURAL,
        helper.absolutePos(TEST_POSITION),
        level.getRandom());
  }

  private static void assertMonsterDropsLifelessHand(
      GameTestHelper helper, EntityType<?> monsterType, Identifier lifelessHandId) {
    ServerLevel level = helper.getLevel();
    LootTable monsterLootTable =
        level
            .getServer()
            .reloadableRegistries()
            .getLootTable(monsterType.getDefaultLootTable().orElseThrow());
    Entity monster = helper.spawn(monsterType, TEST_POSITION);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LootParams lootParams =
        new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, monster)
            .withParameter(LootContextParams.ORIGIN, monster.position())
            .withParameter(
                LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
            .withParameter(LootContextParams.ATTACKING_ENTITY, player)
            .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
            .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
            .create(LootContextParamSets.ENTITY);

    Item lifelessHand = item(lifelessHandId);
    int handDrops = 0;
    for (int i = 0; i < LOOT_ROLLS_PER_MONSTER; i++) {
      for (ItemStack itemStack : monsterLootTable.getRandomItems(lootParams)) {
        if (itemStack.is(lifelessHand)) {
          handDrops++;
        }
      }
    }
    monster.discard();

    helper.assertTrue(
        handDrops >= MINIMUM_EXPECTED_HAND_DROPS && handDrops <= MAXIMUM_EXPECTED_HAND_DROPS,
        Component.literal(
            "Expected 1-5% "
                + lifelessHandId
                + " from "
                + LOOT_ROLLS_PER_MONSTER
                + " "
                + EntityType.getKey(monsterType)
                + " kills, got "
                + handDrops));
  }

  public void testRottenHandSpawnsNaturallyAtNight(GameTestHelper helper) {
    EntityType<?> rottenHandType = rottenHandType();
    ServerLevel level = helper.getLevel();
    helper.assertTrue(
        level
            .getBiome(helper.absolutePos(TEST_POSITION))
            .value()
            .getAttributes()
            .applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY)
            .getMobsInCategory(MobCategory.MONSTER)
            .unwrap()
            .stream()
            .anyMatch(spawner -> spawner.value().type() == rottenHandType),
        Component.literal("Rotten hand is missing from the monster spawns of the test biome"));
    helper.assertTrue(
        SpawnPlacements.getPlacementType(rottenHandType) == RottenHandEntity.SPAWN_PLACEMENT_TYPE,
        Component.literal("Rotten hand has no registered spawn placement"));

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
        spawnWildHand(helper, RottenHandEntity.ENTITY_ID, TEST_POSITION)
            .removeWhenFarAway(DESPAWN_DISTANCE),
        Component.literal("Rotten hand would never despawn"));
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
    Pig pig = helper.spawn(EntityTypes.PIG, TEST_POSITION);
    spawnWildHand(helper, RottenHandEntity.ENTITY_ID, TEST_POSITION);
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                pig.getHealth() < pig.getMaxHealth(),
                Component.literal("Rotten hand did not attack the pig")));
  }

  public void testRottenHandSwimsToSurface(GameTestHelper helper) {
    fillWaterColumn(helper);
    AbstractHandEntity rottenHand =
        spawnWildHand(helper, RottenHandEntity.ENTITY_ID, TEST_POSITION);
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

  public void testMonstersDropTheirLifelessHand(GameTestHelper helper) {
    LIFELESS_HAND_BY_MONSTER.forEach(
        (monsterType, lifelessHandId) ->
            assertMonsterDropsLifelessHand(helper, monsterType, lifelessHandId));
    helper.succeed();
  }

  public void testWitheredHandIsImmuneToFire(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    AbstractHandEntity boneHand =
        spawnWildHand(helper, BoneHandEntity.ENTITY_ID, TEST_POSITION.east(2));
    AbstractHandEntity witheredHand =
        spawnWildHand(helper, WitheredHandEntity.ENTITY_ID, TEST_POSITION.west(2));
    helper.assertTrue(
        boneHand.hurtServer(level, level.damageSources().inFire(), FIRE_DAMAGE),
        Component.literal("Bone hand was not hurt by fire"));
    helper.assertFalse(
        witheredHand.hurtServer(level, level.damageSources().inFire(), FIRE_DAMAGE)
            || witheredHand.hurtServer(level, level.damageSources().lava(), FIRE_DAMAGE),
        Component.literal("Withered hand was hurt by fire"));
    helper.assertTrue(
        witheredHand.getHealth() == witheredHand.getMaxHealth(),
        Component.literal("Withered hand lost health to fire"));
    helper.succeed();
  }

  public void testBoggedHandIsImmuneToPoison(GameTestHelper helper) {
    AbstractHandEntity rottenHand =
        spawnWildHand(helper, RottenHandEntity.ENTITY_ID, TEST_POSITION.east(2));
    AbstractHandEntity boggedHand =
        spawnWildHand(helper, BoggedHandEntity.ENTITY_ID, TEST_POSITION.west(2));
    helper.assertTrue(
        rottenHand.addEffect(new MobEffectInstance(MobEffects.POISON, OWNER_POISON_TICKS)),
        Component.literal("Rotten hand could not be poisoned"));
    helper.assertFalse(
        boggedHand.addEffect(new MobEffectInstance(MobEffects.POISON, OWNER_POISON_TICKS)),
        Component.literal("Bogged hand was poisoned"));
    helper.succeed();
  }

  public void testDrownedHandBreathesUnderwater(GameTestHelper helper) {
    fillWaterColumn(helper);
    helper.setBlock(TEST_POSITION.above(WATER_DEPTH), Blocks.GLASS);
    AbstractHandEntity drownedHand =
        spawnWildHand(helper, DrownedHandEntity.ENTITY_ID, TEST_POSITION);
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
}
