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

package de.markusbordihn.wickedhands.ritual;

import de.markusbordihn.wickedhands.Constants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RevivalRitual {

  public static final TagKey<Block> RITUAL_BASE_BLOCKS =
      TagKey.create(
          Registries.BLOCK,
          Identifier.fromNamespaceAndPath(Constants.MOD_ID, "ritual_base_blocks"));
  public static final int REQUIRED_LIT_CANDLES = 4;
  public static final int DURATION_TICKS = 140;

  private static final int CANDLE_HORIZONTAL_RADIUS = 2;
  private static final int CANDLE_VERTICAL_RADIUS = 1;

  private RevivalRitual() {}

  public static boolean isRitualBase(BlockState blockState) {
    return blockState.is(RITUAL_BASE_BLOCKS);
  }

  public static boolean hasEnoughLitCandles(Level level, BlockPos ritualBasePosition) {
    return findLitCandles(level, ritualBasePosition).size() >= REQUIRED_LIT_CANDLES;
  }

  public static void extinguishCandles(Level level, BlockPos ritualBasePosition) {
    for (BlockPos candlePosition : findLitCandles(level, ritualBasePosition)) {
      AbstractCandleBlock.extinguish(
          null, level.getBlockState(candlePosition), level, candlePosition);
    }
  }

  public static List<BlockPos> findLitCandles(Level level, BlockPos ritualBasePosition) {
    BlockPos handPosition = ritualBasePosition.above();
    List<BlockPos> litCandles = new ArrayList<>();
    for (BlockPos position :
        BlockPos.betweenClosed(
            handPosition.offset(
                -CANDLE_HORIZONTAL_RADIUS, -CANDLE_VERTICAL_RADIUS, -CANDLE_HORIZONTAL_RADIUS),
            handPosition.offset(
                CANDLE_HORIZONTAL_RADIUS, CANDLE_VERTICAL_RADIUS, CANDLE_HORIZONTAL_RADIUS))) {
      BlockState blockState = level.getBlockState(position);
      if (blockState.is(BlockTags.CANDLES)
          && blockState.getValueOrElse(AbstractCandleBlock.LIT, false)) {
        litCandles.add(position.immutable());
      }
    }
    return litCandles;
  }
}
