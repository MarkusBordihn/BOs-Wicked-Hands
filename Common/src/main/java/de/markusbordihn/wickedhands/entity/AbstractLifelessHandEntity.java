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

import de.markusbordihn.easynpc.api.handler.EasyNPCEntityHandler;
import de.markusbordihn.easynpc.data.model.RootModelData;
import de.markusbordihn.easynpc.data.rotation.CustomRotation;
import de.markusbordihn.easynpc.data.scale.CustomScale;
import de.markusbordihn.easynpc.data.synched.SynchedDataIndex;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import de.markusbordihn.easynpc.entity.easynpc.npc.easymodelentities.EasyModelNPC;
import de.markusbordihn.easynpc.handler.OwnerHandler;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.client.effect.RevivalRitualEffects;
import de.markusbordihn.wickedhands.data.RevivalState;
import de.markusbordihn.wickedhands.item.HandDataComponents;
import de.markusbordihn.wickedhands.item.LifelessHandItem;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractLifelessHandEntity extends AbstractHandEntity {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final double MAX_HEALTH = 6.0D;
  private static final byte REVIVAL_STARTED_EVENT = 100;
  private static final byte REVIVAL_COMPLETED_EVENT = 101;
  private static final double RITUAL_DARKNESS_RADIUS = 16.0D;
  private static final int RITUAL_DARKNESS_FADE_TICKS = 20;
  private static final String REVIVAL_STATE_TAG = "WickedRevivalState";
  private static final RootModelData LYING_ON_BACK =
      new RootModelData(new CustomRotation((float) Math.PI, 0.0F, 0.0F), CustomScale.DEFAULT);
  private static final String RITUAL_INCOMPLETE_MESSAGE_KEY =
      MESSAGE_KEY_PREFIX + "ritual_incomplete";
  private static final String RITUAL_WHISPER_MESSAGE_KEY = MESSAGE_KEY_PREFIX + "ritual_whisper";
  private static final String FOREIGN_REMAINS_MESSAGE_KEY = MESSAGE_KEY_PREFIX + "foreign_remains";

  private final RevivalRitualEffects revivalEffects;
  private final Set<UUID> whisperedPlayers = new HashSet<>();
  private final Identifier companionHandId;
  private final TagKey<Item> catalystItems;
  private RevivalState revivalState = RevivalState.EMPTY;

  protected AbstractLifelessHandEntity(
      EntityType<? extends PathfinderMob> entityType,
      Level level,
      Identifier companionHandId,
      ParticleOptions ritualParticle) {
    super(entityType, level);
    this.companionHandId = companionHandId;
    this.catalystItems = catalystItems(companionHandId);
    this.revivalEffects = new RevivalRitualEffects(this, ritualParticle);
    this.setNoAi(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return EasyModelNPC.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH);
  }

  public static boolean place(
      ServerLevel serverLevel,
      Identifier lifelessHandId,
      Vec3 position,
      float yaw,
      Player player,
      ItemStack itemStack) {
    if (!(BuiltInRegistries.ENTITY_TYPE
            .getValue(lifelessHandId)
            .create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE)
        instanceof AbstractLifelessHandEntity lifelessHand)) {
      return false;
    }

    lifelessHand.revivalState =
        RevivalState.EMPTY
            .withCompanionUUID(
                Optional.ofNullable(itemStack.get(HandDataComponents.COMPANION_UUID)))
            .withBoundOwnerUUID(Optional.ofNullable(itemStack.get(HandDataComponents.OWNER_UUID)));
    lifelessHand.setCustomName(itemStack.get(DataComponents.CUSTOM_NAME));
    lifelessHand.snapTo(position.x(), position.y(), position.z(), yaw, 0.0F);
    lifelessHand.setYHeadRot(yaw);
    lifelessHand.setYBodyRot(yaw);
    serverLevel.addFreshEntity(lifelessHand);
    serverLevel.playSound(
        null, lifelessHand.blockPosition(), SoundEvents.SOUL_SAND_PLACE, SoundSource.BLOCKS);
    lifelessHand.gameEvent(GameEvent.ENTITY_PLACE, player);
    return true;
  }

  public RevivalState getRevivalState() {
    return this.revivalState;
  }

  @Override
  public void defineSynchedModelRootData(SynchedEntityData.Builder builder) {
    this.defineSynchedEntityData(builder, SynchedDataIndex.MODEL_ROOT_DATA, LYING_ON_BACK);
  }

  @Override
  public InteractionResult mobInteract(Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);
    if (itemStack.is(this.catalystItems)) {
      if (this.level() instanceof ServerLevel serverLevel) {
        this.tryStartRevival(serverLevel, player, itemStack);
      }
      return InteractionResult.SUCCESS;
    }

    return super.mobInteract(player, hand);
  }

  private void tryStartRevival(ServerLevel serverLevel, Player player, ItemStack catalyst) {
    if (this.revivalState.ownerUUID().isPresent()) {
      return;
    }

    if (this.revivalState.isBoundToOtherThan(player.getUUID())) {
      player.sendOverlayMessage(Component.translatable(FOREIGN_REMAINS_MESSAGE_KEY));
      return;
    }

    BlockPos ritualBasePosition = this.getOnPos();
    if (!RevivalRitual.isRitualBase(serverLevel.getBlockState(ritualBasePosition))
        || !RevivalRitual.hasEnoughLitCandles(serverLevel, ritualBasePosition)) {
      player.sendOverlayMessage(Component.translatable(RITUAL_INCOMPLETE_MESSAGE_KEY));
      if (this.whisperedPlayers.add(player.getUUID())) {
        player.sendSystemMessage(
            createWhisper(this.getName(), Component.translatable(RITUAL_WHISPER_MESSAGE_KEY)));
      }
      return;
    }

    catalyst.consume(1, player);
    this.revivalState = this.revivalState.withOwnerUUID(Optional.of(player.getUUID()));
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, this);
    }
    serverLevel.broadcastEntityEvent(this, REVIVAL_STARTED_EVENT);
    this.darkenNearbyPlayers(serverLevel);
  }

  private void darkenNearbyPlayers(ServerLevel serverLevel) {
    for (ServerPlayer serverPlayer :
        serverLevel.getEntitiesOfClass(
            ServerPlayer.class, this.getBoundingBox().inflate(RITUAL_DARKNESS_RADIUS))) {
      serverPlayer.addEffect(
          new MobEffectInstance(
              MobEffects.DARKNESS,
              RevivalRitual.DURATION_TICKS + RITUAL_DARKNESS_FADE_TICKS,
              0,
              false,
              false),
          this);
    }
  }

  @Override
  public void handleEntityEvent(byte eventId) {
    if (eventId == REVIVAL_STARTED_EVENT) {
      this.revivalEffects.start();
      return;
    }

    if (eventId == REVIVAL_COMPLETED_EVENT) {
      this.revivalEffects.complete();
      return;
    }

    super.handleEntityEvent(eventId);
  }

  @Override
  public void baseTick() {
    super.baseTick();
    if (!(this.level() instanceof ServerLevel serverLevel)) {
      this.revivalEffects.tick();
      return;
    }

    if (this.isAlive()) {
      this.revivalState
          .ownerUUID()
          .ifPresent(ownerUUID -> this.tickRevival(serverLevel, ownerUUID));
    }
  }

  @Override
  public void onClientRemoval() {
    super.onClientRemoval();
    this.revivalEffects.stop();
  }

  private void tickRevival(ServerLevel serverLevel, UUID ownerUUID) {
    int elapsedTicks = this.revivalState.elapsedTicks() + 1;
    if (elapsedTicks < RevivalRitual.DURATION_TICKS) {
      this.revivalState = this.revivalState.withElapsedTicks(elapsedTicks);
      return;
    }

    RevivalRitual.extinguishCandles(serverLevel, this.getOnPos());
    serverLevel.broadcastEntityEvent(this, REVIVAL_COMPLETED_EVENT);
    this.strikeVisualLightning(serverLevel);
    if (!this.awakenCompanion(serverLevel, ownerUUID)) {
      log.warn("Unable to awaken companion of {}, the ritual can be repeated", this.getUUID());
      this.revivalState = this.revivalState.withOwnerUUID(Optional.empty()).withElapsedTicks(0);
      return;
    }

    EasyNPCEntityHandler.delete(this);
  }

  private void strikeVisualLightning(ServerLevel serverLevel) {
    LightningBolt lightningBolt =
        EntityTypes.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
    if (lightningBolt == null) {
      return;
    }

    lightningBolt.snapTo(this.position());
    lightningBolt.setVisualOnly(true);
    serverLevel.addFreshEntity(lightningBolt);
  }

  private boolean awakenCompanion(ServerLevel serverLevel, UUID ownerUUID) {
    Optional<AbstractCompanionHandEntity> awakenedCompanion =
        this.restoreKilledOrCreateCompanion(serverLevel);
    if (awakenedCompanion.isEmpty()) {
      return false;
    }

    AbstractCompanionHandEntity companion = awakenedCompanion.get();
    this.moveToThisHand(companion);
    Player owner = serverLevel.getPlayerByUUID(ownerUUID);
    if (owner == null || !OwnerHandler.setOwner(companion, owner)) {
      companion.setNPCOwnerUUID(ownerUUID);
    }
    companion.awakenAsCompanion(serverLevel);
    return true;
  }

  private Optional<AbstractCompanionHandEntity> restoreKilledOrCreateCompanion(
      ServerLevel serverLevel) {
    Optional<UUID> companionUUID = this.revivalState.companionUUID();
    if (companionUUID.isEmpty()
        || !EasyNPCEntityHandler.revive(companionUUID.get(), serverLevel, this.position())) {
      return this.createCompanion(serverLevel);
    }

    return EasyNPCEntityHandler.find(companionUUID.get(), serverLevel)
        .map(EasyNPC::getEntity)
        .filter(AbstractCompanionHandEntity.class::isInstance)
        .map(AbstractCompanionHandEntity.class::cast);
  }

  private Optional<AbstractCompanionHandEntity> createCompanion(ServerLevel serverLevel) {
    if (!(BuiltInRegistries.ENTITY_TYPE
            .getValue(this.companionHandId)
            .create(serverLevel, EntitySpawnReason.CONVERSION)
        instanceof AbstractCompanionHandEntity companion)) {
      return Optional.empty();
    }

    this.moveToThisHand(companion);
    companion.setCustomName(this.getCustomName());
    serverLevel.addFreshEntity(companion);
    return Optional.of(companion);
  }

  private void moveToThisHand(AbstractCompanionHandEntity companion) {
    companion.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
    companion.setYHeadRot(this.getYHeadRot());
    companion.setYBodyRot(this.yBodyRot);
  }

  @Override
  protected void dropFromLootTable(
      ServerLevel serverLevel, DamageSource damageSource, boolean causedByPlayer) {
    if (this.revivalState.companionUUID().isEmpty()) {
      super.dropFromLootTable(serverLevel, damageSource, causedByPlayer);
      return;
    }

    ItemEntity droppedRemains =
        this.spawnAtLocation(
            serverLevel,
            LifelessHandItem.createCompanionRemains(
                EntityType.getKey(this.getType()),
                this.revivalState.companionUUID().get(),
                this.revivalState.boundOwnerUUID(),
                this.getCustomName()));
    this.revivalState
        .boundOwnerUUID()
        .ifPresent(ownerUUID -> LifelessHandItem.protectRemains(droppedRemains, ownerUUID));
  }

  @Override
  public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
    return super.isInvulnerableTo(serverLevel, damageSource)
        || (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
            && this.isProtectedFrom(damageSource));
  }

  private boolean isProtectedFrom(DamageSource damageSource) {
    if (this.revivalState.ownerUUID().isPresent()) {
      return true;
    }

    if (this.revivalState.companionUUID().isEmpty()) {
      return false;
    }

    return !(damageSource.getEntity() instanceof Player player)
        || (!player.isCreative() && this.revivalState.isBoundToOtherThan(player.getUUID()));
  }

  @Override
  public void addAdditionalSaveData(ValueOutput valueOutput) {
    super.addAdditionalSaveData(valueOutput);
    valueOutput.store(REVIVAL_STATE_TAG, RevivalState.CODEC, this.revivalState);
  }

  @Override
  public void readAdditionalSaveData(ValueInput valueInput) {
    super.readAdditionalSaveData(valueInput);
    this.revivalState =
        valueInput.read(REVIVAL_STATE_TAG, RevivalState.CODEC).orElse(RevivalState.EMPTY);
  }

  @Override
  public boolean removeWhenFarAway(double distanceToClosestPlayer) {
    return false;
  }

  @Override
  public boolean requiresCustomPersistence() {
    return true;
  }
}
