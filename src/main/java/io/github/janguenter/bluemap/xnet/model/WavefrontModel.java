/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.model;

import java.util.List;

/** Immutable textured triangles read from an installed XNet OBJ. */
public record WavefrontModel(List<Triangle> triangles) {

    public WavefrontModel {
        triangles = List.copyOf(triangles);
    }

    /** One textured triangle and its source MTL material. */
    public record Triangle(Vertex first, Vertex second, Vertex third, String material) {
    }

    /** Direct block-space position and normalized UV coordinates. */
    public record Vertex(float x, float y, float z, float u, float v) {
    }
}
