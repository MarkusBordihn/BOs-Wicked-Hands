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
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class CandleLightingEffects {

  private static final int SCAN_INTERVAL = 5;
  private static final float STORM_INTENSITY_PER_CANDLE = 0.1F;
  private static final int READY_RING_PARTICLES = 24;
  private static final double READY_RING_RADIUS = 2.0D;

  private final Entity hand;
  private Set<BlockPos> litCandles;

  CandleLightingEffects(Entity hand) {
    this.hand = hand;
  }

  void tick(Level level) {
    if (this.hand.tickCount % SCAN_INTERVAL == 0) {
      this.scanCandles(level);
    }
    if (this.hasLitCandles()) {
      int candleSteps = Math.min(this.litCandles.size(), RevivalRitual.REQUIRED_LIT_CANDLES);
      RitualStorm.request(this.hand, candleSteps * STORM_INTENSITY_PER_CANDLE);
    }
  }

  boolean hasLitCandles() {
    return this.litCandles != null && !this.litCandles.isEmpty();
  }

  private void scanCandles(Level level) {
    BlockPos ritualBasePosition = this.hand.getOnPos();
    if (!RevivalRitual.isRitualBase(level.getBlockState(ritualBasePosition))) {
      this.litCandles = Set.of();
      return;
    }

    Set<BlockPos> currentLitCandles =
        new HashSet<>(RevivalRitual.findLitCandles(level, ritualBasePosition));
    if (this.litCandles != null) {
      int litCandleCount = this.litCandles.size();
      for (BlockPos candlePosition : currentLitCandles) {
        if (!this.litCandles.contains(candlePosition)) {
          this.playCandleLit(level, candlePosition, ++litCandleCount);
        }
      }
    }
    this.litCandles = currentLitCandles;
  }

  private void playCandleLit(Level level, BlockPos candlePosition, int litCandleCount) {
    RandomSource random = level.getRandom();
    Vec3 candleFlame = EffectHelper.candleFlame(candlePosition);
    Vec3 handCenter = EffectHelper.handCenter(this.hand);
    for (int i = 0; i < 10; i++) {
      level.addParticle(
          ParticleTypes.SOUL_FIRE_FLAME,
          candleFlame.x(),
          candleFlame.y(),
          candleFlame.z(),
          random.triangle(0.0D, 0.02D),
          0.05D + random.nextDouble() * 0.07D,
          random.triangle(0.0D, 0.02D));
    }
    for (int i = 0; i < 4; i++) {
      EffectHelper.spawnSoulStream(level, candleFlame, handCenter);
    }
    for (int i = 0; i < 6; i++) {
      level.addParticle(
          ParticleTypes.SMOKE,
          handCenter.x() + random.triangle(0.0D, 0.3D),
          handCenter.y(),
          handCenter.z() + random.triangle(0.0D, 0.3D),
          0.0D,
          0.02D,
          0.0D);
    }

    int candleStep = Math.min(litCandleCount, RevivalRitual.REQUIRED_LIT_CANDLES);
    EffectHelper.playSound(
        level, candleFlame, SoundEvents.WARDEN_HEARTBEAT, 1.0F, 0.5F + 0.15F * candleStep);
    EffectHelper.playSound(
        level, handCenter, SoundEvents.SOUL_ESCAPE.value(), 0.8F, 0.4F + 0.1F * candleStep);
    EffectHelper.playSound(level, handCenter, SoundEvents.CREAKING_TWITCH, 0.6F, 0.8F);
    if (litCandleCount == RevivalRitual.REQUIRED_LIT_CANDLES) {
      this.playRitualReady(level, handCenter);
    }
  }

  private void playRitualReady(Level level, Vec3 handCenter) {
    for (int i = 0; i < READY_RING_PARTICLES; i++) {
      double angle = Mth.TWO_PI * i / READY_RING_PARTICLES;
      level.addParticle(
          ParticleTypes.SOUL_FIRE_FLAME,
          handCenter.x() + Math.cos(angle) * READY_RING_RADIUS,
          handCenter.y(),
          handCenter.z() + Math.sin(angle) * READY_RING_RADIUS,
          0.0D,
          0.02D,
          0.0D);
    }
    RandomSource random = level.getRandom();
    for (int i = 0; i < 8; i++) {
      level.addParticle(
          ParticleTypes.SCULK_SOUL,
          handCenter.x(),
          handCenter.y(),
          handCenter.z(),
          random.triangle(0.0D, 0.03D),
          0.06D,
          random.triangle(0.0D, 0.03D));
    }
    EffectHelper.playSound(
        level, handCenter, SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE, 1.0F, 0.6F);
  }
}
