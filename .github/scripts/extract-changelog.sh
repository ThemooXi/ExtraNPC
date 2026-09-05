#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:?Usage: extract-changelog.sh <version>}"
CHANGELOG="${2:-CHANGELOG.md}"

if [[ ! -f "$CHANGELOG" ]]; then
  echo "Changelog file not found: $CHANGELOG" >&2
  exit 1
fi

# Extract the section for [VERSION] until the next version header or file end.
notes="$(awk -v version="$VERSION" '
  BEGIN { found = 0 }
  /^## \[/ {
    if (found) { exit }
    if ($0 ~ "^## \\[" version "\\]") { found = 1; next }
  }
  found {
    if ($0 ~ /^---$/) { exit }
    print
  }
' "$CHANGELOG")"

if [[ -z "${notes//[[:space:]]/}" ]]; then
  echo "No changelog section found for version ${VERSION}" >&2
  exit 1
fi

{
  echo "## ExtraNPC ${VERSION}"
  echo
  echo "$notes"
} > release-notes.md

cat release-notes.md
