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

import de.markusbordihn.easynpc.entity.easynpc.npc.easymodelentities.EasyModelNPC;
import de.markusbordihn.easynpc.handler.OwnerHandler;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.client.effect.RevivalRitualEffects;
import de.markusbordihn.wickedhands.data.RevivalState;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class LifelessRottenHandEntity extends AbstractRottenHandEntity {

  public static final String ID = "lifeless_rotten_hand";
  public static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, ID);
  public static final TagKey<Item> CATALYST_ITEMS =
      TagKey.create(
          Registries.ITEM,
          Identifier.fromNamespaceAndPath(Constants.MOD_ID, RottenHandEntity.ID + "_catalysts"));

  private static final String MODEL_ID = Constants.MOD_ID + ":entity/" + ID;
  private static final double MAX_HEALTH = 6.0D;
  private static final byte REVIVAL_STARTED_EVENT = 100;
  private static final byte REVIVAL_COMPLETED_EVENT = 101;
  private static final double RITUAL_DARKNESS_RADIUS = 16.0D;
  private static final int RITUAL_DARKNESS_FADE_TICKS = 20;
  private static final String REVIVAL_STATE_TAG = "WickedRevivalState";
  private static final String RITUAL_INCOMPLETE_TRANSLATION_KEY =
      "entity." + Constants.MOD_ID + "." + ID + ".ritual_incomplete";

  private final RevivalRitualEffects revivalEffects = new RevivalRitualEffects(this);
  private RevivalState revivalState = RevivalState.EMPTY;

  public LifelessRottenHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
    this.setNoAi(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return EasyModelNPC.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH);
  }

  public static EntityType<LifelessRottenHandEntity> createEntityType() {
    return EntityType.Builder.of(LifelessRottenHandEntity::new, MobCategory.MISC)
        .sized(0.75F, 0.375F)
        .clientTrackingRange(8)
        .build(ResourceKey.create(Registries.ENTITY_TYPE, ENTITY_ID));
  }

  public static boolean place(ServerLevel serverLevel, Vec3 position, float yaw, Player player) {
    if (!(BuiltInRegistries.ENTITY_TYPE
            .getValue(ENTITY_ID)
            .create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE)
        instanceof LifelessRottenHandEntity lifelessRottenHand)) {
      return false;
    }

    lifelessRottenHand.snapTo(position.x(), position.y(), position.z(), yaw, 0.0F);
    lifelessRottenHand.setYHeadRot(yaw);
    lifelessRottenHand.setYBodyRot(yaw);
    serverLevel.addFreshEntity(lifelessRottenHand);
    serverLevel.playSound(
        null, lifelessRottenHand.blockPosition(), SoundEvents.SOUL_SAND_PLACE, SoundSource.BLOCKS);
    lifelessRottenHand.gameEvent(GameEvent.ENTITY_PLACE, player);
    return true;
  }

  public RevivalState getRevivalState() {
    return this.revivalState;
  }

  @Override
  protected String getModelId() {
    return MODEL_ID;
  }

  @Override
  public InteractionResult mobInteract(Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);
    if (itemStack.is(CATALYST_ITEMS)) {
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

    BlockPos ritualBasePosition = this.getOnPos();
    if (!RevivalRitual.isRitualBase(serverLevel.getBlockState(ritualBasePosition))
        || !RevivalRitual.hasEnoughLitCandles(serverLevel, ritualBasePosition)) {
      player.sendOverlayMessage(Component.translatable(RITUAL_INCOMPLETE_TRANSLATION_KEY));
      return;
    }

    catalyst.consume(1, player);
    this.revivalState = RevivalState.EMPTY.withOwnerUUID(player.getUUID());
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
    this.awakenCompanion(serverLevel, ownerUUID);
    this.discard();
  }

  private void strikeVisualLightning(ServerLevel serverLevel) {
    LightningBolt lightningBolt =
        EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
    if (lightningBolt == null) {
      return;
    }

    lightningBolt.snapTo(this.position());
    lightningBolt.setVisualOnly(true);
    serverLevel.addFreshEntity(lightningBolt);
  }

  private void awakenCompanion(ServerLevel serverLevel, UUID ownerUUID) {
    if (!(BuiltInRegistries.ENTITY_TYPE
            .getValue(RottenHandEntity.ENTITY_ID)
            .create(serverLevel, EntitySpawnReason.CONVERSION)
        instanceof RottenHandEntity companion)) {
      return;
    }

    companion.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
    companion.setYHeadRot(this.getYHeadRot());
    companion.setYBodyRot(this.yBodyRot);
    serverLevel.addFreshEntity(companion);
    Player owner = serverLevel.getPlayerByUUID(ownerUUID);
    if (owner == null || !OwnerHandler.setOwner(companion, owner)) {
      companion.setNPCOwnerUUID(ownerUUID);
    }
    companion.awakenAsCompanion(serverLevel);
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
