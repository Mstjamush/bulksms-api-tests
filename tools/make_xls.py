"""Writes a minimal legacy Excel (.xls, BIFF8 in an OLE2 compound file) with one
sheet of text and number cells - enough for xlrd, for test fixtures, without
installing a writer library. Usage: python3 tools/make_xls.py out.xls < rows.csv  (regenerates testdata/loans.xls)."""

import csv
import struct
import sys


def rec(rtype: int, data: bytes) -> bytes:
    return struct.pack("<HH", rtype, len(data)) + data


def bof(dt: int) -> bytes:
    return rec(0x0809, struct.pack("<HHHHII", 0x0600, dt, 0x0DBB, 0x07CC, 0, 6))


def label(r: int, c: int, text: str) -> bytes:
    try:
        raw, flag = text.encode("latin-1"), 0
    except UnicodeEncodeError:
        raw, flag = text.encode("utf-16-le"), 1
    return rec(0x0204, struct.pack("<HHHHB", r, c, 15, len(text), flag) + raw)


def number(r: int, c: int, value: float) -> bytes:
    return rec(0x0203, struct.pack("<HHHd", r, c, 15, value))


def font() -> bytes:
    name = "Arial"
    return rec(0x0031, struct.pack("<HHHHHBBBB", 200, 0, 0x7FFF, 400, 0, 0, 0, 0, 0)
               + struct.pack("<BB", len(name), 0) + name.encode())


def xf(style: bool) -> bytes:
    # font 0, format 0 (General), style/cell XF flags; the rest default.
    return rec(0x00E0, struct.pack("<HHHBBBBIIH", 0, 0, 0xFFF5 if style else 0x0001, 0x20, 0, 0, 0, 0, 0, 0x20C0))


def workbook(rows: list[list[str]]) -> bytes:
    sheet_name = "Sheet1"
    globals_ = [bof(0x0005), rec(0x0042, struct.pack("<H", 1200))]
    globals_ += [font() for _ in range(5)]
    globals_ += [xf(True) for _ in range(15)] + [xf(False)]
    boundsheet_at = len(b"".join(globals_))
    globals_.append(rec(0x0085, struct.pack("<IBB", 0, 0, 0) + struct.pack("<BB", len(sheet_name), 0) + sheet_name.encode()))
    globals_.append(rec(0x000A, b""))
    g = b"".join(globals_)

    cells = []
    ncols = max((len(r) for r in rows), default=0)
    for ri, row in enumerate(rows):
        for ci, value in enumerate(row):
            if value == "":
                continue
            if ri > 0 and value.isdigit():
                cells.append(number(ri, ci, float(value)))
            else:
                cells.append(label(ri, ci, value))
    sheet = bof(0x0010) + rec(0x0200, struct.pack("<IIHHH", 0, len(rows), 0, ncols, 0)) + b"".join(cells) + rec(0x000A, b"")

    # patch BOUNDSHEET's absolute stream offset of the sheet BOF
    g = g[:boundsheet_at + 4] + struct.pack("<I", len(g)) + g[boundsheet_at + 8:]
    stream = g + sheet
    return stream + b"\x00" * max(0, 4096 - len(stream))  # >= 4096: a regular (not mini) stream


def compound_file(stream: bytes) -> bytes:
    sector = 512
    n = -(-len(stream) // sector)
    fat = [0xFFFFFFFD, 0xFFFFFFFE] + [3 + i for i in range(n - 1)] + [0xFFFFFFFE]
    fat += [0xFFFFFFFF] * (128 - len(fat))
    header = (b"\xD0\xCF\x11\xE0\xA1\xB1\x1A\xE1" + b"\x00" * 16
              + struct.pack("<HHHHH", 0x3E, 3, 0xFFFE, 9, 6) + b"\x00" * 6
              + struct.pack("<IIIIIIIII", 0, 1, 1, 0, 4096, 0xFFFFFFFE, 0, 0xFFFFFFFE, 0)
              + struct.pack("<I", 0) + struct.pack("<I", 0xFFFFFFFF) * 108)

    def entry(name: str, etype: int, child: int, start: int, size: int) -> bytes:
        encoded = (name + "\x00").encode("utf-16-le") if name else b""
        return (encoded.ljust(64, b"\x00") + struct.pack("<HBB", len(encoded), etype, 1)
                + struct.pack("<III", 0xFFFFFFFF, 0xFFFFFFFF, child) + b"\x00" * 16 + b"\x00" * 4
                + b"\x00" * 16 + struct.pack("<III", start, size, 0))

    directory = (entry("Root Entry", 5, 1, 0xFFFFFFFE, 0) + entry("Workbook", 2, 0xFFFFFFFF, 2, len(stream))
                 + entry("", 0, 0xFFFFFFFF, 0, 0) * 2)
    return header + struct.pack("<128I", *fat) + directory + stream.ljust(n * sector, b"\x00")


if __name__ == "__main__":
    rows = list(csv.reader(sys.stdin))
    with open(sys.argv[1], "wb") as f:
        f.write(compound_file(workbook(rows)))
