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
import de.markusbordihn.easynpc.data.render.RenderDataEntry;
import de.markusbordihn.easynpc.data.render.RenderType;
import de.markusbordihn.easynpc.data.synched.SynchedDataIndex;
import de.markusbordihn.easynpc.entity.easynpc.npc.easymodelentities.EasyModelNPC;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;

public abstract class AbstractRottenHandEntity extends EasyModelNPC {

  protected AbstractRottenHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
  }

  private static boolean hasSavedEntityAttributes(ValueInput valueInput) {
    return valueInput.read(EntityAttributes.ENTITY_ATTRIBUTE_TAG, CompoundTag.CODEC).isPresent();
  }

  protected abstract String getModelId();

  @Override
  public void defineSynchedRenderData(SynchedEntityData.Builder builder) {
    this.defineSynchedEntityData(
        builder,
        SynchedDataIndex.RENDER_DATA,
        new RenderDataEntry(RenderType.EASY_MODEL_ENTITY, null, this.getModelId()));
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
}
