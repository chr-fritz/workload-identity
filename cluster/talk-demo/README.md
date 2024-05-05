# Demo @ Workload Identity Talk

## Before the talk

* Setup cluster
* Check colima ip, if it is not 192.168.107.4 search for `middleware-server-192-168-107-4.nip.io`
  and `keycloak-192-168-107-4.nip.io`, fill in the correct ip and commit it.
* Run
* ```shell
  k create ns demo \
    && k apply -f ghcr-secret.yaml \
    && k apply -k flux-system \
    && k apply -f flux-system-secret.yaml
   ```
* Patch `middleware-server` service account (no reconcile, ghcr
  secret): `k apply -f services/middleware-server/middleware-server-serviceaccount.yaml`

## Describe the setup

1. Keycloak Helm-Chart
2. Backend-Service
3. Middleware-Server
    1. Configuration (Service Account, Application Config, Volumes)
    2. `K8sOAuth2*RequestEntityConverter`

## Keycloak Setup:

https://keycloak-192-168-107-4.nip.io/

1. Setup `client-with-k8s` authentication flow:
    1. Copy the `clients` authentication flow
    2. Add `kubernetes-authenticator` and set requirement to `Alternative`
    3. Bind to the `Client authentication flow`
2. Add Keycloak Client:
    * Client ID: `middleware-server`
    * Name: `Middleware-Server`
    * Root & Home URL: `http://middleware-server-192-168-107-4.nip.io/`
    * Callback URL: `http://middleware-server-192-168-107-4.nip.io/*`
    * Description: `system:serviceaccount:demo:middleware-server@https://kubernetes.default.svc.cluster.local`
    * JWKS URL: `https://kubernetes.default/openid/v1/jwks`
    * Credentials Type: `Kubernetes Service Account`

## Finale

Open http://middleware-server-192-168-107-4.nip.io/hello in incognito mode

Won't work, as missing audience

Add new Audience through client scope `middleware-server-dedicated`: `backend-service`