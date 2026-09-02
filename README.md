# BlueMap XNet Add-on

A Java 21 BlueMap add-on for the exact `xnet-1.21-7.0.7` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Version `0.1.0-alpha.2` is the unpublished native BlueMap 5.23 migration
candidate. It preserves the owner-accepted `0.1.0-alpha.1` behavior. After exact artifact
admission, it renders XNet cables and connectors from installed textures,
facades from their persisted mimic state, and the three antenna families from
their installed OBJ models.

## Build

Clone with `--recurse-submodules`, or initialize an existing checkout with
`git submodule update --init --recursive -- tooling/bluemap-addon-toolkit \
modules/bluemap-addon-adapter-api`.
The settings preflight accepts only the committed toolkit and Adapter API
gitlinks and rejects an uninitialized, changed, or dirty checkout. The four
Adapter API helpers are compiled as source; its standalone JAR is not bundled.

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PxnetJar=/path/to/xnet-1.21-7.0.7.jar \
  -PrftoolsBaseJar=/path/to/rftoolsbase-1.21-6.0.11.jar \
  clean prototypeCheck build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the generated
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

## Install

Place the production JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock behavior; the add-on
creates no custom world state.

Set `-Dbluemap.xnet.disabled=true` to leave the exact profile inactive.

## Scope boundary

The implemented pass covers five cable colors, six connection directions, the
`none`, `cable`, and `block` connection states, persisted facade mimic blocks,
and all authored rotations of the antenna, antenna base, and antenna dish.
Live contents, activity overlays, particles, and animation phase stay outside
this pass. Invalid facade mimic data uses the neutral installed facade model;
an unsupported artifact profile leaves the whole add-on inactive.

No XNet binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
