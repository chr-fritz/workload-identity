#!/usr/bin/env bash
set -uo pipefail -o functrace -o xtrace
#set -uo pipefail

if [[ "$1" != "" ]]; then
  export PARTICIPANT=$1
elif [[ "$1" == "" && "$2" != "" ]]; then
  export PARTICIPANT=$2
else
  echo "Can not find participant"
  exit 1
fi

# shellcheck disable=SC2034
for i in {1..10}; do
  if [[ $(vcluster list --output json | jq --raw-output ".[] | select(.Status == \"Running\" and .Name == \"participant-${PARTICIPANT}\").Name") == "participant-${PARTICIPANT}" ]]; then
    echo "vcluster found"
    FOUND="1"
    break
  fi
  sleep 10
done

if [[ $FOUND != "1" ]]; then
  echo "Can not find cluster"
  exit 1
fi

vcluster connect --debug --log-output plain --background-proxy --update-current --kube-config-context-name vc-participant "participant-${PARTICIPANT}" &
VC_CONNECT_PID=$!
sleep 10

ssh-keygen -t ed25519 -f ./identity -N ""
kubectl --context vc-participant create ns flux-system || true
flux install --export | kubectl --context vc-participant apply -f -
kubectl --context vc-participant create secret generic flux-system --from-file identity --from-file identity.pub --from-file known_hosts --namespace flux-system -o yaml
SYNC_CONF=$(cat ../cluster/workshop-participant-cluster/flux-system/gotk-sync.yaml | envsubst)
echo "${SYNC_CONF}" | kubectl --context vc-participant apply -f -

kill $VC_CONNECT_PID

# shellcheck disable=SC2129
echo "## Participant creation for ${PARTICIPANT}" >>$GITHUB_STEP_SUMMARY
echo "Participant cluster successfully created." >>$GITHUB_STEP_SUMMARY
echo "Please add the following ssh-key as [Deploy Key](https://github.com/${PARTICIPANT}/workload-identity/settings/keys) to your repository:" >>$GITHUB_STEP_SUMMARY
echo '```text' >>$GITHUB_STEP_SUMMARY
cat identity.pub >>$GITHUB_STEP_SUMMARY
echo '```' >>$GITHUB_STEP_SUMMARY
