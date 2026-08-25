# XNet visual gallery

This generated gallery places 21 bounded XNet cases plus one stock control. It
covers cable colors and topologies, both connector tiers, persisted brick and
oak-log facades, ordinary machine states, and every supported antenna family.

Keep the generated files synchronized with `cases.py` using these commands:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/xnet-gallery.zip
```

Keep gallery generation deterministic, bounded, synthetic where practical,
and free of candidate assets or captured meshes.
