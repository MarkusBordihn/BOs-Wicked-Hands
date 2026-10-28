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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.PotionUtils;

public enum ShoulderPassive {
  FIRM_GRIP(
      RottenHandEntity.ENTITY_ID,
      0x6B8E3A,
      Attributes.KNOCKBACK_RESISTANCE,
      0.5D,
      Operation.ADDITION),
  SECOND_BREATH(DrownedHandEntity.ENTITY_ID, 0x3E8C84),
  BONE_GUARD(BoneHandEntity.ENTITY_ID, 0xD8D2C0, Attributes.ARMOR, 2.0D, Operation.ADDITION),
  DRY_STOMACH(HuskHandEntity.ENTITY_ID, 0xC2A36B, owner -> owner.removeEffect(MobEffects.HUNGER)),
  COLD_BLOOD(StrayHandEntity.ENTITY_ID, 0x9FB8C4, owner -> owner.setTicksFrozen(0)),
  ASHEN_SKIN(WitheredHandEntity.ENTITY_ID, 0x4FD6D6),
  MOSSY_VEINS(BoggedHandEntity.ENTITY_ID, 0x5E7A34, owner -> owner.removeEffect(MobEffects.POISON)),
  WITCH_SIGHT(WitchHandEntity.ENTITY_ID, 0x7E3F98, ShoulderPassive::refreshNightVision);

  public static final int NIGHT_VISION_DURATION_TICKS = 400;
  // Vanilla flickers Night Vision during its last 200 ticks, see GameRenderer.getNightVisionScale.
  public static final int NIGHT_VISION_REFRESH_TICKS = 220;

  private final ResourceLocation effectId =
      new ResourceLocation(Constants.MOD_ID, this.name().toLowerCase(Locale.ROOT));
  private final UUID modifierUUID =
      UUID.nameUUIDFromBytes(
          this.effectId.withPrefix("shoulder_passive/").toString().getBytes(StandardCharsets.UTF_8));
  private final TagKey<DamageType> grippingDamage =
      TagKey.create(Registries.DAMAGE_TYPE, this.effectId.withPrefix("shoulder_grip/"));
  private final ResourceLocation handId;
  private final int color;
  private final Attribute attribute;
  private final double amount;
  private final Operation operation;
  private final Consumer<ServerPlayer> ownerAction;

  ShoulderPassive(
      ResourceLocation handId, int color, Attribute attribute, double amount, Operation operation) {
    this.handId = handId;
    this.color = color;
    this.attribute = attribute;
    this.amount = amount;
    this.operation = operation;
    this.ownerAction = owner -> {};
  }

  ShoulderPassive(ResourceLocation handId, int color, Consumer<ServerPlayer> ownerAction) {
    this.handId = handId;
    this.color = color;
    this.attribute = null;
    this.amount = 0.0D;
    this.operation = Operation.ADDITION;
    this.ownerAction = ownerAction;
  }

  ShoulderPassive(ResourceLocation handId, int color) {
    this(handId, color, owner -> {});
  }

  private static void refreshNightVision(ServerPlayer owner) {
    MobEffectInstance nightVision = owner.getEffect(MobEffects.NIGHT_VISION);
    if (nightVision == null || nightVision.endsWithin(NIGHT_VISION_REFRESH_TICKS)) {
      owner.addEffect(
          new MobEffectInstance(
              MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION_TICKS, 0, true, false, false));
    }
  }

  public static Optional<ShoulderPassive> forHand(ResourceLocation handId) {
    return Arrays.stream(values())
        .filter(shoulderPassive -> shoulderPassive.handId.equals(handId))
        .findFirst();
  }

  private static boolean isPoisonArrow(Entity entity) {
    if (!(entity instanceof Arrow arrow)) {
      return false;
    }

    CompoundTag arrowTag = new CompoundTag();
    arrow.addAdditionalSaveData(arrowTag);
    for (MobEffectInstance mobEffectInstance : PotionUtils.getAllEffects(arrowTag)) {
      if (mobEffectInstance.getEffect() == MobEffects.POISON) {
        return true;
      }
    }
    return false;
  }

  public ResourceLocation getEffectId() {
    return this.effectId;
  }

  public UUID getModifierUUID() {
    return this.modifierUUID;
  }

  public Optional<Attribute> getAttribute() {
    return Optional.ofNullable(this.attribute);
  }

  public MobEffect getEffect() {
    return BuiltInRegistries.MOB_EFFECT.get(this.effectId);
  }

  public MobEffect createEffect() {
    MobEffect mobEffect =
        new MobEffect(MobEffectCategory.BENEFICIAL, this.color) {
          @Override
          public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
            ShoulderPassive.this.slowDownDrowningAndBurning(livingEntity);
          }

          @Override
          public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
          }
        };
    if (this.attribute != null) {
      mobEffect.addAttributeModifier(
          this.attribute, this.modifierUUID.toString(), this.amount, this.operation);
    }
    return mobEffect;
  }

  private void slowDownDrowningAndBurning(LivingEntity owner) {
    switch (this) {
      case SECOND_BREATH -> {
        if (owner.isUnderWater()
            && owner.tickCount % 2 == 0
            && owner.getAirSupply() < owner.getMaxAirSupply()) {
          owner.setAirSupply(owner.getAirSupply() + 1);
        }
      }
      case ASHEN_SKIN -> {
        if (owner.getRemainingFireTicks() > 0) {
          owner.setRemainingFireTicks(owner.getRemainingFireTicks() - 1);
        }
      }
      default -> {}
    }
  }

  // Removing Hunger in a MobEffect tick would modify activeEffects while vanilla iterates it.
  void apply(ServerPlayer owner) {
    this.ownerAction.accept(owner);
    MobEffect effect = this.getEffect();
    if (!owner.hasEffect(effect)) {
      owner.addEffect(
          new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
    }
  }

  void remove(ServerPlayer owner) {
    owner.removeEffect(this.getEffect());
  }

  boolean holdsOnThrough(DamageSource damageSource) {
    return damageSource.is(this.grippingDamage)
        || (this == MOSSY_VEINS && isPoisonArrow(damageSource.getDirectEntity()));
  }

  boolean holdsOnInSurroundingsOf(ServerPlayer owner) {
    return switch (this) {
      case SECOND_BREATH -> owner.isInWater();
      case COLD_BLOOD -> owner.isInPowderSnow;
      default -> false;
    };
  }
}
