/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.xnet.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.xnet.model.WavefrontModel.Vertex;

import java.util.LinkedHashMap;
import java.util.Map;

/** Emits one frozen XNet OBJ with its authored blockstate rotation. */
final class XNetAntennaEmitter {

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;

    XNetAntennaEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean emit(
            CompiledAntennaModel installed,
            Variant orientation,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (installed == null) {
            return false;
        }
        Map<String, Material> materials = resolve(installed.materials());
        if (materials.size() != installed.materials().size()) {
            return false;
        }
        int start = target.getTileModel().size();
        for (Triangle triangle : installed.model().triangles()) {
            Direction direction = nearestDirection(triangle);
            if (settings.isRenderTopOnly() && direction != Direction.UP) {
                continue;
            }
            var normal = direction.toVector();
            LightData own = block.getLightData();
            LightData faced = block.getNeighborBlock(
                    normal.getX(), normal.getY(), normal.getZ()
            ).getLightData();
            int sunlight = Math.max(own.getSkyLight(), faced.getSkyLight());
            int blocklight = Math.max(own.getBlockLight(), faced.getBlockLight());
            int visible = settings.isCaveDetectionUsesBlockLight()
                    ? Math.max(sunlight, blocklight) : sunlight;
            if (block.isRemoveIfCave() && visible == 0) {
                continue;
            }
            Material material = materials.get(triangle.material());
            if (material == null) {
                return false;
            }
            int index = target.add(1);
            TileModel mesh = target.getTileModel();
            positions(mesh, index, triangle);
            uvs(mesh, index, triangle);
            mesh.setMaterialIndex(index, material.index());
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, sunlight);
            mesh.setBlocklight(index, blocklight);
        }
        if (target.getTileModel().size() == start) {
            return false;
        }
        if (orientation.isTransformed()) {
            target.initialize(start).transform(orientation.getTransformMatrix());
        } else {
            target.initialize(start);
        }
        materials.values().stream().map(Material::texture).distinct().forEach(texture ->
                mapColor.add(new Color().set(texture.getColorPremultiplied()))
        );
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return true;
    }

    private Map<String, Material> resolve(Map<String, Key> mappings) {
        Map<String, Material> result = new LinkedHashMap<>();
        mappings.forEach((name, path) -> {
            Texture texture = resourcePack.getTextures().get(path);
            if (texture != null) {
                result.put(name, new Material(textures.get(path), texture));
            }
        });
        return result;
    }

    private static Direction nearestDirection(Triangle triangle) {
        Vertex first = triangle.first();
        Vertex second = triangle.second();
        Vertex third = triangle.third();
        float abx = second.x() - first.x();
        float aby = second.y() - first.y();
        float abz = second.z() - first.z();
        float acx = third.x() - first.x();
        float acy = third.y() - first.y();
        float acz = third.z() - first.z();
        float x = aby * acz - abz * acy;
        float y = abz * acx - abx * acz;
        float z = abx * acy - aby * acx;
        float absoluteX = Math.abs(x);
        float absoluteY = Math.abs(y);
        float absoluteZ = Math.abs(z);
        if (absoluteY >= absoluteX && absoluteY >= absoluteZ) {
            return y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (absoluteX >= absoluteZ) {
            return x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void positions(TileModel mesh, int index, Triangle triangle) {
        Vertex first = triangle.first();
        Vertex second = triangle.second();
        Vertex third = triangle.third();
        mesh.setPositions(
                index,
                first.x(), first.y(), first.z(),
                second.x(), second.y(), second.z(),
                third.x(), third.y(), third.z()
        );
    }

    private static void uvs(TileModel mesh, int index, Triangle triangle) {
        Vertex first = triangle.first();
        Vertex second = triangle.second();
        Vertex third = triangle.third();
        mesh.setUvs(index, first.u(), first.v(), second.u(), second.v(), third.u(), third.v());
    }

    private record Material(int index, Texture texture) {
    }
}
