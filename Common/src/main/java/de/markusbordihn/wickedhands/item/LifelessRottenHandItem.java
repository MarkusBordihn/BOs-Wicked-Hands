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
import de.markusbordihn.wickedhands.entity.LifelessRottenHandEntity;
import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import java.util.List;
import java.util.UUID;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LifelessRottenHandItem extends Item {

  public static final String ID = "lifeless_rotten_hand";
  public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, ID);

  private static final String TOOLTIP_TRANSLATION_KEY =
      "item." + Constants.MOD_ID + "." + ID + ".tooltip";
  private static final String REVIVAL_HINT_TRANSLATION_KEY =
      "item." + Constants.MOD_ID + "." + ID + ".revival_hint";

  private LifelessRottenHandItem(Item.Properties properties) {
    super(properties);
  }

  public static Item create() {
    return new LifelessRottenHandItem(
        new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, ITEM_ID))
            .component(DataComponents.LORE, createTooltip()));
  }

  public static ItemStack createCompanionRemains(UUID companionUUID, Component customName) {
    ItemStack remains = new ItemStack(BuiltInRegistries.ITEM.getValue(ITEM_ID));
    remains.set(HandDataComponents.COMPANION_UUID, companionUUID);
    if (customName != null) {
      remains.set(DataComponents.CUSTOM_NAME, customName);
    }
    return remains;
  }

  private static ItemLore createTooltip() {
    return new ItemLore(
        List.of(
            Component.translatable(TOOLTIP_TRANSLATION_KEY)
                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))));
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
      if (!LifelessRottenHandEntity.place(
          serverLevel,
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
      player.sendOverlayMessage(Component.translatable(REVIVAL_HINT_TRANSLATION_KEY));
    }
    return InteractionResult.SUCCESS;
  }
}
