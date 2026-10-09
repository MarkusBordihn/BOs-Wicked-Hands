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

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class EffectHelper {

  private static final double SOUL_STREAM_SPEED = 0.06D;

  private EffectHelper() {}

  static Vec3 handCenter(Entity hand) {
    return hand.position().add(0.0D, 0.2D, 0.0D);
  }

  static Vec3 candleFlame(BlockPos candlePosition) {
    return Vec3.atCenterOf(candlePosition).add(0.0D, 0.1D, 0.0D);
  }

  static void spawnSoulStream(Level level, Vec3 from, Vec3 to) {
    Vec3 velocity = to.subtract(from).scale(SOUL_STREAM_SPEED);
    level.addParticle(
        ParticleTypes.SCULK_SOUL,
        from.x(),
        from.y(),
        from.z(),
        velocity.x(),
        velocity.y(),
        velocity.z());
  }

  static void playSound(
      Level level, Vec3 position, SoundEvent soundEvent, float volume, float pitch) {
    level.playLocalSound(
        position.x(),
        position.y(),
        position.z(),
        soundEvent,
        SoundSource.HOSTILE,
        volume,
        pitch,
        false);
  }
}
