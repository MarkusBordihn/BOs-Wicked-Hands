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

package de.markusbordihn.wickedhands.advancement;

import de.markusbordihn.wickedhands.Constants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class HandAdvancements {

  public static final Identifier ROOT_ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "root");
  public static final Identifier START_RITUAL_ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "start_ritual");

  private static final String COMPANION_CRITERION = "companion";

  private HandAdvancements() {}

  public static Identifier companionAdvancementId(Identifier handId) {
    return handId.withSuffix("_companion");
  }

  public static void awardCompanion(ServerPlayer serverPlayer, Identifier handId) {
    AdvancementHolder advancement =
        serverPlayer.level().getServer().getAdvancements().get(companionAdvancementId(handId));
    if (advancement == null) {
      return;
    }

    serverPlayer.getAdvancements().award(advancement, COMPANION_CRITERION);
  }
}
