#!/usr/bin/env bash
set -uo pipefail -o functrace -o xtrace
#set -uo pipefail

if [[ "$1" != "" ]]; then
  export PARTICIPANT=$1
else
  echo "Can not find participant"
  exit 1
fi

mkdir -p ../kube-configs

vcluster connect \
  "participant-${PARTICIPANT}" \
  --server "vcluster-${PARTICIPANT}.workload-id.chr-fritz.de" \
  --kube-config-context-name "participant-${PARTICIPANT}" \
  --print > "../kube-configs/participant-${PARTICIPANT}.yaml"