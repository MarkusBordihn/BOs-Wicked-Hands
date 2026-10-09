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

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AwakeningEffects {

  private static final int AFTERMATH_TICKS = 40;

  private final Entity hand;
  private int awakeningTicks = -1;

  public AwakeningEffects(Entity hand) {
    this.hand = hand;
  }

  public void start() {
    this.awakeningTicks = 0;
  }

  public void tick() {
    if (this.awakeningTicks < 0) {
      return;
    }

    Level level = this.hand.level();
    RitualStorm.tick(level);
    int tick = this.awakeningTicks++;
    if (tick >= AFTERMATH_TICKS) {
      this.awakeningTicks = -1;
      return;
    }

    if (tick % 4 == 0) {
      this.spawnAftermath(level);
    }
  }

  public void stop() {
    if (this.awakeningTicks >= 0) {
      RitualStorm.restore(this.hand.level());
    }
  }

  private void spawnAftermath(Level level) {
    RandomSource random = level.getRandom();
    Vec3 handCenter = EffectHelper.handCenter(this.hand);
    level.addParticle(
        ParticleTypes.SMOKE,
        handCenter.x() + random.triangle(0.0D, 0.3D),
        handCenter.y(),
        handCenter.z() + random.triangle(0.0D, 0.3D),
        0.0D,
        0.03D,
        0.0D);
    level.addParticle(
        ParticleTypes.SOUL,
        handCenter.x() + random.triangle(0.0D, 1.0D),
        handCenter.y() + 0.5D,
        handCenter.z() + random.triangle(0.0D, 1.0D),
        0.0D,
        0.05D,
        0.0D);
  }
}
