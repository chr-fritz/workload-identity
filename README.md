# Workshop Workload Identity

This repository contains all the information about the workshop Workload Identity. The idea is to show how to develop,
deploy and run services which communicate together without any non-personalized credentials. The only two credentials
are your personal GitHub credentials and the one to login into your cloud provider.

## Talk demo

This repository contains a small demo which shows how to use a Kubernetes ServiceAccount token as a replacement of
the `client_id` and `client_secret` combination in keycloak. For this demo we recommend to set up a small kubernetes
cluster with [colima](https://github.com/abiosoft/colima).

The recommended start command is:

```shell
colima start --kubernetes \
             --activate \
             --network-address \ 
             --k3s-arg=--kube-apiserver-arg=--anonymous-auth=true \
             -p workload-id
```

The `kube-apiserver` argument `--anonymous-auth=true` allows anonymous access to `.well-known/openid-configuration`
and `/openid/v1/jwks` which is required within the demo.

To tear it down just run:

```shell
colima stop -p workload-id && colima delete -p workload-id
```

## Workshop "Hands on: Workload Identity - Wie wird man statische Zugangsdaten los?"

The workshop shows where workload id can be used and the participants will create their own vcluster and set up the
previous shown talk demo. Additionally, the workshop show how to work with workload ids in AWS.

The detailed workshop documentation is located in
the [docs/workshop-hands-on-workload-id.md](docs/workshop-hands-on-workload-id.md) (German!).

## Branching concept

To allow multiple workshops on this repository it uses a simple branching concept:

* The `main` branch is the rolling base for every new workshop.
* To prepare a new workshop create a new branch from `main`, named with a short description (i.e. conference title) that
  unique identifies the workshop iteration. i.e. `cloudland-2024`
* This branch should be the base and target branch for any pull requests which participants must create to create their
  own vCluster instances.
* Additionally, on this branch there should some checkpoint branches which contains the base and final version for every
  workshop step.

## Maintainer

Christian Fritz, QAware (@chrfritz)
Stefan Schmöller, QAware

## License

This workshop is released under the Apache 2.0 license. See [LICENSE](LICENSE)
