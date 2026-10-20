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
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public enum ShoulderPassive {
  FIRM_GRIP(
      RottenHandEntity.ENTITY_ID,
      0x6B8E3A,
      Attributes.KNOCKBACK_RESISTANCE,
      0.5D,
      Operation.ADD_VALUE),
  SECOND_BREATH(
      DrownedHandEntity.ENTITY_ID, 0x3E8C84, Attributes.OXYGEN_BONUS, 1.0D, Operation.ADD_VALUE),
  BONE_GUARD(BoneHandEntity.ENTITY_ID, 0xD8D2C0, Attributes.ARMOR, 2.0D, Operation.ADD_VALUE),
  DRY_STOMACH(HuskHandEntity.ENTITY_ID, 0xC2A36B, owner -> owner.removeEffect(MobEffects.HUNGER)),
  COLD_BLOOD(StrayHandEntity.ENTITY_ID, 0x9FB8C4, owner -> owner.setTicksFrozen(0)),
  ASHEN_SKIN(
      WitheredHandEntity.ENTITY_ID,
      0x4FD6D6,
      Attributes.BURNING_TIME,
      -0.5D,
      Operation.ADD_MULTIPLIED_TOTAL);

  private final Identifier effectId =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, this.name().toLowerCase(Locale.ROOT));
  private final ResourceKey<MobEffect> effectKey =
      ResourceKey.create(Registries.MOB_EFFECT, this.effectId);
  private final Identifier modifierId = this.effectId.withPrefix("shoulder_passive/");
  private final Identifier handId;
  private final int color;
  private final Holder<Attribute> attribute;
  private final double amount;
  private final Operation operation;
  private final Consumer<ServerPlayer> ownerAction;

  ShoulderPassive(
      Identifier handId,
      int color,
      Holder<Attribute> attribute,
      double amount,
      Operation operation) {
    this.handId = handId;
    this.color = color;
    this.attribute = attribute;
    this.amount = amount;
    this.operation = operation;
    this.ownerAction = owner -> {};
  }

  ShoulderPassive(Identifier handId, int color, Consumer<ServerPlayer> ownerAction) {
    this.handId = handId;
    this.color = color;
    this.attribute = null;
    this.amount = 0.0D;
    this.operation = Operation.ADD_VALUE;
    this.ownerAction = ownerAction;
  }

  public static Optional<ShoulderPassive> forHand(Identifier handId) {
    return Arrays.stream(values())
        .filter(shoulderPassive -> shoulderPassive.handId.equals(handId))
        .findFirst();
  }

  public Identifier getEffectId() {
    return this.effectId;
  }

  public Identifier getModifierId() {
    return this.modifierId;
  }

  public Holder<MobEffect> getEffect() {
    return BuiltInRegistries.MOB_EFFECT.getOrThrow(this.effectKey);
  }

  public MobEffect createEffect() {
    MobEffect mobEffect = new MobEffect(MobEffectCategory.BENEFICIAL, this.color) {};
    if (this.attribute != null) {
      mobEffect.addAttributeModifier(this.attribute, this.modifierId, this.amount, this.operation);
    }
    return mobEffect;
  }

  // Removing Hunger in a MobEffect tick would modify activeEffects while vanilla iterates it.
  void apply(ServerPlayer owner) {
    this.ownerAction.accept(owner);
    Holder<MobEffect> effect = this.getEffect();
    if (!owner.hasEffect(effect)) {
      owner.addEffect(
          new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
    }
  }

  void remove(ServerPlayer owner) {
    owner.removeEffect(this.getEffect());
  }
}
