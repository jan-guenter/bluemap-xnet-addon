/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistrationPlan;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueMap523RegistrationPlanTest {

    @Test
    void preservesRendererExtensionAndFacadeRegistrationOrder() {
        List<String> order = new ArrayList<>();
        RecordingRegistry<BlockRendererType> renderers =
                new RecordingRegistry<>(order, "renderer");
        RecordingRegistry<ResourcePack.Extension<?>> extensions =
                new RecordingRegistry<>(order, "extension");
        RecordingRegistry<BlockEntityType> blockEntities =
                new RecordingRegistry<>(order, "facade");
        RegistrationPlan plan = BlueMap523Adapter.registrationPlan(
                renderers,
                extensions,
                blockEntities
        );

        assertTrue(plan.canApply());
        assertTrue(plan.apply());
        assertEquals(List.of("renderer", "extension", "facade"), order);
        assertEquals("bluemap_xnet:antenna", onlyKey(renderers).getFormatted());
        assertEquals("bluemap_xnet:exact_profile", onlyKey(extensions).getFormatted());
        assertEquals("xnet:facade", onlyKey(blockEntities).getFormatted());
        assertTrue(plan.apply());
        assertEquals(List.of("renderer", "extension", "facade"), order);
    }

    @Test
    void collisionPreflightRetainsTheExistingOwnerAndMutatesNothingElse() {
        Registry<BlockRendererType> renderers = new Registry<>();
        Registry<ResourcePack.Extension<?>> extensions = new Registry<>();
        Registry<BlockEntityType> blockEntities = new Registry<>();
        BlockRendererType collision = new BlockRendererType.Impl(
                Key.parse("bluemap_xnet:antenna"),
                BlockRendererType.DEFAULT::create
        );
        renderers.register(collision);
        RegistrationPlan plan = BlueMap523Adapter.registrationPlan(
                renderers,
                extensions,
                blockEntities
        );

        assertFalse(plan.canApply());
        assertFalse(plan.apply());
        assertSame(collision, renderers.get(collision.getKey()));
        assertNull(extensions.get(Key.parse("bluemap_xnet:exact_profile")));
        assertNull(blockEntities.get(Key.parse("xnet:facade")));
    }

    private static Key onlyKey(Registry<? extends Keyed> registry) {
        assertEquals(1, registry.keys().size());
        return registry.keys().iterator().next();
    }

    private static final class RecordingRegistry<T extends Keyed> extends Registry<T> {

        private final List<String> order;
        private final String label;

        private RecordingRegistry(List<String> order, String label) {
            this.order = order;
            this.label = label;
        }

        @Override
        public boolean register(T entry) {
            order.add(label);
            return super.register(entry);
        }
    }
}
