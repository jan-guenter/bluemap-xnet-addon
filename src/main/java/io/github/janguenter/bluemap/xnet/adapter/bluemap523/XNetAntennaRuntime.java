/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;

import java.util.Map;
import java.util.WeakHashMap;

/** Resource-pack-scoped compiled OBJ storage. */
final class XNetAntennaRuntime {

    private static final Map<ResourcePack, Map<String, CompiledAntennaModel>> MODELS =
            new WeakHashMap<>();

    private XNetAntennaRuntime() {
    }

    static synchronized void put(
            ResourcePack pack,
            Map<String, CompiledAntennaModel> models
    ) {
        MODELS.put(pack, Map.copyOf(models));
    }

    static synchronized Map<String, CompiledAntennaModel> get(ResourcePack pack) {
        return MODELS.getOrDefault(pack, Map.of());
    }
}
