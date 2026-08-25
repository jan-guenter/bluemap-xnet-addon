/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class WavefrontParserTest {

    @Test
    void triangulatesFacesAndAppliesTheAuthoredFlipVContract() throws IOException {
        WavefrontModel model = WavefrontParser.parse(bytes("""
                v 0 0 0
                v 1 0 0
                v 1 1 0
                v 0 1 0
                vt 0 0.25
                vt 1 0.25
                vt 1 0.75
                vt 0 0.75
                usemtl Material
                f 1/1 2/2 3/3 4/4
                """));

        assertEquals(2, model.triangles().size());
        assertEquals(0.75F, model.triangles().getFirst().first().v());
    }

    @Test
    void acceptsOnlyDirectNamespacedMaterialTextures() throws IOException {
        assertEquals(
                "xnet:block/antenna",
                WavefrontParser.parseMaterials(bytes("""
                        newmtl Material
                        map_Kd xnet:block/antenna
                        """)).get("Material")
        );
        assertThrows(IOException.class, () -> WavefrontParser.parseMaterials(bytes("""
                newmtl Material
                map_Kd #alias
                """)));
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
