/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Narrow BlueNBT projection of XNet's persisted NeoForge facade attachment. */
public final class XNetFacadeBlockEntityData extends MCABlockEntity {

    @NBTName("neoforge:attachments")
    private Attachments attachments;

    public XNetFacadeBlockEntityData() {
    }

    BlockState mimic() {
        return attachments == null || attachments.mimicData == null
                ? null : attachments.mimicData.state;
    }

    /** Exact attachment container written by NeoForge. */
    public static final class Attachments {

        @NBTName("xnet:mimic_data")
        private MimicData mimicData;

        public Attachments() {
        }
    }

    /** Exact XNet attachment payload. */
    public static final class MimicData {

        private BlockState state;

        public MimicData() {
        }
    }
}
