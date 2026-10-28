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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.world.level.block.Blocks;

public class HuskHandEntity extends AbstractCompanionHandEntity {

  public static final String ID = "husk_hand";
  public static final ParticleOptions SAND_DUST =
      new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
  public static final ResourceLocation ENTITY_ID =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, ID);
  private static final double ATTACK_DAMAGE = 2.0D;
  private static final int PARCH_DURATION_TICKS = 80;

  public HuskHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level, LifelessHuskHandEntity.ENTITY_ID);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createHandAttributes(ATTACK_DAMAGE);
  }

  public static EntityType<HuskHandEntity> createEntityType() {
    return createHandEntityType(HuskHandEntity::new, MobCategory.MONSTER, ENTITY_ID);
  }

  @Override
  protected void applyHitEffect(ServerLevel serverLevel, LivingEntity target) {
    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, PARCH_DURATION_TICKS), this);
    this.playSound(SoundEvents.HUSK_AMBIENT, 0.6F, 1.4F);
    spawnHitParticles(serverLevel, target, SAND_DUST, 16, 0.0D);
  }
}
