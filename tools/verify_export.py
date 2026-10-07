"""Verify the public API boundary, provenance, and distributable JAR contents."""
import argparse
import hashlib
import json
import re
import urllib.request
from io import BytesIO
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'me/therealaqz/deluxegradient/api/'
TOKENS = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|/\*.*?\*/|//[^\n]*|\s+|[^\s]', re.S)


def code_hash(source):
    parts = [m.group() for m in TOKENS.finditer(source)
             if not m.group().isspace() and not m.group().startswith(('/*', '//'))]
    return hashlib.sha256(''.join(parts).encode()).hexdigest()


def verify(check_upstream=False):
    provenance = json.loads((ROOT / 'docs/provenance.json').read_text(encoding='utf-8'))
    expected = provenance['files']
    allowed = re.compile(re.escape(PREFIX) + r'(DeluxeGradientApi\.java|DeluxeGradientProvider\.java|Slot\.java|color/[^/]+\.java|event/[^/]+\.java|text/Decoration\.java|text/tree/[^/]+\.java)$')
    if not all(allowed.fullmatch(name) for name in expected):
        raise ValueError('Manifest includes a file outside the supported API boundary')
    source_root = ROOT / 'src/main/java'
    sources = {p.relative_to(source_root).as_posix(): p for p in source_root.rglob('*') if p.is_file()}
    if set(sources) != set(expected):
        raise ValueError('Production file set differs from the public-source manifest')
    for name, path in sources.items():
        source = path.read_text(encoding='utf-8')
        if code_hash(source) != expected[name]['java_tokens_sha256']:
            raise ValueError('Executable Java tokens differ from the upstream snapshot: ' + name)
        if 'DESIGN.md' in source or ':core' in source:
            raise ValueError('Private architecture reference: ' + name)

    if check_upstream:
        with urllib.request.urlopen(provenance['source_url'], timeout=30) as response:
            payload = response.read()
        if hashlib.sha256(payload).hexdigest() != provenance['source_jar_sha256']:
            raise ValueError('Upstream sources JAR checksum mismatch')
        with ZipFile(BytesIO(payload)) as upstream:
            for name, hashes in expected.items():
                content = upstream.read(name)
                if hashlib.sha256(content).hexdigest() != hashes['upstream_sha256']:
                    raise ValueError('Upstream source checksum mismatch: ' + name)
                if code_hash(content.decode('utf-8')) != hashes['java_tokens_sha256']:
                    raise ValueError('Upstream executable-token checksum mismatch: ' + name)

    target = ROOT / 'target'
    base = 'deluxegradient-api-contracts-1.0.0-SNAPSHOT'
    with ZipFile(target / (base + '.jar')) as jar:
        if jar.read('META-INF/LICENSE') != (ROOT / 'LICENSE').read_bytes():
            raise ValueError('Runtime JAR license is missing or differs from LICENSE')
        class_names = set()
        for name in jar.namelist():
            if name.endswith('/') or name.startswith('META-INF/'):
                continue
            if not name.endswith('.class'):
                raise ValueError('Unexpected runtime JAR resource: ' + name)
            outer = name.split('$', 1)[0].removesuffix('.class') + '.java'
            if outer not in expected:
                raise ValueError('Unexpected class outside the public API set: ' + name)
            content = jar.read(name)
            if content[:4] != b'\xca\xfe\xba\xbe' or int.from_bytes(content[6:8], 'big') != 52:
                raise ValueError('Expected Java 8 class: ' + name)
            class_names.add(outer)
        if class_names != set(expected):
            raise ValueError('Compiled JAR is missing an exported API type')
    with ZipFile(target / (base + '-sources.jar')) as jar:
        names = {n for n in jar.namelist() if not n.endswith('/') and not n.startswith('META-INF/')}
        if names != set(expected):
            raise ValueError('Sources JAR contains missing or unexpected files')
        for name in names:
            if jar.read(name) != sources[name].read_bytes():
                raise ValueError('Packaged source differs from the working source: ' + name)
    if not (target / (base + '-javadoc.jar')).is_file():
        raise ValueError('Javadoc JAR is missing')
    print(f'Verified {len(expected)} public source files, unchanged Java tokens, Java 8 classes, and API-only JAR contents.')
    if check_upstream:
        print('Verified upstream Maven Central JAR and individual source checksums.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check-upstream', action='store_true')
    args = parser.parse_args()
    verify(args.check_upstream)
