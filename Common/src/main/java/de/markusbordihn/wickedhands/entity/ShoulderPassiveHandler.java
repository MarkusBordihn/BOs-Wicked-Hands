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

import de.markusbordihn.easynpc.api.handler.EasyNPCShoulderHandler;
import de.markusbordihn.easynpc.data.npc.ShoulderSide;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

// Easy NPC removes a shoulder NPC from the level like a vanilla parrot, so its own tick stops.
public final class ShoulderPassiveHandler {

  public static final int CHECK_INTERVAL_TICKS = 20;
  private static final String ENTITY_ID_TAG = "id";

  private ShoulderPassiveHandler() {}

  public static void tick(ServerPlayer player) {
    if (player.tickCount % CHECK_INTERVAL_TICKS != 0) {
      return;
    }

    update(player);
  }

  public static void update(ServerPlayer player) {
    Set<ShoulderPassive> activeShoulderPassives = EnumSet.noneOf(ShoulderPassive.class);
    for (ShoulderSide shoulderSide : ShoulderSide.values()) {
      getShoulderPassive(player, shoulderSide).ifPresent(activeShoulderPassives::add);
    }
    for (ShoulderPassive shoulderPassive : ShoulderPassive.values()) {
      if (activeShoulderPassives.contains(shoulderPassive)) {
        shoulderPassive.apply(player);
      } else {
        shoulderPassive.remove(player);
      }
    }
  }

  public static Set<ShoulderSide> getSidesHoldingOnThrough(
      ServerPlayer player, DamageSource damageSource) {
    return getSidesHoldingOn(
        player, shoulderPassive -> shoulderPassive.holdsOnThrough(damageSource));
  }

  public static Set<ShoulderSide> getSidesHoldingOnInSurroundings(ServerPlayer player) {
    return getSidesHoldingOn(
        player, shoulderPassive -> shoulderPassive.holdsOnInSurroundingsOf(player));
  }

  private static Set<ShoulderSide> getSidesHoldingOn(
      ServerPlayer player, Predicate<ShoulderPassive> holdsOn) {
    Set<ShoulderSide> shoulderSides = EnumSet.noneOf(ShoulderSide.class);
    for (ShoulderSide shoulderSide : ShoulderSide.values()) {
      if (getShoulderPassive(player, shoulderSide).filter(holdsOn).isPresent()) {
        shoulderSides.add(shoulderSide);
      }
    }
    return shoulderSides;
  }

  private static Optional<ShoulderPassive> getShoulderPassive(
      ServerPlayer player, ShoulderSide shoulderSide) {
    return EasyNPCShoulderHandler.getShoulderNPCData(player, shoulderSide)
        .flatMap(entityTag -> entityTag.getString(ENTITY_ID_TAG))
        .map(Identifier::tryParse)
        .flatMap(ShoulderPassive::forHand);
  }
}
