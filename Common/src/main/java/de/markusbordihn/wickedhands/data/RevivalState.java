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

package de.markusbordihn.wickedhands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record RevivalState(
    Optional<UUID> ownerUUID, Optional<UUID> companionUUID, int elapsedTicks) {

  public static final RevivalState EMPTY = new RevivalState(Optional.empty(), Optional.empty(), 0);

  public static final Codec<RevivalState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(RevivalState::ownerUUID),
                      UUIDUtil.CODEC
                          .optionalFieldOf("companion")
                          .forGetter(RevivalState::companionUUID),
                      Codec.INT
                          .optionalFieldOf("elapsed_ticks", EMPTY.elapsedTicks())
                          .forGetter(RevivalState::elapsedTicks))
                  .apply(instance, RevivalState::new));

  public RevivalState withOwnerUUID(UUID ownerUUID) {
    return new RevivalState(Optional.of(ownerUUID), this.companionUUID, this.elapsedTicks);
  }

  public RevivalState withCompanionUUID(Optional<UUID> companionUUID) {
    return new RevivalState(this.ownerUUID, companionUUID, this.elapsedTicks);
  }

  public RevivalState withElapsedTicks(int elapsedTicks) {
    return new RevivalState(this.ownerUUID, this.companionUUID, elapsedTicks);
  }
}
