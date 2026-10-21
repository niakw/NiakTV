#!/usr/bin/env python3
"""Verify resolved build-tool dependencies, not merely declared constraints.

Input is produced by ./gradlew buildEnvironment from the Gradle root project.
The -> notation means Gradle selected a version different from the one
requested by a transitive dependency; only the selected version is checked.
"""

import argparse
import re
from pathlib import Path

MINIMUM_VERSIONS = {
    "org.bouncycastle:bcprov-jdk18on": "1.85",
    "org.bouncycastle:bcpkix-jdk18on": "1.85",
    "org.bouncycastle:bcutil-jdk18on": "1.85",
    "org.apache.httpcomponents:httpclient": "4.5.13",
    "org.apache.commons:commons-lang3": "3.18.0",
    "org.jetbrains.kotlin:kotlin-gradle-plugin": "2.4.20",
}


def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))


def check(report: str) -> list[str]:
    # Don't accidentally pass when Gradle did not generate a buildEnvironment graph.
    if "Root project 'NiakTV'" not in report or "\nclasspath\n" not in report:
        return ["Missing root buildEnvironment classpath report"]
    errors = []
    for coordinate, minimum in MINIMUM_VERSIONS.items():
        pattern = re.compile(
            re.escape(coordinate)
            + r":(?P<requested>\d+(?:\.\d+){1,3})"
            + r"(?:\s*->\s*(?P<selected>\d+(?:\.\d+){1,3}))?"
        )
        versions = [m.group("selected") or m.group("requested") for m in pattern.finditer(report)]
        if not versions:
            errors.append(f"{coordinate}: missing from resolved graph (check report format)")
            continue
        insecure = sorted({version for version in versions if version_tuple(version) < version_tuple(minimum)})
        if insecure:
            errors.append(f"{coordinate}: vulnerable resolved version(s): {', '.join(insecure)}; minimum {minimum}")
        else:
            print(f"OK {coordinate}: {', '.join(sorted(set(versions)))} (minimum {minimum})")
    return errors


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path, help="Output from ./gradlew buildEnvironment")
    args = parser.parse_args()
    errors = check(args.report.read_text(encoding="utf-8"))
    if errors:
        for error in errors:
            print(f"ERROR {error}")
        raise SystemExit(1)
    print("All checked Gradle build dependencies resolve to patched versions.")


if __name__ == "__main__":
    main()
