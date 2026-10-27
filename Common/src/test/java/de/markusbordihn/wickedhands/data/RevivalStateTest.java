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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RevivalStateTest {

  private static final UUID OWNER_UUID = UUID.randomUUID();
  private static final UUID COMPANION_UUID = UUID.randomUUID();

  private static RevivalState runningRitualOfBoundRemains() {
    return RevivalState.EMPTY
        .withCompanionUUID(Optional.of(COMPANION_UUID))
        .withBoundOwnerUUID(Optional.of(OWNER_UUID))
        .withOwnerUUID(Optional.of(OWNER_UUID))
        .withElapsedTicks(120);
  }

  @Test
  @DisplayName("Resetting a failed ritual keeps the identity and binding of the remains")
  void resetRitualKeepsRemainsIdentity() {
    RevivalState resetState =
        runningRitualOfBoundRemains().withOwnerUUID(Optional.empty()).withElapsedTicks(0);

    assertEquals(
        RevivalState.EMPTY
            .withCompanionUUID(Optional.of(COMPANION_UUID))
            .withBoundOwnerUUID(Optional.of(OWNER_UUID)),
        resetState);
  }

  @Test
  @DisplayName("Only players other than the bound owner count as strangers")
  void boundRemainsKnowTheirOwner() {
    RevivalState boundState = runningRitualOfBoundRemains();

    assertFalse(boundState.isBoundToOtherThan(OWNER_UUID));
    assertTrue(boundState.isBoundToOtherThan(UUID.randomUUID()));
    assertFalse(RevivalState.EMPTY.isBoundToOtherThan(UUID.randomUUID()));
  }

  @Test
  @DisplayName("Revival state survives saving and loading")
  void codecRoundTrip() {
    RevivalState state = runningRitualOfBoundRemains();
    Tag tag = RevivalState.CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();

    assertEquals(state, RevivalState.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow());
  }
}
