FROM bitnami/keycloak:latest
USER 0
ADD --chmod=0666 keycloak-kubernetes-authenticator.jar /opt/bitnami/keycloak/providers/keycloak-kubernetes-authenticator.jar
USER 1001
