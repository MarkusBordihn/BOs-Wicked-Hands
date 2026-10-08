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

package de.markusbordihn.wickedhands.tabs;

import de.markusbordihn.wickedhands.item.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;

public final class ModTabs {

  private ModTabs() {}

  public static void register() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        WickedHandsTab.TAB_ID,
        FabricCreativeModeTab.builder()
            .icon(ModItems.LIFELESS_ROTTEN_HAND::getDefaultInstance)
            .title(WickedHandsTab.TITLE)
            .displayItems(
                (itemDisplayParameters, output) -> {
                  output.accept(ModItems.ROTTEN_HAND_SPAWN_EGG);
                  output.accept(ModItems.LIFELESS_ROTTEN_HAND);
                })
            .build());
    CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
        .register(output -> output.accept(ModItems.ROTTEN_HAND_SPAWN_EGG));
  }
}
