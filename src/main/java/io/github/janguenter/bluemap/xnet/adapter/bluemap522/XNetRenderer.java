/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.xnet.activation.AddonRuntime;

import java.util.Map;

/** Renderer for the three exact OBJ-backed XNet antenna blocks. */
final class XNetRenderer implements BlockRenderer {

    private final AddonRuntime runtime;
    private final BlockRenderer fallback;
    private final XNetAntennaEmitter emitter;
    private final Map<String, CompiledAntennaModel> models;

    XNetRenderer(
            ResourcePack pack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.runtime = runtime;
        this.fallback = BlockRendererType.DEFAULT.create(pack, textures, settings);
        this.emitter = new XNetAntennaEmitter(pack, textures, settings);
        this.models = XNetAntennaRuntime.get(pack);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        try {
            String blockId = block.getBlockState().getId().getFormatted();
            if (!runtime.active()
                    || !emitter.emit(models.get(blockId), variant, block, target, mapColor)) {
                fallback.render(block, variant, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (Error error) {
            throwIfFatal(error);
            reset(target, start);
            runtime.inactive("antenna-renderer-" + error.getClass().getSimpleName());
            fallback.render(block, variant, target, mapColor);
        } catch (RuntimeException exception) {
            reset(target, start);
            runtime.inactive("antenna-renderer-" + exception.getClass().getSimpleName());
            fallback.render(block, variant, target, mapColor);
        }
    }

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }

    @SuppressWarnings("removal")
    private static void throwIfFatal(Error error) {
        if (error instanceof OutOfMemoryError outOfMemory) {
            throw outOfMemory;
        }
        if (error instanceof ThreadDeath threadDeath) {
            throw threadDeath;
        }
    }
}
