#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated XNet gallery without starting Minecraft."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent
REGISTERED_XNET_BLOCKS = {
    "xnet:advanced_connector",
    "xnet:antenna",
    "xnet:antenna_base",
    "xnet:antenna_dish",
    "xnet:connector",
    "xnet:controller",
    "xnet:facade",
    "xnet:netcable",
    "xnet:redstone_proxy",
    "xnet:redstone_proxy_upd",
    "xnet:router",
    "xnet:wireless_router",
}
REQUIRED_CABLE_CASES = {
    "cable-blue-isolated",
    "cable-red-straight",
    "cable-yellow-corner",
    "cable-green-t-junction",
    "cable-routing-cross",
    "cable-blue-vertical",
    "cable-red-block-ended",
}
LEGAL_CABLE_COLORS = {"blue", "red", "yellow", "green", "routing"}
LEGAL_CONNECTOR_TYPES = {"none", "cable", "block"}
DIRECTION_PROPERTIES = ("north", "south", "east", "west", "up", "down")


def block_id(block_state: str) -> str:
    return re.split(r"[\[{]", block_state, maxsplit=1)[0]


def property_value(block_state: str, name: str) -> str | None:
    match = re.search(rf"(?:\[|,){re.escape(name)}=([^,\]]+)", block_state)
    return match.group(1) if match else None


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")

    if len(cases.PLACEMENTS) != 22:
        raise ValueError("gallery must contain exactly 22 bounded anchors")
    case_ids = [placement.case_id for placement in cases.PLACEMENTS]
    if len(case_ids) != len(set(case_ids)):
        raise ValueError("case IDs must be unique")
    coordinates = [
        (placement.x, placement.y, placement.z) for placement in cases.PLACEMENTS
    ]
    if len(coordinates) != len(set(coordinates)):
        raise ValueError("placement coordinates must be unique")

    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = (
        cases.ENVELOPE
    )
    for placement in cases.PLACEMENTS:
        if not (
            minimum_x <= placement.x <= maximum_x
            and minimum_y <= placement.y <= maximum_y
            and minimum_z <= placement.z <= maximum_z
        ):
            raise ValueError(f"placement escaped envelope: {placement.case_id}")
        if any(character.isspace() for character in placement.block_state):
            raise ValueError(f"block command contains whitespace: {placement.case_id}")

    xnet_ids = {
        block_id(placement.block_state)
        for placement in cases.PLACEMENTS
        if block_id(placement.block_state).startswith("xnet:")
    }
    if xnet_ids != REGISTERED_XNET_BLOCKS:
        raise ValueError(
            "registered XNet block coverage differs: "
            f"missing={sorted(REGISTERED_XNET_BLOCKS - xnet_ids)}, "
            f"extra={sorted(xnet_ids - REGISTERED_XNET_BLOCKS)}"
        )
    if not REQUIRED_CABLE_CASES.issubset(case_ids):
        raise ValueError("required cable topology case is missing")

    cable_family = {
        "xnet:netcable",
        "xnet:connector",
        "xnet:advanced_connector",
        "xnet:facade",
    }
    colors = set()
    for placement in cases.PLACEMENTS:
        if block_id(placement.block_state) not in cable_family:
            continue
        color = property_value(placement.block_state, "color")
        if color not in LEGAL_CABLE_COLORS:
            raise ValueError(f"illegal cable color: {placement.case_id}")
        colors.add(color)
        for direction in DIRECTION_PROPERTIES:
            if property_value(placement.block_state, direction) not in (
                LEGAL_CONNECTOR_TYPES
            ):
                raise ValueError(
                    f"illegal {direction} connector value: {placement.case_id}"
                )
        if property_value(placement.block_state, "waterlogged") != "false":
            raise ValueError(f"gallery cable must be dry: {placement.case_id}")
    if colors != LEGAL_CABLE_COLORS:
        raise ValueError("gallery must cover all five exact cable colors")

    facades = [
        placement
        for placement in cases.PLACEMENTS
        if block_id(placement.block_state) == "xnet:facade"
    ]
    if len(facades) != 2 or any(
        '"neoforge:attachments":{"xnet:mimic_data":{state:{Name:'
        not in row.block_state for row in facades
    ):
        raise ValueError("gallery must contain two persisted facade mimic states")
    if not {"antenna-north", "antenna-south"}.issubset(case_ids):
        raise ValueError("antenna rotation pair is missing")
    if not {"antenna-dish-east", "antenna-dish-west"}.issubset(case_ids):
        raise ValueError("antenna dish rotation pair is missing")
    if "xnet:controller[error=true,facing=north]" not in {
        placement.block_state for placement in cases.PLACEMENTS
    }:
        raise ValueError("controller error-state fixture is missing")

    stock_controls = [
        placement
        for placement in cases.PLACEMENTS
        if placement.expected == "stock-visible"
    ]
    if (
        len(stock_controls) != 1
        or stock_controls[0].block_state != "minecraft:stone"
    ):
        raise ValueError("gallery must retain one honest stone stock control")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != len(
        cases.PLACEMENTS
    ):
        raise ValueError("generated setblock count differs from placement count")
    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden gallery command: {forbidden}")
    print(
        "XNet gallery lint passed: 22 anchors, 12 registered blocks, "
        "five cable colors"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
