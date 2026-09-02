/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.xnet.model.WavefrontModel;

import java.util.Map;

/** Parsed installed OBJ plus its direct material texture locations. */
record CompiledAntennaModel(WavefrontModel model, Map<String, Key> materials) {

    CompiledAntennaModel {
        materials = Map.copyOf(materials);
    }
}
