#!/usr/bin/env python3
"""Draw the angel block texture.

This script is the source of the sprite; the PNG under src/main/resources is its
output and is not edited by hand.

Shape. The tile is a square with an octagonal hole cut out of it, and the
octagon is two numbers: how far the hole reaches straight out from the centre,
and where its diagonal cuts the corner off. At sixteen pixels that yields four
short corner posts joined by single-pixel rails, which is the intent rather
than a by-product - a foothold should look like something you could stand on
the edge of.

The hole is the point. A block placed in mid-air is a block you are looking
through at the drop below it, and one that reads as solid is a worse answer to
the same problem: it hides the thing you are building over. Everything else here
follows from wanting to see past it.

Shading. Tone is read off how deep a pixel sits inside the solid, measured out
from the hole - not off a light in some corner. A cube_all texture is on all six
faces at once, so an upper-left highlight is correct on one of them and wrong on
the rest. Depth is the same from every side. It also does the work for free: the
rails are a single pixel and stay dark, the posts run two deep and come up
bright, so the frame reads as posts-and-rails without either being drawn.

Palette. One base tone, and the ramp is that tone darkened towards the hole.
Off-white and only barely cool because the block is seen against sky and water
and is meant to look temporary - not like a material anyone would finish a
building in. That is a choice, not something read off vanilla, and the base is
the only place it is written down.

No third-party libraries: the PNG is assembled from zlib and struct, both of
which ship with Python.

    python tools/make_textures.py
"""

from __future__ import annotations

import pathlib
import struct
import zlib

SIZE = 16

OUT_DIR = (pathlib.Path(__file__).resolve().parents[1]
           / "src/main/resources/assets/angelus/textures/block")
NAME = "angel_block"

# --- shape -----------------------------------------------------------------
# Half-width of the hole, and where its diagonal cuts. Raising REACH thins the
# rails; lowering CUT fattens the corner posts. Nothing else describes the shape.
REACH = 6.5
CUT = 11.5

# --- colour ----------------------------------------------------------------
BASE = (0xE8, 0xEA, 0xEC)

# Multipliers off BASE, from the lip of the hole outwards. Two, because two is how
# deep the corner posts run at this REACH and CUT - a ramp shorter than the
# deepest run flattens their middles into one tone, and a longer one has entries
# that never get used. BASE is the last step, so the named colour is the outer
# edge of the frame.
DEPTHS = (0.88, 1.00)


def open_cell(x: int, y: int) -> bool:
    """Is this pixel inside the hole?

    Distances are to the centre of the tile, which falls between pixels, so every
    pixel is a half-step off it and no pixel sits on the boundary by accident.
    """
    dx = abs(x - (SIZE - 1) / 2)
    dy = abs(y - (SIZE - 1) / 2)
    return dx <= REACH and dy <= REACH and dx + dy <= CUT


def depth(x: int, y: int, holes: set[tuple[int, int]]) -> int:
    """How many pixels in from the hole this one is, one for the lip itself.

    Chebyshev rather than Euclidean: a corner post's diagonal should count as one
    step in the same way a rail's does, otherwise the four corners come out with a
    ring of intermediate tone that is not describing anything.
    """
    if not holes:
        return len(DEPTHS)
    return min(max(abs(x - hx), abs(y - hy)) for hx, hy in holes)


def _tone(step: int) -> tuple[int, int, int, int]:
    factor = DEPTHS[min(step, len(DEPTHS)) - 1]
    return (*(min(255, round(channel * factor)) for channel in BASE), 255)


# --- output ----------------------------------------------------------------

def _png(pixels: dict[tuple[int, int], tuple[int, int, int, int]]) -> bytes:
    raw = bytearray()
    for y in range(SIZE):
        raw.append(0)  # filter type 0 for the row
        for x in range(SIZE):
            raw.extend(pixels.get((x, y), (0, 0, 0, 0)))

    def chunk(kind: bytes, data: bytes) -> bytes:
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body))

    header = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)  # 8-bit RGBA
    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", header)
        + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        + chunk(b"IEND", b"")
    )


def draw() -> tuple[bytes, str]:
    holes = {(x, y) for y in range(SIZE) for x in range(SIZE) if open_cell(x, y)}
    pixels = {}
    art = []
    for y in range(SIZE):
        row = ""
        for x in range(SIZE):
            if (x, y) in holes:
                row += "."
                continue
            step = depth(x, y, holes)
            pixels[(x, y)] = _tone(step)
            row += str(min(step, len(DEPTHS)))
        art.append(row)
    return _png(pixels), "\n".join(art)


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    data, art = draw()
    path = OUT_DIR / f"{NAME}.png"
    path.write_bytes(data)
    print(art)
    print(f"wrote {path}")


if __name__ == "__main__":
    main()
