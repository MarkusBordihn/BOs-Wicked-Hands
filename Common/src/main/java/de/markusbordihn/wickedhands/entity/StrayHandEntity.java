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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class StrayHandEntity extends AbstractCompanionHandEntity {

  public static final String ID = "stray_hand";
  public static final ResourceLocation ENTITY_ID =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, ID);
  private static final double ATTACK_DAMAGE = 1.5D;
  private static final int CHILL_DURATION_TICKS = 60;
  private static final int HARMLESS_FROZEN_TICKS = 100;

  public StrayHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level, LifelessStrayHandEntity.ENTITY_ID);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createHandAttributes(ATTACK_DAMAGE);
  }

  public static EntityType<StrayHandEntity> createEntityType() {
    return createHandEntityType(StrayHandEntity::new, MobCategory.MONSTER, ENTITY_ID);
  }

  @Override
  protected void applyHitEffect(ServerLevel serverLevel, LivingEntity target) {
    target.addEffect(
        new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CHILL_DURATION_TICKS), this);
    if (target.canFreeze()) {
      target.setTicksFrozen(Math.max(target.getTicksFrozen(), HARMLESS_FROZEN_TICKS));
    }
    this.playSound(SoundEvents.POWDER_SNOW_BREAK, 1.0F, 1.2F);
    spawnHitParticles(serverLevel, target, ParticleTypes.SNOWFLAKE, 16, 0.02D);
  }
}
