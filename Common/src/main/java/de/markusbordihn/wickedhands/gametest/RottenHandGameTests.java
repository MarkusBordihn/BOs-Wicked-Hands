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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.item.LifelessRottenHandItem;
import de.markusbordihn.wickedhands.loot.ZombieLoot;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RottenHandGameTests {

  private static final BlockPos TEST_POSITION = new BlockPos(0, 1, 0);
  private static final long NOON_DAY_TIME = 6000L;
  private static final long MIDNIGHT_DAY_TIME = 18000L;
  private static final int SPAWN_RULE_ATTEMPTS = 200;
  private static final double DESPAWN_DISTANCE = 128.0D;
  private static final int ZOMBIE_LOOT_ROLLS = 2000;
  private static final int MINIMUM_EXPECTED_HAND_DROPS = 20;
  private static final int MAXIMUM_EXPECTED_HAND_DROPS = 100;

  public void testRottenHandSpawnsNaturallyAtNight(GameTestHelper helper) {
    EntityType<?> rottenHandType = rottenHandType();
    Biome plains =
        helper
            .getLevel()
            .registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getValueOrThrow(Biomes.PLAINS);
    boolean spawnsInPlains =
        plains.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
            .anyMatch(spawner -> spawner.value().type() == rottenHandType);
    helper.assertTrue(
        spawnsInPlains, Component.literal("Rotten hand is missing from the plains monster spawns"));
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

  public void testRottenHandDespawnsWhenFarAway(GameTestHelper helper) {
    Mob rottenHand =
        (Mob)
            helper.spawn(
                rottenHandType(), Vec3.atBottomCenterOf(TEST_POSITION), EntitySpawnReason.NATURAL);
    helper.assertTrue(
        rottenHand.removeWhenFarAway(DESPAWN_DISTANCE),
        Component.literal("Rotten hand would never despawn"));
    helper.succeed();
  }

  public void testRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    Entity rottenHand = helper.spawn(rottenHandType(), TEST_POSITION);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    rottenHand.hurtServer(
        helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 100.0F);
    helper.assertItemEntityPresent(lifelessRottenHandItem(), TEST_POSITION, 2.0D);
    helper.succeed();
  }

  public void testSummonedRottenHandDropsLifelessRottenHand(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    CommandSourceStack commandSource =
        level
            .getServer()
            .createCommandSourceStack()
            .withLevel(level)
            .withPosition(helper.absoluteVec(Vec3.atBottomCenterOf(TEST_POSITION)));
    CommandDispatcher<CommandSourceStack> commandDispatcher =
        level.getServer().getCommands().getDispatcher();
    try {
      commandDispatcher.execute("summon " + RottenHandEntity.ENTITY_ID, commandSource);
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

    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    rottenHand.hurtServer(level, level.damageSources().playerAttack(player), 100.0F);
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

  public void testZombieDropsLifelessRottenHand(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    LootTable zombieLootTable =
        level.getServer().reloadableRegistries().getLootTable(ZombieLoot.ZOMBIE_LOOT_TABLE);
    Entity zombie = helper.spawn(EntityType.ZOMBIE, TEST_POSITION);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    DamageSource playerAttack = level.damageSources().playerAttack(player);
    LootParams lootParams =
        new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, zombie)
            .withParameter(LootContextParams.ORIGIN, zombie.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, playerAttack)
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
}
