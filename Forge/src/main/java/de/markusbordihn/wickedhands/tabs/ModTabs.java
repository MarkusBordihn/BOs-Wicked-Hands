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

import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModTabs {

  public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

  public static final RegistryObject<CreativeModeTab> WICKED_HANDS =
      CREATIVE_TABS.register(
          WickedHandsTab.ID,
          () ->
              CreativeModeTab.builder()
                  .icon(() -> ModItems.LIFELESS_ROTTEN_HAND.get().getDefaultInstance())
                  .title(WickedHandsTab.TITLE)
                  .displayItems(
                      (itemDisplayParameters, output) -> {
                        output.accept(ModItems.ROTTEN_HAND_SPAWN_EGG.get());
                        output.accept(ModItems.LIFELESS_ROTTEN_HAND.get());
                      })
                  .build());

  private ModTabs() {}

  public static void registerEvents() {
    BuildCreativeModeTabContentsEvent.BUS.addListener(ModTabs::addToVanillaTabs);
  }

  private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
      event.accept(ModItems.ROTTEN_HAND_SPAWN_EGG.get());
    }
  }
}
