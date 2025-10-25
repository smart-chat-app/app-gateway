#!/usr/bin/env bash
set -euo pipefail

# Determine project root based on script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

SETTINGS_FILE="${MAVEN_SETTINGS:-$PROJECT_ROOT/scripts/maven-settings.xml}"
MVN_ARGS=()

if [[ -f "$SETTINGS_FILE" ]]; then
  MVN_ARGS=(-s "$SETTINGS_FILE")
fi

if command -v mvn >/dev/null 2>&1; then
  MVN_CMD=(mvn "${MVN_ARGS[@]}")
elif [[ -x "$PROJECT_ROOT/mvnw" ]]; then
  MVN_CMD=("$PROJECT_ROOT/mvnw" "${MVN_ARGS[@]}")
else
  echo "Error: Maven is not installed and Maven wrapper is unavailable." >&2
  exit 1
fi

exec "${MVN_CMD[@]}" -B -ntp clean test "$@"
