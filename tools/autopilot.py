import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OPTIONS = [
    'pauseOnLostFocus:false',
    'onboardAccessibility:false',
    'tutorialStep:none',
    'guiScale:2',
    'skipMultiplayerWarning:true',
    'joinedFirstServer:true',
    'narrator:0',
    'renderClouds:"false"',
    'soundCategory_master:0.0',
]
WORLD = 'Test World'


def run(node, world, timeout):
    game = ROOT / 'versions' / node / 'autopilot-run'
    for stale in ('saves', 'screenshots', 'autopilot'):
        shutil.rmtree(game / stale, ignore_errors=True)
    (game / 'saves').mkdir(parents=True, exist_ok=True)
    shutil.copytree(world, game / 'saves' / WORLD)
    (game / 'options.txt').write_text('\n'.join(OPTIONS) + '\n', encoding='utf-8')

    gradlew = ROOT / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
    log = game / 'autopilot.log'
    with open(log, 'w', encoding='utf-8', errors='replace') as out:
        try:
            subprocess.run([str(gradlew), '--console=plain', f':{node}:runAutopilot', f'-PautopilotWorld={WORLD}'],
                           cwd=ROOT, stdout=out, stderr=subprocess.STDOUT, timeout=timeout)
        except subprocess.TimeoutExpired:
            print(f'{node}: no result within {timeout} s; see {log}')
            return False

    result = game / 'autopilot' / 'result.txt'
    if not result.exists():
        print(f'{node}: the client wrote no result; see {log}')
        return False
    lines = result.read_text(encoding='utf-8').splitlines()
    for line in lines:
        print(f'{node}: {line}')
    for shot in sorted((game / 'screenshots').glob('autopilot-*.png')):
        print(f'{node}: screenshot {shot}')
    return bool(lines) and lines[-1] == 'RESULT PASS'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('nodes', nargs='+')
    parser.add_argument('--world', required=True, help='a saved world to copy in, one per Minecraft version is safest')
    parser.add_argument('--timeout', type=int, default=600)
    args = parser.parse_args()
    passed = [run(node, Path(args.world), args.timeout) for node in args.nodes]
    sys.exit(0 if all(passed) else 1)


if __name__ == '__main__':
    main()
