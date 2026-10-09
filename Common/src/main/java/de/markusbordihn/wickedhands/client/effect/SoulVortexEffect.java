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

import de.markusbordihn.wickedhands.ritual.RevivalRitual;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class SoulVortexEffect {

  private static final int START_TICK = 10;
  private static final int ARMS = 3;
  private static final double START_RADIUS = 1.8D;
  private static final double END_RADIUS = 0.3D;
  private static final double HEIGHT = 1.6D;
  private static final float SLOWEST_SPIN = 0.15F;
  private static final float FASTEST_SPIN = 0.6F;

  private float angle;

  void reset() {
    this.angle = 0.0F;
  }

  void tick(Level level, Vec3 center, int ritualTick) {
    if (ritualTick < START_TICK) {
      return;
    }

    float progress =
        Math.min(
            (float) (ritualTick - START_TICK) / (RevivalRitual.DURATION_TICKS - START_TICK), 1.0F);
    this.angle += Mth.lerp(progress, SLOWEST_SPIN, FASTEST_SPIN);
    this.spawnArms(level, center, ritualTick, progress);
    this.spawnSmokeAndAsh(level, center, ritualTick);
  }

  private void spawnArms(Level level, Vec3 center, int ritualTick, float progress) {
    double radius = Mth.lerp(progress, START_RADIUS, END_RADIUS);
    double maxHeight = HEIGHT * (0.5D + progress * 0.5D);
    for (int arm = 0; arm < ARMS; arm++) {
      for (int layer = 0; layer < 2; layer++) {
        double armAngle = this.angle + Mth.TWO_PI * arm / ARMS + layer * 0.4D;
        double relativeHeight = (ritualTick * 0.05D + layer * 0.5D + (double) arm / ARMS) % 1.0D;
        double layerRadius = radius * (1.0D - relativeHeight * 0.3D);
        level.addParticle(
            arm == 0 ? ParticleTypes.SOUL : ParticleTypes.SCULK_SOUL,
            center.x() + Math.cos(armAngle) * layerRadius,
            center.y() + relativeHeight * maxHeight,
            center.z() + Math.sin(armAngle) * layerRadius,
            -Math.sin(armAngle) * 0.05D,
            0.02D,
            Math.cos(armAngle) * 0.05D);
      }
    }
  }

  private void spawnSmokeAndAsh(Level level, Vec3 center, int ritualTick) {
    RandomSource random = level.getRandom();
    if (ritualTick % 2 == 0) {
      double smokeAngle = random.nextDouble() * Mth.TWO_PI;
      level.addParticle(
          ParticleTypes.LARGE_SMOKE,
          center.x() + Math.cos(smokeAngle) * START_RADIUS,
          center.y(),
          center.z() + Math.sin(smokeAngle) * START_RADIUS,
          0.0D,
          0.03D,
          0.0D);
    }
    level.addParticle(
        ParticleTypes.ASH,
        center.x() + random.triangle(0.0D, 3.0D),
        center.y() + random.nextDouble() * 2.0D,
        center.z() + random.triangle(0.0D, 3.0D),
        0.0D,
        0.0D,
        0.0D);
  }
}
