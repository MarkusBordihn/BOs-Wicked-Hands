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
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class RevivalRitualEffects {

  private static final int SOUL_STREAM_START_TICK = 30;
  private static final int SPELL_CAST_TICK = RevivalRitual.DURATION_TICKS - 40;
  private static final int[] WIND_TICKS = {10, 50, 90};
  private static final int WHISPER_INTERVAL = 25;
  private static final int SLOWEST_HEARTBEAT_INTERVAL = 20;
  private static final int FASTEST_HEARTBEAT_INTERVAL = 5;
  private static final int SHOCKWAVE_PARTICLES = 36;
  private static final double SHOCKWAVE_SPEED = 0.25D;
  private static final float RITUAL_STORM_INTENSITY = 1.0F;

  private final Entity hand;
  private final CandleLightingEffects candleLightingEffects;
  private final SoulVortexEffect soulVortex = new SoulVortexEffect();
  private List<BlockPos> ritualCandles = List.of();
  private int ritualTicks = -1;
  private boolean completed;
  private int nextHeartbeatTick;

  public RevivalRitualEffects(Entity hand) {
    this.hand = hand;
    this.candleLightingEffects = new CandleLightingEffects(hand);
  }

  public void start() {
    this.ritualTicks = 0;
    this.nextHeartbeatTick = 0;
    this.soulVortex.reset();
    this.ritualCandles = RevivalRitual.findLitCandles(this.hand.level(), this.hand.getOnPos());
  }

  public void tick() {
    Level level = this.hand.level();
    RitualStorm.tick(level);
    if (this.ritualTicks >= 0) {
      this.tickRitual(level);
      return;
    }

    this.candleLightingEffects.tick(level);
  }

  public void complete() {
    this.completed = true;
    this.ritualTicks = -1;
    this.playClimax(this.hand.level());
  }

  public void stop() {
    if (!this.completed && (this.ritualTicks >= 0 || this.candleLightingEffects.hasLitCandles())) {
      RitualStorm.restore(this.hand.level());
    }
  }

  private void tickRitual(Level level) {
    int tick = this.ritualTicks++;
    Vec3 handCenter = EffectHelper.handCenter(this.hand);
    RitualStorm.request(this.hand, RITUAL_STORM_INTENSITY);
    this.playRitualSounds(level, handCenter, tick);
    this.spawnCandleFlares(level, tick);
    this.spawnSoulStreams(level, handCenter, tick);
    this.soulVortex.tick(level, handCenter, tick);
  }

  private void playRitualSounds(Level level, Vec3 handCenter, int tick) {
    if (tick == 0) {
      EffectHelper.playSound(level, handCenter, SoundEvents.SOUL_ESCAPE.value(), 1.5F, 0.6F);
      EffectHelper.playSound(level, handCenter, SoundEvents.AMBIENT_CAVE.value(), 1.0F, 0.7F);
    }
    for (int windTick : WIND_TICKS) {
      if (tick == windTick) {
        EffectHelper.playSound(level, handCenter, SoundEvents.BREEZE_WHIRL, 1.0F, 0.5F);
      }
    }
    if (tick > 0 && tick % WHISPER_INTERVAL == 0) {
      EffectHelper.playSound(
          level, handCenter, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 1.0F, 0.8F);
    }
    if (tick == SPELL_CAST_TICK) {
      EffectHelper.playSound(level, handCenter, SoundEvents.EVOKER_PREPARE_SUMMON, 1.0F, 0.6F);
    }
    if (tick >= this.nextHeartbeatTick) {
      float progress = Math.min((float) tick / RevivalRitual.DURATION_TICKS, 1.0F);
      EffectHelper.playSound(
          level, handCenter, SoundEvents.WARDEN_HEARTBEAT, 1.0F, 0.6F + progress * 0.6F);
      this.nextHeartbeatTick =
          tick
              + Math.round(
                  Mth.lerp(progress, SLOWEST_HEARTBEAT_INTERVAL, FASTEST_HEARTBEAT_INTERVAL));
    }
  }

  private void spawnCandleFlares(Level level, int tick) {
    if (tick % 4 != 0) {
      return;
    }

    RandomSource random = level.getRandom();
    for (BlockPos candlePosition : this.ritualCandles) {
      Vec3 candleFlame = EffectHelper.candleFlame(candlePosition);
      level.addParticle(
          ParticleTypes.SOUL_FIRE_FLAME,
          candleFlame.x(),
          candleFlame.y(),
          candleFlame.z(),
          random.triangle(0.0D, 0.01D),
          0.03D + random.nextDouble() * 0.05D,
          random.triangle(0.0D, 0.01D));
    }
  }

  private void spawnSoulStreams(Level level, Vec3 handCenter, int tick) {
    if (tick < SOUL_STREAM_START_TICK || tick % 3 != 0) {
      return;
    }

    for (BlockPos candlePosition : this.ritualCandles) {
      EffectHelper.spawnSoulStream(level, EffectHelper.candleFlame(candlePosition), handCenter);
    }
  }

  private void playClimax(Level level) {
    Vec3 handCenter = EffectHelper.handCenter(this.hand);
    level.addParticle(
        ParticleTypes.GUST_EMITTER_SMALL,
        handCenter.x(),
        handCenter.y(),
        handCenter.z(),
        0.0D,
        0.0D,
        0.0D);
    for (int i = 0; i < SHOCKWAVE_PARTICLES; i++) {
      double angle = Mth.TWO_PI * i / SHOCKWAVE_PARTICLES;
      level.addParticle(
          i % 2 == 0 ? ParticleTypes.SCULK_SOUL : ParticleTypes.SOUL_FIRE_FLAME,
          handCenter.x(),
          handCenter.y(),
          handCenter.z(),
          Math.cos(angle) * SHOCKWAVE_SPEED,
          0.01D,
          Math.sin(angle) * SHOCKWAVE_SPEED);
    }
    for (BlockPos candlePosition : this.ritualCandles) {
      Vec3 candleFlame = EffectHelper.candleFlame(candlePosition);
      level.addParticle(
          ParticleTypes.LARGE_SMOKE,
          candleFlame.x(),
          candleFlame.y(),
          candleFlame.z(),
          0.0D,
          0.05D,
          0.0D);
    }
    EffectHelper.playSound(
        level, handCenter, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 1.0F, 0.6F);
    EffectHelper.playSound(level, handCenter, SoundEvents.GHAST_SCREAM, 0.6F, 0.5F);
    EffectHelper.playSound(level, handCenter, SoundEvents.CREAKING_TWITCH, 1.0F, 1.0F);
  }
}
