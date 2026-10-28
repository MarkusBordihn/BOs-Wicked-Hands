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

package de.markusbordihn.wickedhands.item;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class HandItemData {

  private static final String COMPANION_UUID_TAG = "CompanionUUID";
  private static final String OWNER_UUID_TAG = "OwnerUUID";

  private HandItemData() {}

  public static Optional<UUID> getCompanionUUID(ItemStack itemStack) {
    return getUUID(itemStack, COMPANION_UUID_TAG);
  }

  public static void setCompanionUUID(ItemStack itemStack, UUID companionUUID) {
    itemStack.getOrCreateTag().putUUID(COMPANION_UUID_TAG, companionUUID);
  }

  public static Optional<UUID> getOwnerUUID(ItemStack itemStack) {
    return getUUID(itemStack, OWNER_UUID_TAG);
  }

  public static void setOwnerUUID(ItemStack itemStack, UUID ownerUUID) {
    itemStack.getOrCreateTag().putUUID(OWNER_UUID_TAG, ownerUUID);
  }

  private static Optional<UUID> getUUID(ItemStack itemStack, String key) {
    CompoundTag itemTag = itemStack.getTag();
    if (itemTag == null || !itemTag.hasUUID(key)) {
      return Optional.empty();
    }

    return Optional.of(itemTag.getUUID(key));
  }
}
