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

package de.markusbordihn.wickedhands.clienttest;

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandAdvancementsClientTest extends ClientTestBase {

  private static final long CHAT_TICKS = 20;
  private static final long RENDER_TICKS = 10;

  @Test
  @DisplayName("Advancement tab shows all hand advancements")
  void advancementTabShowsHandAdvancements() {
    renderProfile("shown");
    command("advancement grant @s through wicked_hands:rotten_hand_companion");
    client.await(Until.ticksElapsed(CHAT_TICKS));
    client.saveScreenshot("advancement_chat");
    client.pressBindingAndAwaitScreen("key.advancements");
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("advancement_tab");
    command("advancement revoke @s from wicked_hands:root");
  }
}
