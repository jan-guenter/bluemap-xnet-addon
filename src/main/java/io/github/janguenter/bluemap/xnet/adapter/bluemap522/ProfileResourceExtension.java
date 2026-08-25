/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.xnet.activation.AddonRuntime;
import io.github.janguenter.bluemap.xnet.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.xnet.profile.XNet707Profile;

import java.nio.file.Path;
import java.util.Set;

/** Exact-artifact admission hook for XNet's custom cable-model loader. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final AddonRuntime runtime;

    ProfileResourceExtension(ResourcePack resourcePack, AddonRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.xnet.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactArtifactDetector.matchesAll(roots, XNet707Profile.ARTIFACTS)) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }

        if (!XNetCableModelInstaller.install(resourcePack)) {
            runtime.inactive("required-installed-resource-missing");
            return;
        }
        runtime.activate();
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return runtime.active() ? XNetCableModelInstaller.requiredTextureKeys() : Set.of();
    }
}
