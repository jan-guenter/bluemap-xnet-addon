# Add-on execution

This repository stays inactive and stock-safe unless the exact supported XNet
and RFTools Base artifacts are present. Keep new work bounded to observed XNet
rendering defects.

Before running Gradle gates, activate a Python 3.11 or newer virtual
environment, initialize the pinned toolkit submodule, and install the exact
development-only toolkit into the environment:

```bash
git submodule update --init --recursive -- tooling/bluemap-addon-toolkit
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
```

The requirement locks the 19,827-byte `v0.2.0-alpha.1` wheel at SHA-256
`cbfbad7ea12ea631b9f36a5261482dde3ca4d8f270df1b5faf75310020b115f9`.

## Prototype

Acquire and verify the exact candidate JARs outside Git. Their Gradle
properties are:

- `-PxnetJar=/path/to/xnet-1.21-7.0.7.jar`
- `-PrftoolsBaseJar=/path/to/rftoolsbase-1.21-6.0.11.jar`

Then run:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> clean prototypeCheck build
bash gallery/package.sh /tmp/xnet-gallery.zip
```

Deploy that JAR and gallery only to disposable staging, verify the intended
BlueMap link loads, and compare it with the matching client. Iterate from
observed defects until the owner explicitly accepts one exact staging JAR.

## Acceptance and release

Freeze that accepted JAR's functional entries once; the writer refuses to
overwrite an existing acceptance record:

```bash
bluemap-addon-toolkit jar-entries write \
  --jar /absolute/path/accepted-staging.jar \
  --entries provenance/accepted-staging-entries.sha256
```

Record the manifest in `provenance/release.json` as
`accepted_staging_entries` with exact `path`, `entry_count`, and `sha256`.
Record `visual_acceptance: true` under `owner_accepted_staging`, and record the
production JAR, sources JAR, POM and Gradle module file names, sizes and hashes
under `final_release_artifacts`.

Promote `addon_version` through a pull request, remove every scaffold
implementation marker, and run with all exact candidate properties:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> -PreleaseTag=v<version> \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after final-version CI passes this gate. Create an annotated
`v<version>` tag at reviewed `main`; the release workflow independently checks
the tag, exact BlueMap checkout, accepted bytes and draft assets before making
the prerelease public. Publication never deploys to production.
