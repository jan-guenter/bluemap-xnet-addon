/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.xnet.activation.AddonRuntime;
import io.github.janguenter.bluemap.xnet.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.xnet.profile.XNet707Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Exact-artifact admission hook for XNet's custom cable-model loader. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private Map<String, CompiledAntennaModel> antennaModels = Map.of();
    private Set<Key> usedTextures = Set.of();

    ProfileResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.xnet.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        Map<String, Path> artifacts = ExactArtifactDetector.matchAll(
                roots, XNet707Profile.ARTIFACTS
        );
        Path xnetJar = artifacts.get("xnet");
        if (xnetJar == null) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }

        XNetAntennaModels.Catalog antennas;
        try {
            antennas = XNetAntennaModels.load(xnetJar);
        } catch (IOException | RuntimeException exception) {
            runtime.inactive("antenna-resources-" + exception.getClass().getSimpleName());
            return;
        }
        if (!XNetCableModelInstaller.install(resourcePack)) {
            runtime.inactive("required-installed-resource-missing");
            return;
        }
        antennaModels = antennas.models();
        LinkedHashSet<Key> textures = new LinkedHashSet<>(
                XNetCableModelInstaller.requiredTextureKeys()
        );
        textures.addAll(antennas.textures());
        usedTextures = Set.copyOf(textures);
        runtime.activate();
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return runtime.active() ? usedTextures : Set.of();
    }

    @Override
    public void bake() {
        if (!runtime.active()) {
            return;
        }
        if (!XNetAntennaModelInstaller.install(resourcePack, renderer)) {
            runtime.inactive("antenna-blockstate-contract-changed");
            return;
        }
        XNetAntennaRuntime.put(resourcePack, antennaModels);
        System.out.println("BlueMap XNet add-on active: installed cable multipart models and "
                + antennaModels.size() + " exact OBJ antenna models.");
    }
}
