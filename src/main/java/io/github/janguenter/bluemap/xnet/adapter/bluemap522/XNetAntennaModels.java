/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.xnet.model.WavefrontModel;
import io.github.janguenter.bluemap.xnet.model.WavefrontParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Compiles the three OBJ models from the admitted XNet JAR. */
final class XNetAntennaModels {

    private static final int MAX_RESOURCE_BYTES = 4 * 1024 * 1024;
    private static final Map<String, String> ROUTES = Map.of(
            "xnet:antenna", "antenna",
            "xnet:antenna_base", "antenna_base",
            "xnet:antenna_dish", "antenna_dish"
    );

    private XNetAntennaModels() {
    }

    static Catalog load(Path xnetJar) throws IOException {
        Map<String, CompiledAntennaModel> models = new LinkedHashMap<>();
        Set<Key> textures = new LinkedHashSet<>();
        try (ZipFile zip = new ZipFile(xnetJar.toFile())) {
            for (Map.Entry<String, String> route : ROUTES.entrySet()) {
                String root = "assets/xnet/models/block/" + route.getValue();
                WavefrontModel geometry = WavefrontParser.parse(read(zip, root + ".obj"));
                Map<String, String> rawMaterials = WavefrontParser.parseMaterials(
                        read(zip, root + ".mtl")
                );
                Map<String, Key> materials = new LinkedHashMap<>();
                for (Map.Entry<String, String> material : rawMaterials.entrySet()) {
                    Key texture = Key.parse(material.getValue());
                    materials.put(material.getKey(), texture);
                    textures.add(texture);
                }
                if (geometry.triangles().stream().anyMatch(
                        triangle -> !materials.containsKey(triangle.material())
                )) {
                    throw new IOException("OBJ face references unmapped material");
                }
                models.put(route.getKey(), new CompiledAntennaModel(
                        geometry, materials
                ));
            }
        }
        if (models.size() != ROUTES.size()
                || !textures.equals(Set.of(
                        Key.parse("xnet:block/antenna"),
                        Key.parse("xnet:block/antenna_dish")
                ))) {
            throw new IOException("installed antenna resource contract changed");
        }
        return new Catalog(models, textures);
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory()
                || entry.getSize() > MAX_RESOURCE_BYTES) {
            throw new IOException("missing or oversized installed resource " + path);
        }
        try (InputStream input = zip.getInputStream(entry)) {
            byte[] bytes = input.readNBytes(MAX_RESOURCE_BYTES + 1);
            if (bytes.length > MAX_RESOURCE_BYTES) {
                throw new IOException("oversized installed resource " + path);
            }
            return bytes;
        }
    }

    /** Fully parsed models and their texture roster. */
    record Catalog(Map<String, CompiledAntennaModel> models, Set<Key> textures) {

        Catalog {
            models = Map.copyOf(models);
            textures = Set.copyOf(textures);
        }
    }
}
