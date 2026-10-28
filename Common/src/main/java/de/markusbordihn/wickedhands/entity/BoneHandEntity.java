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
import java.util.Comparator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BoneHandEntity extends AbstractCompanionHandEntity {

  public static final String ID = "bone_hand";
  public static final ResourceLocation ENTITY_ID =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, ID);
  public static final int WARNING_COOLDOWN_TICKS = 200;
  private static final double ATTACK_DAMAGE = 1.5D;
  private static final double WARNING_RANGE = 12.0D;
  private static final double MINIMUM_APPROACH_SPEED_SQUARED = 1.0E-4D;

  private long nextWarningGameTime;

  public BoneHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level, LifelessBoneHandEntity.ENTITY_ID);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createHandAttributes(ATTACK_DAMAGE);
  }

  public static EntityType<BoneHandEntity> createEntityType() {
    return createHandEntityType(BoneHandEntity::new, MobCategory.MONSTER, ENTITY_ID);
  }

  private static boolean threatens(Monster monster, LivingEntity owner) {
    if (monster.getTarget() == owner) {
      return true;
    }

    Vec3 horizontalMotion = monster.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
    return horizontalMotion.lengthSqr() > MINIMUM_APPROACH_SPEED_SQUARED
        && horizontalMotion.dot(owner.position().subtract(monster.position())) > 0.0D;
  }

  @Override
  protected void companionTick(ServerLevel serverLevel) {
    if (this.tickCount % COMPANION_CHECK_INTERVAL_TICKS != 0
        || this.getActiveGesture() != null
        || this.getTarget() != null
        || serverLevel.getGameTime() < this.nextWarningGameTime
        || !(this.getOwner() instanceof LivingEntity owner)) {
      return;
    }

    serverLevel
        .getEntitiesOfClass(
            Monster.class,
            owner.getBoundingBox().inflate(WARNING_RANGE),
            monster -> monster.isAlive() && threatens(monster, owner))
        .stream()
        .min(Comparator.comparingDouble(owner::distanceToSqr))
        .ifPresent(monster -> this.warnAbout(serverLevel, monster));
  }

  private void warnAbout(ServerLevel serverLevel, Monster monster) {
    this.nextWarningGameTime = serverLevel.getGameTime() + WARNING_COOLDOWN_TICKS;
    this.playGesture(HandGesture.POINT, monster);
    this.playSound(SoundEvents.BONE_BLOCK_HIT, 0.6F, 1.4F);
  }
}
