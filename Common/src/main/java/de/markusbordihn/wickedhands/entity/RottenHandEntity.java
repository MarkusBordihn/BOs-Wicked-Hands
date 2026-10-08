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

import de.markusbordihn.easynpc.data.attribute.CombatAttributes;
import de.markusbordihn.easynpc.data.attribute.EntityAttributes;
import de.markusbordihn.easynpc.data.display.DisplayAttributeDataSet;
import de.markusbordihn.easynpc.data.display.DisplayAttributeEntry;
import de.markusbordihn.easynpc.data.display.DisplayAttributeType;
import de.markusbordihn.easynpc.data.display.NameVisibilityType;
import de.markusbordihn.easynpc.data.objective.ObjectiveDataEntry;
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.easynpc.data.render.RenderDataEntry;
import de.markusbordihn.easynpc.data.render.RenderType;
import de.markusbordihn.easynpc.data.synched.SynchedDataIndex;
import de.markusbordihn.easynpc.entity.easynpc.npc.easymodelentities.EasyModelNPC;
import de.markusbordihn.easynpc.handler.ObjectiveHandler;
import de.markusbordihn.wickedhands.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;

public class RottenHandEntity extends EasyModelNPC {

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
  private static final int MELEE_ATTACK_PRIORITY = 1;
  private static final int PLAYER_TARGET_PRIORITY = 1;
  private static final int ANIMAL_TARGET_PRIORITY = 2;
  private static final int RANDOM_STROLL_PRIORITY = 5;

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

  @Override
  public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor serverLevelAccessor,
      DifficultyInstance difficulty,
      EntitySpawnReason entitySpawnReason,
      SpawnGroupData spawnGroupData) {
    SpawnGroupData result =
        super.finalizeSpawn(serverLevelAccessor, difficulty, entitySpawnReason, spawnGroupData);
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.MELEE_ATTACK, MELEE_ATTACK_PRIORITY));
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.ATTACK_PLAYER, PLAYER_TARGET_PRIORITY));
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.ATTACK_ANIMAL, ANIMAL_TARGET_PRIORITY));
    ObjectiveHandler.addOrUpdateCustomObjective(
        this, new ObjectiveDataEntry(ObjectiveType.RANDOM_STROLL, RANDOM_STROLL_PRIORITY));
    return result;
  }

  @Override
  public void defineSynchedRenderData(SynchedEntityData.Builder builder) {
    this.defineSynchedEntityData(
        builder,
        SynchedDataIndex.RENDER_DATA,
        new RenderDataEntry(RenderType.EASY_MODEL_ENTITY, null, MODEL_ID));
  }

  @Override
  public void defineSynchedDisplayAttributeData(SynchedEntityData.Builder builder) {
    this.defineSynchedEntityData(
        builder,
        SynchedDataIndex.DISPLAY_ATTRIBUTE_SET,
        DisplayAttributeDataSet.createDefault()
            .withAttribute(
                DisplayAttributeType.NAME_VISIBILITY,
                new DisplayAttributeEntry(NameVisibilityType.NEVER.toString())));
  }

  @Override
  public void defineSynchedAttributeData(SynchedEntityData.Builder builder) {
    EntityAttributes entityAttributes = new EntityAttributes();
    entityAttributes.setCombatAttributes(
        new CombatAttributes()
            .withIsInvulnerable(false)
            .withIsAttackableByPlayers(true)
            .withIsAttackableByMonsters(true));
    this.defineSynchedEntityData(builder, SynchedDataIndex.ENTITY_ATTRIBUTES, entityAttributes);
  }

  @Override
  public void readAdditionalAttributeData(ValueInput valueInput) {
    if (!hasSavedEntityAttributes(valueInput)) {
      return;
    }

    super.readAdditionalAttributeData(valueInput);
  }

  private static boolean hasSavedEntityAttributes(ValueInput valueInput) {
    return valueInput.read(EntityAttributes.ENTITY_ATTRIBUTE_TAG, CompoundTag.CODEC).isPresent();
  }

  @Override
  public boolean removeWhenFarAway(double distanceToClosestPlayer) {
    return true;
  }
}
