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

package de.markusbordihn.wickedhands.client.effect;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

final class RitualStorm {

  private static final float FADE_PER_TICK = 0.04F;
  private static final double RADIUS = 32.0D;

  private static Level boundLevel;
  private static long lastTickGameTime = -1;
  private static float requestedIntensity;
  private static float intensity;
  private static float baseRainLevel;
  private static float baseThunderLevel;
  private static float appliedRainLevel;
  private static float appliedRainWeightedThunderLevel;

  private RitualStorm() {}

  static void request(Entity source, float stormIntensity) {
    Level level = source.level();
    for (Player player : level.players()) {
      if (player.isLocalPlayer() && player.distanceToSqr(source) <= RADIUS * RADIUS) {
        bindTo(level);
        requestedIntensity = Math.max(requestedIntensity, stormIntensity);
        return;
      }
    }
  }

  static void tick(Level level) {
    bindTo(level);
    if (level.getGameTime() == lastTickGameTime) {
      return;
    }

    lastTickGameTime = level.getGameTime();
    float targetIntensity = requestedIntensity;
    requestedIntensity = 0.0F;
    if (intensity <= 0.0F && targetIntensity <= 0.0F) {
      return;
    }

    if (intensity <= 0.0F || weatherChangedByServer(level)) {
      baseRainLevel = level.getRainLevel(1.0F);
      baseThunderLevel = rawThunderLevel(level);
    }

    intensity = Mth.approach(intensity, targetIntensity, FADE_PER_TICK);
    if (intensity <= 0.0F) {
      level.setRainLevel(baseRainLevel);
      level.setThunderLevel(baseThunderLevel);
      return;
    }

    level.setRainLevel(Math.max(baseRainLevel, intensity));
    level.setThunderLevel(Math.max(baseThunderLevel, intensity));
    appliedRainLevel = level.getRainLevel(1.0F);
    appliedRainWeightedThunderLevel = level.getThunderLevel(1.0F);
  }

  static void restore(Level level) {
    if (level != boundLevel || intensity <= 0.0F) {
      return;
    }

    intensity = 0.0F;
    level.setRainLevel(baseRainLevel);
    level.setThunderLevel(baseThunderLevel);
  }

  private static void bindTo(Level level) {
    if (level == boundLevel) {
      return;
    }

    boundLevel = level;
    lastTickGameTime = -1;
    requestedIntensity = 0.0F;
    intensity = 0.0F;
  }

  private static boolean weatherChangedByServer(Level level) {
    return level.getRainLevel(1.0F) != appliedRainLevel
        || level.getThunderLevel(1.0F) != appliedRainWeightedThunderLevel;
  }

  private static float rawThunderLevel(Level level) {
    float rainLevel = level.getRainLevel(1.0F);
    return rainLevel > 0.0F ? level.getThunderLevel(1.0F) / rainLevel : 0.0F;
  }
}
