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

## Maintainer

Christian Fritz, QAware (@chrfritz)
Stefan Schmöller, QAware

## License

This workshop is released under the Apache 2.0 license. See [LICENSE](LICENSE)
