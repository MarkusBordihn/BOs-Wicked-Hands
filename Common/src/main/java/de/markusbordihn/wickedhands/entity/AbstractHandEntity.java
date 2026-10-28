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
import de.markusbordihn.wickedhands.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class AbstractHandEntity extends EasyModelNPC {

  protected static final String MESSAGE_KEY_PREFIX = "message." + Constants.MOD_ID + ".hand.";
  private static final String WHISPER_MESSAGE_KEY = MESSAGE_KEY_PREFIX + "whisper";

  protected AbstractHandEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
  }

  public static Component createWhisper(Component speaker, Component message) {
    return Component.translatable(WHISPER_MESSAGE_KEY, speaker, message)
        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
  }

  protected static TagKey<Item> catalystItems(ResourceLocation companionHandId) {
    return TagKey.create(Registries.ITEM, companionHandId.withSuffix("_catalysts"));
  }

  protected static <T extends AbstractHandEntity> EntityType<T> createHandEntityType(
      EntityType.EntityFactory<T> entityFactory,
      MobCategory mobCategory,
      ResourceLocation entityId) {
    return EntityType.Builder.of(entityFactory, mobCategory)
        .sized(0.75F, 0.375F)
        .clientTrackingRange(8)
        .build(entityId.toString());
  }

  private String getModelId() {
    ResourceLocation entityId = EntityType.getKey(this.getType());
    return entityId.getNamespace() + ":entity/" + entityId.getPath();
  }

  @Override
  public void defineSynchedRenderData() {
    this.defineSynchedEntityData(
        SynchedDataIndex.RENDER_DATA,
        new RenderDataEntry(RenderType.EASY_MODEL_ENTITY, null, this.getModelId()));
  }

  @Override
  public void defineSynchedDisplayAttributeData() {
    this.defineSynchedEntityData(
        SynchedDataIndex.DISPLAY_ATTRIBUTE_SET,
        DisplayAttributeDataSet.createDefault()
            .withAttribute(
                DisplayAttributeType.NAME_VISIBILITY,
                new DisplayAttributeEntry(NameVisibilityType.NEVER.toString())));
  }

  @Override
  public void defineSynchedAttributeData() {
    EntityAttributes entityAttributes = new EntityAttributes();
    entityAttributes.setCombatAttributes(
        new CombatAttributes()
            .withIsInvulnerable(false)
            .withIsAttackableByPlayers(true)
            .withIsAttackableByMonsters(true));
    this.defineSynchedEntityData(SynchedDataIndex.ENTITY_ATTRIBUTES, entityAttributes);
  }
}
