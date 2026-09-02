/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockStateCondition;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Multipart;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Installs static multipart models for the exact XNet cable state properties. */
final class XNetCableModelInstaller {

    private static final String[] COLORS = {"blue", "red", "yellow", "green", "routing"};
    private static final CableDirection[] DIRECTIONS = CableDirection.values();
    private static final Key NETCABLE = Key.parse("xnet:netcable");
    private static final Key CONNECTOR = Key.parse("xnet:connector");
    private static final Key ADVANCED_CONNECTOR = Key.parse("xnet:advanced_connector");
    private static final Key FACADE = Key.parse("xnet:facade");
    private static final Key FACADE_MODEL = Key.parse("bluemap_xnet:block/facade_fallback");
    private static final Key FACADE_TEXTURE = texture("facade");

    private XNetCableModelInstaller() {
    }

    static boolean install(ResourcePack pack) {
        if (!hasRequiredInputs(pack)) {
            return false;
        }

        for (int color = 0; color < COLORS.length; color++) {
            Key cableTexture = texture("cable" + color + "/normal_netcable");
            putModel(pack, modelKey("core", color), box(
                    6.4F, 6.4F, 6.4F, 9.6F, 9.6F, 9.6F, cableTexture
            ));
            for (CableDirection direction : DIRECTIONS) {
                putModel(pack, modelKey("arm_" + direction.property(), color),
                        new Model(arm(direction, false, cableTexture)));
                putModel(pack, modelKey("normal_" + direction.property(), color),
                        endpointModel(direction, cableTexture,
                                texture("cable" + color + "/connector")));
                putModel(pack, modelKey("advanced_" + direction.property(), color),
                        endpointModel(direction, cableTexture,
                                texture("cable" + color + "/advanced_connector")));
            }
        }

        pack.getBlockStates().put(NETCABLE, cableState("normal"));
        pack.getBlockStates().put(CONNECTOR, cableState("normal"));
        pack.getBlockStates().put(ADVANCED_CONNECTOR, cableState("advanced"));
        putModel(pack, FACADE_MODEL, box(0F, 0F, 0F, 16F, 16F, 16F, FACADE_TEXTURE));
        pack.getBlockStates().put(FACADE, singleVariant(FACADE_MODEL));
        return true;
    }

    static boolean routeFacade(ResourcePack pack, BlockRendererType renderer) {
        BlockState state = pack.getBlockStates().get(FACADE);
        if (state == null || state.getMultipart() != null || state.getVariants() == null) {
            return false;
        }
        List<Variant> variants = new ArrayList<>();
        state.forEach(variants::add);
        if (variants.size() != 1 || !FACADE_MODEL.equals(variants.getFirst().getModel())) {
            return false;
        }
        variants.getFirst().setRenderer(renderer);
        return true;
    }

    static Set<Key> requiredTextureKeys() {
        var textures = new HashSet<Key>();
        for (int color = 0; color < COLORS.length; color++) {
            textures.add(texture("cable" + color + "/normal_netcable"));
            textures.add(texture("cable" + color + "/connector"));
            textures.add(texture("cable" + color + "/advanced_connector"));
        }
        textures.add(FACADE_TEXTURE);
        return Set.copyOf(textures);
    }

    private static boolean hasRequiredInputs(ResourcePack pack) {
        return pack.getBlockStates().get(NETCABLE) != null
                && pack.getBlockStates().get(CONNECTOR) != null
                && pack.getBlockStates().get(ADVANCED_CONNECTOR) != null
                && pack.getBlockStates().get(FACADE) != null;
    }

    private static BlockState singleVariant(Key model) {
        return new BlockState(new Variants(
                new VariantSet[0],
                new VariantSet(new Variant(new ResourcePath<Model>(model)))
        ));
    }

