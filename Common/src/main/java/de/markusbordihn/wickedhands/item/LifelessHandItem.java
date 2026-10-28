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

package de.markusbordihn.wickedhands.item;

import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.AbstractLifelessHandEntity;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LifelessHandItem extends Item {

  private static final String RITUAL_TOOLTIP_KEY =
      "item." + Constants.MOD_ID + ".lifeless_hand.ritual";
  private static final String FEEDING_TOOLTIP_KEY =
      "item." + Constants.MOD_ID + ".lifeless_hand.feeding";
  private static final int WHISPER_COOLDOWN_TICKS = 60;

  private final Identifier lifelessHandId;

  private LifelessHandItem(Item.Properties properties, Identifier lifelessHandId) {
    super(properties);
    this.lifelessHandId = lifelessHandId;
  }

  public static Item create(Identifier lifelessHandId) {
    return new LifelessHandItem(
        new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, lifelessHandId))
            .component(DataComponents.LORE, createTooltip(lifelessHandId)),
        lifelessHandId);
  }

  public static ItemStack createCompanionRemains(
      Identifier lifelessHandId,
      UUID companionUUID,
      Optional<UUID> ownerUUID,
      Component customName) {
    ItemStack remains = new ItemStack(BuiltInRegistries.ITEM.getValue(lifelessHandId));
    remains.set(HandDataComponents.COMPANION_UUID, companionUUID);
    ownerUUID.ifPresent(uuid -> remains.set(HandDataComponents.OWNER_UUID, uuid));
    if (customName != null) {
      remains.set(DataComponents.CUSTOM_NAME, customName);
    }
    return remains;
  }

  public static void protectRemains(ItemEntity remains, UUID ownerUUID) {
    if (remains == null) {
      return;
    }

    remains.setInvulnerable(true);
    remains.setUnlimitedLifetime();
    remains.setTarget(ownerUUID);
  }

  private static ItemLore createTooltip(Identifier lifelessHandId) {
    return new ItemLore(
        Stream.of(
                Component.translatable(lifelessHandId.toLanguageKey("item", "tooltip")),
                Component.translatable(RITUAL_TOOLTIP_KEY),
                Component.translatable(lifelessHandId.toLanguageKey("item", "offering")),
                Component.translatable(FEEDING_TOOLTIP_KEY),
                Component.translatable(lifelessHandId.toLanguageKey("item", "shoulder")))
            .<Component>map(
                line ->
                    line.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
            .toList());
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos ritualBasePosition = context.getClickedPos();
    BlockState ritualBase = level.getBlockState(ritualBasePosition);
    if (context.getClickedFace() != Direction.UP
        || !RevivalRitual.isRitualBase(ritualBase)
        || !level.isEmptyBlock(ritualBasePosition.above())) {
      return InteractionResult.PASS;
    }

    if (level instanceof ServerLevel serverLevel) {
      double surfaceHeight =
          ritualBase.getCollisionShape(level, ritualBasePosition).max(Direction.Axis.Y);
      ItemStack itemStack = context.getItemInHand();
      if (!AbstractLifelessHandEntity.place(
          serverLevel,
          this.lifelessHandId,
          Vec3.atBottomCenterOf(ritualBasePosition).add(0.0D, surfaceHeight, 0.0D),
          context.getRotation() + 180.0F,
          context.getPlayer(),
          itemStack)) {
        return InteractionResult.FAIL;
      }

      itemStack.consume(1, context.getPlayer());
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide()) {
      ItemStack itemStack = player.getItemInHand(hand);
      player.displayClientMessage(
          AbstractHandEntity.createWhisper(
              itemStack.getHoverName(),
              Component.translatable(this.getDescriptionId() + ".revival_hint")),
          false);
      player.getCooldowns().addCooldown(itemStack, WHISPER_COOLDOWN_TICKS);
    }
    return InteractionResult.SUCCESS;
  }
}
