/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.xnet.profile;

import java.util.List;

/** Exact All the Mons 1.2.0 profile `xnet-1.21-7.0.7`. */
public final class XNet707Profile {

    public static final String PROFILE_ID = "xnet-1.21-7.0.7";
    public static final List<ArtifactPin> ARTIFACTS = List.of(
            new ArtifactPin(
                    "xnet",
                    "xnet",
                    "1.21-7.0.7",
                    "xnet-1.21-7.0.7.jar",
                    611_577L,
                    "0f393a4bff91a90e0665ec6d66ff649e45f9c33311dc038e4a5df0b154ea9d80"
            ),
            new ArtifactPin(
                    "rftoolsbase",
                    "rftoolsbase",
                    "1.21-6.0.11",
                    "rftoolsbase-1.21-6.0.11.jar",
                    463_973L,
                    "5195ba530e6cf9ba61c9954a3297679e6d29aa1b6182e27ae14ea43463dd4b00"
            )
    );

    private XNet707Profile() {
    }
}
