/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

class XNetFacadeBlockEntityDataTest {

    @Test
    void readsTheExactPersistentAttachmentBlockState() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("id").value("xnet:facade");
            writer.name("neoforge:attachments").beginCompound();
            writer.name("xnet:mimic_data").beginCompound();
            writer.name("state").beginCompound();
            writer.name("Name").value("minecraft:oak_log");
            writer.name("Properties").beginCompound();
            writer.name("axis").value("x");
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
        }

        XNetFacadeBlockEntityData data = MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(bytes.toByteArray()),
                XNetFacadeBlockEntityData.class
        );

        assertEquals("xnet:facade", data.getId().getFormatted());
        assertEquals("minecraft:oak_log", data.mimic().getId().getFormatted());
        assertEquals("x", data.mimic().getProperties().get("axis"));
    }
}
