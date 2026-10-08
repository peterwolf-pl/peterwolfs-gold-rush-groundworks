#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

if command -v gradle >/dev/null 2>&1; then
  exec gradle -p "$ROOT_DIR" "$@"
fi

SCREEN_WRAPPER="${GROUNDWORKS_SCREEN_DIR:-$ROOT_DIR/../Peterwolfs-Groundworks-excavated-material-gradation-screen}/gradlew"
if [ -x "$SCREEN_WRAPPER" ]; then
  exec "$SCREEN_WRAPPER" -p "$ROOT_DIR" "$@"
fi

echo "Gradle not found. Install Gradle 9.5.1 or keep Peterwolfs-Groundworks-excavated-material-gradation-screen next to this repository." >&2
exit 1
