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

1. Add a new identity provider of type "kubernetes" with alias `kubernetes.default.svc.cluster.local` and jwks url
   `https://kubernetes.default/openid/v1/jwks`.
2. Add Keycloak Client:
    * Client ID: `middleware-server`
    * Name: `Middleware-Server`
    * Root & Home URL: `http://middleware-server-192-168-107-4.nip.io/`
    * Callback URL: `http://middleware-server-192-168-107-4.nip.io/*`
   * Credentials:
       * Credentials Type: `Signed JWT - Federated`
       * Identity Provider `kubernetes.default.svc.cluster.local` (alias from above)
       * Federated subject  `system:serviceaccount:demo:middleware-server`

## Finale

Open http://middleware-server-192-168-107-4.nip.io/hello in incognito mode

Won't work, as missing audience

Add new Audience through client scope `middleware-server-dedicated`: `backend-service`
