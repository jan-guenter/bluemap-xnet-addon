/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.util.Key;

import org.junit.jupiter.api.Test;

class XNetCableModelInstallerTest {

    @Test
    void installsBoundedCableAndFacadeStatesBeforeBlueMapLoadsTextures() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        for (String id : new String[]{
                "netcable", "connector", "advanced_connector", "facade"
        }) {
            pack.getBlockStates().put(
                    Key.parse("xnet:" + id),
                    new BlockState(new Variants(
                            new VariantSet[0],
                            new VariantSet(new Variant(ResourcePack.MISSING_BLOCK_MODEL))
                    ))
            );
        }
        assertTrue(XNetCableModelInstaller.install(pack));
        assertEquals(96, pack.getModels().keySet().size());
        assertEquals(16, XNetCableModelInstaller.requiredTextureKeys().size());
        for (String id : new String[]{"netcable", "connector", "advanced_connector"}) {
            BlockState state = pack.getBlockStates().get(Key.parse("xnet:" + id));
            assertNotNull(state.getMultipart());
            assertEquals(65, state.getMultipart().getParts().length);
        }
        assertTrue(XNetCableModelInstaller.routeFacade(
                pack, de.bluecolored.bluemap.core.map.hires.block.BlockRendererType.DEFAULT
        ));
        BlockState facade = pack.getBlockStates().get(Key.parse("xnet:facade"));
        assertNotNull(facade.getVariants());
    }

    @Test
    void missingInstalledBlockStateKeepsStockState() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        assertFalse(XNetCableModelInstaller.install(pack));
        assertEquals(0, pack.getModels().keySet().size());
    }

}
