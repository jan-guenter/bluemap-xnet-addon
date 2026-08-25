/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockStateCondition;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class XNetAntennaModelsTest {

    @Test
    void compilesTheExactInstalledXNetAntennaRoster() throws IOException {
        String candidate = System.getProperty("xnetJar");
        assumeTrue(candidate != null, "exact candidate is a prototype-gate input");

        XNetAntennaModels.Catalog catalog = XNetAntennaModels.load(Path.of(candidate));

        assertEquals(Set.of(
                "xnet:antenna", "xnet:antenna_base", "xnet:antenna_dish"
        ), catalog.models().keySet());
        assertEquals(60, catalog.models().get("xnet:antenna").model().triangles().size());
        assertEquals(
                28,
                catalog.models().get("xnet:antenna_base").model().triangles().size()
        );
        assertEquals(
                42,
                catalog.models().get("xnet:antenna_dish").model().triangles().size()
        );
        assertEquals(Set.of(
                Key.parse("xnet:block/antenna"),
                Key.parse("xnet:block/antenna_dish")
        ), catalog.textures());
    }

    @Test
    void wrapsExactlyNineAuthoredVariantsAfterPreflight() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        putFacingState(pack, "antenna", 4);
        putFacingState(pack, "antenna_base", 1);
        putFacingState(pack, "antenna_dish", 4);

        assertTrue(XNetAntennaModelInstaller.install(pack, BlockRendererType.DEFAULT));
        List<Variant> variants = new ArrayList<>();
        for (String block : List.of("antenna", "antenna_base", "antenna_dish")) {
            pack.getBlockStates().get(Key.parse("xnet:" + block)).forEach(variants::add);
        }
        assertEquals(9, variants.size());
    }

    private static void putFacingState(ResourcePack pack, String name, int count) {
        ResourcePath<Model> model = new ResourcePath<>(Key.parse("xnet:block/" + name));
        if (count == 1) {
            pack.getBlockStates().put(
                    Key.parse("xnet:" + name),
                    new BlockState(new Variants(
                            new VariantSet[0], new VariantSet(new Variant(model))
                    ))
            );
            return;
        }
        String[] facings = {"north", "south", "west", "east"};
        VariantSet[] variants = new VariantSet[facings.length];
        for (int index = 0; index < facings.length; index++) {
            variants[index] = new VariantSet(
                    BlockStateCondition.property("facing", facings[index]),
                    new Variant(model, 0F, index * 90F, 0F)
            );
        }
        pack.getBlockStates().put(
                Key.parse("xnet:" + name),
                new BlockState(new Variants(variants, null))
        );
    }
}
