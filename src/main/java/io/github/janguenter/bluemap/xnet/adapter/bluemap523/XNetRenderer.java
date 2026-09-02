/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.xnet.activation.AddonRuntime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Renderer for persisted facades and the three exact OBJ-backed antenna blocks. */
final class XNetRenderer implements BlockRenderer {

    private static final String FACADE = "xnet:facade";
    private static final int MAX_MIMIC_VARIANTS = 64;

    private final ResourcePack resourcePack;
    private final AddonRuntime runtime;
    private final BlockRenderer fallback;
    private final BlockStateModelRenderer stateRenderer;
    private final XNetAntennaEmitter emitter;
    private final Map<String, CompiledAntennaModel> models;

    XNetRenderer(
            ResourcePack pack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = pack;
        this.runtime = runtime;
        this.fallback = BlockRendererType.DEFAULT.create(pack, textures, settings);
        this.stateRenderer = new BlockStateModelRenderer(pack, textures, settings);
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
            if (!runtime.active()) {
                fallback.render(block, variant, target, mapColor);
            } else if (FACADE.equals(blockId)) {
                renderFacade(block, variant, target, mapColor);
            } else if (!emitter.emit(models.get(blockId), variant, block, target, mapColor)) {
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

    private void renderFacade(
            BlockNeighborhood block,
            Variant fallbackVariant,
            TileModelView target,
            Color mapColor
    ) {
        XNetFacadeBlockEntityData data = block.getBlockEntity()
                instanceof XNetFacadeBlockEntityData found ? found : null;
        BlockState mimic = data == null ? null : data.mimic();
        if (data == null
                || data.getId() == null
                || !FACADE.equals(data.getId().getFormatted())
                || !ordinaryMimic(block, mimic)) {
            fallback.render(block, fallbackVariant, target, mapColor);
            return;
        }
        stateRenderer.render(block, mimic, target, mapColor);
    }

    private boolean ordinaryMimic(BlockNeighborhood block, BlockState mimic) {
        if (mimic == null || mimic.isAir() || mimic.isWaterlogged()) {
            return false;
        }
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(mimic.getId());
        if (state == null || resourcePack.getBlockState(mimic) != state) {
            return false;
        }
        List<Variant> variants = new ArrayList<>();
        state.forEach(mimic, block.getX(), block.getY(), block.getZ(), variants::add);
        if (variants.isEmpty() || variants.size() > MAX_MIMIC_VARIANTS) {
            return false;
        }
        return variants.stream().allMatch(variant ->
                variant.getRenderer() == BlockRendererType.DEFAULT
                        && !ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                        && ordinaryModel(resourcePack.getModels().get(variant.getModel()))
        );
    }

    private static boolean ordinaryModel(Model model) {
        if (model == null || model.getElements() == null || model.getElements().length == 0) {
            return false;
        }
        for (Element element : model.getElements()) {
            if (element.getFaces().values().stream().anyMatch(face -> face.getTintindex() >= 0)) {
                return false;
            }
        }
        return true;
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
