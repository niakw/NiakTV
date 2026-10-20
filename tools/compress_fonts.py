#!/usr/bin/env python3
"""Rebuild lossless WOFF2 assets. Requires fonttools[woff]==4.60.1.

Sources and OFL notices are retained; no subsetting, hint removal or weight instancing.
"""
from pathlib import Path
from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets/fonts"
for name in ("manrope", "instrument-serif"):
    source = ROOT / "tools/font-sources" / f"{name}.ttf"
    output = ASSETS / f"{name}.woff2"
    original = TTFont(source, recalcTimestamp=False)
    original.flavor = "woff2"
    original.save(output, reorderTables=False)
    decoded = TTFont(output, recalcTimestamp=False)
    assert original.getBestCmap() == decoded.getBestCmap(), "Character mapping changed"
    assert original.getGlyphOrder() == decoded.getGlyphOrder(), "Glyph order changed"
    assert original["hmtx"].metrics == decoded["hmtx"].metrics, "Glyph metrics changed"
    if "fvar" in original:
        assert [(a.axisTag, a.minValue, a.defaultValue, a.maxValue) for a in original["fvar"].axes] == [
            (a.axisTag, a.minValue, a.defaultValue, a.maxValue) for a in decoded["fvar"].axes
        ], "Variable font axes changed"
    print(f"{name}: {source.stat().st_size:,} -> {output.stat().st_size:,} bytes; "
          f"{len(decoded.getBestCmap())} characters preserved")
