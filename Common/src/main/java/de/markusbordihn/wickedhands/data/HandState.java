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

public record HandState(HandLifecycle lifecycle, CompanionMode companionMode) {

  public static final HandState EMPTY = new HandState(HandLifecycle.WILD, CompanionMode.FOLLOW);

  public static final Codec<HandState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      HandLifecycle.CODEC
                          .optionalFieldOf("lifecycle", EMPTY.lifecycle())
                          .forGetter(HandState::lifecycle),
                      CompanionMode.CODEC
                          .optionalFieldOf("companion_mode", EMPTY.companionMode())
                          .forGetter(HandState::companionMode))
                  .apply(instance, HandState::new));

  public HandState withLifecycle(HandLifecycle lifecycle) {
    return new HandState(lifecycle, this.companionMode);
  }

  public HandState withCompanionMode(CompanionMode companionMode) {
    return new HandState(this.lifecycle, companionMode);
  }
}
