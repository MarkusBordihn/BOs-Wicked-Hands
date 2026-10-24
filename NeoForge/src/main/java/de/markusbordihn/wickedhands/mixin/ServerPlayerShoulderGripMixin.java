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

package de.markusbordihn.wickedhands.mixin;

import de.markusbordihn.easynpc.data.npc.ShoulderSide;
import de.markusbordihn.wickedhands.access.ShoulderGripAccess;
import de.markusbordihn.wickedhands.entity.ShoulderPassiveHandler;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerShoulderGripMixin implements ShoulderGripAccess {

  @Unique private Optional<CompoundTag> keptShoulderEntityLeft = Optional.empty();
  @Unique private Optional<CompoundTag> keptShoulderEntityRight = Optional.empty();

  @Shadow
  public abstract CompoundTag getShoulderEntityLeft();

  @Shadow
  public abstract CompoundTag getShoulderEntityRight();

  @Shadow
  protected abstract void setShoulderEntityLeft(CompoundTag entityTag);

  @Shadow
  protected abstract void setShoulderEntityRight(CompoundTag entityTag);

  @Override
  public void keepShoulderEntities(Set<ShoulderSide> shoulderSides) {
    if (shoulderSides.contains(ShoulderSide.LEFT)) {
      this.keptShoulderEntityLeft = Optional.of(this.getShoulderEntityLeft());
      this.setShoulderEntityLeft(new CompoundTag());
    }
    if (shoulderSides.contains(ShoulderSide.RIGHT)) {
      this.keptShoulderEntityRight = Optional.of(this.getShoulderEntityRight());
      this.setShoulderEntityRight(new CompoundTag());
    }
  }

  @Override
  public void restoreKeptShoulderEntities() {
    this.keptShoulderEntityLeft.ifPresent(this::setShoulderEntityLeft);
    this.keptShoulderEntityRight.ifPresent(this::setShoulderEntityRight);
    this.keptShoulderEntityLeft = Optional.empty();
    this.keptShoulderEntityRight = Optional.empty();
  }

  @Inject(
      method = "handleShoulderEntities",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/server/level/ServerPlayer;removeEntitiesOnShoulder()V"))
  private void beforeSurroundingsRemoveEntitiesOnShoulder(CallbackInfo callbackInfo) {
    this.keepShoulderEntities(
        ShoulderPassiveHandler.getSidesHoldingOnInSurroundings((ServerPlayer) (Object) this));
  }

  @Inject(
      method = "handleShoulderEntities",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/server/level/ServerPlayer;removeEntitiesOnShoulder()V",
              shift = At.Shift.AFTER))
  private void afterSurroundingsRemoveEntitiesOnShoulder(CallbackInfo callbackInfo) {
    this.restoreKeptShoulderEntities();
  }
}
