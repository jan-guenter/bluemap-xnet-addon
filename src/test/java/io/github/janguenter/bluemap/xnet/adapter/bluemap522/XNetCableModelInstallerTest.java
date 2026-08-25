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
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class XNetCableModelInstallerTest {

    @Test
    void installsThreeBoundedMultipartStatesFromExactTextures() throws Exception {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        for (String id : new String[]{"netcable", "connector", "advanced_connector"}) {
            pack.getBlockStates().put(
                    Key.parse("xnet:" + id),
                    new BlockState(new Variants(
                            new VariantSet[0],
                            new VariantSet(new Variant(ResourcePack.MISSING_BLOCK_MODEL))
                    ))
            );
        }
        for (int color = 0; color < 5; color++) {
            putTexture(pack, "xnet:block/cable" + color + "/normal_netcable");
            putTexture(pack, "xnet:block/cable" + color + "/connector");
            putTexture(pack, "xnet:block/cable" + color + "/advanced_connector");
        }

        assertTrue(XNetCableModelInstaller.install(pack));
        assertEquals(95, pack.getModels().keySet().size());
        for (String id : new String[]{"netcable", "connector", "advanced_connector"}) {
            BlockState state = pack.getBlockStates().get(Key.parse("xnet:" + id));
            assertNotNull(state.getMultipart());
            assertEquals(65, state.getMultipart().getParts().length);
        }
    }

    @Test
    void missingInstalledTextureKeepsStockState() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        assertFalse(XNetCableModelInstaller.install(pack));
        assertEquals(0, pack.getModels().keySet().size());
    }

    private static void putTexture(ResourcePack pack, String id) throws Exception {
        Key key = Key.parse(id);
        pack.getTextures().put(
                key,
                Texture.from(key, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB))
        );
    }
}
