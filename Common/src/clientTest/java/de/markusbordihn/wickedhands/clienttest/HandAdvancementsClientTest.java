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
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandAdvancementsClientTest extends ClientTestBase {

  private static final long CHAT_TICKS = 20;

  @Test
  @DisplayName("Advancement tab shows all hand advancements")
  void advancementTabShowsHandAdvancements() {
    client.renderSettings(
        Parameters.of().with("gui", true).with("chat", true).with("toasts", true));
    client.runCommand("advancement grant @s through " + MOD_ID + ":rotten_hand_companion");
    client.await(Until.ticksElapsed(CHAT_TICKS));
    client.saveScreenshot("advancement_chat");
    client.pressBindingAndAwaitScreen("key.advancements");
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("advancement_tab");
    client.runCommand("advancement revoke @s from " + MOD_ID + ":root");
  }
}
