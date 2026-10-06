#!/usr/bin/env bash
# Avvio dell'Authoritative Server di Agar.io
# Uso:
#   ./run-server.sh           (modalità headless)
#   ./run-server.sh --gui     (con interfaccia GlobalView di monitoraggio)
#   ./run-server.sh --gui --host=192.168.1.50

set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

echo "=== Avvio Agar.io Authoritative Server ==="
sbt "runMain it.unibo.agar.server.ServerApp $*"
