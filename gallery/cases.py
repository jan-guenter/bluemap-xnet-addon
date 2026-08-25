#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Family-owned cases for the bounded XNet visual gallery."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "xnet_gallery"
ENVELOPE = (174, 99, 173, 196, 103, 186)


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str


PLACEMENTS = (
    Placement(
        "cable-blue-isolated",
        "blue network cable, isolated",
        176,
        100,
        175,
        "xnet:netcable[color=blue,north=none,south=none,east=none,west=none,"
        "up=none,down=none,waterlogged=false]",
        "xnet-cable-isolated",
    ),
    Placement(
        "cable-red-straight",
        "red network cable, east-west straight",
        179,
        100,
        175,
        "xnet:netcable[color=red,north=none,south=none,east=cable,west=cable,"
        "up=none,down=none,waterlogged=false]",
        "xnet-cable-straight",
    ),
    Placement(
        "cable-yellow-corner",
        "yellow network cable, north-east corner",
        182,
        100,
        175,
        "xnet:netcable[color=yellow,north=cable,south=none,east=cable,west=none,"
        "up=none,down=none,waterlogged=false]",
        "xnet-cable-corner",
    ),
    Placement(
        "cable-green-t-junction",
        "green network cable, three-way junction",
        185,
        100,
        175,
        "xnet:netcable[color=green,north=cable,south=none,east=cable,west=cable,"
        "up=none,down=none,waterlogged=false]",
        "xnet-cable-three-way",
    ),
    Placement(
        "cable-routing-cross",
        "routing network cable, horizontal cross",
        188,
        100,
        175,
        "xnet:netcable[color=routing,north=cable,south=cable,east=cable,"
        "west=cable,up=none,down=none,waterlogged=false]",
        "xnet-cable-cross",
    ),
    Placement(
        "cable-blue-vertical",
        "blue network cable, vertical straight",
        191,
        100,
        175,
        "xnet:netcable[color=blue,north=none,south=none,east=none,west=none,"
        "up=cable,down=cable,waterlogged=false]",
        "xnet-cable-vertical",
    ),
    Placement(
        "cable-red-block-ended",
        "red network cable, cable arm and block end",
        194,
        100,
        175,
        "xnet:netcable[color=red,north=none,south=none,east=block,west=cable,"
        "up=none,down=none,waterlogged=false]",
        "xnet-cable-block-ended",
    ),
    Placement(
        "connector-normal",
        "normal green connector with block end",
        176,
        100,
        178,
        "xnet:connector[color=green,north=block,south=cable,east=none,west=none,"
        "up=none,down=none,waterlogged=false]",
        "xnet-normal-connector",
    ),
    Placement(
        "connector-advanced",
        "advanced yellow connector with block end",
        179,
        100,
        178,
        "xnet:advanced_connector[color=yellow,north=none,south=none,east=cable,"
        "west=block,up=none,down=none,waterlogged=false]",
        "xnet-advanced-connector",
    ),
    Placement(
        "facade-bricks",
        "blue facade mimicking bricks",
        182,
        100,
        178,
        "xnet:facade[color=blue,north=cable,south=cable,east=none,west=none,"
        "up=none,down=none,waterlogged=false]"
        "{mimic:{Name:\"minecraft:bricks\"}}",
        "xnet-facade-bricks",
    ),
    Placement(
        "facade-oak-log-x",
        "routing facade mimicking an east-west oak log",
        185,
        100,
        178,
        "xnet:facade[color=routing,north=none,south=none,east=cable,west=cable,"
        "up=none,down=none,waterlogged=false]"
        "{mimic:{Name:\"minecraft:oak_log\",Properties:{axis:\"x\"}}}",
        "xnet-facade-oak-log-x",
    ),
    Placement(
        "controller-error-north",
        "controller error face north",
        188,
        100,
        178,
        "xnet:controller[error=true,facing=north]",
        "xnet-error-machine-visible",
    ),
    Placement(
        "router-normal-east",
        "router normal face east",
        191,
        100,
        178,
        "xnet:router[error=false,facing=east]",
        "xnet-static-visible",
    ),
    Placement(
        "wireless-router-normal-west",
        "wireless router normal face west",
        194,
        100,
        178,
        "xnet:wireless_router[error=false,facing=west]",
        "xnet-static-visible",
    ),
    Placement(
        "redstone-proxy",
        "redstone proxy",
        176,
        100,
        181,
        "xnet:redstone_proxy",
        "xnet-static-visible",
    ),
    Placement(
        "redstone-proxy-update",
        "redstone proxy update variant",
        179,
        100,
        181,
        "xnet:redstone_proxy_upd",
        "xnet-static-visible",
    ),
    Placement(
        "antenna-north",
        "antenna face north",
        182,
        100,
        181,
        "xnet:antenna[facing=north]",
        "xnet-obj-visible",
    ),
    Placement(
        "antenna-south",
        "antenna face south",
        185,
        100,
        181,
        "xnet:antenna[facing=south]",
        "xnet-obj-rotated",
    ),
    Placement(
        "antenna-base",
        "antenna base",
        188,
        100,
        181,
        "xnet:antenna_base",
        "xnet-obj-visible",
    ),
    Placement(
        "antenna-dish-east",
        "antenna dish face east",
        191,
        100,
        181,
        "xnet:antenna_dish[facing=east]",
        "xnet-obj-visible",
    ),
    Placement(
        "antenna-dish-west",
        "antenna dish face west",
        194,
        100,
        181,
        "xnet:antenna_dish[facing=west]",
        "xnet-obj-rotated",
    ),
    Placement(
        "stock-control",
        "stone stock rendering control",
        176,
        100,
        184,
        "minecraft:stone",
        "stock-visible",
    ),
)
