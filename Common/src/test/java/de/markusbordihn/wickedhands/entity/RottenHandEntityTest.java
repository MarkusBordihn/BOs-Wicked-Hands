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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.wickedhands.TestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ServerLevelAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RottenHandEntityTest {

  @BeforeAll
  static void bootstrap() {
    TestBootstrap.bootstrap();
  }

  private static ServerLevelAccessor mockLevel(Difficulty difficulty, boolean darkOutside) {
    ServerLevel serverLevel = mock(ServerLevel.class);
    when(serverLevel.isDarkOutside()).thenReturn(darkOutside);
    ServerLevelAccessor levelAccessor = mock(ServerLevelAccessor.class);
    when(levelAccessor.getDifficulty()).thenReturn(difficulty);
    when(levelAccessor.getLevel()).thenReturn(serverLevel);
    return levelAccessor;
  }

  private static boolean checkSpawnRules(ServerLevelAccessor levelAccessor) {
    return RottenHandEntity.checkRottenHandSpawnRules(
        null, levelAccessor, EntitySpawnReason.NATURAL, BlockPos.ZERO, RandomSource.create());
  }

  @Test
  @DisplayName("Rotten hand never spawns on peaceful")
  void noSpawnOnPeaceful() {
    assertFalse(checkSpawnRules(mockLevel(Difficulty.PEACEFUL, true)));
  }

  @Test
  @DisplayName("Rotten hand never spawns during the day")
  void noSpawnDuringDay() {
    assertFalse(checkSpawnRules(mockLevel(Difficulty.NORMAL, false)));
  }
}
