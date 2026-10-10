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

import de.markusbordihn.easynpc.data.attribute.EnvironmentalAttributeType;
import de.markusbordihn.easynpc.data.objective.ObjectiveDataEntry;
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.easynpc.entity.easynpc.npc.easymodelentities.EasyModelNPC;
import de.markusbordihn.easynpc.handler.AttributeHandler;
import de.markusbordihn.easynpc.handler.ObjectiveHandler;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.advancement.HandAdvancements;
import de.markusbordihn.wickedhands.client.effect.AwakeningEffects;
import de.markusbordihn.wickedhands.data.CompanionMode;
import de.markusbordihn.wickedhands.data.HandLifecycle;
import de.markusbordihn.wickedhands.data.HandState;
import de.markusbordihn.wickedhands.item.LifelessRottenHandItem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class RottenHandEntity extends AbstractRottenHandEntity {

  public static final String ID = "rotten_hand";
  public static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, ID);
  public static final int SPAWN_WEIGHT = 5;
  public static final int SPAWN_GROUP_SIZE = 1;
  public static final SpawnPlacementType SPAWN_PLACEMENT_TYPE = SpawnPlacementTypes.ON_GROUND;
  public static final Heightmap.Types SPAWN_HEIGHTMAP = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
  private static final String MODEL_ID = Constants.MOD_ID + ":entity/" + ID;
  private static final double MAX_HEALTH = 6.0D;
  private static final double MOVEMENT_SPEED = 0.25D;
  private static final double ATTACK_DAMAGE = 2.0D;
  private static final double FOLLOW_SPEED_MODIFIER = 1.2D;
  private static final byte AWAKENED_EVENT = 100;
  private static final String HAND_STATE_TAG = "WickedHandState";
  private static final String TRANSLATION_KEY_PREFIX = "entity." + Constants.MOD_ID + "." + ID;
  private static final String FOLLOW_TRANSLATION_KEY = TRANSLATION_KEY_PREFIX + ".follow";
  private static final String STAY_TRANSLATION_KEY = TRANSLATION_KEY_PREFIX + ".stay";
  private static final String REMAINS_RETURNED_TRANSLATION_KEY =
      TRANSLATION_KEY_PREFIX + ".remains_returned";
  private static final String REMAINS_DROPPED_TRANSLATION_KEY =
      TRANSLATION_KEY_PREFIX + ".remains_dropped";
  private static final int GRAB_COOLDOWN_TICKS = 100;
  private static final int GRAB_DURATION_TICKS = 30;
  private static final int GRAB_SLOWNESS_AMPLIFIER = 3;

  private final AwakeningEffects awakeningEffects = new AwakeningEffects(this);
  private HandState handState = HandState.EMPTY;
  private long nextGrabGameTime;

  public RottenHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return EasyModelNPC.createAttributes()
        .add(Attributes.MAX_HEALTH, MAX_HEALTH)
        .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
        .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
  }

  public static EntityType<RottenHandEntity> createEntityType() {
    return EntityType.Builder.of(RottenHandEntity::new, MobCategory.MONSTER)
        .sized(0.75F, 0.375F)
        .clientTrackingRange(8)
        .build(ResourceKey.create(Registries.ENTITY_TYPE, ENTITY_ID));
  }

  public static boolean checkRottenHandSpawnRules(
      EntityType<RottenHandEntity> entityType,
      ServerLevelAccessor level,
      EntitySpawnReason spawnReason,
      BlockPos blockPos,
      RandomSource random) {
    return level.getDifficulty() != Difficulty.PEACEFUL
        && level.getLevel().isDarkOutside()
        && Monster.isDarkEnoughToSpawn(level, blockPos, random)
        && Mob.checkMobSpawnRules(entityType, level, spawnReason, blockPos, random);
  }

  public HandState getHandState() {
    return this.handState;
  }

  @Override
  protected String getModelId() {
    return MODEL_ID;
  }

  @Override
  public InteractionResult mobInteract(Player player, InteractionHand hand) {
    if (player.isSecondaryUseActive() && this.isNPCOwnedBy(player)) {
      if (!this.level().isClientSide()) {
        this.toggleCompanionMode(player);
      }
      return InteractionResult.SUCCESS;
    }

    return super.mobInteract(player, hand);
  }

  void awakenAsCompanion(ServerLevel serverLevel) {
    this.handState = HandState.EMPTY.withLifecycle(HandLifecycle.COMPANION);
    this.applyCompanionObjectives();
    serverLevel.broadcastEntityEvent(this, AWAKENED_EVENT);
    if (this.getOwner() instanceof ServerPlayer owner) {
      HandAdvancements.awardRottenHandCompanion(owner);
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

  private void toggleCompanionMode(Player player) {
    if (!this.isCompanion()) {
      return;
    }

    CompanionMode companionMode =
        this.handState.companionMode() == CompanionMode.FOLLOW
            ? CompanionMode.STAY
            : CompanionMode.FOLLOW;
    this.handState = this.handState.withCompanionMode(companionMode);
    this.applyCompanionMode(companionMode);
    player.sendOverlayMessage(
        Component.translatable(
            companionMode == CompanionMode.STAY ? STAY_TRANSLATION_KEY : FOLLOW_TRANSLATION_KEY));
  }

  @Override
  public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
    boolean hurtTarget = super.doHurtTarget(serverLevel, target);
    if (hurtTarget
        && this.isCompanion()
        && serverLevel.getGameTime() >= this.nextGrabGameTime
        && target instanceof LivingEntity livingTarget) {
      this.grab(serverLevel, livingTarget);
    }
    return hurtTarget;
  }

  private void grab(ServerLevel serverLevel, LivingEntity target) {
    this.nextGrabGameTime = serverLevel.getGameTime() + GRAB_COOLDOWN_TICKS;
    target.addEffect(
        new MobEffectInstance(MobEffects.SLOWNESS, GRAB_DURATION_TICKS, GRAB_SLOWNESS_AMPLIFIER),
        this);
    target.setDeltaMovement(Vec3.ZERO);
    target.hurtMarked = true;
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
    ItemStack remains =
        LifelessRottenHandItem.createCompanionRemains(this.getUUID(), this.getCustomName());
    UUID ownerUUID = this.getOwnerUUID();
    ServerPlayer owner =
        ownerUUID == null ? null : serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
    if (owner != null && owner.isAlive()) {
      if (!owner.getInventory().add(remains)) {
        owner.drop(remains, false);
      }
      owner.sendSystemMessage(Component.translatable(REMAINS_RETURNED_TRANSLATION_KEY));
      return;
    }

    ItemEntity droppedRemains = this.spawnAtLocation(serverLevel, remains);
    if (droppedRemains != null) {
      droppedRemains.setInvulnerable(true);
      droppedRemains.setUnlimitedLifetime();
    }
    if (owner != null) {
      BlockPos remainsPosition = this.blockPosition();
      owner.sendSystemMessage(
          Component.translatable(
              REMAINS_DROPPED_TRANSLATION_KEY,
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

  private boolean isCompanion() {
    return this.handState.lifecycle() == HandLifecycle.COMPANION;
  }

  private void applySwimmingAndMeleeAttack() {
    AttributeHandler.setEnvironmentalAttribute(this, EnvironmentalAttributeType.CAN_FLOAT, true);
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.MELEE_ATTACK));
  }

  private void applyCompanionObjectives() {
    this.applySwimmingAndMeleeAttack();
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.OWNER_HURT_BY_TARGET));
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.LOOK_AT_OWNER));
    this.applyCompanionMode(this.handState.companionMode());
  }

  private void applyCompanionMode(CompanionMode companionMode) {
    if (companionMode == CompanionMode.STAY) {
      this.removeObjectiveIfPresent(ObjectiveType.FOLLOW_OWNER);
      this.setNPCHomePosition(this.blockPosition());
      ObjectiveHandler.addOrUpdateCustomObjective(
          this, new ObjectiveDataEntry(ObjectiveType.MOVE_BACK_TO_HOME));
    } else {
      this.removeObjectiveIfPresent(ObjectiveType.MOVE_BACK_TO_HOME);
      ObjectiveHandler.addOrUpdateCustomObjective(
          this,
          new ObjectiveDataEntry(ObjectiveType.FOLLOW_OWNER)
              .setSpeedModifier(FOLLOW_SPEED_MODIFIER));
    }
  }

  private void removeObjectiveIfPresent(ObjectiveType objectiveType) {
    if (this.hasObjective(objectiveType)) {
      this.removeCustomObjective(objectiveType);
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
    this.applySwimmingAndMeleeAttack();
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.ATTACK_PLAYER));
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.ATTACK_ANIMAL));
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.RANDOM_STROLL));
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
