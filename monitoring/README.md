# CartFlow Monitoring

CartFlow uses the Prometheus Community `kube-prometheus-stack` Helm chart to provide Kubernetes monitoring.

The stack includes Prometheus, Grafana, and supporting Kubernetes monitoring components.

## Helm Repository

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

## Create Monitoring Namespace

```bash
kubectl create namespace monitoring
```

## Install Monitoring Stack

```bash
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring
```

The project used `kube-prometheus-stack` chart version **92.1.0** during implementation.

## Verify Monitoring

```bash
helm list -n monitoring
kubectl get pods -n monitoring
kubectl top nodes
```

## Grafana Security

Grafana is normally kept private using a Kubernetes `ClusterIP` service.

During testing, Grafana was temporarily exposed through an Azure LoadBalancer because Azure Cloud Shell Web Preview authentication interfered with browser access through `kubectl port-forward`.

After Grafana and Prometheus monitoring were successfully validated, the Grafana service was returned to `ClusterIP`.

No Grafana passwords, GitHub tokens, or other credentials are stored in this repository.
