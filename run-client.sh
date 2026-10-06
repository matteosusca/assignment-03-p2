#!/usr/bin/env bash
# Avvio di un Client Agar.io
# Uso:
#   ./run-client.sh                  (default: player 'p1' su localhost)
#   ./run-client.sh p2               (player 'p2' su localhost)
#   ./run-client.sh p1 --host=192.168.1.50

set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

PLAYER_ID="${1:-p1}"
if [ $# -gt 0 ]; then
  shift
fi

echo "=== Avvio Agar.io Client per '$PLAYER_ID' ==="
sbt "runMain it.unibo.agar.client.ClientApp $PLAYER_ID $*"
