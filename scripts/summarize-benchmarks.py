#!/usr/bin/env python3
"""Summarize recorded samples without asserting a cross-JVM speedup."""
import json
import re
import statistics
from pathlib import Path

root = Path(__file__).resolve().parents[1]
reports = []
for path in sorted((root / 'benchmarks').glob('java-*.txt')):
    groups = {}
    for line in path.read_text().splitlines():
        match = re.fullmatch(r'measured (.+) ns=(\d+)', line)
        if match:
            groups.setdefault(match[1], []).append(int(match[2]))
    for name, samples in groups.items():
        reports.append(dict(source=path.name, experiment=name, result_type='measured',
                            samples=len(samples), median_ns=statistics.median(samples),
                            min_ns=min(samples), max_ns=max(samples),
                            population_stdev_ns=statistics.pstdev(samples)))
print(json.dumps(dict(methodology='Separate JVMs; cold samples included; no speedup conclusion',
                      reports=reports), indent=2))