    private static BlockState cableState(String endpointKind) {
        List<VariantSet> parts = new ArrayList<>();
        for (int color = 0; color < COLORS.length; color++) {
            BlockStateCondition colorCondition = BlockStateCondition.property(
                    "color", COLORS[color]
            );
            parts.add(part(colorCondition, modelKey("core", color)));
            for (CableDirection direction : DIRECTIONS) {
                parts.add(part(
                        BlockStateCondition.and(
                                colorCondition,
                                BlockStateCondition.property(direction.property(), "cable")
                        ),
                        modelKey("arm_" + direction.property(), color)
                ));
                parts.add(part(
                        BlockStateCondition.and(
                                colorCondition,
                                BlockStateCondition.property(direction.property(), "block")
                        ),
                        modelKey(endpointKind + "_" + direction.property(), color)
                ));
            }
        }
        return new BlockState(new Multipart(parts.toArray(VariantSet[]::new)));
    }

    private static VariantSet part(BlockStateCondition condition, Key model) {
        return new VariantSet(condition, new Variant(new ResourcePath<Model>(model)));
    }

    private static Model endpointModel(
            CableDirection direction,
            Key cableTexture,
            Key connectorTexture
    ) {
        return new Model(
                arm(direction, true, cableTexture),
                plate(direction, connectorTexture)
        );
    }

    private static Element arm(CableDirection direction, boolean endpoint, Key texture) {
        float limit = endpoint ? 14.4F : 16F;
        return switch (direction) {
            case DOWN -> boxElement(6.4F, endpoint ? 1.6F : 0F, 6.4F,
                    9.6F, 6.4F, 9.6F, texture);
            case UP -> boxElement(6.4F, 9.6F, 6.4F,
                    9.6F, limit, 9.6F, texture);
            case NORTH -> boxElement(6.4F, 6.4F, endpoint ? 1.6F : 0F,
                    9.6F, 9.6F, 6.4F, texture);
            case SOUTH -> boxElement(6.4F, 6.4F, 9.6F,
                    9.6F, 9.6F, limit, texture);
            case WEST -> boxElement(endpoint ? 1.6F : 0F, 6.4F, 6.4F,
                    6.4F, 9.6F, 9.6F, texture);
            case EAST -> boxElement(9.6F, 6.4F, 6.4F,
                    limit, 9.6F, 9.6F, texture);
        };
    }

    private static Element plate(CableDirection direction, Key texture) {
        return switch (direction) {
            case DOWN -> boxElement(3.2F, 0F, 3.2F, 12.8F, 1.6F, 12.8F, texture);
            case UP -> boxElement(3.2F, 14.4F, 3.2F, 12.8F, 16F, 12.8F, texture);
            case NORTH -> boxElement(3.2F, 3.2F, 0F, 12.8F, 12.8F, 1.6F, texture);
            case SOUTH -> boxElement(3.2F, 3.2F, 14.4F, 12.8F, 12.8F, 16F, texture);
            case WEST -> boxElement(0F, 3.2F, 3.2F, 1.6F, 12.8F, 12.8F, texture);
            case EAST -> boxElement(14.4F, 3.2F, 3.2F, 16F, 12.8F, 12.8F, texture);
        };
    }

    private static Model box(
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ,
            Key texture
    ) {
        return new Model(boxElement(fromX, fromY, fromZ, toX, toY, toZ, texture));
    }

    private static Element boxElement(
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ,
            Key texture
    ) {
        Map<Direction, Face> faces = new EnumMap<>(Direction.class);
        ResourcePath<Texture> texturePath = new ResourcePath<>(texture);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(new TextureVariable(texturePath)));
        }
        return new Element(
                new Vector3f(fromX, fromY, fromZ),
                new Vector3f(toX, toY, toZ),
                faces
        );
    }

    private static void putModel(ResourcePack pack, Key key, Model model) {
        pack.getModels().put(key, model);
    }

    private static Key modelKey(String part, int color) {
        return Key.parse("bluemap_xnet:block/" + part + '_' + COLORS[color]);
    }

    private static Key texture(String path) {
        return Key.parse("xnet:block/" + path);
    }

    private enum CableDirection {
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST;

        String property() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
