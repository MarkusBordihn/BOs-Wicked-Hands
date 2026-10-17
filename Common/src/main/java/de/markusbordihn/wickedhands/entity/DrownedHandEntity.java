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

import de.markusbordihn.easynpc.api.handler.EasyNPCAttributeHandler;
import de.markusbordihn.easynpc.data.attribute.EnvironmentalAttributeType;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.data.CompanionMode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class DrownedHandEntity extends AbstractCompanionHandEntity {

  public static final String ID = "drowned_hand";
  public static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, ID);
  private static final int EXTINGUISH_COOLDOWN_TICKS = 200;
  private static final double ATTACK_DAMAGE = 2.0D;
  private static final double WATER_MOVEMENT_EFFICIENCY = 1.0D;
  private static final int EXTINGUISH_CHECK_INTERVAL_TICKS = 5;
  private static final double EXTINGUISH_RANGE = 16.0D;
  private static final double EXTINGUISH_REACH = 2.5D;
  private static final double EXTINGUISH_SPEED_MODIFIER = 1.4D;

  private long nextExtinguishGameTime;

  public DrownedHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level, LifelessDrownedHandEntity.ENTITY_ID);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createHandAttributes(ATTACK_DAMAGE)
        .add(Attributes.WATER_MOVEMENT_EFFICIENCY, WATER_MOVEMENT_EFFICIENCY);
  }

  public static EntityType<DrownedHandEntity> createEntityType() {
    return createHandEntityType(DrownedHandEntity::new, MobCategory.MONSTER, ENTITY_ID);
  }

  @Override
  protected void applyMovementAttributes() {
    EasyNPCAttributeHandler.setEnvironmentalAttribute(
        this, EnvironmentalAttributeType.CAN_BREATHE_UNDERWATER, true);
  }

  @Override
  protected void companionTick(ServerLevel serverLevel) {
    if (this.tickCount % EXTINGUISH_CHECK_INTERVAL_TICKS != 0
        || serverLevel.getGameTime() < this.nextExtinguishGameTime
        || !(this.getOwner() instanceof LivingEntity owner)
        || !owner.isOnFire()) {
      return;
    }

    double distanceToOwnerSquared = this.distanceToSqr(owner);
    if (distanceToOwnerSquared > EXTINGUISH_RANGE * EXTINGUISH_RANGE) {
      return;
    }

    if (distanceToOwnerSquared > EXTINGUISH_REACH * EXTINGUISH_REACH) {
      if (this.getHandState().companionMode() != CompanionMode.STAY) {
        this.getNavigation().moveTo(owner, EXTINGUISH_SPEED_MODIFIER);
      }
      return;
    }

    this.extinguish(serverLevel, owner);
  }

  private void extinguish(ServerLevel serverLevel, LivingEntity owner) {
    this.nextExtinguishGameTime = serverLevel.getGameTime() + EXTINGUISH_COOLDOWN_TICKS;
    owner.clearFire();
    this.swing(InteractionHand.MAIN_HAND);
    this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F);
    serverLevel.sendParticles(
        ParticleTypes.SPLASH,
        owner.getX(),
        owner.getY(0.5D),
        owner.getZ(),
        24,
        0.3D,
        0.4D,
        0.3D,
        0.1D);
  }
}
