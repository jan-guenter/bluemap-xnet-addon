/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Routes the exact XNet antenna variants through the frozen OBJ renderer. */
final class XNetAntennaModelInstaller {

    private static final Map<Key, Route> ROUTES = Map.of(
            Key.parse("xnet:antenna"), new Route(Key.parse("xnet:block/antenna"), 4),
            Key.parse("xnet:antenna_base"),
                    new Route(Key.parse("xnet:block/antenna_base"), 1),
            Key.parse("xnet:antenna_dish"),
                    new Route(Key.parse("xnet:block/antenna_dish"), 4)
    );

    private XNetAntennaModelInstaller() {
    }

    static boolean install(ResourcePack pack, BlockRendererType renderer) {
        List<Variant> variants = new ArrayList<>();
        for (Map.Entry<Key, Route> entry : ROUTES.entrySet()) {
            var state = pack.getBlockStates().get(entry.getKey());
            if (state == null) {
                return false;
            }
            List<Variant> routed = new ArrayList<>();
            state.forEach(routed::add);
            Route route = entry.getValue();
            if (routed.size() != route.variantCount()
                    || routed.stream().anyMatch(
                            variant -> !route.model().equals(variant.getModel())
                    )) {
                return false;
            }
            variants.addAll(routed);
        }
        variants.forEach(variant -> variant.setRenderer(renderer));
        return true;
    }

    private record Route(Key model, int variantCount) {
    }
}
