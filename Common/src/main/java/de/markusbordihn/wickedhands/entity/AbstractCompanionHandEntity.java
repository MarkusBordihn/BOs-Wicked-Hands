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

import de.markusbordihn.easynpc.api.animation.ModelAnimationAPI;
import de.markusbordihn.easynpc.api.handler.EasyNPCAttributeHandler;
import de.markusbordihn.easynpc.api.handler.EasyNPCObjectiveHandler;
import de.markusbordihn.easynpc.data.attribute.EnvironmentalAttributeType;
import de.markusbordihn.easynpc.data.attribute.InteractionAttributeType;
import de.markusbordihn.easynpc.data.objective.ObjectiveDataEntry;
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.easynpc.entity.easynpc.npc.easymodelentities.EasyModelNPC;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.advancement.HandAdvancements;
import de.markusbordihn.wickedhands.client.effect.AwakeningEffects;
import de.markusbordihn.wickedhands.data.CompanionMode;
import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.data.HandState;
import de.markusbordihn.wickedhands.item.LifelessHandItem;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractCompanionHandEntity extends AbstractHandEntity {

  public static final TagKey<EntityType<?>> COMPANION_IGNORED_TARGETS =
      TagKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(Constants.MOD_ID, "companion_ignored_targets"));
  public static final float FEEDING_HEAL_AMOUNT = 2.0F;
  public static final int REGENERATION_INTERVAL_TICKS = 100;
  public static final int REGENERATION_COMBAT_COOLDOWN_TICKS = 200;
  protected static final int COMPANION_CHECK_INTERVAL_TICKS = 20;
  private static final float RETREAT_HEALTH_RATIO = 0.35F;
  private static final double HIT_PARTICLE_SPREAD = 0.3D;
  private static final float RECOVERED_HEALTH_RATIO = 0.5F;
  private static final float REGENERATION_AMOUNT = 1.0F;
  private static final double MAX_HEALTH = 6.0D;
  private static final double MOVEMENT_SPEED = 0.25D;
  private static final int HIT_EFFECT_COOLDOWN_TICKS = 100;
  private static final int STRANGER_WAVE_COOLDOWN_TICKS = 1200;
  private static final double STRANGER_WAVE_RANGE = 6.0D;
  private static final double FOLLOW_SPEED_MODIFIER = 1.2D;
  private static final byte AWAKENED_EVENT = 100;
  private static final String HAND_STATE_TAG = "WickedHandState";
  private static final String REMAINS_RETURNED_MESSAGE_KEY =
      MESSAGE_KEY_PREFIX + "remains_returned";
  private static final String REMAINS_DROPPED_MESSAGE_KEY = MESSAGE_KEY_PREFIX + "remains_dropped";

  private final AwakeningEffects awakeningEffects = new AwakeningEffects(this);
  private final Identifier lifelessHandId;
  private final TagKey<Item> catalystItems;
  private HandState handState = HandState.EMPTY;
  private HandGesture activeGesture;
  private Entity gestureLookTarget;
  private long gestureEndGameTime;
  private long nextHitEffectGameTime;
  private long nextStrangerWaveGameTime;
  private long lastHurtGameTime;
  private boolean retreating;

  protected AbstractCompanionHandEntity(
      EntityType<? extends PathfinderMob> entityType, Level level, Identifier lifelessHandId) {
    super(entityType, level);
    this.lifelessHandId = lifelessHandId;
    this.catalystItems = catalystItems(EntityType.getKey(entityType));
  }

  protected static AttributeSupplier.Builder createHandAttributes(double attackDamage) {
    return EasyModelNPC.createAttributes()
        .add(Attributes.MAX_HEALTH, MAX_HEALTH)
        .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
        .add(Attributes.ATTACK_DAMAGE, attackDamage);
  }

  public HandState getHandState() {
    return this.handState;
  }

  public HandGesture getActiveGesture() {
    return this.activeGesture;
  }

  public boolean isRetreating() {
    return this.retreating;
  }

  @Override
  public InteractionResult mobInteract(Player player, InteractionHand hand) {
    if (!this.isNPCOwnedBy(player)) {
      return super.mobInteract(player, hand);
    }

    ItemStack itemStack = player.getItemInHand(hand);
    if (player.isSecondaryUseActive() && itemStack.isEmpty()) {
      if (!this.level().isClientSide()) {
        this.toggleCompanionMode(player);
      }
      return InteractionResult.SUCCESS;
    }
    if (itemStack.is(this.catalystItems) && this.getHealth() < this.getMaxHealth()) {
      if (this.level() instanceof ServerLevel serverLevel) {
        this.feed(serverLevel, player, itemStack);
      }
      return InteractionResult.SUCCESS;
    }

    return super.mobInteract(player, hand);
  }

  private void feed(ServerLevel serverLevel, Player player, ItemStack itemStack) {
    if (!this.isCompanion()) {
      return;
    }

    this.heal(FEEDING_HEAL_AMOUNT);
    itemStack.consume(1, player);
    this.playSound(SoundEvents.GENERIC_EAT.value(), 0.8F, 1.2F);
    serverLevel.sendParticles(
        ParticleTypes.HEART,
        this.getX(),
        this.getY(0.8D),
        this.getZ(),
        3,
        0.3D,
        0.2D,
        0.3D,
        0.0D);
  }

  void awakenAsCompanion(ServerLevel serverLevel) {
    this.handState = HandState.EMPTY.withLifecycle(HandLifecycle.COMPANION);
    this.applyCompanionObjectives();
    serverLevel.broadcastEntityEvent(this, AWAKENED_EVENT);
    this.playGesture(HandGesture.REVIVE, null);
    if (this.getOwner() instanceof ServerPlayer owner) {
      HandAdvancements.awardCompanion(owner, EntityType.getKey(this.getType()));
    }
  }

  @Override
  public void handleEntityEvent(byte eventId) {
    if (eventId == AWAKENED_EVENT) {
      this.awakeningEffects.start();
      return;
    }

    super.handleEntityEvent(eventId);
  }

  @Override
  public void baseTick() {
    super.baseTick();
    if (this.level().isClientSide()) {
      this.awakeningEffects.tick();
    }
  }

  @Override
  public void onClientRemoval() {
    super.onClientRemoval();
    this.awakeningEffects.stop();
  }

  @Override
  protected void customServerAiStep(ServerLevel serverLevel) {
    super.customServerAiStep(serverLevel);
    if (!this.isCompanion()) {
      return;
    }

    if (this.activeGesture != null) {
      this.tickGesture(serverLevel);
    } else if (this.tickCount % COMPANION_CHECK_INTERVAL_TICKS == 0) {
      this.waveAtNearbyStranger(serverLevel);
    }
    if (this.tickCount % COMPANION_CHECK_INTERVAL_TICKS == 0) {
      this.updateRetreat();
    }
    if (this.tickCount % REGENERATION_INTERVAL_TICKS == 0) {
      this.regenerateOutOfCombat(serverLevel);
    }
    this.companionTick(serverLevel);
  }

  private void updateRetreat() {
    float healthRatio = this.getHealth() / this.getMaxHealth();
    if (this.retreating) {
      this.retreating = healthRatio < RECOVERED_HEALTH_RATIO;
      return;
    }

    if (healthRatio < RETREAT_HEALTH_RATIO) {
      this.retreating = true;
      this.setTarget(null);
      this.getNavigation().stop();
    }
  }

  private void regenerateOutOfCombat(ServerLevel serverLevel) {
    if (this.getTarget() == null
        && this.getHealth() < this.getMaxHealth()
        && serverLevel.getGameTime() - this.lastHurtGameTime
            >= REGENERATION_COMBAT_COOLDOWN_TICKS) {
      this.heal(REGENERATION_AMOUNT);
    }
  }

  protected void companionTick(ServerLevel serverLevel) {}

  protected void applyHitEffect(ServerLevel serverLevel, LivingEntity target) {}

  protected static void spawnHitParticles(
      ServerLevel serverLevel,
      LivingEntity target,
      ParticleOptions particle,
      int count,
      double speed) {
    serverLevel.sendParticles(
        particle,
        target.getX(),
        target.getY(0.5D),
        target.getZ(),
        count,
        HIT_PARTICLE_SPREAD,
        HIT_PARTICLE_SPREAD,
        HIT_PARTICLE_SPREAD,
        speed);
  }

  protected void applyMovementAttributes() {
    EasyNPCAttributeHandler.setEnvironmentalAttribute(
        this, EnvironmentalAttributeType.CAN_FLOAT, true);
  }

  protected void playGesture(HandGesture gesture, Entity lookTarget) {
    this.activeGesture = gesture;
    this.gestureLookTarget = lookTarget;
    this.gestureEndGameTime = this.level().getGameTime() + gesture.getDurationTicks();
    this.getNavigation().stop();
    ModelAnimationAPI.playAnimation(this, gesture.getAnimationName());
  }

  private void tickGesture(ServerLevel serverLevel) {
    if (this.getTarget() != null) {
      this.clearGesture();
      ModelAnimationAPI.stopAnimation(this);
      return;
    }

    if (serverLevel.getGameTime() < this.gestureEndGameTime) {
      this.holdStillFacing(this.gestureLookTarget);
      return;
    }

    HandGesture finishedGesture = this.activeGesture;
    this.clearGesture();
    if (finishedGesture == HandGesture.REVIVE && this.getOwner() instanceof LivingEntity owner) {
      this.playGesture(HandGesture.WAVE, owner);
    }
  }

  private void clearGesture() {
    this.activeGesture = null;
    this.gestureLookTarget = null;
  }

  private void holdStillFacing(Entity lookTarget) {
    this.getNavigation().stop();
    this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
    if (lookTarget != null && lookTarget.isAlive()) {
      this.getLookControl().setLookAt(lookTarget);
      this.setYBodyRot(this.getYHeadRot());
    }
  }

  private void waveAtNearbyStranger(ServerLevel serverLevel) {
    if (this.getTarget() != null || serverLevel.getGameTime() < this.nextStrangerWaveGameTime) {
      return;
    }

    Player stranger =
        serverLevel.getNearestPlayer(
            this.getX(),
            this.getY(),
            this.getZ(),
            STRANGER_WAVE_RANGE,
            EntitySelector.NO_SPECTATORS.and(
                candidate -> candidate instanceof Player player && !this.isNPCOwnedBy(player)));
    if (stranger == null) {
      return;
    }

    this.nextStrangerWaveGameTime = serverLevel.getGameTime() + STRANGER_WAVE_COOLDOWN_TICKS;
    this.playGesture(HandGesture.WAVE, stranger);
  }

  private void toggleCompanionMode(Player player) {
    if (!this.isCompanion()) {
      return;
    }

    CompanionMode companionMode =
        switch (this.handState.companionMode()) {
          case FOLLOW -> CompanionMode.SHOULDER;
          case SHOULDER -> CompanionMode.STAY;
          case STAY -> CompanionMode.FOLLOW;
        };
    this.handState = this.handState.withCompanionMode(companionMode);
    this.applyCompanionMode(companionMode);
    player.displayClientMessage(
        Component.translatable(MESSAGE_KEY_PREFIX + companionMode.getSerializedName()), true);
  }

  @Override
  public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
    boolean hurtTarget = super.doHurtTarget(serverLevel, target);
    if (hurtTarget
        && this.isCompanion()
        && target instanceof LivingEntity livingTarget
        && serverLevel.getGameTime() >= this.nextHitEffectGameTime) {
      this.nextHitEffectGameTime = serverLevel.getGameTime() + HIT_EFFECT_COOLDOWN_TICKS;
      this.applyHitEffect(serverLevel, livingTarget);
    }
    return hurtTarget;
  }

  @Override
  public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float damage) {
    boolean hurt = super.hurtServer(serverLevel, damageSource, damage);
    if (hurt) {
      this.lastHurtGameTime = serverLevel.getGameTime();
    }
    return hurt;
  }

  @Override
  public boolean canAttack(LivingEntity target) {
    return super.canAttack(target)
        && !(this.isCompanion() && (this.retreating || this.ignoresAsCompanion(target)));
  }

  private boolean ignoresAsCompanion(LivingEntity target) {
    if (target.getType().is(COMPANION_IGNORED_TARGETS)) {
      return true;
    }

    return target instanceof OwnableEntity ownableEntity
        && ownableEntity.getOwnerReference()
            instanceof EntityReference<LivingEntity> ownerReference
        && !ownerReference.getUUID().equals(this.getOwnerUUID());
  }

  @Override
  public void die(DamageSource damageSource) {
    boolean wasDead = this.dead;
    super.die(damageSource);
    if (!wasDead
        && this.dead
        && this.isCompanion()
        && this.level() instanceof ServerLevel serverLevel) {
      this.returnRemainsToOwner(serverLevel);
    }
  }

  private void returnRemainsToOwner(ServerLevel serverLevel) {
    UUID ownerUUID = this.getOwnerUUID();
    ItemStack remains =
        LifelessHandItem.createCompanionRemains(
            this.lifelessHandId,
            this.getUUID(),
            Optional.ofNullable(ownerUUID),
            this.getCustomName());
    ServerPlayer owner =
        ownerUUID == null ? null : serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
    if (owner != null && owner.isAlive()) {
      if (!owner.getInventory().add(remains)) {
        LifelessHandItem.protectRemains(owner.drop(remains, false), ownerUUID);
      }
      owner.sendSystemMessage(Component.translatable(REMAINS_RETURNED_MESSAGE_KEY));
      return;
    }

    LifelessHandItem.protectRemains(this.spawnAtLocation(serverLevel, remains), ownerUUID);
    if (owner != null) {
      BlockPos remainsPosition = this.blockPosition();
      owner.sendSystemMessage(
          Component.translatable(
              REMAINS_DROPPED_MESSAGE_KEY,
              remainsPosition.getX(),
              remainsPosition.getY(),
              remainsPosition.getZ()));
    }
  }

  @Override
  protected void dropFromLootTable(
      ServerLevel serverLevel, DamageSource damageSource, boolean causedByPlayer) {
    if (!this.isCompanion()) {
      super.dropFromLootTable(serverLevel, damageSource, causedByPlayer);
    }
  }

  protected boolean isCompanion() {
    return this.handState.lifecycle() == HandLifecycle.COMPANION;
  }

  private void applyMovementAndMeleeAttack() {
    this.applyMovementAttributes();
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.MELEE_ATTACK);
  }

  private void applyCompanionObjectives() {
    this.applyMovementAndMeleeAttack();
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.OWNER_HURT_BY_TARGET);
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.OWNER_HURT_TARGET);
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.LOOK_AT_OWNER);
    this.applyCompanionMode(this.handState.companionMode());
  }

  private void applyCompanionMode(CompanionMode companionMode) {
    EasyNPCAttributeHandler.setInteractionAttribute(
        this,
        InteractionAttributeType.CAN_SIT_ON_SHOULDER,
        companionMode == CompanionMode.SHOULDER);
    if (companionMode == CompanionMode.STAY) {
      EasyNPCObjectiveHandler.removeObjective(this, ObjectiveType.FOLLOW_OWNER);
      this.setNPCHomePosition(this.blockPosition());
      EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.MOVE_BACK_TO_HOME);
    } else {
      EasyNPCObjectiveHandler.removeObjective(this, ObjectiveType.MOVE_BACK_TO_HOME);
      EasyNPCObjectiveHandler.addObjective(
          this,
          new ObjectiveDataEntry(ObjectiveType.FOLLOW_OWNER)
              .setSpeedModifier(FOLLOW_SPEED_MODIFIER));
    }
  }

  @Override
  public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor serverLevelAccessor,
      DifficultyInstance difficulty,
      EntitySpawnReason entitySpawnReason,
      SpawnGroupData spawnGroupData) {
    SpawnGroupData result =
        super.finalizeSpawn(serverLevelAccessor, difficulty, entitySpawnReason, spawnGroupData);
    this.applyMovementAndMeleeAttack();
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.ATTACK_PLAYER);
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.ATTACK_ANIMAL);
    EasyNPCObjectiveHandler.addObjective(this, ObjectiveType.RANDOM_STROLL);
    return result;
  }

  @Override
  public void addAdditionalSaveData(ValueOutput valueOutput) {
    super.addAdditionalSaveData(valueOutput);
    valueOutput.store(HAND_STATE_TAG, HandState.CODEC, this.handState);
  }

  @Override
  public void readAdditionalSaveData(ValueInput valueInput) {
    super.readAdditionalSaveData(valueInput);
    this.handState = valueInput.read(HAND_STATE_TAG, HandState.CODEC).orElse(HandState.EMPTY);
  }

  @Override
  public boolean removeWhenFarAway(double distanceToClosestPlayer) {
    return true;
  }

  @Override
  public boolean requiresCustomPersistence() {
    return super.requiresCustomPersistence() || this.handState.lifecycle() != HandLifecycle.WILD;
  }
}
