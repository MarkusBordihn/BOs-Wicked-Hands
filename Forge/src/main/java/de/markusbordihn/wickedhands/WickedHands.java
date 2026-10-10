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

package de.markusbordihn.wickedhands;

import de.markusbordihn.wickedhands.client.renderer.ModEntityRenderers;
import de.markusbordihn.wickedhands.entity.ModEntityTypes;
import de.markusbordihn.wickedhands.gametest.ModGameTests;
import de.markusbordihn.wickedhands.item.ModDataComponents;
import de.markusbordihn.wickedhands.item.ModItems;
import de.markusbordihn.wickedhands.loot.ModLootTables;
import de.markusbordihn.wickedhands.tabs.ModTabs;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
public class WickedHands {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings("java:S1118")
  public WickedHands(FMLJavaModLoadingContext context) {
    log.info("Initializing {} (Forge) ...", Constants.MOD_NAME);

    BusGroup modBusGroup = context.getModBusGroup();
    ModDataComponents.DATA_COMPONENT_TYPES.register(modBusGroup);
    ModItems.ITEMS.register(modBusGroup);
    ModEntityTypes.ENTITY_TYPES.register(modBusGroup);
    ModTabs.CREATIVE_TABS.register(modBusGroup);
    ModEntityTypes.registerEvents();
    ModTabs.registerEvents();
    ModLootTables.registerEvents();
    ModGameTests.register(modBusGroup);

    if (FMLEnvironment.dist.isClient()) {
      ModEntityRenderers.register();
    }
  }
}
