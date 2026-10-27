#!/usr/bin/env python3
"""Collect development candidates with hashes; never label them validated releases."""
import hashlib
import json
import shutil
import subprocess
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[1]
out = root / 'build' / 'candidates'
out.mkdir(parents=True, exist_ok=True)
manifest = {'classification': 'development candidates; see docs/STATUS.md',
            'commit': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=root, text=True).strip(),
            'artifacts': []}
for loader in ('fabric', 'neoforge', 'forge'):
    jar = root / loader / 'build' / 'libs' / f'rustify-{loader}-0.1.0.jar'
    if not jar.is_file():
        raise SystemExit(f'Missing build: {jar}')
    with zipfile.ZipFile(jar) as archive:
        natives = sorted(n for n in archive.namelist() if n.startswith('natives/') and not n.endswith('/'))
        if not natives:
            raise SystemExit(f'No packaged native libraries in {jar}; run scripts/build-loader.sh {loader}')
        if 'dev/rustify/Engine.class' not in archive.namelist():
            raise SystemExit(f'Missing shared engine in {jar}')
    target = out / jar.name
    shutil.copyfile(jar, target)
    manifest['artifacts'].append({'loader': loader, 'file': target.name,
                                  'sha256': hashlib.sha256(target.read_bytes()).hexdigest(),
                                  'natives': natives})
(out / 'manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
print(out)
