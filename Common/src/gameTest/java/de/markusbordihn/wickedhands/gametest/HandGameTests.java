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

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileManager;
import de.markusbordihn.wickedhands.advancement.HandAdvancements;
import de.markusbordihn.wickedhands.entity.AbstractCompanionHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractLifelessHandEntity;
import de.markusbordihn.wickedhands.entity.HandGesture;
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class HandGameTests {

  public static final int BEHAVIOUR_MAX_TICKS = 200;
  public static final int RITUAL_MAX_TICKS = 200;
  public static final int TWO_RITUALS_MAX_TICKS = 400;
  public static final int UNDERWATER_MAX_TICKS = 400;
  public static final int AFTER_AWAKENING_MAX_TICKS = 500;
  protected static final BlockPos TEST_POSITION = new BlockPos(0, 1, 0);
  protected static final double DESPAWN_DISTANCE = 128.0D;
  protected static final int OWNER_POISON_TICKS = 200;
  protected static final int COOLDOWN_END_MARGIN_TICKS = 20;
  protected static final int AFTER_RITUAL_TICKS = RevivalRitual.DURATION_TICKS + 5;
  protected static final BlockPos BESIDE_RITUAL_POSITION = TEST_POSITION.south(3);
  protected static final BlockPos MONSTER_POSITION = TEST_POSITION.north(4);
  protected static final List<BlockPos> CANDLE_POSITIONS =
      List.of(
          TEST_POSITION.east(), TEST_POSITION.west(), TEST_POSITION.north(), TEST_POSITION.south());
  private static final int RITUAL_AREA_RADIUS = 8;

  protected static void placeLitCandles(GameTestHelper helper) {
    for (BlockPos candlePosition : CANDLE_POSITIONS) {
      helper.setBlock(candlePosition.below(), Blocks.STONE);
      helper.setBlock(
          candlePosition, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true));
    }
  }

  protected static void assertActiveModelProfile(
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

  protected static <T extends AbstractLifelessHandEntity> T placeLifelessHand(
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

  protected static AbstractLifelessHandEntity startRottenHandRitual(
      GameTestHelper helper, Player player) {
    return startRevivalRitual(
        helper, player, LifelessRottenHandEntity.ENTITY_ID, Items.ROTTEN_FLESH);
  }

  protected static AbstractLifelessHandEntity startRevivalRitual(
      GameTestHelper helper, Player player, Identifier lifelessHandId, Item catalyst) {
    return startRevivalRitual(helper, player, new ItemStack(item(lifelessHandId)), catalyst);
  }

  protected static AbstractLifelessHandEntity startRevivalRitual(
      GameTestHelper helper, Player player, ItemStack lifelessHandStack, Item catalyst) {
    forceLoadRitualArea(helper);
    placeLitCandles(helper);
    AbstractLifelessHandEntity lifelessHand =
        placeLifelessHand(helper, player, lifelessHandStack, AbstractLifelessHandEntity.class);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(catalyst));
    lifelessHand.mobInteract(player, InteractionHand.MAIN_HAND);
    return lifelessHand;
  }

  // Vanilla force-loads only the chunks of the 1x1x1 structure, entities beyond them stop ticking.
  private static void forceLoadRitualArea(GameTestHelper helper) {
    BlockPos ritualPosition = helper.absolutePos(TEST_POSITION);
    ChunkPos northWestChunk =
        ChunkPos.containing(ritualPosition.offset(-RITUAL_AREA_RADIUS, 0, -RITUAL_AREA_RADIUS));
    ChunkPos southEastChunk =
        ChunkPos.containing(ritualPosition.offset(RITUAL_AREA_RADIUS, 0, RITUAL_AREA_RADIUS));
    for (int chunkX = northWestChunk.x(); chunkX <= southEastChunk.x(); chunkX++) {
      for (int chunkZ = northWestChunk.z(); chunkZ <= southEastChunk.z(); chunkZ++) {
        helper.getLevel().setChunkForced(chunkX, chunkZ, true);
      }
    }
  }

  protected static void placeBesideRitual(GameTestHelper helper, Player player) {
    helper.setBlock(BESIDE_RITUAL_POSITION.north().below(), Blocks.STONE);
    helper.setBlock(BESIDE_RITUAL_POSITION.below(), Blocks.STONE);
    player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(BESIDE_RITUAL_POSITION)));
  }

  protected static AdvancementHolder companionAdvancement(
      GameTestHelper helper, Identifier companionId) {
    return helper
        .getLevel()
        .getServer()
        .getAdvancements()
        .get(HandAdvancements.companionAdvancementId(companionId));
  }

  protected static void assertActiveGesture(
      GameTestHelper helper,
      AbstractCompanionHandEntity hand,
      HandGesture expectedGesture,
      String failureMessage) {
    helper.assertTrue(
        hand.getActiveGesture() == expectedGesture, Component.literal(failureMessage));
  }

  protected static AABB aroundTestPosition(GameTestHelper helper, double radius) {
    return new AABB(helper.absolutePos(TEST_POSITION)).inflate(radius);
  }

  protected static <T extends Entity> T findEntityNearTestPosition(
      GameTestHelper helper, Class<T> entityClass) {
    List<T> entities =
        helper.getLevel().getEntitiesOfClass(entityClass, aroundTestPosition(helper, 1.0D));
    helper.assertFalse(
        entities.isEmpty(), Component.literal("No " + entityClass.getSimpleName() + " was found"));
    return entities.getFirst();
  }

  protected static EntityType<?> rottenHandType() {
    return entityType(RottenHandEntity.ENTITY_ID);
  }

  protected static EntityType<?> entityType(Identifier entityId) {
    return BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
  }

  protected static Item lifelessRottenHandItem() {
    return item(LifelessRottenHandEntity.ENTITY_ID);
  }

  protected static Item item(Identifier itemId) {
    return BuiltInRegistries.ITEM.getValue(itemId);
  }

  protected static AbstractHandEntity spawnWildHand(
      GameTestHelper helper, Identifier handId, BlockPos position) {
    return (AbstractHandEntity)
        helper.spawn(
            entityType(handId), Vec3.atBottomCenterOf(position), EntitySpawnReason.NATURAL);
  }

  protected static <T extends Mob> T spawnMobWithoutAi(
      GameTestHelper helper, EntityType<T> entityType, BlockPos position) {
    helper.setBlock(position.below(), Blocks.STONE);
    T mob = helper.spawn(entityType, position);
    mob.setNoAi(true);
    return mob;
  }
}
