# BlueMap XNet Add-on

A Java 21 BlueMap add-on for the exact `xnet-1.21-7.0.7` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: first rendering prototype. After exact artifact admission, it replaces
XNet's unsupported cable-model loader for net cables and both connector types
with static multipart models built from the installed XNet textures.

## Build

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the placeholder
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

## Install

After a renderer exists, place the production JAR in BlueMap's add-on pack
directory and restart the BlueMap JVM. Removal plus one restart restores stock
behavior; the add-on creates no custom world state.

Set `-Dbluemap.xnet.disabled=true` to leave the exact profile inactive.

## Scope boundary

The first pass covers five cable colors, six connection directions, and the
`none`, `cable`, and `block` connection states. Facade mimic state and OBJ
antenna models remain stock in this prototype. Live contents, activity
overlays, particles, animation phase, and unsupported states remain stock.

No XNet binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
