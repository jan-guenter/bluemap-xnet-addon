/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.xnet.activation.AddonRuntime;

/** BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_xnet:antenna"), BlueMap523Adapter::createRenderer
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    Key.parse("bluemap_xnet:exact_profile"),
                    pack -> new ProfileResourceExtension(pack, RENDERER, RUNTIME)
            );
    private static final BlockEntityType FACADE = new BlockEntityType.Impl(
            Key.parse("xnet:facade"), XNetFacadeBlockEntityData.class
    );

    private BlueMap523Adapter() {
    }

    /** Registers only the safe exact-profile probe in the generated seed. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !RegistryGuard.canRegister(BlockEntityType.REGISTRY, FACADE)) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !RegistryGuard.register(BlockEntityType.REGISTRY, FACADE)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        return true;
    }

    private static BlockRenderer createRenderer(
            ResourcePack pack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        try {
            return new XNetRenderer(pack, textures, settings, RUNTIME);
        } catch (RuntimeException exception) {
            RUNTIME.inactive(
                    "renderer-construction-" + exception.getClass().getSimpleName()
            );
            return BlockRendererType.DEFAULT.create(pack, textures, settings);
        }
    }
}
