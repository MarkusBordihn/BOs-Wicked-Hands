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

import de.markusbordihn.wickedhands.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class RottenHandEntity extends AbstractCompanionHandEntity {

  public static final String ID = "rotten_hand";
  public static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, ID);
  public static final int SPAWN_WEIGHT = 5;
  public static final int SPAWN_GROUP_SIZE = 1;
  public static final SpawnPlacementType SPAWN_PLACEMENT_TYPE = SpawnPlacementTypes.ON_GROUND;
  public static final Heightmap.Types SPAWN_HEIGHTMAP = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
  private static final double ATTACK_DAMAGE = 2.0D;
  private static final int GRAB_DURATION_TICKS = 30;
  private static final int GRAB_SLOWNESS_AMPLIFIER = 3;

  public RottenHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level, LifelessRottenHandEntity.ENTITY_ID);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createHandAttributes(ATTACK_DAMAGE);
  }

  public static EntityType<RottenHandEntity> createEntityType() {
    return createHandEntityType(RottenHandEntity::new, MobCategory.MONSTER, ENTITY_ID);
  }

  public static boolean checkRottenHandSpawnRules(
      EntityType<RottenHandEntity> entityType,
      ServerLevelAccessor level,
      EntitySpawnReason spawnReason,
      BlockPos spawnPosition,
      RandomSource random) {
    return level.getDifficulty() != Difficulty.PEACEFUL
        && level.getLevel().isDarkOutside()
        && Monster.isDarkEnoughToSpawn(level, spawnPosition, random)
        && Mob.checkMobSpawnRules(entityType, level, spawnReason, spawnPosition, random);
  }

  @Override
  protected void applyHitEffect(ServerLevel serverLevel, LivingEntity target) {
    target.addEffect(
        new MobEffectInstance(MobEffects.SLOWNESS, GRAB_DURATION_TICKS, GRAB_SLOWNESS_AMPLIFIER),
        this);
    target.setDeltaMovement(Vec3.ZERO);
    target.syncVelocity = true;
    this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 0.6F);
    BlockState groundState = target.getBlockStateOn();
    if (!groundState.isAir()) {
      serverLevel.sendParticles(
          new BlockParticleOption(ParticleTypes.BLOCK, groundState),
          target.getX(),
          target.getY(),
          target.getZ(),
          12,
          0.25D,
          0.05D,
          0.25D,
          0.1D);
    }
  }
}
