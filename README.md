# CartFlow — End-to-End DevSecOps & GitOps Project

CartFlow is a hands-on DevSecOps portfolio project that demonstrates how a simple Java application can move through a production-style software delivery lifecycle using GitHub, Azure DevOps, Maven, SonarQube, OWASP Dependency-Check, Docker, Trivy, Azure Container Registry (ACR), Azure Kubernetes Service (AKS), Helm, Argo CD, Prometheus, and Grafana.

The purpose of this project is not complex Java development. The Java application is the workload used to demonstrate the DevOps lifecycle: source control, continuous integration, automated testing, code quality, dependency and container security scanning, image management, Kubernetes deployment, GitOps continuous delivery, health checking, and monitoring.

> **Security note:** No passwords, Personal Access Tokens (PATs), SonarQube tokens, NVD API keys, or Grafana passwords belong in this repository. Secrets used during this project were stored outside Git in appropriate secret stores or Kubernetes Secrets.

---

## Table of Contents

1. [Project Goals](#project-goals)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Where Commands Are Run](#where-commands-are-run)
5. [Stage 1 — Create the Java/Maven Application](#stage-1--create-the-javamaven-application)
6. [Stage 2 — Git and GitHub](#stage-2--git-and-github)
7. [Stage 3 — Azure DevOps CI and Self-Hosted Agent](#stage-3--azure-devops-ci-and-self-hosted-agent)
8. [Stage 4 — SonarQube](#stage-4--sonarqube)
9. [Stage 5 — OWASP Dependency-Check](#stage-5--owasp-dependency-check)
10. [Stage 6 — Docker](#stage-6--docker)
11. [Stage 7 — Trivy](#stage-7--trivy)
12. [Stage 8 — Azure Container Registry](#stage-8--azure-container-registry)
13. [Stage 9 — AKS and Kubernetes](#stage-9--aks-and-kubernetes)
14. [Stage 10 — Helm](#stage-10--helm)
15. [Stage 11 — Argo CD and GitOps](#stage-11--argo-cd-and-gitops)
16. [Stage 12 — Prometheus and Grafana](#stage-12--prometheus-and-grafana)
17. [Final Azure DevOps Pipeline](#final-azure-devops-pipeline)
18. [Repository Structure](#repository-structure)
19. [Security Practices](#security-practices)
20. [Cost Management](#cost-management)
21. [Major Troubleshooting Lessons](#major-troubleshooting-lessons)
22. [Verification Checklist](#verification-checklist)
23. [Future Improvements](#future-improvements)
24. [Interview Talking Points](#interview-talking-points)
25. [What This Project Demonstrates](#what-this-project-demonstrates)

---

# Project Goals

The project was designed to demonstrate an end-to-end DevSecOps workflow:

- Create and test a small Java application.
- Store source code in GitHub.
- Build and test automatically with Azure DevOps.
- Perform static code analysis with SonarQube.
- Scan application dependencies with OWASP Dependency-Check.
- Package the application as a Docker container.
- Scan the container with Trivy before publishing it.
- Push approved images to Azure Container Registry.
- Deploy the workload to Azure Kubernetes Service.
- Package Kubernetes resources with Helm.
- Use Argo CD for GitOps-based continuous delivery.
- Monitor the Kubernetes environment with Prometheus and Grafana.
- Apply practical security controls throughout the lifecycle.

---

# Architecture

```text
Developer Workstation — Windows 11 / Git Bash
                  |
                  | git push
                  v
               GitHub
                  |
                  | source trigger
                  v
        +-----------------------+
        |    Azure DevOps CI    |
        | Self-Hosted Linux VM  |
        +-----------------------+
                  |
        +---------+----------+
        |                    |
        v                    v
 Maven Build/Test        SonarQube
        |
        v
 OWASP Dependency-Check
        |
        v
    Docker Build
        |
        v
     Trivy Scan
        |
        | only approved image proceeds
        v
 Azure Container Registry
        |
        v
+-----------------------------------------+
|       Azure Kubernetes Service          |
|                                         |
| GitHub Helm Chart ---> Argo CD          |
|                         |               |
|                         v               |
|                 CartFlow Deployment     |
|                         |               |
|                 Kubernetes Service      |
|                         |               |
|                 Azure Load Balancer     |
|                                         |
| Prometheus <--- Kubernetes Metrics      |
|      |                                  |
|      v                                  |
|   Grafana                               |
+-----------------------------------------+
```

## CI versus CD in this project

**Azure DevOps is the Continuous Integration (CI) system.** It checks out the source, builds the Java application, runs unit tests, performs SonarQube and OWASP analysis, builds the Docker image, scans it with Trivy, pushes approved images to ACR, and publishes artifacts.

**Argo CD is the Continuous Delivery (CD) / GitOps system.** It watches the desired Kubernetes configuration in GitHub and reconciles the AKS cluster with the Git repository.

**Helm is the Kubernetes packaging/template layer.** It is not the CI system or the GitOps controller. Argo CD renders and deploys the Helm chart.

---

# Technology Stack

| Area | Technology |
|---|---|
| Workstation | Windows 11, Git Bash |
| Source Control | Git, GitHub |
| Application | Java 17 |
| Build | Apache Maven |
| Testing | JUnit 5 |
| CI | Azure DevOps Pipelines |
| CI Agent | Self-hosted Ubuntu Azure VM |
| Static Analysis | SonarQube Community Build |
| Dependency Security | OWASP Dependency-Check |
| Containerization | Docker |
| Container Security | Trivy |
| Registry | Azure Container Registry |
| Orchestration | Azure Kubernetes Service / Kubernetes |
| Kubernetes Packaging | Helm |
| GitOps / CD | Argo CD |
| Monitoring | Prometheus |
| Visualization | Grafana |
| Administration | Azure CLI, kubectl, Azure Cloud Shell |

Important versions observed during implementation included Java 17, Maven 3.9.x, Git 2.43.0 on the Azure VM, Trivy 0.75.0, Azure DevOps agent 5.279.0, SonarQube Community Build 26.9.0.129388, Helm v3.13.1 locally, Helm v4.1.4 in Cloud Shell, `kube-prometheus-stack` chart 92.1.0, and AKS Kubernetes v1.35.8 at final verification.

---

# Where Commands Are Run

The project uses several environments. This distinction is important.

### Windows Git Bash

Used for:

```text
Creating/editing project files
Maven development
Git commands
GitHub pushes
Local Helm chart validation
```

Project directory:

```bash
~/CartFlow
```

### Azure Ubuntu VM

Used for:

```text
Azure DevOps self-hosted agent
Java/Maven build tooling
Docker
Trivy
SonarQube container
OWASP Dependency-Check execution
```

### Azure DevOps

Used for:

```text
CI pipeline
Secret variables
SonarQube service connection
ACR service connection
Build artifacts
```

### Azure Cloud Shell

Used primarily for:

```text
Azure CLI
AKS credentials
kubectl
Helm administration
Argo CD installation
Prometheus/Grafana installation
```

Cloud Shell was especially useful because local Azure CLI authentication was blocked by Microsoft Entra Security Defaults.

---

# Stage 1 — Create the Java/Maven Application

## 1. Create the project

**Run on: Windows Git Bash**

```bash
mkdir CartFlow
cd CartFlow

mkdir -p src/main/java/com/cartflow
mkdir -p src/test/java/com/cartflow
```

### What does `mkdir -p` mean?

`mkdir` creates a directory.

The `-p` option means **parents**. It creates any missing parent directories in the path and does not fail if the directory already exists.

For example:

```bash
mkdir -p cartflow-chart/templates
```

can create both `cartflow-chart` and `templates` in one command.

---

## 2. Maven configuration

`pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.cartflow</groupId>
    <artifactId>cartflow</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Initial CartFlow application

The application originally behaved like a command-line application:

```java
package com.cartflow;

public class CartFlow {

    public static String getWelcomeMessage() {
        return "Welcome to CartFlow";
    }

    public static String getHealthStatus() {
        return "CartFlow is healthy";
    }

    public static void main(String[] args) {
        System.out.println(getWelcomeMessage());
        System.out.println(getHealthStatus());
    }
}
```

This simple version was useful for validating Java, Maven, unit testing, Docker, and ACR before Kubernetes was introduced.

---

## 4. Unit tests

`src/test/java/com/cartflow/CartFlowTest.java`:

```java
package com.cartflow;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartFlowTest {

    @Test
    void testWelcomeMessage() {
        assertEquals("Welcome to CartFlow", CartFlow.getWelcomeMessage());
    }

    @Test
    void testHealthStatus() {
        assertEquals("CartFlow is healthy", CartFlow.getHealthStatus());
    }
}
```

---

## 5. Maven validation

**Run on: Windows Git Bash**

```bash
mvn validate
mvn compile
mvn test
mvn package
```

Results:

```text
2 unit tests passed
BUILD SUCCESS
target/cartflow-1.0.0.jar created
```

The `target/` directory is generated by Maven and should not be committed to Git.

---

# Stage 2 — Git and GitHub

## `.gitignore`

```text
target/
*.log
.idea/
.vscode/
*.iml
```

Initialize and connect the project to GitHub using the normal Git workflow:

```bash
git init
git add .
git commit -m "Initial CartFlow application"
git branch -M main
git remote add origin https://github.com/johnson-busuyi/CartFlow.git
git push -u origin main
```

The GitHub repository is private.

## Normal workflow

```bash
git status
git add .
git commit -m "Describe the change"
git push origin main
```

Before pushing important changes, this project also used:

```bash
git fetch origin
git status
```

to detect whether the remote repository had changed.

---

## Git divergence and rebase

During development Git reported:

```text
Your branch and 'origin/main' have diverged
```

and on another push:

```text
! [rejected] main -> main (fetch first)
```

Instead of force-pushing and potentially overwriting remote work, the project used:

```bash
git pull --rebase origin main
```

followed by:

```bash
git push origin main
```

### What does rebase do?

Suppose the histories look like:

```text
Remote: A --- B --- C
Local:  A --- B --- D
```

A rebase takes local commit `D` and replays it after the latest remote commit:

```text
A --- B --- C --- D
```

This produces a clean, linear history and avoids using a destructive force push.

---

# Useful Bash Syntax Used in This Project

Several project files were created using shell heredocs.

Example:

```bash
cat > cartflow-chart/Chart.yaml <<'EOF'
apiVersion: v2
name: cartflow-chart
EOF
```

Explanation:

- `cat` reads or outputs text.
- `>` redirects output into a file and **overwrites** that file.
- `>>` appends instead of overwriting.
- `<<'EOF'` starts a **here-document (heredoc)**.
- Everything until the ending `EOF` becomes input to `cat`.
- Quoting `'EOF'` prevents the shell from expanding variables such as `$HOME` inside the block.
- `EOF` is simply a delimiter. It traditionally means “End Of File,” but another word could be used.
- The closing `EOF` must appear by itself on a line.

If a heredoc was started accidentally and not completed, `Ctrl+C` can cancel the command.

During this project an accidental root-level `values.yaml` was created containing command text rather than the intended Helm values. It was detected with:

```bash
git status
```

inspected, and removed:

```bash
rm values.yaml
git status
```

This reinforced the value of checking `git status` before staging or committing.

---

# Stage 3 — Azure DevOps CI and Self-Hosted Agent

Azure DevOps project:

```text
CartFlow-DevSecOps
```

Agent pool:

```text
CartFlow-Agent-Pool
```

A self-hosted Ubuntu Server 24.04 Azure VM was used as the pipeline agent.

The VM hosted:

```text
Azure DevOps Agent
Java 17
Maven
Git
Docker
Trivy
SonarQube container
```

The agent was installed under:

```bash
~/myagent
```

and managed as a service with commands such as:

```bash
sudo ./svc.sh install
sudo ./svc.sh start
sudo ./svc.sh status
```

The agent version observed during implementation was 5.279.0 Linux x64.

The VM was later resized to approximately 8 GiB RAM because the first OWASP NVD database import placed significant memory pressure on the smaller VM.

To control cost, the VM can be deallocated when the CI environment and SonarQube are not needed. GitHub, ACR, and AKS resources are independent of whether this VM is running, although the self-hosted pipeline agent and SonarQube become unavailable while it is stopped.

---

# Stage 4 — SonarQube

SonarQube Community Build was deployed as a Docker container on the same Azure VM as the CI agent.

## Host kernel configuration

**Run on: Azure Ubuntu VM**

```bash
sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072
```

These were live runtime settings during the lab. For a persistent production installation they should be configured permanently using the operating system's sysctl configuration.

## Run SonarQube

```bash
docker pull sonarqube:community

docker run -d \
  --name sonarqube \
  --restart unless-stopped \
  -p 9000:9000 \
  sonarqube:community
```

SonarQube Community Build version observed:

```text
26.9.0.129388
```

Port 9000 in the Azure Network Security Group was restricted to the administrator's public IP rather than broadly exposed.

A SonarQube token was created but **was never committed to Git**.

The Azure DevOps SonarQube service connection was named:

```text
CartFlow-SonarQube
```

Because the Azure DevOps agent and SonarQube were running on the same VM, the service connection used:

```text
http://localhost:9000
```

This also avoided a public-IP hairpin connectivity problem.

---

## SonarQube Community branch-analysis issue

Azure DevOps automatically supplied:

```text
sonar.branch.name=main
```

but SonarQube Community Build did not support that branch-analysis parameter.

The pipeline therefore removes it after `SonarQubePrepare`:

```yaml
- bash: |
    echo "Removing sonar.branch.name for SonarQube Community Build..."

    CLEAN_PARAMS=$(echo "$SONARQUBE_SCANNER_PARAMS" | \
      sed -E 's/,"sonar.branch.name":"[^"]*"//g')

    echo "##vso[task.setvariable variable=SONARQUBE_SCANNER_PARAMS]$CLEAN_PARAMS"

    echo "Branch parameter removed."
  displayName: 'Remove SonarQube Branch Parameter'
```

This was an important compatibility troubleshooting step rather than disabling SonarQube analysis.

---

# Stage 5 — OWASP Dependency-Check

OWASP Dependency-Check was added to identify known vulnerabilities in application dependencies.

An NVD API key was required for reliable NVD updates.

The key was stored as an Azure DevOps **secret variable**:

```text
NVD_API_KEY
```

The actual key is never written into YAML or Git.

Manual validation used:

```bash
export NVD_API_KEY='YOUR-KEY-IS-NOT-COMMITTED'

mvn org.owasp:dependency-check-maven:check \
  -DnvdApiKey="$NVD_API_KEY"
```

The scan generated:

```text
target/dependency-check-report.html
```

At the successful validation point:

```text
Vulnerabilities found: 0
Suppressed vulnerabilities: 0
```

A Sonatype OSS Index credentials warning was observed but did not prevent the NVD-based scan.

## Resource issue

The initial NVD import placed enough pressure on the smaller VM that the machine became unresponsive/deallocated. The VM was resized to approximately 7.7–8 GiB RAM, after which the Dependency-Check operation completed successfully.

This demonstrates why security tooling must also be included in CI capacity planning.

---

# Stage 6 — Docker

## Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/cartflow-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-cp", "app.jar", "com.cartflow.CartFlow"]
```

The first CLI image was built and tested with:

```bash
docker build -t cartflow:1.0 .
docker run --name cartflow-test cartflow:1.0
```

It printed:

```text
Welcome to CartFlow
CartFlow is healthy
```

and then exited.

At this stage that behavior was expected because the original Java application was a command-line program.

---

## Dockerfile missing from GitHub

One pipeline initially failed with an error similar to:

```text
No Dockerfile matching .../Dockerfile was found.
```

The Dockerfile existed in a working location but had not been committed to the GitHub source repository used by the Azure DevOps checkout.

The fix was to add, commit, and push the Dockerfile to GitHub.

**Lesson:** a CI pipeline can only build files available in its checked-out source or explicitly downloaded artifacts. A file existing on an engineer's machine or an old agent workspace is not enough.

---

# Stage 7 — Trivy

Trivy was installed on the self-hosted agent after the initial:

```text
trivy: command not found
```

error.

Version observed:

```text
Trivy 0.75.0
```

Manual scan:

```bash
trivy image --severity HIGH,CRITICAL cartflow:1.0
```

The scan reported no HIGH/CRITICAL vulnerabilities in the tested image components at that time.

The final pipeline uses:

```bash
trivy image \
  --scanners vuln \
  --severity HIGH,CRITICAL \
  --exit-code 1 \
  cartflowregistry.azurecr.io/cartflow:$(Build.BuildId)
```

`--exit-code 1` is important: a HIGH or CRITICAL vulnerability causes the pipeline step to fail.

Even more importantly, the Trivy step occurs **before the ACR push**, creating a security gate:

```text
Docker build
    |
    v
Trivy scan
    |
    | pass
    v
ACR push
```

---

# Stage 8 — Azure Container Registry

Registry:

```text
cartflowregistry
```

Login server:

```text
cartflowregistry.azurecr.io
```

Resource group:

```text
johnson-busuyi-RG
```

SKU:

```text
Basic
```

The Azure DevOps ACR service connection is:

```text
CartFlow-ACR
```

Workload Identity federation was used for the Azure DevOps service connection rather than embedding registry credentials in the pipeline.

Image tags were inspected with:

```bash
az acr repository show-tags \
  --name cartflowregistry \
  --repository cartflow \
  --orderby time_desc \
  --output table
```

Build-specific tags such as `29` were used for deployment rather than depending only on `latest`.

## Docker tag mismatch

An earlier ACR push failed because the locally built image tag did not match the tag expected by the Docker push task.

The final pipeline solves this by consistently building and pushing:

```text
$(Build.BuildId)
latest
```

with repository:

```text
cartflow
```

and registry:

```text
cartflowregistry.azurecr.io
```

For Kubernetes/GitOps deployments, immutable build IDs are preferable to `latest` because they provide deterministic rollouts and rollback traceability.

---

# Stage 9 — AKS and Kubernetes

AKS cluster:

```text
cartflow-aks
```

Resource group:

```text
johnson-busuyi-RG
```

Region:

```text
East US 2
```

Node pool:

```text
agentpool
```

Node VM:

```text
Standard_D2ds_v6
2 vCPU / 8 GiB
1 node
```

The original region/SKU choices encountered Azure Free Credit quota/capacity limitations. East US 2 with `Standard_D2ds_v6` was selected as a workable configuration.

## AKS configuration decisions

The cluster used:

```text
Azure CNI Overlay
Public cluster
Manual node count: 1
OIDC enabled
Workload Identity enabled
Image Cleaner enabled
ACR integration: cartflowregistry
```

The following were intentionally not enabled during cluster creation:

```text
Istio
Azure Policy
Managed Prometheus
Managed Grafana
Container Insights
Virtual Nodes
Key Vault CSI
```

Prometheus and Grafana were intentionally installed later with Helm so the project could demonstrate the monitoring stack directly.

---

## Azure CLI authentication and Security Defaults

On the Ubuntu VM, Azure CLI authentication using `az login` / device code was blocked with Microsoft Entra error:

```text
AADSTS530035: Access has been blocked by security defaults.
```

Instead of weakening the tenant by disabling Security Defaults, Azure Cloud Shell was used for AKS administration.

This was an intentional security decision:

```text
Do not weaken identity security simply to make a lab command easier.
Use an approved administrative path instead.
```

---

## Connect kubectl to AKS

Initial credentials command:

```bash
az aks get-credentials \
  --resource-group johnson-busuyi-RG \
  --name cartflow-aks
```

When a new Cloud Shell session required credentials to be refreshed:

```bash
az aks get-credentials \
  --resource-group johnson-busuyi-RG \
  --name cartflow-aks \
  --overwrite-existing
```

Verify:

```bash
kubectl get nodes
```

Final observed node state:

```text
Ready
Kubernetes v1.35.8
```

Cloud Shell is ephemeral, so source-controlled configuration should live in GitHub rather than only in the Cloud Shell filesystem.

---

## Create the CartFlow namespace

```bash
kubectl create namespace cartflow
kubectl get namespaces
```

---

# The Important Kubernetes Failure: CLI Application Restarts

The first Kubernetes deployment used image build `28`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cartflow
  namespace: cartflow
spec:
  replicas: 1
  selector:
    matchLabels:
      app: cartflow
  template:
    metadata:
      labels:
        app: cartflow
    spec:
      containers:
        - name: cartflow
          image: cartflowregistry.azurecr.io/cartflow:28
          imagePullPolicy: IfNotPresent
```

The pod repeatedly showed a state similar to:

```text
0/1 Completed
```

Logs showed:

```text
Welcome to CartFlow
CartFlow is healthy
```

The container was not crashing. It was exiting successfully with exit code 0 because the Java application completed its work.

A Kubernetes `Deployment`, however, is designed for a long-running service. Kubernetes therefore restarted the completed process.

This proved several things were already working:

```text
AKS could pull from ACR
The container could start
The Java program executed correctly
Kubernetes restart behavior worked as designed
```

The problem was the application's runtime model.

---

# Convert CartFlow to a Long-Running HTTP Service

The Java application was changed to use Java's built-in `HttpServer`:

```java
package com.cartflow;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class CartFlow {

    public static String getWelcomeMessage() {
        return "Welcome to CartFlow";
    }

    public static String getHealthStatus() {
        return "CartFlow is healthy";
    }

    public static void main(String[] args) throws IOException {

        HttpServer server =
            HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            String response = getWelcomeMessage();
            exchange.sendResponseHeaders(
                200,
                response.getBytes().length
            );

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.createContext("/health", exchange -> {
            String response = getHealthStatus();
            exchange.sendResponseHeaders(
                200,
                response.getBytes().length
            );

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println(
            "CartFlow HTTP server started on port 8080"
        );
    }
}
```

Local Docker validation:

```bash
docker build -t cartflow:2.0 .

docker run --rm \
  -p 8080:8080 \
  --name cartflow-web \
  cartflow:2.0
```

Test:

```bash
curl http://localhost:8080/
curl http://localhost:8080/health
```

Expected:

```text
Welcome to CartFlow
CartFlow is healthy
```

The change was committed with a message such as:

```text
Convert CartFlow to HTTP service with health endpoint
```

Azure DevOps produced build/image `29`.

The AKS deployment was updated:

```bash
kubectl set image deployment/cartflow \
  cartflow=cartflowregistry.azurecr.io/cartflow:29 \
  -n cartflow
```

Watch rollout:

```bash
kubectl get pods -n cartflow -w
```

The new pod became:

```text
1/1 Running
0 restarts
```

---

# Kubernetes Health Probes

The deployment was enhanced with readiness and liveness probes:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cartflow
  namespace: cartflow
spec:
  replicas: 1
  selector:
    matchLabels:
      app: cartflow
  template:
    metadata:
      labels:
        app: cartflow
    spec:
      containers:
        - name: cartflow
          image: cartflowregistry.azurecr.io/cartflow:29
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080

          readinessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 10

          livenessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 20
```

### Readiness probe

Answers:

> Is this pod ready to receive traffic?

A failed readiness probe removes the pod from Service traffic without necessarily restarting it.

### Liveness probe

Answers:

> Is the application still alive?

Repeated liveness failures can cause Kubernetes to restart the container.

---

# Kubernetes Service

The application was exposed through:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: cartflow-service
  namespace: cartflow
spec:
  selector:
    app: cartflow

  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080

  type: LoadBalancer
```

Traffic flow:

```text
Internet
   |
   v
Azure Load Balancer
   |
   v
Kubernetes Service :80
   |
   v
CartFlow Pod :8080
```

A NodePort is automatically allocated behind a `LoadBalancer` Service.

External validation used:

```bash
kubectl get svc -n cartflow
curl http://<EXTERNAL-IP>/
curl http://<EXTERNAL-IP>/health
```

Public IPs changed during redeployments, so historical IP addresses are intentionally not treated as permanent configuration.

---

# Stage 10 — Helm

## What is Helm?

Helm is a package manager for Kubernetes.

A useful comparison is:

```text
Maven -> packages/manages Java applications
Helm  -> packages/manages Kubernetes applications
```

Helm charts turn Kubernetes YAML into reusable templates with configurable values.

---

## Create the chart

Initial Cloud Shell work used:

```bash
helm create cartflow-chart
```

Later, because Cloud Shell is ephemeral, the final chart was recreated in the Windows Git repository:

```bash
cd ~/CartFlow
mkdir -p cartflow-chart/templates
```

Final chart structure:

```text
cartflow-chart/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── deployment.yaml
    └── service.yaml
```

---

## Chart.yaml

```yaml
apiVersion: v2
name: cartflow-chart
description: Helm chart for deploying the CartFlow application to AKS
type: application
version: 0.1.0
appVersion: "1.0.0"
```

---

## values.yaml

```yaml
replicaCount: 1

image:
  repository: cartflowregistry.azurecr.io/cartflow
  tag: "29"
  pullPolicy: IfNotPresent

service:
  type: LoadBalancer
  port: 80
  targetPort: 8080

containerPort: 8080

readinessProbe:
  path: /health
  initialDelaySeconds: 5
  periodSeconds: 10

livenessProbe:
  path: /health
  initialDelaySeconds: 10
  periodSeconds: 20
```

---

## Helm Deployment template

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cartflow
spec:
  replicas: {{ .Values.replicaCount }}

  selector:
    matchLabels:
      app: cartflow

  template:
    metadata:
      labels:
        app: cartflow

    spec:
      containers:
        - name: cartflow
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}

          ports:
            - containerPort: {{ .Values.containerPort }}

          readinessProbe:
            httpGet:
              path: {{ .Values.readinessProbe.path }}
              port: {{ .Values.containerPort }}
            initialDelaySeconds: {{ .Values.readinessProbe.initialDelaySeconds }}
            periodSeconds: {{ .Values.readinessProbe.periodSeconds }}

          livenessProbe:
            httpGet:
              path: {{ .Values.livenessProbe.path }}
              port: {{ .Values.containerPort }}
            initialDelaySeconds: {{ .Values.livenessProbe.initialDelaySeconds }}
            periodSeconds: {{ .Values.livenessProbe.periodSeconds }}
```

---

## Helm Service template

```yaml
apiVersion: v1
kind: Service
metadata:
  name: cartflow-service

spec:
  selector:
    app: cartflow

  ports:
    - protocol: TCP
      port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.targetPort }}

  type: {{ .Values.service.type }}
```

---

## Helm lint problem

The initial chart generated by `helm create` included templates that referenced values removed from the simplified `values.yaml`.

`helm lint` failed on `serviceaccount.yaml` with a nil-pointer error.

Unused generated templates were removed:

```bash
rm -f cartflow-chart/templates/serviceaccount.yaml
rm -f cartflow-chart/templates/ingress.yaml
rm -f cartflow-chart/templates/hpa.yaml
rm -f cartflow-chart/templates/httproute.yaml
rm -f cartflow-chart/templates/NOTES.txt
rm -rf cartflow-chart/templates/tests
```

Then:

```bash
helm lint cartflow-chart
helm template cartflow cartflow-chart
```

succeeded.

The remaining recommendation to add a chart icon was informational, not an error.

---

# Transition from Manual Kubernetes to Helm

Before Helm took ownership, the manually created workload was removed:

```bash
kubectl delete deployment cartflow -n cartflow
kubectl delete service cartflow-service -n cartflow
kubectl get all -n cartflow
```

Then:

```bash
helm install cartflow ./cartflow-chart \
  --namespace cartflow
```

Verify:

```bash
helm list -n cartflow
kubectl get all -n cartflow
```

After Chart metadata cleanup:

```bash
helm upgrade cartflow ./cartflow-chart \
  --namespace cartflow
```

The Helm release reached revision 2.

A changed public LoadBalancer IP after recreation was expected because Kubernetes/Azure provisioned a new external frontend.

---

# Stage 11 — Argo CD and GitOps

## Install Argo CD

Creating the namespace does **not** install Argo CD:

```bash
kubectl create namespace argocd
kubectl get namespace argocd
```

Argo CD itself was installed with:

```bash
kubectl apply -n argocd \
  --server-side \
  --force-conflicts \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Verify:

```bash
kubectl get pods -n argocd
kubectl get svc -n argocd
```

All Argo CD pods reached `Running`.

The `argocd-server` Service was intentionally kept as `ClusterIP` rather than permanently exposing the administrative UI to the Internet.

---

# Argo CD UI / Cloud Shell Troubleshooting

A port-forward to the Argo CD server was attempted.

Azure Cloud Shell Web Preview did not allow port 8080 because its supported preview ranges excluded 8080–8090. Port 8091 was tried instead.

Additional issues occurred:

```text
Web Preview authentication returned Unauthorized.
Sign-in opened another Cloud Shell session.
The original port-forward was lost.
A later attempt reported address already in use.
```

Rather than weaken security or make the UI public just for configuration, the project proceeded using declarative Kubernetes resources and `kubectl`.

This demonstrates an important GitOps principle:

> The Argo CD UI is convenient, but it is not required to operate Argo CD.

---

# Argo CD Application

The repository contains:

```text
argocd/cartflow-argocd.yaml
```

Configuration:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application

metadata:
  name: cartflow
  namespace: argocd

spec:
  project: default

  source:
    repoURL: https://github.com/johnson-busuyi/CartFlow.git
    targetRevision: main
    path: cartflow-chart

    helm:
      valueFiles:
        - values.yaml

  destination:
    server: https://kubernetes.default.svc
    namespace: cartflow

  syncPolicy:
    automated:
      prune: true
      selfHeal: true

    syncOptions:
      - CreateNamespace=true
```

This tells Argo CD:

```text
Repository: CartFlow GitHub repository
Branch: main
Application path: cartflow-chart
Renderer: Helm
Destination: current AKS cluster
Namespace: cartflow
Synchronization: automatic
Prune: enabled
Self-heal: enabled
```

### `prune: true`

If a managed resource is removed from Git, Argo CD can remove it from the cluster.

### `selfHeal: true`

If the live cluster drifts from the Git desired state, Argo CD can reconcile it back.

---

# Transition from Helm CLI Ownership to Argo CD

Before Argo CD took over:

```bash
helm list -n cartflow
```

confirmed the existing Helm release.

It was then removed:

```bash
helm uninstall cartflow -n cartflow
kubectl get all -n cartflow
```

Some resources briefly remained because Kubernetes deletion is asynchronous. They disappeared shortly afterward.

The Argo Application was then applied:

```bash
kubectl apply -f cartflow-argocd.yaml
```

or from the repository path:

```bash
kubectl apply -f argocd/cartflow-argocd.yaml
```

---

# Private GitHub Repository Authentication

The repository is private.

Initially:

```bash
kubectl get applications -n argocd
```

showed:

```text
Unknown
Healthy
```

and:

```bash
kubectl describe application cartflow -n argocd
```

showed a repository authentication `ComparisonError`.

A dedicated GitHub token was used through a Kubernetes Secret rather than being stored in the Application manifest.

The token was entered without echoing it:

```bash
read -s -p "Enter GitHub Argo CD token: " GITHUB_TOKEN
echo
```

Then:

```bash
kubectl create secret generic cartflow-github-repo \
  -n argocd \
  --from-literal=type=git \
  --from-literal=url=https://github.com/johnson-busuyi/CartFlow.git \
  --from-literal=username=johnson-busuyi \
  --from-literal=password="$GITHUB_TOKEN" \
  --dry-run=client -o yaml | kubectl apply -f -
```

The Secret was identified to Argo CD:

```bash
kubectl label secret cartflow-github-repo \
  -n argocd \
  argocd.argoproj.io/secret-type=repository \
  --overwrite
```

A hard refresh was triggered:

```bash
kubectl annotate application cartflow \
  -n argocd \
  argocd.argoproj.io/refresh=hard \
  --overwrite
```

### Security incident and response

During interactive troubleshooting, a GitHub token was accidentally exposed in terminal/chat input. That credential was immediately treated as compromised and revoked, and a new token was created.

**The exposed credential is not reproduced anywhere in this repository or README.**

This demonstrates the correct response to accidental credential disclosure:

```text
1. Treat the credential as compromised.
2. Revoke it immediately.
3. Create a replacement.
4. Store/use the replacement securely.
5. Never commit the secret to source control.
```

### Authorization troubleshooting

After authentication was corrected, GitHub returned an authorization error indicating insufficient repository access. The fine-grained token's repository permission was temporarily changed to `Contents: Read and write`, after which Argo CD synchronized successfully.

Argo CD normally only needs read access to pull a Git repository. Therefore reducing the token to the minimum confirmed working read-only permission is a recommended hardening step. The README does not claim that write permission is inherently required by Argo CD.

---

# Argo CD Success

Final verification:

```bash
kubectl get applications -n argocd
```

showed:

```text
cartflow   Synced   Healthy
```

Meaning:

**Synced** — the live AKS resources match the desired configuration in Git.

**Healthy** — the deployed resources are operating successfully.

Observed CartFlow workload:

```text
Pod:        1/1 Running
Restarts:   0
Deployment: 1/1
```

Argo CD successfully created the Service and Deployment from the GitHub Helm chart using image build `29`.

This completed the GitOps CD portion of the project.

---

# Stage 12 — Prometheus and Grafana

For this project, the `kube-prometheus-stack` Helm chart was used to deploy the monitoring stack. This chart provides Prometheus, Grafana, and supporting Kubernetes monitoring components as an integrated package, rather than installing and configuring Prometheus and Grafana individually.

Helm also makes the monitoring stack easier to install, configure, upgrade, and reproduce.

The stack included:

```text
Prometheus
Grafana
Prometheus Operator
Alertmanager
kube-state-metrics
node-exporter
```

Prometheus collects and stores metrics.

Grafana queries and visualizes those metrics.

---

## Reconnect to AKS

```bash
az aks get-credentials \
  --resource-group johnson-busuyi-RG \
  --name cartflow-aks \
  --overwrite-existing
```

Verify:

```bash
kubectl get nodes
kubectl get applications -n argocd
```

The node was `Ready` and CartFlow remained:

```text
Synced
Healthy
```

---

## Create monitoring namespace

```bash
kubectl create namespace monitoring
kubectl get namespaces
```

Running `kubectl create namespace monitoring` again produced:

```text
AlreadyExists
```

which is harmless. `kubectl create` is not inherently idempotent for an already-existing object.

---

## Add Prometheus Community Helm repository

```bash
helm repo add prometheus-community \
  https://prometheus-community.github.io/helm-charts

helm repo update
helm repo list
```

Search:

```bash
helm search repo \
  prometheus-community/kube-prometheus-stack
```

Version used during the project:

```text
Chart: 92.1.0
Application: v0.94.1
```

---

## Check node resources before monitoring

```bash
kubectl top nodes
```

Observed before installation:

```text
CPU: approximately 124m / 6%
Memory: approximately 3144 MiB / 54%
```

---

# Install kube-prometheus-stack

```bash
helm install monitoring \
  prometheus-community/kube-prometheus-stack \
  --namespace monitoring
```

The first Helm client operation was interrupted/canceled.

A second `helm install` returned:

```text
cannot reuse a name that is still in use
```

`helm list -n monitoring` showed the release as failed, and:

```bash
helm status monitoring -n monitoring
```

reported:

```text
Release "monitoring" failed: context canceled
```

However, inspecting Kubernetes showed that the submitted resources had continued to be created and were healthy.

This is an important Helm lesson:

> A canceled Helm client operation does not necessarily mean Kubernetes stopped processing resources that had already been submitted.

Before deleting everything, the cluster was inspected.

Pods included healthy instances of:

```text
Alertmanager
Grafana
Prometheus Operator
kube-state-metrics
node-exporter
Prometheus
```

---

# Recover the Helm Release

Instead of uninstalling healthy resources, the existing release was reconciled:

```bash
helm upgrade monitoring \
  prometheus-community/kube-prometheus-stack \
  --namespace monitoring
```

Result:

```text
Release "monitoring" has been upgraded.
STATUS: deployed
REVISION: 2
```

This preserved the working resources and repaired the Helm release state.

### Helm lesson

Use:

```text
helm install
```

for a new release.

Use:

```text
helm upgrade
```

for an existing release.

A failed release can still reserve its release name.

---

# Verify Monitoring

```bash
helm list -n monitoring
kubectl get pods -n monitoring
kubectl top nodes
```

All major monitoring pods reached `Running`.

After installation, one `kubectl top nodes` observation showed approximately:

```text
CPU: 178m / 9%
Memory: 4374 MiB / 75%
```

Compared with the earlier measurement, the monitoring stack added meaningful memory overhead, which is important on a one-node lab cluster.

---

# Grafana Credentials

Grafana's generated administrator password can be retrieved when needed:

```bash
kubectl get secret monitoring-grafana \
  -n monitoring \
  -o jsonpath="{.data.admin-password}" \
  | base64 --decode; echo
```

Username:

```text
admin
```

The actual password is **not stored in this README or repository**.

---

# Grafana Access Troubleshooting

Grafana initially used a private `ClusterIP`.

Port-forwarding was attempted:

```bash
kubectl port-forward \
  -n monitoring \
  svc/monitoring-grafana \
  3000:80
```

The port-forward itself worked, but Azure Cloud Shell Web Preview again introduced browser authentication/session problems.

For temporary lab verification only, Grafana was changed to a public Azure LoadBalancer:

```bash
kubectl patch svc monitoring-grafana \
  -n monitoring \
  -p '{"spec":{"type":"LoadBalancer"}}'
```

Then:

```bash
kubectl get svc monitoring-grafana -n monitoring
```

provided a temporary external IP.

Grafana was opened over HTTP for this short validation exercise. A browser "Not secure" warning was expected because TLS had not been configured for the temporary endpoint.

No NSG weakening was required.

---

# Grafana Dashboard Validation

The preinstalled Kubernetes dashboards included views such as:

```text
Kubernetes / Compute Resources / Cluster
Kubernetes / Compute Resources / Namespace (Pods)
Kubernetes / Compute Resources / Node (Pods)
Nodes Overview
Pod
Workload
```

The namespace dashboard was filtered to:

```text
cartflow
```

and displayed the running CartFlow pod and its CPU/memory metrics.

CartFlow memory was observed around 114–116 MiB during the dashboard review.

Some utilization panels displayed:

```text
No data
```

because the CartFlow Helm chart did not yet define CPU/memory requests and limits. Percentage-of-request or percentage-of-limit metrics have no denominator when those resource values do not exist.

This is a future improvement, not a Prometheus failure.

---

# Infrastructure Metrics versus Application Metrics

The monitoring implemented here is primarily **Kubernetes/infrastructure monitoring**.

Prometheus can observe:

```text
Node CPU/memory
Pod CPU/memory
Deployments
Namespaces
Kubernetes object state
Container metrics
```

CartFlow itself currently does **not** expose a Prometheus `/metrics` endpoint.

Therefore application-specific metrics such as:

```text
HTTP request count
Request latency
Error rate
Business transaction metrics
```

would require adding application instrumentation and a Prometheus `ServiceMonitor` or equivalent scrape configuration.

---

# Remove Temporary Public Grafana Access

After monitoring was verified, Grafana was returned to private access:

```bash
kubectl patch svc monitoring-grafana \
  -n monitoring \
  -p '{"spec":{"type":"ClusterIP"}}'
```

Verify:

```bash
kubectl get svc monitoring-grafana -n monitoring
```

Final state:

```text
TYPE: ClusterIP
EXTERNAL-IP: <none>
```

This was an important security cleanup step. A temporary troubleshooting exposure was not left open after testing.

---

# Final Azure DevOps Pipeline

The final CI pipeline is stored in:

```text
azure-pipelines.yml
```

It implements:

```text
1. Checkout source
2. Verify tools
3. Prepare SonarQube
4. Remove unsupported Community branch parameter
5. Maven build + JUnit + SonarQube
6. Publish SonarQube Quality Gate
7. OWASP Dependency-Check
8. Verify OWASP report
9. Build Docker image
10. Trivy security scan
11. Push approved image to ACR
12. Publish OWASP report
13. Publish CartFlow JAR artifacts
```

Exact pipeline:

```yaml
trigger:
- main

pool:
  name: 'CartFlow-Agent-Pool'

steps:

# =========================================================
# 1. CHECKOUT SOURCE CODE
# =========================================================
- checkout: self
  displayName: 'Checkout CartFlow Source Code'


# =========================================================
# 2. VERIFY BUILD TOOLS
# =========================================================
- bash: |
    echo "Checking build environment..."

    echo "Java:"
    java -version

    echo "Maven:"
    mvn -version

    echo "Git:"
    git --version

    echo "Docker:"
    docker --version

    echo "Trivy:"
    trivy --version
  displayName: 'Verify Build Tools'


# =========================================================
# 3. PREPARE SONARQUBE ANALYSIS
# =========================================================
- task: SonarQubePrepare@8
  displayName: 'Prepare SonarQube Analysis'
  inputs:
    SonarQube: 'CartFlow-SonarQube'
    scannerMode: 'other'
    extraProperties: |
      sonar.projectKey=CartFlow
      sonar.projectName=CartFlow


# =========================================================
# 4. REMOVE UNSUPPORTED SONARQUBE BRANCH PARAMETER
# =========================================================
- bash: |
    echo "Removing sonar.branch.name for SonarQube Community Build..."

    CLEAN_PARAMS=$(echo "$SONARQUBE_SCANNER_PARAMS" | \
      sed -E 's/,"sonar.branch.name":"[^"]*"//g')

    echo "##vso[task.setvariable variable=SONARQUBE_SCANNER_PARAMS]$CLEAN_PARAMS"

    echo "Branch parameter removed."
  displayName: 'Remove SonarQube Branch Parameter'


# =========================================================
# 5. MAVEN BUILD + UNIT TESTS + SONARQUBE
# =========================================================
- task: Maven@4
  displayName: 'Maven Build, Test and SonarQube Analysis'
  inputs:
    mavenPomFile: 'pom.xml'
    goals: 'clean package'

    publishJUnitResults: true
    testResultsFiles: '**/surefire-reports/TEST-*.xml'

    javaHomeOption: 'Path'
    jdkDirectory: '/usr/lib/jvm/java-17-openjdk-amd64'

    mavenVersionOption: 'Default'

    sonarQubeRunAnalysis: true


# =========================================================
# 6. PUBLISH SONARQUBE QUALITY GATE
# =========================================================
- task: SonarQubePublish@8
  displayName: 'Publish SonarQube Quality Gate'
  inputs:
    pollingTimeoutSec: '300'


# =========================================================
# 7. OWASP DEPENDENCY-CHECK
# =========================================================
- bash: |
    echo "Starting OWASP Dependency-Check..."

    mvn org.owasp:dependency-check-maven:check \
      -DnvdApiKey="$NVD_API_KEY"

    echo "OWASP Dependency-Check completed."
  displayName: 'OWASP Dependency-Check'
  env:
    NVD_API_KEY: $(NVD_API_KEY)


# =========================================================
# 8. VERIFY OWASP REPORT
# =========================================================
- bash: |
    echo "Checking Dependency-Check report..."

    if [ -f "target/dependency-check-report.html" ]; then
      echo "Dependency-Check report generated successfully."
      ls -lh target/dependency-check-report.html
    else
      echo "ERROR: Dependency-Check report was not generated."
      exit 1
    fi
  displayName: 'Verify OWASP Report'


# =========================================================
# 9. BUILD DOCKER IMAGE FOR ACR
# =========================================================
- task: Docker@2
  displayName: 'Build CartFlow Docker Image'
  inputs:
    command: 'build'
    containerRegistry: 'CartFlow-ACR'
    repository: 'cartflow'
    Dockerfile: '$(Build.SourcesDirectory)/Dockerfile'
    buildContext: '$(Build.SourcesDirectory)'
    tags: |
      $(Build.BuildId)
      latest


# =========================================================
# 10. TRIVY CONTAINER SECURITY SCAN
# =========================================================
- bash: |
    echo "Scanning CartFlow Docker image with Trivy..."

    trivy image \
      --scanners vuln \
      --severity HIGH,CRITICAL \
      --exit-code 1 \
      cartflowregistry.azurecr.io/cartflow:$(Build.BuildId)

    echo "Trivy security scan passed."
  displayName: 'Trivy Container Security Scan'


# =========================================================
# 11. PUSH APPROVED IMAGE TO ACR
# =========================================================
- task: Docker@2
  displayName: 'Push CartFlow Image to ACR'
  inputs:
    command: 'push'
    containerRegistry: 'CartFlow-ACR'
    repository: 'cartflow'
    tags: |
      $(Build.BuildId)
      latest


# =========================================================
# 12. PUBLISH OWASP REPORT
# =========================================================
- task: PublishPipelineArtifact@1
  displayName: 'Publish OWASP Dependency-Check Report'
  inputs:
    targetPath: '$(System.DefaultWorkingDirectory)/target/dependency-check-report.html'
    artifact: 'OWASP-Dependency-Check-Report'


# =========================================================
# 13. PUBLISH CARTFLOW BUILD ARTIFACTS
# =========================================================
- task: PublishPipelineArtifact@1
  displayName: 'Publish CartFlow JAR'
  inputs:
    targetPath: '$(System.DefaultWorkingDirectory)/target'
    artifact: 'CartFlow'
```

---

# Repository Structure

The final source-controlled project contains the major files:

```text
CartFlow/
│
├── .gitignore
├── README.md
├── pom.xml
├── Dockerfile
├── azure-pipelines.yml
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── cartflow/
│   │               └── CartFlow.java
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── cartflow/
│                   └── CartFlowTest.java
│
├── cartflow-chart/
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
│       ├── deployment.yaml
│       └── service.yaml
│
├── argocd/
│   └── cartflow-argocd.yaml
│
└── monitoring/
    └── README.md
```

Generated `target/` artifacts are ignored by Git.

---

# Security Practices

Security was integrated throughout the lifecycle rather than added only after deployment.

### Source control

- Secrets are not committed.
- `.gitignore` excludes generated build output.
- Git status/diffs are reviewed before commits.
- Force push was avoided during divergence troubleshooting.

### CI

- NVD API key stored as Azure DevOps secret variable.
- SonarQube performs source quality/security analysis.
- OWASP Dependency-Check scans third-party dependencies.
- Trivy scans the container before ACR publication.
- HIGH/CRITICAL Trivy findings fail the pipeline.

### Azure

- Azure DevOps ACR connection uses Workload Identity federation.
- Microsoft Entra Security Defaults were not disabled to work around CLI login.
- AKS OIDC and Workload Identity were enabled.
- ACR was integrated with AKS.

### Kubernetes/GitOps

- Argo CD repository credential stored in a Kubernetes Secret.
- GitHub PAT is not stored in Git.
- A disclosed test token was revoked immediately.
- Argo CD server remained private `ClusterIP`.
- Grafana was returned to `ClusterIP` after temporary testing.
- Health probes monitor application availability.

### Image management

- Build-specific image tags provide traceability.
- `latest` is published for convenience, but immutable build IDs are preferred for deployments.

---

# Secret Scan Before Commit

A useful repository check used during the project was:

```bash
grep -RniE \
  --exclude-dir=.git \
  --exclude-dir=target \
  'github_pat_|ghp_|NVD_API_KEY|SONAR_TOKEN|admin-password|password[[:space:]]*[:=]|token[[:space:]]*[:=]' \
  .
```

Expected legitimate references such as:

```text
NVD_API_KEY: $(NVD_API_KEY)
```

may appear because they reference secret variable **names**, not secret values.

Always inspect the output before committing.

---

# Cost Management

This is a lab/portfolio environment, so cost management matters.

Measures included:

- A single-node AKS cluster.
- Basic ACR tier.
- Self-hosted VM deallocation when CI/SonarQube are not needed.
- Avoiding unnecessary managed monitoring products because Prometheus/Grafana were part of the learning objective.
- Temporary public LoadBalancers removed when no longer required.
- Monitoring node resource usage after installing `kube-prometheus-stack`.

Remember that stopping the CI VM makes the self-hosted Azure DevOps agent and VM-hosted SonarQube unavailable until the VM starts again.

---

# Major Troubleshooting Lessons

## 1. SonarQube rejected branch analysis

**Problem**

Azure DevOps injected:

```text
sonar.branch.name=main
```

Community Build did not support it.

**Fix**

Remove the parameter from `SONARQUBE_SCANNER_PARAMS` before Maven analysis.

---

## 2. OWASP NVD API requirement

**Problem**

Dependency-Check could not reliably update NVD data without an API key.

**Fix**

Create an NVD key and store it as the Azure DevOps secret variable:

```text
NVD_API_KEY
```

Never commit the key.

---

## 3. OWASP scan exhausted VM resources

**Problem**

Initial NVD processing placed too much pressure on the small CI VM.

**Fix**

Resize the VM to approximately 8 GiB RAM.

**Lesson**

Security scanning has compute and memory requirements that must be included in CI sizing.

---

## 4. Dockerfile not found

**Problem**

Dockerfile was not present in the GitHub checkout.

**Fix**

Commit and push the Dockerfile.

**Lesson**

The pipeline source of truth is the repository, not an engineer's local or stale agent workspace.

---

## 5. Docker/ACR tag mismatch

**Problem**

The built image tag differed from the tag expected during push.

**Fix**

Use consistent tags for both Docker build and push:

```text
$(Build.BuildId)
latest
```

---

## 6. Azure CLI blocked by Security Defaults

**Problem**

Device login returned:

```text
AADSTS530035
```

**Fix**

Use Azure Cloud Shell.

**Security lesson**

Do not disable tenant security controls merely to make a lab login easier.

---

## 7. Kubernetes pod repeatedly Completed

**Problem**

CartFlow build 28 was a CLI application that exited normally.

**Diagnosis**

Logs were healthy and exit behavior was expected for a CLI process.

**Fix**

Convert the workload to a long-running HTTP server and deploy build 29.

---

## 8. Helm lint nil-pointer failure

**Problem**

Unused generated templates referenced removed values.

**Fix**

Remove unnecessary `serviceaccount`, ingress, HPA, HTTPRoute, NOTES, and test templates.

Then:

```bash
helm lint cartflow-chart
helm template cartflow cartflow-chart
```

---

## 9. Accidental root `values.yaml`

**Problem**

A heredoc command was entered incorrectly and created an unintended file.

**Detection**

```bash
git status
```

**Fix**

```bash
rm values.yaml
```

**Lesson**

Always inspect untracked/modified files before `git add`.

---

## 10. Git push rejected / branches diverged

**Problem**

GitHub contained commits not present locally.

**Fix**

```bash
git fetch origin
git status
git pull --rebase origin main
git push origin main
```

**Lesson**

Rebase safely integrated remote changes without force-pushing.

---

## 11. Argo CD private repository authentication

**Problem**

Application state showed a repository authentication `ComparisonError`.

**Fix**

Create a properly labeled Argo CD repository Secret containing the private-repository credential.

---

## 12. GitHub authentication versus authorization

Authentication proves who the client is.

Authorization determines what that identity is permitted to do.

After authentication was fixed, GitHub returned a separate authorization error. Adjusting repository permission allowed the synchronization to complete.

Least privilege should be applied after confirming the minimum required access.

---

## 13. Credential accidentally exposed

**Problem**

A PAT was accidentally included while entering an interactive shell command.

**Fix**

Immediately revoke the token and replace it.

**Lesson**

A credential should be treated as compromised once exposed, even if the exposure seems brief.

---

## 14. Argo CD UI Web Preview issues

**Problem**

Cloud Shell Web Preview had unsupported ports, authentication/session changes, and local-port conflicts.

**Fix**

Use declarative Argo CD resources and `kubectl` rather than exposing the administrative service.

---

## 15. Helm monitoring installation canceled

**Problem**

The first Helm install ended with:

```text
context canceled
```

A second install returned:

```text
cannot reuse a name that is still in use
```

**Diagnosis**

The Helm release was recorded as failed, but Kubernetes resources were already running.

**Fix**

Inspect before deleting:

```bash
helm status monitoring -n monitoring
kubectl get pods -n monitoring
```

Then repair:

```bash
helm upgrade monitoring \
  prometheus-community/kube-prometheus-stack \
  --namespace monitoring
```

Release became:

```text
STATUS: deployed
REVISION: 2
```

---

## 16. Grafana Web Preview problem

**Problem**

Port-forward worked, but Cloud Shell Web Preview authentication prevented convenient browser access.

**Temporary fix**

Change Grafana Service to `LoadBalancer`, validate dashboards, then immediately return it to:

```text
ClusterIP
```

This preserved the desired secure final state.

---

## 17. Grafana showed `No data` for some utilization panels

**Cause**

CartFlow does not yet specify CPU/memory requests and limits.

Metrics based on:

```text
actual usage / resource request
```

or:

```text
actual usage / resource limit
```

cannot calculate meaningful percentages without the denominator.

**Future fix**

Add resource requests/limits to the Helm chart.

---

# Verification Checklist

## Local application

```bash
mvn clean test
mvn package
```

Expected:

```text
BUILD SUCCESS
2 tests pass
```

## Docker

```bash
docker build -t cartflow:test .
docker run --rm -p 8080:8080 cartflow:test
```

Then:

```bash
curl http://localhost:8080/
curl http://localhost:8080/health
```

## AKS

```bash
kubectl get nodes
kubectl get pods -n cartflow
kubectl get svc -n cartflow
```

Expected workload:

```text
Pod 1/1 Running
Deployment 1/1
```

## Argo CD

```bash
kubectl get applications -n argocd
```

Expected:

```text
cartflow   Synced   Healthy
```

## Monitoring

```bash
helm list -n monitoring
kubectl get pods -n monitoring
kubectl top nodes
```

Expected:

```text
monitoring release deployed
Prometheus/Grafana supporting pods Running
```

## Grafana final security state

```bash
kubectl get svc monitoring-grafana -n monitoring
```

Expected:

```text
TYPE          ClusterIP
EXTERNAL-IP   <none>
```

---

# Future Improvements

The project is functional, but several enhancements would make it closer to a production environment.

### 1. Add Kubernetes resource requests and limits

Example:

```yaml
resources:
  requests:
    cpu: 100m
    memory: 128Mi
  limits:
    cpu: 500m
    memory: 256Mi
```

This would improve scheduling and allow Grafana resource-utilization dashboards to calculate request/limit percentages.

### 2. Add application Prometheus metrics

Expose:

```text
/metrics
```

with metrics such as:

```text
HTTP request count
HTTP error rate
Request latency
Health status
```

Then add a Prometheus `ServiceMonitor`.

### 3. Add HTTPS/TLS

Use an ingress controller and certificate management rather than exposing the application only over HTTP.

### 4. Reduce Argo repository permissions

Confirm the minimum fine-grained GitHub permission required for this private repository and reduce the credential to read-only access if supported by the final repository configuration.

### 5. Add image-tag automation

The CI pipeline currently pushes a new build ID, while the Helm chart contains an explicit image tag.

A future design could automatically update the Helm values through a controlled Git commit/PR, or use an approved image-update workflow. Argo CD would then detect the Git change and deploy it.

### 6. Add Infrastructure as Code

Provision ACR, AKS, VM, networking, and related Azure resources with Terraform or Bicep.

### 7. Add autoscaling

Introduce:

```text
Horizontal Pod Autoscaler
AKS node autoscaling
```

where appropriate.

### 8. Add alerting

Configure Prometheus Alertmanager rules for:

```text
Pod failures
High CPU
High memory
Deployment unavailable
Node pressure
```

### 9. Persistent SonarQube configuration

Persist required kernel settings and use persistent SonarQube storage/database for a production-style installation.

---

# Interview Talking Points

## Explain the project in 30 seconds

> I built an end-to-end DevSecOps project around a Java application called CartFlow. Source code is stored in GitHub and Azure DevOps performs CI using a self-hosted Linux agent. Maven builds and tests the application, SonarQube performs static analysis, OWASP Dependency-Check scans dependencies, Docker packages the application, and Trivy scans the image before it is pushed to Azure Container Registry. The application runs on AKS. Kubernetes resources are packaged with Helm and Argo CD provides GitOps-based continuous delivery from GitHub. Prometheus and Grafana provide Kubernetes monitoring and visualization.

---

## What was your role as a DevOps engineer?

> In a normal organization, application source code usually comes from a development team. My DevOps responsibility is to integrate that code into a reliable build, security, containerization, deployment, and monitoring workflow. For this portfolio project I created a deliberately simple Java application so I could demonstrate the complete DevSecOps lifecycle without making application development the focus.

---

## Why Azure DevOps and Argo CD?

> They serve different purposes. Azure DevOps handles CI: compile, test, analyze, scan, containerize, and publish. Argo CD handles GitOps continuous delivery: it watches the desired Kubernetes configuration in Git and reconciles AKS to match it.

---

## Why Helm?

> Helm provides reusable, parameterized Kubernetes packaging. Instead of maintaining duplicated static YAML for every environment, values such as image tag, replica count, service type, ports, and probes can be controlled through chart values.

---

## Why scan with Trivy before ACR push?

> I wanted the registry to receive only images that passed the configured HIGH/CRITICAL vulnerability gate. The pipeline builds first, scans second, and pushes only after the scan succeeds.

---

## What was the most important Kubernetes troubleshooting issue?

> My first container was a CLI application. Kubernetes kept restarting it even though the logs were healthy because the process exited successfully. A Deployment expects a long-running workload. I converted CartFlow to an HTTP service on port 8080, added `/health`, rebuilt it through CI, deployed the new immutable image tag, and the pod remained 1/1 Running with zero restarts.

---

## What is the difference between readiness and liveness?

> Readiness determines whether a pod should receive Service traffic. Liveness determines whether the application is still healthy enough to keep running; repeated liveness failures can cause Kubernetes to restart the container.

---

## What does `Synced Healthy` mean in Argo CD?

> `Synced` means the live AKS state matches the desired Git state. `Healthy` means the deployed Kubernetes resources are operating successfully.

---

## What security controls did you implement?

> I kept secrets out of Git, used Azure DevOps secret variables for the NVD API key, used Workload Identity federation for the ACR service connection, ran SonarQube and OWASP analysis, scanned Docker images with Trivy before registry publication, used immutable build tags for deployment, kept Argo CD private, returned Grafana to ClusterIP after testing, and did not disable Microsoft Entra Security Defaults when Azure CLI authentication was blocked.

---

## How did you respond to an accidentally exposed token?

> I treated it as compromised, revoked it immediately, generated a replacement, and ensured the credential was not committed to Git. That is the correct incident response even if exposure is brief.

---

## Why did the monitoring Helm release say failed while pods were running?

> The Helm client operation was canceled after some Kubernetes resources had already been submitted. Kubernetes continued creating them even though Helm recorded the release as failed. I inspected the resources before deleting anything, confirmed they were healthy, and used `helm upgrade` to reconcile the existing release. It became deployed at revision 2.

---

## Why did Grafana show `No data` in some panels?

> The basic Kubernetes metrics existed, but CartFlow did not define CPU and memory requests or limits. Dashboards that calculate utilization as a percentage of requests or limits therefore had no denominator. The fix is to add resource requests/limits to the Helm chart.

---

## Infrastructure monitoring versus application monitoring

> The current Prometheus stack monitors Kubernetes infrastructure and pod resources. CartFlow does not yet expose application-specific Prometheus metrics. A future version would instrument the HTTP service, expose `/metrics`, and add a ServiceMonitor for request count, latency, and error rate.

---

# What This Project Demonstrates

CartFlow demonstrates practical experience with:

```text
Git and GitHub
Java/Maven build automation
JUnit testing
Azure DevOps pipelines
Self-hosted CI agents
SonarQube
OWASP Dependency-Check
Docker
Trivy
Azure Container Registry
Azure Kubernetes Service
kubectl
Kubernetes Deployments and Services
Readiness/liveness probes
Azure Load Balancers
Helm
Argo CD
GitOps
Private repository authentication
Prometheus
Grafana
Cloud troubleshooting
Secret management
Security gates
Cost awareness
Operational troubleshooting
```

The most valuable part of the project is not that every step worked immediately. The failures exposed real DevOps concepts: CI source-of-truth problems, security scanner resource requirements, identity restrictions, container lifecycle behavior, Helm ownership and state, Git divergence, private repository authentication, credential handling, GitOps reconciliation, monitoring overhead, and secure cleanup after temporary troubleshooting.

That is the practical DevSecOps lifecycle demonstrated by CartFlow.

---

## Author

**Busuyi Johnson**

DevOps / Cloud / Kubernetes / DevSecOps Portfolio Project

---

## Disclaimer

CartFlow is a hands-on portfolio/lab project. Public endpoints, temporary IP addresses, credentials, and lab resource configurations should not be treated as production defaults. Production deployments should include organization-specific identity controls, TLS, network restrictions, backup/recovery, resource policies, high availability, secret-management standards, and governance.

