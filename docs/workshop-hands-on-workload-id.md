# Hands on: Workload Identity - Wie wird man statische Zugangsdaten los?

<!-- TOC -->
* [Hands on: Workload Identity - Wie wird man statische Zugangsdaten los?](#hands-on-workload-identity---wie-wird-man-statische-zugangsdaten-los)
    * [Einführung](#einführung)
        * [Infrastruktur](#infrastruktur)
    * [Schritt 1: Teilnehmer Cluster erstellen](#schritt-1-teilnehmer-cluster-erstellen)
    * [Schritt 2: Keycloak im Cluster installieren](#schritt-2-keycloak-im-cluster-installieren)
    * [Schritt 3: Backend Service im Cluster installieren](#schritt-3-backend-service-im-cluster-installieren)
    * [Schritt 4: Middleware Server im Cluster installieren](#schritt-4-middleware-server-im-cluster-installieren)
    * [Schritt 5: Middleware Server als Client in Keycloak anlegen](#schritt-5-middleware-server-als-client-in-keycloak-anlegen)
        * [Client anlegen](#client-anlegen)
        * [Audience für Backend-Service hinzufügen](#audience-für-backend-service-hinzufügen)
<!-- TOC -->

## Einführung

Der Workshop zeigt einige Anwendungsbeispiele in denen die Authentifizierung von Diensten, Anwendungen und Containern
gegenüber AWS und anderen Anwendungen durch die Verwendung von Workload IDs sichergestellt ist. Anstatt regulärer
Passwörter, Client Secrets oder API Secret Keys kommen asynchrone Public Key Verfahren und explizite
Vertrauensbeziehungen zum Einsatz.

### Infrastruktur

Der Workshop nutzt einen von den Trainern zur Verfügung gestellten AWS EKS Cluster. Nahezu die gesamte Infrastruktur,
welche innerhalb dieses Clusters läuft, ist ebenfalls as-Code im Repository abgelegt:

* Setup EKS Cluster mit `eksctl`: [eks/main-cluster.yaml](../infrastructure/aws/eks/main-cluster.yaml)
* Kubernetes Resourcen im Cluster: [workshop-main-cluster](../cluster/workshop-main-cluster)
    * FluxCD als GitOps: [flux-system](../cluster/workshop-main-cluster/flux-system)
    * Crossplane um AWS Resourcen anzulegen: [crossplane-system](../cluster/workshop-main-cluster/crossplane-system)
    * Zusätzliche AWS Resourcen mit Crossplane: [aws-setup](../cluster/workshop-main-cluster/aws-setup)

Lediglich für Crossplane ist eine (weitreichende) AWS Role notwendig welche Crossplane den Zugriff auf AWS ermöglicht.
Diese hat derzeit folgende `PolicyAttachments`:

* `AmazonEC2ContainerRegistryFullAccess`
* `AmazonRoute53FullAccess`
* `IAMFullAccess`
* `SecretsManagerReadWrite`

Weiterhin wurde ihr die folgende Trust Policy zugewiesen:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Federated": "arn:aws:iam::730335410257:oidc-provider/oidc.eks.eu-central-1.amazonaws.com/id/16863A4F160277A9C0E1AC5E63C373EB"
            },
            "Action": "sts:AssumeRoleWithWebIdentity",
            "Condition": {
                "StringEquals": {
                    "oidc.eks.eu-central-1.amazonaws.com/id/16863A4F160277A9C0E1AC5E63C373EB:aud": "sts.amazonaws.com"
                },
                "StringLike": {
                    "oidc.eks.eu-central-1.amazonaws.com/id/16863A4F160277A9C0E1AC5E63C373EB:sub": "system:serviceaccount:crossplane-system:provider-aws-*"
                }
            }
        }
    ]
}
```

In diesem Cluster erstellen die Teilnehmer mit [vCluster](https://www.vcluster.com/) ihre eigenen Workshop Kubernetes
Cluster.

## Schritt 1: Teilnehmer Cluster erstellen

Um einen Teilnehmer Cluster zu erstellen, muss zunächst das
Repository [geforked](https://github.com/chr-fritz/workload-identity/fork) werden. Wichtig ist alle Branches mit zu
forken: ![❏ Copy the `main` branch only](fork-all-branches.png).

Anschließend muss das Repository geklont, das Upstream-Repository konfiguriert und der Branch gewechselt werden:

```shell
git clone https://github.com/<YOUR-USERNAME>/workload-identity
git remote add upstream https://github.com/chr-fritz/workload-identity
git checkout cloudland-2024
```

Alternativ sollte SSH zum Klonen verwendet werden:

```shell
git clone git@github.com:<YOUR-USERNAME>/workload-identity
git remote add upstream git@github.com:chr-fritz/workload-identity.git
git checkout cloudland-2024
```

Nun kann der vCluster angelegt werden. Hierfür muss das
Verzeichnis [cluster/workshop-main-cluster/participants/template](../cluster/workshop-main-cluster/participants/template)
kopiert werden. Der neue Name des Verzeichnisses muss dem GitHub Benutzernamen des Teilnehmers entsprechen.

Innerhalb des kopierten Verzeichnisses müssen die Referenzen `template` durch den eigenen GitHub Benutzernamen ersetzt
werden. Weiter muss in der
Datei [cluster/workshop-participant-cluster/flux-system/gotk-sync.yaml](../cluster/workshop-participant-cluster/flux-system/gotk-sync.yaml#L14)
innerhalb der Git URL der String `template` durch den eigenen GitHub Benutzernamen ersetzt werden.

Alle so vorgenommen Änderungen müssen nun committed, gepushed und per PullRequest an das Hauptrepository erstellt
werden:

```shell
# Add changed files to index (working dir is repository root)
git add cluster/workshop-main-cluster/participants/template cluster/workshop-participant-cluster/flux-system/gotk-sync.yaml
git commit -m"Onboard Worshop participant <YOUR-USERNAME>"
git push
```

Nun kann der PullRequest erstellt werden. Wichtig ist, dass als Base-Branch `cloudland-2024` ausgewählt ist. [^1]

Nach dem Merge findet sich
im [Workflow add-participant.yaml](https://github.com/chr-fritz/workload-identity/actions/workflows/add-participant.yaml)
ein Run für den jeweiligen PR, in welchem der Public Key ausgegeben wird mit dem sich die Flux Instanz aus dem eigenen
Cluster mit GitHub verbindet. Dieser muss als Deployment Key im Repository hinterlegt werden.

## Schritt 2: Keycloak im Cluster installieren

* Branch Ausgangssituation: `cloudland-2024-step-2-setup-keycloak`
* Branch Lösung (ohne Teilnehmer spezifische Anpassungen): `cloudland-2024-step-2-setup-keycloak-final`

Nun muss mit Flux das Helm-Chart von [Keycloak](https://github.com/bitnami/charts/tree/main/bitnami/keycloak)
installiert werden. Dabei müssen u.A. folgende Anpassungen durchgeführt werden:

1. Image: Im originalen Image ist
   der [Keycloak Kubernetes Client Authenticator](https://github.com/chr-fritz/keycloak-kubernetes-authenticator) nicht
   enthalten. Muss das erweiterte Image verwendet werden. Dieses wird über den
   Workflow [build-keycloak-extended-image.yaml](../.github/workflows/build-keycloak-extended-image.yaml). Dort findet
   sich auch der volle Image-Name, welcher für das Setup verwendet werden soll.
2. Zusätzlich ist es sinnvoll die Admin-User Daten passend zu setzen.
3. Für den externen Zugriff sollte ein Ingress hinzugefügt werden:
   ```yaml
   ingress:
      enabled: true
      hostname: keycloak-<YOUR-GITHUB-USERNAME>.workload-id.chr-fritz.de
      tls: true
      selfSigned: false
      ingressClassName: nginx
      secrets:
        # Secrets are not required, the Ingress is synchronized to the main cluster which already have a wildcard certificate.
        - name: none
   ``` 

Als Extra können Teilnehmer ihre eigene ECR erstellen und ein selbst gebautes Image verwenden. Der gesamte Code findet
sich im Repository. Zum Erstellen der ECR bitte einen neuen Branch vom originalen `cloudland-2024` Branch erstellen und
die jeweiligen Änderungen als PullRequest an das Haupt-Repository übertragen.

## Schritt 3: Backend Service im Cluster installieren

## Schritt 4: Middleware Server im Cluster installieren

## Schritt 5: Middleware Server als Client in Keycloak anlegen

### Client anlegen

### Audience für Backend-Service hinzufügen

[^1]: GitHub Dokumentation:
[Erstellen eines PR](https://docs.github.com/de/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)