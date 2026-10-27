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
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class WitchHandEntity extends AbstractCompanionHandEntity {

  public static final String ID = "witch_hand";
  public static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, ID);
  public static final float MENDING_HEALTH_RATIO = 0.3F;
  public static final double MENDING_RANGE = 8.0D;
  public static final int MENDING_DURATION_TICKS = 100;
  public static final int MENDING_COOLDOWN_TICKS = 1200;
  public static final int MENDING_AMPLIFIER = 1;
  private static final double ATTACK_DAMAGE = 1.5D;
  private static final int HEX_DURATION_TICKS = 80;
  private static final List<Holder<MobEffect>> HEX_EFFECTS =
      List.of(MobEffects.POISON, MobEffects.SLOWNESS, MobEffects.WEAKNESS);

  private long nextMendingGameTime;

  public WitchHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level, LifelessWitchHandEntity.ENTITY_ID);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createHandAttributes(ATTACK_DAMAGE);
  }

  public static EntityType<WitchHandEntity> createEntityType() {
    return createHandEntityType(WitchHandEntity::new, MobCategory.MONSTER, ENTITY_ID);
  }

  @Override
  protected void companionTick(ServerLevel serverLevel) {
    if (this.tickCount % COMPANION_CHECK_INTERVAL_TICKS != 0
        || serverLevel.getGameTime() < this.nextMendingGameTime
        || !(this.getOwner() instanceof LivingEntity owner)
        || !owner.isAlive()
        || owner.getHealth() >= owner.getMaxHealth() * MENDING_HEALTH_RATIO
        || this.distanceToSqr(owner) > MENDING_RANGE * MENDING_RANGE) {
      return;
    }

    this.mendOwnerAndSiblings(serverLevel, owner);
  }

  private void mendOwnerAndSiblings(ServerLevel serverLevel, LivingEntity owner) {
    this.nextMendingGameTime = serverLevel.getGameTime() + MENDING_COOLDOWN_TICKS;
    owner.addEffect(createMendingEffect(), this);
    serverLevel
        .getEntitiesOfClass(
            AbstractCompanionHandEntity.class,
            owner.getBoundingBox().inflate(MENDING_RANGE),
            hand -> hand.isCompanion() && owner.getUUID().equals(hand.getOwnerUUID()))
        .forEach(hand -> hand.addEffect(createMendingEffect(), this));
    this.swing(InteractionHand.MAIN_HAND);
    this.playSound(SoundEvents.WITCH_DRINK, 0.7F, 1.4F);
    serverLevel.sendParticles(
        ParticleTypes.HAPPY_VILLAGER,
        owner.getX(),
        owner.getY(0.5D),
        owner.getZ(),
        16,
        0.4D,
        0.5D,
        0.4D,
        0.0D);
  }

  private static MobEffectInstance createMendingEffect() {
    return new MobEffectInstance(MobEffects.REGENERATION, MENDING_DURATION_TICKS, MENDING_AMPLIFIER);
  }

  @Override
  protected void applyHitEffect(ServerLevel serverLevel, LivingEntity target) {
    Holder<MobEffect> hexEffect = HEX_EFFECTS.get(this.getRandom().nextInt(HEX_EFFECTS.size()));
    target.addEffect(new MobEffectInstance(hexEffect, HEX_DURATION_TICKS), this);
    this.playSound(SoundEvents.WITCH_CELEBRATE, 0.6F, 1.4F);
    spawnHitParticles(serverLevel, target, ParticleTypes.WITCH, 12, 0.0D);
  }
}
